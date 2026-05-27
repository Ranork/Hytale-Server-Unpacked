package com.hypixel.hytale.server.core.modules.interaction.interaction.config.client;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.protocol.Interaction;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.gameplay.GameplayConfig;
import com.hypixel.hytale.server.core.asset.type.gameplay.WorldConfig;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.BlockHarvestUtils;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3i;

public class BreakBlockInteraction extends SimpleBlockInteraction {
   @Nonnull
   public static final BuilderCodec<BreakBlockInteraction> CODEC = BuilderCodec.builder(
         BreakBlockInteraction.class, BreakBlockInteraction::new, SimpleBlockInteraction.CODEC
      )
      .documentation("Attempts to break the target block.")
      .<Boolean>appendInherited(
         new KeyedCodec<>("Harvest", Codec.BOOLEAN),
         (interaction, v) -> interaction.harvest = v,
         interaction -> interaction.harvest,
         (o, p) -> o.harvest = p.harvest
      )
      .documentation("Whether this should trigger as a harvest gather vs a break gather.")
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("Tool", Codec.STRING), (interaction, v) -> interaction.toolId = v, interaction -> interaction.toolId, (o, p) -> o.toolId = p.toolId
      )
      .documentation("Tool to break as.")
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("MatchTool", Codec.BOOLEAN),
         (interaction, v) -> interaction.matchTool = v,
         interaction -> interaction.matchTool,
         (o, p) -> o.matchTool = p.matchTool
      )
      .documentation("Whether to require an match to `Tool` to work.")
      .add()
      .build();
   protected boolean harvest;
   @Nullable
   protected String toolId;
   protected boolean matchTool;

   @Override
   protected void tick0(
      boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler
   ) {
      super.tick0(firstRun, time, type, context, cooldownHandler);
      computeCurrentBlockSyncData(context);
   }

   @Override
   protected void interactWithBlock(
      @Nonnull World world,
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      @Nonnull InteractionType type,
      @Nonnull InteractionContext context,
      @Nullable ItemStack heldItemStack,
      @Nonnull Vector3i targetBlock,
      @Nonnull CooldownHandler cooldownHandler
   ) {
      Ref<EntityStore> ref = context.getEntity();
      Ref<EntityStore> ownerRef = context.getOwningEntity();
      Player playerComponent = commandBuffer.getComponent(ownerRef, Player.getComponentType());
      if (playerComponent == null) {
         ((HytaleLogger.Api)HytaleLogger.getLogger().at(Level.INFO).atMostEvery(5, TimeUnit.MINUTES))
            .log("BreakBlockInteraction requires a Player but was used for: %s", ref);
      } else {
         Inventory playerInventory = playerComponent.getInventory();
         ItemStack ownerHeldItem = playerInventory.getItemInHand();
         ChunkStore chunkStore = world.getChunkStore();
         Store<ChunkStore> chunkStoreStore = chunkStore.getStore();
         long chunkIndex = ChunkUtil.indexChunkFromBlock(targetBlock.x, targetBlock.z);
         Ref<ChunkStore> chunkReference = chunkStore.getChunkReference(chunkIndex);
         if (chunkReference != null && chunkReference.isValid()) {
            WorldChunk worldChunkComponent = chunkStoreStore.getComponent(chunkReference, WorldChunk.getComponentType());

            assert worldChunkComponent != null;

            BlockChunk blockChunkComponent = chunkStoreStore.getComponent(chunkReference, BlockChunk.getComponentType());

            assert blockChunkComponent != null;

            BlockSection blockSection = blockChunkComponent.getSectionAtBlockY(targetBlock.y());
            GameplayConfig gameplayConfig = world.getGameplayConfig();
            WorldConfig worldConfig = gameplayConfig.getWorldConfig();
            if (this.harvest) {
               int x = targetBlock.x();
               int y = targetBlock.y();
               int z = targetBlock.z();
               BlockType blockType = worldChunkComponent.getBlockType(x, y, z);
               if (blockType == null) {
                  context.getState().state = InteractionState.Failed;
                  return;
               }

               if (!worldConfig.isBlockGatheringAllowed()) {
                  context.getState().state = InteractionState.Failed;
                  return;
               }

               if (!BlockHarvestUtils.shouldPickupByInteraction(blockType)) {
                  context.getState().state = InteractionState.Failed;
                  return;
               }

               int filler = blockSection.getFiller(x, y, z);
               BlockHarvestUtils.performPickupByInteraction(ref, targetBlock, blockType, filler, chunkReference, commandBuffer, chunkStoreStore);
            } else {
               boolean blockBreakingAllowed = worldConfig.isBlockBreakingAllowed();
               if (!blockBreakingAllowed) {
                  context.getState().state = InteractionState.Failed;
                  return;
               }

               switch (playerComponent.getGameMode()) {
                  case null:
                     throw new UnsupportedOperationException("GameMode is not supported");
                  case Adventure:
                     BlockHarvestUtils.performBlockDamage(
                        ref, targetBlock, ownerHeldItem, null, this.toolId, this.matchTool, 1.0F, 0, chunkReference, commandBuffer, chunkStoreStore
                     );
                     break;
                  case Creative:
                     BlockHarvestUtils.performBlockBreak(ref, ownerHeldItem, targetBlock, chunkReference, commandBuffer, chunkStoreStore);
                     break;
                  default:
                     throw new MatchException(null, null);
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
   protected Interaction generatePacket() {
      return new com.hypixel.hytale.protocol.BreakBlockInteraction();
   }

   @Override
   protected void configurePacket(Interaction packet) {
      super.configurePacket(packet);
      com.hypixel.hytale.protocol.BreakBlockInteraction p = (com.hypixel.hytale.protocol.BreakBlockInteraction)packet;
      p.harvest = this.harvest;
   }

   @Nonnull
   @Override
   public String toString() {
      return "BreakBlockInteraction{harvest=" + this.harvest + "} " + super.toString();
   }
}
