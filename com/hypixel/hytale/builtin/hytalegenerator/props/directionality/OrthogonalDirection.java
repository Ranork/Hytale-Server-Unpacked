package com.hypixel.hytale.builtin.hytalegenerator.props.directionality;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import javax.annotation.Nonnull;

public enum OrthogonalDirection {
   N,
   S,
   E,
   W,
   U,
   D;

   @Nonnull
   public static final Codec<OrthogonalDirection> CODEC = new EnumCodec<>(OrthogonalDirection.class, EnumCodec.EnumStyle.LEGACY);
}
