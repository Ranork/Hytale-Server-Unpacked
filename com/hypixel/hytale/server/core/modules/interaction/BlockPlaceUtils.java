package com.hypixel.hytale.server.core.modules.interaction;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.protocol.BlockRotation;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockGathering;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockPlacementSettings;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.SoftBlockDropType;
import com.hypixel.hytale.server.core.asset.type.buildertool.config.PrefabListAsset;
import com.hypixel.hytale.server.core.asset.type.environment.config.Environment;
import com.hypixel.hytale.server.core.asset.type.gameplay.WorldConfig;
import com.hypixel.hytale.server.core.blocktype.component.BlockPhysics;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackSlotTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.modules.interaction.components.PlacedByInteractionComponent;
import com.hypixel.hytale.server.core.prefab.selection.buffer.PrefabBufferUtil;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.PrefabBuffer;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkColumn;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.connectedblocks.ConnectedBlocksUtil;
import com.hypixel.hytale.server.core.universe.world.meta.BlockState;
import com.hypixel.hytale.server.core.universe.world.meta.state.PlacedByBlockState;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import com.hypixel.hytale.server.core.util.PrefabUtil;
import com.hypixel.hytale.sneakythrow.SneakyThrow;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonDocument;
import org.bson.BsonValue;

public class BlockPlaceUtils {
   @Nonnull
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   @Nonnull
   private static final Message MESSAGE_MODULES_INTERACTION_FAILED_ADD_BACK_AFTER_FAILED_PLACE = Message.translation(
      "server.modules.interaction.failedAddBackAfterFailedPlace"
   );
   @Nonnull
   private static final Message MESSAGE_MODULES_INTERACTION_FAILED_CHECK_BLOCK = Message.translation("server.modules.interaction.failedCheckBlock");
   @Nonnull
   private static final Message MESSAGE_MODULES_INTERACTION_FAILED_CHECK_EMPTY = Message.translation("server.modules.interaction.failedCheckEmpty");
   @Nonnull
   private static final Message MESSAGE_MODULES_INTERACTION_FAILED_CHECK_UNKNOWN = Message.translation("server.modules.interaction.failedCheckUnknown");
   @Nonnull
   private static final Message MESSAGE_MODULES_INTERACTION_FAILED_CHECK = Message.translation("server.modules.interaction.failedCheck");
   @Nonnull
   private static final Message MESSAGE_MODULES_INTERACTION_BUILD_FORBIDDEN = Message.translation("server.modules.interaction.buildForbidden");

