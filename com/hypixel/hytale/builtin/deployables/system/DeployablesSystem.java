package com.hypixel.hytale.builtin.deployables.system;

import com.hypixel.hytale.builtin.deployables.component.DeployableComponent;
import com.hypixel.hytale.builtin.deployables.component.DeployableOwnerComponent;
import com.hypixel.hytale.builtin.deployables.config.DeployableConfig;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.protocol.Vector3f;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelParticle;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectList;
import javax.annotation.Nonnull;

public class DeployablesSystem {
   private static void spawnParticleEffect(
      @Nonnull Ref<EntityStore> sourceRef, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull Vector3d position, @Nonnull ModelParticle particle
   ) {
      Vector3f particlePositionOffset = particle.getPositionOffset();
      Direction particleRotationOffset = particle.getRotationOffset();
      Vector3d particlePosition = new Vector3d(position.x, position.y, position.z);
      Vector3f particleRotation = new Vector3f(0.0F, 0.0F, 0.0F);
      if (particlePositionOffset != null) {
         particlePosition.add(particlePositionOffset.x, particlePositionOffset.y, particlePositionOffset.z);
      }

      if (particleRotationOffset != null) {
         particleRotation = new Vector3f(particleRotationOffset.yaw, particleRotationOffset.pitch, particleRotationOffset.roll);
      }

      SpatialResource<Ref<EntityStore>, EntityStore> playerSpatialResource = commandBuffer.getResource(EntityModule.get().getPlayerSpatialResourceType());
      ObjectList<Ref<EntityStore>> results = SpatialResource.getThreadLocalReferenceList();
      playerSpatialResource.getSpatialStructure().collect(particlePosition, 75.0, results);
      ParticleUtil.spawnParticleEffect(
         particle.getSystemId(),
         particlePosition.x,
         particlePosition.y,
         particlePosition.z,
         particleRotation.x,
         particleRotation.y,
         particleRotation.z,
         sourceRef,
         results,
         commandBuffer
      );
   }

   public static class DeployableOwnerTicker extends EntityTickingSystem<EntityStore> {
      @Override
      public Query<EntityStore> getQuery() {
         return Query.and(DeployableOwnerComponent.getComponentType());
      }

      @Override
      public void tick(
         float dt, int index, ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         DeployableOwnerComponent deployableOwnerComponent = archetypeChunk.getComponent(index, DeployableOwnerComponent.getComponentType());

         assert deployableOwnerComponent != null;

         deployableOwnerComponent.tick(commandBuffer);
      }
   }

   public static class DeployableRegisterer extends RefSystem<EntityStore> {
      private static void deregisterOwner(
         @Nonnull Ref<EntityStore> ref, @Nonnull DeployableComponent deployableComponent, @Nonnull DeployableConfig deployableConfig
      ) {
         Ref<EntityStore> ownerRef = deployableComponent.getOwner();
         if (ownerRef != null && ownerRef.isValid()) {
            DeployableOwnerComponent deployableOwnerComponent = ownerRef.getStore().getComponent(ownerRef, DeployableOwnerComponent.getComponentType());
            if (deployableOwnerComponent != null) {
               deployableOwnerComponent.deRegisterDeployable(deployableConfig.getId(), ref);
            }
         }
      }

      @Override
      public Query<EntityStore> getQuery() {
         return Query.and(DeployableComponent.getComponentType());
      }

      @Override
      public void onEntityAdded(
         @Nonnull Ref<EntityStore> ref, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         DeployableComponent deployableComponent = store.getComponent(ref, DeployableComponent.getComponentType());

         assert deployableComponent != null;

         TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());
         if (transformComponent != null) {
            DeployableConfig deployableConfig = deployableComponent.getConfig();
            Vector3d position = transformComponent.getPosition();
            Ref<EntityStore> ownerRef = deployableComponent.getOwner();
            int soundIndex = deployableConfig.getDeploySoundEventIndex();
            SoundUtil.playSoundEvent3d(null, soundIndex, position, commandBuffer);
            ModelParticle[] particles = deployableConfig.getSpawnParticles();
            if (particles != null) {
               for (ModelParticle particle : particles) {
                  DeployablesSystem.spawnParticleEffect(ref, commandBuffer, position, particle);
               }
            }

            if (ownerRef.isValid()) {
               DeployableOwnerComponent deployableOwnerComponent = ownerRef.getStore().getComponent(ownerRef, DeployableOwnerComponent.getComponentType());

               assert deployableOwnerComponent != null;

               deployableOwnerComponent.registerDeployable(ownerRef, deployableComponent, deployableConfig.getId(), ref, store);
            }
         }
      }

      @Override
      public void onEntityRemove(
         @Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         DeployableComponent deployableComponent = store.getComponent(ref, DeployableComponent.getComponentType());

         assert deployableComponent != null;

         TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

         assert transformComponent != null;

         DeployableConfig deployableConfig = deployableComponent.getConfig();
         Vector3d position = transformComponent.getPosition();
         int despawnSoundIndex = deployableConfig.getDespawnSoundEventIndex();
         int dieSoundIndex = deployableConfig.getDieSoundEventIndex();
         if (dieSoundIndex != 0) {
            EntityStatMap statMap = store.getComponent(ref, EntityStatMap.getComponentType());
            if (statMap != null) {
               EntityStatValue healthStat = statMap.get(DefaultEntityStatTypes.getHealth());
               int removeSound = healthStat != null && healthStat.get() <= 0.0F ? dieSoundIndex : despawnSoundIndex;
               SoundUtil.playSoundEvent3d(null, removeSound, position, commandBuffer);
            }
         } else {
            SoundUtil.playSoundEvent3d(null, despawnSoundIndex, position, commandBuffer);
         }

         ModelParticle[] particles = deployableConfig.getDespawnParticles();
         if (particles != null) {
            for (ModelParticle particle : particles) {
               DeployablesSystem.spawnParticleEffect(ref, commandBuffer, position, particle);
            }
         }

         deregisterOwner(ref, deployableComponent, deployableConfig);
      }
   }

   public static class DeployableTicker extends EntityTickingSystem<EntityStore> {
      @Override
      public Query<EntityStore> getQuery() {
         return Query.and(DeployableComponent.getComponentType());
      }

      @Override
      public void tick(
         float dt, int index, ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         DeployableComponent deployableComponent = archetypeChunk.getComponent(index, DeployableComponent.getComponentType());

         assert deployableComponent != null;

         deployableComponent.tick(dt, index, archetypeChunk, store, commandBuffer);
      }
   }
}
