package com.hypixel.hytale.server.flock;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.group.EntityGroup;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.FromWorldGen;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.RoleDebugFlags;
import java.util.EnumSet;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FlockMembershipSystems {
   public static boolean canJoinFlock(@Nonnull Ref<EntityStore> reference, @Nonnull Ref<EntityStore> flockReference, @Nonnull Store<EntityStore> store) {
      Flock flockComponent = store.getComponent(flockReference, Flock.getComponentType());

      assert flockComponent != null;

      PersistentFlockData flockData = flockComponent.getFlockData();
      if (flockData == null) {
         return false;
      } else {
         EntityGroup entityGroupComponent = store.getComponent(flockReference, EntityGroup.getComponentType());

         assert entityGroupComponent != null;

         if (entityGroupComponent.size() >= flockData.getMaxGrowSize()) {
            return false;
         } else {
            NPCEntity npcComponent = store.getComponent(reference, NPCEntity.getComponentType());
            if (npcComponent == null) {
               return false;
            } else {
               String roleName = npcComponent.getRoleName();
               return roleName != null && flockData.isFlockAllowedRole(roleName);
            }
         }
      }
   }

   public static void join(@Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> flockRef, @Nonnull Store<EntityStore> store) {
      FlockMembership membership = new FlockMembership();
      UUIDComponent uuidComponent = store.getComponent(flockRef, UUIDComponent.getComponentType());

      assert uuidComponent != null;

      membership.setFlockId(uuidComponent.getUuid());
      membership.setFlockRef(flockRef);
      membership.setMembershipType(FlockMembership.Type.JOINING);
      store.putComponent(ref, FlockMembership.getComponentType(), membership);
   }

   private static boolean canBecomeLeader(@Nonnull Ref<EntityStore> ref) {
      Store<EntityStore> store = ref.getStore();
      if (store.getComponent(ref, Player.getComponentType()) != null) {
         return true;
      } else {
         NPCEntity npcComponent = store.getComponent(ref, NPCEntity.getComponentType());
         return npcComponent != null && npcComponent.getRole() != null ? npcComponent.getRole().isCanLeadFlock() : false;
      }
   }

   private static void markChunkNeedsSaving(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
      TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());
      if (transformComponent != null) {
         transformComponent.markChunkDirty(store);
      }
   }

   private static void registerFlockDebugListener(
      @Nonnull Ref<EntityStore> ref, @Nonnull FlockMembership membership, @Nonnull Flock flock, @Nonnull ComponentAccessor<EntityStore> accessor
   ) {
      NPCEntity npcComponent = accessor.getComponent(ref, NPCEntity.getComponentType());
      if (npcComponent != null) {
         Role role = npcComponent.getRole();
         if (role != null) {
            membership.registerAsDebugListener(role.getDebugSupport(), flock);
         }
      }
   }

   private static void unregisterFlockDebugListener(
      @Nonnull Ref<EntityStore> ref, @Nonnull FlockMembership membership, @Nonnull Flock flock, @Nonnull ComponentAccessor<EntityStore> accessor
   ) {
      NPCEntity npcComponent = accessor.getComponent(ref, NPCEntity.getComponentType());
      if (npcComponent != null) {
         Role role = npcComponent.getRole();
         if (role != null) {
            membership.unregisterAsDebugListener(role.getDebugSupport(), flock);
         }
      }
   }

   public static class EntityRef extends RefSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, FlockMembership> flockMembershipComponentType;

      public EntityRef(@Nonnull ComponentType<EntityStore, FlockMembership> flockMembershipComponentType) {
         this.flockMembershipComponentType = flockMembershipComponentType;
      }

      @Override
      public Query<EntityStore> getQuery() {
         return this.flockMembershipComponentType;
      }

      @Override
      public void onEntityAdded(
         @Nonnull Ref<EntityStore> ref, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         commandBuffer.run(_store -> this.joinOrCreateFlock(ref, _store));
      }

      private void joinOrCreateFlock(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
         FlockMembership flockMembershipComponent = store.getComponent(ref, this.flockMembershipComponentType);

         assert flockMembershipComponent != null;

         UUID flockId = flockMembershipComponent.getFlockId();
         Ref<EntityStore> flockReference = store.getExternalData().getRefFromUUID(flockId);
         EntityGroup entityGroup;
         Flock flock;
         if (flockReference != null) {
            entityGroup = store.getComponent(flockReference, EntityGroup.getComponentType());

            assert entityGroup != null;

            flock = store.getComponent(flockReference, Flock.getComponentType());

            assert flock != null;
         } else {
            entityGroup = new EntityGroup();
            flock = new Flock();
            Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
            holder.addComponent(UUIDComponent.getComponentType(), new UUIDComponent(flockId));
            holder.addComponent(EntityGroup.getComponentType(), entityGroup);
            holder.addComponent(Flock.getComponentType(), flock);
            flockReference = store.addEntity(holder, AddReason.LOAD);
         }

         flockMembershipComponent.setFlockRef(flockReference);
         if (entityGroup.isMember(ref)) {
            throw new IllegalStateException(String.format("Entity %s attempting to reload into group with ID %s despite already being a member", ref, flockId));
         } else {
            entityGroup.add(ref);
            FlockMembershipSystems.registerFlockDebugListener(ref, flockMembershipComponent, flock, store);
            if (flockMembershipComponent.getMembershipType() == FlockMembership.Type.LEADER) {
               PersistentFlockData persistentFlockData = store.getComponent(ref, PersistentFlockData.getComponentType());
               if (persistentFlockData != null) {
                  flock.setFlockData(persistentFlockData);
               } else {
                  PersistentFlockData flockData = flock.getFlockData();
                  if (flockData != null) {
                     store.putComponent(ref, PersistentFlockData.getComponentType(), flockData);
                  }
               }

               Ref<EntityStore> oldLeaderRef = entityGroup.getLeaderRef();
               entityGroup.setLeaderRef(ref);
               if (oldLeaderRef != null && !oldLeaderRef.equals(ref)) {
                  FlockMembership oldLeaderComponent = store.getComponent(oldLeaderRef, this.flockMembershipComponentType);
                  if (oldLeaderComponent != null) {
                     oldLeaderComponent.setMembershipType(FlockMembership.Type.MEMBER);
                  }

                  store.tryRemoveComponent(oldLeaderRef, PersistentFlockData.getComponentType());
                  FlockMembershipSystems.markChunkNeedsSaving(oldLeaderRef, store);
               }

               markNeedsSave(ref, store, flock);
               if (flock.isTrace()) {
                  FlockPlugin.get()
                     .getLogger()
                     .at(Level.INFO)
                     .log("Flock %s: Set new leader, old=%s, new=%s, size=%s", flockId, oldLeaderRef, ref, entityGroup.size());
               }
            } else if (entityGroup.getLeaderRef() == null) {
               setInterimLeader(store, flockMembershipComponent, entityGroup, ref, flock, flockId);
            }

            if (flock.isTrace()) {
               FlockPlugin.get().getLogger().at(Level.INFO).log("Flock %s: reference=%s, size=%s", flockId, ref, entityGroup.size());
            }
         }
      }

      @Override
      public void onEntityRemove(
         @Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         FlockMembership membership = store.getComponent(ref, this.flockMembershipComponentType);

         assert membership != null;

         Ref<EntityStore> flockRef = membership.getFlockRef();
         if (flockRef != null && flockRef.isValid()) {
            Flock flockComponent = commandBuffer.getComponent(flockRef, Flock.getComponentType());

            assert flockComponent != null;

            EntityGroup entityGroupComponent = commandBuffer.getComponent(flockRef, EntityGroup.getComponentType());

            assert entityGroupComponent != null;

            UUIDComponent uuidComponent = commandBuffer.getComponent(flockRef, UUIDComponent.getComponentType());

            assert uuidComponent != null;

            UUID flockId = uuidComponent.getUuid();
            if (reason == RemoveReason.REMOVE || store.getArchetype(ref).contains(Player.getComponentType())) {
               FlockMembershipSystems.unregisterFlockDebugListener(ref, membership, flockComponent, store);
               entityGroupComponent.remove(ref);
               if (flockComponent.isTrace()) {
                  FlockPlugin.get()
                     .getLogger()
                     .at(Level.INFO)
                     .log(
                        "Flock %s: Left flock, reference=%s, leader=%s, size=%s",
                        flockId,
                        ref,
                        entityGroupComponent.getLeaderRef(),
                        entityGroupComponent.size()
                     );
               }

               if (!entityGroupComponent.isDissolved() && entityGroupComponent.size() < 2) {
                  commandBuffer.removeEntity(flockRef, RemoveReason.REMOVE);
                  return;
               }

               if (entityGroupComponent.isDissolved()) {
                  return;
               }

               PersistentFlockData flockData = flockComponent.getFlockData();
               if (flockData != null) {
                  flockData.decreaseSize();
               }

               Ref<EntityStore> leader = entityGroupComponent.getLeaderRef();
               if (leader != null && !leader.equals(ref)) {
                  FlockMembershipSystems.markChunkNeedsSaving(leader, store);
                  return;
               }

               Ref<EntityStore> newLeader = entityGroupComponent.testMembers(FlockMembershipSystems::canBecomeLeader, true);
               if (newLeader == null) {
                  if (flockComponent.isTrace()) {
                     FlockPlugin.get()
                        .getLogger()
                        .at(Level.INFO)
                        .log(
                           "Flock %s: Leave failed to get new leader, reference=%s, leader=%s, size=%s",
                           flockId,
                           ref,
                           entityGroupComponent.getLeaderRef(),
                           entityGroupComponent.size()
                        );
                  }

                  commandBuffer.removeEntity(flockRef, RemoveReason.REMOVE);
                  return;
               }

               entityGroupComponent.setLeaderRef(newLeader);
               FlockMembership flockMembershipComponent = store.getComponent(newLeader, this.flockMembershipComponentType);

               assert flockMembershipComponent != null;

               flockMembershipComponent.setMembershipType(FlockMembership.Type.LEADER);
               if (flockData != null) {
                  commandBuffer.putComponent(newLeader, PersistentFlockData.getComponentType(), flockData);
               }

               markNeedsSave(newLeader, store, flockComponent);
               if (flockComponent.isTrace()) {
                  FlockPlugin.get()
                     .getLogger()
                     .at(Level.INFO)
                     .log("Flock %s: Set new leader, old=%s, new=%s, size=%s", flockId, ref, newLeader, entityGroupComponent.size());
               }
            } else if (reason == RemoveReason.UNLOAD) {
               FlockMembershipSystems.unregisterFlockDebugListener(ref, membership, flockComponent, store);
               entityGroupComponent.remove(ref);
               if (!entityGroupComponent.isDissolved() && membership.getMembershipType().isActingAsLeader()) {
                  Ref<EntityStore> interimLeader = entityGroupComponent.testMembers(member -> true, true);
                  if (interimLeader != null) {
                     FlockMembership interimLeaderMembership = store.getComponent(interimLeader, this.flockMembershipComponentType);
                     if (interimLeaderMembership == null) {
                        throw new IllegalStateException("Member is missing FlockMembership component!");
                     }

                     setInterimLeader(store, interimLeaderMembership, entityGroupComponent, interimLeader, flockComponent, flockId);
                  }
               }

               membership.unload();
               if (entityGroupComponent.size() <= 0) {
                  commandBuffer.tryRemoveEntity(flockRef, RemoveReason.UNLOAD);
               }

               if (flockComponent.isTrace()) {
                  FlockPlugin.get()
                     .getLogger()
                     .at(Level.INFO)
                     .log(
                        "Flock %s: Unloaded from flock, reference=%s, leader=%s, size=%s",
                        flockId,
                        ref,
                        entityGroupComponent.getLeaderRef(),
                        entityGroupComponent.size()
                     );
               }
            }
         }
      }

      private static void markNeedsSave(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull Flock flockComponent) {
         NPCEntity npcComponent = store.getComponent(ref, NPCEntity.getComponentType());
         if (npcComponent != null) {
            Role role = npcComponent.getRole();
            if (role != null) {
               EnumSet<RoleDebugFlags> flags = role.getDebugSupport().getDebugFlags();
               flockComponent.setTrace(flags.contains(RoleDebugFlags.Flock));
            }
         }

         FlockMembershipSystems.markChunkNeedsSaving(ref, store);
      }

      private static void setInterimLeader(
         @Nonnull Store<EntityStore> store,
         @Nonnull FlockMembership interimLeaderMembership,
         @Nonnull EntityGroup entityGroup,
         Ref<EntityStore> interimLeader,
         @Nonnull Flock flockComponent,
         @Nonnull UUID flockId
      ) {
         interimLeaderMembership.setMembershipType(FlockMembership.Type.INTERIM_LEADER);
         entityGroup.setLeaderRef(interimLeader);
         markNeedsSave(interimLeader, store, flockComponent);
         if (flockComponent.isTrace()) {
            FlockPlugin.get()
               .getLogger()
               .at(Level.INFO)
               .log("Flock %s: Set new interim leader, old=%s, new=%s, size=%s", flockId, entityGroup.getLeaderRef(), interimLeader, entityGroup.size());
         }
      }
   }

   public static class FilterPlayerFlockDamageSystem extends DamageEventSystem {
      @Nonnull
      private final Query<EntityStore> query = Query.and(Player.getComponentType(), FlockMembership.getComponentType());

      @Nullable
      @Override
      public SystemGroup<EntityStore> getGroup() {
         return DamageModule.get().getFilterDamageGroup();
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      public void handle(
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer,
         @Nonnull Damage damage
      ) {
         FlockMembership flockMembership = archetypeChunk.getComponent(index, FlockMembership.getComponentType());

         assert flockMembership != null;

         if (damage.getSource() instanceof Damage.EntitySource entitySource) {
            Ref<EntityStore> flockRef = flockMembership.getFlockRef();
            if (flockRef != null && flockRef.isValid()) {
               Ref<EntityStore> sourceRef = entitySource.getRef();
               if (sourceRef.isValid()) {
                  EntityGroup group = store.getComponent(flockRef, EntityGroup.getComponentType());
                  if (group != null && group.isMember(sourceRef)) {
                     damage.setCancelled(true);
                  }
               }
            }
         }
      }
   }

   public static class NPCAddedFromWorldGen extends HolderSystem<EntityStore> {
      @Nullable
      private final ComponentType<EntityStore, NPCEntity> npcComponentType = NPCEntity.getComponentType();
      @Nonnull
      private final ComponentType<EntityStore, FromWorldGen> fromWorldGenComponentType = FromWorldGen.getComponentType();
      @Nonnull
      private final ComponentType<EntityStore, FlockMembership> flockMembershipComponentType = FlockMembership.getComponentType();
      @Nonnull
      private final Query<EntityStore> query = Query.and(this.npcComponentType, this.fromWorldGenComponentType, this.flockMembershipComponentType);

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      @Nullable
      @Override
      public SystemGroup<EntityStore> getGroup() {
         return EntityModule.get().getPreClearMarkersGroup();
      }

      @Override
      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         holder.removeComponent(this.flockMembershipComponentType);
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
      }
   }

   public static class OnDamageDealt extends DamageEventSystem {
      @Nullable
      @Override
      public SystemGroup<EntityStore> getGroup() {
         return DamageModule.get().getInspectDamageGroup();
      }

      @Nullable
      @Override
      public Query<EntityStore> getQuery() {
         return Archetype.empty();
      }

      public void handle(
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer,
         @Nonnull Damage damage
      ) {
         if (damage.getSource() instanceof Damage.EntitySource entitySource) {
            Ref<EntityStore> damageSourceRef = entitySource.getRef();
            if (damageSourceRef.isValid()) {
               FlockMembership flockMembershipComponent = commandBuffer.getComponent(damageSourceRef, FlockMembership.getComponentType());
               if (flockMembershipComponent != null) {
                  Ref<EntityStore> flockReference = flockMembershipComponent.getFlockRef();
                  if (flockReference != null && flockReference.isValid()) {
                     Flock flockComponent = commandBuffer.getComponent(flockReference, Flock.getComponentType());

                     assert flockComponent != null;

                     Ref<EntityStore> entityRef = archetypeChunk.getReferenceTo(index);
                     flockComponent.getNextDamageData().onInflictedDamage(entityRef, damage.getAmount());
                     if (flockMembershipComponent.getMembershipType().isActingAsLeader()) {
                        flockComponent.getNextLeaderDamageData().onInflictedDamage(entityRef, damage.getAmount());
                     }
                  }
               }
            }
         }
      }
   }

   public static class OnDamageReceived extends DamageEventSystem {
      @Nonnull
      private final Query<EntityStore> query = FlockMembership.getComponentType();

      @Nullable
      @Override
      public SystemGroup<EntityStore> getGroup() {
         return DamageModule.get().getInspectDamageGroup();
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      public void handle(
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer,
         @Nonnull Damage danage
      ) {
         FlockMembership flockMembershipComponent = archetypeChunk.getComponent(index, FlockMembership.getComponentType());
         if (flockMembershipComponent != null) {
            Ref<EntityStore> flockRef = flockMembershipComponent.getFlockRef();
            if (flockRef != null && flockRef.isValid()) {
               Flock flockComponent = commandBuffer.getComponent(flockRef, Flock.getComponentType());

               assert flockComponent != null;

               flockComponent.getNextDamageData().onSufferedDamage(commandBuffer, danage);
               if (flockMembershipComponent.getMembershipType().isActingAsLeader()) {
                  flockComponent.getNextLeaderDamageData().onSufferedDamage(commandBuffer, danage);
               }
            }
         }
      }
   }

   public static class RefChange extends RefChangeSystem<EntityStore, FlockMembership> {
      @Nonnull
      private final ComponentType<EntityStore, FlockMembership> flockMembershipComponentType;

      public RefChange(@Nonnull ComponentType<EntityStore, FlockMembership> flockMembershipComponentType) {
         this.flockMembershipComponentType = flockMembershipComponentType;
      }

      @Override
      public Query<EntityStore> getQuery() {
         return this.flockMembershipComponentType;
      }

      @Nonnull
      @Override
      public ComponentType<EntityStore, FlockMembership> componentType() {
         return this.flockMembershipComponentType;
      }

      public void onComponentAdded(
         @Nonnull Ref<EntityStore> ref,
         @Nonnull FlockMembership component,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         this.doJoin(ref, component, store, commandBuffer);
      }

      public void onComponentSet(
         @Nonnull Ref<EntityStore> ref,
         FlockMembership oldComponent,
         @Nonnull FlockMembership newComponent,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         assert oldComponent != null;

         if (oldComponent.getMembershipType() == FlockMembership.Type.JOINING) {
            this.doJoin(ref, newComponent, store, commandBuffer);
         } else {
            doLeave(ref, oldComponent, store, commandBuffer);
            commandBuffer.run(_store -> this.doJoin(ref, newComponent, store, commandBuffer));
         }
      }

      public void onComponentRemoved(
         @Nonnull Ref<EntityStore> ref,
         @Nonnull FlockMembership component,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         if (component.getMembershipType() != FlockMembership.Type.JOINING) {
            doLeave(ref, component, store, commandBuffer);
         }
      }

      private void doJoin(
         @Nonnull Ref<EntityStore> ref,
         @Nonnull FlockMembership membershipComponent,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         Ref<EntityStore> flockRef = membershipComponent.getFlockRef();
         if (flockRef != null) {
            if (!flockRef.isValid()) {
               ((HytaleLogger.Api)FlockPlugin.get().getLogger().atWarning()).log("Entity %s attempting to join invalid flock with ref %s", ref, flockRef);
               commandBuffer.removeComponent(ref, this.flockMembershipComponentType);
            } else {
               Flock flockComponent = commandBuffer.getComponent(flockRef, Flock.getComponentType());

               assert flockComponent != null;

               EntityGroup entityGroupComponent = commandBuffer.getComponent(flockRef, EntityGroup.getComponentType());

               assert entityGroupComponent != null;

               UUIDComponent uuidComponent = commandBuffer.getComponent(flockRef, UUIDComponent.getComponentType());

               assert uuidComponent != null;

               UUID flockId = uuidComponent.getUuid();
               if (!entityGroupComponent.isMember(ref)) {
                  if (membershipComponent.getMembershipType() != FlockMembership.Type.JOINING) {
                     throw new IllegalStateException(
                        String.format(
                           "Entity %s attempting to join group with ID %s but has wrong membership status %s",
                           ref,
                           flockId,
                           membershipComponent.getMembershipType()
                        )
                     );
                  } else {
                     boolean isDead = store.getArchetype(ref).contains(DeathComponent.getComponentType());
                     if (ref.isValid() && !isDead) {
                        Player playerComponent = commandBuffer.getComponent(ref, Player.getComponentType());
                        if (playerComponent != null && playerComponent.getGameMode() != GameMode.Adventure) {
                           if (flockComponent.isTrace()) {
                              FlockPlugin.get().getLogger().at(Level.INFO).log("Flock %s: Failed to join, ref=%s. Player in creative mode.", flockId, ref);
                           }

                           commandBuffer.removeComponent(ref, this.flockMembershipComponentType);
                        } else {
                           PersistentFlockData flockData = flockComponent.getFlockData();
                           if (flockData == null) {
                              if (flockComponent.isTrace()) {
                                 FlockPlugin.get()
                                    .getLogger()
                                    .at(Level.INFO)
                                    .log(
                                       "Flock %s: Rejected join entity due to leader not being loaded, ref=%s, size=%s",
                                       flockId,
                                       ref,
                                       entityGroupComponent.size()
                                    );
                              }

                              commandBuffer.removeComponent(ref, this.flockMembershipComponentType);
                           } else {
                              Ref<EntityStore> leader = entityGroupComponent.getLeaderRef();
                              boolean mustBecomeLeader = leader == null;
                              boolean wasFirstJoiner = mustBecomeLeader;
                              if (playerComponent != null) {
                                 if (leader != null && store.getComponent(leader, Player.getComponentType()) != null) {
                                    if (flockComponent.isTrace()) {
                                       FlockPlugin.get()
                                          .getLogger()
                                          .at(Level.INFO)
                                          .log("Flock %s: Failed join 2 players, ref=%s, size=%s", flockId, ref, entityGroupComponent.size());
                                    }

                                    commandBuffer.removeComponent(ref, this.flockMembershipComponentType);
                                    return;
                                 }

                                 mustBecomeLeader = true;
                              }

                              entityGroupComponent.add(ref);
                              FlockMembershipSystems.registerFlockDebugListener(ref, membershipComponent, flockComponent, store);
                              if (mustBecomeLeader) {
                                 setNewLeader(flockId, entityGroupComponent, flockComponent, ref, store, commandBuffer);
                                 if (wasFirstJoiner && flockComponent.isTrace()) {
                                    FlockPlugin.get()
                                       .getLogger()
                                       .at(Level.INFO)
                                       .log("Flock %s: Joined no leader, ref=%s, size=%s", flockId, ref, entityGroupComponent.size());
                                 }
                              } else {
                                 membershipComponent.setMembershipType(FlockMembership.Type.MEMBER);
                              }

                              flockData.increaseSize();
                              FlockMembershipSystems.markChunkNeedsSaving(entityGroupComponent.getLeaderRef(), store);
                              if (flockComponent.isTrace()) {
                                 FlockPlugin.get()
                                    .getLogger()
                                    .at(Level.INFO)
                                    .log("Flock %s: Joined join ref=%s, size=%s", flockId, ref, entityGroupComponent.size());
                              }

                              FlockMembershipSystems.markChunkNeedsSaving(ref, store);
                           }
                        }
                     } else {
                        if (flockComponent.isTrace()) {
                           FlockPlugin.get()
                              .getLogger()
                              .at(Level.INFO)
                              .log("Flock %s: Failed to join entity ref=%s, size=%s", flockId, ref, entityGroupComponent.size());
                        }

                        commandBuffer.removeComponent(ref, this.flockMembershipComponentType);
                     }
                  }
               }
            }
         }
      }

      private static void doLeave(
         @Nonnull Ref<EntityStore> ref,
         @Nonnull FlockMembership membershipComponent,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         Ref<EntityStore> flockReference = membershipComponent.getFlockRef();
         if (flockReference != null && flockReference.isValid()) {
            Flock flockComponent = commandBuffer.getComponent(flockReference, Flock.getComponentType());

            assert flockComponent != null;

            EntityGroup entityGroupComponent = commandBuffer.getComponent(flockReference, EntityGroup.getComponentType());

            assert entityGroupComponent != null;

            UUIDComponent uuidComponent = commandBuffer.getComponent(flockReference, UUIDComponent.getComponentType());

            assert uuidComponent != null;

            UUID flockId = uuidComponent.getUuid();
            FlockMembershipSystems.unregisterFlockDebugListener(ref, membershipComponent, flockComponent, store);
            entityGroupComponent.remove(ref);
            if (flockComponent.isTrace()) {
               FlockPlugin.get()
                  .getLogger()
                  .at(Level.INFO)
                  .log("Flock %s: Left flock, reference=%s, leader=%s, size=%s", flockId, ref, entityGroupComponent.getLeaderRef(), entityGroupComponent.size());
            }

            if (!entityGroupComponent.isDissolved() && entityGroupComponent.size() < 2) {
               commandBuffer.removeEntity(flockReference, RemoveReason.REMOVE);
            } else if (!entityGroupComponent.isDissolved()) {
               PersistentFlockData flockData = flockComponent.getFlockData();
               if (flockData != null) {
                  flockData.decreaseSize();
               }

               Ref<EntityStore> leader = entityGroupComponent.getLeaderRef();
               if (leader != null && !ref.equals(leader)) {
                  FlockMembershipSystems.markChunkNeedsSaving(leader, store);
               } else {
                  Ref<EntityStore> newLeader = entityGroupComponent.testMembers(FlockMembershipSystems::canBecomeLeader, true);
                  if (newLeader == null) {
                     if (flockComponent.isTrace()) {
                        FlockPlugin.get()
                           .getLogger()
                           .at(Level.INFO)
                           .log(
                              "Flock %s: Leave failed to get new leader, reference=%s, leader=%s, size=%s",
                              flockId,
                              ref,
                              entityGroupComponent.getLeaderRef(),
                              entityGroupComponent.size()
                           );
                     }

                     commandBuffer.removeEntity(flockReference, RemoveReason.REMOVE);
                     return;
                  }

                  setNewLeader(flockId, entityGroupComponent, flockComponent, newLeader, store, commandBuffer);
               }
            }
         }
      }

      private static void setNewLeader(
         @Nonnull UUID flockId,
         @Nonnull EntityGroup entityGroup,
         @Nonnull Flock flock,
         @Nonnull Ref<EntityStore> ref,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         Ref<EntityStore> oldLeader = entityGroup.getLeaderRef();
         if (oldLeader == null || !oldLeader.equals(ref)) {
            entityGroup.setLeaderRef(ref);
            if (oldLeader != null) {
               FlockMembership oldLeaderComponent = store.getComponent(oldLeader, FlockMembership.getComponentType());
               if (oldLeaderComponent != null) {
                  oldLeaderComponent.setMembershipType(FlockMembership.Type.MEMBER);
               }

               commandBuffer.tryRemoveComponent(oldLeader, PersistentFlockData.getComponentType());
               FlockMembershipSystems.markChunkNeedsSaving(oldLeader, store);
            }

            FlockMembership flockMembershipComponent = store.getComponent(ref, FlockMembership.getComponentType());

            assert flockMembershipComponent != null;

            flockMembershipComponent.setMembershipType(FlockMembership.Type.LEADER);
            NPCEntity newLeaderNpcComponent = store.getComponent(ref, NPCEntity.getComponentType());
            if (newLeaderNpcComponent != null) {
               Role role = newLeaderNpcComponent.getRole();
               if (role != null) {
                  EnumSet<RoleDebugFlags> flags = role.getDebugSupport().getDebugFlags();
                  flock.setTrace(flags.contains(RoleDebugFlags.Flock));
               }
            }

            PersistentFlockData flockData = flock.getFlockData();
            if (flockData != null) {
               commandBuffer.putComponent(ref, PersistentFlockData.getComponentType(), flockData);
            }

            FlockMembershipSystems.markChunkNeedsSaving(ref, store);
            if (flock.isTrace()) {
               FlockPlugin.get()
                  .getLogger()
                  .at(Level.INFO)
                  .log("Flock %s: Set new leader, old=%s, new=%s, size=%s", flockId, oldLeader, ref, entityGroup.size());
            }
         }
      }
   }
}
