package com.hypixel.hytale.server.core.asset.type.model.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.protocol.EntityPart;
import com.hypixel.hytale.protocol.Vector3f;
import com.hypixel.hytale.server.core.asset.type.particle.config.ParticleSystem;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import javax.annotation.Nonnull;

public class ModelParticle implements NetworkSerializable<com.hypixel.hytale.protocol.ModelParticle> {
   public static final BuilderCodec<ModelParticle> CODEC = BuilderCodec.builder(ModelParticle.class, ModelParticle::new)
      .append(new KeyedCodec<>("SystemId", Codec.STRING), (particle, s) -> particle.systemId = s, particle -> particle.systemId)
      .addValidator(Validators.nonNull())
      .addValidator(ParticleSystem.VALIDATOR_CACHE.getValidator())
      .add()
      .<EntityPart>append(
         new KeyedCodec<>("TargetEntityPart", new EnumCodec<>(EntityPart.class)),
         (particle, o) -> particle.targetEntityPart = o,
         particle -> particle.targetEntityPart
      )
      .addValidator(Validators.nonNull())
      .add()
      .append(new KeyedCodec<>("TargetNodeName", Codec.STRING), (particle, s) -> particle.targetNodeName = s, particle -> particle.targetNodeName)
      .add()
      .append(new KeyedCodec<>("Color", ProtocolCodecs.COLOR), (particle, o) -> particle.color = o, particle -> particle.color)
      .add()
      .<Double>append(new KeyedCodec<>("Scale", Codec.DOUBLE), (particle, o) -> particle.scale = o.floatValue(), particle -> (double)particle.scale)
      .addValidator(Validators.greaterThan(0.0))
      .add()
      .append(new KeyedCodec<>("PositionOffset", ProtocolCodecs.VECTOR3F), (particle, s) -> particle.positionOffset = s, particle -> particle.positionOffset)
      .add()
      .append(new KeyedCodec<>("RotationOffset", ProtocolCodecs.DIRECTION), (particle, s) -> particle.rotationOffset = s, particle -> particle.rotationOffset)
      .add()
      .<Boolean>append(
         new KeyedCodec<>("DetachedFromModel", Codec.BOOLEAN),
         (modelParticle, aBoolean) -> modelParticle.detachedFromModel = aBoolean,
         modelParticle -> modelParticle.detachedFromModel
      )
      .documentation("To indicate if the spawned particle should be attached to the model and follow it, or spawn in world space.")
      .add()
      .build();
   public static final ArrayCodec<ModelParticle> ARRAY_CODEC = new ArrayCodec<>(CODEC, ModelParticle[]::new);
   protected String systemId;
   @Nonnull
   protected EntityPart targetEntityPart = EntityPart.Self;
   protected String targetNodeName;
   protected Color color;
   protected float scale = 1.0F;
   protected Vector3f positionOffset;
   protected Direction rotationOffset;
   protected boolean detachedFromModel;

   public ModelParticle(
      String systemId,
      EntityPart targetEntityPart,
      String targetNodeName,
      Color color,
      float scale,
      Vector3f positionOffset,
      Direction rotationOffset,
      boolean detachedFromModel
   ) {
      this.systemId = systemId;
      this.targetEntityPart = targetEntityPart;
      this.targetNodeName = targetNodeName;
      this.color = color;
      this.scale = scale;
      this.positionOffset = positionOffset;
      this.rotationOffset = rotationOffset;
      this.detachedFromModel = detachedFromModel;
   }

   public ModelParticle(ModelParticle other) {
      this.systemId = other.systemId;
      this.targetEntityPart = other.targetEntityPart;
      this.targetNodeName = other.targetNodeName;
      this.color = other.color;
      this.scale = other.scale;
      this.positionOffset = other.positionOffset;
      this.rotationOffset = other.rotationOffset;
      this.detachedFromModel = other.detachedFromModel;
   }

   public ModelParticle() {
   }

   @Nonnull
   public com.hypixel.hytale.protocol.ModelParticle toPacket() {
      com.hypixel.hytale.protocol.ModelParticle packet = new com.hypixel.hytale.protocol.ModelParticle();
      packet.systemId = this.systemId;
      packet.targetEntityPart = this.targetEntityPart;
      packet.targetNodeName = this.targetNodeName;
      packet.color = this.color;
      packet.scale = this.scale;
      packet.positionOffset = this.positionOffset;
      packet.rotationOffset = this.rotationOffset;
      packet.detachedFromModel = this.detachedFromModel;
      return packet;
   }

   public String getSystemId() {
      return this.systemId;
   }

   public void setSystemId(String systemId) {
      this.systemId = systemId;
   }

   public EntityPart getTargetEntityPart() {
      return this.targetEntityPart;
   }

   public String getTargetNodeName() {
      return this.targetNodeName;
   }

   public void setTargetNodeName(String targetNodeName) {
      this.targetNodeName = targetNodeName;
   }

   public Color getColor() {
      return this.color;
   }

   public float getScale() {
      return this.scale;
   }

   public Vector3f getPositionOffset() {
      return this.positionOffset;
   }

   public void setPositionOffset(Vector3f positionOffset) {
      this.positionOffset = positionOffset;
   }

   public Direction getRotationOffset() {
      return this.rotationOffset;
   }

   public boolean isDetachedFromModel() {
      return this.detachedFromModel;
   }

   public void setDetachedFromModel(boolean detachedFromModel) {
      this.detachedFromModel = detachedFromModel;
   }

   public ModelParticle scale(float scale) {
      this.scale *= scale;
      if (this.positionOffset != null) {
         this.positionOffset.x *= scale;
         this.positionOffset.y *= scale;
         this.positionOffset.z *= scale;
      }

      return this;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ModelParticle{systemId='"
         + this.systemId
         + "', targetEntityPart="
         + this.targetEntityPart
         + ", targetNodeName='"
         + this.targetNodeName
         + "', color="
         + this.color
         + ", scale="
         + this.scale
         + ", positionOffset="
         + this.positionOffset
         + ", rotationOffset="
         + this.rotationOffset
         + ", detachedFromModel="
         + this.detachedFromModel
         + "}";
   }

   public ModelParticle clone() {
      return new ModelParticle(this);
   }
}
