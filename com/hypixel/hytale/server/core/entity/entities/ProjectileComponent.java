package com.hypixel.hytale.server.core.entity.entities;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.particle.config.WorldParticle;
import com.hypixel.hytale.server.core.asset.type.projectile.config.Projectile;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.ExplosionConfig;
import com.hypixel.hytale.server.core.entity.ExplosionUtils;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.DespawnComponent;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.modules.physics.SimplePhysicsProvider;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ProjectileComponent implements Component<EntityStore> {
   @Nonnull
   public static final BuilderCodec<ProjectileComponent> CODEC = BuilderCodec.builder(ProjectileComponent.class, ProjectileComponent::new)
      .append(
         new KeyedCodec<>("ProjectileType", Codec.STRING),
         (projectileEntity, projectileName) -> projectileEntity.projectileAssetName = projectileName,
         projectileEntity -> projectileEntity.projectileAssetName
      )
      .add()
      .append(
         new KeyedCodec<>("BrokenDamageModifier", Codec.FLOAT),
         (projectileEntity, brokenDamageModifier) -> projectileEntity.brokenDamageModifier = brokenDamageModifier,
         projectileEntity -> projectileEntity.brokenDamageModifier
      )
      .add()
      .append(
         new KeyedCodec<>("DeadTimer", Codec.DOUBLE),
         (projectileEntity, deadTimer) -> projectileEntity.deadTimer = deadTimer,
         projectileEntity -> projectileEntity.deadTimer
      )
      .add()
      .append(
         new KeyedCodec<>("CreatorUUID", Codec.UUID_STRING),
         (projectileEntity, creatorUuid) -> projectileEntity.creatorUuid = creatorUuid,
         projectileEntity -> projectileEntity.creatorUuid
      )
      .add()
      .append(
         new KeyedCodec<>("HaveHit", Codec.BOOLEAN),
         (projectileEntity, haveHit) -> projectileEntity.haveHit = haveHit,
         projectileEntity -> projectileEntity.haveHit
      )
      .add()
      .append(
         new KeyedCodec<>("LastBouncePosition", Vector3d.CODEC),
         (projectileEntity, lastBouncePosition) -> projectileEntity.lastBouncePosition = lastBouncePosition,
         projectileEntity -> projectileEntity.lastBouncePosition
      )
      .add()
      .append(
         new KeyedCodec<>("SppImpacted", Codec.BOOLEAN),
         (projectileEntity, b) -> projectileEntity.simplePhysicsProvider.setImpacted(b),
         projectileEntity -> projectileEntity.simplePhysicsProvider.isImpacted()
      )
      .add()
      .append(
         new KeyedCodec<>("SppResting", Codec.BOOLEAN),
         (projectileEntity, b) -> projectileEntity.simplePhysicsProvider.setResting(b),
         projectileEntity -> projectileEntity.simplePhysicsProvider.isResting()
      )
      .add()
      .append(
         new KeyedCodec<>("SppVelocity", Vector3d.CODEC),
         (projectileEntity, v) -> projectileEntity.simplePhysicsProvider.setVelocity(v),
         projectileEntity -> projectileEntity.simplePhysicsProvider.getVelocity()
      )
      .add()
      .build();
   private static final double DEFAULT_DESPAWN_SECONDS = 60.0;
   private transient SimplePhysicsProvider simplePhysicsProvider = new SimplePhysicsProvider(this::bounceHandler, this::impactHandler);
   private transient String appearance = "Boy";
   @Nullable
   private transient Projectile projectile;
   private String projectileAssetName;
   private float brokenDamageModifier = 1.0F;
   private double deadTimer = -1.0;
   private UUID creatorUuid;
   private boolean haveHit;
   private Vector3d lastBouncePosition;

   @Nonnull
   public static ComponentType<EntityStore, ProjectileComponent> getComponentType() {
      return EntityModule.get().getProjectileComponentType();
   }

   private ProjectileComponent() {
   }

   public ProjectileComponent(@Nonnull String projectileAssetName) {
      this.projectileAssetName = projectileAssetName;
   }

   @Nonnull
   public static Holder<EntityStore> assembleDefaultProjectile(
      @Nonnull TimeResource time, @Nonnull String projectileAssetName, @Nonnull Vector3d position, @Nonnull Vector3f rotation
   ) {
      if (projectileAssetName.isEmpty()) {
         throw new IllegalArgumentException("No projectile config typeName provided");
      } else {
         Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
         ProjectileComponent projectileComponent = new ProjectileComponent(projectileAssetName);
         holder.putComponent(getComponentType(), projectileComponent);
         holder.putComponent(DespawnComponent.getComponentType(), DespawnComponent.despawnInMilliseconds(time, 60000L));
         holder.putComponent(TransformComponent.getComponentType(), new TransformComponent(position.clone(), rotation));
         holder.ensureComponent(Velocity.getComponentType());
         holder.ensureComponent(UUIDComponent.getComponentType());
         return holder;
      }
   }

   public boolean initialize() {
      this.projectile = Projectile.getAssetMap().getAsset(this.projectileAssetName);
      if (this.projectile == null) {
         return false;
      } else {
         String appearance = this.projectile.getAppearance();
         if (appearance != null && !appearance.isEmpty()) {
            this.appearance = appearance;
         }

         return true;
      }
   }

   public void initializePhysics(@Nonnull BoundingBox boundingBox) {
      this.simplePhysicsProvider.setProvideCharacterCollisions(true);
      this.simplePhysicsProvider.initialize(this.projectile, boundingBox);
   }

   public void onProjectileBounce(@Nonnull Vector3d position, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      WorldParticle bounceParticles = this.projectile.getBounceParticles();
      if (bounceParticles != null) {
         SpatialResource<Ref<EntityStore>, EntityStore> playerSpatialResource = componentAccessor.getResource(EntityModule.get().getPlayerSpatialResourceType());
         ObjectList<Ref<EntityStore>> results = SpatialResource.getThreadLocalReferenceList();
         playerSpatialResource.getSpatialStructure().collect(position, 75.0, results);
         ParticleUtil.spawnParticleEffect(bounceParticles, position, results, componentAccessor);
      }

      SoundUtil.playSoundEvent3d(this.projectile.getBounceSoundEventIndex(), SoundCategory.SFX, position, componentAccessor);
   }

   private void onProjectileHitEvent(
      @Nonnull Ref<EntityStore> ref, @Nonnull Vector3d position, @Nonnull Ref<EntityStore> targetRef, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      WorldParticle hitParticles = this.projectile.getHitParticles();
      if (hitParticles != null) {
         SpatialResource<Ref<EntityStore>, EntityStore> playerSpatialResource = componentAccessor.getResource(EntityModule.get().getPlayerSpatialResourceType());
         ObjectList<Ref<EntityStore>> results = SpatialResource.getThreadLocalReferenceList();
         playerSpatialResource.getSpatialStructure().collect(position, 75.0, results);
         ParticleUtil.spawnParticleEffect(hitParticles, position, results, componentAccessor);
      }

      SoundUtil.playSoundEvent3d(this.projectile.getHitSoundEventIndex(), SoundCategory.SFX, position, componentAccessor);
      Entity targetEntity = EntityUtils.getEntity(targetRef, componentAccessor);
      if (targetEntity instanceof LivingEntity) {
         Ref<EntityStore> shooterRef = componentAccessor.getExternalData().getRefFromUUID(this.creatorUuid);
         DamageSystems.executeDamage(
            targetRef,
            componentAccessor,
            new Damage(
               new Damage.ProjectileSource(shooterRef != null ? shooterRef : ref, ref),
               DamageCause.PROJECTILE,
               this.projectile.getDamage() * this.brokenDamageModifier
            )
         );
         this.haveHit = true;
      }

      this.deadTimer = this.projectile.getDeadTime();
   }

   public boolean consumeDeadTimer(float dt) {
      if (this.deadTimer < 0.0) {
         return false;
      } else {
         this.deadTimer -= dt;
         return this.deadTimer <= 0.0;
      }
   }

   protected void bounceHandler(@Nonnull Vector3d position, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (this.lastBouncePosition == null) {
         this.lastBouncePosition = new Vector3d(position);
      } else {
         if (!(this.lastBouncePosition.distanceSquaredTo(position) >= 0.5)) {
            return;
         }

         this.lastBouncePosition.assign(position);
      }

      this.onProjectileBounce(position, componentAccessor);
   }

   protected void impactHandler(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Vector3d position,
      @Nullable Ref<EntityStore> targetRef,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (targetRef != null) {
         this.onProjectileHitEvent(ref, position, targetRef, componentAccessor);
      } else {
         this.onProjectileMissEvent(position, componentAccessor);
      }
   }

   private void onProjectileMissEvent(@Nonnull Vector3d position, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      WorldParticle missParticles = this.projectile.getMissParticles();
      if (missParticles != null) {
         SpatialResource<Ref<EntityStore>, EntityStore> playerSpatialResource = componentAccessor.getResource(EntityModule.get().getPlayerSpatialResourceType());
         ObjectList<Ref<EntityStore>> results = SpatialResource.getThreadLocalReferenceList();
         playerSpatialResource.getSpatialStructure().collect(position, 75.0, results);
         ParticleUtil.spawnParticleEffect(missParticles, position, results, componentAccessor);
      }

      SoundUtil.playSoundEvent3d(this.projectile.getMissSoundEventIndex(), SoundCategory.SFX, position, componentAccessor);
      this.deadTimer = this.projectile.getDeadTimeMiss();
   }

   public void onProjectileDeath(@Nonnull Ref<EntityStore> ref, @Nonnull Vector3d position, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
      EntityStore entityStore = commandBuffer.getExternalData();
      World world = entityStore.getWorld();
      ExplosionConfig explosionConfig = this.projectile.getExplosionConfig();
      if (explosionConfig != null) {
         Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
         Ref<EntityStore> creatorRef = entityStore.getRefFromUUID(this.creatorUuid);
         Damage.ProjectileSource damageSource = new Damage.ProjectileSource(creatorRef != null ? creatorRef : ref, ref);
         ExplosionUtils.performExplosion(damageSource, position, explosionConfig, ref, commandBuffer, chunkStore);
      }

      if (!this.haveHit || this.projectile.isDeathEffectsOnHit()) {
         WorldParticle deathParticles = this.projectile.getDeathParticles();
         if (deathParticles != null) {
            SpatialResource<Ref<EntityStore>, EntityStore> playerSpatialResource = commandBuffer.getResource(EntityModule.get().getPlayerSpatialResourceType());
            ObjectList<Ref<EntityStore>> results = SpatialResource.getThreadLocalReferenceList();
            playerSpatialResource.getSpatialStructure().collect(position, 75.0, results);
            ParticleUtil.spawnParticleEffect(deathParticles, position, results, commandBuffer);
         }

         SoundUtil.playSoundEvent3d(this.projectile.getDeathSoundEventIndex(), SoundCategory.SFX, position, commandBuffer);
      }
   }

   public void shoot(@Nonnull Holder<EntityStore> holder, @Nonnull UUID creatorUuid, double x, double y, double z, float yaw, float pitch) {
      this.creatorUuid = creatorUuid;
      this.simplePhysicsProvider.setCreatorId(creatorUuid);
      Vector3d direction = new Vector3d();
      computeStartOffset(
         this.projectile.isPitchAdjustShot(),
         this.projectile.getVerticalCenterShot(),
         this.projectile.getHorizontalCenterShot(),
         this.projectile.getDepthShot(),
         yaw,
         pitch,
         direction
      );
      x += direction.x;
      y += direction.y;
      z += direction.z;
      holder.ensureAndGetComponent(TransformComponent.getComponentType()).setPosition(new Vector3d(x, y, z));
      PhysicsMath.vectorFromAngles(yaw, pitch, direction);
      direction.setLength(this.projectile.getMuzzleVelocity());
      this.simplePhysicsProvider.setVelocity(direction);
   }

   public static void computeStartOffset(
      boolean pitchAdjust, double verticalCenterShot, double horizontalCenterShot, double depthShot, float yaw, float pitch, @Nonnull Vector3d offset
   ) {
      offset.assign(0.0, 0.0, 0.0);
      if (depthShot != 0.0) {
         PhysicsMath.vectorFromAngles(yaw, pitchAdjust ? pitch : 0.0F, offset);
         offset.setLength(depthShot);
      }

      offset.add(horizontalCenterShot * -PhysicsMath.headingZ(yaw), -verticalCenterShot, horizontalCenterShot * PhysicsMath.headingX(yaw));
   }

   public boolean isOnGround() {
      return this.simplePhysicsProvider.isOnGround();
   }

   @Nullable
   public Projectile getProjectile() {
      return this.projectile;
   }

   public String getAppearance() {
      return this.appearance;
   }

   public String getProjectileAssetName() {
      return this.projectileAssetName;
   }

   public SimplePhysicsProvider getSimplePhysicsProvider() {
      return this.simplePhysicsProvider;
   }

   public void applyBrokenPenalty(float penalty) {
      this.brokenDamageModifier = 1.0F - penalty;
   }

   public ProjectileComponent(@Nonnull ProjectileComponent other) {
      this.simplePhysicsProvider = other.simplePhysicsProvider;
      this.projectileAssetName = other.projectileAssetName;
      this.projectile = other.projectile;
      this.appearance = other.appearance;
      this.deadTimer = other.deadTimer;
      this.creatorUuid = other.creatorUuid;
      this.haveHit = other.haveHit;
      this.brokenDamageModifier = other.brokenDamageModifier;
      this.lastBouncePosition = other.lastBouncePosition;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      return new ProjectileComponent(this);
   }
}
