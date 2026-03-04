package com.hypixel.hytale.server.worldgen.chunk.populator;

import com.hypixel.hytale.math.util.FastRandom;
import com.hypixel.hytale.math.util.HashUtil;
import com.hypixel.hytale.procedurallib.condition.ConstantIntCondition;
import com.hypixel.hytale.procedurallib.condition.IBlockFluidCondition;
import com.hypixel.hytale.server.core.universe.world.worldgen.GeneratedBlockChunk;
import com.hypixel.hytale.server.worldgen.biome.Biome;
import com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator;
import com.hypixel.hytale.server.worldgen.chunk.ChunkGeneratorExecution;
import com.hypixel.hytale.server.worldgen.chunk.HeightThresholdInterpolator;
import com.hypixel.hytale.server.worldgen.container.CoverContainer;
import com.hypixel.hytale.server.worldgen.container.LayerContainer;
import com.hypixel.hytale.server.worldgen.container.WaterContainer;
import com.hypixel.hytale.server.worldgen.util.BlockFluidEntry;
import com.hypixel.hytale.server.worldgen.util.NoiseBlockArray;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Random;
import javax.annotation.Nonnull;

public class BlockPopulator {
   public static void populate(int seed, @Nonnull ChunkGeneratorExecution execution) {
      Random random = new FastRandom(HashUtil.hash(seed, execution.getX(), execution.getZ(), 5647422603192711886L));

      for (int cx = 0; cx < 32; cx++) {
         for (int cz = 0; cz < 32; cz++) {
            generateBlockColumn(seed, execution, cx, cz, random);
         }
      }
   }

   private static void generateBlockColumn(int seed, @Nonnull ChunkGeneratorExecution execution, int cx, int cz, @Nonnull Random random) {
      HeightThresholdInterpolator interpolator = execution.getInterpolator();
      IntList surfaceBlockList = ChunkGenerator.getResource().coverArray;
      Biome biome = execution.zoneBiomeResult(cx, cz).getBiome();
      LayerContainer layerContainer = biome.getLayerContainer();
      int x = execution.globalX(cx);
      int z = execution.globalZ(cz);
      double heightmapNoise = interpolator.getHeightNoise(cx, cz);
      BlockFluidEntry filling = layerContainer.getFilling();
      int fillingEnvironment = layerContainer.getFillingEnvironment();
      int highest = 0;
      int min = interpolator.getLowestNonOne(cx, cz);
      int y = interpolator.getHighestNonZero(cx, cz);

      boolean empty;
      for (empty = true; y >= min; y--) {
         double threshold = interpolator.getHeightThreshold(seed, x, z, y);
         if (!(threshold > heightmapNoise) && threshold != 1.0) {
            empty = true;
         } else {
            if (y > highest) {
               highest = y;
            }

            execution.setBlock(cx, y, cz, (byte)1, filling, fillingEnvironment);
            if (empty) {
               surfaceBlockList.add(y);
               empty = false;
            }
         }
      }

      if (empty) {
         surfaceBlockList.add(y);
      }

      if (y > highest) {
         highest = y;
      }

      while (y >= 0) {
         execution.setBlock(cx, y, cz, (byte)1, filling);
         y--;
      }

      execution.getChunkGenerator().putHeight(seed, x, z, highest);
      BlockPopulator.LayerPopulator.generateLayers(seed, execution, cx, cz, x, z, biome, surfaceBlockList);
      generateCovers(seed, execution, cx, cz, x, z, random, biome, surfaceBlockList);
      surfaceBlockList.clear();
   }

