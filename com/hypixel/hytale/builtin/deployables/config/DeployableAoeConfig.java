package com.hypixel.hytale.builtin.deployables.config;

import com.hypixel.hytale.builtin.deployables.component.DeployableComponent;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.Vector3f;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DeployableAoeConfig extends DeployableConfig {
   @Nonnull
   public static final BuilderCodec<DeployableAoeConfig> CODEC = BuilderCodec.builder(
         DeployableAoeConfig.class, DeployableAoeConfig::new, DeployableConfig.BASE_CODEC
      )
      .append(
         new KeyedCodec<>("Shape", new EnumCodec<>(DeployableAoeConfig.Shape.class)),
         (DeployableAoeConfig, s) -> DeployableAoeConfig.shape = s,
         DeployableAoeConfig -> DeployableAoeConfig.shape
      )
      .documentation("The shape of the detection area")
      .add()
      .<Float>append(
         new KeyedCodec<>("StartRadius", Codec.FLOAT),
         (DeployableAoeConfig, s) -> DeployableAoeConfig.startRadius = s,
         DeployableAoeConfig -> DeployableAoeConfig.startRadius
      )
      .documentation("The initial detection radius")
      .add()
      .<Float>append(
         new KeyedCodec<>("EndRadius", Codec.FLOAT),
         (DeployableAoeConfig, s) -> DeployableAoeConfig.endRadius = s,
         DeployableAoeConfig -> DeployableAoeConfig.endRadius
      )
      .documentation("If set, the detection radius will expand to this size over the RadiusChangeTime (RadiusChangeTime must be set)")
      .add()
      .<Float>append(
         new KeyedCodec<>("Height", Codec.FLOAT), (DeployableAoeConfig, s) -> DeployableAoeConfig.height = s, DeployableAoeConfig -> DeployableAoeConfig.height
      )
      .documentation("The height of the Shape, if using a cylinder shape")
      .add()
      .<Float>append(
         new KeyedCodec<>("RadiusChangeTime", Codec.FLOAT),
         (DeployableAoeConfig, s) -> DeployableAoeConfig.radiusChangeTime = s,
         DeployableAoeConfig -> DeployableAoeConfig.radiusChangeTime
      )
      .documentation("The time (starting at spawn) it takes to change from StartRadius to EndRadius")
      .add()
      .<Float>append(
         new KeyedCodec<>("DamageInterval", Codec.FLOAT),
         (DeployableAoeConfig, s) -> DeployableAoeConfig.damageInterval = s,
         DeployableAoeConfig -> DeployableAoeConfig.damageInterval
      )
      .documentation("The interval between damage being applied to targets in seconds")
      .add()
      .<Float>append(
         new KeyedCodec<>("DamageAmount", Codec.FLOAT),
         (DeployableAoeConfig, s) -> DeployableAoeConfig.damageAmount = s,
         DeployableAoeConfig -> DeployableAoeConfig.damageAmount
      )
      .documentation("The amount of damage to apply to targets per interval")
      .add()
      .<String>append(
         new KeyedCodec<>("DamageCause", DamageCause.CHILD_ASSET_CODEC),
         (DeployableAoeConfig, s) -> DeployableAoeConfig.damageCause = s,
         DeployableAoeConfig -> DeployableAoeConfig.damageCause
      )
      .documentation("The amount of damage to apply to targets per interval")
      .add()
      .append(
         new KeyedCodec<>("ApplyEffects", new ArrayCodec<>(EntityEffect.CHILD_ASSET_CODEC, String[]::new)),
         (DeployableAoeConfig, s) -> DeployableAoeConfig.effectsToApply = s,
         DeployableAoeConfig -> DeployableAoeConfig.effectsToApply
      )
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("AttackOwner", Codec.BOOLEAN), (o, i) -> o.attackOwner = i, o -> o.attackOwner, (i, o) -> i.attackOwner = o.attackOwner
      )
      .documentation("Whether or not the owner is affected by the attack & effect of this deployable")
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("AttackTeam", Codec.BOOLEAN), (o, i) -> o.attackTeam = i, o -> o.attackTeam, (i, o) -> i.attackTeam = o.attackTeam
      )
      .documentation("Whether or not the team is affected by the attack & effect of this deployable")
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("AttackEnemies", Codec.BOOLEAN), (o, i) -> o.attackEnemies = i, o -> o.attackEnemies, (i, o) -> i.attackEnemies = o.attackEnemies
      )
      .documentation("Whether or not this deployable interacts with non-team entities")
      .add()
      .build();
   protected float startRadius = 1.0F;
   protected float endRadius = -1.0F;
   protected float radiusChangeTime = -1.0F;
   protected float damageInterval = 1.0F;
   protected float damageAmount = 1.0F;
   protected String damageCause = "Physical";
   protected String[] effectsToApply;
   protected boolean attackOwner;
   protected boolean attackTeam;
   protected boolean attackEnemies = true;
   protected DeployableAoeConfig.Shape shape = DeployableAoeConfig.Shape.Sphere;
   protected float height = 1.0F;
   @Nullable
   protected DamageCause processedDamageCause;

   protected DeployableAoeConfig() {
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
         Vector3d position = transformComponent.getPosition();
         World world = store.getExternalData().getWorld();
         Ref<EntityStore> entityRef = archetypeChunk.getReferenceTo(index);
         float radius = this.getRadius(store, deployableComponent.getSpawnInstant());
         this.handleDebugGraphics(world, deployableComponent.getDebugColor(), position, radius * 2.0F);
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

         Ref<EntityStore> deployableRef = archetypeChunk.getReferenceTo(index);
         if (deployableComponent.incrementTimeSinceLastAttack(dt) > this.damageInterval) {
            deployableComponent.setTimeSinceLastAttack(0.0F);
            this.handleDetection(store, commandBuffer, deployableRef, deployableComponent, position, radius, DamageCause.PHYSICAL);
         }

         super.tick(deployableComponent, dt, index, archetypeChunk, store, commandBuffer);
      }
   }

   protected void handleDetection(
      @Nonnull final Store<EntityStore> store,
      @Nonnull final CommandBuffer<EntityStore> commandBuffer,
      @Nonnull final Ref<EntityStore> deployableRef,
      @Nonnull DeployableComponent deployableComponent,
      @Nonnull Vector3d position,
      float radius,
      @Nonnull final DamageCause damageCause
   ) {
      var attackConsumer = new Consumer<Ref<EntityStore>>() {
         public void accept(@Nonnull Ref<EntityStore> entityStoreRef) {
            if (entityStoreRef != deployableRef) {
               DeployableAoeConfig.this.attackTarget(entityStoreRef, deployableRef, damageCause, commandBuffer);
               DeployableAoeConfig.this.applyEffectToTarget(store, entityStoreRef);
            }
         }
      };
      switch (this.shape) {
         case Sphere:
            for (Ref<EntityStore> targetRef : TargetUtil.getAllEntitiesInSphere(position, radius, store)) {
               attackConsumer.accept(targetRef);
            }
            break;
         case Cylinder:
            for (Ref<EntityStore> targetRef : TargetUtil.getAllEntitiesInCylinder(position, radius, this.height, store)) {
               attackConsumer.accept(targetRef);
            }
      }
   }

   protected void handleDebugGraphics(World world, Vector3f color, Vector3d position, float scale) {
      if (this.getDebugVisuals()) {
         ;
      }
   }

   protected void attackTarget(
      @Nonnull Ref<EntityStore> targetRef,
      @Nonnull Ref<EntityStore> ownerRef,
      @Nonnull DamageCause damageCause,
      @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      if (!(this.damageAmount <= 0.0F)) {
         Damage damageEntry = new Damage(new Damage.EntitySource(ownerRef), damageCause, this.damageAmount);
         if (targetRef.equals(ownerRef)) {
            damageEntry.setSource(Damage.NULL_SOURCE);
         }

         DamageSystems.executeDamage(targetRef, commandBuffer, damageEntry);
      }
   }

   protected void applyEffectToTarget(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> targetRef) {
      if (this.effectsToApply != null) {
         EffectControllerComponent effectControllerComponent = store.getComponent(targetRef, EffectControllerComponent.getComponentType());
         if (effectControllerComponent != null) {
            for (String effect : this.effectsToApply) {
               if (effect != null) {
                  EntityEffect effectAsset = EntityEffect.getAssetMap().getAsset(effect);
                  if (effectAsset != null) {
                     effectControllerComponent.addEffect(targetRef, effectAsset, store);
                  }
               }
            }
         }
      }
   }

   protected boolean canAttackEntity(@Nonnull Ref<EntityStore> targetRef, @Nonnull DeployableComponent deployable) {
      boolean isOwner = targetRef.equals(deployable.getOwner());
      return !isOwner || this.attackOwner;
   }

   protected float getRadius(@Nonnull Store<EntityStore> store, @Nonnull Instant startInstant) {
      if (!(this.radiusChangeTime <= 0.0F) && !(this.endRadius < 0.0F)) {
         float radiusDiff = this.endRadius - this.startRadius;
         float increment = radiusDiff / this.radiusChangeTime;
         Instant now = store.getResource(TimeResource.getResourceType()).getNow();
         float timeDiff = (float)Duration.between(startInstant, now).toMillis() / 1000.0F;
         if (timeDiff > this.radiusChangeTime) {
            return this.endRadius;
         } else {
            float nowIncrement = increment * timeDiff;
            return this.startRadius + nowIncrement;
         }
      } else {
         return this.startRadius;
      }
   }

   @Nullable
   protected DamageCause getDamageCause() {
      if (this.processedDamageCause == null) {
         this.processedDamageCause = DamageCause.getAssetMap().getAsset(this.damageCause);
         if (this.processedDamageCause == null) {
            this.processedDamageCause = DamageCause.PHYSICAL;
         }
      }

      return this.processedDamageCause;
   }

   @Override
   public String toString() {
      return "DeployableAoeConfig{}" + super.toString();
   }

   public static enum Shape {
      Sphere,
      Cylinder;
   }
}
