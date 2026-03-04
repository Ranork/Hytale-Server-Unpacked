package com.hypixel.hytale.builtin.adventure.reputation;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.server.core.asset.type.gameplay.GameplayConfig;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ReputationGameplayConfig {
   public static final String ID = "Reputation";
   @Nonnull
   public static final BuilderCodec<ReputationGameplayConfig> CODEC = BuilderCodec.builder(ReputationGameplayConfig.class, ReputationGameplayConfig::new)
      .appendInherited(
         new KeyedCodec<>("ReputationStorage", new EnumCodec<>(ReputationGameplayConfig.ReputationStorageType.class)),
         (gameplayConfig, o) -> gameplayConfig.reputationStorageType = o,
         gameplayConfig -> gameplayConfig.reputationStorageType,
         (gameplayConfig, parent) -> gameplayConfig.reputationStorageType = parent.reputationStorageType
      )
      .add()
      .build();
   @Nonnull
   private static final ReputationGameplayConfig DEFAULT_REPUTATION_GAMEPLAY_CONFIG = new ReputationGameplayConfig();
   @Nonnull
   protected ReputationGameplayConfig.ReputationStorageType reputationStorageType = ReputationGameplayConfig.ReputationStorageType.PerPlayer;

   @Nullable
   public static ReputationGameplayConfig get(@Nonnull GameplayConfig config) {
      return config.getPluginConfig().get(ReputationGameplayConfig.class);
   }

   @Nonnull
   public static ReputationGameplayConfig getOrDefault(@Nonnull GameplayConfig config) {
      ReputationGameplayConfig reputationGameplayConfig = get(config);
      return reputationGameplayConfig != null ? reputationGameplayConfig : DEFAULT_REPUTATION_GAMEPLAY_CONFIG;
   }

   @Nonnull
   public ReputationGameplayConfig.ReputationStorageType getReputationStorageType() {
      return this.reputationStorageType;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ReputationGameplayConfig{reputationStorageType=" + this.reputationStorageType + "}";
   }

   public static enum ReputationStorageType {
      PerPlayer,
      PerWorld;
   }
}
