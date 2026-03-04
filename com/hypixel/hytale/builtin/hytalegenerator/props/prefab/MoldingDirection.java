package com.hypixel.hytale.builtin.hytalegenerator.props.prefab;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import javax.annotation.Nonnull;

public enum MoldingDirection {
   NONE,
   UP,
   DOWN,
   NORTH,
   SOUTH,
   EAST,
   WEST;

   @Nonnull
   public static final Codec<MoldingDirection> CODEC = new EnumCodec<>(MoldingDirection.class, EnumCodec.EnumStyle.LEGACY);
}
