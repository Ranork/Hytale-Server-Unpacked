package com.hypixel.hytale.server.core.entity;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.AnimationSlot;
import com.hypixel.hytale.protocol.packets.entities.PlayAnimation;
import com.hypixel.hytale.server.core.asset.type.itemanimation.config.ItemPlayerAnimations;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.world.PlayerUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AnimationUtils {
   public static void playAnimation(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull AnimationSlot animationSlot,
      @Nullable String animationId,
      boolean sendToSelf,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      playAnimation(ref, animationSlot, null, animationId, sendToSelf, componentAccessor);
   }

   public static void playAnimation(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull AnimationSlot animationSlot,
      @Nullable String itemAnimationsId,
      @Nullable String animationId,
      boolean sendToSelf,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      Model model = null;
      ModelComponent modelComponent = componentAccessor.getComponent(ref, ModelComponent.getComponentType());
      if (modelComponent != null) {
         model = modelComponent.getModel();
      }

      if (animationSlot != AnimationSlot.Action && animationId != null && model != null && !model.getAnimationSetMap().containsKey(animationId)) {
         ((HytaleLogger.Api)Entity.LOGGER.at(Level.WARNING).atMostEvery(1, TimeUnit.MINUTES))
            .log("Missing animation '%s' for Model '%s'", animationId, model.getModelAssetId());
      } else {
         NetworkId networkIdComponent = componentAccessor.getComponent(ref, NetworkId.getComponentType());

         assert networkIdComponent != null;

         PlayAnimation animationPacket = new PlayAnimation(networkIdComponent.getId(), itemAnimationsId, animationId, animationSlot);
         if (sendToSelf) {
            PlayerUtil.forEachPlayerThatCanSeeEntity(
               ref, (playerRef, playerRefComponent, ca) -> playerRefComponent.getPacketHandler().writeNoCache(animationPacket), componentAccessor
            );
         } else {
            PlayerUtil.forEachPlayerThatCanSeeEntity(
               ref, (playerRef, playerRefComponent, ca) -> playerRefComponent.getPacketHandler().writeNoCache(animationPacket), ref, componentAccessor
            );
         }
      }
   }

   public static void playAnimation(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull AnimationSlot animationSlot,
      @Nonnull String itemAnimationsId,
      @Nonnull String animationId,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      playAnimation(ref, animationSlot, itemAnimationsId, animationId, false, componentAccessor);
   }

   public static void playAnimation(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull AnimationSlot animationSlot,
      @Nullable ItemPlayerAnimations itemAnimations,
      @Nonnull String animationId,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      String itemAnimationsId = itemAnimations != null ? itemAnimations.getId() : null;
      playAnimation(ref, animationSlot, itemAnimationsId, animationId, false, componentAccessor);
   }

   public static void stopAnimation(
      @Nonnull Ref<EntityStore> ref, @Nonnull AnimationSlot animationSlot, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      stopAnimation(ref, animationSlot, false, componentAccessor);
   }

   public static void stopAnimation(
      @Nonnull Ref<EntityStore> ref, @Nonnull AnimationSlot animationSlot, boolean sendToSelf, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      NetworkId networkIdComponent = componentAccessor.getComponent(ref, NetworkId.getComponentType());

      assert networkIdComponent != null;

      PlayAnimation animationPacket = new PlayAnimation(networkIdComponent.getId(), null, null, animationSlot);
      if (sendToSelf) {
         PlayerUtil.forEachPlayerThatCanSeeEntity(
            ref, (playerRef, playerRefComponent, ca) -> playerRefComponent.getPacketHandler().write(animationPacket), componentAccessor
         );
      } else {
         PlayerUtil.forEachPlayerThatCanSeeEntity(
            ref, (playerRef, playerRefComponent, ca) -> playerRefComponent.getPacketHandler().write(animationPacket), ref, componentAccessor
         );
      }
   }

   public static void playAnimation(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull AnimationSlot animationSlot,
      @Nullable String animationId,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      playAnimation(ref, animationSlot, animationId, false, componentAccessor);
   }
}
