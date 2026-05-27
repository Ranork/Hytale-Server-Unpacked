package com.hypixel.hytale.server.core.modules.interaction.interaction.util;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSettings;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.InteractionConfiguration;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3i;

public class InteractionValidation {
   private static final float MAX_INTERACTION_DISTANCE_BUFFER = 2.0F;
   private static final float EXTENDED_CREATIVE_BLOCK_INTERACTION_DISTANCE = 10.0F;
   private static final float MIN_CREATIVE_BLOCK_INTERACTION_DISTANCE = 0.0F;
   private static final float MAX_CREATIVE_BLOCK_INTERACTION_DISTANCE = 128.0F;

   private static float getPlayerInteractionDistanceSq(
      @Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor, @Nullable ItemStack heldItem
   ) {
      Player playerComponent = componentAccessor.getComponent(ref, Player.getComponentType());
      GameMode gameMode = playerComponent.getGameMode();
      InteractionConfiguration interactionConfig = heldItem != null ? heldItem.getItem().getInteractionConfig() : InteractionConfiguration.DEFAULT;
      float maxDistance = interactionConfig.getUseDistance(gameMode);
      if (gameMode == GameMode.Creative) {
         float creativeDistance = 10.0F;
         PlayerSettings settingsComponent = componentAccessor.getComponent(ref, PlayerSettings.getComponentType());
         if (settingsComponent != null) {
            int clientCreativeDistance = settingsComponent.creativeSettings().creativeInteractionDistance();
            creativeDistance = MathUtil.clamp((float)clientCreativeDistance, 0.0F, 128.0F);
         }

         maxDistance = Math.max(maxDistance, creativeDistance);
      }

      maxDistance += 2.0F;
      return maxDistance * maxDistance;
   }

   private static float getEyeHeight(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      ModelComponent modelComponent = componentAccessor.getComponent(ref, ModelComponent.getComponentType());
      return modelComponent == null ? 0.0F : modelComponent.getModel().getEyeHeight(ref, componentAccessor);
   }

   public static boolean canPlayerInteractWithEntity(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor,
      @Nullable ItemStack heldItem,
      @Nonnull Ref<EntityStore> targetRef
   ) {
      Player playerComponent = componentAccessor.getComponent(ref, Player.getComponentType());
      if (playerComponent == null) {
         return true;
      } else {
         TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());
         if (transformComponent == null) {
            return false;
         } else {
            TransformComponent targetTransformComponent = componentAccessor.getComponent(targetRef, TransformComponent.getComponentType());
            if (targetTransformComponent == null) {
               return false;
            } else {
               float maxDistanceSq = getPlayerInteractionDistanceSq(ref, componentAccessor, heldItem);
               float eyeHeight = getEyeHeight(ref, componentAccessor);
               Vector3d position = transformComponent.getPosition();
               Vector3d targetPosition = targetTransformComponent.getPosition();
               double distanceSq = targetPosition.distanceSquared(position.x(), position.y() + eyeHeight, position.z());
               return distanceSq <= maxDistanceSq;
            }
         }
      }
   }

   public static boolean canPlayerInteractWithBlock(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor,
      @Nullable ItemStack heldItem,
      int blockX,
      int blockY,
      int blockZ
   ) {
      Player playerComponent = componentAccessor.getComponent(ref, Player.getComponentType());
      if (playerComponent == null) {
         return true;
      } else {
         TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());
         if (transformComponent == null) {
            return false;
         } else {
            float maxDistanceSq = getPlayerInteractionDistanceSq(ref, componentAccessor, heldItem);
            float eyeHeight = getEyeHeight(ref, componentAccessor);
            Vector3d position = transformComponent.getPosition();
            double dx = blockX + 0.5 - position.x();
            double dy = blockY + 0.5 - (position.y() + eyeHeight);
            double dz = blockZ + 0.5 - position.z();
            double distanceSq = dx * dx + dy * dy + dz * dz;
            return distanceSq <= maxDistanceSq;
         }
      }
   }

   public static boolean canPlayerInteractWithBlock(
      @Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor, @Nullable ItemStack heldItem, @Nonnull Vector3i blockPosition
   ) {
      return canPlayerInteractWithBlock(ref, componentAccessor, heldItem, blockPosition.x, blockPosition.y, blockPosition.z);
   }

   public static boolean canPlayerInteractWithBlock(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor,
      @Nullable ItemStack heldItem,
      @Nonnull BlockPosition blockPosition
   ) {
      return canPlayerInteractWithBlock(ref, componentAccessor, heldItem, blockPosition.x, blockPosition.y, blockPosition.z);
   }
}
