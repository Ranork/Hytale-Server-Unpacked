package com.hypixel.hytale.server.core.modules.interaction.interaction.config.client;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.Interaction;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ChangeStateInteraction extends SimpleBlockInteraction {
   @Nonnull
   public static final BuilderCodec<ChangeStateInteraction> CODEC = BuilderCodec.builder(
         ChangeStateInteraction.class, ChangeStateInteraction::new, SimpleBlockInteraction.CODEC
      )
      .documentation("Changes the state of the target block to another state based on the mapping provided.")
      .<Map>appendInherited(
         new KeyedCodec<>("Changes", new MapCodec<>(Codec.STRING, HashMap::new)),
         (interaction, changeMap) -> interaction.stateKeys = changeMap,
         interaction -> interaction.stateKeys,
         (o, p) -> o.stateKeys = p.stateKeys
      )
      .documentation("The map of state changes to execute. `\"default\"` can be used for the initial state of a block.")
      .add()
      .appendInherited(
         new KeyedCodec<>("UpdateBlockState", Codec.BOOLEAN),
         (o, i) -> o.updateBlockState = i,
         o -> o.updateBlockState,
         (o, p) -> o.updateBlockState = p.updateBlockState
      )
      .add()
      .build();
   private static final int SET_SETTINGS = 260;
   protected Map<String, String> stateKeys;
   protected boolean updateBlockState = false;

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
      WorldChunk chunk = world.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(targetBlock.x, targetBlock.z));
      if (chunk != null) {
         BlockType current = chunk.getBlockType(targetBlock);
         String currentState = current.getStateForBlock(current);
         if (currentState == null) {
            currentState = "default";
         }

         String newState = this.stateKeys.get(currentState);
         if (newState != null) {
            String newBlock = current.getBlockKeyForState(newState);
            if (newBlock != null) {
               int newBlockId = BlockType.getAssetMap().getIndex(newBlock);
               if (newBlockId == Integer.MIN_VALUE) {
                  context.getState().state = InteractionState.Failed;
                  return;
               }

               BlockType newBlockType = BlockType.getAssetMap().getAsset(newBlockId);
               int rotation = chunk.getRotationIndex(targetBlock.x, targetBlock.y, targetBlock.z);
               int settings = 260;
               if (!this.updateBlockState) {
                  settings |= 2;
               }

               chunk.setBlock(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ(), newBlockId, newBlockType, rotation, 0, settings);
               BlockType interactionStateBlock = current.getBlockForState(newState);
               if (interactionStateBlock == null) {
                  return;
               }

               int soundEventIndex = interactionStateBlock.getInteractionSoundEventIndex();
               if (soundEventIndex == 0) {
                  return;
               }

               Ref<EntityStore> ref = context.getEntity();
               SoundUtil.playSoundEvent3d(ref, soundEventIndex, targetBlock.x + 0.5, targetBlock.y + 0.5, targetBlock.z + 0.5, commandBuffer);
               return;
            }
         }

         context.getState().state = InteractionState.Failed;
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
      return new com.hypixel.hytale.protocol.ChangeStateInteraction();
   }

   @Override
   protected void configurePacket(Interaction packet) {
      super.configurePacket(packet);
      com.hypixel.hytale.protocol.ChangeStateInteraction p = (com.hypixel.hytale.protocol.ChangeStateInteraction)packet;
      p.stateChanges = this.stateKeys;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ChangeStateInteraction{stateKeys=" + this.stateKeys + "} " + super.toString();
   }
}
