package com.hypixel.hytale.server.core.modules.physics;

import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RestingSupport {
   protected int supportMinX;
   protected int supportMaxX;
   protected int supportMinZ;
   protected int supportMaxZ;
   protected int supportMinY;
   protected int supportMaxY;
   @Nullable
   protected int[] supportBlocks;

   public boolean hasChanged(@Nonnull World world) {
      if (this.supportBlocks == null) {
         return false;
      } else {
         int index = 0;

         for (int z = this.supportMinZ; z <= this.supportMaxZ; z++) {
            for (int x = this.supportMinX; x <= this.supportMaxX; x++) {
               WorldChunk chunk = world.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(x, z));
               if (chunk != null) {
                  for (int y = this.supportMinY; y <= this.supportMaxY; y++) {
                     if (this.supportBlocks[index++] != chunk.getBlock(x, y, z)) {
                        return true;
                     }
                  }
               }
            }
         }

         return false;
      }
   }

   public void rest(@Nonnull World world, @Nonnull Box boundingBox, @Nonnull Vector3d position) {
      if (this.supportBlocks == null) {
         int maxSize = (int)(Math.ceil(boundingBox.width() + 1.0) * Math.ceil(boundingBox.depth() + 1.0) * Math.ceil(boundingBox.height() + 1.0));
         this.supportBlocks = new int[maxSize];
      }

      this.supportMinX = MathUtil.floor(position.x + boundingBox.min.x);
      this.supportMaxX = MathUtil.floor(position.x + boundingBox.max.x);
      this.supportMinZ = MathUtil.floor(position.z + boundingBox.min.z);
      this.supportMaxZ = MathUtil.floor(position.z + boundingBox.max.z);
      this.supportMinY = MathUtil.floor(position.y + boundingBox.min.y);
      this.supportMaxY = MathUtil.floor(position.y + boundingBox.max.y);
      int index = 0;

      for (int z = this.supportMinZ; z <= this.supportMaxZ; z++) {
         for (int x = this.supportMinX; x <= this.supportMaxX; x++) {
            WorldChunk chunk = world.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(x, z));
            if (chunk != null) {
               for (int y = this.supportMinY; y <= this.supportMaxY; y++) {
                  this.supportBlocks[index++] = chunk.getBlock(x, y, z);
               }
            } else {
               for (int y = this.supportMinY; y <= this.supportMaxY; y++) {
                  this.supportBlocks[index++] = 1;
               }
            }
         }
      }
   }

   public void clear() {
      this.supportBlocks = null;
   }
}
