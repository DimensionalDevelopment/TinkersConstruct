package slimeknights.tconstruct.library.modifiers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;
import slimeknights.tconstruct.library.TinkerRegistries;

import java.lang.reflect.Type;

/**
 * Data class holding a modifier with a level
 */
public class ModifierEntry implements Comparable<ModifierEntry> {
  /** JSON serialzier instance for GSON */
  public static final Serializer SERIALIZER = new Serializer();

  private final Modifier modifier;
  private final int level;

  public ModifierEntry(Modifier modifier, int level) {
    this.modifier = modifier;
    this.level = level;
  }

  @Override
  public int compareTo(ModifierEntry other) {
    Modifier mod1 = this.getModifier(), mod2 = other.getModifier();
    int priority1 = mod1.getPriority(), priority2 = mod2.getPriority();
    // sort by priority first if different
    if (priority1 != priority2) {
      // reversed order so higher goes first
      return Integer.compare(priority2, priority1);
    }
    // fallback to ID path, approximates localized name so we get mostly alphabetical sort in the tooltip
    return mod1.getId().getPath().compareTo(mod2.getId().getPath());
  }

  /**
   * Parses a modifier entry from JSON
   * @param json  JSON object
   * @return  Parsed JSON
   */
  public static ModifierEntry fromJson(JsonObject json) {
    Identifier name = new Identifier(JsonHelper.getString(json, "name"));
    if (!TinkerRegistries.EMPTY.equals(name) && TinkerRegistries.MODIFIERS.containsId(name)) {
      return new ModifierEntry(TinkerRegistries.MODIFIERS.get(name), JsonHelper.getInt(json, "level", 1));
    }
    throw new JsonSyntaxException("Unable to find modifier " + name);
  }

  /**
   * Converts this entry to JSON
   * @return  Json object of entry
   */
  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    json.addProperty("name", modifier.getId().toString());
    json.addProperty("level", level);
    return json;
  }

  /**
   * Reads this modifier entry from the packet buffer
   * @param buffer  Buffer instance
   * @return  Read entry
   */
  public static ModifierEntry read(PacketByteBuf buffer) {
    return new ModifierEntry(TinkerRegistries.MODIFIERS.get(buffer.readVarInt()), buffer.readVarInt());
  }

  /**
   * Writes this modifier entry to the packet buffer
   * @param buffer  Buffer instance
   */
  public void write(PacketByteBuf buffer) {
    buffer.writeVarInt(TinkerRegistries.MODIFIERS.getRawId(modifier));
    buffer.writeVarInt(level);
  }

  public Modifier getModifier() {
    return modifier;
  }

  public int getLevel() {
    return level;
  }

  private static class Serializer implements JsonDeserializer<ModifierEntry>, JsonSerializer<ModifierEntry> {
    @Override
    public ModifierEntry deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
      return fromJson(JsonHelper.asObject(json, "modifier"));
    }

    @Override
    public JsonElement serialize(ModifierEntry entry, Type type, JsonSerializationContext context) {
      return entry.toJson();
    }
  }
}
