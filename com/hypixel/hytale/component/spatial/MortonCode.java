package com.hypixel.hytale.component.spatial;

public class MortonCode {
   private static final int BITS_PER_AXIS = 21;
   private static final long MAX_COORD = 2097151L;

   public static long encode(double x, double y, double z, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
      double nx = (x - minX) / (maxX - minX);
      double ny = (y - minY) / (maxY - minY);
      double nz = (z - minZ) / (maxZ - minZ);
      long ix = Math.min(Math.max((long)(nx * 2097151.0), 0L), 2097151L);
      long iy = Math.min(Math.max((long)(ny * 2097151.0), 0L), 2097151L);
      long iz = Math.min(Math.max((long)(nz * 2097151.0), 0L), 2097151L);
      return interleaveBits(ix, iy, iz);
   }

   private static long interleaveBits(long x, long y, long z) {
      x = expandBits(x);
      y = expandBits(y);
      z = expandBits(z);
      return x | y << 1 | z << 2;
   }

   private static long expandBits(long value) {
      value &= 2097151L;
      value = (value | value << 32) & 8725724278095871L;
      value = (value | value << 16) & 8725728556220671L;
      value = (value | value << 8) & 1157144660301377551L;
      value = (value | value << 4) & 75488908039734028L;
      return (value | value << 2) & 1317624576693539401L;
   }
}
