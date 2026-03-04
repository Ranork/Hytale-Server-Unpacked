package com.hypixel.hytale.server.core.modules.entity.damage;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RespawnSystems {
   public static class CheckBrokenItemsRespawnSystem extends RespawnSystems.OnRespawnSystem {
      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return Player.getComponentType();
      }

      public void onComponentRemoved(
         @Nonnull Ref<EntityStore> ref, @Nonnull DeathComponent component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         Player playerComponent = commandBuffer.getComponent(ref, Player.getComponentType());

         assert playerComponent != null;

         if (playerComponent.getInventory().containsBrokenItem()) {
            playerComponent.sendMessage(Message.translation("server.general.repair.itemBrokenOnRespawn").color("#ff5555"));
         }
      }
   }

   public static class ClearEntityEffectsRespawnSystem extends RespawnSystems.OnRespawnSystem {
      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return EffectControllerComponent.getComponentType();
      }

      public void onComponentRemoved(
         @Nonnull Ref<EntityStore> ref, @Nonnull DeathComponent component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         EffectControllerComponent effectControllerComponent = commandBuffer.getComponent(ref, EffectControllerComponent.getComponentType());

         assert effectControllerComponent != null;

         effectControllerComponent.clearEffects(ref, commandBuffer);
      }
   }

   public static class ClearInteractionsRespawnSystem extends RespawnSystems.OnRespawnSystem {
      @Override
      public Query<EntityStore> getQuery() {
         return InteractionModule.get().getInteractionManagerComponent();
      }

      public void onComponentRemoved(
         @Nonnull Ref<EntityStore> ref, @Nonnull DeathComponent component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         InteractionManager interactionManagerComponent = store.getComponent(ref, InteractionModule.get().getInteractionManagerComponent());
         interactionManagerComponent.clear();
      }
   }

   public static class ClearRespawnUI extends RespawnSystems.OnRespawnSystem {
      public void onComponentRemoved(
         @Nonnull Ref<EntityStore> ref, @Nonnull DeathComponent component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         Player playerComponent = commandBuffer.getComponent(ref, Player.getComponentType());

         assert playerComponent != null;

         playerComponent.getPageManager().setPage(ref, store, Page.None);
      }

      @Nullable
      @Override
      public Query<EntityStore> getQuery() {
         return Player.getComponentType();
      }
   }

   public abstract static class OnRespawnSystem extends RefChangeSystem<EntityStore, DeathComponent> {
      @Nonnull
      @Override
      public ComponentType<EntityStore, DeathComponent> componentType() {
         return DeathComponent.getComponentType();
      }

      public void onComponentAdded(
         @Nonnull Ref<EntityStore> ref, @Nonnull DeathComponent component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }

      public void onComponentSet(
         @Nonnull Ref<EntityStore> ref,
         DeathComponent oldComponent,
         @Nonnull DeathComponent newComponent,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }
   }

   public static class ResetPlayerRespawnSystem extends RespawnSystems.OnRespawnSystem {
      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return Player.getComponentType();
      }

      public void onComponentRemoved(
         @Nonnull Ref<EntityStore> ref, @Nonnull DeathComponent component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         Player playerComponent = store.getComponent(ref, Player.getComponentType());

         assert playerComponent != null;

         playerComponent.setLastSpawnTimeNanos(System.nanoTime());
      }
   }

   public static class ResetStatsRespawnSystem extends RespawnSystems.OnRespawnSystem {
      @Nonnull
      private static final Query<EntityStore> QUERY = Archetype.of(Player.getComponentType(), EntityStatMap.getComponentType());

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return QUERY;
      }

      public void onComponentRemoved(
         @Nonnull Ref<EntityStore> ref, @Nonnull DeathComponent component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         EntityStatMap entityStatMapComponent = store.getComponent(ref, EntityStatMap.getComponentType());

         assert entityStatMapComponent != null;

         for (int index = 0; index < entityStatMapComponent.size(); index++) {
            EntityStatValue value = entityStatMapComponent.get(index);
            if (value != null) {
               entityStatMapComponent.resetStatValue(index);
            }
         }
      }
   }
}
