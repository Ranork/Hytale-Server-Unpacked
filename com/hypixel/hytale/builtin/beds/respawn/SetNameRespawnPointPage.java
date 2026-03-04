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
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.meta.state.RespawnBlock;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class SetNameRespawnPointPage extends RespawnPointPage {
   @Nonnull
   private final Vector3i respawnBlockPosition;
   @Nonnull
   private final RespawnBlock respawnBlock;

   public SetNameRespawnPointPage(
      @Nonnull PlayerRef playerRef, @Nonnull InteractionType interactionType, @Nonnull Vector3i respawnBlockPosition, @Nonnull RespawnBlock respawnBlock
   ) {
      super(playerRef, interactionType);
      this.respawnBlockPosition = respawnBlockPosition;
      this.respawnBlock = respawnBlock;
   }

   @Override
   public void build(
      @Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store
   ) {
      commandBuilder.append("Pages/NameRespawnPointPage.ui");
      Player playerComponent = store.getComponent(ref, Player.getComponentType());
      if (playerComponent != null) {
         PlayerRef playerRefComponent = store.getComponent(ref, PlayerRef.getComponentType());
         if (playerRefComponent != null) {
            World world = store.getExternalData().getWorld();
            PlayerRespawnPointData[] respawnPoints = playerComponent.getPlayerConfigData().getPerWorldData(world.getName()).getRespawnPoints();
            String respawnPointName = null;
            if (respawnPoints != null) {
               for (PlayerRespawnPointData respawnPoint : respawnPoints) {
                  if (respawnPoint.getBlockPosition().equals(this.respawnBlockPosition)) {
                     respawnPointName = respawnPoint.getName();
                     break;
                  }
               }
            }

            if (respawnPointName == null) {
               commandBuilder.set(
                  "#NameInput.Value", Message.translation("server.customUI.defaultRespawnPointName").param("name", playerRefComponent.getUsername())
               );
            } else {
               commandBuilder.set("#NameInput.Value", respawnPointName);
            }

            eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#SetButton", EventData.of("@RespawnPointName", "#NameInput.Value"));
            eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CancelButton", EventData.of("Action", "Cancel"));
         }
      }
   }

   public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull RespawnPointPage.RespawnPointEventData data) {
      String respawnPointName = data.getRespawnPointName();
      if (respawnPointName != null) {
         this.setRespawnPointForPlayer(ref, store, this.respawnBlockPosition, this.respawnBlock, respawnPointName);
      } else if ("Cancel".equals(data.getAction())) {
         Player playerComponent = store.getComponent(ref, Player.getComponentType());
         if (playerComponent != null) {
            playerComponent.getPageManager().setPage(ref, store, Page.None);
         }
      }
   }
}
