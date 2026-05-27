package com.hypixel.hytale.server.core.universe.world.connectedblocks;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkFlag;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3i;
import org.joml.Vector3ic;

public class ConnectedBlocksUtil {
   private static final int MAX_UPDATE_DEPTH = 3;

   public static void setConnectedBlockAndNotifyNeighbors(
      @Nonnull ChunkStore chunkStore,
      int blockTypeId,
      @Nonnull RotationTuple blockTypeRotation,
      @Nonnull Vector3ic placementNormal,
      @Nonnull Vector3ic blockPosition,
      @Nonnull WorldChunk worldChunkComponent,
      @Nonnull BlockChunk blockChunkComponent
   ) {
      Vector3i coordinate = new Vector3i(blockPosition);
      BlockType blockType = BlockType.getAssetMap().getAsset(blockTypeId);
      if (blockType != null) {
         BlockSection sectionAtY = blockChunkComponent.getSectionAtBlockY(blockPosition.y());
         int filler = sectionAtY.getFiller(blockPosition.x(), blockPosition.y(), blockPosition.z());
         int settings = 132;
         if (blockType.getConnectedBlockRuleSet() != null && filler == 0) {
            int rotationIndex = blockTypeRotation.index();
            Optional<ConnectedBlocksUtil.ConnectedBlockResult> foundPattern = getDesiredConnectedBlockType(
               chunkStore, coordinate, blockType, rotationIndex, placementNormal, true
            );
            if (foundPattern.isPresent() && (!foundPattern.get().blockTypeKey().equals(blockType.getId()) || foundPattern.get().rotationIndex != rotationIndex)
               )
             {
               ConnectedBlocksUtil.ConnectedBlockResult result = foundPattern.get();
               int id = BlockType.getAssetMap().getIndex(result.blockTypeKey());
               int rotation = result.rotationIndex();
               worldChunkComponent.setBlock(coordinate.x, coordinate.y, coordinate.z, id, BlockType.getAssetMap().getAsset(id), rotation, 0, settings);
            }
         }

         updateNeighborsWithDepth(chunkStore, worldChunkComponent, coordinate, placementNormal, settings);
      }
   }

   private static void updateNeighborsWithDepth(
      @Nonnull ChunkStore chunkStore,
      @Nonnull WorldChunk worldChunkComponent,
      @Nonnull Vector3ic startCoordinate,
      @Nonnull Vector3ic placementNormal,
      int settings
   ) {
      Store<ChunkStore> chunkComponentStore = chunkStore.getStore();

      record QueueEntry(Vector3i coordinate, int depth) {
      }

      Queue<QueueEntry> queue = new ArrayDeque<>();
      Set<Vector3i> visited = new ObjectOpenHashSet();
      queue.add(new QueueEntry(new Vector3i(startCoordinate), 0));

      label93:
      while (!queue.isEmpty()) {
         QueueEntry entry = queue.poll();
         Vector3i coordinate = entry.coordinate;
         int depth = entry.depth;
         Map<Vector3i, ConnectedBlocksUtil.ConnectedBlockResult> desiredChanges = new Object2ObjectOpenHashMap();
         notifyNeighborsAndCollectChanges(chunkStore, coordinate, desiredChanges, placementNormal);
         Iterator var12 = desiredChanges.entrySet().iterator();

         while (true) {
            Vector3i location;
            ConnectedBlocksUtil.ConnectedBlockResult connectedBlockResult;
            WorldChunk newWorldChunk;
            while (true) {
               if (!var12.hasNext()) {
                  continue label93;
               }

               Entry<Vector3i, ConnectedBlocksUtil.ConnectedBlockResult> result = (Entry<Vector3i, ConnectedBlocksUtil.ConnectedBlockResult>)var12.next();
               location = result.getKey();
               connectedBlockResult = result.getValue();
               if (visited.add(new Vector3i(location)) && (location.x != coordinate.x || location.y != coordinate.y || location.z != coordinate.z)) {
                  newWorldChunk = worldChunkComponent;
                  long chunkIndex = ChunkUtil.indexChunkFromBlock(location.x, location.z);
                  if (chunkIndex == worldChunkComponent.getIndex()) {
                     break;
                  }

                  Ref<ChunkStore> neighborRef = chunkStore.getChunkReference(chunkIndex);
                  if (neighborRef != null && neighborRef.isValid()) {
                     newWorldChunk = chunkComponentStore.getComponent(neighborRef, WorldChunk.getComponentType());
                     if (newWorldChunk != null && newWorldChunk.is(ChunkFlag.TICKING)) {
                        break;
                     }
                  }
               }
            }

            int blockId = BlockType.getAssetMap().getIndex(connectedBlockResult.blockTypeKey());
            BlockType block = BlockType.getAssetMap().getAsset(blockId);
            if (block != null) {
               newWorldChunk.setBlock(location.x, location.y, location.z, blockId, block, connectedBlockResult.rotationIndex(), 0, settings);

               for (Entry<Vector3i, ObjectIntPair<String>> additionalEntry : connectedBlockResult.getAdditionalConnectedBlocks().entrySet()) {
                  Vector3i offset = additionalEntry.getKey();
                  ObjectIntPair<String> blockData = additionalEntry.getValue();
                  Vector3i additionalLocation = new Vector3i(location).add(offset);
                  WorldChunk additionalChunk = newWorldChunk;
                  long additionalChunkIndex = ChunkUtil.indexChunkFromBlock(additionalLocation.x, additionalLocation.z);
                  if (additionalChunkIndex != newWorldChunk.getIndex()) {
                     Ref<ChunkStore> additionalRef = chunkStore.getChunkReference(additionalChunkIndex);
                     if (additionalRef == null || !additionalRef.isValid()) {
                        continue;
                     }

                     additionalChunk = chunkComponentStore.getComponent(additionalRef, WorldChunk.getComponentType());
                     if (additionalChunk == null || !additionalChunk.is(ChunkFlag.TICKING)) {
                        continue;
                     }
                  }

                  int additionalBlockId = BlockType.getAssetMap().getIndex((String)blockData.first());
                  BlockType additionalBlock = BlockType.getAssetMap().getAsset(additionalBlockId);
                  if (additionalBlock != null) {
                     additionalChunk.setBlock(
                        additionalLocation.x, additionalLocation.y, additionalLocation.z, additionalBlockId, additionalBlock, blockData.rightInt(), 0, settings
                     );
                  }
               }

               if (depth + 1 < 3) {
                  queue.add(new QueueEntry(new Vector3i(location), depth + 1));
               }
            }
         }
      }
   }

