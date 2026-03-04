package com.hypixel.hytale.builtin.creativehub.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CreativeHubWorldConfig {
   @Nonnull
   public static final String ID = "CreativeHub";
   @Nonnull
   public static final BuilderCodec<CreativeHubWorldConfig> CODEC = BuilderCodec.builder(CreativeHubWorldConfig.class, CreativeHubWorldConfig::new)
      .append(new KeyedCodec<>("StartupInstance", Codec.STRING), (o, i) -> o.startupInstance = i, o -> o.startupInstance)
      .documentation("The name of the instance to spawn players into when they first join this world.")
      .add()
      .build();
   @Nullable
   private String startupInstance;

   @Nullable
   public static CreativeHubWorldConfig get(@Nonnull WorldConfig config) {
      return config.getPluginConfig().get(CreativeHubWorldConfig.class);
   }

   @Nullable
   public String getStartupInstance() {
      return this.startupInstance;
   }
}
