package slimeknights.tconstruct.library.component.piggyback;

import net.minecraft.entity.player.PlayerEntity;

public interface ITinkerPiggyback {

  void setRiddenPlayer(PlayerEntity player);

  void updatePassengers();
}