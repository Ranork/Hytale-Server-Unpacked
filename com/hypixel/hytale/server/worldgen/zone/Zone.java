package com.hypixel.hytale.server.worldgen.zone;

import com.hypixel.hytale.math.vector.Vector2i;
import com.hypixel.hytale.server.worldgen.biome.BiomePatternGenerator;
import com.hypixel.hytale.server.worldgen.cave.CaveGenerator;
import com.hypixel.hytale.server.worldgen.container.UniquePrefabContainer;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public record Zone(
   int id,
   @Nonnull String name,
   @Nonnull ZoneDiscoveryConfig discoveryConfig,
   @Nullable CaveGenerator caveGenerator,
   @Nonnull BiomePatternGenerator biomePatternGenerator,
   @Nonnull UniquePrefabContainer uniquePrefabContainer
) {
   @Override
   public int hashCode() {
      return this.id;
   }

   @Nonnull
   @Override
   public String toString() {
      return "Zone{id="
         + this.id
         + ", name='"
         + this.name
         + "', discoveryConfig="
         + this.discoveryConfig
         + ", caveGenerator="
         + this.caveGenerator
         + ", biomePatternGenerator="
         + this.biomePatternGenerator
         + ", uniquePrefabContainer="
         + this.uniquePrefabContainer
         + "}";
   }

   public record Unique(@Nonnull Zone zone, @Nonnull CompletableFuture<Vector2i> position) {
      public Vector2i getPosition() {
         return this.position.join();
      }
   }

   public record UniqueCandidate(@Nonnull Zone.UniqueEntry zone, @Nonnull Vector2i[] positions) {
      public static final Zone.UniqueCandidate[] EMPTY_ARRAY = new Zone.UniqueCandidate[0];
   }

   public record UniqueEntry(@Nonnull Zone zone, int color, int[] parent, int radius, int padding) {
      @Nonnull
      public static final Zone.UniqueEntry[] EMPTY_ARRAY = new Zone.UniqueEntry[0];

      public boolean matchesParent(int color) {
         for (int p : this.parent) {
            if (p == color) {
               return true;
            }
         }

         return false;
      }
   }
}
