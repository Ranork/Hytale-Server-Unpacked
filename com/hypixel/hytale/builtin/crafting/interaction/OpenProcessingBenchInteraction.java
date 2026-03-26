package com.hypixel.hytale.builtin.crafting.interaction;

import com.hypixel.hytale.builtin.crafting.component.BenchBlock;
import com.hypixel.hytale.builtin.crafting.component.ProcessingBenchBlock;
import com.hypixel.hytale.builtin.crafting.window.BenchWindow;
import com.hypixel.hytale.builtin.crafting.window.ProcessingBenchWindow;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.accessor.BlockAccessor;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class OpenProcessingBenchInteraction extends SimpleBlockInteraction {
   @Nonnull
   public static final BuilderCodec<OpenProcessingBenchInteraction> CODEC = BuilderCodec.builder(
         OpenProcessingBenchInteraction.class, OpenProcessingBenchInteraction::new, SimpleBlockInteraction.CODEC
      )
      .documentation("Opens the processing bench page.")
      .build();

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
      Store<EntityStore> store = ref.getStore();
      Player playerComponent = commandBuffer.getComponent(ref, Player.getComponentType());
      if (playerComponent != null) {
         ChunkStore chunkStore = world.getChunkStore();
         Ref<ChunkStore> chunkRef = chunkStore.getChunkReference(ChunkUtil.indexChunkFromBlock(pos.x, pos.z));
         if (chunkRef != null && chunkRef.isValid()) {
            Store<ChunkStore> chunkStoreStore = chunkStore.getStore();
            BlockComponentChunk blockComponentChunk = chunkStoreStore.getComponent(chunkRef, BlockComponentChunk.getComponentType());
            if (blockComponentChunk != null) {
               Ref<ChunkStore> blockEntityRef = blockComponentChunk.getEntityReference(ChunkUtil.indexBlockInColumn(pos.x, pos.y, pos.z));
               if (blockEntityRef != null && blockEntityRef.isValid()) {
                  ProcessingBenchBlock benchState = chunkStoreStore.getComponent(blockEntityRef, ProcessingBenchBlock.getComponentType());
                  if (benchState == null) {
                     playerComponent.sendMessage(
                        Message.translation("server.interactions.invalidBlockState")
                           .param("interaction", this.getClass().getSimpleName())
                           .param("blockState", "null")
                     );
                  } else {
                     BenchBlock benchBlock = chunkStoreStore.getComponent(blockEntityRef, BenchBlock.getComponentType());
                     if (benchBlock != null) {
                        BlockModule.BlockStateInfo blockStateInfo = chunkStoreStore.getComponent(blockEntityRef, BlockModule.BlockStateInfo.getComponentType());
                        BlockType blockType = world.getBlockType(pos.x, pos.y, pos.z);
                        UUIDComponent uuidComponent = commandBuffer.getComponent(ref, UUIDComponent.getComponentType());
                        if (uuidComponent != null) {
                           UUID uuid = uuidComponent.getUuid();
                           WorldChunk worldChunk = world.getChunk(ChunkUtil.indexChunkFromBlock(pos.x, pos.z));
                           if (worldChunk != null) {
                              int rotationIndex = worldChunk.getRotationIndex(pos.x, pos.y, pos.z);
                              ProcessingBenchWindow window = new ProcessingBenchWindow(
                                 benchState, benchBlock, blockStateInfo, pos.x, pos.y, pos.z, rotationIndex, blockType
                              );
                              Map<UUID, BenchWindow> windows = benchBlock.getWindows();
                              if (windows.putIfAbsent(uuid, window) == null) {
                                 benchState.updateFuelValues(benchBlock.getWindows());
                                 if (playerComponent.getPageManager().setPageWithWindows(ref, store, Page.Bench, true, window)) {
                                    window.registerCloseEvent(event -> {
                                       windows.remove(uuid, window);
                                       BlockType currentBlockType = world.getBlockType(pos);
                                       if (currentBlockType != null && currentBlockType != BlockType.EMPTY && currentBlockType != BlockType.UNKNOWN) {
                                          String interactionState = BlockAccessor.getCurrentInteractionState(currentBlockType);
                                          if (windows.isEmpty() && !"Processing".equals(interactionState) && !"ProcessCompleted".equals(interactionState)) {
                                             world.setBlockInteractionState(pos, BenchBlock.getBaseBlockType(currentBlockType), benchBlock.getTierStateName());
                                          }

                                          int soundEventIndexx = blockType.getBench().getLocalCloseSoundEventIndex();
                                          if (soundEventIndexx != 0) {
                                             SoundUtil.playSoundEvent2d(ref, soundEventIndexx, SoundCategory.UI, commandBuffer);
                                          }
                                       }
                                    });
                                    int soundEventIndex = blockType.getBench().getLocalOpenSoundEventIndex();
                                    if (soundEventIndex == 0) {
                                       return;
                                    }

                                    SoundUtil.playSoundEvent2d(ref, soundEventIndex, SoundCategory.UI, commandBuffer);
                                 } else {
                                    windows.remove(uuid, window);
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
   }

   @Override
   protected void simulateInteractWithBlock(
      @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nullable ItemStack itemInHand, @Nonnull World world, @Nonnull Vector3i targetBlock
   ) {
   }
}
