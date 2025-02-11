package slimeknights.tconstruct.gadgets;

import com.google.common.collect.Lists;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import slimeknights.tconstruct.gadgets.entity.EFLNExplosion;
import slimeknights.tconstruct.library.network.TinkerNetwork;
import slimeknights.tconstruct.tools.common.network.EntityMovementChangePacket;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public class Exploder {

  public final double r;
  private final double rr;
  public final int dist;
  private final double explosionStrength;
  private final int blocksPerIteration;
  public final int x, y, z;
  public final World world;
  private final Entity exploder;
  private final EFLNExplosion explosion;

  private int currentRadius;
  private int curX, curY, curZ;

  private List<ItemStack> droppedItems; // map containing all items dropped by the explosion and their amounts
  private boolean finished;

  public Exploder(World world, EFLNExplosion explosion, Entity exploder, BlockPos location, double r, double explosionStrength, int blocksPerIteration) {
    this.r = r;
    this.world = world;
    this.explosion = explosion;
    this.exploder = exploder;
    this.rr = r * r;
    this.dist = (int) r + 1;
    this.explosionStrength = explosionStrength;
    this.blocksPerIteration = blocksPerIteration;
    this.currentRadius = 0;

    this.x = location.getX();
    this.y = location.getY();
    this.z = location.getZ();

    this.curX = 0;
    this.curY = 0;
    this.curZ = 0;

    this.droppedItems = Lists.newArrayList();

    ServerTickEvents.END_WORLD_TICK.register(this::onTick);
  }

  public static void startExplosion(World world, EFLNExplosion explosion, Entity entity, BlockPos location, double r, double explosionStrength) {
    Exploder exploder = new Exploder(world, explosion, entity, location, r, explosionStrength, Math.max(50, (int) (r * r * r / 10d)));
    exploder.handleEntities();
    world.playSound(null, location, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F, (1.0F + (world.random.nextFloat() - world.random.nextFloat()) * 0.2F) * 0.7F);
  }

  private void handleEntities() {
    final Predicate<Entity> predicate = entity -> entity != null
      && !entity.isImmuneToExplosion()
      && EntityPredicates.EXCEPT_SPECTATOR.test(entity)
      && EntityPredicates.VALID_ENTITY.test(entity)
      && entity.getPos().squaredDistanceTo(this.x, this.y, this.z) <= this.r * this.r;

    // damage and blast back entities
    List<Entity> list = this.world.getOtherEntities(this.exploder,
      new Box(this.x - this.r - 1,
        this.y - this.r - 1,
        this.z - this.r - 1,
        this.x + this.r + 1,
        this.y + this.r + 1,
        this.z + this.r + 1),
      predicate
    );

    for (Entity entity : list) {
      // move it away from the center depending on distance and explosion strength
      Vec3d dir = entity.getPos().subtract(this.exploder.getPos().add(0, -this.r / 2, 0));
      double str = (this.r - dir.length()) / this.r;
      str = Math.max(0.3, str);
      dir = dir.normalize();
      dir = dir.multiply(this.explosionStrength * str * 0.3);
      entity.addVelocity(dir.x, dir.y + 0.5, dir.z);
      entity.damage(DamageSource.explosion(this.explosion), (float) (str * this.explosionStrength));

      if (entity instanceof ServerPlayerEntity) {
        TinkerNetwork.getInstance().sendTo(new EntityMovementChangePacket(entity), (ServerPlayerEntity) entity);
      }
    }
  }

  public void onTick(ServerWorld world) {
    if (world == this.world && !finished) {
      if (!this.iteration()) {
        // goodbye world, we're done exploding
        this.finish();
        finished = true;
      }
    }
  }

  private void finish() {
    final int d = (int) this.r / 2;
    final BlockPos pos = new BlockPos(this.x - d, this.y - d, this.z - d);
    final Random random = new Random();

    List<ItemStack> aggregatedDrops = Lists.newArrayList();

    for (ItemStack drop : this.droppedItems) {
      boolean notInList = true;

      // check if it's already in our list
      for (ItemStack stack : aggregatedDrops) {
        if (ItemStack.areItemsEqualIgnoreDamage(drop, stack) && ItemStack.areTagsEqual(drop, stack)) {
          stack.increment(drop.getCount());
          notInList = false;
          break;
        }
      }

      if (notInList) {
        aggregatedDrops.add(drop);
      }
    }

    // actually drop the aggregated items
    for (ItemStack drop : aggregatedDrops) {
      int stacksize = drop.getCount();
      do {
        BlockPos spawnPos = pos.add(random.nextInt((int) this.r), random.nextInt((int) this.r), random.nextInt((int) this.r));
        ItemStack dropItemstack = drop.copy();
        dropItemstack.setCount(Math.min(stacksize, 64));
        Block.dropStack(this.world, spawnPos, dropItemstack);
        stacksize -= dropItemstack.getCount();
      }
      while (stacksize > 0);
    }
  }

  /**
   * Explodes away all blocks for the current iteration
   */
  private boolean iteration() {
    int count = 0;

    this.explosion.clearAffectedBlocks();

    while (count < this.blocksPerIteration && this.currentRadius < (int) this.r + 1) {
      double d = this.curX * this.curX + this.curY * this.curY + this.curZ * this.curZ;
      // inside the explosion?
      if (d <= this.rr) {
        BlockPos blockpos = new BlockPos(this.x + this.curX, this.y + this.curY, this.z + this.curZ);
        BlockState blockState = this.world.getBlockState(blockpos);
        FluidState ifluidstate = this.world.getFluidState(blockpos);

        // no air blocks
        if (!this.world.getBlockState(blockpos).isAir() || !ifluidstate.isEmpty()) {
          // explosion "strength" at the current position
          double f = this.explosionStrength * (1f - d / this.rr);

          float f2 = Math.max(blockState.getBlock().getBlastResistance(), ifluidstate.getBlockState().getBlock().getBlastResistance());
          if (this.exploder != null) {
            f2 = this.exploder.getEffectiveExplosionResistance(this.explosion, this.world, blockpos, blockState, ifluidstate, f2);
          }

          f -= (f2 + 0.3F) * 0.3F;

          if (f > 0.0F && (this.exploder == null || this.exploder.canExplosionDestroyBlock(this.explosion, this.world, blockpos, blockState, (float) f))) {
            // block should be exploded
            count++;
            this.explosion.addAffectedBlock(blockpos);
          }
        }
      }
      // get next coordinate;
      this.step();
    }

    this.explosion.getAffectedBlocks().forEach(this::explodeBlock);

    return count == this.blocksPerIteration; // can lead to 1 more call where nothing is done, but that's ok
  }

  // get the next coordinate
  private void step() {
    // we go X/Z plane wise from top to bottom
    if (++this.curX > this.currentRadius) {
      this.curX = -this.currentRadius;
      if (++this.curZ > this.currentRadius) {
        this.curZ = -this.currentRadius;
        if (--this.curY < -this.currentRadius) {
          this.currentRadius++;
          this.curX = this.curZ = -this.currentRadius;
          this.curY = this.currentRadius;
        }
      }
    }
    // we skip the internals
    if (this.curY != -this.currentRadius && this.curY != this.currentRadius) {
      // we're not in the top or bottom plane
      if (this.curZ != -this.currentRadius && this.curZ != this.currentRadius) {
        // we're not in the X/Y planes of the cube, we can therefore skip the x to the end if we're inside
        if (this.curX > -this.currentRadius) {
          this.curX = this.currentRadius;
        }
      }
    }
  }

  private void explodeBlock(BlockPos blockpos) {
    BlockState blockstate = this.world.getBlockState(blockpos);
    boolean isBlockEntity = this.world.getBlockEntity(blockpos) != null;

    if (!this.world.isClient && world.getGameRules().getBoolean(GameRules.DO_TILE_DROPS)) {
      BlockEntity tileentity = isBlockEntity ? this.world.getBlockEntity(blockpos) : null;
      LootContext.Builder builder = (new LootContext.Builder((ServerWorld) this.world)).random(this.world.random).parameter(LootContextParameters.ORIGIN, Vec3d.ofCenter(blockpos)).parameter(LootContextParameters.TOOL, ItemStack.EMPTY).optionalParameter(LootContextParameters.BLOCK_ENTITY, tileentity);

      this.droppedItems.addAll(blockstate.getDroppedStacks(builder));
    }

    if (this.world instanceof ServerWorld) {
      for (ServerPlayerEntity serverplayerentity : ((ServerWorld) this.world).getPlayers()) {
        ((ServerWorld) this.world).spawnParticles(serverplayerentity, ParticleTypes.POOF, true, blockpos.getX(), blockpos.getY(), blockpos.getZ(), 2, 0, 0, 0, 0d);
        ((ServerWorld) this.world).spawnParticles(serverplayerentity, ParticleTypes.SMOKE, true, blockpos.getX(), blockpos.getY(), blockpos.getZ(), 1, 0, 0, 0, 0d);
      }
    }

    world.setBlockState(blockpos, Blocks.AIR.getDefaultState(), 3);
    blockstate.getBlock().onDestroyedByExplosion(world, blockpos, explosion);
  }

}
