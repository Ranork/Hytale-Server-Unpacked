package com.hypixel.hytale.server.core.asset.type.gameplay;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class GatheringConfig {
   @Nonnull
   public static final BuilderCodec<GatheringConfig> CODEC = BuilderCodec.builder(GatheringConfig.class, GatheringConfig::new)
      .appendInherited(
         new KeyedCodec<>("UnbreakableBlock", GatheringEffectsConfig.CODEC),
         (gameplayConfig, o) -> gameplayConfig.unbreakableBlockConfig = o,
         gameplayConfig -> gameplayConfig.unbreakableBlockConfig,
         (gameplayConfig, parent) -> gameplayConfig.unbreakableBlockConfig = parent.unbreakableBlockConfig
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("IncorrectTool", GatheringEffectsConfig.CODEC),
         (gameplayConfig, o) -> gameplayConfig.incorrectToolConfig = o,
         gameplayConfig -> gameplayConfig.incorrectToolConfig,
         (gameplayConfig, parent) -> gameplayConfig.incorrectToolConfig = parent.incorrectToolConfig
      )
      .add()
      .build();
   @Nonnull
   protected GatheringEffectsConfig unbreakableBlockConfig = new GatheringEffectsConfig();
   @Nonnull
   protected GatheringEffectsConfig incorrectToolConfig = new GatheringEffectsConfig();

   @Nonnull
   public GatheringEffectsConfig getUnbreakableBlockConfig() {
      return this.unbreakableBlockConfig;
   }

   @Nonnull
   public GatheringEffectsConfig getIncorrectToolConfig() {
      return this.incorrectToolConfig;
   }
}
