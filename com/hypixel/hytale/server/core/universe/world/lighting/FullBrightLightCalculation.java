package com.hypixel.hytale.server.core.universe.world.lighting;

import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.ChunkLightDataBuilder;
import javax.annotation.Nonnull;

public class FullBrightLightCalculation implements LightCalculation {
   private final ChunkLightingManager chunkLightingManager;
   private LightCalculation delegate;

   public FullBrightLightCalculation(ChunkLightingManager chunkLightingManager, LightCalculation delegate) {
      this.chunkLightingManager = chunkLightingManager;
      this.delegate = delegate;
   }

   @Override
   public void init(@Nonnull WorldChunk worldChunk) {
      this.delegate.init(worldChunk);
   }

   @Nonnull
   @Override
   public CalculationResult calculateLight(@Nonnull Vector3i chunkPosition) {
      CalculationResult result = this.delegate.calculateLight(chunkPosition);
      if (result == CalculationResult.DONE) {
         WorldChunk worldChunk = this.chunkLightingManager.getWorld().getChunkIfInMemory(ChunkUtil.indexChunk(chunkPosition.x, chunkPosition.z));
         if (worldChunk == null) {
            return CalculationResult.NOT_LOADED;
         }

         this.setFullBright(worldChunk, chunkPosition.y);
      }

      return result;
   }

   @Override
   public boolean invalidateLightAtBlock(
      @Nonnull WorldChunk worldChunk, int blockX, int blockY, int blockZ, @Nonnull BlockType blockType, int oldHeight, int newHeight
   ) {
      boolean handled = this.delegate.invalidateLightAtBlock(worldChunk, blockX, blockY, blockZ, blockType, oldHeight, newHeight);
      if (handled) {
         this.setFullBright(worldChunk, blockY >> 5);
      }

      return handled;
   }

   @Override
   public boolean invalidateLightInChunkSections(@Nonnull WorldChunk worldChunk, int sectionIndexFrom, int sectionIndexTo) {
      boolean handled = this.delegate.invalidateLightInChunkSections(worldChunk, sectionIndexFrom, sectionIndexTo);
      if (handled) {
         for (int y = sectionIndexTo - 1; y >= sectionIndexFrom; y--) {
            this.setFullBright(worldChunk, y);
         }
      }

      return handled;
   }

   public void setFullBright(@Nonnull WorldChunk worldChunk, int chunkY) {
      BlockSection section = worldChunk.getBlockChunk().getSectionAtIndex(chunkY);
      ChunkLightDataBuilder light = new ChunkLightDataBuilder(section.getGlobalChangeCounter());

      for (int i = 0; i < 32768; i++) {
         light.setSkyLight(i, (byte)15);
      }

      section.setGlobalLight(light);
      if (BlockChunk.SEND_LOCAL_LIGHTING_DATA || BlockChunk.SEND_GLOBAL_LIGHTING_DATA) {
         worldChunk.getBlockChunk().invalidateChunkSection(chunkY);
      }
   }
}
