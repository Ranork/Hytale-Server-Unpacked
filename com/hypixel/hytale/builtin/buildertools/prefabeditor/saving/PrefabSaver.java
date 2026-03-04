package com.hypixel.hytale.builtin.buildertools.prefabeditor.saving;

import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.component.ComponentRegistry;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.math.vector.VectorBoxUtil;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.blocktype.component.BlockPhysics;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.BlockEntity;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.prefab.PrefabCopyableComponent;
import com.hypixel.hytale.server.core.prefab.PrefabSaveException;
import com.hypixel.hytale.server.core.prefab.PrefabStore;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkColumn;
import com.hypixel.hytale.server.core.universe.world.chunk.EntityChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.FluidSection;
import com.hypixel.hytale.server.core.universe.world.meta.BlockState;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PrefabSaver {
   protected static final String EDITOR_BLOCK = "Editor_Block";
   protected static final String EDITOR_BLOCK_PREFAB_AIR = "Editor_Empty";
   protected static final String EDITOR_BLOCK_PREFAB_ANCHOR = "Editor_Anchor";

   @Nonnull
   public static CompletableFuture<Boolean> savePrefab(
      @Nonnull CommandSender sender,
      @Nonnull World world,
      @Nonnull Path pathToSave,
      @Nonnull Vector3i anchorPoint,
      @Nonnull Vector3i minPoint,
      @Nonnull Vector3i maxPoint,
      @Nonnull Vector3i pastePosition,
      @Nonnull Vector3i originalFileAnchor,
      @Nonnull PrefabSaverSettings settings
   ) {
      return copyBlocksAsync(sender, world, anchorPoint, minPoint, maxPoint, pastePosition, originalFileAnchor, settings)
         .thenApplyAsync(blockSelection -> blockSelection == null ? false : save(sender, blockSelection, pathToSave, settings), world);
   }

   @Nonnull
   private static CompletableFuture<BlockSelection> copyBlocksAsync(
      @Nonnull CommandSender sender,
      @Nonnull World world,
      @Nonnull Vector3i anchorPoint,
      @Nonnull Vector3i minPoint,
      @Nonnull Vector3i maxPoint,
      @Nonnull Vector3i pastePosition,
      @Nonnull Vector3i originalFileAnchor,
      @Nonnull PrefabSaverSettings settings
   ) {
      ChunkStore chunkStore = world.getChunkStore();
      BlockTypeAssetMap<String, BlockType> assetMap = BlockType.getAssetMap();
      int editorBlock = assetMap.getIndex("Editor_Block");
      if (editorBlock == Integer.MIN_VALUE) {
         sender.sendMessage(Message.translation("server.commands.editprefab.save.error.unknownBlockIdKey").param("key", "Editor_Block".toString()));
         return CompletableFuture.completedFuture(null);
      } else {
         int editorBlockPrefabAir = assetMap.getIndex("Editor_Empty");
         if (editorBlockPrefabAir == Integer.MIN_VALUE) {
            sender.sendMessage(Message.translation("server.commands.editprefab.save.error.unknownBlockIdKey").param("key", "Editor_Empty".toString()));
            return CompletableFuture.completedFuture(null);
         } else {
            int editorBlockPrefabAnchor = assetMap.getIndex("Editor_Anchor");
            if (editorBlockPrefabAnchor == Integer.MIN_VALUE) {
               sender.sendMessage(Message.translation("server.commands.editprefab.save.error.unknownBlockIdKey").param("key", "Editor_Anchor".toString()));
               return CompletableFuture.completedFuture(null);
            } else {
               return preloadChunksInSelectionAsync(chunkStore, minPoint, maxPoint)
                  .thenApplyAsync(
                     loadedChunks -> copyBlocksWithLoadedChunks(
                        sender,
                        world,
                        anchorPoint,
                        minPoint,
                        maxPoint,
                        pastePosition,
                        originalFileAnchor,
                        settings,
                        (Long2ObjectMap<Ref<ChunkStore>>)loadedChunks,
                        editorBlock,
                        editorBlockPrefabAir,
                        editorBlockPrefabAnchor
                     ),
                     world
                  );
            }
         }
      }
   }

   @Nullable
   private static BlockSelection copyBlocksWithLoadedChunks(
      @Nonnull CommandSender sender,
      @Nonnull World world,
      @Nonnull Vector3i anchorPoint,
      @Nonnull Vector3i minPoint,
      @Nonnull Vector3i maxPoint,
      @Nonnull Vector3i pastePosition,
      @Nonnull Vector3i originalFileAnchor,
      @Nonnull PrefabSaverSettings settings,
      @Nonnull Long2ObjectMap<Ref<ChunkStore>> loadedChunks,
      int editorBlock,
      int editorBlockPrefabAir,
      int editorBlockPrefabAnchor
   ) {
      ChunkStore chunkStore = world.getChunkStore();
      long start = System.nanoTime();
      int width = maxPoint.x - minPoint.x;
      int height = maxPoint.y - minPoint.y;
      int depth = maxPoint.z - minPoint.z;
      int newAnchorX = anchorPoint.x - pastePosition.x;
      int newAnchorY = anchorPoint.y - pastePosition.y;
      int newAnchorZ = anchorPoint.z - pastePosition.z;
      BlockSelection selection = new BlockSelection();
      selection.setPosition(pastePosition.x - originalFileAnchor.x, pastePosition.y - originalFileAnchor.y, pastePosition.z - originalFileAnchor.z);
      selection.setSelectionArea(minPoint, maxPoint);
      selection.setAnchor(newAnchorX, newAnchorY, newAnchorZ);
      int blockCount = 0;
      int fluidCount = 0;
      int top = Math.max(minPoint.y, maxPoint.y);
      int bottom = Math.min(minPoint.y, maxPoint.y);

      for (int x = minPoint.x; x <= maxPoint.x; x++) {
         for (int z = minPoint.z; z <= maxPoint.z; z++) {
            long chunkIndex = ChunkUtil.indexChunkFromBlock(x, z);
            Ref<ChunkStore> chunkRef = (Ref<ChunkStore>)loadedChunks.get(chunkIndex);
            if (chunkRef != null && chunkRef.isValid()) {
               WorldChunk worldChunkComponent = chunkStore.getStore().getComponent(chunkRef, WorldChunk.getComponentType());

               assert worldChunkComponent != null;

               ChunkColumn chunkColumnComponent = chunkStore.getStore().getComponent(chunkRef, ChunkColumn.getComponentType());

               assert chunkColumnComponent != null;

               for (int y = top; y >= bottom; y--) {
                  int sectionIndex = ChunkUtil.indexSection(y);
                  Ref<ChunkStore> sectionRef = chunkColumnComponent.getSection(sectionIndex);
                  if (sectionRef != null && sectionRef.isValid()) {
                     BlockSection sectionComponent = chunkStore.getStore().getComponent(sectionRef, BlockSection.getComponentType());

                     assert sectionComponent != null;

                     BlockPhysics blockPhysicsComponent = chunkStore.getStore().getComponent(sectionRef, BlockPhysics.getComponentType());
                     int block = sectionComponent.get(x, y, z);
                     int filler = sectionComponent.getFiller(x, y, z);
                     if (settings.isBlocks() && (block != 0 || settings.isEmpty()) && block != editorBlock) {
                        if (block == editorBlockPrefabAir) {
                           block = 0;
                        }

                        Holder<ChunkStore> holder = worldChunkComponent.getBlockComponentHolder(x, y, z);
                        if (holder != null) {
                           holder = holder.clone();
                           BlockState blockState = BlockState.getBlockState(holder);
                           if (blockState != null) {
                              blockState.clearPositionForSerialization();
                           }
                        }

                        int supportValue = settings.isClearSupportValues() ? 0 : (blockPhysicsComponent != null ? blockPhysicsComponent.get(x, y, z) : 0);
                        selection.addBlockAtWorldPos(x, y, z, block, sectionComponent.getRotationIndex(x, y, z), filler, supportValue, holder);
                        blockCount++;
                     }

                     FluidSection fluidSectionComponent = chunkStore.getStore().getComponent(sectionRef, FluidSection.getComponentType());

                     assert fluidSectionComponent != null;

                     int fluid = fluidSectionComponent.getFluidId(x, y, z);
                     if (settings.isBlocks() && (fluid != 0 || settings.isEmpty())) {
                        byte fluidLevel = fluidSectionComponent.getFluidLevel(x, y, z);
                        selection.addFluidAtWorldPos(x, y, z, fluid, fluidLevel);
                        fluidCount++;
                     }
                  }
               }
            }
         }
      }

      if (settings.isEntities()) {
         Store<EntityStore> store = world.getEntityStore().getStore();
         ComponentType<EntityStore, BlockEntity> blockEntityType = BlockEntity.getComponentType();
         Set<UUID> addedEntityUuids = new HashSet<>();
         ComponentRegistry.Data<EntityStore> data = EntityStore.REGISTRY.getData();
         ComponentType<EntityStore, PrefabCopyableComponent> prefabCopyableType = PrefabCopyableComponent.getComponentType();
         ComponentType<EntityStore, TransformComponent> transformType = TransformComponent.getComponentType();
         BuilderToolsPlugin.forEachCopyableInSelection(world, minPoint.x, minPoint.y, minPoint.z, width, height, depth, e -> {
            BlockEntity blockEntityx = store.getComponent(e, blockEntityType);
            if (blockEntityx != null) {
               String key = blockEntityx.getBlockTypeKey();
               if (key != null && (key.equals("Editor_Block") || key.equals("Editor_Empty") || key.equals("Editor_Anchor"))) {
                  return;
               }
            }

            Holder<EntityStore> holderx = store.copyEntity(e);
            UUIDComponent uuidCompx = holderx.getComponent(UUIDComponent.getComponentType());
            if (uuidCompx != null) {
               addedEntityUuids.add(uuidCompx.getUuid());
            }

            TransformComponent transformx = holderx.getComponent(transformType);
            if (transformx != null && transformx.getPosition() != null) {
               transformx.getPosition().subtract(selection.getX(), selection.getY(), selection.getZ());
            }

            selection.addEntityHolderRaw(holderx);
         });
         ObjectIterator var53 = loadedChunks.values().iterator();

         while (var53.hasNext()) {
            Ref<ChunkStore> chunkRef = (Ref<ChunkStore>)var53.next();
            EntityChunk entityChunk = chunkStore.getStore().getComponent(chunkRef, EntityChunk.getComponentType());
            if (entityChunk != null) {
               for (Holder<EntityStore> holder : entityChunk.getEntityHolders()) {
                  UUIDComponent uuidComp = holder.getComponent(UUIDComponent.getComponentType());
                  TransformComponent transform = holder.getComponent(transformType);
                  Vector3d position = transform != null ? transform.getPosition() : null;
                  boolean hasPrefabCopyable = holder.getArchetype().contains(prefabCopyableType);
                  boolean hasSerializable = holder.hasSerializableComponents(data);
                  if (hasPrefabCopyable && hasSerializable) {
                     BlockEntity blockEntity = holder.getComponent(blockEntityType);
                     if (blockEntity != null) {
                        String key = blockEntity.getBlockTypeKey();
                        if (key != null && (key.equals("Editor_Block") || key.equals("Editor_Empty") || key.equals("Editor_Anchor"))) {
                           continue;
                        }
                     }

                     if (transform != null
                        && position != null
                        && VectorBoxUtil.isInside(minPoint.x, minPoint.y, minPoint.z, 0.0, 0.0, 0.0, width + 1, height + 1, depth + 1, position)
                        && (uuidComp == null || !addedEntityUuids.contains(uuidComp.getUuid()))) {
                        if (uuidComp != null) {
                           addedEntityUuids.add(uuidComp.getUuid());
                        }

                        Holder<EntityStore> clonedHolder = holder.clone();
                        TransformComponent clonedTransform = clonedHolder.getComponent(transformType);
                        if (clonedTransform != null && clonedTransform.getPosition() != null) {
                           clonedTransform.getPosition().subtract(selection.getX(), selection.getY(), selection.getZ());
                        }

                        selection.addEntityHolderRaw(clonedHolder);
                     }
                  }
               }
            }
         }

         selection.sortEntitiesByPosition();
      }

      long end = System.nanoTime();
      long diff = end - start;
      BuilderToolsPlugin.get()
         .getLogger()
         .at(Level.FINE)
         .log("Took: %dns (%dms) to execute copy of %d blocks, %d fluids", diff, TimeUnit.NANOSECONDS.toMillis(diff), blockCount, fluidCount);
      return selection;
   }

   @Nonnull
   private static CompletableFuture<Long2ObjectMap<Ref<ChunkStore>>> preloadChunksInSelectionAsync(
      @Nonnull ChunkStore chunkStore, @Nonnull Vector3i minPoint, @Nonnull Vector3i maxPoint
   ) {
      LongSet chunkIndices = new LongOpenHashSet();
      int minChunkX = minPoint.x >> 5;
      int maxChunkX = maxPoint.x >> 5;
      int minChunkZ = minPoint.z >> 5;
      int maxChunkZ = maxPoint.z >> 5;

      for (int cx = minChunkX; cx <= maxChunkX; cx++) {
         for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
            chunkIndices.add(ChunkUtil.indexChunk(cx, cz));
         }
      }

      Long2ObjectMap<Ref<ChunkStore>> loadedChunks = new Long2ObjectOpenHashMap(chunkIndices.size());
      List<CompletableFuture<Void>> chunkFutures = new ArrayList<>(chunkIndices.size());
      LongIterator var10 = chunkIndices.iterator();

      while (var10.hasNext()) {
         long chunkIndex = (Long)var10.next();
         CompletableFuture<Void> future = chunkStore.getChunkReferenceAsync(chunkIndex).thenAccept(reference -> {
            if (reference != null && reference.isValid()) {
               synchronized (loadedChunks) {
                  loadedChunks.put(chunkIndex, reference);
               }
            }
         });
         chunkFutures.add(future);
      }

      return CompletableFuture.allOf(chunkFutures.toArray(CompletableFuture[]::new)).thenApply(v -> loadedChunks);
   }

   private static boolean save(
      @Nonnull CommandSender sender, @Nonnull BlockSelection copiedSelection, @Nonnull Path saveFilePath, @Nonnull PrefabSaverSettings settings
   ) {
      if (saveFilePath.getFileSystem() != FileSystems.getDefault()) {
         sender.sendMessage(Message.translation("server.builderTools.cannotSaveToReadOnlyPath").param("path", saveFilePath.toString()));
         return false;
      } else {
         try {
            long start = System.nanoTime();
            BlockSelection postClone = settings.isRelativize() ? copiedSelection.relativize() : copiedSelection.cloneSelection();
            PrefabStore.get().savePrefab(saveFilePath, postClone, settings.isOverwriteExisting());
            long diff = System.nanoTime() - start;
            BuilderToolsPlugin.get()
               .getLogger()
               .at(Level.FINE)
               .log("Took: %dns (%dms) to execute save of %d blocks", diff, TimeUnit.NANOSECONDS.toMillis(diff), copiedSelection.getBlockCount());
            return true;
         } catch (PrefabSaveException var9) {
            switch (var9.getType()) {
               case ERROR:
                  ((HytaleLogger.Api)BuilderToolsPlugin.get().getLogger().at(Level.WARNING).withCause(var9)).log("Exception saving prefab %s", saveFilePath);
                  sender.sendMessage(
                     Message.translation("server.builderTools.errorSavingPrefab")
                        .param("name", saveFilePath.toString())
                        .param("message", var9.getCause().getMessage())
                  );
                  break;
               case ALREADY_EXISTS:
                  BuilderToolsPlugin.get().getLogger().at(Level.WARNING).log("Prefab already exists %s", saveFilePath.toString());
                  sender.sendMessage(Message.translation("server.builderTools.prefabAlreadyExists"));
            }

            return false;
         }
      }
   }
}
