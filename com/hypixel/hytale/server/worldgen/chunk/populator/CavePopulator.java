package com.hypixel.hytale.server.worldgen.chunk.populator;

import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.HashUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.procedurallib.condition.DefaultCoordinateCondition;
import com.hypixel.hytale.procedurallib.condition.ICoordinateCondition;
import com.hypixel.hytale.procedurallib.logic.point.IPointGenerator;
import com.hypixel.hytale.server.core.prefab.PrefabRotation;
import com.hypixel.hytale.server.worldgen.cave.Cave;
import com.hypixel.hytale.server.worldgen.cave.CaveBlockPriorityModifier;
import com.hypixel.hytale.server.worldgen.cave.CaveType;
import com.hypixel.hytale.server.worldgen.cave.element.CaveNode;
import com.hypixel.hytale.server.worldgen.cave.element.CavePrefab;
import com.hypixel.hytale.server.worldgen.chunk.BlockPriorityModifier;
import com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator;
import com.hypixel.hytale.server.worldgen.chunk.ChunkGeneratorExecution;
import com.hypixel.hytale.server.worldgen.chunk.ZoneBiomeResult;
import com.hypixel.hytale.server.worldgen.loader.WorldGenPrefabSupplier;
import com.hypixel.hytale.server.worldgen.prefab.PrefabPasteUtil;
import com.hypixel.hytale.server.worldgen.util.condition.BlockMaskCondition;
import com.hypixel.hytale.server.worldgen.zone.Zone;
import java.util.Random;
import javax.annotation.Nonnull;

public class CavePopulator {
   public static void populate(int seed, @Nonnull ChunkGeneratorExecution execution) {
      for (Zone zone : execution.getChunkGenerator().getZonePatternProvider().getZones()) {
         if (zone.caveGenerator() != null) {
            for (CaveType caveType : zone.caveGenerator().getCaveTypes()) {
               IPointGenerator cavePointGenerator = caveType.getEntryPointGenerator();
               cavePointGenerator.collect(
                  seed,
                  ChunkUtil.minBlock(execution.getX()) - caveType.getMaximumSize(),
                  ChunkUtil.minBlock(execution.getZ()) - caveType.getMaximumSize(),
                  ChunkUtil.maxBlock(execution.getX()) + caveType.getMaximumSize(),
                  ChunkUtil.maxBlock(execution.getZ()) + caveType.getMaximumSize(),
                  (x, z) -> run(seed, x, z, execution, zone, caveType)
               );
            }
         }
      }
   }

   private static void run(int seed, double dx, double dz, @Nonnull ChunkGeneratorExecution execution, Zone zone, @Nonnull CaveType caveType) {
      ChunkGenerator chunkGenerator = execution.getChunkGenerator();
      int x = MathUtil.floor(dx);
      int z = MathUtil.floor(dz);
      ZoneBiomeResult result = chunkGenerator.getZoneBiomeResultAt(seed, x, z);
      if (result.getZoneResult().getZone() == zone && caveType.isEntryThreshold(seed, x, z) && isMatchingHeightThreshold(seed, x, z, chunkGenerator, caveType)) {
         populate(seed, execution, execution.getChunkGenerator().getCave(caveType, seed, x, z));
      }
   }

   private static void populate(int seed, @Nonnull ChunkGeneratorExecution execution, @Nonnull Cave cave) {
      long chunkIndex = execution.getIndex();
      if (cave.contains(chunkIndex)) {
         int chunkX = execution.getX();
         int chunkZ = execution.getZ();
         Random random = new Random();
         int environment = cave.getCaveType().getEnvironment();
         execution.setPriorityModifier(CaveBlockPriorityModifier.INSTANCE);

         for (CaveNode node : cave.getCaveNodes(chunkIndex)) {
            populateCaveNode(seed, execution, cave, node, random);
         }

         execution.setPriorityModifier(BlockPriorityModifier.NONE);

         for (CaveNode node : cave.getCaveNodes(chunkIndex)) {
            for (CavePrefab prefab : node.getCavePrefabs()) {
               if (prefab.getBounds().intersectsChunk(chunkX, chunkZ)) {
                  populatePrefab(seed, environment, execution, cave, node, prefab);
               }
            }
         }
      }
   }

   private static void populateCaveNode(
      int seed, @Nonnull ChunkGeneratorExecution execution, @Nonnull Cave cave, @Nonnull CaveNode caveNode, @Nonnull Random random
   ) {
      random.setSeed(seed + caveNode.getSeedOffset());
      caveNode.getShape().populateChunk(seed, execution, cave, caveNode, random);
      if (execution.getChunkGenerator().getBenchmark().isEnabled()) {
         int minX = caveNode.getBounds().getLowBoundX();
         int minZ = caveNode.getBounds().getLowBoundZ();
         if (ChunkUtil.isInsideChunk(execution.getX(), execution.getZ(), minX, minZ)) {
            execution.getChunkGenerator()
               .getBenchmark()
               .registerCaveNode("Cave\t" + cave.getCaveType().getName() + "\t" + caveNode.getCaveNodeType().getName());
         }
      }
   }

   private static void populatePrefab(
      int seed, int environment, @Nonnull ChunkGeneratorExecution execution, @Nonnull Cave cave, @Nonnull CaveNode node, @Nonnull CavePrefab prefab
   ) {
      generatePrefabAt(
         seed,
         prefab.getX(),
         prefab.getZ(),
         prefab.getY(),
         environment,
         execution,
         cave,
         node,
         prefab.getConfiguration(),
         prefab.getPrefab(),
         prefab.getRotation()
      );
   }

   private static void generatePrefabAt(
      int seed,
      int x,
      int z,
      int y,
      int environment,
      @Nonnull ChunkGeneratorExecution execution,
      @Nonnull Cave cave,
      @Nonnull CaveNode node,
      BlockMaskCondition configuration,
      @Nonnull WorldGenPrefabSupplier supplier,
      PrefabRotation rotation
   ) {
      int cx = x - ChunkUtil.minBlock(execution.getX());
      int cz = z - ChunkUtil.minBlock(execution.getZ());
      long externalSeed = HashUtil.rehash(x, z) * -99562191L;
      boolean submerge = cave.getCaveType().isSubmerge();
      PrefabPasteUtil.PrefabPasteBuffer buffer = ChunkGenerator.getResource().prefabBuffer;
      buffer.setSeed(seed, externalSeed);
      buffer.blockMask = configuration;
      buffer.execution = execution;
      buffer.environmentId = environment;
      buffer.priority = (byte)(submerge ? 39 : 7);
      if (execution.getChunkGenerator().getBenchmark().isEnabled() && ChunkUtil.isInsideChunkRelative(cx, cz)) {
         execution.getChunkGenerator()
            .getBenchmark()
            .registerPrefab("CavePrefab: " + cave.getCaveType().getName() + "\t" + node.getCaveNodeType().getName() + "\t" + supplier.getName());
      }

      PrefabPasteUtil.generate(buffer, rotation, supplier, x, y, z, cx, cz);
   }

   private static boolean isMatchingHeightThreshold(int seed, int x, int z, @Nonnull ChunkGenerator chunkGenerator, @Nonnull CaveType caveType) {
      ICoordinateCondition heightCondition = caveType.getHeightCondition();
      if (heightCondition == DefaultCoordinateCondition.DEFAULT_TRUE) {
         return true;
      } else if (heightCondition == DefaultCoordinateCondition.DEFAULT_FALSE) {
         return false;
      } else {
         int height = chunkGenerator.getHeight(seed, x, z);
         return heightCondition.eval(seed + -173220171, x, height, z);
      }
   }
}
