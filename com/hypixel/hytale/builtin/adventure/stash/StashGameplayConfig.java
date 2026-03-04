package com.hypixel.hytale.builtin.adventure.stash;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.gameplay.GameplayConfig;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StashGameplayConfig {
   @Nonnull
   public static final String ID = "Stash";
   @Nonnull
   public static final BuilderCodec<StashGameplayConfig> CODEC = BuilderCodec.builder(StashGameplayConfig.class, StashGameplayConfig::new)
      .appendInherited(
         new KeyedCodec<>("ClearContainerDropList", Codec.BOOLEAN),
         (gameplayConfig, clearContainerDropList) -> gameplayConfig.clearContainerDropList = clearContainerDropList,
         gameplayConfig -> gameplayConfig.clearContainerDropList,
         (gameplayConfig, parent) -> gameplayConfig.clearContainerDropList = parent.clearContainerDropList
      )
      .add()
      .build();
   private static final StashGameplayConfig DEFAULT_STASH_GAMEPLAY_CONFIG = new StashGameplayConfig();
   protected boolean clearContainerDropList = true;

   @Nullable
   public static StashGameplayConfig get(@Nonnull GameplayConfig config) {
      return config.getPluginConfig().get(StashGameplayConfig.class);
   }

   public static StashGameplayConfig getOrDefault(@Nonnull GameplayConfig config) {
      StashGameplayConfig stashGameplayConfig = get(config);
      return stashGameplayConfig != null ? stashGameplayConfig : DEFAULT_STASH_GAMEPLAY_CONFIG;
   }

   public boolean isClearContainerDropList() {
      return this.clearContainerDropList;
   }

   @Nonnull
   @Override
   public String toString() {
      return "StashGameplayConfig{clearContainerDropList=" + this.clearContainerDropList + "}";
   }
}
