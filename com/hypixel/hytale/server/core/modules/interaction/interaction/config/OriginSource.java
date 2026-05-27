package com.hypixel.hytale.server.core.modules.interaction.interaction.config;

import com.hypixel.hytale.codec.codecs.EnumCodec;
import javax.annotation.Nonnull;

public enum OriginSource {
   ENTITY,
   BLOCK;

   @Nonnull
   public static final EnumCodec<OriginSource> CODEC = new EnumCodec<>(OriginSource.class)
      .documentKey(ENTITY, "The origin will be based on the position of the entity performing the interaction.")
      .documentKey(BLOCK, "The origin will be based on the position of the targeted block.");
}
