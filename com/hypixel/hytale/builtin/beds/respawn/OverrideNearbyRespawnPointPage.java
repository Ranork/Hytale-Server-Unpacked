package com.hypixel.hytale.builtin.beds.respawn;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerRespawnPointData;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.meta.state.RespawnBlock;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class OverrideNearbyRespawnPointPage extends RespawnPointPage {
   @Nonnull
   private static final String PAGE_OVERRIDE_NEARBY_SPAWN_POINT_PAGE = "Pages/OverrideNearbyRespawnPointPage.ui";
   @Nonnull
   private final Vector3i respawnPointPosition;
   @Nonnull
   private final RespawnBlock respawnPointToAdd;
   @Nonnull
   private final PlayerRespawnPointData[] nearbyRespawnPoints;
   private final int radiusLimitRespawnPoint;

   public OverrideNearbyRespawnPointPage(
      @Nonnull PlayerRef playerRef,
      @Nonnull InteractionType interactionType,
      @Nonnull Vector3i respawnPointPosition,
      @Nonnull RespawnBlock respawnPointToAdd,
      @Nonnull PlayerRespawnPointData[] nearbyRespawnPoints,
      int radiusLimitRespawnPoint
   ) {
      super(playerRef, interactionType);
      this.respawnPointPosition = respawnPointPosition;
      this.respawnPointToAdd = respawnPointToAdd;
      this.nearbyRespawnPoints = nearbyRespawnPoints;
      this.radiusLimitRespawnPoint = radiusLimitRespawnPoint;
   }

   @Override
   public void build(
      @Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store
   ) {
      commandBuilder.append("Pages/OverrideNearbyRespawnPointPage.ui");
      HeadRotation headRotationComponent = store.getComponent(ref, HeadRotation.getComponentType());
      if (headRotationComponent != null) {
         PlayerRef playerRefComponent = store.getComponent(ref, PlayerRef.getComponentType());
         if (playerRefComponent != null) {
            double direction = Math.toDegrees(headRotationComponent.getRotation().getYaw());
            commandBuilder.set(
               "#DescriptionLabel.Text",
               Message.translation("server.customUI.overrideNearbyRespawnPoint.label")
                  .param("respawnPointCount", this.nearbyRespawnPoints.length)
                  .param("minDistance", this.radiusLimitRespawnPoint)
            );

            for (int i = 0; i < this.nearbyRespawnPoints.length; i++) {
               String selector = "#RespawnPointList[" + i + "]";
               PlayerRespawnPointData nearbyRespawnPoint = this.nearbyRespawnPoints[i];
               commandBuilder.append("#RespawnPointList", "Pages/OverrideRespawnPointButton.ui");
               commandBuilder.set(selector + ".Disabled", true);
               commandBuilder.set(selector + " #Name.Text", nearbyRespawnPoint.getName());
               Vector3i nearbyRespawnPointPosition = nearbyRespawnPoint.getBlockPosition();
               int distance = (int)this.respawnPointPosition
                  .distanceTo(nearbyRespawnPointPosition.x, this.respawnPointPosition.y, nearbyRespawnPointPosition.z);
               commandBuilder.set(selector + " #Distance.Text", Message.translation("server.customUI.respawnPointDistance").param("distance", distance));
               double angle = Math.atan2(nearbyRespawnPointPosition.z - this.respawnPointPosition.z, nearbyRespawnPointPosition.x - this.respawnPointPosition.x);
               commandBuilder.set(selector + " #Icon.Angle", Math.toDegrees(angle) + direction + 90.0);
            }

            commandBuilder.set(
               "#NameInput.Value", Message.translation("server.customUI.defaultRespawnPointName").param("name", playerRefComponent.getUsername())
            );
            eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#ConfirmButton", EventData.of("@RespawnPointName", "#NameInput.Value"));
            eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CancelButton", EventData.of("Action", "Cancel"));
         }
      }
   }

   public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull RespawnPointPage.RespawnPointEventData data) {
      String respawnPointName = data.getRespawnPointName();
      if (respawnPointName != null) {
         this.setRespawnPointForPlayer(ref, store, this.respawnPointPosition, this.respawnPointToAdd, respawnPointName, this.nearbyRespawnPoints);
      } else if ("Cancel".equals(data.getAction())) {
         Player playerComponent = store.getComponent(ref, Player.getComponentType());
         if (playerComponent != null) {
            playerComponent.getPageManager().setPage(ref, store, Page.None);
         }
      }
   }
}
