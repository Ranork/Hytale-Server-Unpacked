package com.hypixel.hytale.server.worldgen.util.bounds;

import com.hypixel.hytale.math.util.ChunkUtil;
import java.util.Random;
import javax.annotation.Nonnull;

public interface IWorldBounds extends IChunkBounds {
   int getLowBoundY();

   int getHighBoundY();

   @Override
   default boolean intersectsChunk(long chunkIndex) {
      return this.intersectsChunk(ChunkUtil.xOfChunkIndex(chunkIndex), ChunkUtil.zOfChunkIndex(chunkIndex));
   }

   default int randomY(@Nonnull Random random) {
      return IChunkBounds.getRandomOffset(this.getLowBoundY(), this.getHighBoundY(), random);
   }

   default double fractionY(double d) {
      return (this.getHighBoundY() - this.getLowBoundY()) * d + this.getLowBoundY();
   }

   @Override
   default boolean isValid() {
      return IChunkBounds.super.isValid() && this.getHighBoundY() > this.getLowBoundY();
   }
}
