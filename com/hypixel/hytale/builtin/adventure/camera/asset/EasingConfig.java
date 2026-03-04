package com.hypixel.hytale.builtin.adventure.camera.asset;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.protocol.EasingType;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import javax.annotation.Nonnull;

public class EasingConfig implements NetworkSerializable<com.hypixel.hytale.protocol.EasingConfig> {
   @Nonnull
   public static final BuilderCodec<EasingConfig> CODEC = BuilderCodec.builder(EasingConfig.class, EasingConfig::new)
      .appendInherited(new KeyedCodec<>("Time", Codec.FLOAT), (o, v) -> o.time = v, o -> o.time, (o, p) -> o.time = p.time)
      .documentation("The duration time of the easing")
      .addValidator(Validators.min(0.0F))
      .add()
      .<EasingType>appendInherited(new KeyedCodec<>("Type", ProtocolCodecs.EASING_TYPE_CODEC), (o, v) -> o.type = v, o -> o.type, (o, p) -> o.type = p.type)
      .documentation("The curve type of the easing")
      .addValidator(Validators.nonNull())
      .add()
      .build();
   @Nonnull
   public static final EasingConfig NONE = new EasingConfig();
   protected float time;
   @Nonnull
   protected EasingType type = EasingType.Linear;

   @Nonnull
   public com.hypixel.hytale.protocol.EasingConfig toPacket() {
      return new com.hypixel.hytale.protocol.EasingConfig(this.time, this.type);
   }

   @Nonnull
   @Override
   public String toString() {
      return "EasingConfig{time=" + this.time + ", type=" + this.type + "}";
   }
}
