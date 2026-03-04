package com.hypixel.hytale.builtin.hytalegenerator.chunkgenerator;

import java.util.Objects;
import java.util.function.LongPredicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public record ChunkRequest(@Nonnull ChunkRequest.GeneratorProfile generatorProfile, @Nonnull ChunkRequest.Arguments arguments) {
   public record Arguments(int seed, long index, int x, int z, @Nullable LongPredicate stillNeeded) {
   }

   public static final class GeneratorProfile {
      @Nonnull
      private final String worldStructureName;
      private int seed;
      private int worldCounter;

      public GeneratorProfile(@Nonnull String worldStructureName, int seed, int worldCounter) {
         this.worldStructureName = worldStructureName;
         this.seed = seed;
         this.worldCounter = worldCounter;
      }

      @Nonnull
      public String worldStructureName() {
         return this.worldStructureName;
      }

      public int seed() {
         return this.seed;
      }

      public void setSeed(int seed) {
         this.seed = seed;
      }

      @Override
      public boolean equals(Object o) {
         return !(o instanceof ChunkRequest.GeneratorProfile that)
            ? false
            : this.seed == that.seed && this.worldCounter == that.worldCounter && Objects.equals(this.worldStructureName, that.worldStructureName);
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.worldStructureName, this.seed, this.worldCounter);
      }

      public ChunkRequest.GeneratorProfile clone() {
         return new ChunkRequest.GeneratorProfile(this.worldStructureName, this.seed, this.worldCounter);
      }

      @Nonnull
      @Override
      public String toString() {
         return "GeneratorProfile{worldStructureName='" + this.worldStructureName + "', seed=" + this.seed + ", worldCounter=" + this.worldCounter + "}";
      }
   }
}
