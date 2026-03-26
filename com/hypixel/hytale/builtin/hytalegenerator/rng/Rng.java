package com.hypixel.hytale.builtin.hytalegenerator.rng;

public class Rng {
   public static final int BIT_NOISE_0 = 1759714724;
   public static final int BIT_NOISE_1 = -1255572915;
   public static final int BIT_NOISE_2 = 458671337;
   public static final int PRIME_0 = 198491317;
   public static final int PRIME_1 = 6542989;

   public static int getRandomInt(int seed, int key) {
      int bits = key * 1759714724;
      bits += seed;
      bits ^= bits >>> 8;
      bits -= 1255572915;
      bits ^= bits << 8;
      bits *= 458671337;
      return bits ^ bits >>> 8;
   }

   public static int mix(int seed, int a, int b) {
      return getRandomInt(seed, a + 198491317 * b);
   }

   public static int mix(int seed, int a, int b, int c) {
      return getRandomInt(seed, a + 198491317 * b + 6542989 * c);
   }

   public static long splitMixLong(long n) {
      n = (n ^ n >>> 30) * -4658895280553007687L;
      n = (n ^ n >>> 27) * -7723592293110705685L;
      return n ^ n >>> 31;
   }

   public static int splitMixInteger(int n) {
      n = (n ^ n >>> 16) * -2048144789;
      n = (n ^ n >>> 13) * -1028477387;
      return n ^ n >>> 16;
   }

   public static int rotateLeft(int bits, int distance) {
      return bits << distance | bits >>> 32 - distance;
   }
}
