package com.hypixel.hytale.server.npc.systems;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.AnimationSlot;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.components.SpawnBeaconReference;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.spawning.SpawningPlugin;
import java.util.Set;
import javax.annotation.Nonnull;

public class NPCPreTickSystem extends SteppableTickingSystem {
   private static final float DEFAULT_DESPAWN_CHECK_DELAY = 30.0F;
   @Nonnull
   private final ComponentType<EntityStore, NPCEntity> npcComponentType;
   @Nonnull
   private final ComponentType<EntityStore, TransformComponent> transformComponentType = TransformComponent.getComponentType();
   @Nonnull
   private final ComponentType<EntityStore, ModelComponent> modelComponentType = ModelComponent.getComponentType();
   @Nonnull
   private final Set<Dependency<EntityStore>> dependencies;
   @Nonnull
   private final Query<EntityStore> query;

   public NPCPreTickSystem(@Nonnull ComponentType<EntityStore, NPCEntity> npcComponentType) {
      this.npcComponentType = npcComponentType;
      this.dependencies = Set.of(new SystemDependency<>(Order.BEFORE, DeathSystems.CorpseRemoval.class));
      this.query = Archetype.of(npcComponentType, this.transformComponentType);
   }

   @Nonnull
   @Override
   public Set<Dependency<EntityStore>> getDependencies() {
      return this.dependencies;
   }

   @Override
   public boolean isParallel(int archetypeChunkSize, int taskCount) {
      return EntityTickingSystem.maybeUseParallel(archetypeChunkSize, taskCount);
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

      TransformComponent transformComponent = archetypeChunk.getComponent(index, this.transformComponentType);

      assert transformComponent != null;

      Vector3d position = transformComponent.getPosition();
      npcComponent.storeTickStartPosition(position);
      if (npcComponent.isPlayingDespawnAnim()) {
         if (npcComponent.tickDespawnAnimationRemainingSeconds(dt)) {
            commandBuffer.removeEntity(archetypeChunk.getReferenceTo(index), RemoveReason.REMOVE);
         }
      } else if (npcComponent.isDespawning()) {
         if (npcComponent.tickDespawnRemainingSeconds(dt)) {
            npcComponent.setDespawning(false);
            ModelComponent modelComponent = archetypeChunk.getComponent(index, this.modelComponentType);
            if (modelComponent != null && modelComponent.getModel().getAnimationSetMap().containsKey("Despawn")) {
               npcComponent.setPlayingDespawnAnim(true);
               npcComponent.setDespawnAnimationRemainingSeconds(npcComponent.getRole().getDespawnAnimationTime());
               commandBuffer.run(_store -> npcComponent.playAnimation(ref, AnimationSlot.Status, "Despawn", _store));
               return;
            }

            commandBuffer.removeEntity(archetypeChunk.getReferenceTo(index), RemoveReason.REMOVE);
         }
      } else {
         if (npcComponent.tickDespawnCheckRemainingSeconds(dt)) {
            npcComponent.setDespawnCheckRemainingSeconds(30.0F);
            if (npcComponent.getRole().getStateSupport().isInBusyState()) {
               return;
            }

            SpawnBeaconReference spawnBeaconReference = archetypeChunk.getComponent(index, SpawnBeaconReference.getComponentType());
            WorldTimeResource timeManager = commandBuffer.getResource(WorldTimeResource.getResourceType());
            if (SpawningPlugin.get().shouldNPCDespawn(store, npcComponent, timeManager, npcComponent.getSpawnConfiguration(), spawnBeaconReference != null)) {
               npcComponent.setDespawning(true);
               npcComponent.setDespawnRemainingSeconds(0.0F);
            }
         }
      }
   }
}
