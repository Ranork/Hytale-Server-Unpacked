package com.hypixel.hytale.server.core.universe.world.lighting;

import com.hypixel.hytale.common.util.CompletableFutureUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.ChunkLightDataBuilder;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
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

         setFullBright(this.chunkLightingManager.getWorld().getChunkStore(), chunkPosition.x, chunkPosition.y, chunkPosition.z);
      }

      return result;
   }

   @Override
   public boolean invalidateLightAtBlock(
      @Nonnull ChunkStore chunkStore, int blockX, int blockY, int blockZ, @Nonnull BlockType blockType, int oldHeight, int newHeight
   ) {
      boolean handled = this.delegate.invalidateLightAtBlock(chunkStore, blockX, blockY, blockZ, blockType, oldHeight, newHeight);
      if (handled) {
         setFullBright(chunkStore, ChunkUtil.chunkCoordinate(blockX), ChunkUtil.chunkCoordinate(blockY), ChunkUtil.chunkCoordinate(blockZ));
      }

      return handled;
   }

   @Override
   public boolean invalidateLightInChunkSections(@Nonnull ChunkStore chunkStore, int chunkX, int chunkZ, int sectionIndexFrom, int sectionIndexTo) {
      boolean handled = this.delegate.invalidateLightInChunkSections(chunkStore, chunkX, chunkZ, sectionIndexFrom, sectionIndexTo);
      if (handled) {
         for (int y = sectionIndexTo - 1; y >= sectionIndexFrom; y--) {
            setFullBright(chunkStore, chunkX, y, chunkZ);
         }
      }

      return handled;
   }

   public static void setFullBright(@Nonnull ChunkStore chunkStore, int chunkX, int chunkY, int chunkZ) {
      CompletableFutureUtil._catch(
         chunkStore.getChunkSectionReferenceAsync(chunkX, chunkY, chunkZ)
            .thenApplyAsync(
               ref -> ref != null && ref.isValid() ? ref.getStore().getComponent((Ref<ChunkStore>)ref, BlockSection.getComponentType()) : null,
               chunkStore.getWorld()
            )
            .thenAcceptAsync(section -> {
               if (section != null) {
                  ChunkLightDataBuilder light = new ChunkLightDataBuilder(section.getGlobalChangeCounter());

                  for (int i = 0; i < 32768; i++) {
                     light.setSkyLight(i, (byte)15);
                  }

                  section.setGlobalLight(light);
                  if (BlockChunk.SEND_LOCAL_LIGHTING_DATA || BlockChunk.SEND_GLOBAL_LIGHTING_DATA) {
                     section.invalidate();
                  }
               }
            })
      );
   }
}
