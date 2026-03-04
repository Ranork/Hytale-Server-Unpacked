package com.hypixel.hytale.server.worldgen.biome;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntMaps.EmptyMap;
import javax.annotation.Nonnull;

public class BiomeInterpolation {
   public static final Int2IntMap EMPTY_MAP = new BiomeInterpolation.EmptyInt2IntMap();
   public static final BiomeInterpolation DEFAULT = new BiomeInterpolation(5, EMPTY_MAP);
   protected final int radius;
   protected final Int2IntMap biomeRadii2;

   protected BiomeInterpolation(int radius, Int2IntMap biomeRadii2) {
      this.radius = radius;
      this.biomeRadii2 = biomeRadii2;
   }

   public int getRadius() {
      return this.radius;
   }

   public int getBiomeRadius2(int biome) {
      return this.biomeRadii2.get(biome);
   }

   @Nonnull
   @Override
   public String toString() {
      return "BiomeInterpolation{radius=" + this.radius + ", biomeRadii2=" + this.biomeRadii2 + "}";
   }

   @Nonnull
   public static BiomeInterpolation create(int radius, @Nonnull Int2IntMap biomeRadii2) {
      if (radius == DEFAULT.getRadius() && biomeRadii2.isEmpty()) {
         return DEFAULT;
      } else {
         if (biomeRadii2.isEmpty()) {
            biomeRadii2 = EMPTY_MAP;
         } else {
            biomeRadii2.defaultReturnValue(radius * radius);
         }

         return new BiomeInterpolation(radius, biomeRadii2);
      }
   }

   protected static class EmptyInt2IntMap extends EmptyMap {
      public int defaultReturnValue() {
         return 25;
      }

      public int get(int k) {
         return 25;
      }
   }
}
