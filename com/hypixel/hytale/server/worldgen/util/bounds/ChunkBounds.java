package com.hypixel.hytale.server.worldgen.util.bounds;

import com.hypixel.hytale.math.util.MathUtil;
import javax.annotation.Nonnull;

public class ChunkBounds implements IChunkBounds {
   protected int minX;
   protected int minZ;
   protected int maxX;
   protected int maxZ;

   public ChunkBounds() {
      this(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
   }

   public ChunkBounds(@Nonnull IChunkBounds bounds) {
      this(bounds.getLowBoundX(), bounds.getLowBoundZ(), bounds.getHighBoundX(), bounds.getHighBoundZ());
   }

   public ChunkBounds(int minX, int minZ, int maxX, int maxZ) {
      this.minX = minX;
      this.minZ = minZ;
      this.maxX = maxX;
      this.maxZ = maxZ;
   }

   public ChunkBounds(int x, int z) {
      this.minX = this.maxX = x;
      this.minZ = this.maxZ = z;
   }

   @Override
   public int getLowBoundX() {
      return this.minX;
   }

   @Override
   public int getLowBoundZ() {
      return this.minZ;
   }

   @Override
   public int getHighBoundX() {
      return this.maxX;
   }

   @Override
   public int getHighBoundZ() {
      return this.maxZ;
   }

   public void expandNegative(int x, int z) {
      this.minX += x;
      this.minZ += z;
   }

   public void expandPositive(int x, int z) {
      this.maxX += x;
      this.maxZ += z;
   }

   public void expandNegative(double x, double z) {
      this.minX = MathUtil.floor(this.minX + x);
      this.minZ = MathUtil.floor(this.minZ + z);
   }

   public void expandPositive(double x, double z) {
      this.maxX = MathUtil.ceil(this.maxX + x);
      this.maxZ = MathUtil.ceil(this.maxZ + z);
   }

   public void include(int minX, int minZ, int maxX, int maxZ) {
      if (this.minX > minX) {
         this.minX = minX;
      }

      if (this.minZ > minZ) {
         this.minZ = minZ;
      }

      if (this.maxX < maxX) {
         this.maxX = maxX;
      }

      if (this.maxZ < maxZ) {
         this.maxZ = maxZ;
      }
   }

   public void include(int x, int z) {
      if (this.minX > x) {
         this.minX = x;
      } else if (this.maxX < x) {
         this.maxX = x;
      }

      if (this.minZ > z) {
         this.minZ = z;
      } else if (this.maxZ < z) {
         this.maxZ = z;
      }
   }

   public void include(@Nonnull IChunkBounds box) {
      if (this.minX > box.getLowBoundX()) {
         this.minX = box.getLowBoundX();
      }

      if (this.minZ > box.getLowBoundZ()) {
         this.minZ = box.getLowBoundZ();
      }

      if (this.maxX < box.getHighBoundX()) {
         this.maxX = box.getHighBoundX();
      }

      if (this.maxZ < box.getHighBoundZ()) {
         this.maxZ = box.getHighBoundZ();
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "ChunkBounds{minX=" + this.minX + ", minZ=" + this.minZ + ", maxX=" + this.maxX + ", maxZ=" + this.maxZ + "}";
   }
}
