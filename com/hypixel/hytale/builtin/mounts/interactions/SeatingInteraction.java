package com.hypixel.hytale.builtin.mounts.interactions;

import com.hypixel.hytale.builtin.mounts.BlockMountAPI;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3iUtil;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.BlockSoundEvent;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocksound.config.BlockSoundSet;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3i;

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
      PlayerRef playerRef = commandBuffer.getComponent(ref, PlayerRef.getComponentType());
      if (playerRef != null) {
         BlockPosition rawTarget = context.getMetaStore().getMetaObject(TARGET_BLOCK_RAW);
         Vector3d whereWasHit = new Vector3d(rawTarget.x + 0.5, rawTarget.y + 0.5, rawTarget.z + 0.5);
         BlockMountAPI.BlockMountResult result = BlockMountAPI.mountOnBlock(ref, commandBuffer, targetBlock, whereWasHit);
         if (result == BlockMountAPI.DidNotMount.ALREADY_MOUNTED) {
            int soundEventIndex = SoundEvent.getAssetMap().getIndex("SFX_Creative_Play_Add_Mask");
            SoundUtil.playSoundEvent2d(ref, soundEventIndex, SoundCategory.SFX, commandBuffer);
         } else if (result instanceof BlockMountAPI.Mounted mounted) {
            BlockSoundSet soundSet = BlockSoundSet.getAssetMap().getAsset(mounted.blockType().getBlockSoundSetIndex());
            String seatSoundId = soundSet == null ? null : soundSet.getSoundEventIds().getOrDefault(BlockSoundEvent.Walk, null);
            if (seatSoundId != null) {
               int soundEventIndex = SoundEvent.getAssetMap().getIndex(seatSoundId);
               SoundUtil.playSoundEvent3dToPlayer(ref, soundEventIndex, SoundCategory.SFX, Vector3iUtil.toVector3d(targetBlock), commandBuffer);
            }
         } else {
            playerRef.sendMessage(Message.translation("server.interactions.didNotMount").param("state", result.toString()));
         }
      }
   }

   @Override
   protected void simulateInteractWithBlock(
      @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nullable ItemStack itemInHand, @Nonnull World world, @Nonnull Vector3i targetBlock
   ) {
   }
}
