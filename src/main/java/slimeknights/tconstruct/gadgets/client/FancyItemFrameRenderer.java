package slimeknights.tconstruct.gadgets.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.ItemFrameEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.EntityType;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderItemInFrameEvent;
import net.minecraftforge.common.MinecraftForge;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.gadgets.entity.FancyItemFrameEntity;
import slimeknights.tconstruct.gadgets.entity.FrameType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.HashMap;
import java.util.Map;

// TODO: needs so much cleanup
public class FancyItemFrameRenderer extends EntityRenderer<FancyItemFrameEntity> {

  private static final Identifier MAP_BACKGROUND_TEXTURES = new Identifier("textures/map/map_background.png");

  private static final Map<FrameType, ModelIdentifier> LOCATIONS_MODEL = new HashMap<>();
  private static final Map<FrameType, ModelIdentifier> LOCATIONS_MODEL_MAP = new HashMap<>();

  private final MinecraftClient mc = MinecraftClient.getInstance();
  private final ItemRenderer itemRenderer;
  private final ItemFrameEntityRenderer defaultRenderer;

  public FancyItemFrameRenderer(EntityRenderDispatcher renderManagerIn, ItemRenderer itemRendererIn) {
    super(renderManagerIn);
    this.itemRenderer = itemRendererIn;
    this.defaultRenderer = (ItemFrameEntityRenderer) renderManagerIn.renderers.get(EntityType.ITEM_FRAME);

    for (FrameType frameType : FrameType.values()) {
      // TODO: reinstate when Forge fixes itself
      // LOCATIONS_MODEL.put(color, new ModelResourceLocation(new ResourceLocation(TConstruct.modID, frameType.getName() + "_frame"), "map=false"));
      // LOCATIONS_MODEL_MAP.put(color, new ModelResourceLocation(new ResourceLocation(TConstruct.modID, frameType.getName() + "_frame"), "map=true"));

      LOCATIONS_MODEL.put(frameType, new ModelIdentifier(new Identifier(TConstruct.modID, frameType.asString() + "_frame_empty"), "inventory"));
      LOCATIONS_MODEL_MAP.put(frameType, new ModelIdentifier(new Identifier(TConstruct.modID, frameType.asString() + "_frame_map"), "inventory"));
    }
  }

  @Override
  public void render(FancyItemFrameEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, VertexConsumerProvider bufferIn, int packedLightIn) {
    super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    matrixStackIn.push();
    Vector3d vec3d = this.getRenderOffset(entityIn, partialTicks);
    matrixStackIn.translate(-vec3d.getX(), -vec3d.getY(), -vec3d.getZ());
    Direction direction = entityIn.getHorizontalFacing();
    matrixStackIn.translate((double) direction.getXOffset() * 0.46875D, (double) direction.getYOffset() * 0.46875D, (double) direction.getZOffset() * 0.46875D);
    matrixStackIn.rotate(Vector3f.XP.rotationDegrees(entityIn.rotationPitch));
    matrixStackIn.rotate(Vector3f.YP.rotationDegrees(180.0F - entityIn.rotationYaw));

    // render the frame
    FrameType frameType = entityIn.getFrameType();
    ItemStack stack = entityIn.getDisplayedItem();
    // clear does not render the frame if filled
    if (frameType != FrameType.CLEAR || stack.isEmpty()) {
      BlockRendererDispatcher blockrendererdispatcher = this.mc.getBlockRendererDispatcher();
      ModelManager modelmanager = blockrendererdispatcher.getBlockModelShapes().getModelManager();
      ModelResourceLocation location = entityIn.getDisplayedItem().getItem() instanceof FilledMapItem ? LOCATIONS_MODEL_MAP.get(frameType) : LOCATIONS_MODEL.get(frameType);
      matrixStackIn.push();
      matrixStackIn.translate(-0.5D, -0.5D, -0.5D);
      blockrendererdispatcher.getBlockModelRenderer().renderModelBrightnessColor(matrixStackIn.getLast(), bufferIn.getBuffer(Atlases.getCutoutBlockType()), null, modelmanager.getModel(location), 1.0F, 1.0F, 1.0F, packedLightIn, OverlayTexture.NO_OVERLAY);
      matrixStackIn.pop();
    }

    // render the item
    if (!stack.isEmpty()) {
      MapData mapdata = FilledMapItem.getMapData(stack, entityIn.world);
      matrixStackIn.translate(0.0D, 0.0D, 0.4375D);
      int i = mapdata != null ? entityIn.getRotation() % 4 * 2 : entityIn.getRotation();
      matrixStackIn.rotate(Vector3f.ZP.rotationDegrees((float) i * 360.0F / 8.0F));
      if (!MinecraftForge.EVENT_BUS.post(new RenderItemInFrameEvent(entityIn, this.defaultRenderer, matrixStackIn, bufferIn, packedLightIn))) {
        if (mapdata != null) {
          matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(180.0F));
          matrixStackIn.scale(0.0078125F, 0.0078125F, 0.0078125F);
          matrixStackIn.translate(-64.0D, -64.0D, 0.0D);
          matrixStackIn.translate(0.0D, 0.0D, -1.0D);
          this.mc.gameRenderer.getMapItemRenderer().renderMap(matrixStackIn, bufferIn, mapdata, true, packedLightIn);
        } else {
          matrixStackIn.scale(0.5F, 0.5F, 0.5F);
          this.itemRenderer.renderItem(stack, ItemCameraTransforms.TransformType.FIXED, packedLightIn, OverlayTexture.NO_OVERLAY, matrixStackIn, bufferIn);
        }
      }
    }

    matrixStackIn.pop();
  }

  @Nullable
  @Override
  public Identifier getTexture(@NotNull FancyItemFrameEntity entity) {
    return null;
  }

  public Vec3d getRenderOffset(FancyItemFrameEntity entityIn, float partialTicks) {
    return new Vec3d((float) entityIn.getHorizontalFacing().getOffsetX() * 0.3F, -0.25D, (float) entityIn.getHorizontalFacing().getOffsetZ() * 0.3F);
  }
//
//  @Override
//  protected boolean canRenderName(FancyItemFrameEntity entity) {
//    if (MinecraftClient.isHudEnabled() && !entity.getHeldItemStack().isEmpty() && entity.getHeldItemStack().hasCustomName() && this.dispatcher.targetedEntity == entity) {
//      double d0 = this.dispatcher.getSquaredDistanceToCamera(entity);
//      float f = entity.isSneaky() ? 32.0F : 64.0F;
//      return d0 < (double) (f * f);
//    } else {
//      return false;
//    }
//  }
//
//  @Override
//  protected void renderName(FancyItemFrameEntity entityIn, Text displayNameIn, MatrixStack matrixStackIn, VertexConsumerProvider bufferIn, int packedLightIn) {
//    super.renderLabelIfPresent(entityIn, entityIn.getHeldItemStack().getName(), matrixStackIn, bufferIn, packedLightIn);
//  }
}
