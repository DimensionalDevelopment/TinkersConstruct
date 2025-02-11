package slimeknights.tconstruct.tools.modifiers.traits;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.tools.nbt.IModifierToolStack;

public class TastyModifier extends Modifier {
  public TastyModifier() {
    super(0xef9e9b);
  }

  @Override
  public void onInventoryTick(IModifierToolStack tool, int level, World world, LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
    // must be holding the tool to eat, or I suppose if this somehow ended up on armor wearing it is fine
    // update once a second, only for players
    if (isCorrectSlot && !tool.isBroken() && holder.age % 20 == 0 && !holder.world.isClient && holder instanceof PlayerEntity) {
      HungerManager foodStats = ((PlayerEntity) holder).getHungerManager();
      // 5% chance of eating per level, more pig iron makes it tastier
      // unless we are starving, then we just immediately eat
      if (!holder.world.isClient && foodStats.isNotFull() && (foodStats.getFoodLevel() < 7 || RANDOM.nextFloat() < (0.01f * level))) {
        // restores 1 per pig iron level, and better pig iron is more filling
        foodStats.add(level, level * 0.1f);
        // 5 damage for a bite per level, does not process reinforced/overslime, your teeth are tough
        tool.setDamage(tool.getDamage() + (5 * level));
        // play the munch sound
        holder.getEntityWorld().playSound(null, holder.getBlockPos(), SoundEvents.ENTITY_GENERIC_EAT, SoundCategory.PLAYERS, 0.8f, 1.0f);
      }
    }
  }
}
