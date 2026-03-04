package com.hypixel.hytale.server.core.asset.type.particle.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.protocol.Vector3f;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import javax.annotation.Nonnull;

public class ParticleAttractor implements NetworkSerializable<com.hypixel.hytale.protocol.ParticleAttractor> {
   public static final BuilderCodec<ParticleAttractor> CODEC = BuilderCodec.builder(ParticleAttractor.class, ParticleAttractor::new)
      .addField(
         new KeyedCodec<>("Position", ProtocolCodecs.VECTOR3F),
         (particleAttractor, o) -> particleAttractor.position = o,
         particleAttractor -> particleAttractor.position
      )
      .addField(
         new KeyedCodec<>("RadialAxis", ProtocolCodecs.VECTOR3F),
         (particleAttractor, o) -> particleAttractor.radialAxis = o,
         particleAttractor -> particleAttractor.radialAxis
      )
      .<Float>append(
         new KeyedCodec<>("TrailPositionMultiplier", Codec.FLOAT),
         (particleAttractor, f) -> particleAttractor.trailPositionMultiplier = f,
         particleAttractor -> particleAttractor.trailPositionMultiplier
      )
      .addValidator(Validators.range(0.0F, 1.0F))
      .add()
      .addField(new KeyedCodec<>("Radius", Codec.FLOAT), (particleAttractor, f) -> particleAttractor.radius = f, particleAttractor -> particleAttractor.radius)
      .addField(
         new KeyedCodec<>("RadialAcceleration", Codec.FLOAT),
         (particleAttractor, f) -> particleAttractor.radialAcceleration = f,
         particleAttractor -> particleAttractor.radialAcceleration
      )
      .addField(
         new KeyedCodec<>("RadialTangentAcceleration", Codec.FLOAT),
         (particleAttractor, f) -> particleAttractor.radialTangentAcceleration = f,
         particleAttractor -> particleAttractor.radialTangentAcceleration
      )
      .addField(
         new KeyedCodec<>("LinearAcceleration", ProtocolCodecs.VECTOR3F),
         (particleAttractor, o) -> particleAttractor.linearAcceleration = o,
         particleAttractor -> particleAttractor.linearAcceleration
      )
      .addField(
         new KeyedCodec<>("RadialImpulse", Codec.FLOAT),
         (particleAttractor, f) -> particleAttractor.radialImpulse = f,
         particleAttractor -> particleAttractor.radialImpulse
      )
      .addField(
         new KeyedCodec<>("RadialTangentImpulse", Codec.FLOAT),
         (particleAttractor, f) -> particleAttractor.radialTangentImpulse = f,
         particleAttractor -> particleAttractor.radialTangentImpulse
      )
      .addField(
         new KeyedCodec<>("LinearImpulse", ProtocolCodecs.VECTOR3F),
         (particleAttractor, o) -> particleAttractor.linearImpulse = o,
         particleAttractor -> particleAttractor.linearImpulse
      )
      .addField(
         new KeyedCodec<>("DampingMultiplier", ProtocolCodecs.VECTOR3F),
         (particleAttractor, o) -> particleAttractor.dampingMultiplier = o,
         particleAttractor -> particleAttractor.dampingMultiplier
      )
      .build();
   protected Vector3f position;
   protected Vector3f radialAxis;
   protected float trailPositionMultiplier;
   protected float radius;
   protected float radialAcceleration;
   protected float radialTangentAcceleration;
   protected Vector3f linearAcceleration;
   protected float radialImpulse;
   protected float radialTangentImpulse;
   protected Vector3f linearImpulse;
   protected Vector3f dampingMultiplier;

   public ParticleAttractor(
      Vector3f position,
      Vector3f radialAxis,
      float trailPositionMultiplier,
      float radius,
      float radialAcceleration,
      float radialTangentAcceleration,
      Vector3f linearAcceleration,
      float radialImpulse,
      float radialTangentImpulse,
      Vector3f linearImpulse,
      Vector3f dampingMultiplier
   ) {
      this.position = position;
      this.radialAxis = radialAxis;
      this.trailPositionMultiplier = trailPositionMultiplier;
      this.radius = radius;
      this.radialAcceleration = radialAcceleration;
      this.radialTangentAcceleration = radialTangentAcceleration;
      this.linearAcceleration = linearAcceleration;
      this.radialImpulse = radialImpulse;
      this.radialTangentImpulse = radialTangentImpulse;
      this.linearImpulse = linearImpulse;
      this.dampingMultiplier = dampingMultiplier;
   }

   protected ParticleAttractor() {
   }

   public Vector3f getPosition() {
      return this.position;
   }

   public Vector3f getRadialAxis() {
      return this.radialAxis;
   }

   public float getTrailPositionMultiplier() {
      return this.trailPositionMultiplier;
   }

   public float getRadius() {
      return this.radius;
   }

   public float getRadialAcceleration() {
      return this.radialAcceleration;
   }

   public float getRadialTangentAcceleration() {
      return this.radialTangentAcceleration;
   }

   public Vector3f getLinearAcceleration() {
      return this.linearAcceleration;
   }

   public float getRadialImpulse() {
      return this.radialImpulse;
   }

   public float getRadialTangentImpulse() {
      return this.radialTangentImpulse;
   }

   public Vector3f getLinearImpulse() {
      return this.linearImpulse;
   }

   public Vector3f getDampingMultiplier() {
      return this.dampingMultiplier;
   }

   @Nonnull
   public com.hypixel.hytale.protocol.ParticleAttractor toPacket() {
      com.hypixel.hytale.protocol.ParticleAttractor packet = new com.hypixel.hytale.protocol.ParticleAttractor();
      if (this.position != null) {
         packet.position = this.position;
      }

      if (this.radialAxis != null) {
         packet.radialAxis = this.radialAxis;
      }

      packet.trailPositionMultiplier = this.trailPositionMultiplier;
      packet.radius = this.radius;
      packet.radialAcceleration = this.radialAcceleration;
      packet.radialTangentAcceleration = this.radialTangentAcceleration;
      if (this.linearAcceleration != null) {
         packet.linearAcceleration = this.linearAcceleration;
      }

      packet.radialImpulse = this.radialImpulse;
      packet.radialTangentImpulse = this.radialTangentImpulse;
      if (this.linearImpulse != null) {
         packet.linearImpulse = this.linearImpulse;
      }

      if (this.dampingMultiplier != null) {
         packet.dampingMultiplier = this.dampingMultiplier;
      }

      return packet;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ParticleAttractor{position="
         + this.position
         + ", radialAxis="
         + this.radialAxis
         + ", trailPositionMultiplier="
         + this.trailPositionMultiplier
         + ", radius="
         + this.radius
         + ", radialAcceleration="
         + this.radialAcceleration
         + ", radialTangentAcceleration="
         + this.radialTangentAcceleration
         + ", linearAcceleration="
         + this.linearAcceleration
         + ", radialImpulse="
         + this.radialImpulse
         + ", radialTangentImpulse="
         + this.radialTangentImpulse
         + ", linearImpulse="
         + this.linearImpulse
         + ", dampingMultiplier="
         + this.dampingMultiplier
         + "}";
   }
}
