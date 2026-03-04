package com.hypixel.hytale.server.worldgen.chunk.populator;

import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.fluid.FluidTicker;
import com.hypixel.hytale.server.worldgen.biome.Biome;
import com.hypixel.hytale.server.worldgen.chunk.ChunkGeneratorExecution;
import com.hypixel.hytale.server.worldgen.container.WaterContainer;
import javax.annotation.Nonnull;

public class WaterPopulator {
   public static void populate(int seed, @Nonnull ChunkGeneratorExecution execution) {
      for (int cx = 0; cx < 32; cx++) {
         for (int cz = 0; cz < 32; cz++) {
            submergeColumn(seed, cx, cz, execution);
         }
      }
   }

   private static void submergeColumn(int seed, int cx, int cz, @Nonnull ChunkGeneratorExecution execution) {
      Biome biome = execution.zoneBiomeResult(cx, cz).getBiome();
      int x = execution.globalX(cx);
      int z = execution.globalZ(cz);
      WaterContainer waterContainer = biome.getWaterContainer();

      for (WaterContainer.Entry waterEntry : waterContainer.getEntries()) {
         if (waterEntry.shouldPopulate(seed, x, z)) {
            int waterMin = waterEntry.getMin(seed, x, z);
            int waterMax = waterEntry.getMax(seed, x, z);
            if (waterMin <= waterMax) {
               int blockId = waterEntry.getBlock();
               int fluidId = waterEntry.getFluid();

               for (int y = waterMin; y <= waterMax; y++) {
                  submergeBlock(cx, y, cz, blockId, fluidId, execution);
               }
            }
         }
      }
   }

   private static void submergeBlock(int cx, int y, int cz, int blockId, int fluidId, @Nonnull ChunkGeneratorExecution execution) {
      byte rawPriority = execution.getPriorityChunk().getRaw(cx, y, cz);
      byte priority = (byte)(rawPriority & 31);
      if (priority < 4 || (rawPriority & 32) != 0) {
         int currentBlockId;
         if (blockId != 0 && priority < 4) {
            execution.setBlock(cx, y, cz, (byte)-1, blockId);
            currentBlockId = blockId;
         } else {
            currentBlockId = execution.getBlock(cx, y, cz);
         }

         BlockType blockType = BlockType.getAssetMap().getAsset(currentBlockId);
         if (blockType != null && !FluidTicker.isSolid(blockType)) {
            execution.setFluid(cx, y, cz, (byte)-1, fluidId);
         }
      }
   }
}
