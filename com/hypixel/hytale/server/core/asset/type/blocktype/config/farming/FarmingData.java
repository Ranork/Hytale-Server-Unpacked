package com.hypixel.hytale.server.core.asset.type.blocktype.config.farming;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.protocol.Rangef;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FarmingData {
   @Nonnull
   public static Codec<FarmingData> CODEC = BuilderCodec.builder(FarmingData.class, FarmingData::new)
      .append(
         new KeyedCodec<>("Stages", new MapCodec<>(new ArrayCodec<>(FarmingStageData.CODEC, FarmingStageData[]::new), HashMap::new)),
         (farming, stages) -> farming.stages = stages,
         farming -> farming.stages
      )
      .add()
      .append(
         new KeyedCodec<>("StartingStageSet", Codec.STRING), (farming, starting) -> farming.startingStageSet = starting, farming -> farming.startingStageSet
      )
      .add()
      .append(
         new KeyedCodec<>("StageSetAfterHarvest", Codec.STRING), (farming, set) -> farming.stageSetAfterHarvest = set, farming -> farming.stageSetAfterHarvest
      )
      .add()
      .append(
         new KeyedCodec<>("ActiveGrowthModifiers", GrowthModifierAsset.CHILD_ASSET_CODEC_ARRAY),
         (farming, modifiers) -> farming.growthModifiers = modifiers,
         farming -> farming.growthModifiers
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("SoilConfig", FarmingData.SoilConfig.CODEC), (o, v) -> o.soilConfig = v, o -> o.soilConfig, (o, p) -> o.soilConfig = p.soilConfig
      )
      .add()
      .afterDecode(farmingData -> {
         if (farmingData != null && farmingData.getStages() != null) {
            if (!farmingData.getStages().containsKey(farmingData.startingStageSet)) {
               throw new IllegalArgumentException("Invalid StartingStageSet " + farmingData.startingStageSet);
            }

            if (farmingData.stageSetAfterHarvest != null && !farmingData.getStages().containsKey(farmingData.stageSetAfterHarvest)) {
               throw new IllegalArgumentException("Invalid StageSetAfterHarvest " + farmingData.startingStageSet);
            }
         }
      })
      .build();
   protected Map<String, FarmingStageData[]> stages;
   protected String startingStageSet = "Default";
   protected String stageSetAfterHarvest;
   protected String[] growthModifiers;
   @Nullable
   protected FarmingData.SoilConfig soilConfig;

   @Nullable
   public Map<String, FarmingStageData[]> getStages() {
      return this.stages;
   }

   @Nullable
   public String getStartingStageSet() {
      return this.startingStageSet;
   }

   public String getStageSetAfterHarvest() {
      return this.stageSetAfterHarvest;
   }

   public String[] getGrowthModifiers() {
      return this.growthModifiers;
   }

   @Nullable
   public FarmingData.SoilConfig getSoilConfig() {
      return this.soilConfig;
   }

   @Nonnull
   @Override
   public String toString() {
      return "FarmingData{stages="
         + this.stages
         + ", startingStageSet='"
         + this.startingStageSet
         + "', stageSetAfterHarvest='"
         + this.stageSetAfterHarvest
         + "', growthModifiers="
         + Arrays.toString((Object[])this.growthModifiers)
         + "}";
   }

   public static class SoilConfig {
      @Nonnull
      public static final BuilderCodec<FarmingData.SoilConfig> CODEC = BuilderCodec.builder(FarmingData.SoilConfig.class, FarmingData.SoilConfig::new)
         .appendInherited(
            new KeyedCodec<>("TargetBlock", Codec.STRING), (o, v) -> o.targetBlock = v, o -> o.targetBlock, (o, p) -> o.targetBlock = p.targetBlock
         )
         .addValidatorLate(() -> BlockType.VALIDATOR_CACHE.getValidator().late())
         .addValidator(Validators.nonNull())
         .add()
         .<Rangef>appendInherited(
            new KeyedCodec<>("Lifetime", ProtocolCodecs.RANGEF), (o, v) -> o.lifetime = v, o -> o.lifetime, (o, p) -> o.lifetime = p.lifetime
         )
         .addValidator(Validators.nonNull())
         .add()
         .build();
      protected String targetBlock;
      protected Rangef lifetime;

      public String getTargetBlock() {
         return this.targetBlock;
      }

      public Rangef getLifetime() {
         return this.lifetime;
      }
   }
}
