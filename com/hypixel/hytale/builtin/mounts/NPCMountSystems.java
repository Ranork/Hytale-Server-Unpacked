package com.hypixel.hytale.builtin.mounts;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.protocol.packets.interaction.MountNPC;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.Interactable;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.systems.RoleChangeSystem;
import javax.annotation.Nonnull;

public class NPCMountSystems {
   public static class DismountOnMountDeath extends DeathSystems.OnDeathSystem {
      @Nonnull
      private final ComponentType<EntityStore, NPCMountComponent> npcMountComponentType;

      public DismountOnMountDeath(@Nonnull ComponentType<EntityStore, NPCMountComponent> npcMountComponentType) {
         this.npcMountComponentType = npcMountComponentType;
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.npcMountComponentType;
      }

      public void onComponentAdded(
         @Nonnull Ref<EntityStore> ref, @Nonnull DeathComponent component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         NPCMountComponent mountComponent = store.getComponent(ref, this.npcMountComponentType);

         assert mountComponent != null;

         PlayerRef playerRef = mountComponent.getOwnerPlayerRef();
         if (playerRef != null) {
            Ref<EntityStore> playerEntityRef = playerRef.getReference();
            if (playerEntityRef != null && playerEntityRef.isValid()) {
               MountPlugin.resetOriginalPlayerMovementSettings(playerEntityRef, store);
            }
         }
      }
   }

   public static class DismountOnPlayerDeath extends DeathSystems.OnDeathSystem {
      @Nonnull
      private final ComponentType<EntityStore, Player> playerComponentType;

      public DismountOnPlayerDeath(@Nonnull ComponentType<EntityStore, Player> playerComponentType) {
         this.playerComponentType = playerComponentType;
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.playerComponentType;
      }

      public void onComponentAdded(
         @Nonnull Ref<EntityStore> ref, @Nonnull DeathComponent component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         Player playerComponent = store.getComponent(ref, this.playerComponentType);

         assert playerComponent != null;

         MountPlugin.checkDismountNpc(commandBuffer, ref, playerComponent);
      }
   }

   public static class OnAdd extends RefSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, NPCMountComponent> mountComponentType;
      @Nonnull
      private final ComponentType<EntityStore, NPCEntity> npcEntityComponentType;
      @Nonnull
      private final ComponentType<EntityStore, NetworkId> networkIdComponentType;
      @Nonnull
      private final Query<EntityStore> query;

      public OnAdd(
         @Nonnull ComponentType<EntityStore, NPCMountComponent> mountComponentType,
         @Nonnull ComponentType<EntityStore, NPCEntity> npcEntityComponentType,
         @Nonnull ComponentType<EntityStore, NetworkId> networkIdComponentType
      ) {
         this.mountComponentType = mountComponentType;
         this.npcEntityComponentType = npcEntityComponentType;
         this.networkIdComponentType = networkIdComponentType;
         this.query = Query.and(mountComponentType, npcEntityComponentType, networkIdComponentType);
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      @Override
      public void onEntityAdded(
         @Nonnull Ref<EntityStore> ref, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         NPCMountComponent mountComponent = store.getComponent(ref, this.mountComponentType);

         assert mountComponent != null;

         PlayerRef playerRef = mountComponent.getOwnerPlayerRef();
         if (playerRef == null) {
            this.resetOriginalRoleMount(ref, store, commandBuffer, mountComponent);
         } else {
            NPCEntity npcComponent = store.getComponent(ref, this.npcEntityComponentType);

            assert npcComponent != null;

            NetworkId networkIdComponent = store.getComponent(ref, this.networkIdComponentType);

            assert networkIdComponent != null;

            int networkId = networkIdComponent.getId();
            MountNPC packet = new MountNPC(mountComponent.getAnchorX(), mountComponent.getAnchorY(), mountComponent.getAnchorZ(), networkId);
            Player playerComponent = playerRef.getComponent(Player.getComponentType());
            if (playerComponent != null) {
               playerComponent.setMountEntityId(networkId);
               playerRef.getPacketHandler().write(packet);
               commandBuffer.removeComponent(ref, Interactable.getComponentType());
            }
         }
      }

      private void resetOriginalRoleMount(
         @Nonnull Ref<EntityStore> ref,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer,
         @Nonnull NPCMountComponent mountComponent
      ) {
         NPCEntity npcComponent = store.getComponent(ref, this.npcEntityComponentType);

         assert npcComponent != null;

         RoleChangeSystem.requestRoleChange(ref, npcComponent.getRole(), mountComponent.getOriginalRoleIndex(), false, "Idle", null, store);
         commandBuffer.removeComponent(ref, this.mountComponentType);
         commandBuffer.ensureComponent(ref, Interactable.getComponentType());
      }

      @Override
      public void onEntityRemove(
         @Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }
   }

   public static class OnPlayerRemove extends RefSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, Player> playerComponentType;

      public OnPlayerRemove(@Nonnull ComponentType<EntityStore, Player> playerComponentType) {
         this.playerComponentType = playerComponentType;
      }

      @Override
      public void onEntityAdded(
         @Nonnull Ref<EntityStore> ref, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }

      @Override
      public void onEntityRemove(
         @Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         Player player = commandBuffer.getComponent(ref, this.playerComponentType);

         assert player != null;

         MountPlugin.checkDismountNpc(commandBuffer, ref, player);
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.playerComponentType;
      }
   }
}
