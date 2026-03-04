package com.hypixel.hytale.server.core.modules.interaction.interaction.config.server;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.util.TrigMathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.accessor.ChunkAccessor;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
   protected void interactWithBlock(
      @Nonnull World world,
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      @Nonnull InteractionType type,
      @Nonnull InteractionContext context,
      @Nullable ItemStack itemInHand,
      @Nonnull Vector3i targetBlock,
      @Nonnull CooldownHandler cooldownHandler
   ) {
      WorldChunk chunk = world.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(targetBlock.x, targetBlock.z));
      if (chunk != null) {
         BlockType blockType = chunk.getBlockType(targetBlock);
         int rotation = chunk.getRotationIndex(targetBlock.x, targetBlock.y, targetBlock.z);
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

         DoorInteraction.DoorState checkResult = this.checkDoor(world, targetBlock, blockType, rotation, doorState, newDoorState);
         if (checkResult == null) {
            context.getState().state = InteractionState.Failed;
         } else {
            DoorInteraction.DoorState stateDoubleDoor = getOppositeDoorState(doorState);
            BlockType interactionBlockState = activateDoor(world, blockType, targetBlock, doorState, checkResult);
            boolean doubleDoor = this.checkForDoubleDoor(world, targetBlock, blockType, rotation, checkResult, stateDoubleDoor);
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

               pos.add(targetBlock);
               SoundUtil.playSoundEvent3d(ref, interactionBlockState.getInteractionSoundEventIndex(), pos, commandBuffer);
            }
         }
      }
   }

   @Override
   protected void simulateInteractWithBlock(
      @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nullable ItemStack itemInHand, @Nonnull World world, @Nonnull Vector3i targetBlock
   ) {
   }

   private boolean checkForDoubleDoor(
      @Nonnull World world,
      @Nonnull Vector3i blockPosition,
      @Nonnull BlockType blockType,
      int rotation,
      @Nonnull DoorInteraction.DoorState fromState,
      @Nonnull DoorInteraction.DoorState doorStateToCheck
   ) {
      DoorInteraction.DoorInfo doorToOpen = getDoubleDoor(world, blockPosition, blockType, rotation, doorStateToCheck);
      if (doorToOpen == null) {
         return false;
      } else {
         boolean otherDoorIsHorizontal = isHorizontalDoor(doorToOpen.blockType);
         DoorInteraction.DoorState stateForDoubleDoor = otherDoorIsHorizontal ? fromState : getOppositeDoorState(fromState);
         activateDoor(world, doorToOpen.blockType, doorToOpen.blockPosition, doorToOpen.doorState, stateForDoubleDoor);
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
      @Nonnull ChunkAccessor<WorldChunk> chunkAccessor,
      @Nonnull Vector3i blockPosition,
      @Nonnull BlockType blockType,
      int rotation,
      @Nonnull DoorInteraction.DoorState oldDoorState,
      @Nonnull DoorInteraction.DoorState newDoorState
   ) {
      DoorInteraction.DoorInfo doubleDoor = getDoubleDoor(chunkAccessor, blockPosition, blockType, rotation, oldDoorState);
      DoorInteraction.DoorState newOppositeDoorState = getOppositeDoorState(newDoorState);
      String newOppositeDoorInteractionState = getInteractionState(oldDoorState, newOppositeDoorState);
      String newDoorInteractionState = getInteractionState(oldDoorState, newDoorState);
      if (canOpenDoor(chunkAccessor, blockPosition, newDoorInteractionState)) {
         if (this.horizontal || doubleDoor == null || canOpenDoor(chunkAccessor, doubleDoor.blockPosition, newOppositeDoorInteractionState)) {
            return newDoorState;
         } else if (canOpenDoor(chunkAccessor, blockPosition, newOppositeDoorInteractionState)
            && canOpenDoor(chunkAccessor, doubleDoor.blockPosition, newDoorInteractionState)) {
            return newOppositeDoorState;
         } else {
            chunkAccessor.setBlockInteractionState(blockPosition, blockType, "DoorBlocked");
            return null;
         }
      } else if (canOpenDoor(chunkAccessor, blockPosition, newOppositeDoorInteractionState) && !this.horizontal) {
         if (doubleDoor != null && !canOpenDoor(chunkAccessor, doubleDoor.blockPosition, newDoorInteractionState)) {
            chunkAccessor.setBlockInteractionState(blockPosition, blockType, "DoorBlocked");
            return null;
         } else {
            return newOppositeDoorState;
         }
      } else {
         if (newDoorState != DoorInteraction.DoorState.CLOSED) {
            chunkAccessor.setBlockInteractionState(blockPosition, blockType, "DoorBlocked");
         }

         return null;
      }
   }

   @Nullable
   private static BlockType activateDoor(
      @Nonnull World world,
      @Nonnull BlockType blockType,
      @Nonnull Vector3i blockPosition,
      @Nonnull DoorInteraction.DoorState fromState,
      @Nonnull DoorInteraction.DoorState doorState
   ) {
      WorldChunk chunk = world.getChunk(ChunkUtil.indexChunkFromBlock(blockPosition.x, blockPosition.z));
      int rotationIndex = chunk.getRotationIndex(blockPosition.x, blockPosition.y, blockPosition.z);
      BlockBoundingBoxes oldHitbox = BlockBoundingBoxes.getAssetMap().getAsset(blockType.getHitboxTypeIndex());
      String interactionStateToSend = getInteractionState(fromState, doorState);
      world.setBlockInteractionState(blockPosition, blockType, interactionStateToSend);
      BlockType currentBlockType = world.getBlockType(blockPosition);
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
   }

   @Nullable
   private static DoorInteraction.DoorInfo getDoubleDoor(
      @Nonnull ChunkAccessor<WorldChunk> chunkAccessor,
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
               Vector3i blockPosition = worldPosition.clone().add(MathUtil.rotateVectorYAxis(offset, rotationToCheck.getDegrees(), false));
               DoorInteraction.DoorInfo matchingDoor = getDoorAtPosition(
                  chunkAccessor, blockPosition.x, blockPosition.y, blockPosition.z, rotationToCheck.flip()
               );
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
   public static DoorInteraction.DoorInfo getDoorAtPosition(
      @Nonnull ChunkAccessor<WorldChunk> chunkAccessor, int x, int y, int z, @Nonnull Rotation rotationToCheck
   ) {
      WorldChunk chunk = chunkAccessor.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(x, z));
      if (chunk == null) {
         return null;
      } else {
         BlockType blockType = chunk.getBlockType(x, y, z);
         if (blockType != null && blockType.isDoor()) {
            RotationTuple blockRotation = chunk.getRotation(x, y, z);
            String blockState = blockType.getStateForBlock(blockType);
            DoorInteraction.DoorState doorState = DoorInteraction.DoorState.fromBlockState(blockState);
            Rotation doorRotation = blockRotation.yaw();
            int filler = chunk.getFiller(x, y, z);
            return doorRotation != rotationToCheck ? null : new DoorInteraction.DoorInfo(blockType, filler, new Vector3i(x, y, z), doorState);
         } else {
            return null;
         }
      }
   }

   private static boolean canOpenDoor(@Nonnull ChunkAccessor<WorldChunk> chunkAccessor, @Nonnull Vector3i blockPosition, @Nonnull String state) {
      WorldChunk chunk = chunkAccessor.getChunk(ChunkUtil.indexChunkFromBlock(blockPosition.x, blockPosition.z));
      if (chunk == null) {
         return false;
      } else {
         int blockId = chunk.getBlock(blockPosition.x, blockPosition.y, blockPosition.z);
         BlockType originalBlockType = BlockType.getAssetMap().getAsset(blockId);
         if (originalBlockType == null) {
            return false;
         } else {
            BlockType variantBlockType = originalBlockType.getBlockForState(state);
            if (variantBlockType == null) {
               return false;
            } else {
               int rotation = chunk.getRotationIndex(blockPosition.x, blockPosition.y, blockPosition.z);
               return chunkAccessor.testPlaceBlock(
                  blockPosition.x, blockPosition.y, blockPosition.z, variantBlockType, rotation, (blockX, blockY, blockZ, blockType, _rotation, filler) -> {
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
   }

   private static boolean isInFrontOfDoor(@Nonnull Vector3i blockPosition, @Nullable Rotation doorRotationYaw, @Nonnull Vector3d playerPosition) {
      double doorRotationRad = Math.toRadians(doorRotationYaw != null ? doorRotationYaw.getDegrees() : 0.0);
      Vector3d doorRotationVector = new Vector3d(TrigMathUtil.sin(doorRotationRad), 0.0, TrigMathUtil.cos(doorRotationRad));
      Vector3d direction = Vector3d.directionTo(blockPosition, playerPosition);
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

      public DoorInfo(BlockType blockType, int filler, Vector3i blockPosition, DoorInteraction.DoorState doorState) {
         this.blockType = blockType;
         this.filler = filler;
         this.blockPosition = blockPosition;
         this.doorState = doorState;
      }

      public BlockType getBlockType() {
         return this.blockType;
      }

      public Vector3i getBlockPosition() {
         return this.blockPosition;
      }

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
