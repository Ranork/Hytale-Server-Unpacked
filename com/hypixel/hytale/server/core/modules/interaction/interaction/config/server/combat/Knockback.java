package com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.combat;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.ChangeVelocityType;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import com.hypixel.hytale.server.core.modules.splitvelocity.VelocityConfig;
import javax.annotation.Nonnull;

public abstract class Knockback {
   public static final CodecMapCodec<Knockback> CODEC = new CodecMapCodec<>("Type", true);
   public static final BuilderCodec<Knockback> BASE_CODEC = BuilderCodec.abstractBuilder(Knockback.class)
      .append(
         new KeyedCodec<>("Force", Codec.DOUBLE),
         (knockbackAttachment, d) -> knockbackAttachment.force = d.floatValue(),
         knockbackAttachment -> (double)knockbackAttachment.force
      )
      .add()
      .<Float>append(
         new KeyedCodec<>("Duration", Codec.FLOAT),
         (knockbackAttachment, f) -> knockbackAttachment.duration = f,
         knockbackAttachment -> knockbackAttachment.duration
      )
      .addValidator(Validators.greaterThanOrEqual(0.0F))
      .documentation("The duration for which the knockback force should be continuously applied. If 0, force is applied once.")
      .add()
      .append(
         new KeyedCodec<>("VelocityType", ProtocolCodecs.CHANGE_VELOCITY_TYPE_CODEC),
         (knockbackAttachment, d) -> knockbackAttachment.velocityType = d,
         knockbackAttachment -> knockbackAttachment.velocityType
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("VelocityConfig", VelocityConfig.CODEC),
         (o, i) -> o.velocityConfig = i,
         o -> o.velocityConfig,
         (o, p) -> o.velocityConfig = p.velocityConfig
      )
      .add()
      .build();
   protected float force;
   protected float duration;
   protected ChangeVelocityType velocityType = ChangeVelocityType.Add;
   private VelocityConfig velocityConfig;

   protected Knockback() {
   }

   public float getForce() {
      return this.force;
   }

   public float getDuration() {
      return this.duration;
   }

   public ChangeVelocityType getVelocityType() {
      return this.velocityType;
   }

   public VelocityConfig getVelocityConfig() {
      return this.velocityConfig;
   }

   public abstract Vector3d calculateVector(Vector3d var1, float var2, Vector3d var3);

   @Nonnull
   @Override
   public String toString() {
      return "Knockback{, force="
         + this.force
         + ", duration="
         + this.duration
         + ", velocityType="
         + this.velocityType
         + ", velocityConfig="
         + this.velocityConfig
         + "}";
   }
}
