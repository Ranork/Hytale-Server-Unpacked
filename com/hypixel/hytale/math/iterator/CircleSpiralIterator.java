package com.hypixel.hytale.math.iterator;

import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import java.util.NoSuchElementException;

public class CircleSpiralIterator {
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
   private long radiusFromSq;
   private long radiusToSq;
   private boolean hasNext;
   private long nextChunk;

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
         this.radiusFromSq = (long)radiusFrom * radiusFrom;
         this.radiusToSq = (long)radiusTo * radiusTo;
         long widthTo = 1L + radiusTo * 2L;
         this.maxI = widthTo * widthTo;
         if (radiusFrom != 0) {
            float halfFrom = radiusFrom / 2.0F;
            float sq = halfFrom * halfFrom;
            int diagRadius = (int)Math.sqrt(sq + sq);
            long widthFrom = 1L + diagRadius * 2L;
            this.i = widthFrom * widthFrom;
            long pos = SpiralIterator.getPosFromIndex((int)this.i);
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

         this.hasNext = false;
         this.prepareNext();
         this.setup = true;
      }
   }

   public void reset() {
      this.setup = false;
      this.hasNext = false;
   }

   public long next() {
      if (!this.setup) {
         throw new IllegalStateException("SpiralIterator is not setup!");
      } else {
         if (!this.hasNext) {
            this.prepareNext();
         }

         if (!this.hasNext) {
            throw new NoSuchElementException("No more positions inside the circle!");
         } else {
            this.hasNext = false;
            return this.nextChunk;
         }
      }
   }

   public boolean hasNext() {
      if (!this.setup) {
         throw new IllegalStateException("SpiralIterator is not setup!");
      } else {
         if (!this.hasNext) {
            this.prepareNext();
         }

         return this.hasNext;
      }
   }

   public int getCurrentRadius() {
      return MathUtil.ceil((Math.sqrt(this.i) - 1.0) / 2.0);
   }

   public int getCompletedRadius() {
      return (int)((Math.sqrt(this.i) - 1.0) / 2.0);
   }

   private void prepareNext() {
      while (!this.hasNext && this.i < this.maxI) {
         long rx = this.x;
         long rz = this.z;
         long radiusSq = rx * rx + rz * rz;
         if (radiusSq >= this.radiusFromSq && radiusSq <= this.radiusToSq) {
            this.nextChunk = ChunkUtil.indexChunk(this.chunkX + this.x, this.chunkZ + this.z);
            this.hasNext = true;
         }

         if (this.x == this.z || this.x < 0 && this.x == -this.z || this.x > 0 && this.x == 1 - this.z) {
            int tempDx = this.dx;
            this.dx = -this.dz;
            this.dz = tempDx;
         }

         this.x = this.x + this.dx;
         this.z = this.z + this.dz;
         this.i++;
      }
   }
}
