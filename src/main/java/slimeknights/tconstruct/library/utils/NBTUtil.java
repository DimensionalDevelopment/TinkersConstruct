package slimeknights.tconstruct.library.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.NbtCompound;
import java.util.function.BiFunction;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NBTUtil {
  /**
   * Gets an integer from NBT, or the given default value
   * @param nbt           NBT instance
   * @param key           Key
   * @param defaultValue  Value if missing
   * @return Integer, or default if the tag is missing
   */
  public static int getInt(NbtCompound nbt, String key, int defaultValue) {
    return getOrDefault(nbt, key, defaultValue, NbtCompound::getInt);
  }

  /**
   * Gets an float from NBT, or the given default value
   * @param nbt           NBT instance
   * @param key           Key
   * @param defaultValue  Value if missing
   * @return Integer, or default if the tag is missing
   */
  public static float getFloat(NbtCompound nbt, String key, float defaultValue) {
    return getOrDefault(nbt, key, defaultValue, NbtCompound::getFloat);
  }

  /**
   * Gets an boolean from NBT, or the given default value
   * @param nbt           NBT instance
   * @param key           Key
   * @param defaultValue  Value if missing
   * @return Integer, or default if the tag is missing
   */
  public static boolean getBoolean(NbtCompound nbt, String key, boolean defaultValue) {
    return getOrDefault(nbt, key, defaultValue, NbtCompound::getBoolean);
  }

  /**
   * Gets a number value from NBT, or the given default value
   * @param nbt           NBT instance
   * @param key           Key
   * @param defaultValue  Value if missing
   * @return Integer, or default if the tag is missing
   */
  public static <T> T getOrDefault(NbtCompound nbt, String key, T defaultValue, BiFunction<NbtCompound, String, T> valueGetter) {
    if(nbt.contains(key, NbtType.NUMBER)) {
      return valueGetter.apply(nbt, key);
    }
    return defaultValue;
  }
}
