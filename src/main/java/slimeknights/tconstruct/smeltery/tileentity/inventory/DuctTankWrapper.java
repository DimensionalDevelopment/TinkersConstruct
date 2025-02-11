package slimeknights.tconstruct.smeltery.tileentity.inventory;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import lombok.AllArgsConstructor;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import slimeknights.tconstruct.fluids.IFluidHandler;
import slimeknights.tconstruct.fluids.TinkerFluids;

@AllArgsConstructor
public class DuctTankWrapper implements IFluidHandler {
  private final IFluidHandler parent;
  private final DuctItemHandler itemHandler;


  /* Properties */

  @Override
  public int getTanks() {
    return parent.getTanks();
  }

  @Override
  public FluidVolume getFluidInTank(int tank) {
    return parent.getFluidInTank(tank);
  }

  @Override
  public FluidAmount getTankCapacity(int tank) {
    return parent.getTankCapacity(tank);
  }

  @Override
  public boolean isFluidValid(int tank, FluidVolume stack) {
    return stack.getRawFluid() == itemHandler.getFluid();
  }


  /* Interactions */

  @Override
  public FluidVolume fill(FluidVolume resource, Simulation action) {
    if (resource.isEmpty() || resource.getRawFluid() != itemHandler.getFluid()) {
      return resource.withAmount(FluidAmount.ZERO);
    }
    return parent.fill(resource, action);
  }

  @Override
  public FluidVolume drain(FluidAmount maxDrain, Simulation action) {
    Fluid fluid = itemHandler.getFluid();
    if (fluid == Fluids.EMPTY) {
      return TinkerFluids.EMPTY;
    }
    return parent.drain(maxDrain, action);
  }

  @Override
  public FluidVolume drain(FluidVolume resource, Simulation action) {
    if (resource.isEmpty() || resource.getRawFluid() != itemHandler.getFluid()) {
      return TinkerFluids.EMPTY;
    }
    return parent.drain(resource, action);
  }
}
