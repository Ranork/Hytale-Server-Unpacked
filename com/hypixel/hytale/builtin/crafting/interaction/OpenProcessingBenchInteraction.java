package com.hypixel.hytale.builtin.crafting.interaction;

import com.hypixel.hytale.builtin.crafting.state.ProcessingBenchState;
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
import com.hypixel.hytale.server.core.asset.type.blocktype.config.bench.Bench;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.accessor.BlockAccessor;
import com.hypixel.hytale.server.core.universe.world.meta.BlockState;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
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
         BlockState state = world.getState(pos.x, pos.y, pos.z, true);
         if (!(state instanceof ProcessingBenchState benchState)) {
            playerComponent.sendMessage(
               Message.translation("server.interactions.invalidBlockState")
                  .param("interaction", this.getClass().getSimpleName())
                  .param("blockState", state != null ? state.getClass().getSimpleName() : "null")
            );
         } else {
            BlockType blockType = world.getBlockType(pos.x, pos.y, pos.z);
            Bench blockTypeBench = blockType.getBench();
            if ((blockTypeBench == null || !blockTypeBench.equals(benchState.getBench())) && !benchState.initialize(blockType)) {
               ProcessingBenchState.LOGGER.at(Level.WARNING).log("Failed to re-initialize: %s, %s", blockType.getId(), pos);
               int x = pos.getX();
               int z = pos.getZ();
               world.getChunk(ChunkUtil.indexChunkFromBlock(x, z)).setState(x, pos.getY(), z, (BlockState)null);
            } else {
               UUIDComponent uuidComponent = commandBuffer.getComponent(ref, UUIDComponent.getComponentType());
               if (uuidComponent != null) {
                  UUID uuid = uuidComponent.getUuid();
                  ProcessingBenchWindow window = new ProcessingBenchWindow(benchState);
                  Map<UUID, BenchWindow> windows = benchState.getWindows();
                  if (windows.putIfAbsent(uuid, window) == null) {
                     benchState.updateFuelValues();
                     if (playerComponent.getPageManager().setPageWithWindows(ref, store, Page.Bench, true, window)) {
                        window.registerCloseEvent(event -> {
                           windows.remove(uuid, window);
                           BlockType currentBlockType = world.getBlockType(pos);
                           if (currentBlockType != null) {
                              String interactionState = BlockAccessor.getCurrentInteractionState(currentBlockType);
                              if (windows.isEmpty() && !"Processing".equals(interactionState) && !"ProcessCompleted".equals(interactionState)) {
                                 world.setBlockInteractionState(pos, benchState.getBaseBlockType(), benchState.getTierStateName());
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

   @Override
   protected void simulateInteractWithBlock(
      @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nullable ItemStack itemInHand, @Nonnull World world, @Nonnull Vector3i targetBlock
   ) {
   }
}
