package com.hypixel.hytale.server.worldgen.util.bounds;

import com.hypixel.hytale.math.util.MathUtil;
import javax.annotation.Nonnull;

public class WorldBounds extends ChunkBounds implements IWorldBounds {
   protected int minY;
   protected int maxY;

   public WorldBounds() {
      this.minY = Integer.MAX_VALUE;
      this.maxY = Integer.MIN_VALUE;
   }

   public WorldBounds(@Nonnull IWorldBounds bounds) {
      super(bounds);
      this.minY = bounds.getLowBoundY();
      this.maxY = bounds.getHighBoundY();
   }

   public WorldBounds(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
      super(minX, minZ, maxX, maxZ);
      this.minY = minY;
      this.maxY = maxY;
   }

   public WorldBounds(int x, int y, int z) {
      super(x, z);
      this.minY = this.maxY = y;
   }

   @Override
   public int getLowBoundY() {
      return this.minY;
   }

   @Override
   public int getHighBoundY() {
      return this.maxY;
   }

   public void expandNegative(double x, double y, double z) {
      this.expandNegative(x, z);
      this.minY = MathUtil.floor(this.minY + y);
   }

   public void expandPositive(double x, double y, double z) {
      this.expandPositive(x, z);
      this.maxY = MathUtil.ceil(this.maxY + y);
   }

   @Override
   public void include(@Nonnull IChunkBounds bounds) {
      super.include(bounds);
      if (bounds instanceof IWorldBounds worldBounds) {
         if (this.minY > worldBounds.getLowBoundY()) {
            this.minY = worldBounds.getLowBoundY();
         }

         if (this.maxY < worldBounds.getHighBoundY()) {
            this.maxY = worldBounds.getHighBoundY();
         }
      }
   }
}
