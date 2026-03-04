package com.hypixel.hytale.server.worldgen.container;

import com.hypixel.hytale.common.map.IWeightedMap;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.server.worldgen.loader.WorldGenPrefabSupplier;
import com.hypixel.hytale.server.worldgen.prefab.PrefabPatternGenerator;
import com.hypixel.hytale.server.worldgen.util.bounds.IChunkBounds;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class PrefabContainer {
   private final PrefabContainer.PrefabContainerEntry[] entries;
   private final int maxSize;

   public PrefabContainer(PrefabContainer.PrefabContainerEntry[] entries) {
      this.entries = entries;
      this.maxSize = getMaxSize(entries);
   }

   public PrefabContainer.PrefabContainerEntry[] getEntries() {
      return this.entries;
   }

   public int getMaxSize() {
      return this.maxSize;
   }

   @Nonnull
   @Override
   public String toString() {
      return "PrefabContainer{entries=" + Arrays.toString((Object[])this.entries) + "}";
   }

   private static int getMaxSize(PrefabContainer.PrefabContainerEntry[] entries) {
      int max = 0;

      for (PrefabContainer.PrefabContainerEntry entry : entries) {
         max = Math.max(max, entry.getPrefabPatternGenerator().getMaxSize());
      }

      return max;
   }

   public static class PrefabContainerEntry {
      protected final IWeightedMap<WorldGenPrefabSupplier> prefabs;
      protected final PrefabPatternGenerator prefabPatternGenerator;
      protected final int environmentId;
      protected int extend = -1;

      public PrefabContainerEntry(IWeightedMap<WorldGenPrefabSupplier> prefabs, PrefabPatternGenerator prefabPatternGenerator, int environmentId) {
         this.prefabs = prefabs;
         this.prefabPatternGenerator = prefabPatternGenerator;
         this.environmentId = environmentId;
      }

      public IWeightedMap<WorldGenPrefabSupplier> getPrefabs() {
         return this.prefabs;
      }

      public int getEnvironmentId() {
         return this.environmentId;
      }

      public int getExtents() {
         if (this.extend == -1) {
            int max = 0;

            for (WorldGenPrefabSupplier supplier : this.prefabs.internalKeys()) {
               IChunkBounds bounds = supplier.getBounds(supplier.get());
               int lengthX = bounds.getHighBoundX() - bounds.getLowBoundX();
               int lengthZ = bounds.getHighBoundZ() - bounds.getLowBoundZ();
               max = MathUtil.maxValue(max, lengthX, lengthZ);
            }

            this.extend = max;
         }

         return this.extend;
      }

      public PrefabPatternGenerator getPrefabPatternGenerator() {
         return this.prefabPatternGenerator;
      }

      @Nonnull
      @Override
      public String toString() {
         return "PrefabContainerEntry{prefabs=" + this.prefabs + ", prefabPatternGenerator=" + this.prefabPatternGenerator + ", extend=" + this.extend + "}";
      }
   }
}
