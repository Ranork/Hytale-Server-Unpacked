package com.hypixel.hytale.server.core.universe.world.accessor;

import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkFlag;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LocalCachedChunkAccessor implements OverridableChunkAccessor<WorldChunk> {
   private final ChunkAccessor<WorldChunk> delegate;
   private final int minX;
   private final int minZ;
   private final int length;
   @Nonnull
   private final WorldChunk[] chunks;

   @Nonnull
   public static LocalCachedChunkAccessor atWorldCoords(ChunkAccessor<WorldChunk> delegate, int centerX, int centerZ, int blockRadius) {
      int chunkRadius = ChunkUtil.chunkCoordinate(blockRadius) + 1;
      return atChunkCoords(delegate, ChunkUtil.chunkCoordinate(centerX), ChunkUtil.chunkCoordinate(centerZ), chunkRadius);
   }

   @Nonnull
   public static LocalCachedChunkAccessor atChunkCoords(ChunkAccessor<WorldChunk> delegate, int centerX, int centerZ, int chunkRadius) {
      return new LocalCachedChunkAccessor(delegate, centerX, centerZ, chunkRadius);
   }

   @Nonnull
   public static LocalCachedChunkAccessor atChunk(ChunkAccessor<WorldChunk> delegate, @Nonnull WorldChunk chunk, int chunkRadius) {
      LocalCachedChunkAccessor accessor = new LocalCachedChunkAccessor(delegate, chunk.getX(), chunk.getZ(), chunkRadius);
      accessor.overwrite(chunk);
      return accessor;
   }

   protected LocalCachedChunkAccessor(ChunkAccessor<WorldChunk> delegate, int centerX, int centerZ, int radius) {
      this.delegate = delegate;
      this.minX = centerX - radius;
      this.minZ = centerZ - radius;
      this.length = radius * 2 + 1;
      this.chunks = new WorldChunk[this.length * this.length];
   }

   public ChunkAccessor getDelegate() {
      return this.delegate;
   }

   public int getMinX() {
      return this.minX;
   }

   public int getMinZ() {
      return this.minZ;
   }

   public int getLength() {
      return this.length;
   }

   public int getCenterX() {
      return this.minX + this.length / 2;
   }

   public int getCenterZ() {
      return this.minZ + this.length / 2;
   }

   public void cacheChunksInRadius() {
      for (int xOffset = 0; xOffset < this.length; xOffset++) {
         for (int zOffset = 0; zOffset < this.length; zOffset++) {
            int arrayIndex = xOffset * this.length + zOffset;
            WorldChunk chunk = this.chunks[arrayIndex];
            if (chunk == null) {
               this.chunks[arrayIndex] = this.delegate.getChunkIfInMemory(ChunkUtil.indexChunk(this.minX + xOffset, this.minZ + zOffset));
            }
         }
      }
   }

   public void overwrite(@Nonnull WorldChunk wc) {
      int x = wc.getX();
      int z = wc.getZ();
      x -= this.minX;
      z -= this.minZ;
      if (x >= 0 && x < this.length && z >= 0 && z < this.length) {
         int arrayIndex = x * this.length + z;
         this.chunks[arrayIndex] = wc;
      }
   }

   public WorldChunk getChunkIfInMemory(long index) {
      int x = ChunkUtil.xOfChunkIndex(index);
      int z = ChunkUtil.zOfChunkIndex(index);
      x -= this.minX;
      z -= this.minZ;
      if (x >= 0 && x < this.length && z >= 0 && z < this.length) {
         int arrayIndex = x * this.length + z;
         WorldChunk chunk = this.chunks[arrayIndex];
         return chunk != null ? chunk : (this.chunks[arrayIndex] = this.delegate.getChunkIfInMemory(index));
      } else {
         return this.delegate.getChunkIfInMemory(index);
      }
   }

   @Nullable
   public WorldChunk getChunkIfInMemory(int x, int z) {
      int xOffset = x - this.minX;
      int zOffset = z - this.minZ;
      if (xOffset >= 0 && xOffset < this.length && zOffset >= 0 && zOffset < this.length) {
         int arrayIndex = xOffset * this.length + zOffset;
         WorldChunk chunk = this.chunks[arrayIndex];
         return chunk != null ? chunk : (this.chunks[arrayIndex] = this.delegate.getChunkIfInMemory(ChunkUtil.indexChunk(x, z)));
      } else {
         return this.delegate.getChunkIfInMemory(ChunkUtil.indexChunk(x, z));
      }
   }

   public WorldChunk loadChunkIfInMemory(long index) {
      int x = ChunkUtil.xOfChunkIndex(index);
      int z = ChunkUtil.zOfChunkIndex(index);
      x -= this.minX;
      z -= this.minZ;
      if (x >= 0 && x < this.length && z >= 0 && z < this.length) {
         int arrayIndex = x * this.length + z;
         WorldChunk chunk = this.chunks[arrayIndex];
         if (chunk != null) {
            chunk.setFlag(ChunkFlag.TICKING, true);
            return chunk;
         } else {
            return this.chunks[arrayIndex] = this.delegate.loadChunkIfInMemory(index);
         }
      } else {
         return this.delegate.loadChunkIfInMemory(index);
      }
   }

   @Nullable
   public WorldChunk getChunkIfLoaded(long index) {
      int x = ChunkUtil.xOfChunkIndex(index);
      int z = ChunkUtil.zOfChunkIndex(index);
      x -= this.minX;
      z -= this.minZ;
      if (x >= 0 && x < this.length && z >= 0 && z < this.length) {
         int arrayIndex = x * this.length + z;
         WorldChunk chunk = this.chunks[arrayIndex];
         if (chunk == null) {
            chunk = this.chunks[arrayIndex] = this.delegate.getChunkIfInMemory(index);
         }

         return chunk != null && chunk.is(ChunkFlag.TICKING) ? chunk : null;
      } else {
         return this.delegate.getChunkIfLoaded(index);
      }
   }

   @Nullable
   public WorldChunk getChunkIfLoaded(int x, int z) {
      int xOffset = x - this.minX;
      int zOffset = z - this.minZ;
      if (xOffset >= 0 && xOffset < this.length && zOffset >= 0 && zOffset < this.length) {
         int arrayIndex = xOffset * this.length + zOffset;
         WorldChunk chunk = this.chunks[arrayIndex];
         if (chunk == null) {
            chunk = this.chunks[arrayIndex] = this.delegate.getChunkIfInMemory(ChunkUtil.indexChunk(x, z));
         }

         return chunk != null && chunk.is(ChunkFlag.TICKING) ? chunk : null;
      } else {
         return this.delegate.getChunkIfLoaded(ChunkUtil.indexChunk(x, z));
      }
   }

   @Nullable
   public WorldChunk getChunkIfNonTicking(long index) {
      int x = ChunkUtil.xOfChunkIndex(index);
      int z = ChunkUtil.zOfChunkIndex(index);
      x -= this.minX;
      z -= this.minZ;
      if (x >= 0 && x < this.length && z >= 0 && z < this.length) {
         int arrayIndex = x * this.length + z;
         WorldChunk chunk = this.chunks[arrayIndex];
         if (chunk == null) {
            chunk = this.chunks[arrayIndex] = this.delegate.getChunkIfInMemory(index);
         }

         return chunk != null && chunk.is(ChunkFlag.TICKING) ? null : chunk;
      } else {
         return this.delegate.getChunkIfNonTicking(index);
      }
   }

   public WorldChunk getChunk(long index) {
      int x = ChunkUtil.xOfChunkIndex(index);
      int z = ChunkUtil.zOfChunkIndex(index);
      x -= this.minX;
      z -= this.minZ;
      if (x >= 0 && x < this.length && z >= 0 && z < this.length) {
         int arrayIndex = x * this.length + z;
         WorldChunk chunk = this.chunks[arrayIndex];
         return chunk != null ? chunk : (this.chunks[arrayIndex] = this.delegate.getChunk(index));
      } else {
         return this.delegate.getChunk(index);
      }
   }

   public WorldChunk getNonTickingChunk(long index) {
      int x = ChunkUtil.xOfChunkIndex(index);
      int z = ChunkUtil.zOfChunkIndex(index);
      x -= this.minX;
      z -= this.minZ;
      if (x >= 0 && x < this.length && z >= 0 && z < this.length) {
         int arrayIndex = x * this.length + z;
         WorldChunk chunk = this.chunks[arrayIndex];
         return chunk != null ? chunk : (this.chunks[arrayIndex] = this.delegate.getNonTickingChunk(index));
      } else {
         return this.delegate.getNonTickingChunk(index);
      }
   }
}
