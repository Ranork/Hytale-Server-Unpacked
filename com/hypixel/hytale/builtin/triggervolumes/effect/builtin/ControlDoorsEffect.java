package com.hypixel.hytale.builtin.triggervolumes.effect.builtin;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.builtin.triggervolumes.manager.VolumeEntry;
import com.hypixel.hytale.builtin.triggervolumes.shape.TriggerVolumeShape;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.block.BlockUtil;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.util.TrigMathUtil;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.DoorInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.accessor.ChunkAccessor;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3i;

public class ControlDoorsEffect extends TriggerEffect {
   private static final String OPEN_DOOR_IN = "OpenDoorIn";
   private static final String OPEN_DOOR_OUT = "OpenDoorOut";
   private static final String CLOSE_DOOR_IN = "CloseDoorIn";
   private static final String CLOSE_DOOR_OUT = "CloseDoorOut";
   @Nonnull
   public static final BuilderCodec<ControlDoorsEffect> CODEC = BuilderCodec.builder(ControlDoorsEffect.class, ControlDoorsEffect::new, BASE_CODEC)
      .append(new KeyedCodec<>("Action", new EnumCodec<>(ControlDoorsEffect.DoorAction.class)), (e, v) -> e.action = v, e -> e.action)
      .add()
      .build();
   @Nonnull
   private ControlDoorsEffect.DoorAction action = ControlDoorsEffect.DoorAction.CLOSE;

