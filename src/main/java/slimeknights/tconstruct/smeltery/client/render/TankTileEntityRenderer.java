package slimeknights.tconstruct.smeltery.client.render;

import lombok.extern.log4j.Log4j2;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import slimeknights.tconstruct.common.config.TConfig;
import slimeknights.tconstruct.smeltery.tileentity.ITankTileEntity;

@Log4j2
public class TankTileEntityRenderer<T extends BlockEntity & ITankTileEntity> extends BlockEntityRenderer<T> {

  public TankTileEntityRenderer(BlockEntityRenderDispatcher rendererDispatcherIn) {
    super(rendererDispatcherIn);
  }

  @Override
  public void render(T tile, float partialTicks, MatrixStack matrixStack, VertexConsumerProvider buffer, int combinedLightIn, int combinedOverlayIn) {
    if (TConfig.client.tankFluidModel) {
      return;
    }
//    // render the fluid
//    TankModel.BakedModel<?> model = ModelHelper.getBakedModel(tile.getCachedState(), TankModel.BakedModel.class);
//    if (model != null) {
//      RenderUtils.renderFluidTank(matrixStack, buffer, model.getFluid(), tile.getTank(), combinedLightIn, partialTicks, true);
//    }
  }
}
