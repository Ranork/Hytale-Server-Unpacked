package com.hypixel.hytale.builtin.portals.integrations;

import com.hypixel.hytale.builtin.portals.components.voidevent.config.VoidEventConfig;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PortalGameplayConfig {
   @Nonnull
   public static final BuilderCodec<PortalGameplayConfig> CODEC = BuilderCodec.builder(PortalGameplayConfig.class, PortalGameplayConfig::new)
      .append(new KeyedCodec<>("VoidEvent", VoidEventConfig.CODEC), (config, o) -> config.voidEvent = o, config -> config.voidEvent)
      .add()
      .build();
   private VoidEventConfig voidEvent;

   @Nullable
   public VoidEventConfig getVoidEvent() {
      return this.voidEvent;
   }
}
