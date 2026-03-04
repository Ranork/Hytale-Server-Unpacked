package com.hypixel.hytale.server.core.asset.type.entityeffect.config;

import com.hypixel.hytale.codec.codecs.EnumCodec;
import javax.annotation.Nonnull;

public enum RemovalBehavior {
   COMPLETE,
   INFINITE,
   DURATION;

   @Nonnull
   public static final EnumCodec<RemovalBehavior> CODEC = new EnumCodec<>(RemovalBehavior.class);
}
