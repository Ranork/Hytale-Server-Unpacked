package com.hypixel.hytale.builtin.ambience.systems;

import com.hypixel.hytale.builtin.ambience.AmbiencePlugin;
import com.hypixel.hytale.builtin.ambience.components.AmbientEmitterComponent;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.NonSerialized;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.modules.entity.component.AudioComponent;
import com.hypixel.hytale.server.core.modules.entity.component.Intangible;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.prefab.PrefabCopyableComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class AmbientEmitterSystems {
   public static class EntityAdded extends HolderSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, NetworkId> networkIdComponentType;
      @Nonnull
      private final ComponentType<EntityStore, Intangible> intangibleComponentType;
      @Nonnull
      private final ComponentType<EntityStore, PrefabCopyableComponent> prefabCopyableComponentType;
      @Nonnull
      private final Query<EntityStore> query;

      public EntityAdded(
         @Nonnull ComponentType<EntityStore, AmbientEmitterComponent> ambientEmitterComponentType,
         @Nonnull ComponentType<EntityStore, TransformComponent> transformComponentType,
         @Nonnull ComponentType<EntityStore, NetworkId> networkIdComponentType,
         @Nonnull ComponentType<EntityStore, Intangible> intangibleComponentType,
         @Nonnull ComponentType<EntityStore, PrefabCopyableComponent> prefabCopyableComponentType
      ) {
         this.networkIdComponentType = networkIdComponentType;
         this.intangibleComponentType = intangibleComponentType;
         this.prefabCopyableComponentType = prefabCopyableComponentType;
         this.query = Query.and(ambientEmitterComponentType, transformComponentType);
      }

      @Override
      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         Archetype<EntityStore> archetype = holder.getArchetype();
         if (!archetype.contains(this.networkIdComponentType)) {
            int nextNetworkId = store.getExternalData().takeNextNetworkId();
            holder.addComponent(this.networkIdComponentType, new NetworkId(nextNetworkId));
         }

         holder.ensureComponent(this.intangibleComponentType);
         holder.ensureComponent(this.prefabCopyableComponentType);
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

   public static class EntityRefAdded extends RefSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, AmbientEmitterComponent> ambientEmitterComponentType;
      @Nonnull
      private final ComponentType<EntityStore, TransformComponent> transformComponentType;
      @Nonnull
      private final ComponentType<EntityStore, AudioComponent> audioComponentType;
      @Nonnull
      private final ComponentType<EntityStore, NetworkId> networkIdComponentType;
      @Nonnull
      private final ComponentType<EntityStore, Intangible> intangibleComponentType;
      @Nonnull
      private final Query<EntityStore> query;

      public EntityRefAdded(
         @Nonnull ComponentType<EntityStore, AmbientEmitterComponent> ambientEmitterComponentType,
         @Nonnull ComponentType<EntityStore, TransformComponent> transformComponentType,
         @Nonnull ComponentType<EntityStore, AudioComponent> audioComponentType,
         @Nonnull ComponentType<EntityStore, NetworkId> networkIdComponentType,
         @Nonnull ComponentType<EntityStore, Intangible> intangibleComponentType
      ) {
         this.ambientEmitterComponentType = ambientEmitterComponentType;
         this.transformComponentType = transformComponentType;
         this.audioComponentType = audioComponentType;
         this.networkIdComponentType = networkIdComponentType;
         this.intangibleComponentType = intangibleComponentType;
         this.query = Query.and(ambientEmitterComponentType, transformComponentType);
      }

      @Override
      public void onEntityAdded(
         @Nonnull Ref<EntityStore> ref, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         AmbientEmitterComponent emitterComponent = store.getComponent(ref, this.ambientEmitterComponentType);

         assert emitterComponent != null;

         TransformComponent transformComponent = store.getComponent(ref, this.transformComponentType);

         assert transformComponent != null;

         Holder<EntityStore> emitterHolder = EntityStore.REGISTRY.newHolder();
         emitterHolder.addComponent(this.transformComponentType, transformComponent.clone());
         AudioComponent audioComponent = new AudioComponent();
         audioComponent.addSound(SoundEvent.getAssetMap().getIndex(emitterComponent.getSoundEventId()));
         emitterHolder.addComponent(this.audioComponentType, audioComponent);
         int nextNetworkId = store.getExternalData().takeNextNetworkId();
         emitterHolder.addComponent(this.networkIdComponentType, new NetworkId(nextNetworkId));
         emitterHolder.ensureComponent(this.intangibleComponentType);
         emitterHolder.addComponent(EntityStore.REGISTRY.getNonSerializedComponentType(), NonSerialized.get());
         Ref<EntityStore> emitterRef = commandBuffer.addEntity(emitterHolder, AddReason.SPAWN);
         emitterComponent.setSpawnedEmitter(emitterRef);
      }

      @Override
      public void onEntityRemove(
         @Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         if (reason == RemoveReason.REMOVE) {
            AmbientEmitterComponent emitterComponent = store.getComponent(ref, this.ambientEmitterComponentType);

            assert emitterComponent != null;

            Ref<EntityStore> emitterRef = emitterComponent.getSpawnedEmitter();
            if (emitterRef != null) {
               commandBuffer.removeEntity(emitterRef, RemoveReason.REMOVE);
            }
         }
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }
   }

   public static class Ticking extends EntityTickingSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, AmbientEmitterComponent> ambientEmitterComponentType;
      @Nonnull
      private final ComponentType<EntityStore, TransformComponent> transformComponentType;
      @Nonnull
      private final Query<EntityStore> query;

      public Ticking(
         @Nonnull ComponentType<EntityStore, AmbientEmitterComponent> ambientEmitterComponentType,
         @Nonnull ComponentType<EntityStore, TransformComponent> transformComponentType
      ) {
         this.ambientEmitterComponentType = ambientEmitterComponentType;
         this.transformComponentType = transformComponentType;
         this.query = Query.and(ambientEmitterComponentType, transformComponentType);
      }

      @Override
      public void tick(
         float dt,
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         AmbientEmitterComponent emitterComponent = archetypeChunk.getComponent(index, this.ambientEmitterComponentType);

         assert emitterComponent != null;

         TransformComponent transformComponent = archetypeChunk.getComponent(index, this.transformComponentType);

         assert transformComponent != null;

         Ref<EntityStore> spawnedEmitterRef = emitterComponent.getSpawnedEmitter();
         if (spawnedEmitterRef != null && spawnedEmitterRef.isValid()) {
            TransformComponent ownedEmitterTransform = commandBuffer.getComponent(spawnedEmitterRef, this.transformComponentType);
            if (ownedEmitterTransform != null) {
               if (transformComponent.getPosition().distanceSquaredTo(ownedEmitterTransform.getPosition()) > 1.0) {
                  ownedEmitterTransform.setPosition(transformComponent.getPosition());
               }
            }
         } else {
            Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
            AmbiencePlugin.get()
               .getLogger()
               .at(Level.WARNING)
               .log("Ambient emitter lost at %s: %d %s", transformComponent.getPosition(), ref.getIndex(), emitterComponent.getSoundEventId());
            commandBuffer.removeEntity(ref, RemoveReason.REMOVE);
         }
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }
   }
}
