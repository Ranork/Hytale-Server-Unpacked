package com.hypixel.hytale.server.core.asset.type.projectile.config;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.asset.type.particle.config.WorldParticle;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.asset.type.soundevent.validator.SoundEventValidators;
import com.hypixel.hytale.server.core.entity.ExplosionConfig;
import com.hypixel.hytale.server.core.modules.physics.SimplePhysicsProvider;
import com.hypixel.hytale.server.core.modules.projectile.config.BallisticData;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Deprecated
public class Projectile implements JsonAssetWithMap<String, DefaultAssetMap<String, Projectile>>, BallisticData {
   public static final AssetBuilderCodec<String, Projectile> CODEC = AssetBuilderCodec.builder(
         Projectile.class,
         Projectile::new,
         Codec.STRING,
         (projectile, s) -> projectile.id = s,
         projectile -> projectile.id,
         (asset, data) -> asset.data = data,
         asset -> asset.data
      )
      .appendInherited(
         new KeyedCodec<>("Appearance", Codec.STRING),
         (projectile, s) -> projectile.appearance = s,
         projectile -> projectile.appearance,
         (projectile, parent) -> projectile.appearance = parent.appearance
      )
      .addValidator(ModelAsset.VALIDATOR_CACHE.getValidator())
      .add()
      .appendInherited(
         new KeyedCodec<>("Radius", Codec.DOUBLE),
         (projectile, s) -> projectile.radius = s,
         projectile -> projectile.radius,
         (projectile, parent) -> projectile.radius = parent.radius
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("Height", Codec.DOUBLE),
         (projectile, s) -> projectile.height = s,
         projectile -> projectile.height,
         (projectile, parent) -> projectile.height = parent.height
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("VerticalCenterShot", Codec.DOUBLE),
         (projectile, s) -> projectile.verticalCenterShot = s,
         projectile -> projectile.verticalCenterShot,
         (projectile, parent) -> projectile.verticalCenterShot = parent.verticalCenterShot
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("HorizontalCenterShot", Codec.DOUBLE),
         (projectile, s) -> projectile.horizontalCenterShot = s,
         projectile -> projectile.horizontalCenterShot,
         (projectile, parent) -> projectile.horizontalCenterShot = parent.horizontalCenterShot
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("DepthShot", Codec.DOUBLE),
         (projectile, s) -> projectile.depthShot = s,
         projectile -> projectile.depthShot,
         (projectile, parent) -> projectile.depthShot = parent.depthShot
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("PitchAdjustShot", Codec.BOOLEAN),
         (projectile, s) -> projectile.pitchAdjustShot = s,
         projectile -> projectile.pitchAdjustShot,
         (projectile, parent) -> projectile.pitchAdjustShot = parent.pitchAdjustShot
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("MuzzleVelocity", Codec.DOUBLE),
         (projectile, s) -> projectile.muzzleVelocity = s,
         projectile -> projectile.muzzleVelocity,
         (projectile, parent) -> projectile.muzzleVelocity = parent.muzzleVelocity
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("TerminalVelocity", Codec.DOUBLE),
         (projectile, s) -> projectile.terminalVelocity = s,
         projectile -> projectile.terminalVelocity,
         (projectile, parent) -> projectile.terminalVelocity = parent.terminalVelocity
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("Gravity", Codec.DOUBLE),
         (projectile, s) -> projectile.gravity = s,
         projectile -> projectile.gravity,
         (projectile, parent) -> projectile.gravity = parent.gravity
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("Bounciness", Codec.DOUBLE),
         (projectile, s) -> projectile.bounciness = s,
         projectile -> projectile.bounciness,
         (projectile, parent) -> projectile.bounciness = parent.bounciness
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("ImpactSlowdown", Codec.DOUBLE),
         (projectile, s) -> projectile.impactSlowdown = s,
         projectile -> projectile.impactSlowdown,
         (projectile, parent) -> projectile.impactSlowdown = parent.impactSlowdown
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("SticksVertically", Codec.BOOLEAN),
         (projectile, s) -> projectile.sticksVertically = s,
         projectile -> projectile.sticksVertically,
         (projectile, parent) -> projectile.sticksVertically = parent.sticksVertically
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("ComputeYaw", Codec.BOOLEAN),
         (projectile, s) -> projectile.computeYaw = s,
         projectile -> projectile.computeYaw,
         (projectile, parent) -> projectile.computeYaw = parent.computeYaw
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("ComputePitch", Codec.BOOLEAN),
         (projectile, s) -> projectile.computePitch = s,
         projectile -> projectile.computePitch,
         (projectile, parent) -> projectile.computePitch = parent.computePitch
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("ComputeRoll", Codec.BOOLEAN),
         (projectile, s) -> projectile.computeRoll = s,
         projectile -> projectile.computeRoll,
         (projectile, parent) -> projectile.computeRoll = parent.computeRoll
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("TimeToLive", Codec.DOUBLE),
         (projectile, s) -> projectile.timeToLive = s,
         projectile -> projectile.timeToLive,
         (projectile, parent) -> projectile.timeToLive = parent.timeToLive
      )
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("BounceSoundEventId", Codec.STRING),
         (projectile, s) -> projectile.bounceSoundEventId = s,
         projectile -> projectile.bounceSoundEventId,
         (projectile, parent) -> projectile.bounceSoundEventId = parent.bounceSoundEventId
      )
      .addValidator(SoundEvent.VALIDATOR_CACHE.getValidator())
      .addValidator(SoundEventValidators.MONO)
      .add()
      .appendInherited(
         new KeyedCodec<>("BounceParticles", WorldParticle.CODEC),
         (projectile, s) -> projectile.bounceParticles = s,
         projectile -> projectile.bounceParticles,
         (projectile, parent) -> projectile.bounceParticles = parent.bounceParticles
      )
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("HitSoundEventId", Codec.STRING),
         (projectile, s) -> projectile.hitSoundEventId = s,
         projectile -> projectile.hitSoundEventId,
         (projectile, parent) -> projectile.hitSoundEventId = parent.hitSoundEventId
      )
      .addValidator(SoundEvent.VALIDATOR_CACHE.getValidator())
      .addValidator(SoundEventValidators.MONO)
      .add()
      .appendInherited(
         new KeyedCodec<>("HitParticles", WorldParticle.CODEC),
         (projectile, s) -> projectile.hitParticles = s,
         projectile -> projectile.hitParticles,
         (projectile, parent) -> projectile.hitParticles = parent.hitParticles
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("Damage", Codec.INTEGER),
         (projectile, s) -> projectile.damage = s,
         projectile -> projectile.damage,
         (projectile, parent) -> projectile.damage = parent.damage
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("DeadTime", Codec.DOUBLE),
         (projectile, s) -> projectile.deadTime = s,
         projectile -> projectile.deadTime,
         (projectile, parent) -> projectile.deadTime = parent.deadTime
      )
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("MissSoundEventId", Codec.STRING),
         (projectile, s) -> projectile.missSoundEventId = s,
         projectile -> projectile.missSoundEventId,
         (projectile, parent) -> projectile.missSoundEventId = parent.missSoundEventId
      )
      .addValidator(SoundEvent.VALIDATOR_CACHE.getValidator())
      .addValidator(SoundEventValidators.MONO)
      .add()
      .appendInherited(
         new KeyedCodec<>("MissParticles", WorldParticle.CODEC),
         (projectile, s) -> projectile.missParticles = s,
         projectile -> projectile.missParticles,
         (projectile, parent) -> projectile.missParticles = parent.missParticles
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("DeadTimeMiss", Codec.DOUBLE),
         (projectile, s) -> projectile.deadTimeMiss = s,
         projectile -> projectile.deadTimeMiss,
         (projectile, parent) -> projectile.deadTimeMiss = parent.deadTimeMiss
      )
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("DeathSoundEventId", Codec.STRING),
         (projectile, s) -> projectile.deathSoundEventId = s,
         projectile -> projectile.deathSoundEventId,
         (projectile, parent) -> projectile.deathSoundEventId = parent.deathSoundEventId
      )
      .addValidator(SoundEvent.VALIDATOR_CACHE.getValidator())
      .addValidator(SoundEventValidators.MONO)
      .add()
      .appendInherited(
         new KeyedCodec<>("DeathParticles", WorldParticle.CODEC),
         (projectile, s) -> projectile.deathParticles = s,
         projectile -> projectile.deathParticles,
         (projectile, parent) -> projectile.deathParticles = parent.deathParticles
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("DeathEffectsOnHit", Codec.BOOLEAN),
         (projectile, b) -> projectile.deathEffectsOnHit = b,
         projectile -> projectile.deathEffectsOnHit,
         (projectile, parent) -> projectile.deathEffectsOnHit = parent.deathEffectsOnHit
      )
      .add()
      .<ExplosionConfig>appendInherited(
         new KeyedCodec<>("ExplosionConfig", ExplosionConfig.CODEC),
         (projectile, s) -> projectile.explosionConfig = s,
         projectile -> projectile.explosionConfig,
         (projectile, parent) -> projectile.explosionConfig = parent.explosionConfig
      )
      .documentation("The explosion config associated with this projectile")
      .add()
      .appendInherited(
         new KeyedCodec<>("Density", Codec.DOUBLE),
         (projectile, s) -> projectile.density = s,
         projectile -> projectile.density,
         (projectile, parent) -> projectile.density = parent.density
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("WaterTerminalVelocityMultiplier", Codec.DOUBLE),
         (projectile, s) -> projectile.waterTerminalVelocityMultiplier = s,
         projectile -> projectile.waterTerminalVelocityMultiplier,
         (projectile, parent) -> projectile.waterTerminalVelocityMultiplier = parent.waterTerminalVelocityMultiplier
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("WaterHitImpulseLoss", Codec.DOUBLE),
         (projectile, s) -> projectile.waterHitImpulseLoss = s,
         projectile -> projectile.waterHitImpulseLoss,
         (projectile, parent) -> projectile.waterHitImpulseLoss = parent.waterHitImpulseLoss
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("DampingRotation", Codec.DOUBLE),
         (projectile, s) -> projectile.dampingRotation = s,
         projectile -> projectile.dampingRotation,
         (projectile, parent) -> projectile.dampingRotation = parent.dampingRotation
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("RotationSpeedVelocityRatio", Codec.DOUBLE),
         (projectile, s) -> projectile.rotationSpeedVelocityRatio = s,
         projectile -> projectile.rotationSpeedVelocityRatio,
         (projectile, parent) -> projectile.rotationSpeedVelocityRatio = parent.rotationSpeedVelocityRatio
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("SwimmingDampingFactor", Codec.DOUBLE),
         (projectile, s) -> projectile.swimmingDampingFactor = s,
         projectile -> projectile.swimmingDampingFactor,
         (projectile, parent) -> projectile.swimmingDampingFactor = parent.swimmingDampingFactor
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("RotationMode", new EnumCodec<>(SimplePhysicsProvider.ROTATION_MODE.class)),
         (projectile, type) -> projectile.rotationMode = type,
         projectile -> projectile.rotationMode,
         (projectile, parent) -> projectile.rotationMode = parent.rotationMode
      )
      .add()
      .afterDecode(Projectile::processConfig)
      .build();
   private static AssetStore<String, Projectile, DefaultAssetMap<String, Projectile>> ASSET_STORE;
   public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(Projectile::getAssetStore));
   protected AssetExtraInfo.Data data;
   protected String id;
   protected String appearance;
   protected double radius;
   protected double height;
   protected double verticalCenterShot;
   protected double horizontalCenterShot;
   protected double depthShot;
   protected boolean pitchAdjustShot;
   protected double muzzleVelocity;
   protected double terminalVelocity;
   protected double gravity;
   protected double bounciness;
   protected double impactSlowdown;
   protected boolean sticksVertically;
   protected boolean computeYaw = true;
   protected boolean computePitch = true;
   protected boolean computeRoll = true;
   protected SimplePhysicsProvider.ROTATION_MODE rotationMode = SimplePhysicsProvider.ROTATION_MODE.Velocity;
   protected double timeToLive;
   protected String bounceSoundEventId;
   protected transient int bounceSoundEventIndex;
   @Nullable
   protected WorldParticle bounceParticles;
   protected String hitSoundEventId;
   protected transient int hitSoundEventIndex;
   @Nullable
   protected WorldParticle hitParticles;
   protected int damage;
   protected double deadTime;
   protected String missSoundEventId;
   protected transient int missSoundEventIndex;
   protected WorldParticle missParticles;
   protected double deadTimeMiss = 10.0;
   protected String deathSoundEventId;
   protected transient int deathSoundEventIndex;
   @Nullable
   protected WorldParticle deathParticles;
   protected boolean deathEffectsOnHit;
   @Nullable
   protected ExplosionConfig explosionConfig;
   double density = 2000.0;
   double waterTerminalVelocityMultiplier = 1.0;
   double waterHitImpulseLoss = 0.0;
   double dampingRotation = 0.0;
   double rotationSpeedVelocityRatio = 2.0;
   double swimmingDampingFactor = 1.0;

   public static AssetStore<String, Projectile, DefaultAssetMap<String, Projectile>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(Projectile.class);
      }

      return ASSET_STORE;
   }

   public static DefaultAssetMap<String, Projectile> getAssetMap() {
      return (DefaultAssetMap<String, Projectile>)getAssetStore().getAssetMap();
   }

   protected Projectile() {
   }

   public String getId() {
      return this.id;
   }

   public String getAppearance() {
      return this.appearance;
   }

   public double getRadius() {
      return this.radius;
   }

   public double getHeight() {
      return this.height;
   }

   @Override
   public double getVerticalCenterShot() {
      return this.verticalCenterShot;
   }

   @Override
   public double getHorizontalCenterShot() {
      return this.horizontalCenterShot;
   }

   @Override
   public double getDepthShot() {
      return this.depthShot;
   }

   @Override
   public boolean isPitchAdjustShot() {
      return this.pitchAdjustShot;
   }

   public boolean isSticksVertically() {
      return this.sticksVertically;
   }

   @Override
   public double getMuzzleVelocity() {
      return this.muzzleVelocity;
   }

   public double getTerminalVelocity() {
      return this.terminalVelocity;
   }

   @Override
   public double getGravity() {
      return this.gravity;
   }

   public double getBounciness() {
      return this.bounciness;
   }

   public double getImpactSlowdown() {
      return this.impactSlowdown;
   }

   public double getTimeToLive() {
      return this.timeToLive;
   }

   public int getDamage() {
      return this.damage;
   }

   public double getDeadTime() {
      return this.deadTime;
   }

   public double getDeadTimeMiss() {
      return this.deadTimeMiss;
   }

   public String getBounceSoundEventId() {
      return this.bounceSoundEventId;
   }

   public int getBounceSoundEventIndex() {
      return this.bounceSoundEventIndex;
   }

   public String getHitSoundEventId() {
      return this.hitSoundEventId;
   }

   public int getHitSoundEventIndex() {
      return this.hitSoundEventIndex;
   }

   public String getMissSoundEventId() {
      return this.missSoundEventId;
   }

   public int getMissSoundEventIndex() {
      return this.missSoundEventIndex;
   }

   public String getDeathSoundEventId() {
      return this.deathSoundEventId;
   }

   public int getDeathSoundEventIndex() {
      return this.deathSoundEventIndex;
   }

   @Nullable
   public WorldParticle getBounceParticles() {
      return this.bounceParticles;
   }

   @Nullable
   public WorldParticle getMissParticles() {
      return this.missParticles;
   }

   @Nullable
   public WorldParticle getDeathParticles() {
      return this.deathParticles;
   }

   @Nullable
   public WorldParticle getHitParticles() {
      return this.hitParticles;
   }

   public boolean isDeathEffectsOnHit() {
      return this.deathEffectsOnHit;
   }

   public boolean isComputeYaw() {
      return this.computeYaw;
   }

   public boolean isComputePitch() {
      return this.computePitch;
   }

   public boolean isComputeRoll() {
      return this.computeRoll;
   }

   public SimplePhysicsProvider.ROTATION_MODE getRotationMode() {
      return this.rotationMode;
   }

   public double getDensity() {
      return this.density;
   }

   public double getWaterTerminalVelocityMultiplier() {
      return this.waterTerminalVelocityMultiplier;
   }

   public double getWaterHitImpulseLoss() {
      return this.waterHitImpulseLoss;
   }

   public double getDampingRotation() {
      return this.dampingRotation;
   }

   public double getRotationSpeedVelocityRatio() {
      return this.rotationSpeedVelocityRatio;
   }

   public double getSwimmingDampingFactor() {
      return this.swimmingDampingFactor;
   }

   protected void processConfig() {
      if (this.bounceSoundEventId != null) {
         this.bounceSoundEventIndex = SoundEvent.getAssetMap().getIndex(this.bounceSoundEventId);
      }

      if (this.hitSoundEventId != null) {
         this.hitSoundEventIndex = SoundEvent.getAssetMap().getIndex(this.hitSoundEventId);
      }

      if (this.missSoundEventId != null) {
         this.missSoundEventIndex = SoundEvent.getAssetMap().getIndex(this.missSoundEventId);
      }

      if (this.deathSoundEventId != null) {
         this.deathSoundEventIndex = SoundEvent.getAssetMap().getIndex(this.deathSoundEventId);
      }
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Projectile that = (Projectile)o;
         if (Double.compare(that.radius, this.radius) != 0) {
            return false;
         } else if (Double.compare(that.height, this.height) != 0) {
            return false;
         } else if (Double.compare(that.verticalCenterShot, this.verticalCenterShot) != 0) {
            return false;
         } else if (Double.compare(that.horizontalCenterShot, this.horizontalCenterShot) != 0) {
            return false;
         } else if (Double.compare(that.depthShot, this.depthShot) != 0) {
            return false;
         } else if (this.pitchAdjustShot != that.pitchAdjustShot) {
            return false;
         } else if (Double.compare(that.muzzleVelocity, this.muzzleVelocity) != 0) {
            return false;
         } else if (Double.compare(that.terminalVelocity, this.terminalVelocity) != 0) {
            return false;
         } else if (Double.compare(that.gravity, this.gravity) != 0) {
            return false;
         } else if (Double.compare(that.bounciness, this.bounciness) != 0) {
            return false;
         } else if (Double.compare(that.impactSlowdown, this.impactSlowdown) != 0) {
            return false;
         } else if (this.sticksVertically != that.sticksVertically) {
            return false;
         } else if (this.computeYaw != that.computeYaw) {
            return false;
         } else if (this.computePitch != that.computePitch) {
            return false;
         } else if (this.computeRoll != that.computeRoll) {
            return false;
         } else if (Double.compare(that.timeToLive, this.timeToLive) != 0) {
            return false;
         } else if (this.bounceSoundEventIndex != that.bounceSoundEventIndex) {
            return false;
         } else if (this.hitSoundEventIndex != that.hitSoundEventIndex) {
            return false;
         } else if (this.damage != that.damage) {
            return false;
         } else if (Double.compare(that.deadTime, this.deadTime) != 0) {
            return false;
         } else if (this.missSoundEventIndex != that.missSoundEventIndex) {
            return false;
         } else if (Double.compare(that.deadTimeMiss, this.deadTimeMiss) != 0) {
            return false;
         } else if (this.deathSoundEventIndex != that.deathSoundEventIndex) {
            return false;
         } else if (this.deathEffectsOnHit != that.deathEffectsOnHit) {
            return false;
         } else if (this.explosionConfig != that.explosionConfig) {
            return false;
         } else if (Double.compare(that.density, this.density) != 0) {
            return false;
         } else if (Double.compare(that.waterTerminalVelocityMultiplier, this.waterTerminalVelocityMultiplier) != 0) {
            return false;
         } else if (Double.compare(that.waterHitImpulseLoss, this.waterHitImpulseLoss) != 0) {
            return false;
         } else if (Double.compare(that.dampingRotation, this.dampingRotation) != 0) {
            return false;
         } else if (Double.compare(that.rotationSpeedVelocityRatio, this.rotationSpeedVelocityRatio) != 0) {
            return false;
         } else if (Double.compare(that.swimmingDampingFactor, this.swimmingDampingFactor) != 0) {
            return false;
         } else if (!this.id.equals(that.id)) {
            return false;
         } else if (this.appearance != null ? this.appearance.equals(that.appearance) : that.appearance == null) {
            if (this.rotationMode != that.rotationMode) {
               return false;
            } else if (this.bounceSoundEventId != null ? this.bounceSoundEventId.equals(that.bounceSoundEventId) : that.bounceSoundEventId == null) {
               if (this.bounceParticles != null ? this.bounceParticles.equals(that.bounceParticles) : that.bounceParticles == null) {
                  if (this.hitSoundEventId != null ? this.hitSoundEventId.equals(that.hitSoundEventId) : that.hitSoundEventId == null) {
                     if (this.hitParticles != null ? this.hitParticles.equals(that.hitParticles) : that.hitParticles == null) {
                        if (this.missSoundEventId != null ? this.missSoundEventId.equals(that.missSoundEventId) : that.missSoundEventId == null) {
                           if (this.missParticles != null ? this.missParticles.equals(that.missParticles) : that.missParticles == null) {
                              if (this.deathSoundEventId != null ? this.deathSoundEventId.equals(that.deathSoundEventId) : that.deathSoundEventId == null) {
                                 return this.deathParticles != null ? !this.deathParticles.equals(that.deathParticles) : that.deathParticles != null;
                              } else {
                                 return false;
                              }
                           } else {
                              return false;
                           }
                        } else {
                           return false;
                        }
                     } else {
                        return false;
                     }
                  } else {
                     return false;
                  }
               } else {
                  return false;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = this.id.hashCode();
      result = 31 * result + (this.appearance != null ? this.appearance.hashCode() : 0);
      long temp = Double.doubleToLongBits(this.radius);
      result = 31 * result + (int)(temp ^ temp >>> 32);
      temp = Double.doubleToLongBits(this.height);
      result = 31 * result + (int)(temp ^ temp >>> 32);
      temp = Double.doubleToLongBits(this.verticalCenterShot);
      result = 31 * result + (int)(temp ^ temp >>> 32);
      temp = Double.doubleToLongBits(this.horizontalCenterShot);
      result = 31 * result + (int)(temp ^ temp >>> 32);
      temp = Double.doubleToLongBits(this.depthShot);
      result = 31 * result + (int)(temp ^ temp >>> 32);
      result = 31 * result + (this.pitchAdjustShot ? 1 : 0);
      temp = Double.doubleToLongBits(this.muzzleVelocity);
      result = 31 * result + (int)(temp ^ temp >>> 32);
      temp = Double.doubleToLongBits(this.terminalVelocity);
      result = 31 * result + (int)(temp ^ temp >>> 32);
      temp = Double.doubleToLongBits(this.gravity);
      result = 31 * result + (int)(temp ^ temp >>> 32);
      temp = Double.doubleToLongBits(this.bounciness);
      result = 31 * result + (int)(temp ^ temp >>> 32);
      temp = Double.doubleToLongBits(this.impactSlowdown);
      result = 31 * result + (int)(temp ^ temp >>> 32);
      result = 31 * result + (this.sticksVertically ? 1 : 0);
      result = 31 * result + (this.computeYaw ? 1 : 0);
      result = 31 * result + (this.computePitch ? 1 : 0);
      result = 31 * result + (this.computeRoll ? 1 : 0);
      result = 31 * result + (this.rotationMode != null ? this.rotationMode.hashCode() : 0);
      temp = Double.doubleToLongBits(this.timeToLive);
      result = 31 * result + (int)(temp ^ temp >>> 32);
      result = 31 * result + (this.bounceSoundEventId != null ? this.bounceSoundEventId.hashCode() : 0);
      result = 31 * result + this.bounceSoundEventIndex;
      result = 31 * result + (this.bounceParticles != null ? this.bounceParticles.hashCode() : 0);
      result = 31 * result + (this.hitSoundEventId != null ? this.hitSoundEventId.hashCode() : 0);
      result = 31 * result + this.hitSoundEventIndex;
      result = 31 * result + (this.hitParticles != null ? this.hitParticles.hashCode() : 0);
      result = 31 * result + this.damage;
      temp = Double.doubleToLongBits(this.deadTime);
      result = 31 * result + (int)(temp ^ temp >>> 32);
      result = 31 * result + (this.missSoundEventId != null ? this.missSoundEventId.hashCode() : 0);
      result = 31 * result + this.missSoundEventIndex;
      result = 31 * result + (this.missParticles != null ? this.missParticles.hashCode() : 0);
      temp = Double.doubleToLongBits(this.deadTimeMiss);
      result = 31 * result + (int)(temp ^ temp >>> 32);
      result = 31 * result + (this.deathSoundEventId != null ? this.deathSoundEventId.hashCode() : 0);
      result = 31 * result + this.deathSoundEventIndex;
      result = 31 * result + (this.deathParticles != null ? this.deathParticles.hashCode() : 0);
      result = 31 * result + (this.deathEffectsOnHit ? 1 : 0);
      result = 31 * result + (int)(temp ^ temp >>> 32);
      temp = Double.doubleToLongBits(this.density);
      result = 31 * result + (int)(temp ^ temp >>> 32);
      temp = Double.doubleToLongBits(this.waterTerminalVelocityMultiplier);
      result = 31 * result + (int)(temp ^ temp >>> 32);
      temp = Double.doubleToLongBits(this.waterHitImpulseLoss);
      result = 31 * result + (int)(temp ^ temp >>> 32);
      temp = Double.doubleToLongBits(this.dampingRotation);
      result = 31 * result + (int)(temp ^ temp >>> 32);
      temp = Double.doubleToLongBits(this.rotationSpeedVelocityRatio);
      result = 31 * result + (int)(temp ^ temp >>> 32);
      temp = Double.doubleToLongBits(this.swimmingDampingFactor);
      return 31 * result + (int)(temp ^ temp >>> 32);
   }

   @Nonnull
   @Override
   public String toString() {
      return "Projectile{id='"
         + this.id
         + "', appearance='"
         + this.appearance
         + "', radius="
         + this.radius
         + ", height="
         + this.height
         + ", verticalCenterShot="
         + this.verticalCenterShot
         + ", horizontalCenterShot="
         + this.horizontalCenterShot
         + ", depthShot="
         + this.depthShot
         + ", pitchAdjustShot="
         + this.pitchAdjustShot
         + ", muzzleVelocity="
         + this.muzzleVelocity
         + ", terminalVelocity="
         + this.terminalVelocity
         + ", gravity="
         + this.gravity
         + ", bounciness="
         + this.bounciness
         + ", impactSlowdown="
         + this.impactSlowdown
         + ", sticksVertically="
         + this.sticksVertically
         + ", computeYaw="
         + this.computeYaw
         + ", computePitch="
         + this.computePitch
         + ", computeRoll="
         + this.computeRoll
         + ", rotationMode="
         + this.rotationMode
         + ", timeToLive="
         + this.timeToLive
         + ", bounceSoundEventId='"
         + this.bounceSoundEventId
         + "', bounceParticles='"
         + this.bounceParticles
         + "', hitSoundEventId='"
         + this.hitSoundEventId
         + "', hitParticles='"
         + this.hitParticles
         + "', damage="
         + this.damage
         + ", deadTime="
         + this.deadTime
         + ", missSoundEventId='"
         + this.missSoundEventId
         + "', missParticles='"
         + this.missParticles
         + "', deadTimeMiss="
         + this.deadTimeMiss
         + ", deathSoundEventId='"
         + this.deathSoundEventId
         + "', deathParticles='"
         + this.deathParticles
         + "', deathEffectsOnHit="
         + this.deathEffectsOnHit
         + ", density="
         + this.density
         + ", waterTerminalVelocityMultiplier="
         + this.waterTerminalVelocityMultiplier
         + ", waterHitImpulseLoss="
         + this.waterHitImpulseLoss
         + ", dampingRotation="
         + this.dampingRotation
         + ", rotationSpeedVelocityRatio="
         + this.rotationSpeedVelocityRatio
         + ", swimmingDampingFactor="
         + this.swimmingDampingFactor
         + "}";
   }

   @Nullable
   public ExplosionConfig getExplosionConfig() {
      return this.explosionConfig;
   }
}