   private static void generateCovers(
      int seed,
      @Nonnull ChunkGeneratorExecution execution,
      int cx,
      int cz,
      int x,
      int z,
      @Nonnull Random random,
      @Nonnull Biome biome,
      @Nonnull IntList surfaceBlockList
   ) {
      CoverContainer coverContainer = biome.getCoverContainer();
      int size = surfaceBlockList.size();
      if (size != 0) {
         for (WaterContainer.Entry waterContainer : biome.getWaterContainer().getEntries()) {
            for (CoverContainer.CoverContainerEntry coverContainerEntry : coverContainer.getEntries()) {
               if (coverContainerEntry.isOnWater() && isMatchingCoverColumn(seed, coverContainerEntry, random, x, z)) {
                  int y = waterContainer.getMax(seed, x, z) + 1;
                  if (isMatchingCoverHeight(seed, coverContainerEntry, random, x, y, z)
                     && isMatchingParentCover(execution, coverContainerEntry, cx, cz, y, waterContainer.getBlock(), waterContainer.getFluid())) {
                     CoverContainer.CoverContainerEntry.CoverContainerEntryPart coverEntry = coverContainerEntry.get(random);
                     execution.setBlock(cx, y + coverEntry.getOffset(), cz, (byte)3, coverEntry.getEntry());
                     execution.setFluid(cx, y + coverEntry.getOffset(), cz, (byte)3, coverEntry.getEntry().fluidId());
                  }
               }
            }
         }

         for (int i = 0; i < size; i++) {
            int y = surfaceBlockList.getInt(i) + 1;

            for (CoverContainer.CoverContainerEntry coverContainerEntryx : coverContainer.getEntries()) {
               if (!coverContainerEntryx.isOnWater()
                  && isMatchingParentCover(execution, coverContainerEntryx, cx, cz, y, 0, 0)
                  && isMatchingCoverColumn(seed, coverContainerEntryx, random, x, z)
                  && isMatchingCoverHeight(seed, coverContainerEntryx, random, x, y, z)) {
                  CoverContainer.CoverContainerEntry.CoverContainerEntryPart coverEntry = coverContainerEntryx.get(random);
                  execution.setBlock(cx, y + coverEntry.getOffset(), cz, (byte)3, coverEntry.getEntry());
                  execution.setFluid(cx, y + coverEntry.getOffset(), cz, (byte)3, coverEntry.getEntry().fluidId());
               }
            }
         }
      }
   }

   private static boolean isMatchingParentCover(
      @Nonnull ChunkGeneratorExecution execution,
      @Nonnull CoverContainer.CoverContainerEntry coverContainerEntry,
      int cx,
      int cz,
      int y,
      int defaultId,
      int defaultFluidId
   ) {
      if (y > 0 && y < 320) {
         IBlockFluidCondition parentCondition = coverContainerEntry.getParentCondition();
         if (parentCondition == ConstantIntCondition.DEFAULT_TRUE) {
            return true;
         } else if (parentCondition == ConstantIntCondition.DEFAULT_FALSE) {
            return false;
         } else {
            GeneratedBlockChunk chunk = execution.getChunk();
            int block = chunk.getBlock(cx, y - 1, cz);
            if (block == 0) {
               block = defaultId;
            }

            int fluid = execution.getFluid(cx, y - 1, cz);
            if (fluid == 0) {
               fluid = defaultFluidId;
            }

            return parentCondition.eval(block, fluid);
         }
      } else {
         return false;
      }
   }

   private static boolean isMatchingCoverColumn(int seed, @Nonnull CoverContainer.CoverContainerEntry coverContainerEntry, @Nonnull Random random, int x, int z) {
      return random.nextDouble() < coverContainerEntry.getCoverDensity() && coverContainerEntry.getMapCondition().eval(seed, x, z);
   }

   private static boolean isMatchingCoverHeight(int seed, @Nonnull CoverContainer.CoverContainerEntry coverContainerEntry, Random random, int x, int y, int z) {
      return coverContainerEntry.getHeightCondition().eval(seed, x, z, y, random);
   }

   private static class LayerPopulator {
      static void generateLayers(
         int seed, @Nonnull ChunkGeneratorExecution execution, int cx, int cz, int x, int z, @Nonnull Biome biome, @Nonnull IntList surfaceBlockList
      ) {
         generateStaticLayers(seed, execution, cx, cz, x, z, biome);
         generateDynamicLayers(seed, execution, cx, cz, x, z, biome, surfaceBlockList);
      }

