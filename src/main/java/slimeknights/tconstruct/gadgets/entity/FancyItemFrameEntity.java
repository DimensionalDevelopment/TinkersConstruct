package slimeknights.tconstruct.gadgets.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import slimeknights.tconstruct.gadgets.TinkerGadgets;

public class FancyItemFrameEntity extends ItemFrameEntity {// implements IEntityAdditionalSpawnData {

  private static final TrackedData<Integer> VARIANT = DataTracker.registerData(FancyItemFrameEntity.class, TrackedDataHandlerRegistry.INTEGER);
  private static final String TAG_VARIANT = "Variant";

  public FancyItemFrameEntity(EntityType<? extends FancyItemFrameEntity> type, World world) {
    super(type, world);
  }

  public FancyItemFrameEntity(World worldIn, BlockPos blockPos, Direction face, int variant) {
    super(TinkerGadgets.itemFrameEntity, worldIn);
    this.attachmentPos = blockPos;
    this.setFacing(face);
    this.dataTracker.set(VARIANT, variant);
  }

  @Override
  protected void initDataTracker() {
    super.initDataTracker();

    this.dataTracker.startTracking(VARIANT, 0);
  }

  public FrameType getFrameType() {
    return FrameType.byId(this.getVariantIndex());
  }

  public int getVariantIndex() {
    return this.dataTracker.get(VARIANT);
  }

  @Nullable
  @Override
  public ItemEntity dropStack(@NotNull ItemStack stack, float offset) {
    if (stack.getItem() == Items.ITEM_FRAME) {
      stack = new ItemStack(FrameType.getFrameFromType(this.getFrameType()));
    }
    return super.dropStack(stack, offset);
  }

//  @NotNull
//  @Override
//  public ItemStack getPickedResult(HitResult target) {
//    ItemStack held = this.getHeldItemStack();
//    if (held.isEmpty()) {
//      return new ItemStack(FrameType.getFrameFromType(this.getFrameType()));
//    } else {
//      return held.copy();
//    }
//  }

  @Override
  public void writeCustomDataToNbt(NbtCompound compound) {
    super.writeCustomDataToNbt(compound);
    compound.putInt(TAG_VARIANT, this.getVariantIndex());
  }

  @Override
  public void readCustomDataFromNbt(NbtCompound compound) {
    super.readCustomDataFromNbt(compound);
    this.dataTracker.set(VARIANT, compound.getInt(TAG_VARIANT));
  }

  @NotNull
  @Override
  public Packet<?> createSpawnPacket() {
    return super.createSpawnPacket();
  }
//
//  @Override
//  public void writeSpawnData(PacketByteBuf buffer) {
//    buffer.writeVarInt(this.getVariantIndex());
//    buffer.writeBlockPos(this.attachmentPos);
//    buffer.writeVarInt(this.facing.getId());
//  }
//
//  @Override
//  public void readSpawnData(PacketByteBuf buffer) {
//    this.dataTracker.set(VARIANT, buffer.readVarInt());
//    this.attachmentPos = buffer.readBlockPos();
//    this.setFacing(Direction.byId(buffer.readVarInt()));
//  }

  private static void removeClickEvents(Text text) {
    if (text instanceof MutableText) {
      ((MutableText)text).styled((p_213318_0_) -> p_213318_0_.withClickEvent(null))
          .getSiblings().forEach(FancyItemFrameEntity::removeClickEvents);
    }
  }

  @Override
  public Text getName() {
    Text itextcomponent = this.getCustomName();
    if (itextcomponent != null) {
      Text textComponent = itextcomponent.shallowCopy();
      removeClickEvents(textComponent);
      return textComponent;
    } else {
      String translationKey = this.getType().getTranslationKey();

      return new TranslatableText(translationKey + "." + this.getFrameType().asString());
    }
  }
}
