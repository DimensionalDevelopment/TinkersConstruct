package slimeknights.tconstruct.smeltery.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * Fluid container holding 1 ingot of fluid
 */
public class CopperCanItem extends Item {
  private static final String TAG_FLUID = "fluid";

  public CopperCanItem(Settings properties) {
    super(properties);
  }

//  @Override
//  public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
//    return new CopperCanFluidHandler(stack);
//  }

//  @Override
  public boolean hasContainerItem(ItemStack stack) {
    return getFluid(stack) != Fluids.EMPTY;
  }

//  @Override
  public ItemStack getContainerItem(ItemStack stack) {
    Fluid fluid = getFluid(stack);
    if (fluid != Fluids.EMPTY) {
      return new ItemStack(this);
    }
    return ItemStack.EMPTY;
  }

  @Override
  @Environment(EnvType.CLIENT)
  public void appendTooltip(ItemStack stack, @Nullable World worldIn, List<Text> tooltip, TooltipContext flagIn) {
    Fluid fluid = getFluid(stack);
    if (fluid != Fluids.EMPTY) {
      tooltip.add(new TranslatableText(this.getTranslationKey() + ".contents", new TranslatableText(Registry.FLUID.getId(fluid).toString())).formatted(Formatting.GRAY));
    } else {
      tooltip.add(new TranslatableText(this.getTranslationKey() + ".tooltip").formatted(Formatting.GRAY));
    }
  }

  /** Sets the fluid on the given stack */
  public static ItemStack setFluid(ItemStack stack, Fluid fluid) {
    if (stack.hasTag() || fluid != Fluids.EMPTY) {
      NbtCompound nbt = stack.getOrCreateTag();
      nbt.putString(TAG_FLUID, Objects.requireNonNull(Registry.FLUID.getId(fluid)).toString());
    }
    return stack;
  }

  /** Gets the fluid from the given stack */
  public static Fluid getFluid(ItemStack stack) {
    NbtCompound nbt = stack.getTag();
    if (nbt != null) {
      Identifier location = Identifier.tryParse(nbt.getString(TAG_FLUID));
      if (location != null && Registry.FLUID.containsId(location)) {
        Fluid fluid = Registry.FLUID.get(location);
        if (fluid != null) {
          return fluid;
        }
      }
    }
    return Fluids.EMPTY;
  }

  /**
   * Gets a string variant name for the given stack
   * @param stack  Stack instance to check
   * @return  String variant name
   */
  public static String getSubtype(ItemStack stack) {
    NbtCompound nbt = stack.getTag();
    if (nbt != null) {
      return nbt.getString(TAG_FLUID);
    }
    return "";
  }
}
