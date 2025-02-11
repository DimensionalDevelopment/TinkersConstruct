package slimeknights.tconstruct.smeltery.tileentity;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import net.fabricmc.fabric.api.util.NbtType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.recipe.RecipeHelper;
import slimeknights.tconstruct.library.EmptyItemHandler;
import slimeknights.tconstruct.library.recipe.RecipeTypes;
import slimeknights.tconstruct.library.recipe.casting.ICastingRecipe;
import slimeknights.tconstruct.library.recipe.molding.MoldingRecipe;
import slimeknights.tconstruct.misc.ItemHandlerHelper;
import slimeknights.tconstruct.shared.tileentity.TableTileEntity;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.smeltery.network.FluidUpdatePacket;
import slimeknights.tconstruct.smeltery.recipe.TileCastingWrapper;
import slimeknights.tconstruct.smeltery.tileentity.inventory.MoldingInventoryWrapper;
import slimeknights.tconstruct.smeltery.tileentity.tank.CastingFluidHandler;
import java.util.Optional;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Tickable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public abstract class CastingTileEntity extends TableTileEntity implements Tickable, SidedInventory, FluidUpdatePacket.IFluidPacketReceiver {
  // slots
  public static final int INPUT = 0;
  public static final int OUTPUT = 1;
  // NBT
  private static final String TAG_TANK = "tank";
  private static final String TAG_TIMER = "timer";
  private static final String TAG_RECIPE = "recipe";

  /** Special casting fluid tank */
  private final CastingFluidHandler tank = new CastingFluidHandler(this);

  /* Casting recipes */
  /** Recipe type for casting recipes, may be basin or table */
  private final RecipeType<ICastingRecipe> castingType;
  /** Inventory for use in casting recipes */
  public final TileCastingWrapper castingInventory;
  /** Current recipe progress */
  private int timer;
  /** Current in progress recipe */
  private ICastingRecipe currentRecipe;
  /** Name of the current recipe, fetched from NBT. Used since NBT is read before recipe manager access */
  private Identifier recipeName;
  /** Cache recipe to reduce time during recipe lookups. Not saved to NBT */
  private ICastingRecipe lastCastingRecipe;
  /** Last recipe output for client side display */
  private ItemStack lastOutput = null;

  /* Molding recipes */
  /** Recipe type for molding recipes, may be basin or table */
  private final RecipeType<MoldingRecipe> moldingType;
  /** Inventory to use for molding recipes */
  public final MoldingInventoryWrapper moldingInventory;
  /** Cache recipe to reduce time during recipe lookups. Not saved to NBT */
  private MoldingRecipe lastMoldingRecipe;

  protected CastingTileEntity(BlockEntityType<?> tileEntityTypeIn, RecipeType<ICastingRecipe> castingType, RecipeType<MoldingRecipe> moldingType) {
    super(tileEntityTypeIn, "gui.tconstruct.casting", 2, 1);
    this.castingType = castingType;
    this.moldingType = moldingType;
    this.castingInventory = new TileCastingWrapper(this, Fluids.EMPTY);
    this.moldingInventory = new MoldingInventoryWrapper(EmptyItemHandler.INSTANCE, INPUT);
  }

  /**
   * Called from {@link slimeknights.tconstruct.smeltery.block.AbstractCastingBlock#onUse(BlockState, World, BlockPos, PlayerEntity, Hand, BlockHitResult)}
   * @param player Player activating the block.
   */
  public void interact(PlayerEntity player, Hand hand) {
    if (world == null || world.isClient) {
      return;
    }
    // can't interact if liquid inside
    if (!tank.isEmpty()) {
      return;
    }

    ItemStack held = player.getStackInHand(hand);
    ItemStack input = getStack(INPUT);
    ItemStack output = getStack(OUTPUT);

    // all molding recipes require a stack in the input slot and nothing in the output slot
    if (!input.isEmpty() && output.isEmpty()) {
      // first, try the players hand item for a recipe
      moldingInventory.setPattern(held);
      MoldingRecipe recipe = findMoldingRecipe();
      if (recipe != null) {
        // if hand is empty, pick up the result (hand empty will only match recipes with no mold item)
        ItemStack result = recipe.craft(moldingInventory);
        if (held.isEmpty()) {
          setStack(INPUT, ItemStack.EMPTY);
          player.setStackInHand(hand, result);
        } else {
          // if the recipe has a mold, hand item goes on table (if not consumed in crafting)
          setStack(INPUT, result);
          if (!recipe.isPatternConsumed()) {
            throw new RuntimeException("crab");
//            setStack(OUTPUT, ItemHandlerHelper.copyStackWithSize(held, 1));
//             send a block update for the comparator, needs to be done after the stack is removed
//            world.updateNeighborsAlways(this.pos, this.getCachedState().getBlock());
          }
          held.decrement(1);
          player.setStackInHand(hand, held.isEmpty() ? ItemStack.EMPTY : held);
        }
        moldingInventory.setPattern(ItemStack.EMPTY);
        return;
      } else {
        // if no recipe was found using the held item, try to find a mold-less recipe to perform
        // this ensures that if a recipe happens "on pickup" you get consistent behavior, without this it would fall though to pick up normally
        moldingInventory.setPattern(ItemStack.EMPTY);
        recipe = findMoldingRecipe();
        if (recipe != null) {
          setStack(INPUT, ItemStack.EMPTY);
          player.giveItemStack(recipe.craft(moldingInventory));
          return;
        }
      }
      // clear mold stack, prevents storing an unneeded item
      moldingInventory.setPattern(ItemStack.EMPTY);
    }

    // recipes failed, so do normal pickup
    // completely empty -> insert current item into input
    if (input.isEmpty() && output.isEmpty()) {
      if (!held.isEmpty()) {
        ItemStack stack = held.split(stackSizeLimit);
        player.setStackInHand(hand, held.isEmpty() ? ItemStack.EMPTY : held);
        setStack(INPUT, stack);
      }
    } else {
      // stack in either slot, take one out
      // prefer output stack, as often the input is a cast that we want to use again
      int slot = output.isEmpty() ? INPUT : OUTPUT;

      // Additional info: Only 1 item can be put into the casting block usually, however recipes
      // can have ItemStacks with stacksize > 1 as output
      // we therefore spill the whole contents on extraction.
      ItemStack stack = getStack(slot);
      ItemHandlerHelper.giveItemToPlayer(player, stack, player.inventory.selectedSlot);
      setStack(slot, ItemStack.EMPTY);

      // send a block update for the comparator, needs to be done after the stack is removed
      if (slot == OUTPUT) {
        world.updateNeighborsAlways(this.pos, this.getCachedState().getBlock());
      }
    }
  }

  @Override
  public void setStack(int slot, ItemStack stack) {
    ItemStack original = getStack(slot);
    super.setStack(slot, stack);
    // if the stack changed emptiness, update
    if (original.isEmpty() != stack.isEmpty() && world != null && !world.isClient) {
      world.updateComparators(pos, this.getCachedState().getBlock());
    }
  }
  
  @Override
  public int[] getAvailableSlots(Direction side) {
    return new int[]{INPUT, OUTPUT};
  }

  @Override
  public boolean canInsert(int index, ItemStack itemStackIn, @Nullable Direction direction) {
    return tank.isEmpty() && index == INPUT && !isStackInSlot(OUTPUT);
  }

  @Override
  public boolean canExtract(int index, ItemStack stack, Direction direction) {
    return tank.isEmpty() && index == OUTPUT;
  }

  @Override
  public void tick() {
    // no recipe
    if (world == null || currentRecipe == null) {
      return;
    }
    // fully filled
    FluidVolume currentFluid = tank.getFluid();
    if (currentFluid.getAmount() >= tank.getCapacity() && !currentFluid.isEmpty()) {
      timer++;
      if (!world.isClient) {
        castingInventory.setFluid(currentFluid.getRawFluid());
        if (timer >= currentRecipe.getCoolingTime(castingInventory)) {
          if (!currentRecipe.matches(castingInventory, world)) {
            // if lost our recipe or the recipe needs more fluid then we have, we are done
            // will come around later for the proper fluid amount
            currentRecipe = findCastingRecipe();
            recipeName = null;
            if (currentRecipe == null || currentRecipe.getFluidAmount(castingInventory) > currentFluid.getAmount()) {
              timer = 0;
              return;
            }
          }

          // actual recipe result
          ItemStack output = currentRecipe.craft(castingInventory);
          if (currentRecipe.switchSlots()) {
            if (!currentRecipe.isConsumed()) {
              setStack(OUTPUT, getStack(INPUT));
            }
            setStack(INPUT, output);
          } else {
            if (currentRecipe.isConsumed()) {
              setStack(INPUT, ItemStack.EMPTY);
            }
            setStack(OUTPUT, output);
          }
          world.playSound(null, pos, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.AMBIENT, 0.07f, 4f);

          reset();

          world.updateNeighborsAlways(this.pos, this.getCachedState().getBlock());
        }
      }
      else if (world.random.nextFloat() > 0.9f) {
        world.addParticle(ParticleTypes.SMOKE, pos.getX() + world.random.nextDouble(), pos.getY() + 1.1d, pos.getZ() + world.random.nextDouble(), 0.0D, 0.0D, 0.0D);
      }
    }
  }

  @Nullable
  private ICastingRecipe findCastingRecipe() {
    if (world == null) return null;
    if (this.lastCastingRecipe != null && this.lastCastingRecipe.matches(castingInventory, world)) {
      return this.lastCastingRecipe;
    }
    ICastingRecipe castingRecipe = world.getRecipeManager().getFirstMatch(this.castingType, castingInventory, world).orElse(null);
    if (castingRecipe != null) {
      this.lastCastingRecipe = castingRecipe;
    }
    return castingRecipe;
  }


  /**
   * Finds a molding recipe for the given inventory
   * @return  Recipe, or null if no recipe found
   */
  @Nullable
  private MoldingRecipe findMoldingRecipe() {
    if (world == null) return null;
    if (lastMoldingRecipe != null && lastMoldingRecipe.matches(moldingInventory, world)) {
      return lastMoldingRecipe;
    }
    Optional<MoldingRecipe> newRecipe = world.getRecipeManager().getFirstMatch(moldingType, moldingInventory, world);
    if (newRecipe.isPresent()) {
      lastMoldingRecipe = newRecipe.get();
      return lastMoldingRecipe;
    }
    return null;
  }


  /**
   * Called from CastingFluidHandler.fill()
   * @param fluid   Fluid used in casting
   * @param action  EXECUTE or SIMULATE
   * @return        Amount of fluid needed for recipe, used to resize the tank.
   */
  public int initNewCasting(Fluid fluid, Simulation action) {
    if (this.currentRecipe != null || this.recipeName != null) {
      return 0;
    }
    this.castingInventory.setFluid(fluid);
    ICastingRecipe castingRecipe = findCastingRecipe();
    if (castingRecipe != null) {
      if (action == Simulation.ACTION) {
        this.currentRecipe = castingRecipe;
        this.recipeName = null;
        this.lastOutput = null;
      }
      return castingRecipe.getFluidAmount(castingInventory);
    }
    return 0;
  }

  /**
   * Resets the casting table recipe to the default empty state
   */
  public void reset() {
    timer = 0;
    currentRecipe = null;
    recipeName = null;
    lastOutput = null;
    castingInventory.setFluid(Fluids.EMPTY);
    tank.reset();
  }

  @Override
  public void updateFluidTo(FluidVolume fluid) {
    if (fluid.isEmpty()) {
      reset();
    } else {
      int capacity = initNewCasting(fluid.getRawFluid(), Simulation.ACTION);
      if (capacity > 0) {
        tank.setCapacity(capacity);
      }
    }
    tank.setFluid(fluid);
  }

  @Nullable
  @Override
  public ScreenHandler createMenu(int id, PlayerInventory inv, PlayerEntity player) {
    // no GUI
    return null;
  }


  /* TER display */

  /**
   * Gets the recipe output for display in the TER
   * @return  Recipe output
   */
  public ItemStack getRecipeOutput() {
    if (lastOutput == null) {
      if (currentRecipe == null) {
        return ItemStack.EMPTY;
      }
      castingInventory.setFluid(tank.getFluid().getRawFluid());
      lastOutput = currentRecipe.craft(castingInventory);
    }
    return lastOutput;
  }

  /**
   * Gets the total time for this recipe for display in the TER
   * @return  total recipe time
   */
  public int getRecipeTime() {
    if (currentRecipe == null) {
      return -1;
    }
    return currentRecipe.getCoolingTime(castingInventory);
  }


  /* NBT */

  /**
   * Loads a recipe in from its name and updates the tank capacity
   * @param world  Nonnull world instance
   * @param name   Recipe name to load
   */
  private void loadRecipe(World world, Identifier name) {
    // if the tank is empty, ignore old recipe
    FluidVolume fluid = tank.getFluid();
    if(!fluid.isEmpty()) {
      // fetch recipe by name
      RecipeHelper.getRecipe(world.getRecipeManager(), name, ICastingRecipe.class).ifPresent(recipe -> {
        this.currentRecipe = recipe;
        castingInventory.setFluid(fluid.getRawFluid());
        tank.setCapacity(recipe.getFluidAmount(castingInventory));
      });
    }
  }

  @Override
  public void setLocation(World world, BlockPos pos) {
    super.setLocation(world, pos);
    // if we have a recipe name, swap recipe name for recipe instance
    if (recipeName != null) {
      loadRecipe(world, recipeName);
      recipeName = null;
    }
  }

  @Override
  @NotNull
  public NbtCompound writeNbt(NbtCompound tags) {
    tags = super.writeNbt(tags);
    tags.put(TAG_TANK, tank.writeToNBT(new NbtCompound()));
    tags.putInt(TAG_TIMER, timer);
    if (currentRecipe != null) {
      tags.putString(TAG_RECIPE, currentRecipe.getId().toString());
    } else if (recipeName != null) {
      tags.putString(TAG_RECIPE, recipeName.toString());
    }
    return tags;
  }

  @Override
  public void fromTag(BlockState state, NbtCompound tags) {
    super.fromTag(state, tags);
    tank.readFromNBT(tags.getCompound(TAG_TANK));
    timer = tags.getInt(TAG_TIMER);
    if (tags.contains(TAG_RECIPE, NbtType.STRING)) {
      Identifier name = new Identifier(tags.getString(TAG_RECIPE));
      // if we have a world, fetch the recipe
      if (world != null) {
        loadRecipe(world, name);
      } else {
        // otherwise fetch the recipe when the world is set
        recipeName = name;
      }
    }
  }

  public CastingFluidHandler getTank() {
    return this.tank;
  }

  public int getTimer() {
    return this.timer;
  }

  public static class Basin extends CastingTileEntity {
    public Basin() {
      super(TinkerSmeltery.basin, RecipeTypes.CASTING_BASIN, RecipeTypes.MOLDING_BASIN);
    }
  }

  public static class Table extends CastingTileEntity {
    public Table() {
      super(TinkerSmeltery.table, RecipeTypes.CASTING_TABLE, RecipeTypes.MOLDING_TABLE);
    }
  }
}
