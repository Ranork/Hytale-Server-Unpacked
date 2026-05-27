package com.hypixel.hytale.server.core.modules.interaction.interaction.config.server;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.windows.ContainerBlockWindow;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3i;

public class OpenContainerInteraction extends SimpleBlockInteraction {
   public static final BuilderCodec<OpenContainerInteraction> CODEC = BuilderCodec.builder(
         OpenContainerInteraction.class, OpenContainerInteraction::new, SimpleBlockInteraction.CODEC
      )
      .documentation("Opens the container of the block currently being interacted with.")
      .build();
   public static final String OPEN_WINDOW = "OpenWindow";
   public static final String CLOSE_WINDOW = "CloseWindow";

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
         PlayerRef playerRefComponent = commandBuffer.getComponent(ref, PlayerRef.getComponentType());
         if (playerRefComponent != null) {
            ChunkStore chunkStore = world.getChunkStore();
            long chunkIndex = ChunkUtil.indexChunkFromBlock(pos.x, pos.z);
            Ref<ChunkStore> chunkRef = chunkStore.getChunkReference(chunkIndex);
            if (chunkRef != null && chunkRef.isValid()) {
               Store<ChunkStore> chunkComponentStore = chunkStore.getStore();
               BlockComponentChunk blockComponentChunk = chunkComponentStore.getComponent(chunkRef, BlockComponentChunk.getComponentType());
               if (blockComponentChunk != null) {
                  int columnBlockIndex = ChunkUtil.indexBlockInColumn(pos.x, pos.y, pos.z);
                  Ref<ChunkStore> blockRef = blockComponentChunk.getEntityReference(columnBlockIndex);
                  if (blockRef != null) {
                     ItemContainerBlock itemContainerBlock = chunkComponentStore.getComponent(blockRef, ItemContainerBlock.getComponentType());
                     if (itemContainerBlock == null) {
                        playerRefComponent.sendMessage(
                           Message.translation("server.interactions.invalidBlockState")
                              .param("interaction", this.getClass().getSimpleName())
                              .param("blockState", chunkComponentStore.getArchetype(blockRef).toString())
                        );
                     } else {
                        BlockChunk blockChunkComponent = chunkComponentStore.getComponent(chunkRef, BlockChunk.getComponentType());
                        if (blockChunkComponent != null) {
                           int blockId = blockChunkComponent.getBlock(pos.x, pos.y, pos.z);
                           BlockType blockType = BlockType.getAssetMap().getAsset(blockId);
                           if (blockType != null) {
                              BlockSection section = blockChunkComponent.getSectionAtBlockY(pos.y);
                              int rotationIndex = section.getRotationIndex(pos.x, pos.y, pos.z);
                              ContainerBlockWindow window = new ContainerBlockWindow(
                                 pos.x, pos.y, pos.z, rotationIndex, blockType, itemContainerBlock.getItemContainer()
                              );
                              Map<UUID, ContainerBlockWindow> windows = itemContainerBlock.getWindows();
                              UUIDComponent uuidComponent = commandBuffer.getComponent(ref, UUIDComponent.getComponentType());

                              assert uuidComponent != null;

                              UUID uuid = uuidComponent.getUuid();
                              if (windows.putIfAbsent(uuid, window) == null) {
                                 if (playerComponent.getPageManager().setPageWithWindows(ref, store, Page.Bench, true, window)) {
                                    window.registerCloseEvent(var8x -> onWindowClose(world, ref, uuid, pos, blockType, window, windows, commandBuffer));
                                    if (windows.size() == 1) {
                                       world.setBlockInteractionState(pos, blockType, "OpenWindow");
                                    }

                                    BlockType interactionState = blockType.getBlockForState("OpenWindow");
                                    if (interactionState == null) {
                                       return;
                                    }

                                    int soundEventIndex = interactionState.getInteractionSoundEventIndex();
                                    if (soundEventIndex == 0) {
                                       return;
                                    }

                                    Vector3d soundPos = new Vector3d();
                                    blockType.getBlockCenter(rotationIndex, soundPos);
                                    soundPos.add(pos.x, pos.y, pos.z);
                                    SoundUtil.playSoundEvent3d(ref, soundEventIndex, soundPos, commandBuffer);
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

   private static void onWindowClose(
      @Nonnull World world,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull UUID uuid,
      @Nonnull Vector3i pos,
      @Nonnull BlockType blockType,
      @Nonnull ContainerBlockWindow window,
      @Nonnull Map<UUID, ContainerBlockWindow> windows,
      @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      windows.remove(uuid, window);
      ChunkStore chunkStore = world.getChunkStore();
      long chunkIndex = ChunkUtil.indexChunkFromBlock(pos.x, pos.z);
      Ref<ChunkStore> chunkRef = chunkStore.getChunkReference(chunkIndex);
      if (chunkRef != null && chunkRef.isValid()) {
         Store<ChunkStore> chunkComponentStore = chunkStore.getStore();
         BlockChunk blockChunkComponent = chunkComponentStore.getComponent(chunkRef, BlockChunk.getComponentType());
         if (blockChunkComponent != null) {
            WorldChunk worldChunkComponent = chunkComponentStore.getComponent(chunkRef, WorldChunk.getComponentType());
            if (worldChunkComponent != null) {
               BlockType currentBlockType = worldChunkComponent.getBlockType(pos);
               if (currentBlockType != null) {
                  if (windows.isEmpty()) {
                     world.setBlockInteractionState(pos, currentBlockType, "CloseWindow");
                  }

                  BlockType interactionState = currentBlockType.getBlockForState("CloseWindow");
                  if (interactionState != null) {
                     int soundEventIndex = interactionState.getInteractionSoundEventIndex();
                     if (soundEventIndex != 0) {
                        BlockSection section = blockChunkComponent.getSectionAtBlockY(pos.y);
                        int rotationIndex = section.getRotationIndex(pos.x, pos.y, pos.z);
                        Vector3d soundPos = new Vector3d();
                        blockType.getBlockCenter(rotationIndex, soundPos);
                        soundPos.add(pos.x, pos.y, pos.z);
                        SoundUtil.playSoundEvent3d(ref, soundEventIndex, soundPos, commandBuffer);
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
