package slimeknights.tconstruct.common.registration;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import slimeknights.mantle.registration.deferred.ItemDeferredRegister;
import slimeknights.mantle.registration.object.ItemObject;

public class ItemDeferredRegisterExtension extends ItemDeferredRegister {
  public ItemDeferredRegisterExtension(String modID) {
    super(modID);
  }

  /**
   * Registers a set of three cast items at once
   * @param name   Base name of cast
   * @param props  Item properties
   * @return  Object containing casts
   */
  public CastItemObject registerCast(String name, Item.Settings props) {
    ItemObject<Item> cast = register(name + "_cast", props);
    ItemObject<Item> sandCast = register(name + "_sand_cast", props);
    ItemObject<Item> redSandCast = register(name + "_red_sand_cast", props);
    return new CastItemObject(new Identifier(resourceName(name)), cast, sandCast, redSandCast);
  }
}
