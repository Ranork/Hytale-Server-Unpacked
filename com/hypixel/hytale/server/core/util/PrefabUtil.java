package com.hypixel.hytale.server.core.util;

import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.common.util.CompletableFutureUtil;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.blocktype.component.BlockPhysics;
import com.hypixel.hytale.server.core.modules.entity.component.FromPrefab;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.prefab.PrefabCopyableComponent;
import com.hypixel.hytale.server.core.prefab.PrefabRotation;
import com.hypixel.hytale.server.core.prefab.event.PrefabPasteEvent;
import com.hypixel.hytale.server.core.prefab.event.PrefabPlaceEntityEvent;
import com.hypixel.hytale.server.core.prefab.selection.buffer.PrefabBufferCall;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.IPrefabBuffer;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.accessor.LocalCachedChunkAccessor;
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkColumn;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.FluidSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PrefabUtil {
   protected static final String EDITOR_BLOCK = "Editor_Block";
   protected static final String EDITOR_BLOCK_PREFAB_AIR = "Editor_Empty";
   protected static final String EDITOR_BLOCK_PREFAB_ANCHOR = "Editor_Anchor";
   private static final AtomicInteger PREFAB_ID_SOURCE = new AtomicInteger(0);

   public static boolean prefabMatchesAtPosition(
      @Nonnull IPrefabBuffer prefabBuffer, World world, @Nonnull Vector3i position, @Nonnull Rotation yaw, Random random
   ) {
      double xLength = prefabBuffer.getMaxX() - prefabBuffer.getMinX();
      double zLength = prefabBuffer.getMaxZ() - prefabBuffer.getMinZ();
      int prefabRadius = (int)MathUtil.fastFloor(0.5 * Math.sqrt(xLength * xLength + zLength * zLength));
      LocalCachedChunkAccessor chunkAccessor = LocalCachedChunkAccessor.atWorldCoords(world, position.getX(), position.getZ(), prefabRadius);
      return prefabBuffer.compare((x, y, z, blockId, rotation, holder, prefabBufferCall) -> {
         int bx = position.x + x;
         int by = position.y + y;
         int bz = position.z + z;
         WorldChunk chunk = chunkAccessor.getNonTickingChunk(ChunkUtil.indexChunkFromBlock(bx, bz));
         int blockIdAtPos = chunk.getBlock(bx, by, bz);
         return blockIdAtPos == blockId;
      }, new PrefabBufferCall(random, PrefabRotation.fromRotation(yaw)));
   }

   public static boolean canPlacePrefab(
      @Nonnull IPrefabBuffer prefabBuffer,
      World world,
      @Nonnull Vector3i position,
      @Nonnull Rotation yaw,
      @Nullable IntSet mask,
      Random random,
      boolean ignoreOrigin
   ) {
      double xLength = prefabBuffer.getMaxX() - prefabBuffer.getMinX();
      double zLength = prefabBuffer.getMaxZ() - prefabBuffer.getMinZ();
      int prefabRadius = (int)MathUtil.fastFloor(0.5 * Math.sqrt(xLength * xLength + zLength * zLength));
      LocalCachedChunkAccessor chunkAccessor = LocalCachedChunkAccessor.atWorldCoords(world, position.getX(), position.getZ(), prefabRadius);
      return prefabBuffer.compare(
         (x, y, z, blockId, rotation, holder, prefabBufferCall) -> {
            if (ignoreOrigin && x == 0 && y == 0 && z == 0) {
               return true;
            } else {
               int bx = position.x + x;
               int by = position.y + y;
               int bz = position.z + z;
               WorldChunk chunk = chunkAccessor.getNonTickingChunk(ChunkUtil.indexChunkFromBlock(bx, bz));
               return chunk.testPlaceBlock(
                  bx,
                  by,
                  bz,
                  BlockType.getAssetMap().getAsset(blockId),
                  rotation,
                  (x1, y1, z1, blockType, _rotation, filler) -> mask != null && mask.contains(BlockType.getAssetMap().getIndex(blockType.getId()))
               );
            }
         },
         new PrefabBufferCall(random, PrefabRotation.fromRotation(yaw))
      );
   }

   public static void paste(
      @Nonnull IPrefabBuffer buffer,
      @Nonnull World world,
      @Nonnull Vector3i position,
      @Nonnull Rotation yaw,
      boolean force,
      Random random,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      paste(buffer, world, position, yaw, force, random, 0, componentAccessor);
   }

   public static void paste(
      @Nonnull IPrefabBuffer buffer,
      @Nonnull World world,
      @Nonnull Vector3i position,
      @Nonnull Rotation yaw,
      boolean force,
      Random random,
      int setBlockSettings,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      paste(buffer, world, position, yaw, force, random, setBlockSettings, false, false, false, componentAccessor);
   }

   public static int getNextPrefabId() {
      return PREFAB_ID_SOURCE.getAndIncrement();
   }

   public static void paste(
      @Nonnull IPrefabBuffer buffer,
      @Nonnull World world,
      @Nonnull Vector3i position,
      @Nonnull Rotation yaw,
      boolean force,
      Random random,
      int setBlockSettings,
      boolean technicalPaste,
      boolean pasteAnchorAsBlock,
      boolean loadEntities,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      double xLength = buffer.getMaxX() - buffer.getMinX();
      double zLength = buffer.getMaxZ() - buffer.getMinZ();
      int prefabRadius = (int)MathUtil.fastFloor(0.5 * Math.sqrt(xLength * xLength + zLength * zLength));
      LocalCachedChunkAccessor chunkAccessor = LocalCachedChunkAccessor.atWorldCoords(world, position.getX(), position.getZ(), prefabRadius);
      BlockTypeAssetMap<String, BlockType> blockTypeMap = BlockType.getAssetMap();
      int editorBlock = blockTypeMap.getIndex("Editor_Block");
      if (editorBlock == Integer.MIN_VALUE) {
         throw new IllegalArgumentException("Unknown key! Editor_Block");
      } else {
         PrefabRotation rotation = PrefabRotation.fromRotation(yaw);
         int prefabId = getNextPrefabId();
         PrefabPasteEvent startEvent = new PrefabPasteEvent(prefabId, true);
         componentAccessor.invoke(startEvent);
         if (!startEvent.isCancelled()) {
            buffer.forEach(IPrefabBuffer.iterateAllColumns(), (x, y, z, blockId, holder, supportValue, blockRotation, filler, call, fluidId, fluidLevel) -> {
               int bx = position.x + x;
               int by = position.y + y;
               int bz = position.z + z;
               WorldChunk chunk = chunkAccessor.getNonTickingChunk(ChunkUtil.indexChunkFromBlock(bx, bz));
               Store<ChunkStore> fluidStore = world.getChunkStore().getStore();
               ChunkColumn fluidColumn = fluidStore.getComponent(chunk.getReference(), ChunkColumn.getComponentType());
               Ref<ChunkStore> section = fluidColumn.getSection(ChunkUtil.chunkCoordinate(by));
               FluidSection fluidSection = fluidStore.ensureAndGetComponent(section, FluidSection.getComponentType());
               fluidSection.setFluid(bx, by, bz, fluidId, (byte)fluidLevel);
               BlockType block;
               if (technicalPaste) {
                  if (blockId == 0 && fluidId == 0) {
                     block = blockTypeMap.getAsset("Editor_Empty");
                  } else {
                     block = blockTypeMap.getAsset(blockId);
                  }
               } else {
                  block = blockTypeMap.getAsset(blockId);
               }

               String blockKey = block.getId();
               if (filler != 0) {
                  if (holder != null) {
                     chunk.setState(bx, by, bz, holder.clone());
                  }
               } else {
                  if (pasteAnchorAsBlock && technicalPaste && x == buffer.getAnchorX() && y == buffer.getAnchorY() && z == buffer.getAnchorZ()) {
                     int index = blockTypeMap.getIndex("Editor_Anchor");
                     BlockType type = blockTypeMap.getAsset(index);
                     chunk.setBlock(bx, by, bz, index, type, blockRotation, filler, setBlockSettings);
                  } else if (!force) {
                     RotationTuple rot = RotationTuple.get(blockRotation);
                     chunk.placeBlock(bx, by, bz, blockKey, rot.yaw(), rot.pitch(), rot.roll(), setBlockSettings);
                  } else {
                     int index = blockTypeMap.getIndex(blockKey);
                     BlockType type = blockTypeMap.getAsset(index);
                     chunk.setBlock(bx, by, bz, index, type, blockRotation, filler, setBlockSettings);
                  }

                  if (supportValue != 0) {
                     if (!world.isInThread()) {
                        CompletableFutureUtil._catch(CompletableFuture.runAsync(() -> {
                           Ref<ChunkStore> refx = chunk.getReference();
                           Store<ChunkStore> storex = refx.getStore();
                           ChunkColumn columnx = storex.getComponent(refx, ChunkColumn.getComponentType());
                           BlockPhysics.setSupportValue(storex, columnx.getSection(ChunkUtil.chunkCoordinate(by)), bx, by, bz, supportValue);
                        }, world));
                     } else {
                        Ref<ChunkStore> ref = chunk.getReference();
                        Store<ChunkStore> store = ref.getStore();
                        ChunkColumn column = store.getComponent(ref, ChunkColumn.getComponentType());
                        BlockPhysics.setSupportValue(store, column.getSection(ChunkUtil.chunkCoordinate(by)), bx, by, bz, supportValue);
                     }
                  }

                  if (holder != null) {
                     chunk.setState(bx, by, bz, holder.clone());
                  }
               }
            }, (x, z, entityWrappers, t) -> {
               if (loadEntities) {
                  if (entityWrappers != null && entityWrappers.length != 0) {
                     for (int i = 0; i < entityWrappers.length; i++) {
                        Holder<EntityStore> entityToAdd = entityWrappers[i].clone();
                        TransformComponent transformComp = entityToAdd.getComponent(TransformComponent.getComponentType());
                        if (transformComp != null) {
                           Vector3d entityPosition = transformComp.getPosition().clone();
                           rotation.rotate(entityPosition);
                           Vector3d entityWorldPosition = entityPosition.add(position);
                           transformComp = entityToAdd.getComponent(TransformComponent.getComponentType());
                           if (transformComp != null) {
                              entityPosition = transformComp.getPosition();
                              entityPosition.x = entityWorldPosition.x;
                              entityPosition.y = entityWorldPosition.y;
                              entityPosition.z = entityWorldPosition.z;
                              PrefabPlaceEntityEvent prefabPlaceEntityEvent = new PrefabPlaceEntityEvent(prefabId, entityToAdd);
                              componentAccessor.invoke(prefabPlaceEntityEvent);
                              entityToAdd.ensureComponent(FromPrefab.getComponentType());
                              if (technicalPaste) {
                                 entityToAdd.ensureComponent(PrefabCopyableComponent.getComponentType());
                              }

                              componentAccessor.addEntity(entityToAdd, AddReason.LOAD);
                           }
                        }
                     }
                  }
               }
            }, (x, y, z, path, fitHeightmap, inheritSeed, inheritHeightCondition, weights, rot, t) -> {}, new PrefabBufferCall(random, rotation));
            PrefabPasteEvent endEvent = new PrefabPasteEvent(prefabId, false);
            componentAccessor.invoke(endEvent);
         }
      }
   }

   public static void remove(
      @Nonnull IPrefabBuffer prefabBuffer, @Nonnull World world, @Nonnull Vector3i position, boolean force, @Nonnull Random random, int setBlockSettings
   ) {
      remove(prefabBuffer, world, position, force, random, setBlockSettings, 1.0);
   }

   public static void remove(
      @Nonnull IPrefabBuffer prefabBuffer,
      @Nonnull World world,
      @Nonnull Vector3i position,
      boolean force,
      @Nonnull Random random,
      int setBlockSettings,
      double brokenParticlesRate
   ) {
   }

   public static void remove(
      @Nonnull IPrefabBuffer prefabBuffer,
      @Nonnull World world,
      @Nonnull Vector3i position,
      Rotation prefabRotation,
      boolean force,
      @Nonnull Random random,
      int setBlockSettings,
      double brokenParticlesRate
   ) {
      double xLength = prefabBuffer.getMaxX() - prefabBuffer.getMinX();
      double zLength = prefabBuffer.getMaxZ() - prefabBuffer.getMinZ();
      int prefabRadius = (int)MathUtil.fastFloor(0.5 * Math.sqrt(xLength * xLength + zLength * zLength));
      LocalCachedChunkAccessor chunkAccessor = LocalCachedChunkAccessor.atWorldCoords(world, position.getX(), position.getZ(), prefabRadius);
      BlockTypeAssetMap<String, BlockType> blockTypeMap = BlockType.getAssetMap();
      prefabBuffer.forEach(
         IPrefabBuffer.iterateAllColumns(),
         (x, y, z, blockId, state, support, rotation, filler, call, fluidId, fluidLevel) -> {
            int bx = position.x + x;
            int by = position.y + y;
            int bz = position.z + z;
            WorldChunk chunk = chunkAccessor.getNonTickingChunk(ChunkUtil.indexChunkFromBlock(bx, bz));
            Store<ChunkStore> store = world.getChunkStore().getStore();
            if (fluidId != 0) {
               ChunkColumn column = store.getComponent(chunk.getReference(), ChunkColumn.getComponentType());
               Ref<ChunkStore> section = column.getSection(ChunkUtil.chunkCoordinate(by));
               FluidSection fluidSection = store.ensureAndGetComponent(section, FluidSection.getComponentType());
               fluidSection.setFluid(bx, by, bz, 0, (byte)0);
            }

            if (blockId != 0) {
               if (filler == 0) {
                  int updatedSetBlockSettings = setBlockSettings;
                  if ((setBlockSettings & 4) != 4 && random.nextDouble() > brokenParticlesRate) {
                     updatedSetBlockSettings = setBlockSettings | 4;
                  }

                  if (!force) {
                     chunk.breakBlock(bx, by, bz, updatedSetBlockSettings);
                  } else {
                     chunk.setBlock(bx, by, bz, "Empty", updatedSetBlockSettings);
                  }
               }
            }
         },
         (x, z, entityWrappers, t) -> {},
         (x, y, z, path, fitHeightmap, inheritSeed, inheritHeightCondition, weights, rotation, t) -> {},
         new PrefabBufferCall(random, PrefabRotation.fromRotation(prefabRotation))
      );
   }
}
