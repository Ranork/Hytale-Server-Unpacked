package com.hypixel.hytale.builtin.adventure.farming.interactions;

import com.hypixel.hytale.builtin.adventure.farming.states.FarmingBlock;
import com.hypixel.hytale.builtin.adventure.farming.states.TilledSoilBlock;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
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
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FertilizeSoilInteraction extends SimpleBlockInteraction {
   @Nonnull
   public static final BuilderCodec<FertilizeSoilInteraction> CODEC = BuilderCodec.builder(
         FertilizeSoilInteraction.class, FertilizeSoilInteraction::new, SimpleBlockInteraction.CODEC
      )
      .documentation("If the target block is farmable then set it to fertilized.")
      .addField(
         new KeyedCodec<>("RefreshModifiers", Codec.STRING_ARRAY),
         (interaction, refreshModifiers) -> interaction.refreshModifiers = refreshModifiers,
         interaction -> interaction.refreshModifiers
      )
      .build();
   protected String[] refreshModifiers;

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
      int x = targetBlock.getX();
      int z = targetBlock.getZ();
      long chunkIndex = ChunkUtil.indexChunkFromBlock(x, z);
      WorldChunk worldChunkComponent = world.getChunk(chunkIndex);
      if (worldChunkComponent == null) {
         context.getState().state = InteractionState.Failed;
      } else {
         Ref<ChunkStore> blockRef = worldChunkComponent.getBlockComponentEntity(x, targetBlock.getY(), z);
         if (blockRef == null || !blockRef.isValid()) {
            blockRef = BlockModule.ensureBlockEntity(worldChunkComponent, targetBlock.x, targetBlock.y, targetBlock.z);
         }

         if (blockRef != null && blockRef.isValid()) {
            Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
            TilledSoilBlock tilledSoilComponent = chunkStore.getComponent(blockRef, TilledSoilBlock.getComponentType());
            if (tilledSoilComponent != null && !tilledSoilComponent.isFertilized()) {
               tilledSoilComponent.setFertilized(true);
               worldChunkComponent.setTicking(x, targetBlock.getY(), z, true);
               worldChunkComponent.setTicking(x, targetBlock.getY() + 1, z, true);
            } else {
               FarmingBlock farmingBlockComponent = chunkStore.getComponent(blockRef, FarmingBlock.getComponentType());
               if (farmingBlockComponent == null) {
                  context.getState().state = InteractionState.Failed;
               } else {
                  Ref<ChunkStore> soilBlockRef = worldChunkComponent.getBlockComponentEntity(x, targetBlock.getY() - 1, z);
                  if (soilBlockRef != null && soilBlockRef.isValid()) {
                     tilledSoilComponent = chunkStore.getComponent(soilBlockRef, TilledSoilBlock.getComponentType());
                     if (tilledSoilComponent != null && !tilledSoilComponent.isFertilized()) {
                        tilledSoilComponent.setFertilized(true);
                        worldChunkComponent.setTicking(x, targetBlock.getY() - 1, z, true);
                        worldChunkComponent.setTicking(x, targetBlock.getY(), z, true);
                     } else {
                        context.getState().state = InteractionState.Failed;
                     }
                  } else {
                     context.getState().state = InteractionState.Failed;
                  }
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
