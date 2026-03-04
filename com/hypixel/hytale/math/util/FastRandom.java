package com.hypixel.hytale.math.util;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class FastRandom extends Random {
   private static final long multiplier = 25214903917L;
   private static final long addend = 11L;
   private static final long mask = 281474976710655L;
   private long seed;

   public FastRandom() {
      this.seed = initialScramble(ThreadLocalRandom.current().nextLong());
   }

   public FastRandom(long seed) {
      this.seed = initialScramble(seed);
   }

   @Override
   public void setSeed(long seed) {
      this.seed = initialScramble(seed);
   }

   private static long initialScramble(long seed) {
      return (seed ^ 25214903917L) & 281474976710655L;
   }

   @Override
   protected int next(int bits) {
      long seed = this.seed;
      seed = seed * 25214903917L + 11L & 281474976710655L;
      this.seed = seed;
      return (int)(seed >>> 48 - bits);
   }

   @Override
   public synchronized double nextGaussian() {
      throw new UnsupportedOperationException();
   }
}
