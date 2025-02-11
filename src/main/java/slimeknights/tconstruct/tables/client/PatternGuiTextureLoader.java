package slimeknights.tconstruct.tables.client;

import slimeknights.tconstruct.library.client.util.ResourceValidator;

/**
 * Stitches all GUI part textures into the texture sheet
 */
public class PatternGuiTextureLoader extends ResourceValidator {
  /** Singleton instance */
  public static final PatternGuiTextureLoader INSTANCE = new PatternGuiTextureLoader();
  private PatternGuiTextureLoader() {
    super("textures/gui/tinker_pattern", "textures", ".png");
//    FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onTextureStitch);
  }

/*  *//**
   * Called during texture stitch to add the textures in
   * @param event
   *//*
  private void onTextureStitch(TextureStitchEvent.Pre event) {
    if (PlayerScreenHandler.BLOCK_ATLAS_TEXTURE.equals(event.getMap().getId())) {
      this.resources.forEach(event::addSprite);
    }
  }*/
}
