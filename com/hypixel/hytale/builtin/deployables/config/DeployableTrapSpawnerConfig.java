package com.hypixel.hytale.builtin.deployables.config;

import com.hypixel.hytale.builtin.deployables.DeployablesUtils;
import com.hypixel.hytale.builtin.deployables.component.DeployableComponent;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import javax.annotation.Nonnull;

public class DeployableTrapSpawnerConfig extends DeployableTrapConfig {
   @Nonnull
   public static final BuilderCodec<DeployableTrapSpawnerConfig> CODEC = BuilderCodec.builder(
         DeployableTrapSpawnerConfig.class, DeployableTrapSpawnerConfig::new, DeployableTrapConfig.CODEC
      )
      .appendInherited(
         new KeyedCodec<>("DeployableConfig", new ArrayCodec<>(Codec.STRING, String[]::new)),
         (o, i) -> o.deployableSpawnerIds = i,
         o -> o.deployableSpawnerIds,
         (o, p) -> o.deployableSpawnerIds = p.deployableSpawnerIds
      )
      .addValidator(Validators.nonNull())
      .add()
      .afterDecode(config -> {
         if (config.deployableSpawnerIds != null) {
            int length = config.deployableSpawnerIds.length;
            config.deployableSpawners = new DeployableSpawner[length];

            for (int i = 0; i < length; i++) {
               String key = config.deployableSpawnerIds[i];
               config.deployableSpawners[i] = DeployableSpawner.getAssetMap().getAsset(key);
            }
         }
      })
      .build();
   private String[] deployableSpawnerIds;
   private DeployableSpawner[] deployableSpawners;

   @Override
   public void tick(
      @Nonnull DeployableComponent deployableComponent,
      float dt,
      int index,
      @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      Ref<EntityStore> entityRef = archetypeChunk.getReferenceTo(index);
      switch (deployableComponent.getFlag(DeployableComponent.DeployableFlag.STATE)) {
         case 0:
            this.tickDeploymentState(store, deployableComponent, entityRef);
            break;
         case 1:
            this.tickDeployAnimationState(store, deployableComponent, entityRef);
            break;
         case 2:
            this.tickFuzeState(store, deployableComponent);
            break;
         case 3:
            this.tickLiveState(store, deployableComponent, entityRef, commandBuffer, dt);
            break;
         case 4:
            this.tickTriggeredState(commandBuffer, store, deployableComponent, entityRef);
            break;
         case 5:
            this.tickDespawnState(deployableComponent, entityRef, store);
      }
   }

   private void tickDeploymentState(@Nonnull Store<EntityStore> store, @Nonnull DeployableComponent component, @Nonnull Ref<EntityStore> entityRef) {
      component.setFlag(DeployableComponent.DeployableFlag.STATE, 1);
      playAnimation(store, entityRef, this, "Deploy");
   }

   private void tickDeployAnimationState(@Nonnull Store<EntityStore> store, @Nonnull DeployableComponent component, @Nonnull Ref<EntityStore> entityRef) {
      component.setFlag(DeployableComponent.DeployableFlag.STATE, 2);
      playAnimation(store, entityRef, this, "Deploy");
   }

   private void tickFuzeState(@Nonnull Store<EntityStore> store, @Nonnull DeployableComponent component) {
      Instant now = store.getResource(TimeResource.getResourceType()).getNow();
      Instant readyTime = component.getSpawnInstant().plus((long)this.fuzeDuration, ChronoUnit.SECONDS);
      if (now.isAfter(readyTime)) {
         component.setFlag(DeployableComponent.DeployableFlag.STATE, 3);
      }
   }

   private void tickLiveState(
      @Nonnull Store<EntityStore> store,
      @Nonnull DeployableComponent component,
      @Nonnull Ref<EntityStore> entityRef,
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      float dt
   ) {
      TransformComponent transformComponent = store.getComponent(entityRef, TransformComponent.getComponentType());
      if (transformComponent != null) {
         Vector3d position = transformComponent.getPosition();
         float radius = this.getRadius(store, component.getSpawnInstant());
         component.setTimeSinceLastAttack(component.getTimeSinceLastAttack() + dt);
         if (component.getTimeSinceLastAttack() > this.damageInterval && this.isLive(store, component)) {
            component.setTimeSinceLastAttack(0.0F);
            this.handleDetection(store, commandBuffer, entityRef, component, position, radius, DamageCause.PHYSICAL);
         }
      }
   }

   private void tickTriggeredState(
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      @Nonnull Store<EntityStore> store,
      @Nonnull DeployableComponent component,
      @Nonnull Ref<EntityStore> entityRef
   ) {
      TransformComponent transformComponent = store.getComponent(entityRef, TransformComponent.getComponentType());
      if (transformComponent != null) {
         component.setFlag(DeployableComponent.DeployableFlag.STATE, 5);
         Vector3d parentPosition = transformComponent.getPosition();
         Ref<EntityStore> parentOwner = component.getOwner();
         World world = store.getExternalData().getWorld();
         if (this.deployableSpawners != null) {
            for (DeployableSpawner spawner : this.deployableSpawners) {
               if (spawner != null) {
                  DeployableConfig config = spawner.getConfig();
                  Vector3d[] positionOffsets = spawner.getPositionOffsets();

                  for (Vector3d offset : positionOffsets) {
                     Vector3f childPosition = Vector3d.add(parentPosition, offset).toVector3f();
                     world.execute(() -> DeployablesUtils.spawnDeployable(commandBuffer, store, config, parentOwner, childPosition, new Vector3f(), "UP"));
                  }
               }
            }
         }
      }
   }

   private void tickDespawnState(@Nonnull DeployableComponent component, @Nonnull Ref<EntityStore> entityRef, @Nonnull Store<EntityStore> store) {
      component.setFlag(DeployableComponent.DeployableFlag.STATE, 6);
      super.onTriggered(store, entityRef);
   }

   @Override
   protected void onTriggered(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref) {
      DeployableComponent deployableComponent = store.getComponent(ref, DeployableComponent.getComponentType());
      if (deployableComponent != null) {
         deployableComponent.setFlag(DeployableComponent.DeployableFlag.STATE, 4);
      }
   }
}
