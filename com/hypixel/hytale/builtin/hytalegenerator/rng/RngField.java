package com.hypixel.hytale.builtin.hytalegenerator.rng;

public class RngField {
   private final int seed;

   public RngField(int seed) {
      this.seed = seed;
   }

   public int get(int x, int y, int z) {
      return Rng.mix(this.seed, x, y, z);
   }

   public int get(int x, int y) {
      return Rng.mix(this.seed, x, y);
   }

   public int get(double x, double y, double z) {
      int bitsX = Float.floatToRawIntBits((float)x);
      int bitsY = Float.floatToRawIntBits((float)y);
      int bitsZ = Float.floatToRawIntBits((float)z);
      return Rng.mix(this.seed, bitsX, bitsY, bitsZ);
   }

   public int get(double x, double y) {
      int bitsX = Float.floatToRawIntBits((float)x);
      int bitsY = Float.floatToRawIntBits((float)y);
      return Rng.mix(this.seed, bitsX, bitsY);
   }
}
