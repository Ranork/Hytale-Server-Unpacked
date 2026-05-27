package com.hypixel.hytale.server.core.modules.interaction.interaction.config.server;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.util.TrigMathUtil;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockGathering;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.PhysicsDropType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.SoftBlockDropType;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.modules.interaction.BlockHarvestUtils;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3i;

public class DoorInteraction extends SimpleBlockInteraction {
   private static final String OPEN_DOOR_IN = "OpenDoorIn";
   private static final String OPEN_DOOR_OUT = "OpenDoorOut";
   private static final String CLOSE_DOOR_IN = "CloseDoorIn";
   private static final String CLOSE_DOOR_OUT = "CloseDoorOut";
   private static final String DOOR_BLOCKED = "DoorBlocked";
   @Nonnull
   public static final BuilderCodec<DoorInteraction> CODEC = BuilderCodec.builder(DoorInteraction.class, DoorInteraction::new, SimpleBlockInteraction.CODEC)
      .documentation("Opens/Closes a door")
      .<Boolean>appendInherited(
         new KeyedCodec<>("Horizontal", Codec.BOOLEAN), (t, i) -> t.horizontal = i, t -> t.horizontal, (t, parent) -> t.horizontal = parent.horizontal
      )
      .documentation("Whether the door is horizontal (e.g. gates) or vertical (e.g. regular doors).")
      .add()
      .build();
   private boolean horizontal;

   @Override
   protected void simulateInteractWithBlock(
      @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nullable ItemStack itemInHand, @Nonnull World world, @Nonnull Vector3i targetBlock
   ) {
   }

   @Override
   protected void interactWithBlock(
      @Nonnull World world,
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      @Nonnull InteractionType type,
      @Nonnull InteractionContext context,
      @Nullable ItemStack itemInHand,
      @Nonnull Vector3i targetBlock,
      @Nonnull CooldownHandler cooldownHandler
   ) {
      int x = targetBlock.x();
      int y = targetBlock.y();
      int z = targetBlock.z();
      ChunkStore chunkStore = world.getChunkStore();
      long chunkIndex = ChunkUtil.indexChunkFromBlock(x, z);
      Ref<ChunkStore> chunkReference = chunkStore.getChunkReference(chunkIndex);
      if (chunkReference != null && chunkReference.isValid()) {
         Store<ChunkStore> chunkComponentStore = chunkStore.getStore();
         WorldChunk worldChunkComponent = chunkComponentStore.getComponent(chunkReference, WorldChunk.getComponentType());

         assert worldChunkComponent != null;

         BlockChunk blockChunkComponent = chunkComponentStore.getComponent(chunkReference, BlockChunk.getComponentType());

         assert blockChunkComponent != null;

         BlockType blockType = worldChunkComponent.getBlockType(targetBlock);
         if (blockType != null) {
            int rotation = blockChunkComponent.getSectionAtBlockY(y).getRotationIndex(x, y, z);
            RotationTuple rotationTuple = RotationTuple.get(rotation);
            String blockState = blockType.getStateForBlock(blockType);
            DoorInteraction.DoorState doorState = DoorInteraction.DoorState.fromBlockState(blockState);
            Ref<EntityStore> ref = context.getEntity();
            TransformComponent transformComponent = commandBuffer.getComponent(ref, TransformComponent.getComponentType());

            assert transformComponent != null;

            Vector3d entityPosition = transformComponent.getPosition();
            DoorInteraction.DoorState newDoorState;
            if (doorState != DoorInteraction.DoorState.CLOSED) {
               newDoorState = DoorInteraction.DoorState.CLOSED;
            } else if (!this.horizontal && isInFrontOfDoor(targetBlock, rotationTuple.yaw(), entityPosition)) {
               newDoorState = DoorInteraction.DoorState.OPENED_OUT;
            } else {
               newDoorState = DoorInteraction.DoorState.OPENED_IN;
            }

            DoorInteraction.DoorState checkResult = this.checkDoor(chunkStore, worldChunkComponent, targetBlock, blockType, rotation, doorState, newDoorState);
            if (checkResult == null) {
               context.getState().state = InteractionState.Failed;
            } else {
               DoorInteraction.DoorState stateDoubleDoor = getOppositeDoorState(doorState);
               BlockType interactionBlockState = activateDoor(world, commandBuffer, chunkStore, blockType, targetBlock, doorState, checkResult);
               boolean doubleDoor = checkForDoubleDoor(world, commandBuffer, chunkStore, targetBlock, blockType, rotation, checkResult, stateDoubleDoor);
               if (interactionBlockState != null) {
                  Vector3d pos = new Vector3d();
                  int hitboxTypeIndex = BlockType.getAssetMap().getAsset(blockType.getItem().getId()).getHitboxTypeIndex();
                  BlockBoundingBoxes blockBoundingBoxes = BlockBoundingBoxes.getAssetMap().getAsset(hitboxTypeIndex);
                  BlockBoundingBoxes.RotatedVariantBoxes rotatedBoxes = blockBoundingBoxes.get(rotation);
                  Box hitbox = rotatedBoxes.getBoundingBox();
                  if (doubleDoor) {
                     Vector3d offset = new Vector3d(hitbox.middleX(), 0.0, 0.0);
                     Rotation rotationToCheck = RotationTuple.get(rotation).yaw();
                     pos.add(MathUtil.rotateVectorYAxis(offset, rotationToCheck.getDegrees(), false));
                     pos.add(hitbox.middleX(), hitbox.middleY(), hitbox.middleZ());
                  } else {
                     pos.add(hitbox.middleX(), hitbox.middleY(), hitbox.middleZ());
                  }

                  pos.add(targetBlock.x, targetBlock.y, targetBlock.z);
                  SoundUtil.playSoundEvent3d(ref, interactionBlockState.getInteractionSoundEventIndex(), pos, commandBuffer);
               }
            }
         }
      }
   }

