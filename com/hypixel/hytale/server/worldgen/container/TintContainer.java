package com.hypixel.hytale.server.worldgen.container;

import com.hypixel.hytale.common.map.IWeightedMap;
import com.hypixel.hytale.procedurallib.condition.DefaultCoordinateCondition;
import com.hypixel.hytale.procedurallib.condition.ICoordinateCondition;
import com.hypixel.hytale.procedurallib.property.NoiseProperty;
import java.util.List;
import javax.annotation.Nonnull;

public class TintContainer {
   private final TintContainer.DefaultTintContainerEntry defaultEntry;
   private final List<TintContainer.TintContainerEntry> entries;

   public TintContainer(TintContainer.DefaultTintContainerEntry defaultEntry, List<TintContainer.TintContainerEntry> entries) {
      this.defaultEntry = defaultEntry;
      this.entries = entries;
   }

   public int getTintColorAt(int seed, int x, int z) {
      for (int i = 0; i < this.entries.size(); i++) {
         if (this.entries.get(i).shouldGenerate(seed, x, z)) {
            return this.entries.get(i).getTintColorAt(seed, x, z);
         }
      }

      return this.defaultEntry.getTintColorAt(seed, x, z);
   }

   @Nonnull
   @Override
   public String toString() {
      return "TintContainer{defaultEntry=" + this.defaultEntry + ", entries=" + this.entries + "}";
   }

   public static class DefaultTintContainerEntry extends TintContainer.TintContainerEntry {
      public DefaultTintContainerEntry(IWeightedMap<Integer> colorMapping, NoiseProperty valueNoise) {
         super(colorMapping, valueNoise, DefaultCoordinateCondition.DEFAULT_TRUE);
      }

      @Nonnull
      @Override
      public String toString() {
         return "DefaultTintContainerEntry{}";
      }
   }

   public static class TintContainerEntry {
      private final IWeightedMap<Integer> colorMapping;
      private final NoiseProperty valueNoise;
      private final ICoordinateCondition mapCondition;

      public TintContainerEntry(IWeightedMap<Integer> colorMapping, NoiseProperty valueNoise, ICoordinateCondition mapCondition) {
         this.colorMapping = colorMapping;
         this.valueNoise = valueNoise;
         this.mapCondition = mapCondition;
      }

      public boolean shouldGenerate(int seed, int x, int z) {
         return this.mapCondition.eval(seed, x, z);
      }

      public int getTintColorAt(int seed, int x, int z) {
         return this.colorMapping.get(seed, x, z, (iSeed, ix, iz, entry) -> entry.valueNoise.get(iSeed, ix, iz), this);
      }

      @Nonnull
      @Override
      public String toString() {
         return "TintContainerEntry{colorMapping=" + this.colorMapping + ", valueNoise=" + this.valueNoise + ", mapCondition=" + this.mapCondition + "}";
      }
   }
}
