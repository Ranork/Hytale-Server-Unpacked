package com.hypixel.hytale.server.core.modules.entity.stamina;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import javax.annotation.Nonnull;

public class StaminaGameplayConfig {
   public static final String ID = "Stamina";
   public static final BuilderCodec<StaminaGameplayConfig> CODEC = BuilderCodec.builder(StaminaGameplayConfig.class, StaminaGameplayConfig::new)
      .appendInherited(
         new KeyedCodec<>("SprintRegenDelay", StaminaGameplayConfig.SprintRegenDelayConfig.CODEC),
         (staminaGameplayConfig, s) -> staminaGameplayConfig.sprintRegenDelay = s,
         staminaGameplayConfig -> staminaGameplayConfig.sprintRegenDelay,
         (staminaGameplayConfig, parent) -> staminaGameplayConfig.sprintRegenDelay = parent.sprintRegenDelay
      )
      .addValidator(Validators.nonNull())
      .documentation("The stamina regeneration delay applied after sprinting")
      .add()
      .build();
   protected StaminaGameplayConfig.SprintRegenDelayConfig sprintRegenDelay;

   @Nonnull
   public StaminaGameplayConfig.SprintRegenDelayConfig getSprintRegenDelay() {
      return this.sprintRegenDelay;
   }

   @Nonnull
   @Override
   public String toString() {
      return "StaminaGameplayConfig{sprintRegenDelay=" + this.sprintRegenDelay + "}";
   }

   public static class SprintRegenDelayConfig {
      public static final BuilderCodec<StaminaGameplayConfig.SprintRegenDelayConfig> CODEC = BuilderCodec.builder(
            StaminaGameplayConfig.SprintRegenDelayConfig.class, StaminaGameplayConfig.SprintRegenDelayConfig::new
         )
         .appendInherited(
            new KeyedCodec<>("EntityStatId", Codec.STRING),
            (entityStatConfig, s) -> entityStatConfig.statId = s,
            entityStatConfig -> entityStatConfig.statId,
            (entityStatConfig, parent) -> entityStatConfig.statId = parent.statId
         )
         .addValidator(Validators.nonNull())
         .addValidator(EntityStatType.VALIDATOR_CACHE.getValidator())
         .documentation("The ID of the stamina regen delay EntityStat")
         .add()
         .<Float>appendInherited(
            new KeyedCodec<>("Value", Codec.FLOAT),
            (entityStatConfig, s) -> entityStatConfig.statValue = s,
            entityStatConfig -> entityStatConfig.statValue,
            (entityStatConfig, parent) -> entityStatConfig.statValue = parent.statValue
         )
         .addValidator(Validators.max(0.0F))
         .documentation("The amount of stamina regen delay to apply")
         .add()
         .afterDecode(entityStatConfig -> entityStatConfig.statIndex = EntityStatType.getAssetMap().getIndex(entityStatConfig.statId))
         .build();
      protected String statId;
      protected int statIndex;
      protected float statValue;

      public int getIndex() {
         return this.statIndex;
      }

      public float getValue() {
         return this.statValue;
      }

      @Nonnull
      @Override
      public String toString() {
         return "SprintRegenDelayConfig{statId='" + this.statId + "', statIndex=" + this.statIndex + ", statValue=" + this.statValue + "}";
      }
   }
}
