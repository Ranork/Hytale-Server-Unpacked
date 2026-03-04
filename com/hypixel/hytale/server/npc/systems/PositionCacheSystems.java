package com.hypixel.hytale.server.npc.systems;

import com.hypixel.hytale.common.collection.BucketItemPool;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.OrderPriority;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.system.PlayerSpatialSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.flock.FlockMembership;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.decisionmaker.stateevaluator.StateEvaluator;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.instructions.Instruction;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.support.EntityList;
import com.hypixel.hytale.server.npc.role.support.PositionCache;
import com.hypixel.hytale.server.npc.statetransition.StateTransitionController;
import com.hypixel.hytale.server.spawning.SpawningPlugin;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PositionCacheSystems {
   public static void initialisePositionCache(@Nonnull Role role, @Nullable StateEvaluator stateEvaluator, double flockInfluenceRange) {
      PositionCache positionCache = role.getPositionCache();
      positionCache.reset(true);
      if (role.isAvoidingEntities()) {
         double collisionProbeDistance = role.getCollisionProbeDistance();
         positionCache.requireEntityDistanceAvoidance(collisionProbeDistance);
         positionCache.requirePlayerDistanceAvoidance(collisionProbeDistance);
      }

      if (role.isApplySeparation()) {
         double separationDistance = role.getSeparationDistance();
         positionCache.requireEntityDistanceAvoidance(separationDistance);
         positionCache.requirePlayerDistanceAvoidance(separationDistance);
      }

      if (flockInfluenceRange > 0.0) {
         positionCache.requireEntityDistanceAvoidance(flockInfluenceRange);
         positionCache.requirePlayerDistanceAvoidance(flockInfluenceRange);
      }

      Instruction instruction = role.getRootInstruction();
      instruction.registerWithSupport(role);
      Instruction interactionInstruction = role.getInteractionInstruction();
      if (interactionInstruction != null) {
         interactionInstruction.registerWithSupport(role);
         positionCache.requirePlayerDistanceUnsorted(10.0);
      }

      Instruction deathInstruction = role.getDeathInstruction();
      if (deathInstruction != null) {
         deathInstruction.registerWithSupport(role);
      }

      StateTransitionController stateTransitions = role.getStateSupport().getStateTransitionController();
      if (stateTransitions != null) {
         stateTransitions.registerWithSupport(role);
      }

      if (stateEvaluator != null) {
         stateEvaluator.setupNPC(role);
      }

      for (Consumer<Role> registration : positionCache.getExternalRegistrations()) {
         registration.accept(role);
      }

      positionCache.finalizeConfiguration();
   }

   public static class OnFlockJoinSystem extends RefChangeSystem<EntityStore, FlockMembership> {
      @Nonnull
      private final ComponentType<EntityStore, FlockMembership> flockMembershipComponentType;
      @Nonnull
      private final ComponentType<EntityStore, NPCEntity> npcComponentType;
      @Nonnull
      private final ComponentType<EntityStore, StateEvaluator> stateEvaluatorComponentType;
      @Nonnull
      private final Query<EntityStore> query;

      public OnFlockJoinSystem(
         @Nonnull ComponentType<EntityStore, NPCEntity> npcComponentType, @Nonnull ComponentType<EntityStore, FlockMembership> flockMembershipComponentType
      ) {
         this.flockMembershipComponentType = flockMembershipComponentType;
         this.npcComponentType = npcComponentType;
         this.stateEvaluatorComponentType = StateEvaluator.getComponentType();
         this.query = Archetype.of(npcComponentType, flockMembershipComponentType);
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
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
         NPCEntity npcComponent = store.getComponent(ref, this.npcComponentType);

         assert npcComponent != null;

         Role role = npcComponent.getRole();
         PositionCacheSystems.initialisePositionCache(role, store.getComponent(ref, this.stateEvaluatorComponentType), role.getFlockInfluenceRange());
      }

      public void onComponentSet(
         @Nonnull Ref<EntityStore> ref,
         FlockMembership oldComponent,
         @Nonnull FlockMembership newComponent,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         NPCEntity npcComponent = store.getComponent(ref, this.npcComponentType);

         assert npcComponent != null;

         Role role = npcComponent.getRole();
         PositionCacheSystems.initialisePositionCache(role, store.getComponent(ref, this.stateEvaluatorComponentType), role.getFlockInfluenceRange());
      }

      public void onComponentRemoved(
         @Nonnull Ref<EntityStore> ref,
         @Nonnull FlockMembership component,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }
   }

   public static class RoleActivateSystem extends HolderSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, NPCEntity> npcComponentType;
      @Nonnull
      private final ComponentType<EntityStore, StateEvaluator> stateEvaluatorComponentType;

      public RoleActivateSystem(
         @Nonnull ComponentType<EntityStore, NPCEntity> npcComponentType, @Nonnull ComponentType<EntityStore, StateEvaluator> stateEvaluatorComponentType
      ) {
         this.npcComponentType = npcComponentType;
         this.stateEvaluatorComponentType = stateEvaluatorComponentType;
      }

      @Override
      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         NPCEntity npcComponent = holder.getComponent(this.npcComponentType);

         assert npcComponent != null;

         Role role = npcComponent.getRole();
         double influenceRadius;
         if (holder.getComponent(FlockMembership.getComponentType()) != null) {
            influenceRadius = role.getFlockInfluenceRange();
         } else {
            influenceRadius = 0.0;
         }

         StateEvaluator stateEvaluator = holder.getComponent(this.stateEvaluatorComponentType);
         if (stateEvaluator != null) {
            stateEvaluator.setupNPC(holder);
         }

         PositionCacheSystems.initialisePositionCache(role, stateEvaluator, influenceRadius);
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
         NPCEntity npcComponent = holder.getComponent(this.npcComponentType);

         assert npcComponent != null;

         npcComponent.getRole().getPositionCache().reset(false);
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.npcComponentType;
      }
   }

   public static class UpdateSystem extends SteppableTickingSystem {
      @Nonnull
      private static final ThreadLocal<BucketItemPool<Ref<EntityStore>>> BUCKET_POOL_THREAD_LOCAL = ThreadLocal.withInitial(BucketItemPool::new);
      @Nonnull
      private final ComponentType<EntityStore, NPCEntity> npcComponentType;
      @Nonnull
      private final ComponentType<EntityStore, ModelComponent> modelComponentType;
      @Nonnull
      private final ComponentType<EntityStore, TransformComponent> transformComponentType;
      @Nonnull
      private final ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> playerSpatialResource;
      @Nonnull
      private final ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> npcSpatialResource;
      @Nonnull
      private final ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> itemSpatialResource;
      @Nonnull
      private final Query<EntityStore> query;
      @Nonnull
      private final Set<Dependency<EntityStore>> dependencies = Set.of(
         new SystemDependency<>(Order.AFTER, PlayerSpatialSystem.class, OrderPriority.CLOSEST),
         new SystemDependency<>(Order.BEFORE, RoleSystems.PreBehaviourSupportTickSystem.class)
      );

      public UpdateSystem(
         @Nonnull ComponentType<EntityStore, NPCEntity> npcComponentType,
         @Nonnull ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> npcSpatialResource
      ) {
         this.npcComponentType = npcComponentType;
         this.modelComponentType = ModelComponent.getComponentType();
         this.transformComponentType = TransformComponent.getComponentType();
         this.playerSpatialResource = EntityModule.get().getPlayerSpatialResourceType();
         this.npcSpatialResource = npcSpatialResource;
         this.itemSpatialResource = EntityModule.get().getItemSpatialResourceType();
         this.query = Query.and(npcComponentType, this.transformComponentType, this.modelComponentType);
      }

      @Nonnull
      @Override
      public Set<Dependency<EntityStore>> getDependencies() {
         return this.dependencies;
      }

      @Override
      public boolean isParallel(int archetypeChunkSize, int taskCount) {
         return false;
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      @Override
      public void steppedTick(
         float dt,
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
         NPCEntity npcComponent = archetypeChunk.getComponent(index, this.npcComponentType);

         assert npcComponent != null;

         Role role = npcComponent.getRole();
         PositionCache positionCache = role.getPositionCache();
         positionCache.setBenchmarking(NPCPlugin.get().isBenchmarkingSensorSupport());
         long packed = LivingEntity.getPackedMaterialAndFluidAtBreathingHeight(ref, commandBuffer);
         BlockMaterial material = BlockMaterial.VALUES[MathUtil.unpackLeft(packed)];
         int fluidId = MathUtil.unpackRight(packed);
         positionCache.setCouldBreathe(role.canBreathe(material, fluidId));
         if (positionCache.tickPositionCacheNextUpdate(dt)) {
            positionCache.resetPositionCacheNextUpdate();
            TransformComponent transformComponent = archetypeChunk.getComponent(index, this.transformComponentType);

            assert transformComponent != null;

            Vector3d position = transformComponent.getPosition();
            EntityList players = positionCache.getPlayers();
            if (players.getSearchRadius() > 0) {
               SpatialResource<Ref<EntityStore>, EntityStore> spatialResource = store.getResource(this.playerSpatialResource);
               players.setBucketItemPool(BUCKET_POOL_THREAD_LOCAL.get());
               if (positionCache.isBenchmarking()) {
                  long startTime = System.nanoTime();
                  addEntities(ref, position, players, spatialResource, commandBuffer);
                  long getTime = System.nanoTime();
                  NPCPlugin.get()
                     .collectSensorSupportPlayerList(
                        role.getRoleIndex(),
                        getTime - startTime,
                        players.getMaxDistanceSorted(),
                        players.getMaxDistanceUnsorted(),
                        players.getMaxDistanceAvoidance(),
                        0
                     );
               } else {
                  addEntities(ref, position, players, spatialResource, commandBuffer);
               }
            }

            EntityList npcEntities = positionCache.getNpcs();
            if (npcEntities.getSearchRadius() > 0) {
               SpatialResource<Ref<EntityStore>, EntityStore> spatialResource = store.getResource(this.npcSpatialResource);
               npcEntities.setBucketItemPool(BUCKET_POOL_THREAD_LOCAL.get());
               if (positionCache.isBenchmarking()) {
                  long startTime = System.nanoTime();
                  addEntities(ref, position, npcEntities, spatialResource, commandBuffer);
                  long getTime = System.nanoTime();
                  NPCPlugin.get()
                     .collectSensorSupportEntityList(
                        role.getRoleIndex(),
                        getTime - startTime,
                        npcEntities.getMaxDistanceSorted(),
                        npcEntities.getMaxDistanceUnsorted(),
                        npcEntities.getMaxDistanceAvoidance(),
                        0
                     );
               } else {
                  addEntities(ref, position, npcEntities, spatialResource, commandBuffer);
               }
            }

            double maxDroppedItemDistance = positionCache.getMaxDroppedItemDistance();
            if (maxDroppedItemDistance > 0.0) {
               SpatialResource<Ref<EntityStore>, EntityStore> spatialResource = store.getResource(this.itemSpatialResource);
               List<Ref<EntityStore>> list = positionCache.getDroppedItemList();
               list.clear();
               spatialResource.getSpatialStructure().ordered(position, (int)maxDroppedItemDistance + 1, list);
            }

            double maxSpawnMarkerDistance = positionCache.getMaxSpawnMarkerDistance();
            if (maxSpawnMarkerDistance > 0.0) {
               SpatialResource<Ref<EntityStore>, EntityStore> spatialResource = store.getResource(SpawningPlugin.get().getSpawnMarkerSpatialResource());
               List<Ref<EntityStore>> list = positionCache.getSpawnMarkerList();
               list.clear();
               spatialResource.getSpatialStructure().collect(position, (int)maxSpawnMarkerDistance + 1, list);
            }

            int maxSpawnBeaconDistance = positionCache.getMaxSpawnBeaconDistance();
            if (maxSpawnBeaconDistance > 0) {
               SpatialResource<Ref<EntityStore>, EntityStore> spatialResource = store.getResource(SpawningPlugin.get().getManualSpawnBeaconSpatialResource());
               List<Ref<EntityStore>> list = positionCache.getSpawnBeaconList();
               list.clear();
               spatialResource.getSpatialStructure().ordered(position, maxSpawnBeaconDistance + 1, list);
            }
         }
      }

      private static void addEntities(
         @Nonnull Ref<EntityStore> self,
         @Nonnull Vector3d position,
         @Nonnull EntityList entityList,
         @Nonnull SpatialResource<Ref<EntityStore>, EntityStore> spatialResource,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         List<Ref<EntityStore>> results = SpatialResource.<EntityStore>getThreadLocalReferenceList();
         spatialResource.getSpatialStructure().collect(position, entityList.getSearchRadius(), results);

         for (Ref<EntityStore> result : results) {
            if (result.isValid() && !result.equals(self)) {
               entityList.add(result, position, commandBuffer);
            }
         }
      }
   }
}
