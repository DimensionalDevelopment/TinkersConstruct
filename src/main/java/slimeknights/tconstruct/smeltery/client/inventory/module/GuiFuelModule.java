package slimeknights.tconstruct.smeltery.client.inventory.module;

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import slimeknights.mantle.client.screen.ScalableElementScreen;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.client.GuiUtil;
import slimeknights.tconstruct.library.client.util.FluidTooltipHandler;
import slimeknights.tconstruct.smeltery.tileentity.module.FuelModule;
import slimeknights.tconstruct.smeltery.tileentity.module.FuelModule.FuelInfo;

import org.jetbrains.annotations.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * GUI component handling the fuel module
 */
@RequiredArgsConstructor
public class GuiFuelModule {
  private static final ScalableElementScreen FIRE = new ScalableElementScreen(176, 136, 14, 14, 256, 256);

  // tooltips
  private static final String TOOLTIP_TEMPERATURE = Util.makeTranslationKey("gui", "melting.fuel.temperature");
  private static final List<Text> TOOLTIP_NO_TANK = Collections.singletonList(new TranslatableText(Util.makeTranslationKey("gui", "melting.fuel.no_tank")));
  private static final List<Text> TOOLTIP_NO_FUEL = Collections.singletonList(new TranslatableText(Util.makeTranslationKey("gui", "melting.fuel.empty")));
  private static final Text TOOLTIP_INVALID_FUEL = new TranslatableText(Util.makeTranslationKey("gui", "melting.fuel.invalid")).formatted(Formatting.RED);
  private static final Text TOOLTIP_SOLID_FUEL = new TranslatableText(Util.makeTranslationKey("gui", "melting.fuel.solid"));

  private final HandledScreen<?> screen;
  private final FuelModule fuelModule;
  /** location to draw the tank */
  private final int x, y, width, height;
  /** location to draw the fire */
  private final int fireX, fireY;
  /** If true, UI has a fuel slot */
  private final boolean hasFuelSlot;

  private FuelInfo fuelInfo = FuelInfo.EMPTY;

  /**
   * Checks if the fuel tank is hovered
   * @param checkX  X position to check
   * @param checkY  Y position to check
   * @return  True if hovered
   */
  private boolean isHovered(int checkX, int checkY) {
    return GuiUtil.isHovered(checkX, checkY, x - 1, y - 1, width + 2, height + 2);
  }

  /**
   * Draws the fuel at the correct location
   * @param matrices  Matrix stack instance
   */
  public void draw(MatrixStack matrices) {
    // draw fire
    int fuel = fuelModule.getFuel();
    int fuelQuality = fuelModule.getFuelQuality();
    if (fuel > 0 && fuelQuality > 0) {
      FIRE.drawScaledYUp(matrices, fireX + screen.x, fireY + screen.y, 14 * fuel / fuelQuality);
    }

    // draw tank second, it changes the image
    // store fuel info into a field for other methods, this one updates most often
    if (!hasFuelSlot) {
      fuelInfo = fuelModule.getFuelInfo();
      if (!fuelInfo.isEmpty()) {
        GuiUtil.renderFluidTank(matrices, screen, fuelInfo.getFluid(), fuelInfo.getTotalAmount(), fuelInfo.getCapacity(), x, y, width, height, 100);
      }
    }
  }

  /**
   * Highlights the hovered fuel
   * @param matrices  Matrix stack instance
   * @param checkX    Top corner relative mouse X
   * @param checkY    Top corner relative mouse Y
   */
  public void renderHighlight(MatrixStack matrices, int checkX, int checkY) {
    if (isHovered(checkX, checkY)) {
      // if there is a fuel slot, render highlight lower
      if (hasFuelSlot) {
        if (checkY > y + 18) {
          GuiUtil.renderHighlight(matrices, x, y + 18, width, height - 18);
        }
      } else {
        // full fluid highlight
        GuiUtil.renderHighlight(matrices, x, y, width, height);
      }
    }
  }

  /**
   * Adds the tooltip for the fuel
   * @param matrices  Matrix stack instance
   * @param mouseX    Mouse X position
   * @param mouseY    Mouse Y position
   */
  public void addTooltip(MatrixStack matrices, int mouseX, int mouseY, boolean hasTank) {
    int checkX = mouseX - screen.x;
    int checkY = mouseY - screen.y;

    if (isHovered(checkX, checkY)) {
      List<Text> tooltip;
      // if an item or we have a fuel slot, do item tooltip
      if (hasFuelSlot || fuelInfo.isItem()) {
        // if there is a fuel slot, start below the fuel slot
        if (!hasFuelSlot || checkY > y + 18) {
          if (hasTank) {
            // no invalid fuel, we assume the slot is validated (hasFuelSlot is only true for the heater which validates)
            int temperature = fuelModule.getTemperature();
            if (temperature > 0) {
              tooltip = Arrays.asList(TOOLTIP_SOLID_FUEL, new TranslatableText(TOOLTIP_TEMPERATURE, temperature).formatted(Formatting.GRAY, Formatting.ITALIC));
            } else {
              tooltip = TOOLTIP_NO_FUEL;
            }
          } else {
            tooltip = TOOLTIP_NO_TANK;
          }
        } else {
          tooltip = Collections.emptyList();
        }
      } else if (!fuelInfo.isEmpty()) {
        FluidVolume fluid = fuelInfo.getFluid();
        tooltip = FluidTooltipHandler.getFluidTooltip(fluid, FluidAmount.of(fuelInfo.getTotalAmount(), 1000));
        int temperature = fuelInfo.getTemperature();
        if (temperature > 0) {
          tooltip.add(1, new TranslatableText(TOOLTIP_TEMPERATURE, temperature).formatted(Formatting.GRAY, Formatting.ITALIC));
        } else {
          tooltip.add(1, TOOLTIP_INVALID_FUEL);
        }
      } else {
        tooltip = hasTank ? TOOLTIP_NO_FUEL : TOOLTIP_NO_TANK;
      }

      // TODO: func_243308_b->renderTooltip
      screen.renderTooltip(matrices, tooltip, mouseX, mouseY);
    }
  }

  /**
   * Gets the fluid stack under the mouse
   * @param checkX  Mouse X position
   * @param checkY  Mouse Y position
   * @return  Fluid stack under mouse
   */
  @Nullable
  public FluidVolume getIngredient(int checkX, int checkY) {
    if (!hasFuelSlot && isHovered(checkX, checkY) && !fuelInfo.isEmpty()) {
      return fuelInfo.getFluid();
    }
    return null;
  }
}
