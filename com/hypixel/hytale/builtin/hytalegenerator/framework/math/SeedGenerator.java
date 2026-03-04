package com.hypixel.hytale.builtin.hytalegenerator.framework.math;

import java.util.Arrays;
import javax.annotation.Nonnull;

public class SeedGenerator {
   @Nonnull
   private final long[] coPrimes;
   private static final long FLOOR = 10000000L;

   public SeedGenerator(long seed) {
      this.coPrimes = CoPrimeGenerator.generateCoPrimes(seed, 100, 7, 10000000L);
   }

   public long seedAt(long x, long y, long z, long w, long k, long t) {
      return (x * this.coPrimes[0] + y * this.coPrimes[1] + z * this.coPrimes[2] + w * this.coPrimes[3] + k * this.coPrimes[4] + t * this.coPrimes[5])
         % this.coPrimes[6];
   }

   public long seedAt(long x, long y, long z, long w, long k) {
      return (x * this.coPrimes[0] + y * this.coPrimes[1] + z * this.coPrimes[2] + w * this.coPrimes[3] + k * this.coPrimes[4]) % this.coPrimes[6];
   }

   public long seedAt(long x, long y, long z, long w) {
      return (x * this.coPrimes[0] + y * this.coPrimes[1] + z * this.coPrimes[2] + w * this.coPrimes[3]) % this.coPrimes[6];
   }

   public long seedAt(long x, long y, long z) {
      return (x * this.coPrimes[0] + y * this.coPrimes[1] + z * this.coPrimes[2]) % this.coPrimes[6];
   }

   public long seedAt(long x, long y) {
      return (x * this.coPrimes[0] + y * this.coPrimes[1]) % this.coPrimes[6];
   }

   public long seedAt(double xd, double yd, double zd, double wd, double kd, double td, double resolution) {
      int x = (int)(xd * resolution);
      int y = (int)(yd * resolution);
      int z = (int)(zd * resolution);
      int w = (int)(wd * resolution);
      int k = (int)(kd * resolution);
      int t = (int)(td * resolution);
      return (x * this.coPrimes[0] + y * this.coPrimes[1] + z * this.coPrimes[2] + w * this.coPrimes[3] + k * this.coPrimes[4] + t * this.coPrimes[5])
         % this.coPrimes[6];
   }

   public long seedAt(double xd, double yd, double zd, double wd, double kd, double resolution) {
      int x = (int)(xd * resolution);
      int y = (int)(yd * resolution);
      int z = (int)(zd * resolution);
      int w = (int)(wd * resolution);
      int k = (int)(kd * resolution);
      return (x * this.coPrimes[0] + y * this.coPrimes[1] + z * this.coPrimes[2] + w * this.coPrimes[3] + k * this.coPrimes[4]) % this.coPrimes[6];
   }

   public long seedAt(double xd, double yd, double zd, double wd, double resolution) {
      int x = (int)(xd * resolution);
      int y = (int)(yd * resolution);
      int z = (int)(zd * resolution);
      int w = (int)(wd * resolution);
      return (x * this.coPrimes[0] + y * this.coPrimes[1] + z * this.coPrimes[2] + w * this.coPrimes[3]) % this.coPrimes[6];
   }

   public long seedAt(double xd, double yd, double zd, double resolution) {
      int x = (int)(xd * resolution);
      int y = (int)(yd * resolution);
      int z = (int)(zd * resolution);
      return (x * this.coPrimes[0] + y * this.coPrimes[1] + z * this.coPrimes[2]) % this.coPrimes[6];
   }

   public long seedAt(double xd, double yd, double resolution) {
      int x = (int)(xd * resolution);
      int y = (int)(yd * resolution);
      return (x * this.coPrimes[0] + y * this.coPrimes[1]) % this.coPrimes[6];
   }

   @Nonnull
   @Override
   public String toString() {
      return "SeedGenerator{coPrimes=" + Arrays.toString(this.coPrimes) + "}";
   }
}
