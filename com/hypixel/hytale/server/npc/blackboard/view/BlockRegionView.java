package com.hypixel.hytale.server.npc.blackboard.view;

import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;

public abstract class BlockRegionView<ViewType extends IBlackboardView<ViewType>> implements IBlackboardView<ViewType> {
   public static final int BITS = 7;
   public static final int SIZE = 128;
   public static final int SIZE_MASK = 127;
   public static final int BITS2 = 14;

   public static int toRegionalBlackboardCoordinate(int pos) {
      return pos >> 7;
   }

   public static int toWorldCoordinate(int pos) {
      return pos << 7;
   }

   public static int chunkToRegionalBlackboardCoordinate(int pos) {
      return pos >> 2;
   }

   public static long indexView(int x, int z) {
      return ChunkUtil.indexChunk(x, z);
   }

   public static int indexSection(int y) {
      return y >> 7;
   }

   public static int xOfViewIndex(long index) {
      return ChunkUtil.xOfChunkIndex(index);
   }

   public static int zOfViewIndex(long index) {
      return ChunkUtil.zOfChunkIndex(index);
   }

   public static long indexViewFromChunkCoordinates(int x, int z) {
      return indexView(toRegionalBlackboardCoordinate(x), toRegionalBlackboardCoordinate(z));
   }

   public static long indexViewFromWorldPosition(@Nonnull Vector3d pos) {
      int blackboardX = toRegionalBlackboardCoordinate(MathUtil.floor(pos.getX()));
      int blackboardZ = toRegionalBlackboardCoordinate(MathUtil.floor(pos.getZ()));
      return indexView(blackboardX, blackboardZ);
   }

   public static int indexBlock(int x, int y, int z) {
      return (y & 127) << 14 | (z & 127) << 7 | x & 127;
   }

   public static int xFromIndex(int index) {
      return index & 127;
   }

   public static int yFromIndex(int index) {
      return index >> 14 & 127;
   }

   public static int zFromIndex(int index) {
      return index >> 7 & 127;
   }
}
