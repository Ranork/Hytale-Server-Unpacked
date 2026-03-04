package com.hypixel.hytale.server.core.universe.world.lighting;

import com.hypixel.hytale.common.util.FormatUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector2i;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.metrics.metric.AverageCollector;
import com.hypixel.hytale.protocol.ColorLight;
import com.hypixel.hytale.protocol.Opacity;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.accessor.LocalCachedChunkAccessor;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.ChunkLightData;
import com.hypixel.hytale.server.core.universe.world.chunk.section.ChunkLightDataBuilder;
import com.hypixel.hytale.server.core.universe.world.chunk.section.FluidSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Arrays;
import java.util.BitSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.IntBinaryOperator;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FloodLightCalculation implements LightCalculation {
   protected final ChunkLightingManager chunkLightingManager;
   protected final AverageCollector emptyAvg = new AverageCollector();
   protected final AverageCollector blocksAvg = new AverageCollector();
   protected final AverageCollector borderAvg = new AverageCollector();
   protected final AverageCollector avgChunk = new AverageCollector();
   protected final BlockSection[][] fromSections = new BlockSection[][]{
      new BlockSection[Vector3i.BLOCK_SIDES.length], new BlockSection[Vector3i.BLOCK_EDGES.length], new BlockSection[Vector3i.BLOCK_CORNERS.length]
   };

   public FloodLightCalculation(ChunkLightingManager chunkLightingManager) {
      this.chunkLightingManager = chunkLightingManager;
   }

   @Override
   public void init(@Nonnull WorldChunk chunk) {
      this.chunkLightingManager.getWorld().debugAssertInTickingThread();
      int x = chunk.getX();
      int z = chunk.getZ();
      this.initChunk(chunk, x, z);
      this.initNeighbours(x, z);
   }

   private void initChunk(int x, int z) {
      WorldChunk chunk = this.chunkLightingManager.getWorld().getChunkIfInMemory(ChunkUtil.indexChunk(x, z));
      if (chunk != null) {
         this.initChunk(chunk, x, z);
      }
   }

   private void initChunk(@Nonnull WorldChunk chunk, int x, int z) {
      for (int y = 0; y < 10; y++) {
         this.initSection(chunk, x, y, z);
      }
   }

   private void initNeighbours(int x, int z) {
      this.initChunk(x - 1, z - 1);
      this.initChunk(x - 1, z + 1);
      this.initChunk(x + 1, z - 1);
      this.initChunk(x + 1, z + 1);
      this.initChunk(x - 1, z);
      this.initChunk(x + 1, z);
      this.initChunk(x, z - 1);
      this.initChunk(x, z + 1);
   }

   private void initSection(@Nonnull WorldChunk chunk, int x, int y, int z) {
      BlockSection section = chunk.getBlockChunk().getSectionAtIndex(y);
      if (!section.hasLocalLight()) {
         this.chunkLightingManager.getLogger().at(Level.FINEST).log("Init chunk %d, %d, %d because doesn't have local light", x, y, z);
      } else {
         if (section.hasGlobalLight()) {
            return;
         }

         this.chunkLightingManager.getLogger().at(Level.FINEST).log("Init chunk %d, %d, %d because doesn't have global light", x, y, z);
      }

      this.chunkLightingManager.addToQueue(new Vector3i(x, y, z));
   }

   private void initNeighbours(@Nonnull LocalCachedChunkAccessor accessor, int chunkX, int chunkY, int chunkZ) {
      this.initNeighbourSections(accessor, chunkX - 1, chunkY, chunkZ - 1);
      this.initNeighbourSections(accessor, chunkX - 1, chunkY, chunkZ + 1);
      this.initNeighbourSections(accessor, chunkX + 1, chunkY, chunkZ - 1);
      this.initNeighbourSections(accessor, chunkX + 1, chunkY, chunkZ + 1);
      this.initNeighbourSections(accessor, chunkX - 1, chunkY, chunkZ);
      this.initNeighbourSections(accessor, chunkX + 1, chunkY, chunkZ);
      this.initNeighbourSections(accessor, chunkX, chunkY, chunkZ - 1);
      this.initNeighbourSections(accessor, chunkX, chunkY, chunkZ + 1);
   }

   private void initNeighbourSections(@Nonnull LocalCachedChunkAccessor accessor, int x, int y, int z) {
      WorldChunk chunk = accessor.getChunkIfInMemory(x, z);
      if (chunk != null) {
         if (y < 9) {
            this.initSection(chunk, x, y + 1, z);
         }

         if (y > 0) {
            this.initSection(chunk, x, y - 1, z);
         }
      }
   }

   @Nonnull
   @Override
   public CalculationResult calculateLight(@Nonnull Vector3i chunkPosition) {
      int chunkX = chunkPosition.x;
      int chunkY = chunkPosition.y;
      int chunkZ = chunkPosition.z;
      WorldChunk worldChunk = this.chunkLightingManager.getWorld().getChunkIfInMemory(ChunkUtil.indexChunk(chunkX, chunkZ));
      if (worldChunk == null) {
         return CalculationResult.NOT_LOADED;
      } else {
         AtomicLong chunkLightTiming = worldChunk.chunkLightTiming;
         boolean fineLoggable = this.chunkLightingManager.getLogger().at(Level.FINE).isEnabled();
         LocalCachedChunkAccessor accessor = LocalCachedChunkAccessor.atChunkCoords(this.chunkLightingManager.getWorld(), chunkX, chunkZ, 1);
         accessor.overwrite(worldChunk);
         CompletableFuture.runAsync(accessor::cacheChunksInRadius, this.chunkLightingManager.getWorld()).join();
         BlockSection toSection = worldChunk.getBlockChunk().getSectionAtIndex(chunkY);
         FluidSection fluidSection = CompletableFuture.<FluidSection>supplyAsync(() -> {
            Ref<ChunkStore> sectionx = this.chunkLightingManager.getWorld().getChunkStore().getChunkSectionReference(chunkX, chunkY, chunkZ);
            return sectionx == null ? null : sectionx.getStore().getComponent(sectionx, FluidSection.getComponentType());
         }, this.chunkLightingManager.getWorld()).join();
         if (fluidSection == null) {
            return CalculationResult.NOT_LOADED;
         } else if (toSection.hasLocalLight() && toSection.hasGlobalLight()) {
            this.initNeighbours(accessor, chunkX, chunkY, chunkZ);
            return CalculationResult.DONE;
         } else {
            if (!toSection.hasLocalLight()) {
               CalculationResult localLightResult = this.updateLocalLight(
                  accessor, worldChunk, chunkX, chunkY, chunkZ, toSection, fluidSection, chunkLightTiming, fineLoggable
               );
               switch (localLightResult) {
                  case NOT_LOADED:
                  case INVALIDATED:
                  case WAITING_FOR_NEIGHBOUR:
                     return localLightResult;
                  case DONE:
                  default:
                     this.initNeighbours(accessor, chunkX, chunkY, chunkZ);
               }
            }

            if (!toSection.hasGlobalLight()) {
               CalculationResult globalLightResult = this.updateGlobalLight(
                  accessor, worldChunk, chunkX, chunkY, chunkZ, toSection, chunkLightTiming, fineLoggable
               );
               switch (globalLightResult) {
                  case NOT_LOADED:
                  case INVALIDATED:
                  case WAITING_FOR_NEIGHBOUR:
                     return globalLightResult;
                  case DONE:
               }
            }

            if (fineLoggable) {
               long chunkDiff = chunkLightTiming.get();
               boolean done = chunkDiff != 0L;

               for (int i = 0; i < 10; i++) {
                  BlockSection section = worldChunk.getBlockChunk().getSectionAtIndex(i);
                  done = done && section.hasLocalLight() && section.hasGlobalLight();
               }

               if (done) {
                  this.avgChunk.add(chunkDiff);
                  this.chunkLightingManager
                     .getLogger()
                     .at(Level.FINE)
                     .log(
                        "Flood Chunk: Took %s at %d, %d - Avg: %s",
                        FormatUtil.nanosToString(chunkDiff),
                        chunkX,
                        chunkZ,
                        FormatUtil.nanosToString((long)this.avgChunk.get())
                     );
               }
            }

            if (BlockChunk.SEND_LOCAL_LIGHTING_DATA || BlockChunk.SEND_GLOBAL_LIGHTING_DATA) {
               worldChunk.getBlockChunk().invalidateChunkSection(chunkY);
            }

            return CalculationResult.DONE;
         }
      }
   }

   @Nonnull
   public CalculationResult updateLocalLight(
      LocalCachedChunkAccessor accessor,
      @Nonnull WorldChunk worldChunk,
      int chunkX,
      int chunkY,
      int chunkZ,
      @Nonnull BlockSection toSection,
      @Nonnull FluidSection fluidSection,
      @Nonnull AtomicLong chunkLightTiming,
      boolean fineLoggable
   ) {
      long start = System.nanoTime();
      boolean solidAir = toSection.isSolidAir() && fluidSection.isEmpty();
      ChunkLightDataBuilder localLight;
      if (solidAir) {
         localLight = this.floodEmptyChunkSection(worldChunk, toSection.getLocalChangeCounter(), chunkY);
      } else {
         localLight = this.floodChunkSection(worldChunk, toSection, fluidSection, chunkY);
      }

      toSection.setLocalLight(localLight);
      worldChunk.markNeedsSaving();
      if (fineLoggable) {
         long end = System.nanoTime();
         long diff = end - start;
         if (solidAir) {
            this.emptyAvg.add(diff);
         } else {
            this.blocksAvg.add(diff);
         }

         chunkLightTiming.addAndGet(diff);
         this.chunkLightingManager
            .getLogger()
            .at(Level.FINER)
            .log(
               "Flood Chunk Section (local): Took %s at %d, %d, %d - %s Avg: %s",
               FormatUtil.nanosToString(diff),
               chunkX,
               chunkY,
               chunkZ,
               solidAir ? "air" : "blocks",
               FormatUtil.nanosToString((long)(solidAir ? this.emptyAvg.get() : this.blocksAvg.get()))
            );
      }

      if (!toSection.hasLocalLight()) {
         this.chunkLightingManager
            .getLogger()
            .at(Level.FINEST)
            .log(
               "Chunk Section still needs relight! (local) %d, %d, %d - %d != %d (counter != id)",
               chunkX,
               chunkY,
               chunkZ,
               toSection.getLocalChangeCounter(),
               toSection.getLocalLight().getChangeId()
            );
         return CalculationResult.INVALIDATED;
      } else {
         return CalculationResult.DONE;
      }
   }

   @Nonnull
   public CalculationResult updateGlobalLight(
      @Nonnull LocalCachedChunkAccessor accessor,
      @Nonnull WorldChunk worldChunk,
      int chunkX,
      int chunkY,
      int chunkZ,
      @Nonnull BlockSection toSection,
      @Nonnull AtomicLong chunkLightTiming,
      boolean fineLoggable
   ) {
      long start = System.nanoTime();
      if (this.testNeighboursForLocalLight(accessor, worldChunk, chunkX, chunkY, chunkZ)) {
         return CalculationResult.WAITING_FOR_NEIGHBOUR;
      } else {
         ChunkLightDataBuilder globalLight = new ChunkLightDataBuilder(toSection.getLocalLight(), toSection.getGlobalChangeCounter());
         BitSet bitSetQueue = new BitSet(32768);
         this.propagateSides(toSection, globalLight, bitSetQueue);
         this.propagateEdges(toSection, globalLight, bitSetQueue);
         this.propagateCorners(toSection, globalLight, bitSetQueue);
         this.propagateLight(bitSetQueue, toSection, globalLight);
         toSection.setGlobalLight(globalLight);
         worldChunk.markNeedsSaving();
         if (fineLoggable) {
            long end = System.nanoTime();
            long diff = end - start;
            chunkLightTiming.addAndGet(diff);
            this.borderAvg.add(diff);
            this.chunkLightingManager
               .getLogger()
               .at(Level.FINER)
               .log(
                  "Flood Chunk Section (global): Took "
                     + FormatUtil.nanosToString(diff)
                     + " at "
                     + chunkX
                     + ", "
                     + chunkY
                     + ", "
                     + chunkZ
                     + " - Avg: "
                     + FormatUtil.nanosToString((long)this.borderAvg.get())
               );
         }

         if (!toSection.hasGlobalLight()) {
            this.chunkLightingManager
               .getLogger()
               .at(Level.FINEST)
               .log(
                  "Chunk Section still needs relight! (global) %d, %d, %d - %d != %d (counter != id)",
                  chunkX,
                  chunkY,
                  chunkZ,
                  toSection.getGlobalChangeCounter(),
                  toSection.getGlobalLight().getChangeId()
               );
            return CalculationResult.INVALIDATED;
         } else {
            return CalculationResult.DONE;
         }
      }
   }

   @Override
   public boolean invalidateLightAtBlock(
      @Nonnull WorldChunk worldChunk, int blockX, int blockY, int blockZ, @Nonnull BlockType blockType, int oldHeight, int newHeight
   ) {
      int chunkX = worldChunk.getX();
      int chunkY = blockY >> 5;
      int chunkZ = worldChunk.getZ();
      int oldHeightChunk = oldHeight >> 5;
      int newHeightChunk = newHeight >> 5;
      int from = Math.max(MathUtil.minValue(oldHeightChunk, newHeightChunk, chunkY), 0);
      int to = MathUtil.maxValue(oldHeightChunk, newHeightChunk, chunkY) + 1;
      boolean handled = this.invalidateLightInChunkSections(worldChunk, from, to);
      this.chunkLightingManager
         .getLogger()
         .at(Level.FINER)
         .log("updateLightAtBlock(%d, %d, %d, %s): %d, %d, %d", blockX, blockY, blockZ, blockType.getId(), chunkX, chunkY, chunkZ);
      return handled;
   }

   @Override
   public boolean invalidateLightInChunkSections(@Nonnull WorldChunk worldChunk, int sectionIndexFrom, int sectionIndexTo) {
      int chunkX = worldChunk.getX();
      int chunkZ = worldChunk.getZ();
      World world = this.chunkLightingManager.getWorld();
      LocalCachedChunkAccessor accessor = LocalCachedChunkAccessor.atChunkCoords(world, chunkX, chunkZ, 1);
      accessor.overwrite(worldChunk);
      if (!world.isInThread()) {
         CompletableFuture.runAsync(accessor::cacheChunksInRadius, world).join();
      } else {
         accessor.cacheChunksInRadius();
      }

      for (int x = chunkX - 1; x <= chunkX + 1; x++) {
         for (int z = chunkZ - 1; z <= chunkZ + 1; z++) {
            WorldChunk worldChunkTemp = accessor.getChunkIfInMemory(x, z);
            if (worldChunkTemp != null) {
               for (int y = sectionIndexTo - 1; y >= sectionIndexFrom; y--) {
                  BlockSection section = worldChunkTemp.getBlockChunk().getSectionAtIndex(y);
                  if (worldChunkTemp == worldChunk) {
                     section.invalidateLocalLight();
                  } else {
                     section.invalidateGlobalLight();
                  }

                  if (BlockChunk.SEND_LOCAL_LIGHTING_DATA || BlockChunk.SEND_GLOBAL_LIGHTING_DATA) {
                     worldChunkTemp.getBlockChunk().invalidateChunkSection(y);
                  }
               }
            }
         }
      }

      for (int x = chunkX - 1; x <= chunkX + 1; x++) {
         for (int zx = chunkZ - 1; zx <= chunkZ + 1; zx++) {
            WorldChunk worldChunkTemp = accessor.getChunkIfInMemory(x, zx);
            if (worldChunkTemp != null) {
               for (int y = sectionIndexTo - 1; y >= sectionIndexFrom; y--) {
                  this.chunkLightingManager.addToQueue(new Vector3i(x, y, zx));
               }
            }
         }
      }

      return false;
   }

   @Nonnull
   private ChunkLightDataBuilder floodEmptyChunkSection(@Nonnull WorldChunk worldChunk, short changeCounter, int chunkY) {
      int sectionY = chunkY * 32;
      ChunkLightDataBuilder light = new ChunkLightDataBuilder(changeCounter);
      BitSet bitSetQueue = new BitSet(1024);

      for (int x = 0; x < 32; x++) {
         for (int z = 0; z < 32; z++) {
            int column = ChunkUtil.indexColumn(x, z);
            short height = worldChunk.getHeight(column);
            if (sectionY > height) {
               for (int y = 0; y < 32; y++) {
                  light.setLight(ChunkUtil.indexBlockFromColumn(column, y), 3, (byte)15);
               }

               bitSetQueue.set(column);
            }
         }
      }

      if (bitSetQueue.cardinality() < 1024) {
         IntSet changedColumns = new IntOpenHashSet(1024);
         int counter = 0;

         while (true) {
            int column = bitSetQueue.nextSetBit(counter);
            if (column == -1) {
               if (bitSetQueue.isEmpty()) {
                  IntIterator iterator = changedColumns.iterator();

                  while (iterator.hasNext()) {
                     int columnx = iterator.nextInt();
                     byte skyLight = light.getLight(columnx, 3);

                     for (int y = 1; y < 32; y++) {
                        light.setLight(ChunkUtil.indexBlockFromColumn(columnx, y), 3, skyLight);
                     }
                  }
                  break;
               }

               counter = 0;
            } else {
               bitSetQueue.clear(column);
               counter = column;
               int x = ChunkUtil.xFromColumn(column);
               int zx = ChunkUtil.zFromColumn(column);
               byte skyLight = light.getLight(column, 3);
               byte propagatedValue = (byte)(skyLight - 1);
               if (propagatedValue >= 1) {
                  for (Vector2i side : Vector2i.DIRECTIONS) {
                     int nx = x + side.x;
                     int nz = zx + side.y;
                     if (nx >= 0 && nx < 32 && nz >= 0 && nz < 32) {
                        int neighbourColumn = ChunkUtil.indexColumn(nx, nz);
                        byte neighbourSkyLight = light.getLight(neighbourColumn, 3);
                        if (neighbourSkyLight < propagatedValue) {
                           light.setLight(neighbourColumn, 3, propagatedValue);
                           changedColumns.add(neighbourColumn);
                           if (propagatedValue > 1) {
                              bitSetQueue.set(neighbourColumn);
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      return light;
   }

   @Nonnull
   private ChunkLightDataBuilder floodChunkSection(
      @Nonnull WorldChunk worldChunk, @Nonnull BlockSection toSection, @Nonnull FluidSection fluidSection, int chunkY
   ) {
      int sectionY = chunkY * 32;
      ChunkLightDataBuilder toLight = new ChunkLightDataBuilder(toSection.getLocalChangeCounter());
      BitSet bitSetQueue = new BitSet(32768);

      for (int x = 0; x < 32; x++) {
         for (int z = 0; z < 32; z++) {
            int column = ChunkUtil.indexColumn(x, z);
            short height = worldChunk.getHeight(column);

            for (int y = 0; y < 32; y++) {
               int blockIndex = ChunkUtil.indexBlockFromColumn(column, y);
               byte skyValue = this.getSkyValue(worldChunk, chunkY, x, y, z, sectionY, height);
               short lightValue = (short)(skyValue << 12);
               int blockId = toSection.get(blockIndex);
               BlockType blockType = BlockType.getAssetMap().getAsset(blockId);
               ColorLight blockTypeLight = blockType.getLight();
               int fluidId = fluidSection.getFluidId(blockIndex);
               Fluid fluid = Fluid.getAssetMap().getAsset(fluidId);
               ColorLight fluidLight = fluid.getLight();
               if (blockTypeLight != null && fluidLight != null) {
                  lightValue = ChunkLightData.combineLightValues(
                     (byte)Math.max(blockTypeLight.red, fluidLight.red),
                     (byte)Math.max(blockTypeLight.green, fluidLight.green),
                     (byte)Math.max(blockTypeLight.blue, fluidLight.blue),
                     skyValue
                  );
               } else if (fluidLight != null) {
                  lightValue = ChunkLightData.combineLightValues(fluidLight.red, fluidLight.green, fluidLight.blue, skyValue);
               } else if (blockTypeLight != null) {
                  lightValue = ChunkLightData.combineLightValues(blockTypeLight.red, blockTypeLight.green, blockTypeLight.blue, skyValue);
               }

               if (lightValue != 0) {
                  toLight.setLightRaw(blockIndex, lightValue);
                  bitSetQueue.set(blockIndex);
               }
            }
         }
      }

      this.propagateLight(bitSetQueue, toSection, toLight);
      return toLight;
   }

   protected byte getSkyValue(WorldChunk worldChunk, int chunkY, int blockX, int blockY, int blockZ, int sectionY, int height) {
      int originY = sectionY + blockY;
      boolean hasSky = originY >= height;
      return (byte)(hasSky ? 15 : 0);
   }

   private void propagateLight(@Nonnull BitSet bitSetQueue, @Nonnull BlockSection section, @Nonnull ChunkLightDataBuilder light) {
      int counter = 0;

      while (true) {
         int blockIndex = bitSetQueue.nextSetBit(counter);
         if (blockIndex == -1) {
            if (bitSetQueue.isEmpty()) {
               return;
            }

            counter = 0;
         } else {
            bitSetQueue.clear(blockIndex);
            counter = blockIndex;
            BlockType fromBlockType = BlockType.getAssetMap().getAsset(section.get(blockIndex));
            Opacity fromOpacity = fromBlockType.getOpacity();
            if (fromOpacity != Opacity.Solid) {
               short lightValue = light.getLightRaw(blockIndex);
               byte redLight = ChunkLightData.getLightValue(lightValue, 0);
               byte greenLight = ChunkLightData.getLightValue(lightValue, 1);
               byte blueLight = ChunkLightData.getLightValue(lightValue, 2);
               byte skyLight = ChunkLightData.getLightValue(lightValue, 3);
               if (redLight >= 2 || greenLight >= 2 || blueLight >= 2 || skyLight >= 2) {
                  byte propagatedRedValue = (byte)(redLight - 1);
                  byte propagatedGreenValue = (byte)(greenLight - 1);
                  byte propagatedBlueValue = (byte)(blueLight - 1);
                  byte propagatedSkyValue = (byte)(skyLight - 1);
                  if (fromOpacity == Opacity.Semitransparent || fromOpacity == Opacity.Cutout) {
                     propagatedRedValue--;
                     propagatedGreenValue--;
                     propagatedBlueValue--;
                     propagatedSkyValue--;
                  }

                  if (propagatedRedValue >= 1 || propagatedGreenValue >= 1 || propagatedBlueValue >= 1 || propagatedSkyValue >= 1) {
                     int x = ChunkUtil.xFromIndex(blockIndex);
                     int y = ChunkUtil.yFromIndex(blockIndex);
                     int z = ChunkUtil.zFromIndex(blockIndex);

                     for (Vector3i side : Vector3i.BLOCK_SIDES) {
                        int nx = x + side.x;
                        if (nx >= 0 && nx < 32) {
                           int ny = y + side.y;
                           if (ny >= 0 && ny < 32) {
                              int nz = z + side.z;
                              if (nz >= 0 && nz < 32) {
                                 int neighbourBlock = ChunkUtil.indexBlock(nx, ny, nz);
                                 this.propagateLight(
                                    bitSetQueue,
                                    propagatedRedValue,
                                    propagatedGreenValue,
                                    propagatedBlueValue,
                                    propagatedSkyValue,
                                    section,
                                    light,
                                    neighbourBlock
                                 );
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public boolean testNeighboursForLocalLight(@Nonnull LocalCachedChunkAccessor accessor, @Nonnull WorldChunk worldChunk, int chunkX, int chunkY, int chunkZ) {
      Vector3i[][] blockParts = Vector3i.BLOCK_PARTS;

      for (int partType = 0; partType < this.fromSections.length; partType++) {
         BlockSection[] partSections = this.fromSections[partType];
         Arrays.fill(partSections, null);
         Vector3i[] directions = blockParts[partType];

         for (int i = 0; i < directions.length; i++) {
            Vector3i side = directions[i];
            int nx = chunkX + side.x;
            int ny = chunkY + side.y;
            int nz = chunkZ + side.z;
            if (ny >= 0 && ny < 10) {
               if (nx == chunkX && nz == chunkZ) {
                  BlockSection fromSection = worldChunk.getBlockChunk().getSectionAtIndex(ny);
                  if (!fromSection.hasLocalLight()) {
                     return true;
                  }

                  partSections[i] = fromSection;
               } else {
                  WorldChunk neighbourChunk = accessor.getChunkIfInMemory(nx, nz);
                  if (neighbourChunk == null) {
                     return true;
                  }

                  BlockSection fromSection = neighbourChunk.getBlockChunk().getSectionAtIndex(ny);
                  if (!fromSection.hasLocalLight()) {
                     return true;
                  }

                  partSections[i] = fromSection;
               }
            }
         }
      }

      return false;
   }

   public void propagateSides(@Nonnull BlockSection toSection, @Nonnull ChunkLightDataBuilder globalLight, @Nonnull BitSet bitSetQueue) {
      BlockSection[] fromSectionsSides = this.fromSections[0];
      int i = 0;
      this.propagateSide(
         bitSetQueue, fromSectionsSides[i++], toSection, globalLight, (a, b) -> ChunkUtil.indexBlock(a, 0, b), (a, b) -> ChunkUtil.indexBlock(a, 31, b)
      );
      this.propagateSide(
         bitSetQueue, fromSectionsSides[i++], toSection, globalLight, (a, b) -> ChunkUtil.indexBlock(a, 31, b), (a, b) -> ChunkUtil.indexBlock(a, 0, b)
      );
      this.propagateSide(
         bitSetQueue, fromSectionsSides[i++], toSection, globalLight, (a, b) -> ChunkUtil.indexBlock(a, b, 31), (a, b) -> ChunkUtil.indexBlock(a, b, 0)
      );
      this.propagateSide(
         bitSetQueue, fromSectionsSides[i++], toSection, globalLight, (a, b) -> ChunkUtil.indexBlock(a, b, 0), (a, b) -> ChunkUtil.indexBlock(a, b, 31)
      );
      this.propagateSide(
         bitSetQueue, fromSectionsSides[i++], toSection, globalLight, (a, b) -> ChunkUtil.indexBlock(31, a, b), (a, b) -> ChunkUtil.indexBlock(0, a, b)
      );
      this.propagateSide(
         bitSetQueue, fromSectionsSides[i++], toSection, globalLight, (a, b) -> ChunkUtil.indexBlock(0, a, b), (a, b) -> ChunkUtil.indexBlock(31, a, b)
      );
   }

   private void propagateSide(
      @Nonnull BitSet bitSetQueue,
      @Nullable BlockSection fromSection,
      @Nonnull BlockSection toSection,
      @Nonnull ChunkLightDataBuilder toLight,
      @Nonnull IntBinaryOperator fromIndex,
      @Nonnull IntBinaryOperator toIndex
   ) {
      if (fromSection != null) {
         ChunkLightData fromLight = fromSection.getLocalLight();

         for (int a = 0; a < 32; a++) {
            for (int b = 0; b < 32; b++) {
               int fromBlockIndex = fromIndex.applyAsInt(a, b);
               int toBlockIndex = toIndex.applyAsInt(a, b);
               BlockType fromBlockType = BlockType.getAssetMap().getAsset(fromSection.get(fromBlockIndex));
               Opacity fromOpacity = fromBlockType.getOpacity();
               if (fromOpacity != Opacity.Solid) {
                  short lightValue = fromLight.getLightRaw(fromBlockIndex);
                  byte redLight = ChunkLightData.getLightValue(lightValue, 0);
                  byte greenLight = ChunkLightData.getLightValue(lightValue, 1);
                  byte blueLight = ChunkLightData.getLightValue(lightValue, 2);
                  byte skyLight = ChunkLightData.getLightValue(lightValue, 3);
                  if (redLight >= 2 || greenLight >= 2 || blueLight >= 2 || skyLight >= 2) {
                     byte propagatedRedValue = (byte)(redLight - 1);
                     byte propagatedGreenValue = (byte)(greenLight - 1);
                     byte propagatedBlueValue = (byte)(blueLight - 1);
                     byte propagatedSkyValue = (byte)(skyLight - 1);
                     if (fromOpacity == Opacity.Semitransparent || fromOpacity == Opacity.Cutout) {
                        propagatedRedValue--;
                        propagatedGreenValue--;
                        propagatedBlueValue--;
                        propagatedSkyValue--;
                     }

                     if (propagatedRedValue >= 1 || propagatedGreenValue >= 1 || propagatedBlueValue >= 1 || propagatedSkyValue >= 1) {
                        this.propagateLight(
                           bitSetQueue, propagatedRedValue, propagatedGreenValue, propagatedBlueValue, propagatedSkyValue, toSection, toLight, toBlockIndex
                        );
                     }
                  }
               }
            }
         }
      }
   }

   public void propagateEdges(@Nonnull BlockSection toSection, @Nonnull ChunkLightDataBuilder globalLight, @Nonnull BitSet bitSetQueue) {
      BlockSection[] fromSectionsEdges = this.fromSections[1];
      int i = 0;
      this.propagateEdge(bitSetQueue, fromSectionsEdges[i++], toSection, globalLight, a -> ChunkUtil.indexBlock(a, 0, 31), a -> ChunkUtil.indexBlock(a, 31, 0));
      this.propagateEdge(bitSetQueue, fromSectionsEdges[i++], toSection, globalLight, a -> ChunkUtil.indexBlock(a, 31, 31), a -> ChunkUtil.indexBlock(a, 0, 0));
      this.propagateEdge(bitSetQueue, fromSectionsEdges[i++], toSection, globalLight, a -> ChunkUtil.indexBlock(a, 0, 0), a -> ChunkUtil.indexBlock(a, 31, 31));
      this.propagateEdge(bitSetQueue, fromSectionsEdges[i++], toSection, globalLight, a -> ChunkUtil.indexBlock(a, 31, 0), a -> ChunkUtil.indexBlock(a, 0, 31));
      this.propagateEdge(bitSetQueue, fromSectionsEdges[i++], toSection, globalLight, a -> ChunkUtil.indexBlock(31, 0, a), a -> ChunkUtil.indexBlock(0, 31, a));
      this.propagateEdge(bitSetQueue, fromSectionsEdges[i++], toSection, globalLight, a -> ChunkUtil.indexBlock(31, 31, a), a -> ChunkUtil.indexBlock(0, 0, a));
      this.propagateEdge(bitSetQueue, fromSectionsEdges[i++], toSection, globalLight, a -> ChunkUtil.indexBlock(0, 0, a), a -> ChunkUtil.indexBlock(31, 31, a));
      this.propagateEdge(bitSetQueue, fromSectionsEdges[i++], toSection, globalLight, a -> ChunkUtil.indexBlock(0, 31, a), a -> ChunkUtil.indexBlock(31, 0, a));
      this.propagateEdge(bitSetQueue, fromSectionsEdges[i++], toSection, globalLight, a -> ChunkUtil.indexBlock(31, a, 31), a -> ChunkUtil.indexBlock(0, a, 0));
      this.propagateEdge(bitSetQueue, fromSectionsEdges[i++], toSection, globalLight, a -> ChunkUtil.indexBlock(0, a, 31), a -> ChunkUtil.indexBlock(31, a, 0));
      this.propagateEdge(bitSetQueue, fromSectionsEdges[i++], toSection, globalLight, a -> ChunkUtil.indexBlock(31, a, 0), a -> ChunkUtil.indexBlock(0, a, 31));
      this.propagateEdge(bitSetQueue, fromSectionsEdges[i++], toSection, globalLight, a -> ChunkUtil.indexBlock(0, a, 0), a -> ChunkUtil.indexBlock(31, a, 31));
   }

   private void propagateEdge(
      @Nonnull BitSet bitSetQueue,
      @Nullable BlockSection fromSection,
      @Nonnull BlockSection toSection,
      @Nonnull ChunkLightDataBuilder toLight,
      @Nonnull Int2IntFunction fromIndex,
      @Nonnull Int2IntFunction toIndex
   ) {
      if (fromSection != null) {
         ChunkLightData fromLight = fromSection.getLocalLight();

         for (int a = 0; a < 32; a++) {
            int fromBlockIndex = fromIndex.applyAsInt(a);
            int toBlockIndex = toIndex.applyAsInt(a);
            BlockType fromBlockType = BlockType.getAssetMap().getAsset(fromSection.get(fromBlockIndex));
            Opacity fromOpacity = fromBlockType.getOpacity();
            if (fromOpacity != Opacity.Solid) {
               short lightValue = fromLight.getLightRaw(fromBlockIndex);
               byte redLight = ChunkLightData.getLightValue(lightValue, 0);
               byte greenLight = ChunkLightData.getLightValue(lightValue, 1);
               byte blueLight = ChunkLightData.getLightValue(lightValue, 2);
               byte skyLight = ChunkLightData.getLightValue(lightValue, 3);
               if (redLight >= 3 || greenLight >= 3 || blueLight >= 3 || skyLight >= 3) {
                  byte propagatedRedValue = (byte)(redLight - 2);
                  byte propagatedGreenValue = (byte)(greenLight - 2);
                  byte propagatedBlueValue = (byte)(blueLight - 2);
                  byte propagatedSkyValue = (byte)(skyLight - 2);
                  if (fromOpacity == Opacity.Semitransparent || fromOpacity == Opacity.Cutout) {
                     propagatedRedValue--;
                     propagatedGreenValue--;
                     propagatedBlueValue--;
                     propagatedSkyValue--;
                  }

                  if (propagatedRedValue >= 1 || propagatedGreenValue >= 1 || propagatedBlueValue >= 1 || propagatedSkyValue >= 1) {
                     this.propagateLight(
                        bitSetQueue, propagatedRedValue, propagatedGreenValue, propagatedBlueValue, propagatedSkyValue, toSection, toLight, toBlockIndex
                     );
                  }
               }
            }
         }
      }
   }

   public void propagateCorners(@Nonnull BlockSection toSection, @Nonnull ChunkLightDataBuilder globalLight, @Nonnull BitSet bitSetQueue) {
      BlockSection[] fromSectionsCorners = this.fromSections[2];
      int i = 0;
      this.propagateCorner(bitSetQueue, fromSectionsCorners[i++], toSection, globalLight, ChunkUtil.indexBlock(31, 0, 31), ChunkUtil.indexBlock(0, 31, 0));
      this.propagateCorner(bitSetQueue, fromSectionsCorners[i++], toSection, globalLight, ChunkUtil.indexBlock(0, 0, 31), ChunkUtil.indexBlock(31, 31, 0));
      this.propagateCorner(bitSetQueue, fromSectionsCorners[i++], toSection, globalLight, ChunkUtil.indexBlock(31, 31, 31), ChunkUtil.indexBlock(0, 0, 0));
      this.propagateCorner(bitSetQueue, fromSectionsCorners[i++], toSection, globalLight, ChunkUtil.indexBlock(0, 31, 31), ChunkUtil.indexBlock(31, 0, 0));
      this.propagateCorner(bitSetQueue, fromSectionsCorners[i++], toSection, globalLight, ChunkUtil.indexBlock(31, 0, 0), ChunkUtil.indexBlock(0, 31, 31));
      this.propagateCorner(bitSetQueue, fromSectionsCorners[i++], toSection, globalLight, ChunkUtil.indexBlock(0, 0, 0), ChunkUtil.indexBlock(31, 31, 31));
      this.propagateCorner(bitSetQueue, fromSectionsCorners[i++], toSection, globalLight, ChunkUtil.indexBlock(31, 31, 0), ChunkUtil.indexBlock(0, 0, 31));
      this.propagateCorner(bitSetQueue, fromSectionsCorners[i++], toSection, globalLight, ChunkUtil.indexBlock(0, 31, 0), ChunkUtil.indexBlock(31, 0, 31));
   }

   private void propagateCorner(
      @Nonnull BitSet bitSetQueue,
      @Nullable BlockSection fromSection,
      @Nonnull BlockSection toSection,
      @Nonnull ChunkLightDataBuilder toLight,
      int fromBlockIndex,
      int toBlockIndex
   ) {
      if (fromSection != null) {
         ChunkLightData fromLight = fromSection.getLocalLight();
         BlockType fromBlockType = BlockType.getAssetMap().getAsset(fromSection.get(fromBlockIndex));
         Opacity fromOpacity = fromBlockType.getOpacity();
         if (fromOpacity != Opacity.Solid) {
            short lightValue = fromLight.getLightRaw(fromBlockIndex);
            byte redLight = ChunkLightData.getLightValue(lightValue, 0);
            byte greenLight = ChunkLightData.getLightValue(lightValue, 1);
            byte blueLight = ChunkLightData.getLightValue(lightValue, 2);
            byte skyLight = ChunkLightData.getLightValue(lightValue, 3);
            if (redLight >= 4 || greenLight >= 4 || blueLight >= 4 || skyLight >= 4) {
               byte propagatedRedValue = (byte)(redLight - 3);
               byte propagatedGreenValue = (byte)(greenLight - 3);
               byte propagatedBlueValue = (byte)(blueLight - 3);
               byte propagatedSkyValue = (byte)(skyLight - 3);
               if (fromOpacity == Opacity.Semitransparent || fromOpacity == Opacity.Cutout) {
                  propagatedRedValue--;
                  propagatedGreenValue--;
                  propagatedBlueValue--;
                  propagatedSkyValue--;
               }

               if (propagatedRedValue >= 1 || propagatedGreenValue >= 1 || propagatedBlueValue >= 1 || propagatedSkyValue >= 1) {
                  this.propagateLight(
                     bitSetQueue, propagatedRedValue, propagatedGreenValue, propagatedBlueValue, propagatedSkyValue, toSection, toLight, toBlockIndex
                  );
               }
            }
         }
      }
   }

   private void propagateLight(
      @Nonnull BitSet bitSetQueue,
      byte propagatedRedValue,
      byte propagatedGreenValue,
      byte propagatedBlueValue,
      byte propagatedSkyValue,
      @Nonnull BlockSection toSection,
      @Nonnull ChunkLightDataBuilder toLight,
      int toBlockIndex
   ) {
      BlockType toBlockType = BlockType.getAssetMap().getAsset(toSection.get(toBlockIndex));
      Opacity toOpacity = toBlockType.getOpacity();
      if (toOpacity == Opacity.Cutout) {
         propagatedRedValue--;
         propagatedGreenValue--;
         propagatedBlueValue--;
         propagatedSkyValue--;
      }

      if (propagatedRedValue >= 1 || propagatedGreenValue >= 1 || propagatedBlueValue >= 1 || propagatedSkyValue >= 1) {
         short oldLightValue = toLight.getLightRaw(toBlockIndex);
         byte neighbourRedLight = ChunkLightData.getLightValue(oldLightValue, 0);
         byte neighbourGreenLight = ChunkLightData.getLightValue(oldLightValue, 1);
         byte neighbourBlueLight = ChunkLightData.getLightValue(oldLightValue, 2);
         byte neighbourSkyLight = ChunkLightData.getLightValue(oldLightValue, 3);
         if (neighbourRedLight < propagatedRedValue) {
            neighbourRedLight = propagatedRedValue;
         }

         if (neighbourGreenLight < propagatedGreenValue) {
            neighbourGreenLight = propagatedGreenValue;
         }

         if (neighbourBlueLight < propagatedBlueValue) {
            neighbourBlueLight = propagatedBlueValue;
         }

         if (neighbourSkyLight < propagatedSkyValue) {
            neighbourSkyLight = propagatedSkyValue;
         }

         short newLightValue = (short)((neighbourRedLight & 15) << 0);
         newLightValue = (short)(newLightValue | (neighbourGreenLight & 15) << 4);
         newLightValue = (short)(newLightValue | (neighbourBlueLight & 15) << 8);
         newLightValue = (short)(newLightValue | (neighbourSkyLight & 15) << 12);
         toLight.setLightRaw(toBlockIndex, newLightValue);
         if (newLightValue != oldLightValue && (propagatedRedValue > 1 || propagatedGreenValue > 1 || propagatedBlueValue > 1 || propagatedSkyValue > 1)) {
            bitSetQueue.set(toBlockIndex);
         }
      }
   }
}