   public static void placeBlock(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull ItemStack itemStack,
      @Nullable String blockTypeKey,
      @Nonnull ItemContainer itemContainer,
      @Nonnull Vector3i placementNormal,
      @Nonnull Vector3i blockPosition,
      @Nonnull BlockRotation blockRotation,
      @Nullable Inventory inventory,
      byte activeSlot,
      boolean removeItemInHand,
      @Nonnull Ref<ChunkStore> chunkReference,
      @Nonnull ComponentAccessor<ChunkStore> chunkStore,
      @Nonnull ComponentAccessor<EntityStore> entityStore
   ) {
      if (blockPosition.getY() >= 0 && blockPosition.getY() < 320) {
         Ref<ChunkStore> targetChunkReference = chunkReference;
         RotationTuple targetRotation = RotationTuple.of(
            Rotation.valueOf(blockRotation.rotationYaw), Rotation.valueOf(blockRotation.rotationPitch), Rotation.valueOf(blockRotation.rotationRoll)
         );
         BlockChunk targetBlockChunkComponent = chunkStore.getComponent(chunkReference, BlockChunk.getComponentType());

         assert targetBlockChunkComponent != null;

         BlockSection targetBlockSection = targetBlockChunkComponent.getSectionAtBlockY(blockPosition.getY());
         PlaceBlockEvent event = new PlaceBlockEvent(itemStack, blockPosition, targetRotation);
         entityStore.invoke(ref, event);
         if (event.isCancelled()) {
            targetBlockSection.invalidateBlock(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
         } else {
            Vector3i targetBlockPosition = event.getTargetBlock();
            targetRotation = event.getRotation();
            boolean positionIsDifferent = !ChunkUtil.isSameChunk(targetBlockPosition.x, targetBlockPosition.z, blockPosition.x, blockPosition.z);
            if (positionIsDifferent) {
               long chunkIndex = ChunkUtil.indexChunkFromBlock(targetBlockPosition.x, targetBlockPosition.z);
               targetChunkReference = chunkStore.getExternalData().getChunkReference(chunkIndex);
               if (targetChunkReference == null || !targetChunkReference.isValid()) {
                  return;
               }
            }

            if (positionIsDifferent) {
               targetBlockSection.invalidateBlock(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
            }

            if (!targetChunkReference.equals(chunkReference) || targetBlockPosition.y != blockPosition.y) {
               targetBlockChunkComponent = chunkStore.getComponent(targetChunkReference, BlockChunk.getComponentType());

               assert targetBlockChunkComponent != null;

               targetBlockSection = targetBlockChunkComponent.getSectionAtBlockY(targetBlockPosition.getY());
            }

            PlayerRef playerRefComponent = entityStore.getComponent(ref, PlayerRef.getComponentType());
            Player playerComponent = entityStore.getComponent(ref, Player.getComponentType());
            boolean isAdventureMode = playerComponent == null || playerComponent.getGameMode() == GameMode.Adventure;
            if (isAdventureMode && removeItemInHand) {
               ItemStackSlotTransaction transaction = itemContainer.removeItemStackFromSlot(activeSlot, itemStack, 1);
               if (!transaction.succeeded()) {
                  if (playerRefComponent != null) {
                     playerRefComponent.sendMessage(MESSAGE_MODULES_INTERACTION_FAILED_CHECK);
                  }

                  return;
               }

               itemStack = transaction.getOutput();
            }

            if (blockTypeKey == null && itemStack != null) {
               blockTypeKey = itemStack.getBlockKey();
            }

            if (validateBlockToPlace(blockTypeKey, playerRefComponent)) {
               assert blockTypeKey != null;

               BlockType blockTypeAsset = BlockType.getAssetMap().getAsset(blockTypeKey);
               if (blockTypeAsset != null) {
                  String prefabListAssetId = blockTypeAsset.getPrefabListAssetId();
                  if (prefabListAssetId != null && !validateAndPlacePrefab(blockPosition, prefabListAssetId, playerRefComponent, entityStore)) {
                     return;
                  }
               }

               WorldChunk worldChunkComponent = chunkStore.getComponent(targetChunkReference, WorldChunk.getComponentType());
               if (worldChunkComponent != null) {
                  boolean success = tryPlaceBlock(
                     ref,
                     placementNormal,
                     targetBlockPosition,
                     blockTypeKey,
                     targetRotation,
                     worldChunkComponent,
                     targetBlockChunkComponent,
                     chunkReference,
                     chunkStore,
                     entityStore
                  );
                  if (success) {
                     onPlaceBlockSuccess(itemStack, worldChunkComponent, targetBlockPosition);
                  } else {
                     onPlaceBlockFailure(itemStack, inventory, activeSlot, playerComponent, targetBlockSection, targetBlockPosition);
                  }
               }
            }
         }
      }
   }

   private static void onPlaceBlockFailure(
      @Nullable ItemStack itemStack,
      @Nullable Inventory inventory,
      byte activeSlot,
      @Nullable Player playerComponent,
      @Nonnull BlockSection blockSection,
      @Nonnull Vector3i blockPosition
   ) {
      boolean isAdventure = playerComponent == null || playerComponent.getGameMode() == GameMode.Adventure;
      if (inventory != null && itemStack != null && isAdventure) {
         ItemContainer hotbar = inventory.getHotbar();
         ItemStackSlotTransaction transaction = hotbar.addItemStackToSlot(activeSlot, itemStack);
         if (!transaction.succeeded()) {
            ItemStackTransaction itemStackTransaction = hotbar.addItemStack(itemStack);
            if (!itemStackTransaction.succeeded() && playerComponent != null) {
               playerComponent.sendMessage(MESSAGE_MODULES_INTERACTION_FAILED_ADD_BACK_AFTER_FAILED_PLACE);
            }

            return;
         }
      }

      blockSection.invalidateBlock(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
   }

   private static void onPlaceBlockSuccess(@Nullable ItemStack itemStack, @Nonnull WorldChunk worldChunkComponent, @Nonnull Vector3i blockPosition) {
      if (itemStack != null) {
         BsonDocument metadata = itemStack.getMetadata();
         if (metadata != null) {
            BsonValue bsonValue = metadata.get("BlockState");
            if (bsonValue != null) {
               try {
                  BsonDocument document = bsonValue.asDocument();
                  BlockState blockState = BlockState.load(document, worldChunkComponent, blockPosition.clone());
                  if (blockState != null) {
                     worldChunkComponent.setState(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ(), blockState);
                  } else {
                     LOGGER.at(Level.WARNING).log("Failed to set BlockState from item metadata: %s, %s", itemStack.getItemId(), document);
                  }
               } catch (Exception var7) {
                  throw SneakyThrow.sneakyThrow(var7);
               }
            }
         }
      }
   }

   private static boolean validateBlockToPlace(@Nullable String blockTypeKey, @Nullable PlayerRef playerRefComponent) {
      if (blockTypeKey == null) {
         if (playerRefComponent != null) {
            playerRefComponent.sendMessage(MESSAGE_MODULES_INTERACTION_FAILED_CHECK_BLOCK);
         }

         return false;
      } else if (blockTypeKey.equals("Empty")) {
         if (playerRefComponent != null) {
            playerRefComponent.sendMessage(MESSAGE_MODULES_INTERACTION_FAILED_CHECK_EMPTY);
         }

         return false;
      } else if (blockTypeKey.equals("Unknown")) {
         if (playerRefComponent != null) {
            playerRefComponent.sendMessage(MESSAGE_MODULES_INTERACTION_FAILED_CHECK_UNKNOWN);
         }

         return false;
      } else {
         return true;
      }
   }

   private static boolean validateAndPlacePrefab(
      @Nonnull Vector3i blockPosition,
      @Nonnull String prefabListAssetId,
      @Nullable PlayerRef playerRefComponent,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      PrefabListAsset prefabListAsset = PrefabListAsset.getAssetMap().getAsset(prefabListAssetId);
      if (prefabListAsset == null) {
         if (playerRefComponent != null) {
            playerRefComponent.sendMessage(Message.translation("server.modules.interaction.placeBlock.prefabListNotFound").param("name", prefabListAssetId));
         }

         return false;
      } else {
         Path randomPrefab = prefabListAsset.getRandomPrefab();
         if (randomPrefab == null) {
            if (playerRefComponent != null) {
               playerRefComponent.sendMessage(Message.translation("server.modules.interaction.placeBlock.prefabListEmpty").param("name", prefabListAssetId));
            }

            return false;
         } else if (!Files.exists(randomPrefab)) {
            if (playerRefComponent != null) {
               playerRefComponent.sendMessage(Message.translation("server.commands.editprefab.prefabNotFound").param("name", randomPrefab.toString()));
            }

            return false;
         } else {
            World world = componentAccessor.getExternalData().getWorld();
            PrefabBuffer prefabBuffer = PrefabBufferUtil.loadBuffer(randomPrefab);
            world.execute(() -> {
               Store<EntityStore> store = world.getEntityStore().getStore();
               PrefabBuffer.PrefabBufferAccessor prefabBufferAccessor = prefabBuffer.newAccess();
               PrefabUtil.paste(prefabBufferAccessor, world, blockPosition, Rotation.None, true, new Random(), store);
               prefabBufferAccessor.release();
            });
            return true;
         }
      }
   }

   private static boolean tryPlaceBlock(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Vector3i placementNormal,
      @Nonnull Vector3i blockPosition,
      @Nonnull String blockTypeKey,
      @Nonnull RotationTuple rotation,
      @Nonnull WorldChunk worldChunkComponent,
      @Nonnull BlockChunk blockChunkComponent,
      @Nonnull Ref<ChunkStore> chunkReference,
      @Nonnull ComponentAccessor<ChunkStore> chunkStore,
      @Nonnull ComponentAccessor<EntityStore> entityStore
   ) {
      WorldConfig worldConfig = entityStore.getExternalData().getWorld().getGameplayConfig().getWorldConfig();
      if (!worldConfig.isBlockPlacementAllowed()) {
         return false;
      } else {
         Player playerComponent = entityStore.getComponent(ref, Player.getComponentType());
         PlayerRef playerRefComponent = entityStore.getComponent(ref, PlayerRef.getComponentType());
         boolean isAdventure = playerComponent == null || playerComponent.getGameMode() == GameMode.Adventure;
         if (isAdventure) {
            int environmentId = blockChunkComponent.getEnvironment(blockPosition);
            Environment environment = Environment.getAssetMap().getAsset(environmentId);
            if (environment != null && !environment.isBlockModificationAllowed()) {
               if (playerRefComponent != null) {
                  playerRefComponent.sendMessage(MESSAGE_MODULES_INTERACTION_BUILD_FORBIDDEN);
               }

               return false;
            }
         }

         BlockType blockType = BlockType.getAssetMap().getAsset(blockTypeKey);
         int rotationIndex = rotation.index();
         if (blockType != null
            && worldChunkComponent.testPlaceBlock(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ(), blockType, rotationIndex)) {
            BlockBoundingBoxes hitBoxType = BlockBoundingBoxes.getAssetMap().getAsset(blockType.getHitboxTypeIndex());
            if (hitBoxType != null) {
               FillerBlockUtil.forEachFillerBlock(
                  hitBoxType.get(rotationIndex),
                  (x1, y1, z1) -> breakAndDropReplacedBlock(
                     blockPosition.clone().add(x1, y1, z1), worldChunkComponent, chunkReference, ref, chunkStore, entityStore
                  )
               );
            } else {
               breakAndDropReplacedBlock(blockPosition, worldChunkComponent, chunkReference, ref, chunkStore, entityStore);
            }

            int placeBlockSettings = 10;
            if (!worldChunkComponent.placeBlock(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ(), blockTypeKey, rotation, 10, false)) {
               return false;
            } else {
               if (playerComponent != null && !playerComponent.isOverrideBlockPlacementRestrictions() && blockType.canBePlacedAsDeco()) {
                  ChunkColumn chunkColumnComponent = chunkStore.getComponent(chunkReference, ChunkColumn.getComponentType());

                  assert chunkColumnComponent != null;

                  Ref<ChunkStore> sectionRef = chunkColumnComponent.getSection(ChunkUtil.chunkCoordinate(blockPosition.y));
                  if (sectionRef != null && sectionRef.isValid()) {
                     BlockPhysics.markDeco(chunkStore, sectionRef, blockPosition.x, blockPosition.y, blockPosition.z);
                  }
               }

               BlockState blockState = worldChunkComponent.getState(blockPosition.x, blockPosition.y, blockPosition.z);
               if (blockState instanceof PlacedByBlockState placedByBlockState) {
                  placedByBlockState.placedBy(ref, blockTypeKey, blockState, entityStore);
               }

               int blockIndexInChunk = ChunkUtil.indexBlockInColumn(blockPosition.x, blockPosition.y, blockPosition.z);
               BlockComponentChunk blockComponentChunk = worldChunkComponent.getBlockComponentChunk();
               Ref<ChunkStore> blockRef = blockComponentChunk == null ? null : blockComponentChunk.getEntityReference(blockIndexInChunk);
               if (blockRef != null) {
                  UUIDComponent uuidComponent = entityStore.getComponent(ref, UUIDComponent.getComponentType());

                  assert uuidComponent != null;

                  PlacedByInteractionComponent placedByInteractionComponent = new PlacedByInteractionComponent(uuidComponent.getUuid());
                  chunkStore.putComponent(blockRef, PlacedByInteractionComponent.getComponentType(), placedByInteractionComponent);
               }

               ConnectedBlocksUtil.setConnectedBlockAndNotifyNeighbors(
                  BlockType.getAssetMap().getIndex(blockTypeKey), rotation, placementNormal, blockPosition, worldChunkComponent, blockChunkComponent
               );
               return true;
            }
         } else {
            return false;
         }
      }
   }

   private static void breakAndDropReplacedBlock(
      @Nonnull Vector3i blockPosition,
      @Nonnull WorldChunk worldChunkComponent,
      @Nonnull Ref<ChunkStore> chunkReference,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull ComponentAccessor<ChunkStore> chunkStore,
      @Nonnull ComponentAccessor<EntityStore> entityStore
   ) {
      BlockType existingBlock = worldChunkComponent.getBlockType(blockPosition);
      if (existingBlock != null) {
         if (existingBlock.getMaterial() != BlockMaterial.Empty) {
            return;
         }

         BlockGathering gathering = existingBlock.getGathering();
         int dropQuantity = 1;
         String itemId = null;
         String dropListId = null;
         if (gathering != null) {
            SoftBlockDropType softGathering = gathering.getSoft();
            if (softGathering != null) {
               itemId = softGathering.getItemId();
               dropListId = softGathering.getDropListId();
            }
         }

         int setBlockSettings = 288;
         BlockHarvestUtils.performBlockBreak(
            chunkStore.getExternalData().getWorld(),
            blockPosition,
            existingBlock,
            null,
            dropQuantity,
            itemId,
            dropListId,
            288,
            ref,
            chunkReference,
            entityStore,
            chunkStore
         );
      }
   }

   public static boolean canPlaceBlock(@Nonnull BlockType blockType, @Nonnull String placedBlockTypeKey) {
      if (blockType.getId().equals(placedBlockTypeKey)) {
         return true;
      } else {
         BlockPlacementSettings placementSettings = blockType.getPlacementSettings();
         return placementSettings == null
            ? false
            : placedBlockTypeKey.equals(placementSettings.getWallPlacementOverrideBlockId())
               || placedBlockTypeKey.equals(placementSettings.getFloorPlacementOverrideBlockId())
               || placedBlockTypeKey.equals(placementSettings.getCeilingPlacementOverrideBlockId());
      }
   }
}
