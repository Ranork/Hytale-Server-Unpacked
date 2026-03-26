package com.hypixel.hytale.server.core.command.system;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AbbreviationMap<Value> {
   private final List<Pair<String, Value>> entries;

   private AbbreviationMap(@Nonnull List<Pair<String, Value>> entries) {
      this.entries = entries;
   }

   @Nullable
   public Value get(@Nonnull String input) {
      String lower = input.toLowerCase();
      Value prefixMatch = null;
      boolean prefixAmbiguous = false;
      Value substringMatch = null;
      boolean substringAmbiguous = false;

      for (Pair<String, Value> entry : this.entries) {
         String key = (String)entry.left();
         Value value = (Value)entry.right();
         if (key.equals(lower)) {
            return value;
         }

         if (key.startsWith(lower)) {
            if (prefixMatch == null) {
               prefixMatch = value;
            } else if (!prefixMatch.equals(value)) {
               prefixAmbiguous = true;
            }
         }

         if (key.contains(lower)) {
            if (substringMatch == null) {
               substringMatch = value;
            } else if (!substringMatch.equals(value)) {
               substringAmbiguous = true;
            }
         }
      }

      if (prefixMatch != null && !prefixAmbiguous) {
         return prefixMatch;
      } else {
         return substringMatch != null && !substringAmbiguous ? substringMatch : null;
      }
   }

   @Nonnull
   public static <V> AbbreviationMap.AbbreviationMapBuilder<V> create() {
      return new AbbreviationMap.AbbreviationMapBuilder();
   }

   public static class AbbreviationMapBuilder<Value> {
      private final Map<String, Value> keys = new Object2ObjectOpenHashMap();

      @Nonnull
      public AbbreviationMap.AbbreviationMapBuilder<Value> put(@Nonnull String key, @Nonnull Value value) {
         if (this.keys.putIfAbsent(key.toLowerCase(), value) != null) {
            throw new IllegalArgumentException("Cannot have values with the same key in AbbreviationMap: " + key);
         } else {
            return this;
         }
      }

      @Nonnull
      public AbbreviationMap<Value> build() {
         List<Pair<String, Value>> entries = new ArrayList<>(this.keys.size());

         for (Entry<String, Value> entry : this.keys.entrySet()) {
            entries.add(Pair.of(entry.getKey(), entry.getValue()));
         }

         return new AbbreviationMap<>(Collections.unmodifiableList(entries));
      }
   }
}
