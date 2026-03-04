package com.hypixel.hytale.server.worldgen.prefab;

import com.google.gson.JsonElement;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;

public record PrefabCategory(String name, int priority) {
   public static final String FILENAME = "PrefabCategories.json";
   public static final int MIN_PRIORITY = Integer.MIN_VALUE;
   public static final int MAX_PRIORITY = Integer.MAX_VALUE;
   public static final PrefabCategory NONE = new PrefabCategory("None", Integer.MIN_VALUE);
   public static final PrefabCategory UNIQUE = new PrefabCategory("Unique", Integer.MAX_VALUE);

   public static void parse(@Nullable JsonElement json, BiConsumer<String, PrefabCategory> consumer) {
      if (json != null && json.isJsonObject()) {
         for (Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
            String name = entry.getKey();
            JsonElement value = entry.getValue();
            if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
               throw new Error(String.format("Invalid prefab category priority for '%s'. Must be an integer", name));
            }

            consumer.accept(name, new PrefabCategory(name, value.getAsInt()));
         }
      }
   }
}
