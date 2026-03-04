package com.hypixel.hytale.server.worldgen.util.bounds;

import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.prefab.PrefabRotation;
import java.util.Random;
import javax.annotation.Nonnull;

public interface IChunkBounds {
   int getLowBoundX();

   int getLowBoundZ();

   int getHighBoundX();

   int getHighBoundZ();

   default int getLowBoundX(@Nonnull PrefabRotation rotation) {
      return Math.min(rotation.getX(this.getLowBoundX(), this.getLowBoundZ()), rotation.getX(this.getHighBoundX(), this.getHighBoundZ()));
   }

   default int getLowBoundZ(@Nonnull PrefabRotation rotation) {
      return Math.min(rotation.getZ(this.getLowBoundX(), this.getLowBoundZ()), rotation.getZ(this.getHighBoundX(), this.getHighBoundZ()));
   }

   default int getHighBoundX(@Nonnull PrefabRotation rotation) {
      return Math.max(rotation.getX(this.getLowBoundX(), this.getLowBoundZ()), rotation.getX(this.getHighBoundX(), this.getHighBoundZ()));
   }

   default int getHighBoundZ(@Nonnull PrefabRotation rotation) {
      return Math.max(rotation.getZ(this.getLowBoundX(), this.getLowBoundZ()), rotation.getZ(this.getHighBoundX(), this.getHighBoundZ()));
   }

   default boolean intersectsChunk(long chunkIndex) {
      return this.intersectsChunk(ChunkUtil.xOfChunkIndex(chunkIndex), ChunkUtil.zOfChunkIndex(chunkIndex));
   }

   default boolean intersectsChunk(int chunkX, int chunkZ) {
      return ChunkUtil.maxBlock(chunkX) >= this.getLowBoundX()
         && ChunkUtil.minBlock(chunkX) <= this.getHighBoundX()
         && ChunkUtil.maxBlock(chunkZ) >= this.getLowBoundZ()
         && ChunkUtil.minBlock(chunkZ) <= this.getHighBoundZ();
   }

   default int randomX(@Nonnull Random random) {
      return random.nextInt(this.getHighBoundX() - this.getLowBoundX()) + this.getLowBoundX();
   }

   default int randomZ(@Nonnull Random random) {
      return random.nextInt(this.getHighBoundZ() - this.getLowBoundZ()) + this.getLowBoundZ();
   }

   default double fractionX(double d) {
      return (this.getHighBoundX() - this.getLowBoundX()) * d + this.getLowBoundX();
   }

   default double fractionZ(double d) {
      return (this.getHighBoundZ() - this.getLowBoundZ()) * d + this.getLowBoundZ();
   }

   default int getLowChunkX() {
      return ChunkUtil.chunkCoordinate(this.getLowBoundX());
   }

   default int getLowChunkZ() {
      return ChunkUtil.chunkCoordinate(this.getLowBoundZ());
   }

   default int getHighChunkX() {
      return ChunkUtil.chunkCoordinate(this.getHighBoundX());
   }

   default int getHighChunkZ() {
      return ChunkUtil.chunkCoordinate(this.getHighBoundZ());
   }
}
