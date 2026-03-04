package com.hypixel.hytale.builtin.adventure.objectives.config.gameplayconfig;

import com.hypixel.hytale.builtin.adventure.objectives.config.ObjectiveLineAsset;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.server.core.asset.type.gameplay.GameplayConfig;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ObjectiveGameplayConfig {
   @Nonnull
   public static final String ID = "Objective";
   @Nonnull
   public static final BuilderCodec<ObjectiveGameplayConfig> CODEC = BuilderCodec.builder(ObjectiveGameplayConfig.class, ObjectiveGameplayConfig::new)
      .appendInherited(
         new KeyedCodec<>("StarterObjectiveLinePerWorld", new MapCodec<>(Codec.STRING, Object2ObjectOpenHashMap::new, true)),
         (o, s) -> o.starterObjectiveLinePerWorld = s,
         o -> o.starterObjectiveLinePerWorld,
         (o, parent) -> o.starterObjectiveLinePerWorld = parent.starterObjectiveLinePerWorld
      )
      .addValidator(ObjectiveLineAsset.VALIDATOR_CACHE.getMapValueValidator())
      .add()
      .build();
   protected Map<String, String> starterObjectiveLinePerWorld;

   @Nullable
   public static ObjectiveGameplayConfig get(@Nonnull GameplayConfig config) {
      return config.getPluginConfig().get(ObjectiveGameplayConfig.class);
   }

   public Map<String, String> getStarterObjectiveLinePerWorld() {
      return this.starterObjectiveLinePerWorld;
   }
}
