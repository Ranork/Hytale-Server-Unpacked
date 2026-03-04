package com.hypixel.hytale.procedurallib.logic;

public class ResultBuffer {
   public static final ResultBuffer.Bounds2d bounds2d = new ResultBuffer.Bounds2d();
   public static final ResultBuffer.ResultBuffer2d buffer2d = new ResultBuffer.ResultBuffer2d();
   public static final ResultBuffer.ResultBuffer3d buffer3d = new ResultBuffer.ResultBuffer3d();

   public static class Bounds2d {
      public double minX;
      public double minY;
      public double maxX;
      public double maxY;

      public void assign(double minX, double minY, double maxX, double maxY) {
         this.minX = minX;
         this.minY = minY;
         this.maxX = maxX;
         this.maxY = maxY;
      }

      public boolean contains(double x, double y) {
         return x >= this.minX && x <= this.maxX && y >= this.minY && y <= this.maxY;
      }
   }

   public static class ResultBuffer2d {
      public int hash;
      public int hash2;
      public int ix;
      public int iy;
      public int ix2;
      public int iy2;
      public double distance;
      public double distance2;
      public double x;
      public double y;
      public double x2;
      public double y2;

      public void register(int hash, int ix, int iy, double distance, double x, double y) {
         if (distance < this.distance) {
            this.ix = ix;
            this.iy = iy;
            this.distance = distance;
            this.x = x;
            this.y = y;
            this.hash = hash;
         }
      }

      public void register2(int hash, int ix, int iy, double distance, double x, double y) {
         if (distance < this.distance) {
            this.distance2 = this.distance;
            this.x2 = this.x;
            this.y2 = this.y;
            this.ix2 = this.ix;
            this.iy2 = this.iy;
            this.distance = distance;
            this.x = x;
            this.y = y;
            this.ix = ix;
            this.iy = iy;
            this.hash2 = this.hash;
            this.hash = hash;
         } else if (distance < this.distance2) {
            this.distance2 = distance;
            this.x2 = x;
            this.y2 = y;
            this.ix2 = ix;
            this.iy2 = iy;
            this.hash2 = hash;
         }
      }
   }

   public static class ResultBuffer3d {
      public int hash;
      public int hash2;
      public int ix;
      public int iy;
      public int iz;
      public int ix2;
      public int iy2;
      public int iz2;
      public double distance;
      public double distance2;
      public double x;
      public double y;
      public double z;
      public double x2;
      public double y2;
      public double z2;

      public void register(int hash, int ix, int iy, int iz, double distance, double x, double y, double z) {
         if (distance < this.distance) {
            this.hash = hash;
            this.ix = ix;
            this.iy = iy;
            this.iz = iz;
            this.distance = distance;
            this.x = x;
            this.y = y;
            this.z = z;
         }
      }

      public void register2(int hash, int ix, int iy, int iz, double distance, double x, double y, double z) {
         if (distance < this.distance) {
            this.distance2 = this.distance;
            this.x2 = this.x;
            this.y2 = this.y;
            this.z2 = this.z;
            this.ix2 = this.ix;
            this.iy2 = this.iy;
            this.iz2 = this.iz;
            this.distance = distance;
            this.x = x;
            this.y = y;
            this.z = z;
            this.ix = ix;
            this.iy = iy;
            this.iz = iz;
            this.hash2 = this.hash;
            this.hash = hash;
         } else if (distance < this.distance2) {
            this.distance2 = distance;
            this.x2 = x;
            this.y2 = y;
            this.z2 = z;
            this.ix2 = ix;
            this.iy2 = iy;
            this.iz2 = iz;
            this.hash2 = hash;
         }
      }
   }
}