   public boolean getIsHorizontal() {
      return this.horizontal;
   }

   private static boolean checkForDoubleDoor(
      @Nonnull World world,
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      @Nonnull ChunkStore chunkStore,
      @Nonnull Vector3i blockPosition,
      @Nonnull BlockType blockType,
      int rotation,
      @Nonnull DoorInteraction.DoorState fromState,
      @Nonnull DoorInteraction.DoorState doorStateToCheck
   ) {
      DoorInteraction.DoorInfo doorToOpen = getDoubleDoor(chunkStore, blockPosition, blockType, rotation, doorStateToCheck);
      if (doorToOpen == null) {
         return false;
      } else {
         boolean otherDoorIsHorizontal = isHorizontalDoor(doorToOpen.blockType);
         DoorInteraction.DoorState stateForDoubleDoor = otherDoorIsHorizontal ? fromState : getOppositeDoorState(fromState);
         activateDoor(world, commandBuffer, chunkStore, doorToOpen.blockType, doorToOpen.blockPosition, doorToOpen.doorState, stateForDoubleDoor);
         return true;
      }
   }

   private static boolean isHorizontalDoor(@Nonnull BlockType blockType) {
      String rootInteractionId = blockType.getInteractions().get(InteractionType.Use);
      if (rootInteractionId == null) {
         return false;
      } else {
         RootInteraction rootInteraction = RootInteraction.getAssetMap().getAsset(rootInteractionId);
         if (rootInteraction == null) {
            return false;
         } else {
            for (String interactionId : rootInteraction.getInteractionIds()) {
               Interaction interaction = Interaction.getAssetMap().getAsset(interactionId);
               if (interaction instanceof DoorInteraction doorInteraction) {
                  return doorInteraction.horizontal;
               }
            }

            return false;
         }
      }
   }

