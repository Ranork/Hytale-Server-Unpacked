package com.hypixel.hytale.server.core.asset.type.particle.config;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.protocol.ParticleCollisionAction;
import com.hypixel.hytale.protocol.ParticleCollisionBlockType;
import com.hypixel.hytale.protocol.ParticleRotationInfluence;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import javax.annotation.Nonnull;

public class ParticleCollision implements NetworkSerializable<com.hypixel.hytale.protocol.ParticleCollision> {
   public static final BuilderCodec<ParticleCollision> CODEC = BuilderCodec.builder(ParticleCollision.class, ParticleCollision::new)
      .append(
         new KeyedCodec<>("BlockType", new EnumCodec<>(ParticleCollisionBlockType.class)),
         (particleCollision, o) -> particleCollision.blockType = o,
         particleCollision -> particleCollision.blockType
      )
      .addValidator(Validators.nonNull())
      .add()
      .<ParticleCollisionAction>append(
         new KeyedCodec<>("Action", new EnumCodec<>(ParticleCollisionAction.class)),
         (particleCollision, o) -> particleCollision.action = o,
         particleCollision -> particleCollision.action
      )
      .addValidator(Validators.nonNull())
      .add()
      .append(
         new KeyedCodec<>("ParticleRotationInfluence", new EnumCodec<>(ParticleRotationInfluence.class)),
         (particleCollision, o) -> particleCollision.particleRotationInfluence = o,
         particleCollision -> particleCollision.particleRotationInfluence
      )
      .add()
      .build();
   @Nonnull
   private ParticleCollisionBlockType blockType = ParticleCollisionBlockType.None;
   @Nonnull
   private ParticleCollisionAction action = ParticleCollisionAction.Expire;
   private ParticleRotationInfluence particleRotationInfluence;

   public ParticleCollision(ParticleCollisionBlockType blockType, ParticleCollisionAction action, ParticleRotationInfluence particleRotationInfluence) {
      this.blockType = blockType;
      this.action = action;
      this.particleRotationInfluence = particleRotationInfluence;
   }

   protected ParticleCollision() {
   }

   public ParticleCollisionBlockType getParticleMapCollision() {
      return this.blockType;
   }

   public ParticleCollisionAction getType() {
      return this.action;
   }

   public ParticleRotationInfluence getParticleRotationInfluence() {
      return this.particleRotationInfluence;
   }

   @Nonnull
   public com.hypixel.hytale.protocol.ParticleCollision toPacket() {
      com.hypixel.hytale.protocol.ParticleCollision packet = new com.hypixel.hytale.protocol.ParticleCollision();
      packet.blockType = this.blockType;
      packet.action = this.action;
      packet.particleRotationInfluence = this.particleRotationInfluence != null ? this.particleRotationInfluence : ParticleRotationInfluence.None;
      return packet;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ParticleCollision{blockType="
         + this.blockType
         + ", action="
         + this.action
         + ", particleRotationInfluence="
         + this.particleRotationInfluence
         + "}";
   }
}
