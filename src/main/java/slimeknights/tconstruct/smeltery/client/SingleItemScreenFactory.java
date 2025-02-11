package slimeknights.tconstruct.smeltery.client;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.gui.screen.ingame.HandledScreens.Provider;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import slimeknights.mantle.client.screen.BackgroundContainerScreen;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.smeltery.inventory.SingleItemContainer;

import org.jetbrains.annotations.Nullable;

/**
 * Screen factory for the single item container, one container for multiple backgrounds
 */
public class SingleItemScreenFactory implements Provider<SingleItemContainer,BackgroundContainerScreen<SingleItemContainer>> {
  private static final int HEIGHT = 133;
  private static final Identifier DEFAULT = Util.getResource("textures/gui/blank.png");

  /**
   * Gets the background path for the given tile
   * @param tile  Tile
   * @return  Background path
   */
  private static Identifier getBackground(@Nullable BlockEntity tile) {
    if (tile != null) {
      Identifier id = Registry.BLOCK_ENTITY_TYPE.getId(tile.getType());
      if (id != null) {
        return new Identifier(id.getNamespace(), String.format("textures/gui/%s.png", id.getPath()));
      }
    }
    return DEFAULT;
  }

  @Override
  public BackgroundContainerScreen<SingleItemContainer> create(SingleItemContainer container, PlayerInventory inventory, Text name) {
    return new BackgroundContainerScreen<>(container, inventory, name, HEIGHT, getBackground(container.getTile()));
  }
}