   @Nullable
   private DoorInteraction.DoorState checkDoor(
      @Nonnull ChunkStore chunkStore,
      @Nonnull WorldChunk worldChunkComponent,
      @Nonnull Vector3i blockPosition,
      @Nonnull BlockType blockType,
      int rotation,
      @Nonnull DoorInteraction.DoorState oldDoorState,
      @Nonnull DoorInteraction.DoorState newDoorState
   ) {
      DoorInteraction.DoorInfo doubleDoor = getDoubleDoor(chunkStore, blockPosition, blockType, rotation, oldDoorState);
      DoorInteraction.DoorState newOppositeDoorState = getOppositeDoorState(newDoorState);
      String newOppositeDoorInteractionState = getInteractionState(oldDoorState, newOppositeDoorState);
      String newDoorInteractionState = getInteractionState(oldDoorState, newDoorState);
      if (canOpenDoor(chunkStore, blockPosition, newDoorInteractionState)) {
         if (this.horizontal || doubleDoor == null || canOpenDoor(chunkStore, doubleDoor.blockPosition, newOppositeDoorInteractionState)) {
            return newDoorState;
         } else if (canOpenDoor(chunkStore, blockPosition, newOppositeDoorInteractionState)
            && canOpenDoor(chunkStore, doubleDoor.blockPosition, newDoorInteractionState)) {
            return newOppositeDoorState;
         } else {
            worldChunkComponent.setBlockInteractionState(blockPosition, blockType, "DoorBlocked");
            return null;
         }
      } else if (canOpenDoor(chunkStore, blockPosition, newOppositeDoorInteractionState) && !this.horizontal) {
         if (doubleDoor != null && !canOpenDoor(chunkStore, doubleDoor.blockPosition, newDoorInteractionState)) {
            worldChunkComponent.setBlockInteractionState(blockPosition, blockType, "DoorBlocked");
            return null;
         } else {
            return newOppositeDoorState;
         }
      } else {
         if (newDoorState != DoorInteraction.DoorState.CLOSED) {
            worldChunkComponent.setBlockInteractionState(blockPosition, blockType, "DoorBlocked");
         }

         return null;
      }
   }

   @Nullable
   private static BlockType activateDoor(
      @Nonnull World world,
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      @Nonnull ChunkStore chunkStore,
      @Nonnull BlockType blockType,
      @Nonnull Vector3i blockPosition,
      @Nonnull DoorInteraction.DoorState fromState,
      @Nonnull DoorInteraction.DoorState doorState
   ) {
      long chunkIndex = ChunkUtil.indexChunkFromBlock(blockPosition.x, blockPosition.z);
      Ref<ChunkStore> chunkReference = chunkStore.getChunkReference(chunkIndex);
      if (chunkReference != null && chunkReference.isValid()) {
         Store<ChunkStore> chunkComponentStore = chunkStore.getStore();
         WorldChunk worldChunkComponent = chunkComponentStore.getComponent(chunkReference, WorldChunk.getComponentType());

         assert worldChunkComponent != null;

         BlockChunk blockChunkComponent = chunkComponentStore.getComponent(chunkReference, BlockChunk.getComponentType());

         assert blockChunkComponent != null;

         int rotationIndex = blockChunkComponent.getSectionAtBlockY(blockPosition.y).getRotationIndex(blockPosition.x, blockPosition.y, blockPosition.z);
         BlockBoundingBoxes oldHitbox = BlockBoundingBoxes.getAssetMap().getAsset(blockType.getHitboxTypeIndex());
         String interactionStateToSend = getInteractionState(fromState, doorState);
         BlockType newState = blockType.getBlockForState(interactionStateToSend);
         if (newState != null) {
            BlockBoundingBoxes newHitbox = BlockBoundingBoxes.getAssetMap().getAsset(newState.getHitboxTypeIndex());
            if (newHitbox != null) {
               breakSoftBlocksInHitbox(commandBuffer, chunkStore, blockPosition, newHitbox.get(rotationIndex));
            }
         }

         worldChunkComponent.setBlockInteractionState(blockPosition, blockType, interactionStateToSend);
         BlockType currentBlockType = worldChunkComponent.getBlockType(blockPosition);
         if (currentBlockType == null) {
            return null;
         } else {
            BlockType newBlockType = currentBlockType.getBlockForState(interactionStateToSend);
            if (oldHitbox != null) {
               FillerBlockUtil.forEachFillerBlock(
                  oldHitbox.get(rotationIndex), (x, y, z) -> world.performBlockUpdate(blockPosition.x + x, blockPosition.y + y, blockPosition.z + z)
               );
            }

            if (newBlockType != null) {
               BlockBoundingBoxes newHitbox = BlockBoundingBoxes.getAssetMap().getAsset(newBlockType.getHitboxTypeIndex());
               if (newHitbox != null && newHitbox != oldHitbox) {
                  FillerBlockUtil.forEachFillerBlock(
                     newHitbox.get(rotationIndex), (x, y, z) -> world.performBlockUpdate(blockPosition.x + x, blockPosition.y + y, blockPosition.z + z)
                  );
               }
            }

            return newBlockType;
         }
      } else {
         return null;
      }
   }

