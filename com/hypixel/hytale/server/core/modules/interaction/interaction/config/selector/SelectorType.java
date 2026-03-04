package com.hypixel.hytale.server.core.modules.interaction.interaction.config.selector;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import javax.annotation.Nonnull;

public abstract class SelectorType implements NetworkSerializable<com.hypixel.hytale.protocol.Selector> {
   @Nonnull
   public static final CodecMapCodec<SelectorType> CODEC = new CodecMapCodec<>();
   @Nonnull
   public static final BuilderCodec<SelectorType> BASE_CODEC = BuilderCodec.abstractBuilder(SelectorType.class).build();

   @Nonnull
   public abstract Selector newSelector();
}
