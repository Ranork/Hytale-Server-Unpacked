package com.hypixel.hytale.builtin.buildertools.prefabeditor;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PrefabSelectionInteraction extends SimpleInstantInteraction {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_EDIT_PREFAB_SELECT_ERROR_NO_TARGET_FOUND = Message.translation(
      "server.commands.editprefab.select.error.noTargetFound"
   );
   @Nonnull
   private static final Message MESSAGE_COMMANDS_EDIT_PREFAB_SELECT_ERROR_NO_PREFAB_FOUND = Message.translation(
      "server.commands.editprefab.select.error.noPrefabFound"
   );
   @Nonnull
   private static final Message MESSAGE_COMMANDS_EDIT_PREFAB_NOT_IN_EDIT_SESSION = Message.translation("server.commands.editprefab.notInEditSession");
   private static final float ENTITY_TARGET_RADIUS = 50.0F;
   @Nonnull
   public static final BuilderCodec<PrefabSelectionInteraction> CODEC = BuilderCodec.builder(
         PrefabSelectionInteraction.class, PrefabSelectionInteraction::new, SimpleInstantInteraction.CODEC
      )
      .documentation("Interaction that handles the selection functionally for the prefab selection tool.")
      .build();

   @Override
   protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
      CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();

      assert commandBuffer != null;

      Ref<EntityStore> ref = context.getEntity();
      Player playerComponent = commandBuffer.getComponent(ref, Player.getComponentType());
      if (playerComponent != null) {
         if (type == InteractionType.Primary || type == InteractionType.Secondary) {
            UUIDComponent uuidComponent = commandBuffer.getComponent(ref, UUIDComponent.getComponentType());

            assert uuidComponent != null;

            UUID uuid = uuidComponent.getUuid();
            PrefabEditSessionManager prefabEditSessionManager = BuilderToolsPlugin.get().getPrefabEditSessionManager();
            PrefabEditSession prefabEditSession = prefabEditSessionManager.getPrefabEditSession(uuid);
            if (prefabEditSession == null) {
               playerComponent.sendMessage(MESSAGE_COMMANDS_EDIT_PREFAB_NOT_IN_EDIT_SESSION);
            } else {
               TransformComponent transformComponent = commandBuffer.getComponent(ref, TransformComponent.getComponentType());

               assert transformComponent != null;

               Vector3d playerPosition = transformComponent.getPosition();
               PrefabEditingMetadata prefabEditingMetadata = null;
               if (type == InteractionType.Secondary) {
                  Vector3d playerLocation = playerPosition.clone();
                  playerLocation.setY(0.0);
                  double distance = 2.147483647E9;

                  for (PrefabEditingMetadata value : prefabEditSession.getLoadedPrefabMetadata().values()) {
                     Vector3d centerPoint = new Vector3d(
                        (value.getMaxPoint().x + value.getMinPoint().x) / 2.0, 0.0, (value.getMaxPoint().z + value.getMinPoint().z) / 2.0
                     );
                     double distanceTo = centerPoint.distanceTo(playerLocation);
                     if (distance > distanceTo) {
                        distance = distanceTo;
                        prefabEditingMetadata = value;
                     }
                  }
               } else {
                  Vector3i targetLocation = getTargetLocation(ref, commandBuffer);
                  if (targetLocation == null) {
                     playerComponent.sendMessage(MESSAGE_COMMANDS_EDIT_PREFAB_SELECT_ERROR_NO_TARGET_FOUND);
                     return;
                  }

                  for (PrefabEditingMetadata valuex : prefabEditSession.getLoadedPrefabMetadata().values()) {
                     boolean isWithinPrefab = valuex.isLocationWithinPrefabBoundingBox(targetLocation);
                     if (isWithinPrefab) {
                        prefabEditingMetadata = valuex;
                        break;
                     }
                  }
               }

               if (prefabEditingMetadata == null) {
                  playerComponent.sendMessage(MESSAGE_COMMANDS_EDIT_PREFAB_SELECT_ERROR_NO_PREFAB_FOUND);
               } else {
                  prefabEditSession.setSelectedPrefab(ref, prefabEditingMetadata, commandBuffer);
               }
            }
         }
      }
   }

   @Nullable
   private static Vector3i getTargetLocation(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      Vector3i targetBlock = TargetUtil.getTargetBlock(ref, 200.0, componentAccessor);
      if (targetBlock != null) {
         return targetBlock;
      } else {
         Ref<EntityStore> targetEntityRef = TargetUtil.getTargetEntity(ref, 50.0F, componentAccessor);
         if (targetEntityRef != null && targetEntityRef.isValid()) {
            TransformComponent entityTransformComponent = componentAccessor.getComponent(targetEntityRef, TransformComponent.getComponentType());
            return entityTransformComponent == null ? null : entityTransformComponent.getPosition().toVector3i();
         } else {
            return null;
         }
      }
   }
}
