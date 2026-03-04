package com.hypixel.hytale.server.worldgen.container;

import com.hypixel.hytale.math.util.FastRandom;
import com.hypixel.hytale.math.vector.Vector2i;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.prefab.PrefabRotation;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.IPrefabBuffer;
import com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator;
import com.hypixel.hytale.server.worldgen.loader.WorldGenPrefabSupplier;
import com.hypixel.hytale.server.worldgen.prefab.PrefabCategory;
import com.hypixel.hytale.server.worldgen.prefab.unique.UniquePrefabConfiguration;
import com.hypixel.hytale.server.worldgen.prefab.unique.UniquePrefabGenerator;
import com.hypixel.hytale.server.worldgen.util.bounds.IChunkBounds;
import com.hypixel.hytale.server.worldgen.util.condition.BlockMaskCondition;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UniquePrefabContainer {
   protected final int seedOffset;
   protected final UniquePrefabGenerator[] generators;

   public UniquePrefabContainer(int seedOffset, UniquePrefabGenerator[] generators) {
      this.seedOffset = seedOffset;
      this.generators = generators;
   }

   public UniquePrefabGenerator[] getGenerators() {
      return this.generators;
   }

   @Nonnull
   public UniquePrefabContainer.UniquePrefabEntry[] generate(int seed, @Nullable Vector2i position, @Nonnull ChunkGenerator chunkGenerator) {
      Random random = new FastRandom(seed + this.seedOffset);
      UniquePrefabContainer.UniquePrefabEntry[] entries = new UniquePrefabContainer.UniquePrefabEntry[this.generators.length];

      for (int i = 0; i < this.generators.length; i++) {
         UniquePrefabGenerator generator = this.generators[i];
         UniquePrefabConfiguration configuration = generator.getConfiguration();
         Vector3i location = generator.generate(seed, position, chunkGenerator, random, configuration.getMaxAttempts(), entries);
         entries[i] = new UniquePrefabContainer.UniquePrefabEntry(
            generator.getName(),
            generator.getCategory(),
            location,
            generator.generatePrefab(random),
            configuration.getPlacementConfiguration(),
            configuration.getRotation(random),
            configuration.getSpawnOffset(),
            configuration.getEnvironmentId(),
            configuration.isFitHeightmap(),
            configuration.isSubmerge(),
            configuration.getExclusionRadiusSquared(),
            configuration.isSpawnLocation(),
            configuration.isShowOnMap()
         );
      }

      return entries;
   }

   @Nonnull
   @Override
   public String toString() {
      return "UniquePrefabContainer{seedOffset=" + this.seedOffset + ", generators=" + Arrays.toString((Object[])this.generators) + "}";
   }

   public static class UniquePrefabEntry {
      protected final String name;
      protected final PrefabCategory category;
      @Nonnull
      protected final Vector3i position;
      @Nonnull
      protected final WorldGenPrefabSupplier prefabSupplier;
      protected final BlockMaskCondition configuration;
      @Nonnull
      protected final PrefabRotation rotation;
      protected final Vector3d spawnOffset;
      protected final int lowBoundX;
      protected final int lowBoundY;
      protected final int lowBoundZ;
      protected final int highBoundX;
      protected final int highBoundY;
      protected final int highBoundZ;
      protected final int environmentId;
      protected final boolean fitHeightmap;
      protected final boolean submerge;
      protected final double exclusionRadiusSquared;
      protected final boolean spawnLocation;
      protected final boolean showOnMap;

      private UniquePrefabEntry(
         String name,
         @Nonnull PrefabCategory category,
         @Nonnull Vector3i position,
         @Nonnull WorldGenPrefabSupplier prefabSupplier,
         BlockMaskCondition configuration,
         @Nonnull PrefabRotation rotation,
         Vector3d spawnOffset,
         int environmentId,
         boolean fitHeightmap,
         boolean submergeable,
         double exclusionRadiusSquared,
         boolean spawnLocation,
         boolean showOnMap
      ) {
         this.name = name;
         this.category = category;
         this.position = position;
         this.prefabSupplier = prefabSupplier;
         this.configuration = configuration;
         this.rotation = rotation;
         this.spawnOffset = spawnOffset;
         this.environmentId = environmentId;
         this.fitHeightmap = fitHeightmap;
         this.submerge = submergeable;
         this.exclusionRadiusSquared = exclusionRadiusSquared;
         this.spawnLocation = spawnLocation;
         this.showOnMap = showOnMap;
         IPrefabBuffer prefab = Objects.requireNonNull(prefabSupplier.get());
         IChunkBounds bounds = prefabSupplier.getBounds(prefab);
         this.lowBoundY = position.y + prefab.getMinY();
         this.highBoundY = position.y + prefab.getMaxY();
         this.lowBoundX = position.x + bounds.getLowBoundX(rotation);
         this.lowBoundZ = position.z + bounds.getLowBoundZ(rotation);
         this.highBoundX = position.x + bounds.getHighBoundX(rotation);
         this.highBoundZ = position.z + bounds.getHighBoundZ(rotation);
      }

      public String getName() {
         return this.name;
      }

      @Nonnull
      public PrefabCategory getCategory() {
         return this.category;
      }

      @Nonnull
      public Vector3i getPosition() {
         return this.position;
      }

      @Nonnull
      public WorldGenPrefabSupplier getPrefabSupplier() {
         return this.prefabSupplier;
      }

      public BlockMaskCondition getConfiguration() {
         return this.configuration;
      }

      @Nonnull
      public PrefabRotation getRotation() {
         return this.rotation;
      }

      public Vector3d getSpawnOffset() {
         return this.spawnOffset;
      }

      public int getLowBoundX() {
         return this.lowBoundX;
      }

      public int getLowBoundY() {
         return this.lowBoundY;
      }

      public int getLowBoundZ() {
         return this.lowBoundZ;
      }

      public int getHighBoundX() {
         return this.highBoundX;
      }

      public int getHighBoundY() {
         return this.highBoundY;
      }

      public int getHighBoundZ() {
         return this.highBoundZ;
      }

      public int getEnvironmentId() {
         return this.environmentId;
      }

      public boolean isFitHeightmap() {
         return this.fitHeightmap;
      }

      public boolean isSubmerge() {
         return this.submerge;
      }

      public double getExclusionRadiusSquared() {
         return this.exclusionRadiusSquared;
      }

      public boolean isSpawnLocation() {
         return this.spawnLocation;
      }

      public boolean isShowOnMap() {
         return this.showOnMap;
      }

      @Nonnull
      @Override
      public String toString() {
         return "UniquePrefabEntry{position="
            + this.position
            + ", prefabSupplier="
            + this.prefabSupplier
            + ", configuration="
            + this.configuration
            + ", rotation="
            + this.rotation
            + ", lowBoundX="
            + this.lowBoundX
            + ", lowBoundZ="
            + this.lowBoundZ
            + ", highBoundX="
            + this.highBoundX
            + ", highBoundZ="
            + this.highBoundZ
            + ", fitHeightmap="
            + this.fitHeightmap
            + ", submerge="
            + this.submerge
            + ", exclusionRadiusSquared="
            + this.exclusionRadiusSquared
            + ", spawnLocation="
            + this.spawnLocation
            + "}";
      }
   }
}
