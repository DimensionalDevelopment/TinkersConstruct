package slimeknights.tconstruct.smeltery.client.render;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BasicBakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3f;

import org.apache.logging.log4j.LogManager;
import slimeknights.mantle.client.model.fluid.FluidCuboid;
import slimeknights.mantle.client.model.fluid.FluidsModel;
import slimeknights.mantle.client.model.inventory.InventoryModel;
import slimeknights.mantle.client.model.inventory.ModelItem;
import slimeknights.mantle.client.model.util.ModelHelper;
import slimeknights.mantle.client.render.FluidRenderer;
import slimeknights.mantle.client.render.RenderingHelper;
import slimeknights.tconstruct.library.client.RenderUtils;
import slimeknights.tconstruct.smeltery.client.util.CastingItemRenderTypeBuffer;
import slimeknights.tconstruct.smeltery.tileentity.CastingTileEntity;
import slimeknights.tconstruct.smeltery.tileentity.tank.CastingFluidHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CastingTileEntityRenderer extends BlockEntityRenderer<CastingTileEntity> {
  public CastingTileEntityRenderer(BlockEntityRenderDispatcher dispatcher) {
    super(dispatcher);
  }

  @Override
  public void render(CastingTileEntity casting, float partialTicks, MatrixStack matrices, VertexConsumerProvider buffer, int light, int combinedOverlayIn) {
    BlockState state = casting.getWorld().getBlockState(casting.getPos());

    BakedModel model = ModelHelper.getBakedModel(state, BakedModel.class);
    //LogManager.getLogger().info(model);
    //
    //BasicBakedModel model = (BasicBakedModel) MinecraftClient.getInstance().getBakedModelManager().getBlockModels().getModel(state);
    if (model != null) {
      // rotate the matrix
      boolean isRotated = RenderingHelper.applyRotation(matrices, state);

      // if the recipe is in progress, start fading the item away
      int timer = casting.getTimer();
      int totalTime = casting.getRecipeTime();
      int itemOpacity = 0;
      int fluidOpacity = 0xFF;
      if (timer > 0 && totalTime > 0) {
        int opacity = (4 * 0xFF) * timer / totalTime;
        // fade item in
        itemOpacity = opacity / 4;

        // fade fluid and temperature out during last 10%
        if (opacity > 3 * 0xFF) {
          fluidOpacity = (4 * 0xFF) - opacity;
        } else {
          fluidOpacity = 0xFF;
        }
      }

      // render fluids
      CastingFluidHandler tank = casting.getTank();
      //LogManager.getLogger().info(tank);
      // if full, start rendering with opacity for progress
      Map<Direction, FluidCuboid.FluidFace> faces = new HashMap<>();
      faces.put(Direction.UP, FluidCuboid.FluidFace.NORMAL);
      faces.put(Direction.DOWN, FluidCuboid.FluidFace.NORMAL);
      faces.put(Direction.NORTH, FluidCuboid.FluidFace.NORMAL);
      faces.put(Direction.EAST, FluidCuboid.FluidFace.NORMAL);
      faces.put(Direction.SOUTH, FluidCuboid.FluidFace.NORMAL);
      faces.put(Direction.WEST, FluidCuboid.FluidFace.NORMAL);
      FluidCuboid model1 = new FluidCuboid(new Vec3f(), new Vec3f(), faces);
      if (tank.getFluid().getAmount() == tank.getCapacity()) {
        RenderUtils.renderTransparentCuboid(matrices, buffer, model1, tank.getFluid(), 1, light);
      } else {
        FluidRenderer.renderScaledCuboid(matrices, buffer, model1, tank.getFluid(), 0, tank.getCapacity(), light, false);
      }

      // render items
//      List<ModelItem> modelItems = model.getItems();
//      // input is normal
//      if (modelItems.size() >= 1) {
//        RenderingHelper.renderItem(matrices, buffer, casting.getStack(0), light);
//      }
//
//      // output may be the recipe output instead of the current item
//      if (modelItems.size() >= 2) {
//        ModelItem outputModel = modelItems.get(1);
//        if (!outputModel.isHidden()) {
//          // get output stack
//          ItemStack output = casting.getStack(1);
//          VertexConsumerProvider outputBuffer = buffer;
//          if (itemOpacity > 0 && output.isEmpty()) {
//            output = casting.getRecipeOutput();
//            // apply a buffer wrapper to tint and add opacity
//            outputBuffer = new CastingItemRenderTypeBuffer(buffer, itemOpacity, fluidOpacity);
//          }
//          RenderingHelper.renderItem(matrices, outputBuffer, output, light);
//        }
//      }

      //pop back rotation
      if (isRotated) {
        matrices.pop();
      }
    }
  }
}
