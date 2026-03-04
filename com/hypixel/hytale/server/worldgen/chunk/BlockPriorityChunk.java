package com.hypixel.hytale.server.worldgen.chunk;

import com.hypixel.hytale.math.util.ChunkUtil;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class BlockPriorityChunk {
   public static final byte NO_CHANGE = -1;
   public static final byte NONE = 0;
   public static final byte FILLING = 1;
   public static final byte LAYER = 2;
   public static final byte COVER = 3;
   public static final byte WATER = 4;
   public static final byte CAVE_COVER = 5;
   public static final byte CAVE = 6;
   public static final byte CAVE_PREFAB = 7;
   public static final byte PREFAB_CAVE = 8;
   public static final byte PREFAB = 9;
   public static final byte EXCLUSIVE_MAX_PRIORITY = 32;
   public static final byte MASK = 31;
   public static final byte FLAG_MASK = -32;
   public static final byte FLAG_SUBMERGE = 32;
   @Nonnull
   private final byte[] blocks = new byte[327680];

   @Nonnull
   public BlockPriorityChunk reset() {
      Arrays.fill(this.blocks, (byte)0);
      return this;
   }

   public byte get(int x, int y, int z) {
      return (byte)(this.blocks[index(x, y, z)] & 31);
   }

   public byte getRaw(int x, int y, int z) {
      return this.blocks[index(x, y, z)];
   }

   public void set(int x, int y, int z, byte type) {
      this.blocks[index(x, y, z)] = type;
   }

   private static int index(int x, int y, int z) {
      return ChunkUtil.indexBlockInColumn(x, y, z);
   }
}