   private static void breakSoftBlocksInHitbox(
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      @Nonnull ChunkStore chunkStore,
      @Nonnull Vector3i basePosition,
      @Nonnull BlockBoundingBoxes.RotatedVariantBoxes hitbox
   ) {
      Store<ChunkStore> chunkComponentStore = chunkStore.getStore();
      FillerBlockUtil.forEachFillerBlock(hitbox, (x, y, z) -> {
         if (x != 0 || y != 0 || z != 0) {
            int worldX = basePosition.x + x;
            int worldY = basePosition.y + y;
            int worldZ = basePosition.z + z;
            long chunkIndex = ChunkUtil.indexChunkFromBlock(worldX, worldZ);
            Ref<ChunkStore> chunkReference = chunkStore.getChunkReference(chunkIndex);
            if (chunkReference != null && chunkReference.isValid()) {
               WorldChunk existingChunk = chunkComponentStore.getComponent(chunkReference, WorldChunk.getComponentType());
               if (existingChunk != null) {
                  BlockType existingType = existingChunk.getBlockType(worldX, worldY, worldZ);
                  if (existingType != null && existingType != BlockType.EMPTY && existingType.getMaterial() == BlockMaterial.Empty) {
                     int settings = 1312;
                     existingChunk.breakBlock(worldX, worldY, worldZ, 1312);
                     String itemId = null;
                     String dropListId = null;
                     BlockGathering gathering = existingType.getGathering();
                     if (gathering != null) {
                        PhysicsDropType physics = gathering.getPhysics();
                        SoftBlockDropType soft = gathering.getSoft();
                        if (physics != null) {
                           itemId = physics.getItemId();
                           dropListId = physics.getDropListId();
                        } else if (soft != null) {
                           itemId = soft.getItemId();
                           dropListId = soft.getDropListId();
                        }
                     }

                     List<ItemStack> itemStacks = BlockHarvestUtils.getDrops(existingType, 1, itemId, dropListId);
                     if (!itemStacks.isEmpty()) {
                        Vector3d dropPosition = new Vector3d(worldX + 0.5, worldY, worldZ + 0.5);
                        Holder<EntityStore>[] itemEntityHolders = ItemComponent.generateItemDrops(commandBuffer, itemStacks, dropPosition, Rotation3f.IDENTITY);
                        commandBuffer.addEntities(itemEntityHolders, AddReason.SPAWN);
                     }
                  }
               }
            }
         }
      });
   }