      private static void generateDynamicLayers(
         int seed, @Nonnull ChunkGeneratorExecution execution, int cx, int cz, int x, int z, @Nonnull Biome biome, @Nonnull IntList surfaceBlockList
      ) {
         LayerContainer layers = biome.getLayerContainer();
         int i = 0;

         for (int size = surfaceBlockList.size(); i < size; i++) {
            int surfaceY = surfaceBlockList.getInt(i);
            int y = surfaceY;
            int maxY = surfaceY;

            label47:
            for (LayerContainer.DynamicLayer layer : layers.getDynamicLayers()) {
               LayerContainer.DynamicLayerEntry entry = layer.getActiveEntry(seed, x, z);
               if (entry != null) {
                  int environmentId = layer.getEnvironmentId();
                  y += layer.getOffset(seed, x, z);
                  maxY = Math.max(maxY, y);
                  NoiseBlockArray blockArray = entry.getBlockArray();

                  for (NoiseBlockArray.Entry blockArrayEntry : blockArray.getEntries()) {
                     int repetitions = blockArrayEntry.getRepetitions(seed, x, z);

                     for (int j = 0; j < repetitions; j++) {
                        if (y <= surfaceY && execution.getBlock(cx, y, cz) == 0) {
                           break label47;
                        }

                        execution.setBlock(cx, y, cz, (byte)2, blockArrayEntry.getBlockEntry(), environmentId);
                        execution.setFluid(cx, y, cz, (byte)2, blockArrayEntry.getBlockEntry().fluidId(), environmentId);
                        y--;
                     }
                  }
               }
            }

            if (maxY > surfaceY) {
               surfaceBlockList.set(i, maxY);
            }
         }
      }

      private static void generateStaticLayers(int seed, @Nonnull ChunkGeneratorExecution execution, int cx, int cz, int x, int z, @Nonnull Biome biome) {
         LayerContainer layers = biome.getLayerContainer();

         for (LayerContainer.StaticLayer layer : layers.getStaticLayers()) {
            LayerContainer.StaticLayerEntry entry = layer.getActiveEntry(seed, x, z);
            if (entry != null) {
               int environmentId = layer.getEnvironmentId();
               NoiseBlockArray.Entry[] blockEntries = entry.getBlockArray().getEntries();
               int min = Math.max(entry.getMinInt(seed, x, z), 0);
               int max = Math.min(entry.getMaxInt(seed, x, z), 320);
               int layerY = entry.getMaxInt(seed, x, z);
               BlockFluidEntry lastBlock = null;

               for (NoiseBlockArray.Entry blockEntry : blockEntries) {
                  int repetitions = blockEntry.getRepetitions(seed, x, z);
                  if (repetitions > 0) {
                     BlockFluidEntry block = blockEntry.getBlockEntry();
                     lastBlock = block;

                     for (int i = 0; i < repetitions; i++) {
                        int currentBlock = execution.getBlock(cx, --layerY, cz);
                        if (currentBlock != 0) {
                           execution.setBlock(cx, layerY, cz, (byte)2, block, environmentId);
                           execution.setFluid(cx, layerY, cz, (byte)2, block.fluidId(), environmentId);
                        }

                        if (layerY <= min) {
                           return;
                        }
                     }
                  }
               }

               if (blockEntries.length == 0 && environmentId != Integer.MIN_VALUE) {
                  for (int y = max - 1; y >= min; y--) {
                     execution.setEnvironment(cx, y, cz, environmentId);
                  }
               }

               if (lastBlock != null) {
                  while (layerY > min) {
                     int currentBlockx = execution.getBlock(cx, --layerY, cz);
                     if (currentBlockx != 0) {
                        execution.setBlock(cx, layerY, cz, (byte)2, lastBlock);
                        execution.setFluid(cx, layerY, cz, (byte)2, lastBlock.fluidId());
                     }
                  }
               }
            }
         }
      }
   }
}
