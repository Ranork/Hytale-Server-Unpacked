package com.hypixel.hytale.server.core.asset.type.blocktype.config;

import com.hypixel.hytale.codec.codecs.EnumCodec;

public enum SupportDropType {
   BREAK,
   DESTROY;

   public static final EnumCodec<SupportDropType> CODEC = new EnumCodec<>(SupportDropType.class);
}
