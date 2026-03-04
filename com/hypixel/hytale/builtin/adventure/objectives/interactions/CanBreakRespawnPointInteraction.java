package com.hypixel.hytale.builtin.adventure.objectives.interactions;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.meta.state.RespawnBlock;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CanBreakRespawnPointInteraction extends SimpleBlockInteraction {
   @Nonnull
   public static final BuilderCodec<CanBreakRespawnPointInteraction> CODEC = BuilderCodec.builder(
         CanBreakRespawnPointInteraction.class, CanBreakRespawnPointInteraction::new, SimpleBlockInteraction.CODEC
      )
      .build();

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
      ChunkStore chunkStore = world.getChunkStore();
      Ref<ChunkStore> chunk = chunkStore.getChunkReference(ChunkUtil.indexChunkFromBlock(targetBlock.x, targetBlock.z));
      if (chunk == null) {
         context.getState().state = InteractionState.Failed;
      } else {
         BlockComponentChunk blockComp = chunkStore.getStore().getComponent(chunk, BlockComponentChunk.getComponentType());
         if (blockComp == null) {
            context.getState().state = InteractionState.Failed;
         } else {
            Ref<ChunkStore> blockEntity = blockComp.getEntityReference(ChunkUtil.indexBlockInColumn(targetBlock.x, targetBlock.y, targetBlock.z));
            if (blockEntity == null) {
               context.getState().state = InteractionState.Finished;
            } else {
               RespawnBlock respawnState = chunkStore.getStore().getComponent(blockEntity, RespawnBlock.getComponentType());
               if (respawnState == null) {
                  context.getState().state = InteractionState.Finished;
               } else {
                  UUIDComponent uuidComponent = commandBuffer.getComponent(context.getOwningEntity(), UUIDComponent.getComponentType());
                  if (uuidComponent == null) {
                     context.getState().state = InteractionState.Failed;
                  } else {
                     UUID ownerUUID = respawnState.getOwnerUUID();
                     if (ownerUUID != null && !uuidComponent.getUuid().equals(ownerUUID)) {
                        context.getState().state = InteractionState.Failed;
                     } else {
                        context.getState().state = InteractionState.Finished;
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
}
