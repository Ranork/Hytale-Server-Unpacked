package com.hypixel.hytale.server.core.universe.world;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.function.consumer.TriConsumer;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.cosmetics.CosmeticsModule;
import com.hypixel.hytale.server.core.entity.entities.player.HiddenPlayersManager;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSkinComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerUtil {
   public static void forEachPlayerThatCanSeeEntity(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull TriConsumer<Ref<EntityStore>, PlayerRef, ComponentAccessor<EntityStore>> consumer,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      Store<EntityStore> store = componentAccessor.getExternalData().getStore();
      ComponentType<EntityStore, PlayerRef> playerRefComponentType = PlayerRef.getComponentType();
      store.forEachChunk(playerRefComponentType, (archetypeChunk, commandBuffer) -> {
         for (int index = 0; index < archetypeChunk.size(); index++) {
            EntityTrackerSystems.EntityViewer entityViewerComponent = archetypeChunk.getComponent(index, EntityTrackerSystems.EntityViewer.getComponentType());
            if (entityViewerComponent != null && entityViewerComponent.visible.contains(ref)) {
               Ref<EntityStore> targetPlayerRef = archetypeChunk.getReferenceTo(index);
               PlayerRef targetPlayerRefComponent = archetypeChunk.getComponent(index, playerRefComponentType);

               assert targetPlayerRefComponent != null;

               consumer.accept(targetPlayerRef, targetPlayerRefComponent, commandBuffer);
            }
         }
      });
   }

   public static void forEachPlayerThatCanSeeEntity(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull TriConsumer<Ref<EntityStore>, PlayerRef, ComponentAccessor<EntityStore>> consumer,
      @Nullable Ref<EntityStore> ignoredPlayerRef,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      Store<EntityStore> store = componentAccessor.getExternalData().getStore();
      ComponentType<EntityStore, PlayerRef> playerRefComponentType = PlayerRef.getComponentType();
      store.forEachChunk(playerRefComponentType, (archetypeChunk, commandBuffer) -> {
         for (int index = 0; index < archetypeChunk.size(); index++) {
            EntityTrackerSystems.EntityViewer entityViewerComponent = archetypeChunk.getComponent(index, EntityTrackerSystems.EntityViewer.getComponentType());
            if (entityViewerComponent != null && entityViewerComponent.visible.contains(ref)) {
               Ref<EntityStore> targetRef = archetypeChunk.getReferenceTo(index);
               if (!targetRef.equals(ignoredPlayerRef)) {
                  PlayerRef targetPlayerRefComponent = archetypeChunk.getComponent(index, playerRefComponentType);

                  assert targetPlayerRefComponent != null;

                  consumer.accept(targetRef, targetPlayerRefComponent, commandBuffer);
               }
            }
         }
      });
   }

   public static void broadcastMessageToPlayers(@Nullable UUID sourcePlayerUuid, @Nonnull Message message, @Nonnull Store<EntityStore> store) {
      World world = store.getExternalData().getWorld();

      for (PlayerRef targetPlayerRef : world.getPlayerRefs()) {
         HiddenPlayersManager targetHiddenPlayersManager = targetPlayerRef.getHiddenPlayersManager();
         if (sourcePlayerUuid == null || !targetHiddenPlayersManager.isPlayerHidden(sourcePlayerUuid)) {
            targetPlayerRef.sendMessage(message);
         }
      }
   }

   public static void broadcastPacketToPlayers(@Nonnull ComponentAccessor<EntityStore> componentAccessor, @Nonnull ToClientPacket packet) {
      World world = componentAccessor.getExternalData().getWorld();

      for (PlayerRef targetPlayerRef : world.getPlayerRefs()) {
         targetPlayerRef.getPacketHandler().write(packet);
      }
   }

   public static void broadcastPacketToPlayersNoCache(@Nonnull ComponentAccessor<EntityStore> componentAccessor, @Nonnull ToClientPacket packet) {
      World world = componentAccessor.getExternalData().getWorld();

      for (PlayerRef targetPlayerRef : world.getPlayerRefs()) {
         targetPlayerRef.getPacketHandler().writeNoCache(packet);
      }
   }

   public static void broadcastPacketToPlayers(@Nonnull ComponentAccessor<EntityStore> componentAccessor, @Nonnull ToClientPacket... packets) {
      World world = componentAccessor.getExternalData().getWorld();

      for (PlayerRef targetPlayerRef : world.getPlayerRefs()) {
         targetPlayerRef.getPacketHandler().write(packets);
      }
   }

   @Deprecated(forRemoval = true)
   public static void resetPlayerModel(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      PlayerSkinComponent playerSkinComponent = componentAccessor.getComponent(ref, PlayerSkinComponent.getComponentType());
      if (playerSkinComponent != null) {
         playerSkinComponent.setNetworkOutdated();
         Model newModel = CosmeticsModule.get().createModel(playerSkinComponent.getPlayerSkin());
         if (newModel != null) {
            componentAccessor.putComponent(ref, ModelComponent.getComponentType(), new ModelComponent(newModel));
         }
      }
   }
}
