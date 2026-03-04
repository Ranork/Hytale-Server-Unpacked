package com.hypixel.hytale.builtin.adventure.farming.interactions;

import com.hypixel.hytale.builtin.adventure.farming.FarmingUtil;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HarvestCropInteraction extends SimpleBlockInteraction {
   @Nonnull
   public static final BuilderCodec<HarvestCropInteraction> CODEC = BuilderCodec.builder(
         HarvestCropInteraction.class, HarvestCropInteraction::new, SimpleBlockInteraction.CODEC
      )
      .documentation("Harvests the resources from the target farmable block.")
      .<Boolean>appendInherited(
         new KeyedCodec<>("RequireNotBroken", Codec.BOOLEAN),
         (interaction, s) -> interaction.requireNotBroken = s,
         interaction -> interaction.requireNotBroken,
         (interaction, parent) -> interaction.requireNotBroken = parent.requireNotBroken
      )
      .documentation("If true, the interaction will fail if the held item is broken (durability = 0).")
      .add()
      .build();
   protected boolean requireNotBroken = false;

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
      if (this.requireNotBroken && itemInHand != null && itemInHand.isBroken()) {
         context.getState().state = InteractionState.Failed;
      } else {
         Ref<EntityStore> ref = context.getEntity();
         ChunkStore chunkStore = world.getChunkStore();
         long chunkIndex = ChunkUtil.indexChunkFromBlock(targetBlock.x, targetBlock.z);
         Ref<ChunkStore> chunkRef = chunkStore.getChunkReference(chunkIndex);
         if (chunkRef != null && chunkRef.isValid()) {
            BlockChunk blockChunkComponent = chunkStore.getStore().getComponent(chunkRef, BlockChunk.getComponentType());

            assert blockChunkComponent != null;

            BlockSection blockSection = blockChunkComponent.getSectionAtBlockY(targetBlock.y);
            if (blockSection != null) {
               WorldChunk worldChunkComponent = chunkStore.getStore().getComponent(chunkRef, WorldChunk.getComponentType());

               assert worldChunkComponent != null;

               BlockType blockType = worldChunkComponent.getBlockType(targetBlock);
               if (blockType != null) {
                  int rotationIndex = blockSection.getRotationIndex(targetBlock.x, targetBlock.y, targetBlock.z);
                  if (!FarmingUtil.harvest(world, commandBuffer, ref, blockType, rotationIndex, targetBlock)) {
                     context.getState().state = InteractionState.Failed;
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

   @Nonnull
   @Override
   public String toString() {
      return "HarvestCropInteraction{} " + super.toString();
   }
}
