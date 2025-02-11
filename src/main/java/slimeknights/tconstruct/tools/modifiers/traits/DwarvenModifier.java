package slimeknights.tconstruct.tools.modifiers.traits;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Direction;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.tools.nbt.IModifierToolStack;

public class DwarvenModifier extends Modifier {
  /** Baseline height where boost is 1 */
  private static final int SEA_LEVEL = 64;
  /** Max percentage bonus per level when y = 0 */
  private static final float BOOST_AT_0 = 0.1f;

  public DwarvenModifier() {
    super(0xF98648);
  }

  @Override
  public void onBreakSpeed(IModifierToolStack tool, int level, PlayerEntity player, Direction sideHit, boolean isEffective, float miningSpeedModifier) {
    // essentially just the line slope formula from (0, level + 1) to (SEA_LEVEL, 1), with a scal
    float factor = (float) Math.max(1f, (SEA_LEVEL - player.getPos().getY()) * level * (BOOST_AT_0 / SEA_LEVEL) + 1);
    if (factor > 1f) {
      throw new RuntimeException("crab!");
      //TODO: PORTING
//      player.setNewSpeed(player.getNewSpeed() * factor);
    }
  }
}