   @Nullable
   private static DoorInteraction.DoorInfo getDoubleDoor(
      @Nonnull ChunkStore chunkStore,
      @Nonnull Vector3i worldPosition,
      @Nonnull BlockType blockType,
      int rotation,
      @Nonnull DoorInteraction.DoorState doorStateToCheck
   ) {
      Item blockTypeItem = blockType.getItem();
      if (blockTypeItem == null) {
         return null;
      } else {
         BlockType blockTypeItemAsset = BlockType.getAssetMap().getAsset(blockTypeItem.getId());
         if (blockTypeItemAsset == null) {
            return null;
         } else {
            int hitboxTypeIndex = blockTypeItemAsset.getHitboxTypeIndex();
            BlockBoundingBoxes blockBoundingBoxes = BlockBoundingBoxes.getAssetMap().getAsset(hitboxTypeIndex);
            if (blockBoundingBoxes == null) {
               return null;
            } else {
               BlockBoundingBoxes.RotatedVariantBoxes baseBoxes = blockBoundingBoxes.get(Rotation.None, Rotation.None, Rotation.None);
               Vector3i offset = new Vector3i((int)baseBoxes.getBoundingBox().getMax().x * 2 - 1, 0, 0);
               Rotation rotationToCheck = RotationTuple.get(rotation).yaw();
               Vector3i blockPosition = new Vector3i(worldPosition).add(MathUtil.rotateVectorYAxis(offset, rotationToCheck.getDegrees(), false));
               DoorInteraction.DoorInfo matchingDoor = getDoorAtPosition(chunkStore, blockPosition.x, blockPosition.y, blockPosition.z, rotationToCheck.flip());
               if (matchingDoor != null && matchingDoor.doorState == doorStateToCheck) {
                  BlockType matchingBlockType = matchingDoor.blockType;
                  if (matchingDoor.filler != 0) {
                     return null;
                  } else {
                     int matchingDoorHitboxIndex = BlockType.getAssetMap().getAsset(matchingBlockType.getItem().getId()).getHitboxTypeIndex();
                     return matchingDoorHitboxIndex == hitboxTypeIndex ? matchingDoor : null;
                  }
               } else {
                  return null;
               }
            }
         }
      }
   }

   @Nullable
   public static DoorInteraction.DoorInfo getDoorAtPosition(@Nonnull ChunkStore chunkStore, int x, int y, int z, @Nonnull Rotation rotationToCheck) {
      long chunkIndex = ChunkUtil.indexChunkFromBlock(x, z);
      Ref<ChunkStore> chunkReference = chunkStore.getChunkReference(chunkIndex);
      if (chunkReference != null && chunkReference.isValid()) {
         Store<ChunkStore> chunkComponentStore = chunkStore.getStore();
         WorldChunk worldChunkComponent = chunkComponentStore.getComponent(chunkReference, WorldChunk.getComponentType());
         if (worldChunkComponent == null) {
            return null;
         } else {
            BlockType blockType = worldChunkComponent.getBlockType(x, y, z);
            if (blockType != null && blockType.isDoor()) {
               BlockChunk blockChunkComponent = chunkComponentStore.getComponent(chunkReference, BlockChunk.getComponentType());

               assert blockChunkComponent != null;

               BlockSection blockSection = blockChunkComponent.getSectionAtBlockY(y);
               RotationTuple blockRotation = blockSection.getRotation(x, y, z);
               String blockState = blockType.getStateForBlock(blockType);
               DoorInteraction.DoorState doorState = DoorInteraction.DoorState.fromBlockState(blockState);
               Rotation doorRotation = blockRotation.yaw();
               int filler = blockSection.getFiller(x, y, z);
               return doorRotation != rotationToCheck ? null : new DoorInteraction.DoorInfo(blockType, filler, new Vector3i(x, y, z), doorState);
            } else {
               return null;
            }
         }
      } else {
         return null;
      }
   }

   private static boolean canOpenDoor(@Nonnull ChunkStore chunkStore, @Nonnull Vector3i blockPosition, @Nonnull String state) {
      long chunkIndex = ChunkUtil.indexChunkFromBlock(blockPosition.x, blockPosition.z);
      Ref<ChunkStore> chunkReference = chunkStore.getChunkReference(chunkIndex);
      if (chunkReference != null && chunkReference.isValid()) {
         Store<ChunkStore> chunkComponentStore = chunkStore.getStore();
         WorldChunk worldChunkComponent = chunkComponentStore.getComponent(chunkReference, WorldChunk.getComponentType());
         if (worldChunkComponent == null) {
            return false;
         } else {
            int blockId = worldChunkComponent.getBlock(blockPosition.x, blockPosition.y, blockPosition.z);
            BlockType originalBlockType = BlockType.getAssetMap().getAsset(blockId);
            if (originalBlockType == null) {
               return false;
            } else {
               BlockType variantBlockType = originalBlockType.getBlockForState(state);
               if (variantBlockType == null) {
                  return false;
               } else {
                  BlockChunk blockChunkComponent = chunkComponentStore.getComponent(chunkReference, BlockChunk.getComponentType());

                  assert blockChunkComponent != null;

                  int rotation = blockChunkComponent.getSectionAtBlockY(blockPosition.y).getRotationIndex(blockPosition.x, blockPosition.y, blockPosition.z);
                  return worldChunkComponent.testPlaceBlock(
                     blockPosition.x, blockPosition.y, blockPosition.z, variantBlockType, rotation, (blockX, blockY, blockZ, var4, var5x, filler) -> {
                        if (filler != 0) {
                           blockX -= FillerBlockUtil.unpackX(filler);
                           blockY -= FillerBlockUtil.unpackY(filler);
                           blockZ -= FillerBlockUtil.unpackZ(filler);
                        }

                        return blockX == blockPosition.x && blockY == blockPosition.y && blockZ == blockPosition.z;
                     }
                  );
               }
            }
         }
      } else {
         return false;
      }
   }

