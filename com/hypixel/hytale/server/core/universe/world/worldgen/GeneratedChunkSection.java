package com.hypixel.hytale.server.core.universe.world.worldgen;

import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.palette.EmptySectionPalette;
import com.hypixel.hytale.server.core.universe.world.chunk.section.palette.ISectionPalette;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.IntArrays;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class GeneratedChunkSection {
   @Nonnull
   private final int[] data = new int[32768];
   @Nonnull
   private final int[] temp = new int[32768];
   private ISectionPalette fillers = EmptySectionPalette.INSTANCE;
   private ISectionPalette rotations = EmptySectionPalette.INSTANCE;

   public int getRotationIndex(int x, int y, int z) {
      return this.getRotationIndex(ChunkUtil.indexBlock(x, y, z));
   }

   private int getRotationIndex(int index) {
      return this.rotations.get(index);
   }

   public int getBlock(int x, int y, int z) {
      return this.getBlock(ChunkUtil.indexBlock(x, y, z));
   }

   public int getFiller(int x, int y, int z) {
      return this.fillers.get(ChunkUtil.indexBlock(x, y, z));
   }

   private int getBlock(int index) {
      return this.data[index];
   }

   public void setBlock(int x, int y, int z, int block, int rotation, int filler) {
      this.setBlock(ChunkUtil.indexBlock(x, y, z), block, rotation, filler);
   }

   public void setBlock(int index, int block, int rotation, int filler) {
      this.data[index] = block;
      ISectionPalette.SetResult result = this.fillers.set(index, filler);
      if (result == ISectionPalette.SetResult.REQUIRES_PROMOTE) {
         this.fillers = this.fillers.promote();
         this.fillers.set(index, filler);
      } else if (result == ISectionPalette.SetResult.ADDED_OR_REMOVED && this.fillers.shouldDemote()) {
         this.fillers = this.fillers.demote();
      }

      result = this.rotations.set(index, rotation);
      if (result == ISectionPalette.SetResult.REQUIRES_PROMOTE) {
         this.rotations = this.rotations.promote();
         this.rotations.set(index, rotation);
      } else if (result == ISectionPalette.SetResult.ADDED_OR_REMOVED && this.rotations.shouldDemote()) {
         this.rotations = this.rotations.demote();
      }
   }

   public int[] getData() {
      return this.data;
   }

   public void reset() {
      Arrays.fill(this.data, 0);
   }

   public boolean isSolidAir() {
      for (int i = 0; i < this.data.length; i++) {
         if (this.data[i] != 0) {
            return false;
         }
      }

      return true;
   }

   @Nonnull
   public BlockSection toChunkSection() {
      System.arraycopy(this.data, 0, this.temp, 0, 32768);
      IntArrays.unstableSort(this.temp);
      int count = 1;

      for (int i = 1; i < 32768; i++) {
         if (this.temp[i] != this.temp[i - 1]) {
            this.temp[count++] = this.temp[i];
         }
      }

      return new BlockSection(ISectionPalette.from(this.data, this.temp, count), this.fillers, this.rotations);
   }

   public void serialize(@Nonnull ByteBuf buf) {
      for (int i = 0; i < 32768; i++) {
         buf.writeInt(this.data[i]);
      }
   }

   public void deserialize(@Nonnull ByteBuf buf, int version) {
      int[] blocks = new int[32768];

      for (int i = 0; i < blocks.length; i++) {
         blocks[i] = buf.readInt();
      }
   }
}
