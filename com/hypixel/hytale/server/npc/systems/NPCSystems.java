package com.hypixel.hytale.server.npc.systems;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.component.system.WorldEventSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.entity.Frozen;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.DisplayNameComponent;
import com.hypixel.hytale.server.core.modules.entity.component.FromPrefab;
import com.hypixel.hytale.server.core.modules.entity.component.FromWorldGen;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.MovementAudioComponent;
import com.hypixel.hytale.server.core.modules.entity.component.NPCMarkerComponent;
import com.hypixel.hytale.server.core.modules.entity.component.NewSpawnComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PositionDataComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.component.WorldGenId;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.modules.entity.damage.DeferredCorpseRemoval;
import com.hypixel.hytale.server.core.modules.entity.damage.event.KillFeedEvent;
import com.hypixel.hytale.server.core.modules.entity.system.ModelSystems;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.modules.entity.teleport.TeleportSystems;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.prefab.PrefabCopyableComponent;
import com.hypixel.hytale.server.core.prefab.event.PrefabPlaceEntityEvent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.flock.FlockMembership;
import com.hypixel.hytale.server.flock.FlockPlugin;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.blackboard.view.blocktype.BlockTypeView;
import com.hypixel.hytale.server.npc.config.balancing.BalanceAsset;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NPCSystems {
   public static class AddSpawnEntityEffectSystem extends RefSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, NPCEntity> npcComponentType;

      public AddSpawnEntityEffectSystem(@Nonnull ComponentType<EntityStore, NPCEntity> npcComponentType) {
         this.npcComponentType = npcComponentType;
      }

      @Override
      public void onEntityAdded(
         @Nonnull Ref<EntityStore> ref, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         NPCEntity npcComponent = store.getComponent(ref, this.npcComponentType);

         assert npcComponent != null;

         EffectControllerComponent effectController = store.getComponent(ref, EffectControllerComponent.getComponentType());

         assert effectController != null;

         Role role = npcComponent.getRole();
         if (role == null) {
            ((HytaleLogger.Api)((HytaleLogger.Api)NPCPlugin.get().getLogger().atSevere())
                  .withCause(new IllegalStateException("NPC has no role or role index in onLoad!")))
               .log();
            commandBuffer.removeEntity(ref, RemoveReason.REMOVE);
         } else {
            String balanceAssetId = role.getBalanceAsset();
            if (balanceAssetId != null) {
               BalanceAsset balanceAsset = BalanceAsset.getAssetMap().getAsset(balanceAssetId);
               String entityEffectId = balanceAsset.getEntityEffect();
               if (entityEffectId != null) {
                  EntityEffect entityEffect = EntityEffect.getAssetMap().getAsset(entityEffectId);
                  effectController.addEffect(ref, entityEffect, commandBuffer);
               }
            }
         }
      }

      @Override
      public void onEntityRemove(
         @Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return Query.and(this.npcComponentType, EffectControllerComponent.getComponentType());
      }
   }

   public static class AddedFromExternalSystem extends RefSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, NPCEntity> npcComponentType;
      @Nonnull
      private final ComponentType<EntityStore, TransformComponent> transformComponentType;
      @Nonnull
      private final Set<Dependency<EntityStore>> dependencies;
      @Nonnull
      private final Query<EntityStore> query;

      public AddedFromExternalSystem(@Nonnull ComponentType<EntityStore, NPCEntity> npcComponentType) {
         this.npcComponentType = npcComponentType;
         this.transformComponentType = TransformComponent.getComponentType();
         this.dependencies = Set.of(new SystemDependency<>(Order.AFTER, NPCSystems.AddedSystem.class));
         this.query = Query.and(npcComponentType, this.transformComponentType, Query.or(FromWorldGen.getComponentType(), FromPrefab.getComponentType()));
      }

      @Override
      public void onEntityAdded(
         @Nonnull Ref<EntityStore> ref, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         WorldTimeResource worldTimeResource = commandBuffer.getResource(WorldTimeResource.getResourceType());
         NPCEntity npcComponent = store.getComponent(ref, this.npcComponentType);

         assert npcComponent != null;

         Archetype<EntityStore> archetype = store.getArchetype(ref);
         boolean fromWorldGen = archetype.contains(FromWorldGen.getComponentType());
         TransformComponent transformComponent = store.getComponent(ref, this.transformComponentType);

         assert transformComponent != null;

         npcComponent.getLeashPoint().assign(transformComponent.getPosition());
         Vector3f bodyRotation = transformComponent.getRotation();
         npcComponent.setLeashHeading(bodyRotation.getYaw());
         npcComponent.setLeashPitch(bodyRotation.getPitch());
         npcComponent.setSpawnInstant(worldTimeResource.getGameTime());
         npcComponent.getRole().onLoadFromWorldGenOrPrefab(ref, npcComponent, commandBuffer);
         if (fromWorldGen) {
            commandBuffer.tryRemoveComponent(ref, Frozen.getComponentType());
         }
      }

      @Override
      public void onEntityRemove(
         @Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      @Nonnull
      @Override
      public Set<Dependency<EntityStore>> getDependencies() {
         return this.dependencies;
      }

      @Nullable
      @Override
      public SystemGroup<EntityStore> getGroup() {
         return EntityModule.get().getPreClearMarkersGroup();
      }
   }

   public static class AddedFromWorldGenSystem extends HolderSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, NPCEntity> npcComponentType = NPCEntity.getComponentType();
      @Nonnull
      private final ComponentType<EntityStore, WorldGenId> worldGenIdComponentType = WorldGenId.getComponentType();
      @Nonnull
      private final ComponentType<EntityStore, FromWorldGen> fromWorldGenComponentType = FromWorldGen.getComponentType();
      @Nonnull
      private final Query<EntityStore> query = Query.and(this.npcComponentType, this.fromWorldGenComponentType);

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
         FromWorldGen fromWorldGenComponent = holder.getComponent(this.fromWorldGenComponentType);

         assert fromWorldGenComponent != null;

         holder.putComponent(this.worldGenIdComponentType, new WorldGenId(fromWorldGenComponent.getWorldGenId()));
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
      }
   }

   public static class AddedSystem extends RefSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, NPCEntity> npcComponentType;

      public AddedSystem(@Nonnull ComponentType<EntityStore, NPCEntity> npcComponentType) {
         this.npcComponentType = npcComponentType;
      }

      @Override
      public void onEntityAdded(
         @Nonnull Ref<EntityStore> ref, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         NPCEntity npcComponent = store.getComponent(ref, this.npcComponentType);

         assert npcComponent != null;

         Role role = npcComponent.getRole();
         if (role == null) {
            ((HytaleLogger.Api)((HytaleLogger.Api)NPCPlugin.get().getLogger().atSevere())
                  .withCause(new IllegalStateException("NPC has no role or role index in onLoad!")))
               .log();
            commandBuffer.removeEntity(ref, RemoveReason.REMOVE);
         } else {
            npcComponent.initBlockChangeBlackboardView(ref, commandBuffer);
            role.loaded();
            commandBuffer.ensureComponent(ref, PrefabCopyableComponent.getComponentType());
            commandBuffer.ensureComponent(ref, NPCMarkerComponent.getComponentType());
            commandBuffer.ensureComponent(ref, PositionDataComponent.getComponentType());
            commandBuffer.ensureComponent(ref, MovementAudioComponent.getComponentType());
            if (reason == AddReason.SPAWN) {
               NewSpawnComponent newSpawnComponent = new NewSpawnComponent(role.getSpawnLockTime());
               commandBuffer.addComponent(ref, NewSpawnComponent.getComponentType(), newSpawnComponent);
            }
         }
      }

      @Override
      public void onEntityRemove(
         @Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         NPCEntity npcComponent = store.getComponent(ref, this.npcComponentType);

         assert npcComponent != null;

         BlockTypeView blockTypeView = npcComponent.removeBlockTypeBlackboardView();
         if (blockTypeView != null) {
            blockTypeView.removeSearchedBlockSets(ref, npcComponent, npcComponent.getBlackboardBlockTypeSets());
         }

         switch (reason) {
            case REMOVE:
               npcComponent.getRole().removed();
               break;
            case UNLOAD:
               npcComponent.getRole().unloaded();
         }
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.npcComponentType;
      }
   }

   public static class KillFeedDecedentEventSystem extends EntityEventSystem<EntityStore, KillFeedEvent.DecedentMessage> {
      @Nonnull
      private final ComponentType<EntityStore, NPCEntity> npcComponentType = NPCEntity.getComponentType();

      public KillFeedDecedentEventSystem() {
         super(KillFeedEvent.DecedentMessage.class);
      }

      public void handle(
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer,
         @Nonnull KillFeedEvent.DecedentMessage event
      ) {
         event.setCancelled(true);
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.npcComponentType;
      }
   }

   public static class KillFeedKillerEventSystem extends EntityEventSystem<EntityStore, KillFeedEvent.KillerMessage> {
      @Nonnull
      private final ComponentType<EntityStore, NPCEntity> npcComponentType = NPCEntity.getComponentType();
      @Nonnull
      private final ComponentType<EntityStore, Player> playerComponentType = Player.getComponentType();

      public KillFeedKillerEventSystem() {
         super(KillFeedEvent.KillerMessage.class);
      }

      public void handle(
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer,
         @Nonnull KillFeedEvent.KillerMessage event
      ) {
         Ref<EntityStore> targetRef = event.getTargetRef();
         if (targetRef.isValid()) {
            Player playerComponent = store.getComponent(targetRef, this.playerComponentType);
            if (playerComponent == null) {
               event.setCancelled(true);
            } else {
               DisplayNameComponent displayNameComponent = archetypeChunk.getComponent(index, DisplayNameComponent.getComponentType());
               Message displayName;
               if (displayNameComponent != null) {
                  displayName = displayNameComponent.getDisplayName();
               } else {
                  NPCEntity npcComponent = archetypeChunk.getComponent(index, this.npcComponentType);

                  assert npcComponent != null;

                  displayName = Message.raw(npcComponent.getRoleName());
               }

               event.setMessage(displayName);
            }
         }
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.npcComponentType;
      }
   }

   @Deprecated(forRemoval = true)
   public static class LegacyWorldGenId extends HolderSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, NPCEntity> npcComponentType = NPCEntity.getComponentType();
      @Nonnull
      private final ComponentType<EntityStore, WorldGenId> worldGenIdComponentType = WorldGenId.getComponentType();
      @Nonnull
      private final Query<EntityStore> query = Query.and(
         this.npcComponentType, Query.not(this.worldGenIdComponentType), Query.not(FromWorldGen.getComponentType())
      );

      @Override
      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         NPCEntity npcComponent = holder.getComponent(this.npcComponentType);

         assert npcComponent != null;

         int worldGenId = npcComponent.getLegacyWorldgenId();
         if (worldGenId != 0) {
            holder.addComponent(this.worldGenIdComponentType, new WorldGenId(worldGenId));
         }
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }
   }

   public static class ModelChangeSystem extends RefChangeSystem<EntityStore, ModelComponent> {
      @Nonnull
      private final ComponentType<EntityStore, ModelComponent> modelComponentType = ModelComponent.getComponentType();
      @Nullable
      private final ComponentType<EntityStore, NPCEntity> npcComponentType = NPCEntity.getComponentType();
      @Nonnull
      private final Set<Dependency<EntityStore>> dependencies = Set.of(new SystemDependency<>(Order.AFTER, ModelSystems.UpdateBoundingBox.class));

      @Nullable
      @Override
      public Query<EntityStore> getQuery() {
         return this.npcComponentType;
      }

      @Nonnull
      @Override
      public Set<Dependency<EntityStore>> getDependencies() {
         return this.dependencies;
      }

      @Nonnull
      @Override
      public ComponentType<EntityStore, ModelComponent> componentType() {
         return this.modelComponentType;
      }

      public void onComponentAdded(
         @Nonnull Ref<EntityStore> ref, @Nonnull ModelComponent component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         Model model = component.getModel();
         NPCEntity npcComponent = store.getComponent(ref, this.npcComponentType);

         assert npcComponent != null;

         npcComponent.getRole().updateMotionControllers(ref, model, model.getBoundingBox(), commandBuffer);
      }

      public void onComponentSet(
         @Nonnull Ref<EntityStore> ref,
         ModelComponent oldComponent,
         @Nonnull ModelComponent newComponent,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         Model model = newComponent.getModel();
         NPCEntity npcComponent = store.getComponent(ref, this.npcComponentType);

         assert npcComponent != null;

         npcComponent.getRole().updateMotionControllers(ref, model, model.getBoundingBox(), commandBuffer);
      }

      public void onComponentRemoved(
         @Nonnull Ref<EntityStore> ref, @Nonnull ModelComponent component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         NPCEntity npcComponent = store.getComponent(ref, this.npcComponentType);

         assert npcComponent != null;

         npcComponent.getRole().updateMotionControllers(ref, null, null, commandBuffer);
      }
   }

   public static class OnDeathSystem extends DeathSystems.OnDeathSystem {
      @Nonnull
      private final ComponentType<EntityStore, NPCEntity> npcComponentType = NPCEntity.getComponentType();
      @Nonnull
      private final ComponentType<EntityStore, DeferredCorpseRemoval> deferredCorpseRemovalComponentType = DeferredCorpseRemoval.getComponentType();

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.npcComponentType;
      }

      public void onComponentAdded(
         @Nonnull Ref<EntityStore> ref, @Nonnull DeathComponent component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         NPCEntity npcComponent = commandBuffer.getComponent(ref, this.npcComponentType);

         assert npcComponent != null;

         Role role = npcComponent.getRole();
         double deathAnimationTime = role.getDeathAnimationTime();
         if (deathAnimationTime > 0.0) {
            commandBuffer.addComponent(ref, this.deferredCorpseRemovalComponentType, new DeferredCorpseRemoval(deathAnimationTime));
         }
      }
   }

   public static class OnTeleportSystem extends RefChangeSystem<EntityStore, Teleport> {
      @Nonnull
      private final ComponentType<EntityStore, NPCEntity> npcComponentType = NPCEntity.getComponentType();
      @Nonnull
      private final ComponentType<EntityStore, Teleport> teleportComponentType = Teleport.getComponentType();
      @Nonnull
      private final Set<Dependency<EntityStore>> dependencies = Set.of(new SystemDependency<>(Order.AFTER, TeleportSystems.MoveSystem.class));

      @Nonnull
      @Override
      public Set<Dependency<EntityStore>> getDependencies() {
         return this.dependencies;
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.npcComponentType;
      }

      @Nonnull
      @Override
      public ComponentType<EntityStore, Teleport> componentType() {
         return this.teleportComponentType;
      }

      public void onComponentAdded(
         @Nonnull Ref<EntityStore> ref, @Nonnull Teleport component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         NPCEntity npcComponent = commandBuffer.getComponent(ref, this.npcComponentType);

         assert npcComponent != null;

         World world = store.getExternalData().getWorld();
         World worldTo = component.getWorld();
         Role role = npcComponent.getRole();

         assert role != null;

         role.teleported(world, worldTo == null ? world : worldTo);
      }

      public void onComponentSet(
         @Nonnull Ref<EntityStore> ref,
         Teleport oldComponent,
         @Nonnull Teleport newComponent,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }

      public void onComponentRemoved(
         @Nonnull Ref<EntityStore> ref, @Nonnull Teleport component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }
   }

   public static class PrefabPlaceEntityEventSystem extends WorldEventSystem<EntityStore, PrefabPlaceEntityEvent> {
      public PrefabPlaceEntityEventSystem() {
         super(PrefabPlaceEntityEvent.class);
      }

      public void handle(@Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull PrefabPlaceEntityEvent event) {
         Holder<EntityStore> holder = event.getHolder();
         FlockMembership flockMembershipComponent = holder.getComponent(FlockMembership.getComponentType());
         if (flockMembershipComponent != null) {
            UUID flockId = FlockPlugin.get().getPrefabRemappedFlockReference(event.getPrefabId(), flockMembershipComponent.getFlockId());
            flockMembershipComponent.setFlockId(flockId);
            NPCEntity npcComponent = holder.getComponent(NPCEntity.getComponentType());

            assert npcComponent != null;

            npcComponent.markNeedsSave();
         }
      }
   }
}
