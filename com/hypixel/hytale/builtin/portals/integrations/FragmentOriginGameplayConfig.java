package com.hypixel.hytale.builtin.portals.integrations;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class FragmentOriginGameplayConfig {
   @Nonnull
   public static final BuilderCodec<FragmentOriginGameplayConfig> CODEC = BuilderCodec.builder(
         FragmentOriginGameplayConfig.class, FragmentOriginGameplayConfig::new
      )
      .append(
         new KeyedCodec<>("MaxConcurrentFragments", Codec.INTEGER), (config, o) -> config.maxConcurrentFragments = o, config -> config.maxConcurrentFragments
      )
      .documentation(
         "The max number of portal fragments which can be open at the same time. Note that while the fragment count is shared across the server, the limit is enforced by the world's GameplayConfig."
      )
      .add()
      .build();
   public static final int DEFAULT_MAX_CONCURRENT_FRAGMENTS = 4;
   private int maxConcurrentFragments = 4;

   public int getMaxConcurrentFragments() {
      return this.maxConcurrentFragments;
   }
}
