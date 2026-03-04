package com.hypixel.hytale.builtin.adventure.teleporter.interaction.server;

import com.hypixel.hytale.builtin.adventure.teleporter.component.Teleporter;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.PendingTeleport;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectList;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TeleporterInteraction extends SimpleBlockInteraction {
   @Nonnull
   public static final BuilderCodec<TeleporterInteraction> CODEC = BuilderCodec.builder(
         TeleporterInteraction.class, TeleporterInteraction::new, SimpleBlockInteraction.CODEC
      )
      .appendInherited(
         new KeyedCodec<>("Particle", Codec.STRING),
         (interaction, s) -> interaction.particle = s,
         interaction -> interaction.particle,
         (interaction, parent) -> interaction.particle = parent.particle
      )
      .documentation("The particle to play on the entity when teleporting.")
      .add()
      .<Double>appendInherited(
         new KeyedCodec<>("ClearOutXZ", Codec.DOUBLE),
         (interaction, s) -> interaction.clearoutXZ = s,
         interaction -> interaction.clearoutXZ,
         (interaction, parent) -> interaction.clearoutXZ = parent.clearoutXZ
      )
      .addValidator(Validators.greaterThanOrEqual(0.0))
      .documentation("Upon reaching the warp destination, how far away one has to move on the XZ plane in order to use another Teleporter.")
      .add()
      .<Double>appendInherited(
         new KeyedCodec<>("ClearOutY", Codec.DOUBLE),
         (interaction, s) -> interaction.clearoutY = s,
         interaction -> interaction.clearoutY,
         (interaction, parent) -> interaction.clearoutY = parent.clearoutY
      )
      .addValidator(Validators.greaterThanOrEqual(0.0))
      .documentation("Upon reaching the warp destination, how far away one has to move along the Y axis in order to use another Teleporter.")
      .add()
      .build();
   @Nullable
   private String particle;
   private double clearoutXZ = 1.3;
   private double clearoutY = 2.5;

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
      boolean result = this.interact(world, commandBuffer, context, targetBlock);
      if (!result) {
         context.getState().state = InteractionState.Failed;
      }
   }

   private boolean interact(
      @Nonnull World world, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull InteractionContext context, @Nonnull Vector3i targetBlock
   ) {
      ChunkStore chunkStore = world.getChunkStore();
      long chunkIndex = ChunkUtil.indexChunkFromBlock(targetBlock.getX(), targetBlock.getZ());
      BlockComponentChunk blockComponentChunk = chunkStore.getChunkComponent(chunkIndex, BlockComponentChunk.getComponentType());
      if (blockComponentChunk == null) {
         return false;
      } else {
         int blockIndex = ChunkUtil.indexBlockInColumn(targetBlock.x, targetBlock.y, targetBlock.z);
         Ref<ChunkStore> blockRef = blockComponentChunk.getEntityReference(blockIndex);
         if (blockRef != null && blockRef.isValid()) {
            BlockModule.BlockStateInfo blockStateInfoComponent = blockRef.getStore().getComponent(blockRef, BlockModule.BlockStateInfo.getComponentType());
            if (blockStateInfoComponent == null) {
               return false;
            } else {
               Ref<ChunkStore> chunkRef = blockStateInfoComponent.getChunkRef();
               if (!chunkRef.isValid()) {
                  return false;
               } else {
                  Teleporter teleporter = chunkStore.getStore().getComponent(blockRef, Teleporter.getComponentType());
                  if (teleporter == null) {
                     return false;
                  } else {
                     Ref<EntityStore> ref = context.getEntity();
                     Player playerComponent = commandBuffer.getComponent(ref, Player.getComponentType());
                     if (playerComponent != null && playerComponent.isWaitingForClientReady()) {
                        return false;
                     } else {
                        Archetype<EntityStore> archetype = commandBuffer.getArchetype(ref);
                        if (archetype.contains(Teleport.getComponentType()) || archetype.contains(PendingTeleport.getComponentType())) {
                           return false;
                        } else if (archetype.contains(UsedTeleporter.getComponentType())) {
                           return false;
                        } else {
                           WorldChunk worldChunkComponent = chunkRef.getStore().getComponent(chunkRef, WorldChunk.getComponentType());
                           if (worldChunkComponent == null) {
                              return false;
                           } else {
                              BlockType blockType = worldChunkComponent.getBlockType(targetBlock.x, targetBlock.y, targetBlock.z);
                              if (blockType == null) {
                                 return false;
                              } else {
                                 if (!teleporter.isValid()) {
                                    String currentState = blockType.getStateForBlock(blockType);
                                    if (!"default".equals(currentState)) {
                                       BlockType variantBlockType = blockType.getBlockForState("default");
                                       if (variantBlockType != null) {
                                          worldChunkComponent.setBlockInteractionState(
                                             targetBlock.x, targetBlock.y, targetBlock.z, variantBlockType, "default", true
                                          );
                                       }
                                    }
                                 }

                                 TransformComponent transformComponent = commandBuffer.getComponent(ref, TransformComponent.getComponentType());
                                 if (transformComponent == null) {
                                    return false;
                                 } else {
                                    Teleport teleportComponent = teleporter.toTeleport(
                                       transformComponent.getPosition(), transformComponent.getRotation(), targetBlock
                                    );
                                    if (teleportComponent == null) {
                                       return false;
                                    } else {
                                       commandBuffer.addComponent(ref, Teleport.getComponentType(), teleportComponent);
                                       commandBuffer.addComponent(
                                          ref,
                                          UsedTeleporter.getComponentType(),
                                          new UsedTeleporter(teleporter.getWorldUuid(), teleportComponent.getPosition(), this.clearoutXZ, this.clearoutY)
                                       );
                                       if (this.particle != null) {
                                          Vector3d particlePosition = transformComponent.getPosition();
                                          SpatialResource<Ref<EntityStore>, EntityStore> playerSpatialResource = commandBuffer.getResource(
                                             EntityModule.get().getPlayerSpatialResourceType()
                                          );
                                          ObjectList<Ref<EntityStore>> results = SpatialResource.getThreadLocalReferenceList();
                                          playerSpatialResource.getSpatialStructure().collect(particlePosition, 75.0, results);
                                          ParticleUtil.spawnParticleEffect(this.particle, particlePosition, results, commandBuffer);
                                       }

                                       return true;
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         } else {
            return false;
         }
      }
   }

   @Override
   protected void simulateInteractWithBlock(
      @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nullable ItemStack itemInHand, @Nonnull World world, @Nonnull Vector3i targetBlock
   ) {
   }
}
