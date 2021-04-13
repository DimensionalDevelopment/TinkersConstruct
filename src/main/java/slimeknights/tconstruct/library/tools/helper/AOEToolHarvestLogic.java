package slimeknights.tconstruct.library.tools.helper;

import com.google.common.collect.ImmutableList;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.Material;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.tag.Tag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.tools.item.ToolCore;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.tools.TinkerModifiers;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class AOEToolHarvestLogic extends ToolHarvestLogic {
  /** Instance for an AOE tool that only works on a single block, extended by modifiers */
  public static final AOEToolHarvestLogic SMALL_TOOL = new AOEToolHarvestLogic(1, 1, 1);

  /** Instance for an AOE tool like a hammer or a excavator */
  public static final AOEToolHarvestLogic LARGE_TOOL = new AOEToolHarvestLogic(3, 3, 1);

  protected final int width;
  protected final int height;
  protected final int depth;

  public AOEToolHarvestLogic(int width, int height, int depth) {
    this.width = width;
    this.height = height;
    this.depth = depth;
  }

  @Override
  public final List<BlockPos> getAOEBlocks(ToolStack tool, ItemStack stack, World world, PlayerEntity player, BlockPos origin) {
    // only works with modifiable harvest
    if (stack.isEmpty() || tool.isBroken()) {
      return Collections.emptyList();
    }

    // air will break the raytrace
    BlockState state = world.getBlockState(origin);
    if (state.getMaterial() == Material.AIR) {
      return Collections.emptyList();
    }

    // if unharvestable, skip
    if (!isEffective(tool, stack, state)) {
      return Collections.emptyList();
    }

    // raytrace to get the side, but has to result in the same block
    BlockHitResult mop = ToolCore.blockRayTrace(world, player, RaycastContext.FluidHandling.ANY);
    if (!origin.equals(mop.getBlockPos())) {
      mop = ToolCore.blockRayTrace(world, player, RaycastContext.FluidHandling.NONE);
      if (!origin.equals(mop.getBlockPos())) {
        return Collections.emptyList();
      }
    }

    // return default method
    return getAOEBlocks(tool, player, origin, mop.getSide(), mop.getPos(), check -> isEffective(tool, stack, check));
  }

  /**
   * Gets a list of blocks the tool can affect, including side hit context
   * @param tool     tool stack
   * @param player   the player using the tool
   * @param origin   the origin block spot to start from
   * @param sideHit  Side that was hit
   * @param hitVec   Where the block was hit
   * @return A list of BlockPos's that the AOE tool can affect.
   */
  protected List<BlockPos> getAOEBlocks(ToolStack tool, PlayerEntity player, BlockPos origin, Direction sideHit, Vec3d hitVec, Predicate<BlockState> predicate) {
    // only works with modifiable harvest
    if (tool.isBroken()) {
      return Collections.emptyList();
    }

    // hardcoding expanders I suppose
    int expanded = tool.getModifierLevel(TinkerModifiers.expanded.get());
    return calculateAOEBlocks(player, origin, width + expanded, height + expanded, depth, sideHit, hitVec, predicate);
  }

  /**
   *  Calculates the blocks that the AOE tool can affect
   *
   * @param player Player instance
   * @param origin Origin Position
   * @param width The Width
   * @param height The Height
   * @param depth The Depth
   * @param sideHit  Side of the block hit
   * @param hitVec  hit vector
   * @return a list of BlockPoses
   */
  protected final List<BlockPos> calculateAOEBlocks(PlayerEntity player, BlockPos origin, int width, int height, int depth, Direction sideHit, Vec3d hitVec, Predicate<BlockState> predicate) {
    // we know the block and we know which side of the block we're hitting. time to calculate the depth along the different axes
    int x, y, z;
    BlockPos start = origin;
    int offset = sideHit.getDirection().offset();
    switch (sideHit.getAxis()) {
      case Y:
        // x y depends on the angle we look
        Vec3i vec = player.getHorizontalFacing().getVector();
        x = vec.getX() * height + vec.getZ() * width;
        y = offset * -depth;
        z = vec.getX() * width + vec.getZ() * height;
        start = start.add(-x / 2, 0, -z / 2);
        // for even numbers, offset based on where we hit
        if (x % 2 == 0) {
          if (x > 0 && hitVec.getX() - origin.getX() > 0.5d) {
            start = start.add(1, 0, 0);
          }
          else if (x < 0 && hitVec.getX() - origin.getX() < 0.5d) {
            start = start.add(-1, 0, 0);
          }
        }
        if (z % 2 == 0) {
          if (z > 0 && hitVec.getZ() - origin.getZ() > 0.5d) {
            start = start.add(0, 0, 1);
          }
          else if (z < 0 && hitVec.getZ() - origin.getZ() < 0.5d) {
            start = start.add(0, 0, -1);
          }
        }
        break;

      case Z:
        x = width;
        y = height;
        z = sideHit.getDirection().offset() * -depth;
        start = start.add(-x / 2, -y / 2, 0);
        // for even numbers, offset based on where we hit
        if (x % 2 == 0 && hitVec.getX() - origin.getX() > 0.5d) {
          start = start.add(1, 0, 0);
        }
        if (y % 2 == 0 && hitVec.getY() - origin.getY() > 0.5d) {
          start = start.add(0, 1, 0);
        }
        break;
      case X:
        x = offset * -depth;
        y = height;
        z = width;
        start = start.add(-0, -y / 2, -z / 2);
        // for even numbers, offset based on where we hit
        if (y % 2 == 0 && hitVec.getY() - origin.getY() > 0.5d) {
          start = start.add(0, 1, 0);
        }
        if (z % 2 == 0 && hitVec.getZ() - origin.getZ() > 0.5d) {
          start = start.add(0, 0, 1);
        }
        break;
      default:
        x = y = z = 0;
    }

    // start building the position list
    ImmutableList.Builder<BlockPos> builder = ImmutableList.builder();
    int endX = start.getX() + x, endY = start.getY() + y, endZ = start.getZ() + z;
    int offsetX = Util.sign(x), offsetY = Util.sign(y), offsetZ = Util.sign(z);
    for (int xp = start.getX(); xp != endX; xp += offsetX) {
      for (int yp = start.getY(); yp != endY; yp += offsetY) {
        for (int zp = start.getZ(); zp != endZ; zp += offsetZ) {
          // don't add the origin block
          if (xp == origin.getX() && yp == origin.getY() && zp == origin.getZ()) {
            continue;
          }

          // if checking distance, make sure the distance is not too far
          //          if (distance > 0 && MathHelper.abs(xp - origin.getX()) + MathHelper.abs(yp - origin.getY()) + MathHelper.abs(zp - origin.getZ()) > distance) {
          //            continue;
          //          }

          // if valid, add it
          BlockPos pos = new BlockPos(xp, yp, zp);
          if (predicate.test(player.getEntityWorld().getBlockState(pos))) {
            builder.add(pos);
          }
        }
      }
    }
    return builder.build();
  }

  /**
   * Tills blocks within an AOE area
   * @param context   Harvest context
   * @param toolType  Tool type used
   * @param sound     Sound to play on tilling
   * @return  Action result from tilling
   */
  public ActionResult transformBlocks(ItemUsageContext context, Tag toolType, SoundEvent sound, boolean requireGround) {
    PlayerEntity player = context.getPlayer();
    if (player == null || player.isSneaking()) {
      return ActionResult.PASS;
    }

    // for hoes and shovels, must have nothing but plants above
    World world = context.getWorld();
    BlockPos pos = context.getBlockPos();
    if (requireGround) {
      if (context.getSide() == Direction.DOWN) {
        return ActionResult.PASS;
      }
      Material material = world.getBlockState(pos.up()).getMaterial();
      if (!material.isReplaceable() && material != Material.PLANT) {
        return ActionResult.PASS;
      }
    }

    // tool must not be broken
    Hand hand = context.getHand();
    ItemStack stack = player.getStackInHand(hand);
    ToolStack tool = ToolStack.from(stack);
    if (tool.isBroken()) {
      return ActionResult.FAIL;
    }

    // must actually transform
    BlockState original = world.getBlockState(pos);
    BlockState transformed = original.getToolModifiedState(world, pos, player, stack, toolType);
    boolean isCampfire = false;
    boolean didTransform = transformed != null;
    if (transformed == null) {
      // shovel special case: campfires
      if (toolType == FabricToolTags.SHOVELS && original.getBlock() instanceof CampfireBlock && original.get(CampfireBlock.LIT)) {
        isCampfire = true;
        if (!world.isClient()) {
          world.syncWorldEvent(null, WorldEvents.FIRE_EXTINGUISH_SOUND, pos, 0);
          CampfireBlock.extinguish(world, pos, original);
        }
        transformed = original.with(CampfireBlock.LIT, false);
      } else {
        // try to match the clicked block
        transformed = world.getBlockState(pos);
      }
    }

    // if we made a successful transform, client can stop early
    EquipmentSlot slot = hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
    if (didTransform || isCampfire) {
      if (world.isClient()) {
        return ActionResult.SUCCESS;
      }

      // change the block state
      world.setBlockState(pos, transformed, Constants.BlockFlags.DEFAULT_AND_RERENDER);
      if (requireGround) {
        world.breakBlock(pos.up(), true);
      }

      // play sound
      if (!isCampfire) {
        world.playSound(null, pos, sound, SoundCategory.BLOCKS, 1.0F, 1.0F);
      }

      // damage the tool, if it breaks or if we changed a campfire, we are done
      if ((!player.isCreative() && ToolDamageUtil.damageAnimated(tool, 1, player, slot)) || isCampfire) {
        return ActionResult.SUCCESS;
      }
    }

    // AOE transforming, run even if we did not transform the center
    // note we consider anything effective, as hoes are not effective on all tillable blocks
    for (BlockPos newPos : getAOEBlocks(tool, player, pos, context.getSide(), context.getHitPos(), state -> true)) {
      if (pos.equals(newPos)) {
        //in case it attempts to run the same position twice
        continue;
      }

      // hoes and shovels: air or plants above
      BlockPos above = newPos.up();
      if (requireGround) {
        Material material = world.getBlockState(above).getMaterial();
        if (!material.isReplaceable() && material != Material.PLANT) {
          continue;
        }
      }

      // block type must be the same
      BlockState newState = world.getBlockState(newPos).getToolModifiedState(world, newPos, player, stack, toolType);
      if (newState != null && transformed.getBlock() == newState.getBlock()) {
        if (world.isClient()) {
          return ActionResult.SUCCESS;
        }
        didTransform = true;
        world.setBlockState(newPos, newState, Constants.BlockFlags.DEFAULT_AND_RERENDER);
        world.playSound(null, newPos, sound, SoundCategory.BLOCKS, 1.0F, 1.0F);

        // if required, break the block above (typically plants)
        if (requireGround) {
          world.breakBlock(above, true);
        }

        // stop if the tool broke
        if (!player.isCreative() && ToolDamageUtil.damageAnimated(tool, 1, player, slot)) {
          break;
        }
      }
    }

    // if anything happened, return success
    return didTransform ? ActionResult.SUCCESS : ActionResult.PASS;
  }
}
