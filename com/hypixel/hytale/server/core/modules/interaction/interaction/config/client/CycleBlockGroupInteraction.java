package com.hypixel.hytale.server.core.modules.interaction.interaction.config.client;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockSoundEvent;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionSyncData;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.blocksound.config.BlockSoundSet;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.gameplay.GameplayConfig;
import com.hypixel.hytale.server.core.asset.type.gameplay.WorldConfig;
import com.hypixel.hytale.server.core.asset.type.item.config.BlockGroup;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
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

public class CycleBlockGroupInteraction extends SimpleBlockInteraction {
   private static final int SET_SETTINGS = 256;
   @Nonnull
   public static final BuilderCodec<CycleBlockGroupInteraction> CODEC = BuilderCodec.builder(
         CycleBlockGroupInteraction.class, CycleBlockGroupInteraction::new, SimpleBlockInteraction.CODEC
      )
      .documentation("Attempts to cycle the target block through its block set.")
      .build();

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
      Store<EntityStore> store = ref.getStore();
      Player playerComponent = commandBuffer.getComponent(ref, Player.getComponentType());
      InteractionSyncData state = context.getState();
      state.state = InteractionState.Failed;
      if (playerComponent == null) {
         ((HytaleLogger.Api)HytaleLogger.getLogger().at(Level.INFO).atMostEvery(5, TimeUnit.MINUTES))
            .log("CycleBlockGroupInteraction requires a Player but was used for: %s", ref);
      } else {
         ChunkStore chunkStore = world.getChunkStore();
         Store<ChunkStore> chunkStoreStore = chunkStore.getStore();
         long chunkIndex = ChunkUtil.indexChunkFromBlock(targetBlock.x, targetBlock.z);
         Ref<ChunkStore> chunkReference = chunkStore.getChunkReference(chunkIndex);
         if (chunkReference != null && chunkReference.isValid()) {
            WorldChunk worldChunkComponent = chunkStoreStore.getComponent(chunkReference, WorldChunk.getComponentType());

            assert worldChunkComponent != null;

            BlockChunk blockChunkComponent = chunkStoreStore.getComponent(chunkReference, BlockChunk.getComponentType());

            assert blockChunkComponent != null;

            BlockSection blockSection = blockChunkComponent.getSectionAtBlockY(targetBlock.getY());
            GameplayConfig gameplayConfig = world.getGameplayConfig();
            WorldConfig worldConfig = gameplayConfig.getWorldConfig();
            boolean blockBreakingAllowed = worldConfig.isBlockBreakingAllowed();
            if (blockBreakingAllowed) {
               int blockIndex = blockSection.get(targetBlock.x, targetBlock.y, targetBlock.z);
               BlockType targetBlockType = BlockType.getAssetMap().getAsset(blockIndex);
               if (targetBlockType != null) {
                  Item targetBlockItem = targetBlockType.getItem();
                  BlockGroup set = BlockGroup.findItemGroup(targetBlockItem);
                  if (set != null) {
                     int currentIndex = set.getIndex(targetBlockItem);
                     if (currentIndex != -1) {
                        String nextBlockKey = set.get((currentIndex + 1) % set.size());
                        BlockType nextBlockType = BlockType.getAssetMap().getAsset(nextBlockKey);
                        if (nextBlockType != null) {
                           ItemStack heldItem = context.getHeldItem();
                           if (heldItem != null && playerComponent.canDecreaseItemStackDurability(ref, store) && !heldItem.isUnbreakable()) {
                              playerComponent.updateItemStackDurability(
                                 ref,
                                 heldItem,
                                 playerComponent.getInventory().getHotbar(),
                                 context.getHeldItemSlot(),
                                 -heldItem.getItem().getDurabilityLossOnHit(),
                                 commandBuffer
                              );
                           }

                           int newBlockId = BlockType.getAssetMap().getIndex(nextBlockType.getId());
                           int rotation = worldChunkComponent.getRotationIndex(targetBlock.x, targetBlock.y, targetBlock.z);
                           worldChunkComponent.setBlock(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ(), newBlockId, nextBlockType, rotation, 0, 256);
                           state.state = InteractionState.NotFinished;
                           BlockSoundSet soundSet = BlockSoundSet.getAssetMap().getAsset(nextBlockType.getBlockSoundSetIndex());
                           if (soundSet != null) {
                              int soundEventIndex = soundSet.getSoundEventIndices().getOrDefault(BlockSoundEvent.Hit, 0);
                              if (soundEventIndex != 0) {
                                 SoundUtil.playSoundEvent3d(ref, soundEventIndex, targetBlock.x + 0.5, targetBlock.y + 0.5, targetBlock.z + 0.5, commandBuffer);
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

   @Override
   protected void simulateInteractWithBlock(
      @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nullable ItemStack itemInHand, @Nonnull World world, @Nonnull Vector3i targetBlock
   ) {
   }

   @Nonnull
   @Override
   public String toString() {
      return "CycleBlockGroupInteraction{}";
   }
}
