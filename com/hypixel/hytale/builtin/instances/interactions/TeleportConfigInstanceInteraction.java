package com.hypixel.hytale.builtin.instances.interactions;

import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.builtin.instances.InstancesPlugin;
import com.hypixel.hytale.builtin.instances.blocks.ConfigurableInstanceBlock;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.Axis;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.teleport.PendingTeleport;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TeleportConfigInstanceInteraction extends SimpleBlockInteraction {
   @Nonnull
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   @Nonnull
   private static final Message MESSAGE_GENERAL_INTERACTION_CONFIGURE_INSTANCE_NO_INSTANCE_NAME = Message.translation(
      "server.general.interaction.configureInstance.noInstanceName"
   );
   @Nonnull
   public static final BuilderCodec<TeleportConfigInstanceInteraction> CODEC = BuilderCodec.builder(
         TeleportConfigInstanceInteraction.class, TeleportConfigInstanceInteraction::new, SimpleBlockInteraction.CODEC
      )
      .documentation(
         "Teleports the **Player** to the named instance, creating it if required.\n\nThis is configured via a UI instead of inside the interaction. This interaction just executes that set configuration."
      )
      .build();
   private static final int SET_BLOCK_SETTINGS = 256;

   @Nonnull
   @Override
   public WaitForDataFrom getWaitForDataFrom() {
      return WaitForDataFrom.Server;
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
      Ref<EntityStore> ref = context.getEntity();
      Player playerComponent = commandBuffer.getComponent(ref, Player.getComponentType());
      if (playerComponent != null && !playerComponent.isWaitingForClientReady()) {
         Archetype<EntityStore> archetype = commandBuffer.getArchetype(ref);
         if (!archetype.contains(Teleport.getComponentType()) && !archetype.contains(PendingTeleport.getComponentType())) {
            InstancesPlugin module = InstancesPlugin.get();
            Universe universe = Universe.get();
            ChunkStore chunkStore = world.getChunkStore();
            Ref<ChunkStore> chunkRef = chunkStore.getChunkReference(ChunkUtil.indexChunkFromBlock(targetBlock.x, targetBlock.z));
            if (chunkRef != null && chunkRef.isValid()) {
               BlockComponentChunk blockComponentChunk = chunkStore.getStore().getComponent(chunkRef, BlockComponentChunk.getComponentType());

               assert blockComponentChunk != null;

               Ref<ChunkStore> blockRef = blockComponentChunk.getEntityReference(ChunkUtil.indexBlockInColumn(targetBlock.x, targetBlock.y, targetBlock.z));
               if (blockRef != null && blockRef.isValid()) {
                  ConfigurableInstanceBlock configurableInstanceBlock = chunkStore.getStore()
                     .getComponent(blockRef, ConfigurableInstanceBlock.getComponentType());
                  if (configurableInstanceBlock != null) {
                     if (configurableInstanceBlock.getInstanceName() == null) {
                        playerComponent.sendMessage(MESSAGE_GENERAL_INTERACTION_CONFIGURE_INSTANCE_NO_INSTANCE_NAME);
                     } else {
                        CompletableFuture<World> targetWorldFuture = null;
                        Transform returnPoint = null;
                        World targetWorld;
                        if (configurableInstanceBlock.getInstanceKey() != null) {
                           targetWorld = universe.getWorld(configurableInstanceBlock.getInstanceKey());
                           if (targetWorld == null) {
                              returnPoint = makeReturnPoint(configurableInstanceBlock, context, commandBuffer);
                              targetWorldFuture = module.spawnInstance(
                                 configurableInstanceBlock.getInstanceName(), configurableInstanceBlock.getInstanceKey(), world, returnPoint
                              );
                           }
                        } else {
                           UUID worldUuid = configurableInstanceBlock.getWorldUUID();
                           targetWorldFuture = configurableInstanceBlock.getWorldFuture();
                           targetWorld = worldUuid != null ? universe.getWorld(worldUuid) : null;
                           if (targetWorld == null && targetWorldFuture == null) {
                              returnPoint = makeReturnPoint(configurableInstanceBlock, context, commandBuffer);
                              targetWorldFuture = module.spawnInstance(configurableInstanceBlock.getInstanceName(), world, returnPoint);
                              configurableInstanceBlock.setWorldFuture(targetWorldFuture);
                              targetWorldFuture.thenAccept(instanceWorld -> {
                                 if (blockRef.isValid()) {
                                    configurableInstanceBlock.setWorldFuture(null);
                                    configurableInstanceBlock.setWorldUUID(instanceWorld.getWorldConfig().getUuid());
                                    blockComponentChunk.markNeedsSaving();
                                 }
                              });
                           }
                        }

                        if (targetWorldFuture != null) {
                           Transform personalReturnPoint = getPersonalReturnPoint(configurableInstanceBlock, context, returnPoint, commandBuffer);
                           InstancesPlugin.teleportPlayerToLoadingInstance(ref, commandBuffer, targetWorldFuture, personalReturnPoint);
                        } else if (targetWorld != null) {
                           Transform personalReturnPoint = getPersonalReturnPoint(configurableInstanceBlock, context, returnPoint, commandBuffer);
                           InstancesPlugin.teleportPlayerToInstance(ref, commandBuffer, targetWorld, personalReturnPoint);
                        }

                        double removeBlockAfter = configurableInstanceBlock.getRemoveBlockAfter();
                        if (removeBlockAfter >= 0.0) {
                           if (removeBlockAfter == 0.0) {
                              long chunkIndex = ChunkUtil.indexChunkFromBlock(targetBlock.x, targetBlock.z);
                              WorldChunk worldChunk = world.getChunk(chunkIndex);
                              if (worldChunk != null) {
                                 worldChunk.setBlock(targetBlock.x, targetBlock.y, targetBlock.z, 0, 256);
                              } else {
                                 ((HytaleLogger.Api)LOGGER.atWarning())
                                    .log("Failed to remove block at %s,%s,%s as chunk is not loaded", targetBlock.x, targetBlock.y, targetBlock.z);
                              }
                           } else {
                              int block = world.getBlock(targetBlock);
                              new CompletableFuture()
                                 .completeOnTimeout(null, (long)(removeBlockAfter * 1.0E9), TimeUnit.NANOSECONDS)
                                 .thenRunAsync(
                                    () -> {
                                       if (world.getBlock(targetBlock) == block) {
                                          long chunkIndex = ChunkUtil.indexChunkFromBlock(targetBlock.x, targetBlock.z);
                                          WorldChunk worldChunkx = world.getChunk(chunkIndex);
                                          if (worldChunkx != null) {
                                             worldChunkx.setBlock(targetBlock.x, targetBlock.y, targetBlock.z, 0, 256);
                                          } else {
                                             ((HytaleLogger.Api)LOGGER.atWarning())
                                                .log("Failed to remove block at %s,%s,%s as chunk is not loaded", targetBlock.x, targetBlock.y, targetBlock.z);
                                          }
                                       }
                                    },
                                    world
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

   @Override
   protected void simulateInteractWithBlock(
      @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nullable ItemStack itemInHand, @Nonnull World world, @Nonnull Vector3i targetBlock
   ) {
   }

   @Nullable
   private static Transform getPersonalReturnPoint(
      @Nonnull ConfigurableInstanceBlock state,
      @Nonnull InteractionContext context,
      @Nullable Transform returnPoint,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (!state.isPersonalReturnPoint()) {
         return null;
      } else {
         return returnPoint == null ? makeReturnPoint(state, context, componentAccessor) : returnPoint;
      }
   }

   @Nonnull
   private static Transform makeReturnPoint(
      @Nonnull ConfigurableInstanceBlock state, @Nonnull InteractionContext context, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      BlockPosition targetBlock = context.getTargetBlock();
      if (targetBlock == null) {
         throw new IllegalArgumentException("Can't use OriginSource.BLOCK without a target block");
      } else {
         World world = componentAccessor.getExternalData().getWorld();
         ChunkStore chunkStore = world.getChunkStore();
         Store<ChunkStore> chunkComponentStore = chunkStore.getStore();
         long chunkIndex = ChunkUtil.indexChunkFromBlock(targetBlock.x, targetBlock.z);
         Ref<ChunkStore> chunkRef = chunkStore.getChunkReference(chunkIndex);
         if (chunkRef != null && chunkRef.isValid()) {
            BlockChunk blockChunkComponent = chunkComponentStore.getComponent(chunkRef, BlockChunk.getComponentType());
            if (blockChunkComponent == null) {
               throw new IllegalArgumentException("Block chunk component not found");
            } else {
               WorldChunk worldChunkComponent = chunkComponentStore.getComponent(chunkRef, WorldChunk.getComponentType());
               if (worldChunkComponent == null) {
                  throw new IllegalArgumentException("World chunk component not found");
               } else {
                  BlockType blockType = worldChunkComponent.getBlockType(targetBlock.x, targetBlock.y, targetBlock.z);
                  if (blockType == null) {
                     throw new IllegalArgumentException("Block type not found");
                  } else {
                     IndexedLookupTableAssetMap<String, BlockBoundingBoxes> hitboxAssetMap = BlockBoundingBoxes.getAssetMap();
                     BlockSection section = blockChunkComponent.getSectionAtBlockY(targetBlock.y);
                     int rotationIndex = section.getRotationIndex(targetBlock.x, targetBlock.y, targetBlock.z);
                     RotationTuple rotation = RotationTuple.get(rotationIndex);
                     BlockBoundingBoxes hitboxAsset = hitboxAssetMap.getAsset(blockType.getHitboxTypeIndex());
                     if (hitboxAsset == null) {
                        throw new IllegalArgumentException("Hitbox asset not found for block type: " + blockType.getId());
                     } else {
                        Box hitbox = hitboxAsset.get(rotationIndex).getBoundingBox();
                        Vector3d position = state.getPositionOffset() != null ? rotation.rotate(state.getPositionOffset()) : new Vector3d();
                        position.x = position.x + (hitbox.middleX() + targetBlock.x);
                        position.y = position.y + (hitbox.middleY() + targetBlock.y);
                        position.z = position.z + (hitbox.middleZ() + targetBlock.z);
                        Vector3f rotationOutput = Vector3f.NaN;
                        if (state.getRotation() != null) {
                           rotationOutput = state.getRotation().clone();
                           rotationOutput.addRotationOnAxis(Axis.Y, rotation.yaw().getDegrees());
                           rotationOutput.addRotationOnAxis(Axis.X, rotation.pitch().getDegrees());
                        }

                        return new Transform(position, rotationOutput);
                     }
                  }
               }
            }
         } else {
            throw new IllegalArgumentException("Chunk not loaded");
         }
      }
   }
}
