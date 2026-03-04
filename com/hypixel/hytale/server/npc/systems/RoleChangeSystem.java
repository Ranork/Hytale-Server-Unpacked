package com.hypixel.hytale.server.npc.systems;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.entity.repulsion.Repulsion;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.components.Timers;
import com.hypixel.hytale.server.npc.components.messaging.BeaconSupport;
import com.hypixel.hytale.server.npc.components.messaging.NPCBlockEventSupport;
import com.hypixel.hytale.server.npc.components.messaging.NPCEntityEventSupport;
import com.hypixel.hytale.server.npc.components.messaging.PlayerBlockEventSupport;
import com.hypixel.hytale.server.npc.components.messaging.PlayerEntityEventSupport;
import com.hypixel.hytale.server.npc.decisionmaker.stateevaluator.StateEvaluator;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.valuestore.ValueStore;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RoleChangeSystem extends TickingSystem<EntityStore> {
   @Nonnull
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   @Nonnull
   private final ResourceType<EntityStore, RoleChangeSystem.RoleChangeQueue> roleChangeQueueResourceType;
   @Nonnull
   private final ComponentType<EntityStore, BeaconSupport> beaconSupportComponentType;
   @Nonnull
   private final ComponentType<EntityStore, PlayerBlockEventSupport> playerBlockEventSupportComponentType;
   @Nonnull
   private final ComponentType<EntityStore, NPCBlockEventSupport> npcBlockEventSupportComponentType;
   @Nonnull
   private final ComponentType<EntityStore, PlayerEntityEventSupport> playerEntityEventSupportComponentType;
   @Nonnull
   private final ComponentType<EntityStore, NPCEntityEventSupport> npcEntityEventSupportComponentType;
   @Nonnull
   private final ComponentType<EntityStore, Timers> timersComponentType;
   @Nonnull
   private final ComponentType<EntityStore, StateEvaluator> stateEvaluatorComponentType;
   @Nonnull
   private final ComponentType<EntityStore, ValueStore> valueStoreComponentType;
   @Nonnull
   private final Set<Dependency<EntityStore>> dependencies = Set.of(new SystemDependency<>(Order.AFTER, NewSpawnStartTickingSystem.class));

   public RoleChangeSystem(
      @Nonnull ResourceType<EntityStore, RoleChangeSystem.RoleChangeQueue> roleChangeQueueResourceType,
      @Nonnull ComponentType<EntityStore, BeaconSupport> beaconSupportComponentType,
      @Nonnull ComponentType<EntityStore, PlayerBlockEventSupport> playerBlockEventSupportComponentType,
      @Nonnull ComponentType<EntityStore, NPCBlockEventSupport> npcBlockEventSupportComponentType,
      @Nonnull ComponentType<EntityStore, PlayerEntityEventSupport> playerEntityEventSupportComponentType,
      @Nonnull ComponentType<EntityStore, NPCEntityEventSupport> npcEntityEventSupportComponentType,
      @Nonnull ComponentType<EntityStore, Timers> timersComponentType,
      @Nonnull ComponentType<EntityStore, StateEvaluator> stateEvaluatorComponentType,
      @Nonnull ComponentType<EntityStore, ValueStore> valueStoreComponentType
   ) {
      this.roleChangeQueueResourceType = roleChangeQueueResourceType;
      this.beaconSupportComponentType = beaconSupportComponentType;
      this.playerBlockEventSupportComponentType = playerBlockEventSupportComponentType;
      this.npcBlockEventSupportComponentType = npcBlockEventSupportComponentType;
      this.playerEntityEventSupportComponentType = playerEntityEventSupportComponentType;
      this.npcEntityEventSupportComponentType = npcEntityEventSupportComponentType;
      this.timersComponentType = timersComponentType;
      this.stateEvaluatorComponentType = stateEvaluatorComponentType;
      this.valueStoreComponentType = valueStoreComponentType;
   }

   @Nonnull
   @Override
   public Set<Dependency<EntityStore>> getDependencies() {
      return this.dependencies;
   }

   @Override
   public void tick(float dt, int systemIndex, @Nonnull Store<EntityStore> store) {
      RoleChangeSystem.RoleChangeQueue roleChangeQueueResource = store.getResource(this.roleChangeQueueResourceType);
      Deque<RoleChangeSystem.RoleChangeRequest> requests = roleChangeQueueResource.requests;

      while (true) {
         RoleChangeSystem.RoleChangeRequest request;
         NPCEntity npcComponent;
         Ref<EntityStore> npcEntityReference;
         while (true) {
            if (requests.isEmpty()) {
               return;
            }

            request = requests.poll();
            if (request.reference.isValid()) {
               Holder<EntityStore> holder = store.removeEntity(request.reference, RemoveReason.UNLOAD);
               npcComponent = holder.getComponent(NPCEntity.getComponentType());

               assert npcComponent != null;

               npcComponent.setRole(null);
               holder.tryRemoveComponent(this.beaconSupportComponentType);
               holder.tryRemoveComponent(this.playerBlockEventSupportComponentType);
               holder.tryRemoveComponent(this.npcBlockEventSupportComponentType);
               holder.tryRemoveComponent(this.playerEntityEventSupportComponentType);
               holder.tryRemoveComponent(this.npcEntityEventSupportComponentType);
               holder.tryRemoveComponent(this.timersComponentType);
               holder.tryRemoveComponent(this.stateEvaluatorComponentType);
               holder.tryRemoveComponent(this.valueStoreComponentType);
               holder.tryRemoveComponent(Repulsion.getComponentType());
               npcComponent.setRoleName(NPCPlugin.get().getName(request.roleIndex));
               npcComponent.setRoleIndex(request.roleIndex);

               try {
                  npcEntityReference = store.addEntity(holder, AddReason.LOAD);
                  break;
               } catch (Exception var11) {
                  LOGGER.at(Level.SEVERE).log("Failed to change role: %s", var11.getMessage());
               }
            }
         }

         if (npcEntityReference != null && npcEntityReference.isValid()) {
            if (request.changeAppearance) {
               Role role = npcComponent.getRole();
               NPCEntity.setAppearance(npcEntityReference, role.getAppearanceName(), store);
            }

            if (request.state != null) {
               Role role = npcComponent.getRole();
               if (role != null) {
                  role.getStateSupport().setState(npcEntityReference, request.state, request.subState, store);
               }
            }
         } else {
            LOGGER.at(Level.SEVERE).log("Failed to change role: Could not re-add NPC entity to store");
         }
      }
   }

   public static void requestRoleChange(
      @Nonnull Ref<EntityStore> ref, @Nonnull Role role, int roleIndex, boolean changeAppearance, @Nonnull Store<EntityStore> store
   ) {
      requestRoleChange(ref, role, roleIndex, changeAppearance, null, null, store);
   }

   public static void requestRoleChange(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Role role,
      int roleIndex,
      boolean changeAppearance,
      @Nullable String state,
      @Nullable String subState,
      @Nonnull ComponentAccessor<EntityStore> store
   ) {
      RoleChangeSystem.RoleChangeQueue roleChangeResource = store.getResource(RoleChangeSystem.RoleChangeQueue.getResourceType());
      Deque<RoleChangeSystem.RoleChangeRequest> queue = roleChangeResource.requests;
      queue.add(new RoleChangeSystem.RoleChangeRequest(ref, roleIndex, changeAppearance, state, subState));
      role.setRoleChangeRequested();
   }

   public static class RoleChangeQueue implements Resource<EntityStore> {
      @Nonnull
      private final Deque<RoleChangeSystem.RoleChangeRequest> requests = new ArrayDeque<>();

      @Nonnull
      public static ResourceType<EntityStore, RoleChangeSystem.RoleChangeQueue> getResourceType() {
         return NPCPlugin.get().getRoleChangeQueueResourceType();
      }

      @Nonnull
      @Override
      public Resource<EntityStore> clone() {
         RoleChangeSystem.RoleChangeQueue roleChangeQueue = new RoleChangeSystem.RoleChangeQueue();
         roleChangeQueue.requests.addAll(this.requests);
         return roleChangeQueue;
      }
   }

   private static class RoleChangeRequest {
      @Nonnull
      private final Ref<EntityStore> reference;
      private final int roleIndex;
      private final boolean changeAppearance;
      @Nullable
      private final String state;
      @Nullable
      private final String subState;

      private RoleChangeRequest(@Nonnull Ref<EntityStore> reference, int roleIndex, boolean changeAppearance, @Nullable String state, @Nullable String subState) {
         this.reference = reference;
         this.roleIndex = roleIndex;
         this.changeAppearance = changeAppearance;
         this.state = state;
         this.subState = subState;
      }
   }
}