   @Override
   public void execute(@Nonnull TriggerContext context) {
      Store<EntityStore> store = context.getStore();
      World world = store.getExternalData().getWorld();
      if (world != null) {
         TransformComponent triggerTransform = store.getComponent(context.getEntityRef(), TransformComponent.getComponentType());
         Vector3d triggerPos = triggerTransform != null ? triggerTransform.getPosition() : new Vector3d(context.getVolume().getPosition());
         Vector3d min = new Vector3d();
         Vector3d max = new Vector3d();
         LongOpenHashSet processedBlocks = new LongOpenHashSet();

         for (VolumeEntry volume : context.getSpatialVolumes()) {
            TriggerVolumeShape shape = volume.getShape();
            Vector3d origin = volume.getPosition();
            shape.getWorldAABB(origin, min, max);
            int minX = MathUtil.floor(min.x());
            int minY = MathUtil.floor(min.y());
            int minZ = MathUtil.floor(min.z());
            int maxX = MathUtil.floor(max.x());
            int maxY = MathUtil.floor(max.y());
            int maxZ = MathUtil.floor(max.z());

            for (int x = minX; x <= maxX; x++) {
               for (int y = minY; y <= maxY; y++) {
                  for (int z = minZ; z <= maxZ; z++) {
                     WorldChunk chunk = world.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(x, z));
                     if (chunk != null) {
                        BlockType blockType = chunk.getBlockType(x, y, z);
                        if (blockType != null && blockType.isDoor()) {
                           Vector3i anchor = doorAnchorForCell(world, x, y, z);
                           if (processedBlocks.add(BlockUtil.pack(anchor.x, anchor.y, anchor.z))) {
                              WorldChunk chunkAtAnchor = world.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(anchor.x, anchor.z));
                              if (chunkAtAnchor != null) {
                                 BlockType typeAtAnchor = chunkAtAnchor.getBlockType(anchor.x, anchor.y, anchor.z);
                                 if (typeAtAnchor != null && typeAtAnchor.isDoor()) {
                                    this.applyDoorState(world, chunkAtAnchor, typeAtAnchor, anchor.x, anchor.y, anchor.z, triggerPos);
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

   @Nonnull
   private static Vector3i doorAnchorForCell(@Nonnull World world, int x, int y, int z) {
      WorldChunk chunk = world.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(x, z));
      if (chunk == null) {
         return new Vector3i(x, y, z);
      } else {
         int filler = chunk.getFiller(x, y, z);
         return filler == 0
            ? new Vector3i(x, y, z)
            : new Vector3i(x - FillerBlockUtil.unpackX(filler), y - FillerBlockUtil.unpackY(filler), z - FillerBlockUtil.unpackZ(filler));
      }
   }

   private void applyDoorState(@Nonnull World world, @Nonnull WorldChunk chunk, @Nonnull BlockType blockType, int x, int y, int z, @Nonnull Vector3d triggerPos) {
      Vector3i pos = new Vector3i(x, y, z);
      String blockState = blockType.getStateForBlock(blockType);
      ControlDoorsEffect.DoorState doorState = ControlDoorsEffect.DoorState.fromBlockState(blockState);
      if (this.action == ControlDoorsEffect.DoorAction.OPEN) {
         if (doorState != ControlDoorsEffect.DoorState.CLOSED) {
            return;
         }

         ControlDoorsEffect.DoorState preferred;
         if (isHorizontalDoor(blockType)) {
            preferred = ControlDoorsEffect.DoorState.OPENED_IN;
         } else {
            int rotation = chunk.getRotationIndex(x, y, z);
            Rotation yaw = RotationTuple.get(rotation).yaw();
            preferred = isInFrontOfDoor(pos, yaw, triggerPos) ? ControlDoorsEffect.DoorState.OPENED_OUT : ControlDoorsEffect.DoorState.OPENED_IN;
         }

         ControlDoorsEffect.DoorState alternate = getOppositeOpenState(preferred);
         tryOpen(world, pos, blockType, preferred);
         WorldChunk chunkAfter = world.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(x, z));
         if (chunkAfter == null) {
            return;
         }

         BlockType updatedType = chunkAfter.getBlockType(x, y, z);
         if (updatedType == null) {
            return;
         }

         if (ControlDoorsEffect.DoorState.fromBlockState(updatedType.getStateForBlock(updatedType)) == ControlDoorsEffect.DoorState.CLOSED) {
            tryOpen(world, pos, updatedType, alternate);
         }
      } else {
         if (doorState == ControlDoorsEffect.DoorState.CLOSED) {
            return;
         }

         String closeInteraction = getInteractionState(doorState, ControlDoorsEffect.DoorState.CLOSED);
         world.setBlockInteractionState(pos, blockType, closeInteraction);
      }
   }

   private static void tryOpen(@Nonnull World world, @Nonnull Vector3i pos, @Nonnull BlockType blockType, @Nonnull ControlDoorsEffect.DoorState targetOpen) {
      String interaction = getInteractionState(ControlDoorsEffect.DoorState.CLOSED, targetOpen);
      if (canOpenDoor(world, pos, interaction)) {
         world.setBlockInteractionState(pos, blockType, interaction);
      }
   }

   @Nonnull
   private static ControlDoorsEffect.DoorState getOppositeOpenState(@Nonnull ControlDoorsEffect.DoorState open) {
      return open == ControlDoorsEffect.DoorState.OPENED_IN ? ControlDoorsEffect.DoorState.OPENED_OUT : ControlDoorsEffect.DoorState.OPENED_IN;
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
                  return doorInteraction.getIsHorizontal();
               }
            }

            return false;
         }
      }
   }

   private static boolean isInFrontOfDoor(@Nonnull Vector3i blockPosition, @Nullable Rotation doorRotationYaw, @Nonnull Vector3d entityPosition) {
      double doorRotationRad = Math.toRadians(doorRotationYaw != null ? doorRotationYaw.getDegrees() : 0.0);
      Vector3d doorRotationVector = new Vector3d(TrigMathUtil.sin(doorRotationRad), 0.0, TrigMathUtil.cos(doorRotationRad));
      Vector3d direction = Vector3dUtil.directionTo(new Vector3d(blockPosition).add(0.5, 0.5, 0.5), entityPosition);
      return direction.dot(doorRotationVector) < 0.0;
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

   @Nonnull
   private static String getInteractionState(@Nonnull ControlDoorsEffect.DoorState fromState, @Nonnull ControlDoorsEffect.DoorState doorState) {
      if (doorState == ControlDoorsEffect.DoorState.CLOSED && fromState == ControlDoorsEffect.DoorState.OPENED_IN) {
         return "CloseDoorOut";
      } else if (doorState == ControlDoorsEffect.DoorState.CLOSED && fromState == ControlDoorsEffect.DoorState.OPENED_OUT) {
         return "CloseDoorIn";
      } else {
         return doorState == ControlDoorsEffect.DoorState.OPENED_IN ? "OpenDoorOut" : "OpenDoorIn";
      }
   }

   public static enum DoorAction {
      OPEN,
      CLOSE;
   }

   private static enum DoorState {
      CLOSED,
      OPENED_IN,
      OPENED_OUT;

      @Nonnull
      static ControlDoorsEffect.DoorState fromBlockState(@Nullable String state) {
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
