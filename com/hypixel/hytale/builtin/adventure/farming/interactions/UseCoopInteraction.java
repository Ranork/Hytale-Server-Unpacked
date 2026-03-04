package com.hypixel.hytale.builtin.adventure.farming.interactions;

import com.hypixel.hytale.builtin.adventure.farming.states.CoopBlock;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UseCoopInteraction extends SimpleBlockInteraction {
   @Nonnull
   public static final BuilderCodec<UseCoopInteraction> CODEC = BuilderCodec.builder(
         UseCoopInteraction.class, UseCoopInteraction::new, SimpleBlockInteraction.CODEC
      )
      .build();

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
      int x = targetBlock.getX();
      int z = targetBlock.getZ();
      long chunkIndex = ChunkUtil.indexChunkFromBlock(x, z);
      WorldChunk worldChunk = world.getChunk(chunkIndex);
      if (worldChunk == null) {
         context.getState().state = InteractionState.Failed;
      } else {
         Ref<ChunkStore> blockRef = worldChunk.getBlockComponentEntity(x, targetBlock.getY(), z);
         if (blockRef == null || !blockRef.isValid()) {
            blockRef = BlockModule.ensureBlockEntity(worldChunk, targetBlock.x, targetBlock.y, targetBlock.z);
         }

         if (blockRef != null && blockRef.isValid()) {
            Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
            CoopBlock coopBlockComponent = chunkStore.getComponent(blockRef, CoopBlock.getComponentType());
            if (coopBlockComponent == null) {
               context.getState().state = InteractionState.Failed;
            } else {
               Ref<EntityStore> ref = context.getEntity();
               if (EntityUtils.getEntity(ref, commandBuffer) instanceof LivingEntity livingEntity) {
                  CombinedItemContainer inventoryContainer = livingEntity.getInventory().getCombinedHotbarFirst();
                  if (inventoryContainer != null) {
                     coopBlockComponent.gatherProduceFromContainer(inventoryContainer);
                     BlockType currentBlockType = worldChunk.getBlockType(targetBlock);

                     assert currentBlockType != null;

                     worldChunk.setBlockInteractionState(targetBlock, currentBlockType, coopBlockComponent.hasProduce() ? "Produce_Ready" : "default");
                  }
               } else {
                  context.getState().state = InteractionState.Failed;
               }
            }
         } else {
            context.getState().state = InteractionState.Failed;
         }
      }
   }

   @Override
   protected void simulateInteractWithBlock(
      @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nullable ItemStack itemInHand, @Nonnull World world, @Nonnull Vector3i targetBlock
   ) {
   }
}
