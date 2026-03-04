package com.hypixel.hytale.math.iterator;

import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;

public class SpiralIterator {
   public static final long MAX_RADIUS_LONG = (long)Math.sqrt(9.223372E18F) / 2L - 1L;
   public static final int MAX_RADIUS = (int)MAX_RADIUS_LONG;
   private boolean setup;
   private int chunkX;
   private int chunkZ;
   private long maxI;
   private long i;
   private int x;
   private int z;
   private int dx;
   private int dz;

   public SpiralIterator() {
   }

   public SpiralIterator(int chunkX, int chunkZ, int radius) {
      this.init(chunkX, chunkZ, radius);
   }

   public SpiralIterator(int chunkX, int chunkZ, int radiusFrom, int radiusTo) {
      this.init(chunkX, chunkZ, radiusFrom, radiusTo);
   }

   public void init(int chunkX, int chunkZ, int radiusTo) {
      this.init(chunkX, chunkZ, 0, radiusTo);
   }

   public void init(int chunkX, int chunkZ, int radiusFrom, int radiusTo) {
      if (radiusFrom < 0) {
         throw new IllegalArgumentException("radiusFrom must be >= 0: " + radiusFrom);
      } else if (radiusTo <= 0) {
         throw new IllegalArgumentException("radiusTo must be > 0: " + radiusTo);
      } else if (radiusTo > MAX_RADIUS) {
         throw new IllegalArgumentException("radiusTo must be < MAX_RADIUS " + MAX_RADIUS + ": " + radiusTo);
      } else if (radiusFrom >= radiusTo) {
         throw new IllegalArgumentException("radiusFrom must be < radiusTo: " + radiusFrom + " -> " + radiusTo);
      } else {
         this.chunkX = chunkX;
         this.chunkZ = chunkZ;
         long widthTo = 1L + radiusTo * 2L;
         this.maxI = widthTo * widthTo;
         if (radiusFrom != 0) {
            long widthFrom = 1L + radiusFrom * 2L;
            this.i = widthFrom * widthFrom;
            long pos = getPosFromIndex((int)this.i);
            this.x = ChunkUtil.xOfChunkIndex(pos);
            this.z = ChunkUtil.zOfChunkIndex(pos);
            this.dx = 1;
            this.dz = 0;
         } else {
            this.i = 0L;
            this.x = this.z = 0;
            this.dx = 0;
            this.dz = -1;
         }

         this.setup = true;
      }
   }

   public void reset() {
      this.setup = false;
   }

   public long next() {
      if (!this.setup) {
         throw new IllegalStateException("SpiralIterator is not setup!");
      } else {
         long chunkCoordinates = ChunkUtil.indexChunk(this.chunkX + this.x, this.chunkZ + this.z);
         if (this.x == this.z || this.x < 0 && this.x == -this.z || this.x > 0 && this.x == 1 - this.z) {
            int tempDx = this.dx;
            this.dx = -this.dz;
            this.dz = tempDx;
         }

         this.x = this.x + this.dx;
         this.z = this.z + this.dz;
         this.i++;
         return chunkCoordinates;
      }
   }

   public boolean hasNext() {
      return this.i < this.maxI;
   }

   public boolean isSetup() {
      return this.setup;
   }

   public long getIndex() {
      return this.i;
   }

   public long getMaxIndex() {
      return this.maxI;
   }

   public int getChunkX() {
      return this.chunkX;
   }

   public int getChunkZ() {
      return this.chunkZ;
   }

   public int getX() {
      return this.x;
   }

   public int getZ() {
      return this.z;
   }

   public int getDx() {
      return this.dx;
   }

   public int getDz() {
      return this.dz;
   }

   public int getCurrentRadius() {
      return MathUtil.ceil((Math.sqrt(this.i) - 1.0) / 2.0);
   }

   public int getCompletedRadius() {
      return (int)((Math.sqrt(this.i) - 1.0) / 2.0);
   }

   public static long getPosFromIndex(int index) {
      if (index < 0) {
         throw new IllegalArgumentException("Index mus be >= 0");
      } else {
         index++;
         int k = MathUtil.ceil((Math.sqrt(index) - 1.0) / 2.0);
         int t = 2 * k;
         int m = (int)Math.pow(1 + t, 2.0);
         int m1 = m - t;
         if (index < m1) {
            int m2 = m1 - t;
            if (index < m2) {
               return index >= m2 - t ? ChunkUtil.indexChunk(-k + (m2 - index), k) : ChunkUtil.indexChunk(k, k - (m2 - index - t));
            } else {
               return ChunkUtil.indexChunk(-k, -k + (m1 - index));
            }
         } else {
            return ChunkUtil.indexChunk(k - (m - index), -k);
         }
      }
   }
}
