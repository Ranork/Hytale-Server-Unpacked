package com.hypixel.hytale.builtin.hytalegenerator.newsystem;

import com.hypixel.hytale.builtin.hytalegenerator.ArrayUtil;
import com.hypixel.hytale.builtin.hytalegenerator.FutureUtils;
import com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.chunkgenerator.ChunkGenerator;
import com.hypixel.hytale.builtin.hytalegenerator.chunkgenerator.ChunkRequest;
import com.hypixel.hytale.builtin.hytalegenerator.datastructures.voxelspace.VoxelSpace;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.material.MaterialCache;
import com.hypixel.hytale.builtin.hytalegenerator.material.SolidMaterial;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.bufferbundle.NBufferBundle;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.bufferbundle.buffers.NEntityBuffer;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.bufferbundle.buffers.NSimplePixelBuffer;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.bufferbundle.buffers.NVoxelBuffer;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.bufferbundle.buffers.type.NBufferType;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.bufferbundle.buffers.type.NParametrizedBufferType;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.performanceinstruments.TimeInstrument;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.stages.NStage;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.views.NEntityBufferView;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.views.NPixelBufferView;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.views.NVoxelBufferView;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.threadindexer.WorkerIndexer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.blocktype.component.BlockPhysics;
import com.hypixel.hytale.server.core.universe.world.chunk.environment.EnvironmentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.FluidSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.worldgen.GeneratedBlockChunk;
import com.hypixel.hytale.server.core.universe.world.worldgen.GeneratedBlockStateChunk;
import com.hypixel.hytale.server.core.universe.world.worldgen.GeneratedChunk;
import com.hypixel.hytale.server.core.universe.world.worldgen.GeneratedEntityChunk;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class NStagedChunkGenerator implements ChunkGenerator {
   public static final int WORLD_MIN_Y_BUFFER_GRID = 0;
   public static final int WORLD_MAX_Y_BUFFER_GRID = 40;
   public static final int WORLD_HEIGHT_BUFFER_GRID = 40;
   @Nonnull
   public static final Bounds3i CHUNK_BOUNDS_BUFFER_GRID = new Bounds3i(Vector3i.ZERO, new Vector3i(4, 40, 4));
   @Nonnull
   public static final Bounds3i SINGLE_BUFFER_TILE_BOUNDS_BUFFER_GRID = new Bounds3i(
      new Vector3i(0, 0, 0), new Vector3i(NVoxelBuffer.SIZE.x, 320, NVoxelBuffer.SIZE.x)
   );
   private NBufferType materialOutput_bufferType;
   private NBufferType tintOutput_bufferType;
   private NBufferType environmentOutput_bufferType;
   private NBufferType entityOutput_bufferType;
   private NStage[] stages;
   private Bounds3i[] stagesOutputBounds_bufferGrid;
   private NBufferBundle bufferBundle;
   private ExecutorService concurrentExecutor;
   private MaterialCache materialCache;
   private WorkerIndexer workerIndexer;
   private PositionProvider spawnPositions;
   private TimeInstrument timeInstrument;
   private Set<Integer> statsCheckpoints;
   private int generatedChunkCount;
   private long totalCacheBufferRequests;
   private long missedCacheBufferRequests;

   private NStagedChunkGenerator() {
   }

   @NonNullDecl
   @Override
   public GeneratedChunk generate(@Nonnull ChunkRequest.Arguments arguments) {
      if (arguments.stillNeeded() != null && !arguments.stillNeeded().test(arguments.index())) {
         return null;
      } else {
         this.generatedChunkCount++;
         TimeInstrument.Probe total_timeProbe = new TimeInstrument.Probe("Total").start();
         TimeInstrument.Probe contentGeneration_timeProbe = total_timeProbe.createProbe("Content Generation").start();
         TimeInstrument.Probe accessInit_timeProbe = contentGeneration_timeProbe.createProbe("Access Initialization").start();
         Bounds3i localChunkBounds_bufferGrid = GridUtils.createChunkBounds_bufferGrid(arguments.x(), arguments.z());
         Map<NBufferType, NBufferBundle.Access> accessMap = this.createAccesses(localChunkBounds_bufferGrid);
         accessInit_timeProbe.stop();

         for (int stageIndex = 0; stageIndex < this.stages.length; stageIndex++) {
            TimeInstrument.Probe stage_timeProbe = contentGeneration_timeProbe.createProbe(this.stages[stageIndex].getName() + " (Stage " + stageIndex + ")")
               .start();
            TimeInstrument.Probe stagePrep_timeProbe = stage_timeProbe.createProbe("Preparation").start();
            int stageIndexConst = stageIndex;
            NStage stage = this.stages[stageIndex];
            List<NBufferType> outputTypes = stage.getOutputTypes();
            List<NBufferBundle.Grid> outputGrids = new ArrayList<>(outputTypes.size());

            for (NBufferType type : outputTypes) {
               NBufferBundle.Grid grid = this.bufferBundle.getGrid(type);
               outputGrids.add(grid);
            }

            Bounds3i stageChunkOutputBounds_bufferGrid = this.stagesOutputBounds_bufferGrid[stageIndex].clone();
            stageChunkOutputBounds_bufferGrid.stack(CHUNK_BOUNDS_BUFFER_GRID);
            stageChunkOutputBounds_bufferGrid.offset(localChunkBounds_bufferGrid.min);
            List<Vector3i> positions_bufferGrid = new ArrayList<>();
            Vector3i tilePos_bufferGrid = new Vector3i(0, 0, 0);

            for (tilePos_bufferGrid.x = stageChunkOutputBounds_bufferGrid.min.x;
               tilePos_bufferGrid.x < stageChunkOutputBounds_bufferGrid.max.x;
               tilePos_bufferGrid.x++
            ) {
               for (tilePos_bufferGrid.z = stageChunkOutputBounds_bufferGrid.min.z;
                  tilePos_bufferGrid.z < stageChunkOutputBounds_bufferGrid.max.z;
                  tilePos_bufferGrid.z++
               ) {
                  tilePos_bufferGrid.dropHash();
                  this.totalCacheBufferRequests++;
                  boolean isOutputCached = true;

                  for (NBufferBundle.Grid grid : outputGrids) {
                     NBufferBundle.Access access = accessMap.get(grid.getBufferType());
                     if (!isColumnCached(access, tilePos_bufferGrid, stageIndex)) {
                        isOutputCached = false;
                        break;
                     }
                  }

                  if (!isOutputCached) {
                     this.missedCacheBufferRequests++;
                     positions_bufferGrid.add(tilePos_bufferGrid.clone());
                  }
               }
            }

            List<List<Vector3i>> splitPositions_bufferGrid = ArrayUtil.split(positions_bufferGrid, this.workerIndexer.getWorkerCount());
            WorkerIndexer.Session workerSession = this.workerIndexer.createSession();
            List<List<Runnable>> allTasks = new ArrayList<>();

            for (int i = 0; i < splitPositions_bufferGrid.size(); i++) {
               List<Vector3i> workerPositions_bufferGrid = splitPositions_bufferGrid.get(i);
               WorkerIndexer.Id workerId = workerSession.next();
               List<Runnable> workerTasks = new ArrayList<>(workerPositions_bufferGrid.size());

               for (int j = 0; j < workerPositions_bufferGrid.size(); j++) {
                  Vector3i position_bufferGrid = workerPositions_bufferGrid.get(j);
                  Runnable tileTask = this.createTileTask(stageIndexConst, position_bufferGrid, workerId, accessMap);
                  workerTasks.add(tileTask);
               }

               allTasks.add(workerTasks);
            }

            stagePrep_timeProbe.stop();
            TimeInstrument.Probe stageExecution_timeProbe = stage_timeProbe.createProbe("Execution").start();
            TimeInstrument.Probe taskStart_timeProbe = stageExecution_timeProbe.createProbe("Async Processes Start").start();
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (List<Runnable> workerTasks : allTasks) {
               CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                  for (Runnable task : workerTasks) {
                     task.run();
                  }
               }, this.concurrentExecutor).handle((r, e) -> {
                  if (e == null) {
                     return (Void)r;
                  } else {
                     LoggerUtil.logException("during async execution of stage " + stage, e);
                     return null;
                  }
               });
               futures.add(future);
            }

            taskStart_timeProbe.stop();
            FutureUtils.allOf(futures).join();
            stageExecution_timeProbe.stop();
            stage_timeProbe.stop();
         }

         contentGeneration_timeProbe.stop();
         TimeInstrument.Probe transfer_timeProbe = total_timeProbe.createProbe("Data Transfer").start();
         GeneratedChunk outputChunk = new GeneratedChunk(
            new GeneratedBlockChunk(arguments.index(), arguments.x(), arguments.z()),
            new GeneratedBlockStateChunk(),
            new GeneratedEntityChunk(),
            GeneratedChunk.makeSections()
         );
         List<CompletableFuture<Void>> futures = new ArrayList<>();
         futures.add(this.transferMaterials(arguments, outputChunk, transfer_timeProbe));
         futures.add(this.transferEnvironments(arguments, outputChunk, transfer_timeProbe));
         futures.add(this.transferTints(arguments, outputChunk, transfer_timeProbe));
         futures.add(this.transferEntities(arguments, outputChunk, transfer_timeProbe));
         futures.add(this.transferBlockStates(arguments, outputChunk.getBlockStateChunk(), transfer_timeProbe));
         FutureUtils.allOf(futures).join();
         transfer_timeProbe.stop();
         total_timeProbe.stop();
         this.timeInstrument.takeSample(total_timeProbe);
         if (this.statsCheckpoints.contains(this.generatedChunkCount)) {
            NBufferBundle.MemoryReport bufferMemoryReport = this.bufferBundle.createMemoryReport();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(this.timeInstrument.toString())
               .append(bufferMemoryReport.toString())
               .append(this.createContextDependencyReport(0))
               .append(this.createBufferRequestCacheReport());
            LoggerUtil.getLogger().info(stringBuilder.toString());
         }

         this.bufferBundle.closeALlAccesses();
         return outputChunk;
      }
   }

   @NonNullDecl
   @Override
   public PositionProvider getSpawnPositions() {
      return this.spawnPositions;
   }

   @Nonnull
   private Map<NBufferType, NBufferBundle.Access> createAccesses(@Nonnull Bounds3i localChunkBounds_bufferGrid) {
      Map<NBufferType, NBufferBundle.Access> accessMap = new HashMap<>();

      for (int stageIndex = 0; stageIndex < this.stages.length; stageIndex++) {
         NStage stage = this.stages[stageIndex];
         List<NBufferType> outputTypes = stage.getOutputTypes();
         Bounds3i bounds_bufferGrid = this.stagesOutputBounds_bufferGrid[stageIndex].clone();
         bounds_bufferGrid.stack(CHUNK_BOUNDS_BUFFER_GRID);
         bounds_bufferGrid.offset(localChunkBounds_bufferGrid.min);

         for (NBufferType bufferType : outputTypes) {
            NBufferBundle.Access access = this.bufferBundle.createBufferAccess(bufferType, bounds_bufferGrid);
            accessMap.put(bufferType, access);
         }
      }

      return accessMap;
   }

   @Nonnull
   private Runnable createTileTask(
      int stageIndex, @Nonnull Vector3i position_bufferTileGrid, @Nonnull WorkerIndexer.Id workerId, @Nonnull Map<NBufferType, NBufferBundle.Access> accessMap
   ) {
      NStage stage = this.stages[stageIndex];
      Map<NBufferType, Bounds3i> inputTypesAndBounds_tileGrid = stage.getInputTypesAndBounds_bufferGrid();
      List<NBufferType> outputTypes = stage.getOutputTypes();
      int bufferAccessCount = inputTypesAndBounds_tileGrid.size() + outputTypes.size();
      NStage.Context context = new NStage.Context(new HashMap<>(bufferAccessCount), workerId);

      for (Entry<NBufferType, Bounds3i> entry : inputTypesAndBounds_tileGrid.entrySet()) {
         NBufferType bufferType = entry.getKey();
         Bounds3i localInputBounds_bufferGrid = entry.getValue().clone().offset(position_bufferTileGrid);
         NBufferBundle.Access.View bufferAccess = accessMap.get(bufferType).createView(localInputBounds_bufferGrid);
         context.bufferAccess.put(bufferType, bufferAccess);
      }

      for (NBufferType bufferType : stage.getOutputTypes()) {
         assert !context.bufferAccess.containsKey(bufferType);

         Bounds3i columnBounds_bufferGrid = GridUtils.createColumnBounds_bufferGrid(position_bufferTileGrid, 0, 40);
         NBufferBundle.Access.View bufferAccess = accessMap.get(bufferType).createView(columnBounds_bufferGrid);
         context.bufferAccess.put(bufferType, bufferAccess);
      }

      Vector3i bufferPositionClone_bufferTileGrid = position_bufferTileGrid.clone();
      return () -> {
         stage.run(context);

         for (NBufferType outputType : stage.getOutputTypes()) {
            updateTrackersForColumn(stageIndex, accessMap.get(outputType).createView(), bufferPositionClone_bufferTileGrid);
         }
      };
   }

   @Nonnull
   private CompletableFuture<Void> transferBlockStates(
      @Nonnull ChunkRequest.Arguments arguments, @Nonnull GeneratedBlockStateChunk blockStateChunk, @Nonnull TimeInstrument.Probe transfer_timeProbe
   ) {
      Bounds3i chunkBounds_voxelGrid = GridUtils.createChunkBounds_voxelGrid(arguments.x(), arguments.z());
      Bounds3i chunkBounds_bufferGrid = GridUtils.createChunkBounds_bufferGrid(arguments.x(), arguments.z());
      NBufferBundle.Access materialBufferAccess = this.bufferBundle.createBufferAccess(this.materialOutput_bufferType, chunkBounds_bufferGrid);
      VoxelSpace<Material> materialVoxelSpace = new NVoxelBufferView<>(materialBufferAccess.createView(), Material.class);
      TimeInstrument.Probe timeProbe = transfer_timeProbe.createProbe("Block States");
      return CompletableFuture.runAsync(() -> {
         timeProbe.start();
         Vector3i position_voxelGrid = new Vector3i();

         for (position_voxelGrid.x = chunkBounds_voxelGrid.min.x; position_voxelGrid.x < chunkBounds_voxelGrid.max.x; position_voxelGrid.x++) {
            for (position_voxelGrid.z = chunkBounds_voxelGrid.min.z; position_voxelGrid.z < chunkBounds_voxelGrid.max.z; position_voxelGrid.z++) {
               for (position_voxelGrid.y = chunkBounds_voxelGrid.min.y; position_voxelGrid.y < chunkBounds_voxelGrid.max.y; position_voxelGrid.y++) {
                  SolidMaterial solidMaterial = materialVoxelSpace.getContent(position_voxelGrid).solid();
                  if (solidMaterial != null && solidMaterial.holder != null) {
                     blockStateChunk.setState(position_voxelGrid.x, position_voxelGrid.y, position_voxelGrid.z, solidMaterial.holder);
                  }
               }
            }
         }

         timeProbe.stop();
      }, this.concurrentExecutor).handle((r, e) -> {
         if (e == null) {
            return (Void)r;
         } else {
            LoggerUtil.logException("a HytaleGenerator async process", e, LoggerUtil.getLogger());
            return null;
         }
      });
   }

   @Nonnull
   private CompletableFuture<Void> transferMaterials(
      @Nonnull ChunkRequest.Arguments arguments, @Nonnull GeneratedChunk generatedChunk, @Nonnull TimeInstrument.Probe transfer_timeProbe
   ) {
      Bounds3i chunkBounds_voxelGrid = GridUtils.createChunkBounds_voxelGrid(arguments.x(), arguments.z());
      Bounds3i chunkBounds_bufferGrid = GridUtils.createChunkBounds_bufferGrid(arguments.x(), arguments.z());
      NBufferBundle.Access materialBufferAccess = this.bufferBundle.createBufferAccess(this.materialOutput_bufferType, chunkBounds_bufferGrid);
      VoxelSpace<Material> materialVoxelSpace = new NVoxelBufferView<>(materialBufferAccess.createView(), Material.class);
      GeneratedBlockChunk blockChunk = generatedChunk.getBlockChunk();
      Holder<ChunkStore>[] sections = generatedChunk.getSections();
      FluidSection[] fluidSections = new FluidSection[sections.length];

      for (int sectionIndex = 0; sectionIndex < 10; sectionIndex++) {
         Holder<ChunkStore> section = sections[sectionIndex];
         FluidSection fluidSection = section.ensureAndGetComponent(FluidSection.getComponentType());
         fluidSections[sectionIndex] = fluidSection;
      }

      CompletableFuture<?>[] futures = new CompletableFuture[10];

      for (int sectionIndex = 0; sectionIndex < 10; sectionIndex++) {
         int sectionIndexFinal = sectionIndex;
         TimeInstrument.Probe section_timeProbe = transfer_timeProbe.createProbe("Materials Section " + sectionIndexFinal);
         CompletableFuture<Void> task = CompletableFuture.runAsync(
               () -> {
                  section_timeProbe.start();
                  Holder<ChunkStore> section = sections[sectionIndexFinal];
                  FluidSection fluidSection = fluidSections[sectionIndexFinal];
                  Vector3i world_voxelGrid = new Vector3i();

                  for (int x_voxelGrid = 0; x_voxelGrid < 32; x_voxelGrid++) {
                     for (int z_voxelGrid = 0; z_voxelGrid < 32; z_voxelGrid++) {
                        int minY_voxelGrid = sectionIndexFinal * 32;
                        int maxY_voxelGrid = minY_voxelGrid + 32;

                        for (int y_voxelGrid = minY_voxelGrid; y_voxelGrid < maxY_voxelGrid; y_voxelGrid++) {
                           int sectionY = y_voxelGrid - minY_voxelGrid;
                           world_voxelGrid.x = x_voxelGrid + chunkBounds_voxelGrid.min.x;
                           world_voxelGrid.y = y_voxelGrid + chunkBounds_voxelGrid.min.y;
                           world_voxelGrid.z = z_voxelGrid + chunkBounds_voxelGrid.min.z;
                           Material material = materialVoxelSpace.getContent(world_voxelGrid);
                           if (material != null && !material.equals(this.materialCache.EMPTY)) {
                              blockChunk.setBlock(
                                 x_voxelGrid, y_voxelGrid, z_voxelGrid, material.solid().blockId, material.solid().rotation, material.solid().filler
                              );
                              setSupport(generatedChunk, x_voxelGrid, y_voxelGrid, z_voxelGrid, material.solid().blockId, material.solid().support);
                              fluidSection.setFluid(x_voxelGrid, sectionY, z_voxelGrid, material.fluid().fluidId, material.fluid().fluidLevel);
                           }
                        }
                     }
                  }

                  section_timeProbe.stop();
               },
               this.concurrentExecutor
            )
            .handle((r, e) -> {
               if (e == null) {
                  return (Void)r;
               } else {
                  LoggerUtil.logException("a HytaleGenerator async process", e, LoggerUtil.getLogger());
                  return null;
               }
            });
         futures[sectionIndex] = task;
      }

      return CompletableFuture.allOf(futures).thenRun(blockChunk::generateHeight);
   }

   @Nonnull
   private CompletableFuture<Void> transferTints(
      @Nonnull ChunkRequest.Arguments arguments, @Nonnull GeneratedChunk generatedChunk, @Nonnull TimeInstrument.Probe transfer_timeProbe
   ) {
      Bounds3i chunkBounds_voxelGrid = GridUtils.createChunkBounds_voxelGrid(arguments.x(), arguments.z());
      Bounds3i chunkBounds_bufferGrid = GridUtils.createChunkBounds_bufferGrid(arguments.x(), arguments.z());
      NBufferBundle.Access tintBufferAccess = this.bufferBundle.createBufferAccess(this.tintOutput_bufferType, chunkBounds_bufferGrid);
      VoxelSpace<Integer> tintVoxelSpace = new NPixelBufferView<>(tintBufferAccess.createView(), Integer.class);
      GeneratedBlockChunk blockChunk = generatedChunk.getBlockChunk();
      TimeInstrument.Probe tintsTransfer_timeProbe = transfer_timeProbe.createProbe("Tints");
      return CompletableFuture.runAsync(() -> {
         tintsTransfer_timeProbe.start();
         Vector3i worldPosition_voxelGrid = new Vector3i();
         worldPosition_voxelGrid.y = 0;

         for (int x_voxelGrid = 0; x_voxelGrid < 32; x_voxelGrid++) {
            for (int z_voxelGrid = 0; z_voxelGrid < 32; z_voxelGrid++) {
               worldPosition_voxelGrid.x = x_voxelGrid + chunkBounds_voxelGrid.min.x;
               worldPosition_voxelGrid.z = z_voxelGrid + chunkBounds_voxelGrid.min.z;
               Integer tint = tintVoxelSpace.getContent(worldPosition_voxelGrid);
               if (tint == null) {
                  blockChunk.setTint(x_voxelGrid, z_voxelGrid, 0);
               } else {
                  blockChunk.setTint(x_voxelGrid, z_voxelGrid, tint);
               }
            }
         }

         tintsTransfer_timeProbe.stop();
      }, this.concurrentExecutor).handle((r, e) -> {
         if (e == null) {
            return (Void)r;
         } else {
            LoggerUtil.logException("a HytaleGenerator async process", e, LoggerUtil.getLogger());
            return null;
         }
      });
   }

   @Nonnull
   private CompletableFuture<Void> transferEnvironments(
      @Nonnull ChunkRequest.Arguments arguments, @Nonnull GeneratedChunk generatedChunk, @Nonnull TimeInstrument.Probe transfer_timeProbe
   ) {
      Bounds3i chunkBounds_voxelGrid = GridUtils.createChunkBounds_voxelGrid(arguments.x(), arguments.z());
      Bounds3i chunkBounds_bufferGrid = GridUtils.createChunkBounds_bufferGrid(arguments.x(), arguments.z());
      NBufferBundle.Access environmentBufferAccess = this.bufferBundle.createBufferAccess(this.environmentOutput_bufferType, chunkBounds_bufferGrid);
      VoxelSpace<Integer> environmentVoxelSpace = new NVoxelBufferView<>(environmentBufferAccess.createView(), Integer.class);
      EnvironmentChunk.BulkWriter bulkWriter = new EnvironmentChunk.BulkWriter();
      int sectionCounter = 0;
      Vector3i taskSize = new Vector3i(8, 0, 8);
      Vector3i taskCount = new Vector3i(32 / taskSize.x, 0, 32 / taskSize.z);
      List<CompletableFuture<Void>> futures = new ArrayList<>();

      for (int taskX = 0; taskX < taskCount.x; taskX++) {
         for (int taskZ = 0; taskZ < taskCount.z; taskZ++) {
            TimeInstrument.Probe timeProbe = transfer_timeProbe.createProbe("Environment Section " + sectionCounter++);
            int minX_voxelGrid = taskX * taskSize.x;
            int minZ_voxelGrid = taskZ * taskSize.z;
            int maxX_voxelGrid = minX_voxelGrid + taskSize.x;
            int maxZ_voxelGrid = minZ_voxelGrid + taskSize.z;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
               timeProbe.start();
               Vector3i world_voxelGrid = new Vector3i();

               for (int x_voxelGrid = minX_voxelGrid; x_voxelGrid < maxX_voxelGrid; x_voxelGrid++) {
                  for (int z_voxelGrid = minZ_voxelGrid; z_voxelGrid < maxZ_voxelGrid; z_voxelGrid++) {
                     EnvironmentChunk.BulkWriter.ColumnWriter columnWriter = bulkWriter.getColumnWriter(x_voxelGrid, z_voxelGrid);
                     int x_voxelGrid_final = x_voxelGrid;
                     int z_voxelGrid_final = z_voxelGrid;
                     columnWriter.intake(y_voxelGrid -> {
                        world_voxelGrid.x = x_voxelGrid_final + chunkBounds_voxelGrid.min.x;
                        world_voxelGrid.y = y_voxelGrid + chunkBounds_voxelGrid.min.y;
                        world_voxelGrid.z = z_voxelGrid_final + chunkBounds_voxelGrid.min.z;
                        world_voxelGrid.dropHash();
                        Integer environment = environmentVoxelSpace.getContent(world_voxelGrid);

                        assert environment != null;

                        return environment;
                     });
                  }
               }

               timeProbe.stop();
            }, this.concurrentExecutor);
            futures.add(future);
         }
      }

      TimeInstrument.Probe timeProbe = transfer_timeProbe.createProbe("Environment Write");
      return FutureUtils.allOf(futures).thenRun(() -> {
         timeProbe.start();
         bulkWriter.write(generatedChunk.getBlockChunk().getEnvironmentChunk());
         timeProbe.start();
      }).handle((r, e) -> {
         if (e == null) {
            return (Void)r;
         } else {
            LoggerUtil.logException("a HytaleGenerator async process", e, LoggerUtil.getLogger());
            return null;
         }
      });
   }

   @Nonnull
   private CompletableFuture<Void> transferEntities(
      @Nonnull ChunkRequest.Arguments arguments, @Nonnull GeneratedChunk generatedChunk, @Nonnull TimeInstrument.Probe transfer_timeProbe
   ) {
      Bounds3i chunkBounds_bufferGrid = GridUtils.createChunkBounds_bufferGrid(arguments.x(), arguments.z());
      NBufferBundle.Access entityBufferAccess = this.bufferBundle.createBufferAccess(this.entityOutput_bufferType, chunkBounds_bufferGrid);
      NEntityBufferView entityView = new NEntityBufferView(entityBufferAccess.createView());
      GeneratedEntityChunk entityChunk = generatedChunk.getEntityChunk();
      TimeInstrument.Probe entitesTransfer_timeProbe = transfer_timeProbe.createProbe("Entities");
      return CompletableFuture.runAsync(() -> {
         entitesTransfer_timeProbe.start();
         entityView.forEach(e -> entityChunk.addEntities(e.getOffset(), e.getRotation(), new Holder[]{e.getEntityHolder()}, arguments.seed()));
         entitesTransfer_timeProbe.stop();
      }, this.concurrentExecutor).handle((r, e) -> {
         if (e == null) {
            return (Void)r;
         } else {
            LoggerUtil.logException("a HytaleGenerator async process", e, LoggerUtil.getLogger());
            return null;
         }
      });
   }

   @Nonnull
   private String createBufferRequestCacheReport() {
      StringBuilder builder = new StringBuilder();
      builder.append("Buffer Cache Report\n");
      builder.append("Total Cache Buffer Requests: ").append(this.totalCacheBufferRequests).append("\n");
      builder.append("Missed Cache Buffer Requests: ").append(this.missedCacheBufferRequests).append("\n");
      double ratio = (double)this.missedCacheBufferRequests / this.totalCacheBufferRequests * 100.0;
      builder.append("Missed/Total Ratio: ").append(ratio).append("%\n");
      return builder.toString();
   }

   @Nonnull
   private String createContextDependencyReport(int indentation) {
      StringBuilder builder = new StringBuilder();
      builder.append("Context Dependency Report\n");

      for (int stageIndex = 0; stageIndex < this.stages.length; stageIndex++) {
         Bounds3i bounds_bufferGrid = this.stagesOutputBounds_bufferGrid[stageIndex];
         Vector3i size_bufferGrid = bounds_bufferGrid.getSize().add(3, 0, 3);
         Vector3d size_chunkGrid = new Vector3d(size_bufferGrid);
         size_chunkGrid.scale(0.25);
         builder.append("\t".repeat(indentation)).append(this.stages[stageIndex].getName()).append(" (Stage ").append(stageIndex).append("):\n");
         builder.append("\t".repeat(indentation + 1))
            .append("Output Size (Buffer Column): {x=")
            .append(size_bufferGrid.x)
            .append(", z=")
            .append(size_bufferGrid.z)
            .append("}\n");
         builder.append("\t".repeat(indentation + 1))
            .append("Output Size (Chunk Column): {x=")
            .append(size_chunkGrid.x)
            .append(", z=")
            .append(size_chunkGrid.z)
            .append("}\n");
      }

      return builder.toString();
   }

   private static void setSupport(@Nonnull GeneratedChunk chunk, int x, int y, int z, int blockId, int supportValue) {
      Holder<ChunkStore>[] sections = chunk.getSections();
      Holder<ChunkStore> section = sections[ChunkUtil.chunkCoordinate(y)];
      if (supportValue >= 0) {
         BlockPhysics.setSupportValue(section, x, y, z, supportValue);
      } else {
         BlockType blockType = BlockType.getAssetMap().getAsset(blockId);
         if (blockType != null && blockType.hasSupport()) {
            BlockPhysics.reset(section, x, y, z);
         } else {
            BlockPhysics.clear(section, x, y, z);
         }
      }
   }

   private static void setBoundsToWorldHeight_bufferGrid(@Nonnull Bounds3i bounds_bufferGrid) {
      bounds_bufferGrid.min.setY(0);
      bounds_bufferGrid.max.setY(40);
   }

   private static boolean isColumnCached(@Nonnull NBufferBundle.Access access, @Nonnull Vector3i position_bufferGrid, int stageIndex) {
      assert position_bufferGrid.y == 0;

      NBufferBundle.Tracker tracker = access.getBuffer(position_bufferGrid).tracker();
      return tracker.stageIndex == stageIndex;
   }

   private static void updateTrackersForColumn(int stageIndex, @Nonnull NBufferBundle.Access.View access, @Nonnull Vector3i position_bufferGrid) {
      for (position_bufferGrid.y = 0; position_bufferGrid.y < 40; position_bufferGrid.y++) {
         position_bufferGrid.dropHash();
         NBufferBundle.Tracker tracker = access.getBuffer(position_bufferGrid).tracker();
         tracker.stageIndex = stageIndex;
      }
   }

   public static class Builder {
      @Nonnull
      public final NParametrizedBufferType MATERIAL_OUTPUT_BUFFER_TYPE = new NParametrizedBufferType(
         "MaterialResult", -1, NVoxelBuffer.class, Material.class, () -> new NVoxelBuffer<>(Material.class)
      );
      @Nonnull
      public final NParametrizedBufferType TINT_OUTPUT_BUFFER_TYPE = new NParametrizedBufferType(
         "TintResult", -3, NSimplePixelBuffer.class, Integer.class, () -> new NSimplePixelBuffer<>(Integer.class)
      );
      @Nonnull
      public final NParametrizedBufferType ENVIRONMENT_OUTPUT_BUFFER_TYPE = new NParametrizedBufferType(
         "EnvironmentResult", -4, NVoxelBuffer.class, Integer.class, () -> new NVoxelBuffer<>(Integer.class)
      );
      @Nonnull
      public final NBufferType ENTITY_OUTPUT_BUFFER_TYPE = new NBufferType("EntityResult", -5, NEntityBuffer.class, NEntityBuffer::new);
      private List<NStage> stages = new ArrayList<>();
      private ExecutorService concurrentExecutor;
      private MaterialCache materialCache;
      private WorkerIndexer workerIndexer;
      private String statsHeader;
      private Set<Integer> statsCheckpoints;
      private PositionProvider spawnPositions;
      private double bufferCapacityFactor;
      private double targetViewDistance;
      private double targetPlayerCount;

      @Nonnull
      public NStagedChunkGenerator build() {
         assert this.concurrentExecutor != null;

         assert this.materialCache != null;

         assert this.workerIndexer != null;

         assert this.statsHeader != null;

         assert this.statsCheckpoints != null;

         assert this.spawnPositions != null;

         NStagedChunkGenerator instance = new NStagedChunkGenerator();
         instance.materialOutput_bufferType = this.MATERIAL_OUTPUT_BUFFER_TYPE;
         instance.tintOutput_bufferType = this.TINT_OUTPUT_BUFFER_TYPE;
         instance.environmentOutput_bufferType = this.ENVIRONMENT_OUTPUT_BUFFER_TYPE;
         instance.entityOutput_bufferType = this.ENTITY_OUTPUT_BUFFER_TYPE;
         instance.stages = new NStage[this.stages.size()];
         this.stages.toArray(instance.stages);
         Set<NBufferType> allUsedBufferTypes = this.createListOfAllBufferTypes();
         Map<Integer, Set<Integer>> laterToEalierStageMap = this.createStageDependencyMap();
         instance.stagesOutputBounds_bufferGrid = this.createTotalOutputBoundsArray(laterToEalierStageMap);
         instance.bufferBundle = new NBufferBundle();
         instance.bufferBundle
            .createGrid(this.MATERIAL_OUTPUT_BUFFER_TYPE, this.resolveBufferCapacity(this.MATERIAL_OUTPUT_BUFFER_TYPE, instance.stagesOutputBounds_bufferGrid));
         instance.bufferBundle
            .createGrid(this.TINT_OUTPUT_BUFFER_TYPE, this.resolveBufferCapacity(this.TINT_OUTPUT_BUFFER_TYPE, instance.stagesOutputBounds_bufferGrid));
         instance.bufferBundle
            .createGrid(
               this.ENVIRONMENT_OUTPUT_BUFFER_TYPE, this.resolveBufferCapacity(this.ENVIRONMENT_OUTPUT_BUFFER_TYPE, instance.stagesOutputBounds_bufferGrid)
            );
         instance.bufferBundle
            .createGrid(this.ENTITY_OUTPUT_BUFFER_TYPE, this.resolveBufferCapacity(this.ENTITY_OUTPUT_BUFFER_TYPE, instance.stagesOutputBounds_bufferGrid));

         for (NBufferType bufferType : allUsedBufferTypes) {
            if (!this.isGeneratorOutputBufferType(bufferType)) {
               instance.bufferBundle.createGrid(bufferType, this.resolveBufferCapacity(bufferType, instance.stagesOutputBounds_bufferGrid));
            }
         }

         instance.concurrentExecutor = this.concurrentExecutor;
         instance.materialCache = this.materialCache;
         instance.workerIndexer = this.workerIndexer;
         instance.timeInstrument = new TimeInstrument(this.statsHeader);
         instance.statsCheckpoints = new HashSet<>(this.statsCheckpoints);
         instance.generatedChunkCount = 0;
         instance.spawnPositions = this.spawnPositions;
         return instance;
      }

      @Nonnull
      public NStagedChunkGenerator.Builder withStats(@Nonnull String statsHeader, @Nonnull Set<Integer> statsCheckpoints) {
         this.statsHeader = statsHeader;
         this.statsCheckpoints = new HashSet<>(statsCheckpoints);
         return this;
      }

      @Nonnull
      public NStagedChunkGenerator.Builder withSpawnPositions(@Nonnull PositionProvider spawnPositions) {
         this.spawnPositions = spawnPositions;
         return this;
      }

      @Nonnull
      public NStagedChunkGenerator.Builder withConcurrentExecutor(@Nonnull ExecutorService executor, @Nonnull WorkerIndexer workerIndexer) {
         this.concurrentExecutor = executor;
         this.workerIndexer = workerIndexer;
         return this;
      }

      @Nonnull
      public NStagedChunkGenerator.Builder withMaterialCache(@Nonnull MaterialCache materialCache) {
         this.materialCache = materialCache;
         return this;
      }

      @Nonnull
      public NStagedChunkGenerator.Builder withBufferCapacity(double factor, double targetViewDistance, double targetPlayerCount) {
         assert factor >= 0.0;

         assert targetViewDistance >= 0.0;

         assert targetPlayerCount >= 0.0;

         this.bufferCapacityFactor = factor;
         this.targetViewDistance = targetViewDistance;
         this.targetPlayerCount = targetPlayerCount;
         return this;
      }

      @Nonnull
      public NStagedChunkGenerator.Builder appendStage(@Nonnull NStage stage) {
         this.stages.add(stage);
         return this;
      }

      @Nonnull
      private List<Integer> createStagesThatReadFrom(int stageIndex) {
         NStage stage = this.stages.get(stageIndex);
         List<Integer> stagesThatReadFromThis = new ArrayList<>();
         List<NBufferType> outputTypes = stage.getOutputTypes();

         for (int i = 0; i < outputTypes.size(); i++) {
            NBufferType outputType = outputTypes.get(i);

            for (int j = 0; j < this.stages.size(); j++) {
               NStage dependentStage = this.stages.get(j);
               if (dependentStage.getInputTypesAndBounds_bufferGrid().containsKey(outputType)) {
                  stagesThatReadFromThis.add(j);
               }
            }
         }

         return stagesThatReadFromThis;
      }

      @Nonnull
      private Map<Integer, Set<Integer>> createStageDependencyMap() {
         Map<Integer, Set<Integer>> dependencyMap = new HashMap<>();

         for (int stageIndex = 0; stageIndex < this.stages.size(); stageIndex++) {
            dependencyMap.put(stageIndex, new HashSet<>(1));
         }

         for (int stageIndex = 0; stageIndex < this.stages.size(); stageIndex++) {
            for (Integer dependentStage : this.createStagesThatReadFrom(stageIndex)) {
               dependencyMap.get(dependentStage).add(stageIndex);
            }
         }

         return dependencyMap;
      }

      private int resolveBufferCapacity(@Nonnull NBufferType bufferType, @Nonnull Bounds3i[] stagesOutputBounds) {
         int stageIndex = 0;

         while (stageIndex < stagesOutputBounds.length && !this.stages.get(stageIndex).getOutputTypes().contains(bufferType)) {
            stageIndex++;
         }

         if (stageIndex >= stagesOutputBounds.length) {
            return 0;
         } else {
            Bounds3i outputBounds = stagesOutputBounds[stageIndex];
            return calculateCapacityFromBounds(outputBounds, this.bufferCapacityFactor, this.targetViewDistance, this.targetPlayerCount);
         }
      }

      private static int calculateCapacityFromBounds(@Nonnull Bounds3i bounds, double factor, double viewDistance_voxelGrid, double playerCount) {
         assert factor >= 0.0;

         assert viewDistance_voxelGrid >= 0.0;

         assert playerCount >= 0.0;

         Vector3i size = bounds.getSize();
         if (size.x == 1 && size.z == 1) {
            return 0;
         } else {
            double viewDistance_bufferGrid = viewDistance_voxelGrid / NVoxelBuffer.SIZE.x;
            double entireArea = size.x + viewDistance_bufferGrid * 2.0;
            entireArea *= size.z + viewDistance_bufferGrid * 2.0;
            double holeArea;
            if (!(size.x > viewDistance_bufferGrid) && !(size.z > viewDistance_bufferGrid)) {
               holeArea = (viewDistance_bufferGrid - size.x / 2.0) * (viewDistance_bufferGrid - size.z / 2.0);
            } else {
               holeArea = 0.0;
            }

            double ringArea = entireArea - holeArea;
            double totalPlayersArea = ringArea * playerCount;
            double factoredArea = totalPlayersArea * factor;
            double totalVolume = factoredArea * 40.0;

            assert totalVolume >= 0.0;

            return Math.max(0, (int)totalVolume);
         }
      }

      private void createTotalOutputBoundsForStage(
         int stageIndex, @Nonnull Map<Integer, Set<Integer>> stageDependencyMap, @Nonnull Bounds3i[] totalOutputBoundsPerStage_bufferGrid
      ) {
         Bounds3i initialOutputBounds_bufferGrid = new Bounds3i(Vector3i.ZERO, Vector3i.ALL_ONES);
         NStage stage = this.stages.get(stageIndex);
         List<Bounds3i> allOutputBounds = new ArrayList<>();

         for (int dependentStageIndex = this.stages.size() - 1; dependentStageIndex >= stageIndex + 1; dependentStageIndex--) {
            if (stageDependencyMap.get(dependentStageIndex).contains(stageIndex)) {
               NStage dependentStage = this.stages.get(dependentStageIndex);
               Map<NBufferType, Bounds3i> dependentInputTypesAndBounds_bufferGrid = dependentStage.getInputTypesAndBounds_bufferGrid();

               for (NBufferType thisStageOutputTypes : stage.getOutputTypes()) {
                  Bounds3i dependentStageInputBounds_bufferGrid = dependentInputTypesAndBounds_bufferGrid.get(thisStageOutputTypes);
                  if (dependentStageInputBounds_bufferGrid != null) {
                     Bounds3i totalDependentStageOutputBounds_bufferGrid = totalOutputBoundsPerStage_bufferGrid[dependentStageIndex];
                     Bounds3i totalThisStageOutputBounds_bufferGrid = totalDependentStageOutputBounds_bufferGrid.clone()
                        .stack(dependentStageInputBounds_bufferGrid);
                     allOutputBounds.add(totalThisStageOutputBounds_bufferGrid);
                  }
               }
            }
         }

         if (allOutputBounds.isEmpty()) {
            NStagedChunkGenerator.setBoundsToWorldHeight_bufferGrid(initialOutputBounds_bufferGrid);
            totalOutputBoundsPerStage_bufferGrid[stageIndex] = initialOutputBounds_bufferGrid;
         } else {
            Bounds3i totalOutputBounds_bufferGrid = allOutputBounds.getFirst().clone();

            for (int i = 1; i < allOutputBounds.size(); i++) {
               totalOutputBounds_bufferGrid.encompass(allOutputBounds.get(i));
            }

            NStagedChunkGenerator.setBoundsToWorldHeight_bufferGrid(totalOutputBounds_bufferGrid);
            totalOutputBoundsPerStage_bufferGrid[stageIndex] = totalOutputBounds_bufferGrid;
         }
      }

      @Nonnull
      private Bounds3i[] createTotalOutputBoundsArray(@Nonnull Map<Integer, Set<Integer>> stageDependencyMap) {
         Bounds3i[] totalOutputBounds_bufferGrid = new Bounds3i[this.stages.size()];

         for (int stageIndex = this.stages.size() - 1; stageIndex >= 0; stageIndex--) {
            this.createTotalOutputBoundsForStage(stageIndex, stageDependencyMap, totalOutputBounds_bufferGrid);
         }

         return totalOutputBounds_bufferGrid;
      }

      @Nonnull
      private Set<NBufferType> createListOfAllBufferTypes() {
         Set<NBufferType> allBufferTypes = new HashSet<>();

         for (int stageIndex = 0; stageIndex < this.stages.size(); stageIndex++) {
            NStage stage = this.stages.get(stageIndex);
            allBufferTypes.addAll(stage.getInputTypesAndBounds_bufferGrid().keySet());
            allBufferTypes.addAll(stage.getOutputTypes());
         }

         return allBufferTypes;
      }

      @Nonnull
      private static Bounds3i getEncompassingBounds(@Nonnull Collection<Bounds3i> set) {
         Bounds3i out = new Bounds3i();

         for (Bounds3i bounds : set) {
            out.encompass(bounds);
         }

         return out;
      }

      private boolean isGeneratorOutputBufferType(@Nonnull NBufferType bufferType) {
         return bufferType.equals(this.MATERIAL_OUTPUT_BUFFER_TYPE)
            || bufferType.equals(this.TINT_OUTPUT_BUFFER_TYPE)
            || bufferType.equals(this.ENVIRONMENT_OUTPUT_BUFFER_TYPE)
            || bufferType.equals(this.ENTITY_OUTPUT_BUFFER_TYPE);
      }
   }
}
