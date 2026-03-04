package com.hypixel.hytale.common.util;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

public class MapUtil {
   @Nonnull
   public static <T, V> Map<T, V> combineUnmodifiable(@Nonnull Map<T, V> one, @Nonnull Map<T, V> two) {
      Map<T, V> map = new Object2ObjectOpenHashMap();
      map.putAll(one);
      map.putAll(two);
      return Collections.unmodifiableMap(map);
   }

   @Nonnull
   public static <T, V, M extends Map<T, V>> Map<T, V> combineUnmodifiable(@Nonnull Map<T, V> one, @Nonnull Map<T, V> two, @Nonnull Supplier<M> supplier) {
      Map<T, V> map = supplier.get();
      map.putAll(one);
      map.putAll(two);
      return Collections.unmodifiableMap(map);
   }

   @Nonnull
   public static <T, V, M extends Map<T, V>> M combine(@Nonnull Map<T, V> one, @Nonnull Map<T, V> two, @Nonnull Supplier<M> supplier) {
      M map = (M)supplier.get();
      map.putAll(one);
      map.putAll(two);
      return map;
   }
}
