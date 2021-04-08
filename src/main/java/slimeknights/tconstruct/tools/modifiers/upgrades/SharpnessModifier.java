package slimeknights.tconstruct.tools.modifiers.upgrades;

import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableText;
import slimeknights.tconstruct.library.modifiers.IncrementalModifier;
import slimeknights.tconstruct.library.tools.ModifierStatsBuilder;
import slimeknights.tconstruct.library.tools.ToolDefinition;
import slimeknights.tconstruct.library.tools.nbt.IModDataReadOnly;
import slimeknights.tconstruct.library.tools.nbt.StatsNBT;

public class SharpnessModifier extends IncrementalModifier {
  public SharpnessModifier() {
    super(0xEAE5DE);
  }

  @Override
  public Text getDisplayName(int level) {
    // displays special names for levels of sharpness
    if (level <= 5) {
      return new TranslatableText(getTranslationKey() + "." + level)
        .styled(style -> style.withColor(TextColor.fromRgb(getColor())));
    }
    return super.getDisplayName(level);
  }

  @Override
  public void addToolStats(ToolDefinition toolDefinition, StatsNBT baseStats, IModDataReadOnly persistentData, IModDataReadOnly volatileData, int level, ModifierStatsBuilder builder) {
    // vanilla give +1, 1.5, 2, 2.5, 3
    // to make up for that 0.5 on the first level, we give a small bonus after the first level
    float scaledLevel = getScaledLevel(persistentData, level);
    if (scaledLevel >= 1) {
      scaledLevel += 1;
    }
    builder.addAttackDamage(scaledLevel * 0.5f);
  }
}
