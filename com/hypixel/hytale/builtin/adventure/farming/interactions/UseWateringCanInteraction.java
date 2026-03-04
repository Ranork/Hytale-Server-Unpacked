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
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UseWateringCanInteraction extends SimpleBlockInteraction {
   @Nonnull
   public static final BuilderCodec<UseWateringCanInteraction> CODEC = BuilderCodec.builder(
         UseWateringCanInteraction.class, UseWateringCanInteraction::new, SimpleBlockInteraction.CODEC
      )
      .documentation("Waters the target farmable block.")
      .addField(new KeyedCodec<>("Duration", Codec.LONG), (interaction, duration) -> interaction.duration = duration, interaction -> interaction.duration)
      .addField(
         new KeyedCodec<>("RefreshModifiers", Codec.STRING_ARRAY),
         (interaction, refreshModifiers) -> interaction.refreshModifiers = refreshModifiers,
         interaction -> interaction.refreshModifiers
      )
      .build();
   protected long duration;
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
      WorldChunk worldChunk = world.getChunk(chunkIndex);
      if (worldChunk == null) {
         context.getState().state = InteractionState.Failed;
      } else {
         Ref<ChunkStore> blockRef = worldChunk.getBlockComponentEntity(x, targetBlock.getY(), z);
         if (blockRef == null) {
            blockRef = BlockModule.ensureBlockEntity(worldChunk, targetBlock.x, targetBlock.y, targetBlock.z);
         }

         if (blockRef != null && blockRef.isValid()) {
            Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
            WorldTimeResource worldTimeResource = commandBuffer.getResource(WorldTimeResource.getResourceType());
            TilledSoilBlock tilledSoilBlockComponent = chunkStore.getComponent(blockRef, TilledSoilBlock.getComponentType());
            if (tilledSoilBlockComponent != null) {
               Instant wateredUntil = worldTimeResource.getGameTime().plus(this.duration, ChronoUnit.SECONDS);
               tilledSoilBlockComponent.setWateredUntil(wateredUntil);
               worldChunk.setTicking(x, targetBlock.getY(), z, true);
               worldChunk.getBlockChunk().getSectionAtBlockY(targetBlock.y).scheduleTick(ChunkUtil.indexBlock(x, targetBlock.y, z), wateredUntil);
               worldChunk.setTicking(x, targetBlock.getY() + 1, z, true);
            } else {
               FarmingBlock farmingBlockComponent = chunkStore.getComponent(blockRef, FarmingBlock.getComponentType());
               if (farmingBlockComponent == null) {
                  context.getState().state = InteractionState.Failed;
               } else {
                  Ref<ChunkStore> soilBlockRef = worldChunk.getBlockComponentEntity(x, targetBlock.getY() - 1, z);
                  if (soilBlockRef != null && soilBlockRef.isValid()) {
                     tilledSoilBlockComponent = chunkStore.getComponent(soilBlockRef, TilledSoilBlock.getComponentType());
                     if (tilledSoilBlockComponent == null) {
                        context.getState().state = InteractionState.Failed;
                     } else {
                        Instant wateredUntil = worldTimeResource.getGameTime().plus(this.duration, ChronoUnit.SECONDS);
                        tilledSoilBlockComponent.setWateredUntil(wateredUntil);
                        worldChunk.getBlockChunk()
                           .getSectionAtBlockY(targetBlock.y - 1)
                           .scheduleTick(ChunkUtil.indexBlock(x, targetBlock.y - 1, z), wateredUntil);
                        worldChunk.setTicking(x, targetBlock.getY() - 1, z, true);
                        worldChunk.setTicking(x, targetBlock.getY(), z, true);
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
