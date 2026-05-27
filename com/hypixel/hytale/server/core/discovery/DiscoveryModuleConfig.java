package com.hypixel.hytale.server.core.discovery;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nullable;

public class DiscoveryModuleConfig {
   public static final BuilderCodec<DiscoveryModuleConfig> CODEC = BuilderCodec.builder(DiscoveryModuleConfig.class, DiscoveryModuleConfig::new)
      .append(new KeyedCodec<>("DiscoveryToken", Codec.STRING, false), (config, s) -> config.discoveryToken = s, config -> config.discoveryToken)
      .add()
      .build();
   @Nullable
   private String discoveryToken;

   @Nullable
   public String getDiscoveryToken() {
      return this.discoveryToken;
   }

   public void setDiscoveryToken(@Nullable String discoveryToken) {
      this.discoveryToken = discoveryToken;
   }
}
