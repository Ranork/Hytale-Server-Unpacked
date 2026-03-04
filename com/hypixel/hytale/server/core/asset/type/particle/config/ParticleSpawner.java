package com.hypixel.hytale.server.core.asset.type.particle.config;

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
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIDefaultCollapsedState;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditorSectionStart;
import com.hypixel.hytale.codec.schema.metadata.ui.UITypeIcon;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.protocol.EmitShape;
import com.hypixel.hytale.protocol.FXRenderMode;
import com.hypixel.hytale.protocol.InitialVelocity;
import com.hypixel.hytale.protocol.IntersectionHighlight;
import com.hypixel.hytale.protocol.ParticleRotationInfluence;
import com.hypixel.hytale.protocol.Range;
import com.hypixel.hytale.protocol.RangeVector3f;
import com.hypixel.hytale.protocol.Rangef;
import com.hypixel.hytale.protocol.UVMotion;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import java.lang.ref.SoftReference;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class ParticleSpawner
   implements JsonAssetWithMap<String, DefaultAssetMap<String, ParticleSpawner>>,
   NetworkSerializable<com.hypixel.hytale.protocol.ParticleSpawner> {
   public static final String PARTICLE_PATH = "Particles/";
   public static final String PARTICLE_EXTENSION = ".particle";
   public static final AssetBuilderCodec<String, ParticleSpawner> CODEC = AssetBuilderCodec.builder(
         ParticleSpawner.class,
         ParticleSpawner::new,
         Codec.STRING,
         (particleSpawner, s) -> particleSpawner.id = s,
         particleSpawner -> particleSpawner.id,
         (asset, data) -> asset.data = data,
         asset -> asset.data
      )
      .metadata(new UITypeIcon("ParticleSpawner.png"))
      .<EmitShape>appendInherited(
         new KeyedCodec<>("Shape", new EnumCodec<>(EmitShape.class)),
         (particleSpawner, s) -> particleSpawner.shape = s,
         particleSpawner -> particleSpawner.shape,
         (particleSpawner, parent) -> particleSpawner.shape = parent.shape
      )
      .addValidator(Validators.nonNull())
      .add()
      .appendInherited(
         new KeyedCodec<>("EmitOffset", ProtocolCodecs.RANGE_VECTOR3F),
         (particleSpawner, s) -> particleSpawner.emitOffset = s,
         particleSpawner -> particleSpawner.emitOffset,
         (particleSpawner, parent) -> particleSpawner.emitOffset = parent.emitOffset
      )
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("UseEmitDirection", Codec.BOOLEAN),
         (particleSpawner, b) -> particleSpawner.useEmitDirection = b,
         particleSpawner -> particleSpawner.useEmitDirection,
         (particleSpawner, parent) -> particleSpawner.useEmitDirection = parent.useEmitDirection
      )
      .documentation("Use spawn position to determine direction. Overrides pitch/yaw in InitialVelocity.")
      .add()
      .appendInherited(
         new KeyedCodec<>("TotalParticles", ProtocolCodecs.RANGE),
         (particleSpawner, s) -> particleSpawner.totalParticles = s,
         particleSpawner -> particleSpawner.totalParticles,
         (particleSpawner, parent) -> particleSpawner.totalParticles = parent.totalParticles
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("LifeSpan", Codec.FLOAT),
         (particleSpawner, f) -> particleSpawner.lifeSpan = f,
         particleSpawner -> particleSpawner.lifeSpan,
         (particleSpawner, parent) -> particleSpawner.lifeSpan = parent.lifeSpan
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("MaxConcurrentParticles", Codec.INTEGER),
         (particleSpawner, s) -> particleSpawner.maxConcurrentParticles = s,
         particleSpawner -> particleSpawner.maxConcurrentParticles,
         (particleSpawner, parent) -> particleSpawner.maxConcurrentParticles = parent.maxConcurrentParticles
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("ParticleLifeSpan", ProtocolCodecs.RANGEF),
         (particleSpawner, s) -> particleSpawner.particleLifeSpan = s,
         particleSpawner -> particleSpawner.particleLifeSpan,
         (particleSpawner, parent) -> particleSpawner.particleLifeSpan = parent.particleLifeSpan
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("SpawnRate", ProtocolCodecs.RANGEF),
         (particleSpawner, s) -> particleSpawner.spawnRate = s,
         particleSpawner -> particleSpawner.spawnRate,
         (particleSpawner, parent) -> particleSpawner.spawnRate = parent.spawnRate
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("SpawnBurst", Codec.BOOLEAN),
         (particleSpawner, b) -> particleSpawner.spawnBurst = b,
         particleSpawner -> particleSpawner.spawnBurst,
         (particleSpawner, parent) -> particleSpawner.spawnRate = parent.spawnRate
      )
      .add()
      .append(
         new KeyedCodec<>("WaveDelay", ProtocolCodecs.RANGEF),
         (particleSpawner, b) -> particleSpawner.waveDelay = b,
         particleSpawner -> particleSpawner.waveDelay
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("InitialVelocity", ProtocolCodecs.INITIAL_VELOCITY),
         (particleSpawner, s) -> particleSpawner.initialVelocity = s,
         particleSpawner -> particleSpawner.initialVelocity,
         (particleSpawner, parent) -> particleSpawner.initialVelocity = parent.initialVelocity
      )
      .add()
      .<ParticleRotationInfluence>appendInherited(
         new KeyedCodec<>("ParticleRotationInfluence", new EnumCodec<>(ParticleRotationInfluence.class)),
         (particleSpawner, s) -> particleSpawner.particleRotationInfluence = s,
         particleSpawner -> particleSpawner.particleRotationInfluence,
         (particleSpawner, parent) -> particleSpawner.particleRotationInfluence = parent.particleRotationInfluence
      )
      .addValidator(Validators.nonNull())
      .metadata(new UIEditorSectionStart("Motion"))
      .add()
      .appendInherited(
         new KeyedCodec<>("ParticleRotateWithSpawner", Codec.BOOLEAN),
         (particleSpawner, s) -> particleSpawner.particleRotateWithSpawner = s,
         particleSpawner -> particleSpawner.particleRotateWithSpawner,
         (particleSpawner, parent) -> particleSpawner.particleRotateWithSpawner = parent.particleRotateWithSpawner
      )
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("TrailSpawnerPositionMultiplier", Codec.FLOAT),
         (particleSpawner, f) -> particleSpawner.trailSpawnerPositionMultiplier = f,
         particleSpawner -> particleSpawner.trailSpawnerPositionMultiplier,
         (particleSpawner, parent) -> particleSpawner.trailSpawnerPositionMultiplier = parent.trailSpawnerPositionMultiplier
      )
      .addValidator(Validators.range(0.0F, 1.0F))
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("TrailSpawnerRotationMultiplier", Codec.FLOAT),
         (particleSpawner, f) -> particleSpawner.trailSpawnerRotationMultiplier = f,
         particleSpawner -> particleSpawner.trailSpawnerRotationMultiplier,
         (particleSpawner, parent) -> particleSpawner.trailSpawnerRotationMultiplier = parent.trailSpawnerRotationMultiplier
      )
      .addValidator(Validators.range(0.0F, 1.0F))
      .add()
      .append(
         new KeyedCodec<>("VelocityStretchMultiplier", Codec.FLOAT),
         (particleSpawner, f) -> particleSpawner.velocityStretchMultiplier = f,
         particleSpawner -> particleSpawner.velocityStretchMultiplier
      )
      .add()
      .<ParticleAttractor[]>appendInherited(
         new KeyedCodec<>("Attractors", new ArrayCodec<>(ParticleAttractor.CODEC, ParticleAttractor[]::new)),
         (particleSpawner, o) -> particleSpawner.attractors = o,
         particleSpawner -> particleSpawner.attractors,
         (particleSpawner, parent) -> particleSpawner.attractors = parent.attractors
      )
      .metadata(new UIEditorSectionStart("Attractors"))
      .metadata(UIDefaultCollapsedState.UNCOLLAPSED)
      .add()
      .<FXRenderMode>appendInherited(
         new KeyedCodec<>("RenderMode", new EnumCodec<>(FXRenderMode.class)),
         (particleSpawner, s) -> particleSpawner.renderMode = s,
         particleSpawner -> particleSpawner.renderMode,
         (particleSpawner, parent) -> particleSpawner.renderMode = parent.renderMode
      )
      .addValidator(Validators.nonNull())
      .metadata(new UIEditorSectionStart("Material"))
      .add()
      .appendInherited(
         new KeyedCodec<>("LightInfluence", Codec.FLOAT),
         (particleSpawner, f) -> particleSpawner.lightInfluence = f,
         particleSpawner -> particleSpawner.lightInfluence,
         (particleSpawner, parent) -> particleSpawner.lightInfluence = parent.lightInfluence
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("IntersectionHighlight", ProtocolCodecs.INTERSECTION_HIGHLIGHT),
         (particleSpawner, s) -> particleSpawner.intersectionHighlight = s,
         particleSpawner -> particleSpawner.intersectionHighlight,
         (particleSpawner, parent) -> particleSpawner.intersectionHighlight = parent.intersectionHighlight
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("LinearFiltering", Codec.BOOLEAN),
         (particleSpawner, s) -> particleSpawner.linearFiltering = s,
         particleSpawner -> particleSpawner.linearFiltering,
         (particleSpawner, parent) -> particleSpawner.linearFiltering = parent.linearFiltering
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("UVMotion", ProtocolCodecs.UV_MOTION),
         (particleSpawner, s) -> particleSpawner.uvMotion = s,
         particleSpawner -> particleSpawner.uvMotion,
         (particleSpawner, parent) -> particleSpawner.uvMotion = parent.uvMotion
      )
      .add()
      .<Float>append(
         new KeyedCodec<>("CameraOffset", Codec.FLOAT),
         (particleSpawner, f) -> particleSpawner.cameraOffset = f,
         particleSpawner -> particleSpawner.cameraOffset
      )
      .addValidator(Validators.range(-10.0F, 10.0F))
      .add()
      .<ParticleCollision>appendInherited(
         new KeyedCodec<>("ParticleCollision", ParticleCollision.CODEC),
         (particleSpawner, s) -> particleSpawner.particleCollision = s,
         particleSpawner -> particleSpawner.particleCollision,
         (particleSpawner, parent) -> particleSpawner.particleCollision = parent.particleCollision
      )
      .metadata(new UIEditorSectionStart("Collision"))
      .metadata(UIDefaultCollapsedState.UNCOLLAPSED)
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("IsLowRes", Codec.BOOLEAN),
         (particleSpawner, s) -> particleSpawner.isLowRes = s,
         particleSpawner -> particleSpawner.isLowRes,
         (particleSpawner, parent) -> particleSpawner.isLowRes = parent.isLowRes
      )
      .metadata(new UIEditorSectionStart("Optimization"))
      .add()
      .<Particle>appendInherited(
         new KeyedCodec<>("Particle", Particle.CODEC),
         (particleSpawner, o) -> particleSpawner.particle = o,
         particleSpawner -> particleSpawner.particle,
         (particleSpawner, parent) -> particleSpawner.particle = parent.particle
      )
      .addValidator(Validators.nonNull())
      .metadata(new UIEditorSectionStart("Particle"))
      .metadata(UIDefaultCollapsedState.UNCOLLAPSED)
      .add()
      .build();
   public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(ParticleSpawner::getAssetStore));
   private static AssetStore<String, ParticleSpawner, DefaultAssetMap<String, ParticleSpawner>> ASSET_STORE;
   protected AssetExtraInfo.Data data;
   protected String id;
   protected Particle particle;
   @Nonnull
   protected FXRenderMode renderMode = FXRenderMode.BlendLinear;
   @Nonnull
   protected EmitShape shape = EmitShape.Sphere;
   protected RangeVector3f emitOffset;
   protected boolean useEmitDirection;
   protected float cameraOffset;
   @Nonnull
   protected ParticleRotationInfluence particleRotationInfluence = ParticleRotationInfluence.None;
   protected boolean particleRotateWithSpawner;
   protected boolean isLowRes;
   protected float trailSpawnerPositionMultiplier;
   protected float trailSpawnerRotationMultiplier;
   protected ParticleCollision particleCollision;
   protected float lightInfluence;
   protected boolean linearFiltering;
   protected Range totalParticles;
   protected float lifeSpan;
   protected int maxConcurrentParticles;
   protected Rangef particleLifeSpan;
   protected Rangef spawnRate;
   protected boolean spawnBurst;
   protected Rangef waveDelay;
   protected InitialVelocity initialVelocity;
   protected float velocityStretchMultiplier;
   protected UVMotion uvMotion;
   protected ParticleAttractor[] attractors;
   protected IntersectionHighlight intersectionHighlight;
   private SoftReference<com.hypixel.hytale.protocol.ParticleSpawner> cachedPacket;

   public static AssetStore<String, ParticleSpawner, DefaultAssetMap<String, ParticleSpawner>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(ParticleSpawner.class);
      }

      return ASSET_STORE;
   }

   public static DefaultAssetMap<String, ParticleSpawner> getAssetMap() {
      return (DefaultAssetMap<String, ParticleSpawner>)getAssetStore().getAssetMap();
   }

   public ParticleSpawner(
      String id,
      Particle particle,
      FXRenderMode renderMode,
      EmitShape shape,
      RangeVector3f emitOffset,
      boolean useEmitDirection,
      float cameraOffset,
      ParticleRotationInfluence particleRotationInfluence,
      boolean particleRotateWithSpawner,
      boolean isLowRes,
      float trailSpawnerPositionMultiplier,
      float trailSpawnerRotationMultiplier,
      ParticleCollision particleCollision,
      float lightInfluence,
      boolean linearFiltering,
      Range totalParticles,
      float lifeSpan,
      int maxConcurrentParticles,
      Rangef particleLifeSpan,
      Rangef spawnRate,
      boolean spawnBurst,
      Rangef waveDelay,
      InitialVelocity initialVelocity,
      float velocityStretchMultiplier,
      UVMotion uvMotion,
      ParticleAttractor[] attractors,
      IntersectionHighlight intersectionHighlight
   ) {
      this.id = id;
      this.particle = particle;
      this.renderMode = renderMode;
      this.shape = shape;
      this.emitOffset = emitOffset;
      this.useEmitDirection = useEmitDirection;
      this.cameraOffset = cameraOffset;
      this.particleRotationInfluence = particleRotationInfluence;
      this.particleRotateWithSpawner = particleRotateWithSpawner;
      this.isLowRes = isLowRes;
      this.trailSpawnerPositionMultiplier = trailSpawnerPositionMultiplier;
      this.trailSpawnerRotationMultiplier = trailSpawnerRotationMultiplier;
      this.particleCollision = particleCollision;
      this.lightInfluence = lightInfluence;
      this.linearFiltering = linearFiltering;
      this.totalParticles = totalParticles;
      this.lifeSpan = lifeSpan;
      this.maxConcurrentParticles = maxConcurrentParticles;
      this.particleLifeSpan = particleLifeSpan;
      this.spawnRate = spawnRate;
      this.spawnBurst = spawnBurst;
      this.waveDelay = waveDelay;
      this.initialVelocity = initialVelocity;
      this.velocityStretchMultiplier = velocityStretchMultiplier;
      this.uvMotion = uvMotion;
      this.attractors = attractors;
      this.intersectionHighlight = intersectionHighlight;
   }

   protected ParticleSpawner() {
   }

   @Nonnull
   public com.hypixel.hytale.protocol.ParticleSpawner toPacket() {
      com.hypixel.hytale.protocol.ParticleSpawner cached = this.cachedPacket == null ? null : this.cachedPacket.get();
      if (cached != null) {
         return cached;
      } else {
         com.hypixel.hytale.protocol.ParticleSpawner packet = new com.hypixel.hytale.protocol.ParticleSpawner();
         packet.id = this.id;
         if (this.particle != null) {
            packet.particle = this.particle.toPacket();
         }

         packet.shape = this.shape;
         packet.renderMode = this.renderMode;
         packet.emitOffset = this.emitOffset;
         packet.useEmitDirection = this.useEmitDirection;
         packet.cameraOffset = this.cameraOffset;
         packet.particleRotationInfluence = this.particleRotationInfluence;
         packet.particleRotateWithSpawner = this.particleRotateWithSpawner;
         packet.isLowRes = this.isLowRes;
         packet.trailSpawnerPositionMultiplier = this.trailSpawnerPositionMultiplier;
         packet.trailSpawnerRotationMultiplier = this.trailSpawnerRotationMultiplier;
         if (this.particleCollision != null) {
            packet.particleCollision = this.particleCollision.toPacket();
            if (this.particleCollision.getParticleRotationInfluence() == null) {
               packet.particleCollision.particleRotationInfluence = this.particleRotationInfluence;
            }
         }

         packet.lightInfluence = this.lightInfluence;
         packet.linearFiltering = this.linearFiltering;
         packet.totalParticles = this.totalParticles;
         packet.lifeSpan = this.lifeSpan;
         packet.maxConcurrentParticles = this.maxConcurrentParticles;
         if (this.particleLifeSpan != null) {
            packet.particleLifeSpan = this.particleLifeSpan;
         }

         if (this.spawnRate != null) {
            packet.spawnRate = this.spawnRate;
         }

         packet.spawnBurst = this.spawnBurst;
         if (this.waveDelay != null) {
            packet.waveDelay = this.waveDelay;
         }

         packet.initialVelocity = this.initialVelocity;
         packet.velocityStretchMultiplier = this.velocityStretchMultiplier;
         packet.uvMotion = this.uvMotion;
         if (this.attractors != null && this.attractors.length > 0) {
            packet.attractors = ArrayUtil.copyAndMutate(this.attractors, ParticleAttractor::toPacket, com.hypixel.hytale.protocol.ParticleAttractor[]::new);
         }

         packet.intersectionHighlight = this.intersectionHighlight;
         this.cachedPacket = new SoftReference<>(packet);
         return packet;
      }
   }

   public String getId() {
      return this.id;
   }

   public Particle getParticle() {
      return this.particle;
   }

   public FXRenderMode getRenderMode() {
      return this.renderMode;
   }

   public EmitShape getShape() {
      return this.shape;
   }

   public RangeVector3f getEmitOffset() {
      return this.emitOffset;
   }

   public boolean getUseEmitDirection() {
      return this.useEmitDirection;
   }

   public float getCameraOffset() {
      return this.cameraOffset;
   }

   public ParticleRotationInfluence getParticleRotationInfluence() {
      return this.particleRotationInfluence;
   }

   public boolean isParticleRotateWithSpawner() {
      return this.particleRotateWithSpawner;
   }

   public boolean isLowRes() {
      return this.isLowRes;
   }

   public float getTrailSpawnerPositionMultiplier() {
      return this.trailSpawnerPositionMultiplier;
   }

   public float getTrailSpawnerRotationMultiplier() {
      return this.trailSpawnerRotationMultiplier;
   }

   public ParticleCollision getParticleCollision() {
      return this.particleCollision;
   }

   public float getLightInfluence() {
      return this.lightInfluence;
   }

   public boolean isLinearFiltering() {
      return this.linearFiltering;
   }

   public Range getTotalParticles() {
      return this.totalParticles;
   }

   public float getLifeSpan() {
      return this.lifeSpan;
   }

   public int getMaxConcurrentParticles() {
      return this.maxConcurrentParticles;
   }

   public Rangef getParticleLifeSpan() {
      return this.particleLifeSpan;
   }

   public Rangef getSpawnRate() {
      return this.spawnRate;
   }

   public boolean isSpawnBurst() {
      return this.spawnBurst;
   }

   public Rangef getWaveDelay() {
      return this.waveDelay;
   }

   public InitialVelocity getInitialVelocity() {
      return this.initialVelocity;
   }

   public float getVelocityStretchMultiplier() {
      return this.velocityStretchMultiplier;
   }

   public UVMotion getUVMotion() {
      return this.uvMotion;
   }

   public ParticleAttractor[] getAttractors() {
      return this.attractors;
   }

   public IntersectionHighlight getIntersectionHighlight() {
      return this.intersectionHighlight;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ParticleSpawner{id='"
         + this.id
         + "', particle='"
         + this.particle
         + ", renderMode="
         + this.renderMode
         + ", shape="
         + this.shape
         + ", emitOffset="
         + this.emitOffset
         + ", useEmitDirection="
         + this.useEmitDirection
         + ", cameraOffset="
         + this.cameraOffset
         + ", particleRotationInfluence="
         + this.particleRotationInfluence
         + ", particleRotateWithSpawner="
         + this.particleRotateWithSpawner
         + ", isLowRes="
         + this.isLowRes
         + ", trailSpawnerPositionMultiplier="
         + this.trailSpawnerPositionMultiplier
         + ", trailSpawnerRotationMultiplier="
         + this.trailSpawnerRotationMultiplier
         + ", particleCollision="
         + this.particleCollision
         + ", lightInfluence="
         + this.lightInfluence
         + ", linearFiltering="
         + this.linearFiltering
         + ", totalParticles="
         + this.totalParticles
         + ", lifeSpan="
         + this.lifeSpan
         + ", maxConcurrentParticles="
         + this.maxConcurrentParticles
         + ", particleLifeSpan="
         + this.particleLifeSpan
         + ", spawnRate="
         + this.spawnRate
         + ", spawnBurst="
         + this.spawnBurst
         + ", waveDelay="
         + this.waveDelay
         + ", initialVelocity="
         + this.initialVelocity
         + ", velocityStretchMultiplier="
         + this.velocityStretchMultiplier
         + ", uvMotion="
         + this.uvMotion
         + ", attractors="
         + Arrays.toString((Object[])this.attractors)
         + ", intersectionHighlight"
         + this.intersectionHighlight
         + "}";
   }
}
