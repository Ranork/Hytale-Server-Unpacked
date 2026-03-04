package com.hypixel.hytale.server.worldgen.cave.shape;

import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.worldgen.GeneratedBlockChunk;
import com.hypixel.hytale.server.worldgen.cave.Cave;
import com.hypixel.hytale.server.worldgen.cave.CaveNodeType;
import com.hypixel.hytale.server.worldgen.cave.CaveType;
import com.hypixel.hytale.server.worldgen.cave.element.CaveNode;
import com.hypixel.hytale.server.worldgen.chunk.ChunkGeneratorExecution;
import com.hypixel.hytale.server.worldgen.util.BlockFluidEntry;
import com.hypixel.hytale.server.worldgen.util.bounds.IWorldBounds;
import java.util.Random;
import javax.annotation.Nonnull;

public abstract class AbstractCaveNodeShape implements CaveNodeShape {
   @Override
   public void populateChunk(int seed, @Nonnull ChunkGeneratorExecution execution, @Nonnull Cave cave, @Nonnull CaveNode node, @Nonnull Random random) {
      GeneratedBlockChunk chunk = execution.getChunk();
      BlockTypeAssetMap<String, BlockType> blockTypeMap = BlockType.getAssetMap();
      CaveType caveType = cave.getCaveType();
      CaveNodeType caveNodeType = node.getCaveNodeType();
      IWorldBounds shapeBounds = this.getBounds();
      boolean surfaceLimited = cave.getCaveType().isSurfaceLimited();
      int environment = node.getCaveNodeType().hasEnvironment() ? node.getCaveNodeType().getEnvironment() : caveType.getEnvironment();
      int chunkLowX = ChunkUtil.minBlock(execution.getX());
      int chunkLowZ = ChunkUtil.minBlock(execution.getZ());
      int chunkHighX = ChunkUtil.maxBlock(execution.getX());
      int chunkHighZ = ChunkUtil.maxBlock(execution.getZ());
      int minX = Math.max(chunkLowX, shapeBounds.getLowBoundX());
      int minY = shapeBounds.getLowBoundY();
      int minZ = Math.max(chunkLowZ, shapeBounds.getLowBoundZ());
      int maxX = Math.min(chunkHighX, shapeBounds.getHighBoundX());
      int maxY = shapeBounds.getHighBoundY();
      int maxZ = Math.min(chunkHighZ, shapeBounds.getHighBoundZ());

      for (int x = minX; x <= maxX; x++) {
         int cx = x - chunkLowX;

         for (int z = minZ; z <= maxZ; z++) {
            int cz = z - chunkLowZ;
            int height = maxY;
            boolean heightLimited = false;
            if (surfaceLimited) {
               int chunkHeight = chunk.getHeight(cx, cz);
               if (maxY >= chunkHeight) {
                  height = chunkHeight;
                  heightLimited = true;
               }
            }

            int lowest = Integer.MAX_VALUE;
            int lowestPossible = Integer.MAX_VALUE;
            int highest = Integer.MIN_VALUE;
            int highestPossible = Integer.MIN_VALUE;

            for (int y = minY; y <= height; y++) {
               if (this.shouldReplace(seed, x, z, y)) {
                  if (y < lowestPossible) {
                     lowestPossible = y;
                  }

                  if (y > highestPossible) {
                     highestPossible = y;
                  }

                  int current = execution.getBlock(cx, y, cz);
                  int currentFluid = execution.getFluid(cx, y, cz);
                  boolean isCandidateBlock = !surfaceLimited || current != 0;
                  if (isCandidateBlock) {
                     BlockFluidEntry blockEntry = CaveNodeShapeUtils.getFillingBlock(caveType, caveNodeType, y, random);
                     if (caveType.getBlockMask().eval(current, currentFluid, blockEntry.blockId(), blockEntry.fluidId())) {
                        if (execution.setBlock(cx, y, cz, (byte)6, blockEntry, environment)) {
                           if (y < lowest) {
                              lowest = y;
                           }

                           if (y > highest) {
                              highest = y;
                           }
                        }

                        if (execution.setFluid(cx, y, cz, (byte)6, blockEntry.fluidId(), environment)) {
                           if (y < lowest) {
                              lowest = y;
                           }

                           if (y > highest) {
                              highest = y;
                           }
                        }
                     }
                  }
               }
            }

            CaveNodeType.CaveNodeCoverEntry[] covers = caveNodeType.getCovers();

            for (CaveNodeType.CaveNodeCoverEntry cover : covers) {
               CaveNodeType.CaveNodeCoverEntry.Entry entry = cover.get(random);
               int yx = CaveNodeShapeUtils.getCoverHeight(lowest, lowestPossible, highest, highestPossible, heightLimited, cover, entry);
               if (yx >= 0
                  && cover.getDensityCondition().eval(seed + node.getSeedOffset(), x, z)
                  && cover.getHeightCondition().eval(seed, x, z, yx, random)
                  && cover.getMapCondition().eval(seed, x, z)
                  && CaveNodeShapeUtils.isCoverMatchingParent(cx, cz, yx, execution, cover)) {
                  execution.setBlock(cx, yx, cz, (byte)5, entry.getEntry(), environment);
                  execution.setFluid(cx, yx, cz, (byte)5, entry.getEntry().fluidId(), environment);
               }
            }

            if (CaveNodeShapeUtils.invalidateCover(cx, lowest - 1, cz, CaveNodeType.CaveNodeCoverType.CEILING, execution, blockTypeMap)) {
               BlockFluidEntry blockEntry = CaveNodeShapeUtils.getFillingBlock(caveType, caveNodeType, lowest - 1, random);
               execution.overrideBlock(cx, lowest - 1, cz, (byte)6, blockEntry);
               execution.overrideFluid(cx, lowest - 1, cz, (byte)6, blockEntry.fluidId());
            }

            if (CaveNodeShapeUtils.invalidateCover(cx, highest + 1, cz, CaveNodeType.CaveNodeCoverType.FLOOR, execution, blockTypeMap)) {
               BlockFluidEntry blockEntry = CaveNodeShapeUtils.getFillingBlock(caveType, caveNodeType, highest + 1, random);
               execution.overrideBlock(cx, highest + 1, cz, (byte)6, blockEntry);
               execution.overrideFluid(cx, highest + 1, cz, (byte)6, blockEntry.fluidId());
            }
         }
      }
   }
}
