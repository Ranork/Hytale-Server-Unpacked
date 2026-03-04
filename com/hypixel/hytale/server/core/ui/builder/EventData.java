package com.hypixel.hytale.server.core.ui.builder;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;

public record EventData(Map<String, String> events) {
   public EventData() {
      this(new Object2ObjectOpenHashMap());
   }

   @Nonnull
   public EventData append(String key, String value) {
      return this.put(key, value);
   }

   @Nonnull
   public <T extends Enum<T>> EventData append(String key, @Nonnull T enumValue) {
      return this.put(key, enumValue.name());
   }

   @Nonnull
   public EventData put(String key, String value) {
      this.events.put(key, value);
      return this;
   }

   @Nonnull
   public static EventData of(@Nonnull String key, @Nonnull String value) {
      HashMap<String, String> map = new HashMap<>();
      map.put(key, value);
      return new EventData(map);
   }
}
