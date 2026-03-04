package com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.combat;

import com.hypixel.hytale.codec.codecs.EnumCodec;

public enum DamageClass {
   UNKNOWN,
   LIGHT,
   CHARGED,
   SIGNATURE;

   public static final EnumCodec<DamageClass> CODEC = new EnumCodec<>(DamageClass.class);
}