   public static void notifyNeighborsAndCollectChanges(
      @Nonnull ChunkStore chunkStore,
      @Nonnull Vector3ic origin,
      @Nonnull Map<Vector3i, ConnectedBlocksUtil.ConnectedBlockResult> desiredChanges,
      @Nonnull Vector3ic placementNormal
   ) {
      Vector3i coordinate = new Vector3i(origin);
      Store<ChunkStore> store = chunkStore.getStore();
      long chunkIndex = ChunkUtil.indexChunkFromBlock(origin.x(), origin.z());
      Ref<ChunkStore> chunkRef = chunkStore.getChunkReference(chunkIndex);
      WorldChunk chunk = resolveTickingWorldChunk(store, chunkRef);

      for (int x1 = -1; x1 <= 1; x1++) {
         for (int z1 = -1; z1 <= 1; z1++) {
            for (int y1 = -1; y1 <= 1; y1++) {
               if (x1 != 0 || y1 != 0 || z1 != 0) {
                  coordinate.set(origin).add(x1, y1, z1);
                  if (coordinate.y >= 0 && coordinate.y < 320 && !desiredChanges.containsKey(coordinate)) {
                     long neighborChunkIndex = ChunkUtil.indexChunkFromBlock(coordinate.x, coordinate.z);
                     if (neighborChunkIndex != chunkIndex) {
                        chunkIndex = neighborChunkIndex;
                        chunkRef = chunkStore.getChunkReference(neighborChunkIndex);
                        chunk = resolveTickingWorldChunk(store, chunkRef);
                     }

                     if (chunk != null && chunkRef != null) {
                        BlockChunk blockChunk = store.getComponent(chunkRef, BlockChunk.getComponentType());
                        if (blockChunk != null) {
                           BlockSection blockSection = blockChunk.getSectionAtBlockY(coordinate.y);
                           if (blockSection != null) {
                              int neighborBlockId = blockSection.get(coordinate.x, coordinate.y, coordinate.z);
                              BlockType neighborBlockType = BlockType.getAssetMap().getAsset(neighborBlockId);
                              if (neighborBlockType != null) {
                                 ConnectedBlockRuleSet ruleSet = neighborBlockType.getConnectedBlockRuleSet();
                                 if (ruleSet != null && !ruleSet.onlyUpdateOnPlacement()) {
                                    int filler = blockSection.getFiller(coordinate.x, coordinate.y, coordinate.z);
                                    int existingRotation = blockSection.getRotationIndex(coordinate.x, coordinate.y, coordinate.z);
                                    if (filler != 0) {
                                       int originX = coordinate.x - FillerBlockUtil.unpackX(filler);
                                       int originY = coordinate.y - FillerBlockUtil.unpackY(filler);
                                       int originZ = coordinate.z - FillerBlockUtil.unpackZ(filler);
                                       coordinate.set(originX, originY, originZ);
                                    }

                                    Optional<ConnectedBlocksUtil.ConnectedBlockResult> output = getDesiredConnectedBlockType(
                                       chunkStore, coordinate, neighborBlockType, existingRotation, placementNormal, false
                                    );
                                    if (output.isPresent()
                                       && (!neighborBlockType.getId().equals(output.get().blockTypeKey()) || output.get().rotationIndex != existingRotation)) {
                                       desiredChanges.put(new Vector3i(coordinate), output.get());
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
      }
   }

   @Nullable
   private static WorldChunk resolveTickingWorldChunk(@Nonnull ComponentAccessor<ChunkStore> store, @Nullable Ref<ChunkStore> chunkRef) {
      if (chunkRef != null && chunkRef.isValid()) {
         WorldChunk worldChunkComponent = store.getComponent(chunkRef, WorldChunk.getComponentType());
         return worldChunkComponent != null && worldChunkComponent.is(ChunkFlag.TICKING) ? worldChunkComponent : null;
      } else {
         return null;
      }
   }

   @Nonnull
   public static Optional<ConnectedBlocksUtil.ConnectedBlockResult> getDesiredConnectedBlockType(
      @Nonnull ChunkStore chunkStore,
      @Nonnull Vector3ic coordinate,
      @Nonnull BlockType currentBlockType,
      int currentRotation,
      @Nonnull Vector3ic placementNormal,
      boolean isPlacement
   ) {
      ConnectedBlockRuleSet ruleSet = currentBlockType.getConnectedBlockRuleSet();
      return ruleSet == null
         ? Optional.empty()
         : ruleSet.getConnectedBlockType(chunkStore, coordinate, currentBlockType, currentRotation, placementNormal, isPlacement);
   }

   public static final class ConnectedBlockResult {
      @Nonnull
      private final String blockTypeKey;
      private final int rotationIndex;
      @Nonnull
      private final Map<Vector3i, ObjectIntPair<String>> additionalConnectedBlocks = new Object2ObjectOpenHashMap();

      public ConnectedBlockResult(@Nonnull String blockTypeKey, int rotationIndex) {
         this.blockTypeKey = blockTypeKey;
         this.rotationIndex = rotationIndex;
      }

      @Nonnull
      public String blockTypeKey() {
         return this.blockTypeKey;
      }

      public int rotationIndex() {
         return this.rotationIndex;
      }

      @Nonnull
      public Map<Vector3i, ObjectIntPair<String>> getAdditionalConnectedBlocks() {
         return this.additionalConnectedBlocks;
      }

      public void addAdditionalBlock(@Nonnull Vector3i offset, @Nonnull String blockTypeKey, int rotationIndex) {
         this.additionalConnectedBlocks.put(offset, ObjectIntPair.of(blockTypeKey, rotationIndex));
      }

      @Override
      public boolean equals(@Nullable Object obj) {
         if (obj == this) {
            return true;
         } else if (obj != null && obj.getClass() == this.getClass()) {
            ConnectedBlocksUtil.ConnectedBlockResult that = (ConnectedBlocksUtil.ConnectedBlockResult)obj;
            return Objects.equals(this.blockTypeKey, that.blockTypeKey)
               && this.rotationIndex == that.rotationIndex
               && Objects.equals(this.additionalConnectedBlocks, that.additionalConnectedBlocks);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.blockTypeKey, this.rotationIndex, this.additionalConnectedBlocks);
      }

      @Override
      public String toString() {
         return "ConnectedBlockResult[blockTypeKey="
            + this.blockTypeKey
            + ", rotationIndex="
            + this.rotationIndex
            + ", additionalConnectedBlocks="
            + this.additionalConnectedBlocks
            + "]";
      }
   }
}
