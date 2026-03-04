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
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.meta.state.RespawnBlock;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class SelectOverrideRespawnPointPage extends RespawnPointPage {
   @Nonnull
   private static final Message MESSAGE_SERVER_CUSTOM_UI_NEED_TO_SELECT_RESPAWN_POINT = Message.translation("server.customUI.needToSelectRespawnPoint");
   @Nonnull
   private static final Value<String> DEFAULT_RESPAWN_BUTTON_STYLE = Value.ref("Pages/OverrideRespawnPointButton.ui", "DefaultRespawnButtonStyle");
   @Nonnull
   private static final Value<String> SELECTED_RESPAWN_BUTTON_STYLE = Value.ref("Pages/OverrideRespawnPointButton.ui", "SelectedRespawnButtonStyle");
   @Nonnull
   private static final String PAGE_SELECT_OVERRIDE_RESPAWN_POINT_PAGE = "Pages/SelectOverrideRespawnPointPage.ui";
   @Nonnull
   private final Vector3i respawnPointToAddPosition;
   @Nonnull
   private final RespawnBlock respawnPointToAdd;
   @Nonnull
   private final PlayerRespawnPointData[] respawnPoints;
   private int selectedRespawnPointIndex = -1;

   public SelectOverrideRespawnPointPage(
      @Nonnull PlayerRef playerRef,
      @Nonnull InteractionType interactionType,
      @Nonnull Vector3i respawnPointToAddPosition,
      @Nonnull RespawnBlock respawnPointToAdd,
      @Nonnull PlayerRespawnPointData[] respawnPoints
   ) {
      super(playerRef, interactionType);
      this.respawnPointToAddPosition = respawnPointToAddPosition;
      this.respawnPointToAdd = respawnPointToAdd;
      this.respawnPoints = respawnPoints;
   }

   @Override
   public void build(
      @Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store
   ) {
      commandBuilder.append("Pages/SelectOverrideRespawnPointPage.ui");
      commandBuilder.clear("#RespawnPointList");
      PlayerRef playerRefComponent = store.getComponent(ref, PlayerRef.getComponentType());
      if (playerRefComponent != null) {
         HeadRotation rotationComponent = store.getComponent(ref, HeadRotation.getComponentType());
         if (rotationComponent != null) {
            float lookYaw = rotationComponent.getRotation().getYaw();
            double direction = Math.toDegrees(lookYaw);

            for (int i = 0; i < this.respawnPoints.length; i++) {
               String selector = "#RespawnPointList[" + i + "]";
               PlayerRespawnPointData respawnPoint = this.respawnPoints[i];
               commandBuilder.append("#RespawnPointList", "Pages/OverrideRespawnPointButton.ui");
               commandBuilder.set(selector + " #Name.Text", respawnPoint.getName());
               Vector3i respawnPointPosition = respawnPoint.getBlockPosition();
               int distance = (int)this.respawnPointToAddPosition.distanceTo(respawnPointPosition.x, this.respawnPointToAddPosition.y, respawnPointPosition.z);
               commandBuilder.set(selector + " #Distance.Text", Message.translation("server.customUI.respawnPointDistance").param("distance", distance));
               double angle = Math.atan2(respawnPointPosition.z - this.respawnPointToAddPosition.z, respawnPointPosition.x - this.respawnPointToAddPosition.x);
               commandBuilder.set(selector + " #Icon.Angle", Math.toDegrees(angle) + direction + 90.0);
               eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, selector, EventData.of("Index", Integer.toString(i)), false);
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
      if (data.getIndex() != -1) {
         this.setSelectedRespawnPoint(data);
         this.sendUpdate();
      } else if (data.getRespawnPointName() != null) {
         if (this.selectedRespawnPointIndex == -1) {
            this.displayError(MESSAGE_SERVER_CUSTOM_UI_NEED_TO_SELECT_RESPAWN_POINT);
            return;
         }

         this.setRespawnPointForPlayer(
            ref, store, this.respawnPointToAddPosition, this.respawnPointToAdd, data.getRespawnPointName(), this.respawnPoints[this.selectedRespawnPointIndex]
         );
      } else if ("Cancel".equals(data.getAction())) {
         Player playerComponent = store.getComponent(ref, Player.getComponentType());
         if (playerComponent != null) {
            playerComponent.getPageManager().setPage(ref, store, Page.None);
         }
      }
   }

   private void setSelectedRespawnPoint(@Nonnull RespawnPointPage.RespawnPointEventData data) {
      this.selectedRespawnPointIndex = data.getIndex();
      UICommandBuilder commandBuilder = new UICommandBuilder();

      for (int i = 0; i < this.respawnPoints.length; i++) {
         commandBuilder.set("#RespawnPointList[" + i + "].Style", DEFAULT_RESPAWN_BUTTON_STYLE);
      }

      commandBuilder.set("#RespawnPointList[" + this.selectedRespawnPointIndex + "].Style", SELECTED_RESPAWN_BUTTON_STYLE);
      this.sendUpdate(commandBuilder, null, false);
   }
}
