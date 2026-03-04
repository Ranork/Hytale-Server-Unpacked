package com.hypixel.hytale.builtin.deployables.config;

import com.hypixel.hytale.builtin.deployables.DeployablesUtils;
import com.hypixel.hytale.builtin.deployables.component.DeployableComponent;
import com.hypixel.hytale.builtin.deployables.component.DeployableProjectileComponent;
import com.hypixel.hytale.builtin.deployables.component.DeployableProjectileShooterComponent;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.protocol.Opacity;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.knockback.KnockbackComponent;
import com.hypixel.hytale.server.core.modules.debug.DebugUtils;
import com.hypixel.hytale.server.core.modules.entity.DespawnComponent;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.combat.Knockback;
import com.hypixel.hytale.server.core.modules.projectile.config.ProjectileConfig;
import com.hypixel.hytale.server.core.modules.projectile.config.StandardPhysicsProvider;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nonnull;

public class DeployableTurretConfig extends DeployableConfig {
   @Nonnull
   public static final BuilderCodec<DeployableTurretConfig> CODEC = BuilderCodec.builder(
         DeployableTurretConfig.class, DeployableTurretConfig::new, DeployableConfig.BASE_CODEC
      )
      .appendInherited(
         new KeyedCodec<>("TrackableRadius", Codec.FLOAT),
         (o, i) -> o.trackableRadius = i,
         o -> o.trackableRadius,
         (o, p) -> o.trackableRadius = p.trackableRadius
      )
      .documentation("The radius in which a targeted entity can be tracked")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("DetectionRadius", Codec.FLOAT),
         (o, i) -> o.detectionRadius = i,
         o -> o.detectionRadius,
         (o, p) -> o.detectionRadius = p.detectionRadius
      )
      .documentation("The radius in which an entity can be targeted")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("RotationSpeed", Codec.FLOAT), (o, i) -> o.rotationSpeed = i, o -> o.rotationSpeed, (o, p) -> o.rotationSpeed = p.rotationSpeed
      )
      .documentation("The speed at which the turret can rotate to hit it's target")
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("PreferOwnerTarget", Codec.BOOLEAN),
         (o, i) -> o.preferOwnerTarget = i,
         o -> o.preferOwnerTarget,
         (o, p) -> o.preferOwnerTarget = p.preferOwnerTarget
      )
      .documentation("If true, will prefer targeting entities that the owner is attacking")
      .add()
      .<Integer>appendInherited(new KeyedCodec<>("Ammo", Codec.INTEGER), (o, i) -> o.ammo = i, o -> o.ammo, (o, p) -> o.ammo = p.ammo)
      .documentation("The total ammo the turret has, each projectile will consume one")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("DeployDelay", Codec.FLOAT), (o, i) -> o.deployDelay = i, o -> o.deployDelay, (o, p) -> o.deployDelay = p.deployDelay
      )
      .documentation("The delay in seconds until the deployable is ready to begin targeting logic")
      .add()
      .<ProjectileConfig>appendInherited(
         new KeyedCodec<>("ProjectileConfig", ProjectileConfig.CODEC),
         (o, i) -> o.projectileConfig = i,
         o -> o.projectileConfig,
         (o, p) -> o.projectileConfig = p.projectileConfig
      )
      .addValidator(Validators.nonNull())
      .add()
      .appendInherited(
         new KeyedCodec<>("ShotInterval", Codec.FLOAT), (o, i) -> o.shotInterval = i, o -> o.shotInterval, (o, p) -> o.shotInterval = p.shotInterval
      )
      .add()
      .appendInherited(new KeyedCodec<>("BurstCount", Codec.INTEGER), (o, i) -> o.burstCount = i, o -> o.burstCount, (o, p) -> o.burstCount = p.burstCount)
      .add()
      .appendInherited(
         new KeyedCodec<>("BurstCooldown", Codec.FLOAT), (o, i) -> o.burstCooldown = i, o -> o.burstCooldown, (o, p) -> o.burstCooldown = p.burstCooldown
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("ProjectileDamage", Codec.FLOAT),
         (o, i) -> o.projectileDamage = i,
         o -> o.projectileDamage,
         (o, p) -> o.projectileDamage = p.projectileDamage
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("CanShootOwner", Codec.BOOLEAN), (o, i) -> o.canShootOwner = i, o -> o.canShootOwner, (o, p) -> o.canShootOwner = p.canShootOwner
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("Knockback", Knockback.CODEC),
         (i, s) -> i.projectileKnockback = s,
         i -> i.projectileKnockback,
         (i, parent) -> i.projectileKnockback = parent.projectileKnockback
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("TargetOffset", Vector3d.CODEC),
         (i, s) -> i.targetOffset = s,
         i -> i.targetOffset,
         (i, parent) -> i.targetOffset = parent.targetOffset
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("DoLineOfSightTest", Codec.BOOLEAN),
         (o, i) -> o.doLineOfSightTest = i,
         o -> o.doLineOfSightTest,
         (o, p) -> o.doLineOfSightTest = p.doLineOfSightTest
      )
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("ProjectileHitWorldSoundEventId", Codec.STRING),
         (o, i) -> o.projectileHitWorldSoundEventId = i,
         o -> o.projectileHitWorldSoundEventId,
         (o, p) -> o.projectileHitWorldSoundEventId = p.projectileHitWorldSoundEventId
      )
      .documentation("The positioned sound event played to surrounding players when the projectile hits a player")
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("ProjectileHitLocalSoundEventId", Codec.STRING),
         (o, i) -> o.projectileHitLocalSoundEventId = i,
         o -> o.projectileHitLocalSoundEventId,
         (o, p) -> o.projectileHitLocalSoundEventId = p.projectileHitLocalSoundEventId
      )
      .documentation("The positioned sound event played to a player hit by the projectile")
      .add()
      .appendInherited(
         new KeyedCodec<>("RespectTeams", Codec.BOOLEAN), (o, i) -> o.respectTeams = i, o -> o.respectTeams, (o, p) -> o.respectTeams = p.respectTeams
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("ProjectileSpawnOffsets", new MapCodec<>(Vector3d.CODEC, Object2ObjectOpenHashMap::new, true)),
         (o, i) -> o.projectileSpawnOffsets = i,
         o -> o.projectileSpawnOffsets,
         (o, p) -> o.projectileSpawnOffsets = p.projectileSpawnOffsets
      )
      .add()
      .afterDecode(DeployableTurretConfig::processConfig)
      .build();
   protected float trackableRadius;
   protected float detectionRadius;
   protected float rotationSpeed;
   protected float projectileDamage;
   protected boolean preferOwnerTarget;
   protected int ammo;
   protected ProjectileConfig projectileConfig;
   protected float deployDelay;
   protected float shotInterval;
   protected int burstCount;
   protected float burstCooldown;
   protected boolean canShootOwner;
   protected Knockback projectileKnockback;
   protected Vector3d targetOffset = new Vector3d(0.0, 0.0, 0.0);
   protected boolean doLineOfSightTest = true;
   protected String projectileHitWorldSoundEventId;
   protected String projectileHitLocalSoundEventId;
   protected int projectileHitLocalSoundEventIndex = 0;
   protected int projectileHitWorldSoundEventIndex = 0;
   protected boolean respectTeams = true;
   protected Map<String, Vector3d> projectileSpawnOffsets = new Object2ObjectOpenHashMap();

   protected void processConfig() {
      if (this.projectileHitWorldSoundEventId != null) {
         this.projectileHitWorldSoundEventIndex = this.projectileHitLocalSoundEventIndex = SoundEvent.getAssetMap()
            .getIndex(this.projectileHitWorldSoundEventId);
      }

      if (this.projectileHitLocalSoundEventId != null) {
         this.projectileHitLocalSoundEventIndex = SoundEvent.getAssetMap().getIndex(this.projectileHitLocalSoundEventId);
      }
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
      Ref<EntityStore> entityRef = archetypeChunk.getReferenceTo(index);
      switch (deployableComponent.getFlag(DeployableComponent.DeployableFlag.STATE)) {
         case 0:
            this.tickInitState(entityRef, deployableComponent, store, commandBuffer);
            break;
         case 1:
            this.tickStartDeployState(entityRef, deployableComponent, store);
            break;
         case 2:
            this.tickAwaitDeployState(entityRef, deployableComponent, store);
            break;
         case 3:
            this.tickAttackState(entityRef, deployableComponent, dt, store, commandBuffer);
      }
   }

   private void tickInitState(
      @Nonnull Ref<EntityStore> entityRef,
      @Nonnull DeployableComponent component,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      component.setFlag(DeployableComponent.DeployableFlag.STATE, 1);
      commandBuffer.addComponent(entityRef, DeployableProjectileShooterComponent.getComponentType());
      playAnimation(store, entityRef, this, "Deploy");
   }

   private void tickStartDeployState(@Nonnull Ref<EntityStore> ref, @Nonnull DeployableComponent component, @Nonnull Store<EntityStore> store) {
      component.setFlag(DeployableComponent.DeployableFlag.STATE, 2);
      playAnimation(store, ref, this, "Deploy");
   }

   private void tickAwaitDeployState(@Nonnull Ref<EntityStore> ref, @Nonnull DeployableComponent component, @Nonnull Store<EntityStore> store) {
      Instant now = store.getResource(TimeResource.getResourceType()).getNow();
      Instant readyTime = component.getSpawnInstant().plus((long)this.deployDelay, ChronoUnit.SECONDS);
      if (now.isAfter(readyTime)) {
         component.setFlag(DeployableComponent.DeployableFlag.STATE, 3);
         playAnimation(store, ref, this, "Loop");
      }
   }

   private void tickAttackState(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull DeployableComponent component,
      float dt,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      component.setTimeSinceLastAttack(component.getTimeSinceLastAttack() + dt);
      World world = commandBuffer.getExternalData().getWorld();
      DeployableProjectileShooterComponent shooterComponent = store.getComponent(ref, DeployableProjectileShooterComponent.getComponentType());
      Vector3d spawnPos = Vector3d.ZERO.clone();
      if (this.projectileSpawnOffsets != null) {
         Vector3d spawnOffset = this.projectileSpawnOffsets.get(component.getSpawnFace());
         if (spawnOffset != null) {
            spawnPos.add(spawnOffset);
         }
      }

      if (shooterComponent == null) {
         world.execute(() -> {
            if (ref.isValid()) {
               DespawnComponent despawn = store.ensureAndGetComponent(ref, DespawnComponent.getComponentType());
               WorldTimeResource timeManager = commandBuffer.getResource(WorldTimeResource.getResourceType());
               despawn.setDespawn(timeManager.getGameTime());
            }
         });
      } else {
         TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

         assert transformComponent != null;

         HeadRotation headRotationComponent = store.getComponent(ref, HeadRotation.getComponentType());

         assert headRotationComponent != null;

         Vector3d pos = Vector3d.add(spawnPos, transformComponent.getPosition());
         this.updateProjectiles(store, commandBuffer, shooterComponent);
         boolean hasTarget = false;
         Ref<EntityStore> target = shooterComponent.getActiveTarget();
         if (target != null && target.isValid()) {
            TransformComponent targetTransformComponent = store.getComponent(target, TransformComponent.getComponentType());

            assert targetTransformComponent != null;

            Vector3d targetPos = this.calculatedTargetPosition(targetTransformComponent.getPosition());
            Vector3d direction = Vector3d.directionTo(pos, targetPos);
            if (targetPos.distanceTo(pos) <= this.trackableRadius && this.testLineOfSight(pos, targetPos, direction, commandBuffer)) {
               hasTarget = true;
            }
         }

         if (!hasTarget) {
            Ref<EntityStore> closestTarget = null;
            Vector3d closestTargetPos = Vector3d.MAX;

            for (Ref<EntityStore> potentialTargetRef : TargetUtil.getAllEntitiesInSphere(pos, this.detectionRadius, commandBuffer)) {
               if (potentialTargetRef != null && potentialTargetRef.isValid()) {
                  TransformComponent targetTransformComponentx = store.getComponent(potentialTargetRef, TransformComponent.getComponentType());

                  assert targetTransformComponentx != null;

                  Vector3d targetPosition = this.calculatedTargetPosition(targetTransformComponentx.getPosition());
                  Vector3d direction = Vector3d.directionTo(pos, targetPosition);
                  if (this.testLineOfSight(pos, targetPosition, direction, commandBuffer)
                     && this.isValidTarget(ref, store, potentialTargetRef)
                     && pos.distanceTo(targetPosition) < pos.distanceTo(closestTargetPos)) {
                     closestTargetPos = targetPosition;
                     closestTarget = potentialTargetRef;
                  }
               }
            }

            if (closestTarget != null) {
               shooterComponent.setActiveTarget(closestTarget);
               target = closestTarget;
               hasTarget = true;
            }
         }

         Vector3d targetPos = Vector3d.ZERO;
         Vector3f targetLookRotation = Vector3f.ZERO;
         Vector3f lookRotation = Vector3f.ZERO;
         if (hasTarget) {
            TransformComponent targetTransformComponentxx = store.getComponent(target, TransformComponent.getComponentType());

            assert targetTransformComponentxx != null;

            targetPos = this.calculatedTargetPosition(targetTransformComponentxx.getPosition().clone());
            Vector3d relativeTargetOffset = new Vector3d(pos.x - targetPos.x, pos.y - targetPos.y, pos.z - targetPos.z);
            targetLookRotation = Vector3f.lookAt(relativeTargetOffset.negate());
            lookRotation = Vector3f.lerpAngle(headRotationComponent.getRotation(), targetLookRotation, this.rotationSpeed * dt);
         }

         headRotationComponent.setRotation(lookRotation);
         int shotsFired = component.getFlag(DeployableComponent.DeployableFlag.BURST_SHOTS);
         float timeSinceLastAttack = component.getTimeSinceLastAttack();
         boolean canFire = false;
         if (shotsFired < this.burstCount && timeSinceLastAttack >= this.shotInterval) {
            component.setFlag(DeployableComponent.DeployableFlag.BURST_SHOTS, shotsFired + 1);
            canFire = true;
         } else if (shotsFired >= this.burstCount && timeSinceLastAttack >= this.burstCooldown) {
            component.setFlag(DeployableComponent.DeployableFlag.BURST_SHOTS, 1);
            canFire = true;
         }

         if (canFire && hasTarget) {
            Vector3d fwdDirection = new Vector3d().assign(lookRotation.getYaw(), lookRotation.getPitch());
            Vector3d rootPos = transformComponent.getPosition();
            Vector3d projectileSpawnPos = Vector3d.ZERO.clone();
            if (this.projectileSpawnOffsets != null) {
               projectileSpawnPos = this.projectileSpawnOffsets.getOrDefault(component.getSpawnFace(), Vector3d.ZERO).clone();
            }

            projectileSpawnPos.add(fwdDirection.clone().normalize());
            projectileSpawnPos.add(rootPos);
            UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());
            if (uuidComponent != null) {
               UUID uuid = uuidComponent.getUuid();
               shooterComponent.spawnProjectile(ref, commandBuffer, this.projectileConfig, uuid, projectileSpawnPos, fwdDirection.clone());
            }

            playAnimation(store, ref, this, "Shoot");
            component.setTimeSinceLastAttack(0.0F);
         }
      }
   }

   @Nonnull
   private Vector3d calculatedTargetPosition(@Nonnull Vector3d original) {
      return Vector3d.add(original.clone(), this.targetOffset);
   }

   private boolean isValidTarget(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> targetRef) {
      if (targetRef.equals(ref)) {
         return false;
      } else {
         DeployableComponent deployableComponent = store.getComponent(ref, DeployableComponent.getComponentType());
         return deployableComponent == null ? true : this.canShootOwner || !targetRef.equals(deployableComponent.getOwner());
      }
   }

   private boolean testLineOfSight(
      @Nonnull Vector3d attackerPos, @Nonnull Vector3d targetPos, @Nonnull Vector3d direction, @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      if (!this.doLineOfSightTest) {
         return true;
      } else {
         com.hypixel.hytale.protocol.Vector3f spawnOffset = this.projectileConfig.getSpawnOffset();
         Vector3d testFromPos = attackerPos.clone().add(spawnOffset.x, spawnOffset.y + this.generatedModel.getEyeHeight(), spawnOffset.z);
         double distance = testFromPos.distanceTo(targetPos);
         World world = commandBuffer.getExternalData().getWorld();
         Vector3f whiteColor = new Vector3f(1.0F, 1.0F, 1.0F);
         if (this.getDebugVisuals()) {
            Vector3d increment = direction.scale(distance);

            for (int i = 0; i < 10; i++) {
               Vector3d pos = testFromPos.clone();
               pos.addScaled(increment, i / 10.0F);
               DebugUtils.addSphere(world, pos, whiteColor, 0.1F, 0.5F);
            }
         }

         Vector3i blockPosition = TargetUtil.getTargetBlock(world, (id, fluid_id) -> {
            if (id == 0) {
               return false;
            } else {
               BlockType blockType = BlockType.getAssetMap().getAsset(id);
               if (blockType == null) {
                  return false;
               } else {
                  BlockMaterial material = blockType.getMaterial();
                  return material != null && material != BlockMaterial.Empty ? blockType.getOpacity() != Opacity.Transparent : false;
               }
            }
         }, attackerPos.x, attackerPos.y, attackerPos.z, direction.x, direction.y, direction.z, distance);
         if (blockPosition == null) {
            return true;
         } else {
            double entityDistance = attackerPos.distanceSquaredTo(targetPos);
            double blockDistance = attackerPos.distanceSquaredTo(blockPosition.x + 0.5, blockPosition.y + 0.5, blockPosition.z + 0.5);
            return entityDistance < blockDistance;
         }
      }
   }

   private void updateProjectiles(
      @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull DeployableProjectileShooterComponent shooterComponent
   ) {
      List<Ref<EntityStore>> projectiles = shooterComponent.getProjectiles();
      List<Ref<EntityStore>> projectilesForRemoval = shooterComponent.getProjectilesForRemoval();
      projectiles.removeAll(Collections.singleton(null));

      for (Ref<EntityStore> projectile : projectiles) {
         this.updateProjectile(projectile, shooterComponent, store, commandBuffer);
      }

      for (Ref<EntityStore> projectile : projectilesForRemoval) {
         if (projectile.isValid()) {
            commandBuffer.removeEntity(projectile, RemoveReason.REMOVE);
         }

         projectiles.remove(projectile);
      }

      projectilesForRemoval.clear();
   }

   private void updateProjectile(
      @Nonnull Ref<EntityStore> projectileRef,
      @Nonnull DeployableProjectileShooterComponent shooterComponent,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      if (!projectileRef.isValid()) {
         shooterComponent.getProjectilesForRemoval().add(projectileRef);
      } else {
         TransformComponent projTransformComponent = store.getComponent(projectileRef, TransformComponent.getComponentType());
         if (projTransformComponent == null) {
            shooterComponent.getProjectilesForRemoval().add(projectileRef);
         } else {
            Vector3d projPos = projTransformComponent.getPosition();
            AtomicReference<Boolean> hit = new AtomicReference<>(Boolean.FALSE);
            DeployableProjectileComponent deployableProjectileComponent = store.getComponent(projectileRef, DeployableProjectileComponent.getComponentType());
            if (deployableProjectileComponent == null) {
               shooterComponent.getProjectilesForRemoval().add(projectileRef);
            } else {
               Vector3d prevPos = deployableProjectileComponent.getPreviousTickPosition();
               Vector3d increment = new Vector3d((projPos.x - prevPos.x) * 0.1F, (projPos.y - prevPos.y) * 0.1F, (projPos.z - prevPos.z) * 0.1F);

               for (int j = 0; j < 10; j++) {
                  if (!hit.get()) {
                     Vector3d scanPos = deployableProjectileComponent.getPreviousTickPosition().clone();
                     scanPos.x = scanPos.x + increment.x * j;
                     scanPos.y = scanPos.y + increment.y * j;
                     scanPos.z = scanPos.z + increment.z * j;
                     if (this.getDebugVisuals()) {
                        DebugUtils.addSphere(store.getExternalData().getWorld(), scanPos, new Vector3f(1.0F, 1.0F, 1.0F), 0.1F, 5.0F);
                     }

                     for (Ref<EntityStore> targetEntityRef : TargetUtil.getAllEntitiesInSphere(scanPos, 0.1, store)) {
                        if (hit.get()) {
                           return;
                        }

                        this.projectileHit(targetEntityRef, projectileRef, shooterComponent, store, commandBuffer);
                        hit.set(Boolean.TRUE);
                     }
                  }
               }

               deployableProjectileComponent.setPreviousTickPosition(projPos);
               if (!hit.get()) {
                  StandardPhysicsProvider physicsComponent = store.getComponent(projectileRef, StandardPhysicsProvider.getComponentType());
                  if (physicsComponent != null && physicsComponent.getState() != StandardPhysicsProvider.STATE.ACTIVE) {
                     shooterComponent.getProjectilesForRemoval().add(projectileRef);
                  }
               }
            }
         }
      }
   }

   private void projectileHit(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Ref<EntityStore> projectileRef,
      @Nonnull DeployableProjectileShooterComponent shooterComponent,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      Damage damageEntry = new Damage(new Damage.EntitySource(ref), DamageCause.PHYSICAL, this.projectileDamage);
      DamageSystems.executeDamage(ref, commandBuffer, damageEntry);
      TransformComponent projectileTransformComponent = store.getComponent(projectileRef, TransformComponent.getComponentType());

      assert projectileTransformComponent != null;

      Vector3d projectilePosition = projectileTransformComponent.getPosition().clone();
      if (this.projectileKnockback != null) {
         float projectileRotationYaw = projectileTransformComponent.getRotation().getYaw();
         store.getExternalData().getWorld().execute(() -> {
            if (ref.isValid()) {
               this.applyKnockback(ref, projectilePosition, projectileRotationYaw, store);
            }
         });
      }

      DeployablesUtils.playSoundEventsAtEntity(
         ref, commandBuffer, this.projectileHitLocalSoundEventIndex, this.projectileHitWorldSoundEventIndex, projectilePosition
      );
      shooterComponent.getProjectilesForRemoval().add(projectileRef);
   }

   private void applyKnockback(@Nonnull Ref<EntityStore> targetRef, @Nonnull Vector3d attackerPos, float attackerYaw, @Nonnull Store<EntityStore> store) {
      KnockbackComponent knockbackComponent = store.ensureAndGetComponent(targetRef, KnockbackComponent.getComponentType());
      TransformComponent transformComponent = store.getComponent(targetRef, TransformComponent.getComponentType());

      assert transformComponent != null;

      knockbackComponent.setVelocity(this.projectileKnockback.calculateVector(attackerPos, attackerYaw, transformComponent.getPosition()));
      knockbackComponent.setVelocityType(this.projectileKnockback.getVelocityType());
      knockbackComponent.setVelocityConfig(this.projectileKnockback.getVelocityConfig());
      knockbackComponent.setDuration(this.projectileKnockback.getDuration());
   }

   @Nonnull
   @Override
   public String toString() {
      return "DeployableTurretConfig{}" + super.toString();
   }
}
