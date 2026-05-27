package com.hypixel.hytale.server.core.modules.interaction.interaction.config;

import com.hypixel.hytale.codec.codecs.EnumCodec;
import javax.annotation.Nonnull;

public enum RelativeRotationMode {
   NONE,
   YAW,
   FULL;

   @Nonnull
   public static final EnumCodec<RelativeRotationMode> CODEC = new EnumCodec<>(RelativeRotationMode.class)
      .documentKey(NONE, "The reference rotation will not be applied. Values are treated as absolute.")
      .documentKey(YAW, "Only the yaw (Y-axis) component of the reference rotation will be applied.")
      .documentKey(FULL, "The full reference rotation (pitch, yaw, and roll) will be applied.");
}
