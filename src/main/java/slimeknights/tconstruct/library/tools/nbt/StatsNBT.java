package slimeknights.tconstruct.library.tools.nbt;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;
import slimeknights.tconstruct.library.utils.NBTUtil;

/**
 * All the stats that every tool must have.
 * Some may not be used explicitly by all tools (e.g. weapons and harvest  level)
 */
public class StatsNBT {
  static final StatsNBT EMPTY = new StatsNBT(1, 0, 1, 1, 1);

  protected static final String TAG_DURABILITY = "durability";
  protected static final String TAG_ATTACK_DAMAGE = "attack";
  protected static final String TAG_ATTACK_SPEED = "attack_speed";
  protected static final String TAG_MINING_SPEED = "mining_speed";
  protected static final String TAG_HARVEST_LEVEL = "harvest_level";

  /** Total durability for the tool */
  private final int durability;
  /** Harvest level when mining on the tool */
  private final int harvestLevel;
  /** Base dealt by the tool */
  private final float attackDamage;
  /** Base mining speed */
  private final float miningSpeed;
  /** Value to multiply by attack speed, larger values are faster */
  private final float attackSpeed;

  public StatsNBT(int durability, int harvestLevel, float attackDamage, float miningSpeed, float attackSpeed) {
    this.durability = durability;
    this.harvestLevel = harvestLevel;
    this.attackDamage = attackDamage;
    this.miningSpeed = miningSpeed;
    this.attackSpeed = attackSpeed;
  }

  /** Parses the stats from NBT */
  public static StatsNBT readFromNBT(@Nullable Tag inbt) {
    if (inbt == null || inbt.getType() != NbtType.COMPOUND) {
      return EMPTY;
    }

    CompoundTag nbt = (CompoundTag)inbt;
    int durability = NBTUtil.getInt(nbt, TAG_DURABILITY, EMPTY.durability);
    int harvestLevel = NBTUtil.getInt(nbt, TAG_HARVEST_LEVEL, EMPTY.harvestLevel);
    float attack = NBTUtil.getFloat(nbt, TAG_ATTACK_DAMAGE, EMPTY.attackDamage);
    float miningSpeed = NBTUtil.getFloat(nbt, TAG_MINING_SPEED, EMPTY.miningSpeed);
    float attackSpeedMultiplier = NBTUtil.getFloat(nbt, TAG_ATTACK_SPEED, EMPTY.attackSpeed);

    return new StatsNBT(durability, harvestLevel, attack, miningSpeed, attackSpeedMultiplier);
  }

  /** Writes these stats to NBT */
  public CompoundTag serializeToNBT() {
    CompoundTag nbt = new CompoundTag();
    nbt.putInt(TAG_DURABILITY, durability);
    nbt.putInt(TAG_HARVEST_LEVEL, harvestLevel);
    nbt.putFloat(TAG_ATTACK_DAMAGE, attackDamage);
    nbt.putFloat(TAG_MINING_SPEED, miningSpeed);
    nbt.putFloat(TAG_ATTACK_SPEED, attackSpeed);

    return nbt;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof StatsNBT)) return false;
    final StatsNBT other = (StatsNBT) o;
    if (!other.canEqual((Object) this)) return false;
    if (this.durability != other.durability) return false;
    if (this.harvestLevel != other.harvestLevel) return false;
    if (Float.compare(this.attackDamage, other.attackDamage) != 0) return false;
    if (Float.compare(this.miningSpeed, other.miningSpeed) != 0) return false;
    if (Float.compare(this.attackSpeed, other.attackSpeed) != 0) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof StatsNBT;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = result * PRIME + this.durability;
    result = result * PRIME + this.harvestLevel;
    result = result * PRIME + Float.floatToIntBits(this.attackDamage);
    result = result * PRIME + Float.floatToIntBits(this.miningSpeed);
    result = result * PRIME + Float.floatToIntBits(this.attackSpeed);
    return result;
  }

  public String toString() {
    return "StatsNBT(durability=" + this.durability + ", harvestLevel=" + this.harvestLevel + ", attackDamage=" + this.attackDamage + ", miningSpeed=" + this.miningSpeed + ", attackSpeed=" + this.attackSpeed + ")";
  }

  public int getDurability() {
    return this.durability;
  }

  public int getHarvestLevel() {
    return this.harvestLevel;
  }

  public float getAttackDamage() {
    return this.attackDamage;
  }

  public float getMiningSpeed() {
    return this.miningSpeed;
  }

  public float getAttackSpeed() {
    return this.attackSpeed;
  }
}
