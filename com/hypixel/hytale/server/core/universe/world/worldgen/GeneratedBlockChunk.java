package com.hypixel.hytale.server.core.universe.world.worldgen;

import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.protocol.Opacity;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.environment.EnvironmentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.palette.IntBytePalette;
import com.hypixel.hytale.server.core.universe.world.chunk.palette.ShortBytePalette;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GeneratedBlockChunk {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   protected long index;
   protected int x;
   protected int z;
   protected final IntBytePalette tint;
   protected final EnvironmentChunk environments;
   protected final GeneratedChunkSection[] chunkSections;

   public GeneratedBlockChunk() {
      this(0L, 0, 0);
   }

   public GeneratedBlockChunk(long index, int x, int z) {
      this(index, x, z, new IntBytePalette(), new EnvironmentChunk(), new GeneratedChunkSection[10]);
   }

   public GeneratedBlockChunk(long index, int x, int z, IntBytePalette tint, EnvironmentChunk environments, GeneratedChunkSection[] chunkSections) {
      this.index = index;
      this.x = x;
      this.z = z;
      this.tint = tint;
      this.environments = environments;
      this.chunkSections = chunkSections;
   }

   public long getIndex() {
      return this.index;
   }

   public int getX() {
      return this.x;
   }

   public int getZ() {
      return this.z;
   }

   public void setCoordinates(long index, int x, int z) {
      this.index = index;
      this.x = x;
      this.z = z;
   }

   public int getHeight(int x, int z) {
      int y = 320;

      while (--y > 0) {
         GeneratedChunkSection section = this.getSection(y);
         if (section == null) {
            y = ChunkUtil.indexSection(y) * 32;
            if (y == 0) {
               break;
            }
         } else {
            int blockId = section.getBlock(x, y, z);
            BlockType type = BlockType.getAssetMap().getAsset(blockId);
            if (blockId != 0 && type != null && type.getOpacity() != Opacity.Transparent) {
               break;
            }
         }
      }

      return y;
   }

   @Nonnull
   public ShortBytePalette generateHeight() {
      ShortBytePalette height = new ShortBytePalette();

      for (int x = 0; x < 32; x++) {
         for (int z = 0; z < 32; z++) {
            height.set(x, z, (short)this.getHeight(x, z));
         }
      }

      return height;
   }

   @Nonnull
   public EnvironmentChunk getEnvironmentChunk() {
      return this.environments;
   }

   @Nullable
   public GeneratedChunkSection getSection(int y) {
      int index = ChunkUtil.indexSection(y);
      return index >= 0 && index < this.chunkSections.length ? this.chunkSections[index] : null;
   }

   public int getTint(int x, int z) {
      return this.tint.get(x, z);
   }

   public void setTint(int x, int z, int tint) {
      this.tint.set(x, z, tint);
   }

   public void setEnvironment(int x, int y, int z, int environment) {
      this.environments.set(x, y, z, environment);
   }

   public void setEnvironmentColumn(int x, int z, int environment) {
      this.environments.setColumn(x, z, environment);
   }

   public int getEnvironment(int x, int y, int z) {
      return this.environments.get(x, y, z);
   }

   public int getRotationIndex(int x, int y, int z) {
      if (y >= 0 && y < 320) {
         GeneratedChunkSection section = this.getSection(y);
         return section == null ? 0 : section.getRotationIndex(x, y, z);
      } else {
         return 0;
      }
   }

   public int getBlock(int x, int y, int z) {
      if (y >= 0 && y < 320) {
         GeneratedChunkSection section = this.getSection(y);
         return section == null ? 0 : section.getBlock(x, y, z);
      } else {
         return 0;
      }
   }

   public void setBlock(int x, int y, int z, int blockId, int rotation, int filler) {
      if (y >= 0 && y < 320) {
         GeneratedChunkSection section = this.getSection(y);
         int sectionIndex = ChunkUtil.indexSection(y);
         if (section == null) {
            if (blockId == 0) {
               return;
            }

            section = this.initialize(sectionIndex);
         }

         section.setBlock(x, y, z, blockId, rotation, filler);
      } else {
         ((HytaleLogger.Api)LOGGER.at(Level.INFO).withCause(new Exception()))
            .log("Failed to set block %d, %d, %d to %d because it is outside the world bounds", x, y, z, blockId);
      }
   }

   @Nonnull
   private GeneratedChunkSection initialize(int section) {
      return this.chunkSections[section] = new GeneratedChunkSection();
   }

   public void removeSection(int y) {
      int index = ChunkUtil.indexSection(y);
      if (index >= 0 && index < this.chunkSections.length) {
         this.chunkSections[index] = null;
      }
   }

   @Nonnull
   public BlockChunk toBlockChunk(Holder<ChunkStore>[] sectionHolders) {
      for (int y = 0; y < this.chunkSections.length; y++) {
         GeneratedChunkSection chunkSection = this.chunkSections[y];
         if (chunkSection != null) {
            sectionHolders[y].putComponent(BlockSection.getComponentType(), chunkSection.toChunkSection());
         }
      }

      ShortBytePalette height = this.generateHeight();
      this.environments.trim();
      return new BlockChunk(this.x, this.z, height, this.tint, this.environments);
   }
}
