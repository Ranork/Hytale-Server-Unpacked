package com.hypixel.hytale.builtin.adventure.objectives.interactions;

import com.hypixel.hytale.builtin.adventure.objectives.blockstates.TreasureChestBlock;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DestroyTreasureConditionInteraction extends SimpleBlockInteraction {
   public static final BuilderCodec<DestroyTreasureConditionInteraction> CODEC = BuilderCodec.builder(
         DestroyTreasureConditionInteraction.class, DestroyTreasureConditionInteraction::new, SimpleBlockInteraction.CODEC
      )
      .documentation("Checks if the target treasure block is destroyable")
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
      @Nonnull Vector3i pos,
      @Nonnull CooldownHandler cooldownHandler
   ) {
      Ref<EntityStore> ref = context.getEntity();
      Player playerComponent = commandBuffer.getComponent(ref, Player.getComponentType());
      if (playerComponent != null) {
         ChunkStore chunkStore = world.getChunkStore();
         Ref<ChunkStore> chunkRef = chunkStore.getChunkReference(ChunkUtil.indexChunkFromBlock(pos.x, pos.z));
         if (chunkRef != null) {
            Store<ChunkStore> chunkEntityStore = chunkStore.getStore();
            BlockComponentChunk blockComponentChunk = chunkEntityStore.getComponent(chunkRef, BlockComponentChunk.getComponentType());
            if (blockComponentChunk != null) {
               Ref<ChunkStore> blockRef = blockComponentChunk.getEntityReference(ChunkUtil.indexBlockInColumn(pos.x, pos.y, pos.z));
               if (blockRef != null) {
                  TreasureChestBlock treasureBlock = chunkEntityStore.getComponent(blockRef, TreasureChestBlock.getComponentType());
                  if (treasureBlock != null) {
                     if (treasureBlock.canDestroy(ref, commandBuffer)) {
                        context.getState().state = InteractionState.Finished;
                     } else {
                        context.getState().state = InteractionState.Failed;
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
