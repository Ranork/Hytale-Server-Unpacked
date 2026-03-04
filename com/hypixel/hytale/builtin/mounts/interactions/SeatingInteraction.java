package com.hypixel.hytale.builtin.mounts.interactions;

import com.hypixel.hytale.builtin.mounts.BlockMountAPI;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.BlockSoundEvent;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocksound.config.BlockSoundSet;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SeatingInteraction extends SimpleBlockInteraction {
   @Nonnull
   public static final BuilderCodec<SeatingInteraction> CODEC = BuilderCodec.builder(
         SeatingInteraction.class, SeatingInteraction::new, SimpleBlockInteraction.CODEC
      )
      .documentation("Arranges perfect seating accommodations")
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
      Ref<EntityStore> ref = context.getEntity();
      Player player = commandBuffer.getComponent(ref, Player.getComponentType());
      if (player != null) {
         BlockPosition rawTarget = context.getMetaStore().getMetaObject(TARGET_BLOCK_RAW);
         Vector3f whereWasHit = new Vector3f(rawTarget.x + 0.5F, rawTarget.y + 0.5F, rawTarget.z + 0.5F);
         BlockMountAPI.BlockMountResult result = BlockMountAPI.mountOnBlock(ref, commandBuffer, targetBlock, whereWasHit);
         if (result == BlockMountAPI.DidNotMount.ALREADY_MOUNTED) {
            int soundEventIndex = SoundEvent.getAssetMap().getIndex("SFX_Creative_Play_Add_Mask");
            SoundUtil.playSoundEvent2d(ref, soundEventIndex, SoundCategory.SFX, commandBuffer);
         } else if (result instanceof BlockMountAPI.Mounted mounted) {
            BlockSoundSet soundSet = BlockSoundSet.getAssetMap().getAsset(mounted.blockType().getBlockSoundSetIndex());
            String seatSoundId = soundSet == null ? null : soundSet.getSoundEventIds().getOrDefault(BlockSoundEvent.Walk, null);
            if (seatSoundId != null) {
               int soundEventIndex = SoundEvent.getAssetMap().getIndex(seatSoundId);
               SoundUtil.playSoundEvent3dToPlayer(ref, soundEventIndex, SoundCategory.SFX, targetBlock.toVector3d(), commandBuffer);
            }
         } else {
            player.sendMessage(Message.translation("server.interactions.didNotMount").param("state", result.toString()));
         }
      }
   }

   @Override
   protected void simulateInteractWithBlock(
      @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nullable ItemStack itemInHand, @Nonnull World world, @Nonnull Vector3i targetBlock
   ) {
   }
}
