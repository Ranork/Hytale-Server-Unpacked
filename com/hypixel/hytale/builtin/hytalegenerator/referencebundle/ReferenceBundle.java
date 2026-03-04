package com.hypixel.hytale.builtin.hytalegenerator.referencebundle;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ReferenceBundle {
   @Nonnull
   private final Map<String, Object> dataLayerMap = new HashMap<>();
   @Nonnull
   private final Map<String, Class<?>> layerTypeMap = new HashMap<>();

   public <T> void put(@Nonnull String name, @Nonnull T reference, @Nonnull Class<T> type) {
      this.dataLayerMap.put(name, reference);
      this.layerTypeMap.put(name, type);
   }

   @Nullable
   public <T> T get(@Nonnull String name, @Nonnull Class<T> type) {
      Class<?> storedType = this.layerTypeMap.get(name);

      assert storedType != null;

      assert type.isAssignableFrom(storedType);

      return (T)this.dataLayerMap.get(name);
   }
}
