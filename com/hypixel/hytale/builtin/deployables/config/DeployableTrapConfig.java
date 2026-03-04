package com.hypixel.hytale.builtin.deployables.config;

import com.hypixel.hytale.builtin.deployables.component.DeployableComponent;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.DespawnComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.function.Consumer;
import javax.annotation.Nonnull;

public class DeployableTrapConfig extends DeployableAoeConfig {
   @Nonnull
   public static final BuilderCodec<DeployableTrapConfig> CODEC = BuilderCodec.builder(
         DeployableTrapConfig.class, DeployableTrapConfig::new, DeployableAoeConfig.CODEC
      )
      .appendInherited(
         new KeyedCodec<>("FuzeDuration", Codec.FLOAT), (o, i) -> o.fuzeDuration = i, o -> o.fuzeDuration, (o, p) -> o.fuzeDuration = p.fuzeDuration
      )
      .documentation("The time it will take for the trap to become active")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("ActiveDuration", Codec.FLOAT), (o, i) -> o.activeDuration = i, o -> o.activeDuration, (o, p) -> o.activeDuration = p.activeDuration
      )
      .documentation("The time the trap will stay alive after getting triggered")
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("DestroyOnTriggered", Codec.BOOLEAN),
         (o, i) -> o.destroyOnTriggered = i,
         o -> o.destroyOnTriggered,
         (o, p) -> o.destroyOnTriggered = p.destroyOnTriggered
      )
      .documentation("Whether the trap will disappear when it's triggered by a players")
      .add()
      .build();
   protected float fuzeDuration = 0.0F;
   protected float activeDuration = 1.0F;
   protected boolean destroyOnTriggered = false;

   protected DeployableTrapConfig() {
   }

   @Override
   public void tick(
      @Nonnull DeployableComponent deployableComponent,
      float dt,
      int index,
      @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      TransformComponent transformComponent = archetypeChunk.getComponent(index, TransformComponent.getComponentType());
      if (transformComponent != null) {
         Vector3d pos = transformComponent.getPosition();
         World world = store.getExternalData().getWorld();
         Ref<EntityStore> entityRef = archetypeChunk.getReferenceTo(index);
         if (!deployableComponent.getOwner().isValid()) {
            world.execute(() -> {
               if (entityRef.isValid()) {
                  DespawnComponent despawn = store.ensureAndGetComponent(entityRef, DespawnComponent.getComponentType());
                  WorldTimeResource timeManager = commandBuffer.getResource(WorldTimeResource.getResourceType());
                  despawn.setDespawn(timeManager.getGameTime());
               }
            });
         } else {
            float radius = this.getRadius(store, deployableComponent.getSpawnInstant());
            this.handleDebugGraphics(world, deployableComponent.getDebugColor(), pos, radius * 2.0F);
            switch (deployableComponent.getFlag(DeployableComponent.DeployableFlag.STATE)) {
               case 0:
                  deployableComponent.setFlag(DeployableComponent.DeployableFlag.STATE, 1);
                  break;
               case 1:
                  deployableComponent.setFlag(DeployableComponent.DeployableFlag.STATE, 2);
                  playAnimation(store, entityRef, this, "Grow");
                  break;
               case 2:
                  if (radius >= this.endRadius) {
                     deployableComponent.setFlag(DeployableComponent.DeployableFlag.STATE, 3);
                     playAnimation(store, entityRef, this, "Looping");
                  }
            }

            Ref<EntityStore> trapRef = archetypeChunk.getReferenceTo(index);
            deployableComponent.setTimeSinceLastAttack(deployableComponent.getTimeSinceLastAttack() + dt);
            if (deployableComponent.getTimeSinceLastAttack() > this.damageInterval && this.isLive(store, deployableComponent)) {
               deployableComponent.setTimeSinceLastAttack(0.0F);
               this.handleDetection(store, commandBuffer, trapRef, deployableComponent, pos, radius, DamageCause.PHYSICAL);
            }
         }
      }
   }

   @Override
   protected void handleDetection(
      @Nonnull final Store<EntityStore> store,
      @Nonnull final CommandBuffer<EntityStore> commandBuffer,
      @Nonnull final Ref<EntityStore> deployableRef,
      @Nonnull final DeployableComponent deployableComponent,
      @Nonnull Vector3d position,
      float radius,
      @Nonnull final DamageCause damageCause
   ) {
      World world = store.getExternalData().getWorld();
      var consumer = new Consumer<Ref<EntityStore>>() {
         public void accept(@Nonnull Ref<EntityStore> ref) {
            if (ref != deployableRef) {
               if (store.getComponent(ref, DeployableComponent.getComponentType()) == null) {
                  DeployableTrapConfig.this.attackTarget(ref, deployableRef, damageCause, commandBuffer);
                  if (DeployableTrapConfig.this.destroyOnTriggered && deployableComponent.getFlag(DeployableComponent.DeployableFlag.TRIGGERED) == 0) {
                     DeployableTrapConfig.this.onTriggered(store, deployableRef);
                     deployableComponent.setFlag(DeployableComponent.DeployableFlag.TRIGGERED, 1);
                  }

                  DeployableTrapConfig.this.applyEffectToTarget(store, ref);
               }
            }
         }
      };
      switch (this.shape) {
         case Sphere:
            for (Ref<EntityStore> targetRef : TargetUtil.getAllEntitiesInSphere(position, radius, store)) {
               consumer.accept(targetRef);
            }
            break;
         case Cylinder:
            for (Ref<EntityStore> targetRef : TargetUtil.getAllEntitiesInCylinder(position, radius, this.height, store)) {
               consumer.accept(targetRef);
            }
      }
   }

   protected boolean isLive(@Nonnull Store<EntityStore> store, @Nonnull DeployableComponent comp) {
      if (comp.getFlag(DeployableComponent.DeployableFlag.LIVE) == 1) {
         return true;
      } else if (this.fuzeDuration == 0.0F) {
         comp.setFlag(DeployableComponent.DeployableFlag.LIVE, 1);
         return true;
      } else {
         Instant now = store.getResource(TimeResource.getResourceType()).getNow();
         Instant spawnTime = comp.getSpawnInstant();
         float timeDiff = (float)Duration.between(spawnTime, now).toMillis() / 1000.0F;
         if (timeDiff >= this.fuzeDuration) {
            comp.setFlag(DeployableComponent.DeployableFlag.LIVE, 1);
            return true;
         } else {
            return false;
         }
      }
   }

   protected void onTriggered(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref) {
      Instant now = store.getResource(TimeResource.getResourceType()).getNow();
      DespawnComponent despawnComponent = store.getComponent(ref, DespawnComponent.getComponentType());
      despawnComponent.setDespawn(now.plus((long)this.activeDuration, ChronoUnit.SECONDS));
   }

   @Override
   public String toString() {
      return "DeployableTrapConfig{}" + super.toString();
   }
}
