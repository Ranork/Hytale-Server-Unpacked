package com.hypixel.hytale.server.core.asset.type.particle.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.protocol.InitialVelocity;
import com.hypixel.hytale.protocol.RangeVector3f;
import com.hypixel.hytale.protocol.Rangef;
import com.hypixel.hytale.protocol.Vector3f;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class ParticleSpawnerGroup implements NetworkSerializable<com.hypixel.hytale.protocol.ParticleSpawnerGroup> {
   public static final BuilderCodec<ParticleSpawnerGroup> CODEC = BuilderCodec.builder(ParticleSpawnerGroup.class, ParticleSpawnerGroup::new)
      .append(
         new KeyedCodec<>("SpawnerId", Codec.STRING),
         (particleSpawnerGroup, s) -> particleSpawnerGroup.spawnerId = s,
         particleSpawnerGroup -> particleSpawnerGroup.spawnerId
      )
      .addValidator(ParticleSpawner.VALIDATOR_CACHE.getValidator())
      .add()
      .addField(
         new KeyedCodec<>("PositionOffset", ProtocolCodecs.VECTOR3F),
         (particleSpawnerGroup, o) -> particleSpawnerGroup.positionOffset = o,
         particleSpawnerGroup -> particleSpawnerGroup.positionOffset
      )
      .addField(
         new KeyedCodec<>("RotationOffset", ProtocolCodecs.DIRECTION),
         (particleSpawnerGroup, o) -> particleSpawnerGroup.rotationOffset = o,
         particleSpawnerGroup -> particleSpawnerGroup.rotationOffset
      )
      .addField(
         new KeyedCodec<>("FixedRotation", Codec.BOOLEAN),
         (particleSpawnerGroup, b) -> particleSpawnerGroup.fixedRotation = b,
         particleSpawnerGroup -> particleSpawnerGroup.fixedRotation
      )
      .addField(
         new KeyedCodec<>("SpawnRate", ProtocolCodecs.RANGEF),
         (particleSpawnerGroup, b) -> particleSpawnerGroup.spawnRate = b,
         particleSpawnerGroup -> particleSpawnerGroup.spawnRate
      )
      .addField(
         new KeyedCodec<>("LifeSpan", ProtocolCodecs.RANGEF),
         (particleSpawnerGroup, b) -> particleSpawnerGroup.lifeSpan = b,
         particleSpawnerGroup -> particleSpawnerGroup.lifeSpan
      )
      .addField(
         new KeyedCodec<>("StartDelay", Codec.FLOAT), (particleSpawner, f) -> particleSpawner.startDelay = f, particleSpawner -> particleSpawner.startDelay
      )
      .addField(
         new KeyedCodec<>("WaveDelay", ProtocolCodecs.RANGEF),
         (particleSpawnerGroup, b) -> particleSpawnerGroup.waveDelay = b,
         particleSpawnerGroup -> particleSpawnerGroup.waveDelay
      )
      .addField(
         new KeyedCodec<>("TotalSpawners", Codec.INTEGER),
         (particleSpawnerGroup, i) -> particleSpawnerGroup.totalSpawners = i,
         particleSpawnerGroup -> particleSpawnerGroup.totalSpawners
      )
      .addField(
         new KeyedCodec<>("MaxConcurrent", Codec.INTEGER),
         (particleSpawnerGroup, i) -> particleSpawnerGroup.maxConcurrent = i,
         particleSpawnerGroup -> particleSpawnerGroup.maxConcurrent
      )
      .addField(
         new KeyedCodec<>("InitialVelocity", ProtocolCodecs.INITIAL_VELOCITY),
         (particleSpawnerGroup, o) -> particleSpawnerGroup.initialVelocity = o,
         particleSpawnerGroup -> particleSpawnerGroup.initialVelocity
      )
      .addField(
         new KeyedCodec<>("EmitOffset", ProtocolCodecs.RANGE_VECTOR3F),
         (particleSpawnerGroup, o) -> particleSpawnerGroup.emitOffset = o,
         particleSpawnerGroup -> particleSpawnerGroup.emitOffset
      )
      .addField(
         new KeyedCodec<>("Attractors", new ArrayCodec<>(ParticleAttractor.CODEC, ParticleAttractor[]::new)),
         (particleSpawnerGroup, o) -> particleSpawnerGroup.attractors = o,
         particleSpawnerGroup -> particleSpawnerGroup.attractors
      )
      .build();
   protected String spawnerId;
   protected Vector3f positionOffset;
   protected Direction rotationOffset;
   protected boolean fixedRotation;
   protected Rangef spawnRate;
   protected Rangef lifeSpan;
   protected float startDelay;
   protected Rangef waveDelay;
   protected int totalSpawners = 1;
   protected int maxConcurrent;
   protected InitialVelocity initialVelocity;
   protected RangeVector3f emitOffset;
   protected ParticleAttractor[] attractors;

   public ParticleSpawnerGroup(
      String spawnerId,
      Vector3f positionOffset,
      Direction rotationOffset,
      boolean fixedRotation,
      Rangef spawnRate,
      Rangef lifeSpan,
      float startDelay,
      Rangef waveDelay,
      int totalSpawners,
      int maxConcurrent,
      InitialVelocity initialVelocity,
      RangeVector3f emitOffset,
      ParticleAttractor[] attractors
   ) {
      this.spawnerId = spawnerId;
      this.positionOffset = positionOffset;
      this.rotationOffset = rotationOffset;
      this.fixedRotation = fixedRotation;
      this.spawnRate = spawnRate;
      this.startDelay = startDelay;
      this.lifeSpan = lifeSpan;
      this.waveDelay = waveDelay;
      this.totalSpawners = totalSpawners;
      this.maxConcurrent = maxConcurrent;
      this.initialVelocity = initialVelocity;
      this.emitOffset = emitOffset;
      this.attractors = attractors;
   }

   protected ParticleSpawnerGroup() {
   }

   @Nonnull
   public com.hypixel.hytale.protocol.ParticleSpawnerGroup toPacket() {
      com.hypixel.hytale.protocol.ParticleSpawnerGroup packet = new com.hypixel.hytale.protocol.ParticleSpawnerGroup();
      packet.spawnerId = this.spawnerId;
      packet.positionOffset = this.positionOffset;
      packet.rotationOffset = this.rotationOffset;
      if (this.spawnRate != null) {
         packet.spawnRate = this.spawnRate;
      }

      if (this.lifeSpan != null) {
         packet.lifeSpan = this.lifeSpan;
      }

      if (this.waveDelay != null) {
         packet.waveDelay = this.waveDelay;
      }

      packet.startDelay = this.startDelay;
      packet.maxConcurrent = this.maxConcurrent;
      packet.totalSpawners = this.totalSpawners;
      packet.initialVelocity = this.initialVelocity;
      packet.emitOffset = this.emitOffset;
      if (this.attractors != null && this.attractors.length > 0) {
         packet.attractors = ArrayUtil.copyAndMutate(this.attractors, ParticleAttractor::toPacket, com.hypixel.hytale.protocol.ParticleAttractor[]::new);
      }

      packet.fixedRotation = this.fixedRotation;
      return packet;
   }

   public String getSpawnerId() {
      return this.spawnerId;
   }

   public Vector3f getPositionOffset() {
      return this.positionOffset;
   }

   public Direction getRotationOffset() {
      return this.rotationOffset;
   }

   public boolean isFixedRotation() {
      return this.fixedRotation;
   }

   public Rangef getSpawnRate() {
      return this.spawnRate;
   }

   public Rangef getLifeSpan() {
      return this.lifeSpan;
   }

   public float getStartDelay() {
      return this.startDelay;
   }

   public Rangef getWaveDelay() {
      return this.waveDelay;
   }

   public int getTotalSpawners() {
      return this.totalSpawners;
   }

   public int getMaxConcurrent() {
      return this.maxConcurrent;
   }

   public InitialVelocity getInitialVelocity() {
      return this.initialVelocity;
   }

   public RangeVector3f getEmitOffset() {
      return this.emitOffset;
   }

   public ParticleAttractor[] getAttractors() {
      return this.attractors;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ParticleSpawnerGroup{spawnerId='"
         + this.spawnerId
         + "', positionOffset="
         + this.positionOffset
         + ", rotationOffset="
         + this.rotationOffset
         + ", fixedRotation="
         + this.fixedRotation
         + ", spawnRate="
         + this.spawnRate
         + ", lifeSpan="
         + this.lifeSpan
         + ", startDelay="
         + this.startDelay
         + ", waveDelay="
         + this.waveDelay
         + ", totalSpawners="
         + this.totalSpawners
         + ", maxConcurrent="
         + this.maxConcurrent
         + ", initialVelocity="
         + this.initialVelocity
         + ", emitOffset="
         + this.emitOffset
         + ", attractors="
         + Arrays.toString((Object[])this.attractors)
         + "}";
   }
}
