package com.hypixel.hytale.server.core.modules.i18n.generator;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TranslationMap {
   @Nonnull
   private LinkedHashMap<String, String> map = new LinkedHashMap<>();

   public TranslationMap() {
   }

   public TranslationMap(Map<String, String> initial) {
      this.map.putAll(initial);
   }

   public TranslationMap(@Nonnull Properties initial) {
      for (String key : initial.stringPropertyNames()) {
         this.map.put(key, initial.getProperty(key));
      }
   }

   @Nullable
   public String get(String key) {
      return this.map.get(key);
   }

   public void put(String key, String value) {
      this.map.put(key, value);
   }

   public void removeKeys(@Nonnull Collection<? extends String> keys) {
      this.map.keySet().removeAll(keys);
   }

   public int size() {
      return this.map.size();
   }

   public void putAbsentKeys(@Nonnull TranslationMap other) {
      for (Entry<String, String> e : other.map.entrySet()) {
         String key = e.getKey();
         String otherValue = e.getValue();
         this.map.putIfAbsent(key, otherValue);
      }
   }

   public void sortByKeyBeforeFirstDot() {
      List<String> keys = new ObjectArrayList(this.map.keySet());
      Comparator<String> comparator = Comparator.<String, String>comparing(fullKey -> {
         int firstDotIndex = fullKey.indexOf(46);
         return (String)(firstDotIndex == -1 ? fullKey : fullKey.substring(0, firstDotIndex));
      }).thenComparing(fullKey -> {
         int firstDotIndex = fullKey.indexOf(46);
         return firstDotIndex == -1 ? "" : fullKey.substring(firstDotIndex + 1);
      });
      keys.sort(comparator);
      LinkedHashMap<String, String> sorted = new LinkedHashMap<>();

      for (String key : keys) {
         sorted.put(key, this.map.get(key));
      }

      this.map = sorted;
   }

   @Nonnull
   public Map<String, String> asMap() {
      return Collections.unmodifiableMap(this.map);
   }
}
