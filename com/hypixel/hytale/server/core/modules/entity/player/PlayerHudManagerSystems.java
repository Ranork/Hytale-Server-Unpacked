package com.hypixel.hytale.server.core.modules.entity.player;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.HudManager;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class PlayerHudManagerSystems {
   public static class InitializeSystem extends RefSystem<EntityStore> {
      @Nonnull
      private static final ComponentType<EntityStore, PlayerRef> PLAYER_REF_COMPONENT_TYPE = PlayerRef.getComponentType();
      @Nonnull
      private static final ComponentType<EntityStore, Player> PLAYER_COMPONENT_TYPE = Player.getComponentType();
      @Nonnull
      private static final Query<EntityStore> QUERY = Query.and(PLAYER_REF_COMPONENT_TYPE, PLAYER_COMPONENT_TYPE);

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return QUERY;
      }

      @Override
      public void onEntityAdded(
         @Nonnull Ref<EntityStore> ref, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         Player playerComponent = commandBuffer.getComponent(ref, PLAYER_COMPONENT_TYPE);

         assert playerComponent != null;

         PlayerRef playerRefComponent = commandBuffer.getComponent(ref, PLAYER_REF_COMPONENT_TYPE);

         assert playerRefComponent != null;

         HudManager hudManager = playerComponent.getHudManager();
         PacketHandler packetHandler = playerRefComponent.getPacketHandler();
         hudManager.sendVisibleHudComponents(packetHandler);
      }

      @Override
      public void onEntityRemove(
         @Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }
   }
}