   private static boolean isInFrontOfDoor(@Nonnull Vector3i blockPosition, @Nullable Rotation doorRotationYaw, @Nonnull Vector3d playerPosition) {
      double doorRotationRad = Math.toRadians(doorRotationYaw != null ? doorRotationYaw.getDegrees() : 0.0);
      Vector3d doorRotationVector = new Vector3d(TrigMathUtil.sin(doorRotationRad), 0.0, TrigMathUtil.cos(doorRotationRad));
      Vector3d direction = Vector3dUtil.directionTo(new Vector3d(blockPosition).add(0.5, 0.5, 0.5), playerPosition);
      return direction.dot(doorRotationVector) < 0.0;
   }

   @Nonnull
   private static String getInteractionState(@Nonnull DoorInteraction.DoorState fromState, @Nonnull DoorInteraction.DoorState doorState) {
      String stateToSend;
      if (doorState == DoorInteraction.DoorState.CLOSED && fromState == DoorInteraction.DoorState.OPENED_IN) {
         stateToSend = "CloseDoorOut";
      } else if (doorState == DoorInteraction.DoorState.CLOSED && fromState == DoorInteraction.DoorState.OPENED_OUT) {
         stateToSend = "CloseDoorIn";
      } else if (doorState == DoorInteraction.DoorState.OPENED_IN) {
         stateToSend = "OpenDoorOut";
      } else {
         stateToSend = "OpenDoorIn";
      }

      return stateToSend;
   }

   @Nonnull
   private static DoorInteraction.DoorState getOppositeDoorState(@Nonnull DoorInteraction.DoorState doorState) {
      return doorState == DoorInteraction.DoorState.OPENED_OUT
         ? DoorInteraction.DoorState.OPENED_IN
         : (doorState == DoorInteraction.DoorState.OPENED_IN ? DoorInteraction.DoorState.OPENED_OUT : DoorInteraction.DoorState.CLOSED);
   }

   public static class DoorInfo {
      private final BlockType blockType;
      private final int filler;
      private final Vector3i blockPosition;
      private final DoorInteraction.DoorState doorState;

      public DoorInfo(@Nonnull BlockType blockType, int filler, @Nonnull Vector3i blockPosition, @Nonnull DoorInteraction.DoorState doorState) {
         this.blockType = blockType;
         this.filler = filler;
         this.blockPosition = blockPosition;
         this.doorState = doorState;
      }

      @Nonnull
      public BlockType getBlockType() {
         return this.blockType;
      }

      @Nonnull
      public Vector3i getBlockPosition() {
         return this.blockPosition;
      }

      @Nonnull
      public DoorInteraction.DoorState getDoorState() {
         return this.doorState;
      }
   }

   private static enum DoorState {
      CLOSED,
      OPENED_IN,
      OPENED_OUT;

      @Nonnull
      public static DoorInteraction.DoorState fromBlockState(@Nullable String state) {
         if (state == null) {
            return CLOSED;
         } else {
            return switch (state) {
               case "OpenDoorOut" -> OPENED_IN;
               case "OpenDoorIn" -> OPENED_OUT;
               default -> CLOSED;
            };
         }
      }
   }
}
