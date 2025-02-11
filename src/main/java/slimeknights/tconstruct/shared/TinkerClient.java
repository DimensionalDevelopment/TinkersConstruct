package slimeknights.tconstruct.shared;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.resource.ResourceManager;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import org.apache.logging.log4j.LogManager;
import slimeknights.tconstruct.common.recipe.RecipeCacheInvalidator;
import slimeknights.tconstruct.library.book.TinkerBook;
import slimeknights.tconstruct.library.client.materials.MaterialRenderInfoLoader;
import slimeknights.tconstruct.library.client.util.ResourceValidator;
import slimeknights.tconstruct.library.utils.HarvestLevels;
import slimeknights.tconstruct.smeltery.SmelteryClientEvents;
import slimeknights.tconstruct.tables.TableClientEvents;
import slimeknights.tconstruct.world.WorldClientEvents;

import java.util.function.Consumer;

/**
 * This class should only be referenced on the client side
 */
public class TinkerClient implements ClientModInitializer {
  /** Validates that a texture exists for models. During model type as that is when the validator is needed */
  public static final ResourceValidator textureValidator = new ResourceValidator("textures/item/tool", "textures", ".png");

  /**
   * Called by TConstruct to handle any client side logic that needs to run during the constructor
   */
  @Override
  public void onInitializeClient() {
    TinkerBook.initBook();

    //noinspection ConstantConditions
    ClientLifecycleEvents.CLIENT_STARTED.register((client -> {
      ResourceManager manager = client.getResourceManager();
      if (manager instanceof ReloadableResourceManager) {
        addResourceListeners((ReloadableResourceManager)manager);
      }
    }));

    // add the recipe cache invalidator to the client
//    Consumer<RecipesUpdatedEvent> recipesUpdated = event -> RecipeCacheInvalidator.reload(true);
//    MinecraftForge.EVENT_BUS.addListener(recipesUpdated);
  }


  public static void onConstruct() {

  }

  /**
   * Adds resource listeners to the client class
   */
  public static void addResourceListeners(ReloadableResourceManager manager) {
    LogManager.getLogger().info("Registering ResourceListeners");
    WorldClientEvents.addResourceListener(manager);
    TableClientEvents.addResourceListener(manager);
    SmelteryClientEvents.addResourceListener(manager);
    MaterialRenderInfoLoader.addResourceListener(manager);
    manager.registerReloader(textureValidator);
    manager.registerReloader(HarvestLevels.INSTANCE);
  }
}
