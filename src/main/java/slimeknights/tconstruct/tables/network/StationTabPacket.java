package slimeknights.tconstruct.tables.network;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraftforge.fml.network.NetworkHooks;
import slimeknights.mantle.network.packet.IThreadsafePacket;
import slimeknights.tconstruct.library.network.TinkerNetwork;
import slimeknights.tconstruct.tables.block.ITinkerStationBlock;

public class StationTabPacket implements IThreadsafePacket {

  private BlockPos pos;
  public StationTabPacket(BlockPos blockPos) {
    this.pos = blockPos;
  }

  public StationTabPacket(PacketByteBuf buffer) {
    this.pos = buffer.readBlockPos();
  }

  @Override
  public void encode(PacketByteBuf buffer) {
    buffer.writeBlockPos(pos);
  }

  @Override
  public void handleThreadsafe(PlayerEntity player, PacketSender context) {
    ServerPlayerEntity sender = context.getSender();
    if (sender != null) {
      ItemStack heldStack = sender.inventory.getCursorStack();
      if (!heldStack.isEmpty()) {
        // set it to empty, so it's doesn't get dropped
        sender.inventory.setCursorStack(ItemStack.EMPTY);
      }

      BlockState state = sender.getEntityWorld().getBlockState(pos);
      if (state.getBlock() instanceof ITinkerStationBlock) {
        ((ITinkerStationBlock) state.getBlock()).openGui(sender, sender.getEntityWorld(), pos);
      } else {
        NamedScreenHandlerFactory provider = state.createScreenHandlerFactory(sender.getEntityWorld(), pos);
        if (provider != null) {
          NetworkHooks.openGui(sender, provider, pos);
        }
      }

      if (!heldStack.isEmpty()) {
        sender.inventory.setCursorStack(heldStack);
        TinkerNetwork.getInstance().sendVanillaPacket(sender, new ScreenHandlerSlotUpdateS2CPacket(-1, -1, heldStack));
      }
    }
  }
}
