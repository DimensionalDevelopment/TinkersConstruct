package slimeknights.tconstruct.tools.harvest;

import com.google.common.collect.Sets;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import slimeknights.tconstruct.library.tools.ToolDefinition;
import slimeknights.tconstruct.library.tools.helper.AOEToolHarvestLogic;
import slimeknights.tconstruct.library.tools.helper.ToolAttackUtil;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.tools.TinkerTools;

import java.util.Set;

public class AxeTool extends HarvestTool {
  private static final Set<Material> EXTRA_MATERIALS = Sets.newHashSet(Material.WOOD, Material.NETHER_WOOD, Material.PLANT, Material.REPLACEABLE_PLANT, Material.BAMBOO, Material.GOURD, Material.LEAVES);
  public static final AOEToolHarvestLogic HARVEST_LOGIC = new MaterialHarvestLogic(EXTRA_MATERIALS, 1, 1, 1);
  public AxeTool(Settings properties, ToolDefinition toolDefinition) {
    super(properties, toolDefinition);
  }

  @Override
  public AOEToolHarvestLogic getToolHarvestLogic() {
    return HARVEST_LOGIC;
  }

  @Override
  public ActionResult useOnBlock(ItemUsageContext context) {
    return this.getToolHarvestLogic().transformBlocks(context, FabricToolTags.AXES, SoundEvents.ITEM_AXE_STRIP, false);
  }

  @Override
  public boolean dealDamage(ToolStack tool, LivingEntity player, Entity entity, float damage, boolean isCriticalHit, boolean fullyCharged) {
    boolean hit = super.dealDamage(tool, player, entity, damage, isCriticalHit, fullyCharged);
    if (hit && fullyCharged) {
      ToolAttackUtil.spawnAttachParticle(TinkerTools.axeAttackParticle, player, 0.8d);
    }
    return hit;
  }

//  @Override
//  public boolean canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity, LivingEntity attacker) {
//    return true;
//  }
}
