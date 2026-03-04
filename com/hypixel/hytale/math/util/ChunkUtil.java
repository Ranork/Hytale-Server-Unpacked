package com.hypixel.hytale.math.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import javax.annotation.Nonnull;

public class ChunkUtil {
   public static final int BITS = 5;
   public static final int SIZE = 32;
   public static final int SIZE_2 = 1024;
   public static final int SIZE_MINUS_1 = 31;
   public static final int SIZE_MASK = 31;
   public static final int SIZE_COLUMNS = 1024;
   public static final int SIZE_COLUMNS_MASK = 1023;
   public static final int SIZE_BLOCKS = 32768;
   public static final int SIZE_BLOCKS_MASK = 32767;
   public static final int BITS2 = 10;
   public static final int NON_CHUNK_MASK = -32;
   public static final int HEIGHT_SECTIONS = 10;
   public static final int HEIGHT = 320;
   public static final int HEIGHT_MINUS_1 = 319;
   public static final int HEIGHT_MASK = (Integer.highestOneBit(320) << 1) - 1;
   public static final int SIZE_BLOCKS_COLUMN = 327680;
   public static final long NOT_FOUND = indexChunk(Integer.MIN_VALUE, Integer.MIN_VALUE);
   public static final int MIN_Y = 0;
   public static final int MIN_ENTITY_Y = -32;
   public static final int MIN_SECTION = 0;

   private ChunkUtil() {
   }

   public static byte[] shortToByteArray(@Nonnull short[] data) {
      ByteBuffer byteBuffer = ByteBuffer.allocate(data.length * 2).order(ByteOrder.LITTLE_ENDIAN);
      byteBuffer.asShortBuffer().put(data);
      return byteBuffer.array();
   }

   public static byte[] intToByteArray(@Nonnull int[] data) {
      ByteBuffer byteBuffer = ByteBuffer.allocate(data.length * 4).order(ByteOrder.LITTLE_ENDIAN);
      byteBuffer.asIntBuffer().put(data);
      return byteBuffer.array();
   }

   public static int indexColumn(int x, int z) {
      return (z & 31) << 5 | x & 31;
   }

   public static int xFromColumn(int index) {
      return index & 31;
   }

   public static int zFromColumn(int index) {
      return index >> 5 & 31;
   }

   public static int indexSection(int y) {
      return y >> 5;
   }

   public static int indexBlockFromColumn(int column, int y) {
      return (y & 31) << 10 | column & 1023;
   }

   public static int indexBlock(int x, int y, int z) {
      return (y & 31) << 10 | (z & 31) << 5 | x & 31;
   }

   public static int xFromIndex(int index) {
      return index & 31;
   }

   public static int yFromIndex(int index) {
      return index >> 10 & 31;
   }

   public static int zFromIndex(int index) {
      return index >> 5 & 31;
   }

   public static int indexBlockInColumn(int x, int y, int z) {
      return (y & HEIGHT_MASK) << 10 | (z & 31) << 5 | x & 31;
   }

   public static int indexBlockInColumnFromColumn(int column, int y) {
      return (y & HEIGHT_MASK) << 10 | column & 1023;
   }

   public static int xFromBlockInColumn(int index) {
      return index & 31;
   }

   public static int yFromBlockInColumn(int index) {
      return index >> 10 & HEIGHT_MASK;
   }

   public static int zFromBlockInColumn(int index) {
      return index >> 5 & 31;
   }

   public static int localCoordinate(long v) {
      return (int)(v & 31L);
   }

   public static int chunkCoordinate(double block) {
      return MathUtil.floor(block) >> 5;
   }

   public static int chunkCoordinate(int block) {
      return block >> 5;
   }

   public static int chunkCoordinate(long block) {
      return (int)(block >> 5);
   }

   public static int minBlock(int index) {
      return index << 5;
   }

   public static int maxBlock(int index) {
      return (index << 5) + 31;
   }

   public static boolean isWithinLocalChunk(int x, int z) {
      return x >= 0 && z >= 0 && x < 32 && z < 32;
   }

   public static boolean isBorderBlock(int x, int z) {
      return x == 0 || z == 0 || x == 31 || z == 31;
   }

   public static boolean isBorderBlockGlobal(int x, int z) {
      x &= 31;
      z &= 31;
      return isBorderBlock(x, z);
   }

   public static boolean isInsideChunk(int chunkX, int chunkZ, int x, int z) {
      return chunkCoordinate(x) == chunkX && chunkCoordinate(z) == chunkZ;
   }

   public static boolean isSameChunk(int x0, int z0, int x1, int z1) {
      return chunkCoordinate(x0) == chunkCoordinate(x1) && chunkCoordinate(z0) == chunkCoordinate(z1);
   }

   public static boolean isSameChunkSection(int x0, int y0, int z0, int x1, int y1, int z1) {
      return chunkCoordinate(x0) == chunkCoordinate(x1) && chunkCoordinate(y0) == chunkCoordinate(y1) && chunkCoordinate(z0) == chunkCoordinate(z1);
   }

   public static boolean isInsideChunkRelative(int x, int z) {
      return (x & 31) == x && (z & 31) == z;
   }

   public static int xOfChunkIndex(long index) {
      return (int)(index >> 32);
   }

   public static int zOfChunkIndex(long index) {
      return (int)index;
   }

   public static long indexChunk(int x, int z) {
      return (long)x << 32 | z & 4294967295L;
   }

   public static long indexChunkFromBlock(int blockX, int blockZ) {
      return indexChunk(chunkCoordinate(blockX), chunkCoordinate(blockZ));
   }

   public static long indexChunkFromBlock(double blockX, double blockZ) {
      return indexChunkFromBlock(MathUtil.floor(blockX), MathUtil.floor(blockZ));
   }

   public static int worldCoordFromLocalCoord(int chunkCoord, int localCoord) {
      return chunkCoord << 5 | localCoord;
   }
}
