package com.hypixel.hytale.server.core.asset.type.gameplay;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import java.time.Duration;
import javax.annotation.Nonnull;

public class CombatConfig {
   @Nonnull
   public static final BuilderCodec<CombatConfig> CODEC = BuilderCodec.builder(CombatConfig.class, CombatConfig::new)
      .appendInherited(
         new KeyedCodec<>("OutOfCombatDelaySeconds", Codec.DURATION_SECONDS),
         (combatConfig, v) -> combatConfig.outOfCombatDelay = v,
         combatConfig -> combatConfig.outOfCombatDelay,
         (combatConfig, parent) -> combatConfig.outOfCombatDelay = parent.outOfCombatDelay
      )
      .documentation("Delay before an entity is considered out of combat. Expressed in seconds.")
      .addValidator(Validators.greaterThanOrEqual(Duration.ZERO))
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("StaminaBrokenEffectId", Codec.STRING),
         (combatConfig, s) -> combatConfig.staminaBrokenEffectId = s,
         combatConfig -> combatConfig.staminaBrokenEffectId,
         (combatConfig, parent) -> combatConfig.staminaBrokenEffectId = parent.staminaBrokenEffectId
      )
      .documentation("The id of the EntityEffect to apply upon stamina being depleted due to damage.")
      .addValidator(Validators.nonNull())
      .addValidator(EntityEffect.VALIDATOR_CACHE.getValidator())
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("DisplayHealthBars", Codec.BOOLEAN),
         (combatConfig, v) -> combatConfig.displayHealthBars = v,
         combatConfig -> combatConfig.displayHealthBars,
         (combatConfig, parent) -> combatConfig.displayHealthBars = parent.displayHealthBars
      )
      .documentation("Whether to display health bars above entities. Clients can still disable this in their settings.")
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("DisplayCombatText", Codec.BOOLEAN),
         (combatConfig, v) -> combatConfig.displayCombatText = v,
         combatConfig -> combatConfig.displayCombatText,
         (combatConfig, parent) -> combatConfig.displayCombatText = parent.displayCombatText
      )
      .documentation("Whether to display combat text (damage numbers) on entities. Clients can still disable this in their settings.")
      .add()
      .afterDecode(combatConfig -> combatConfig.staminaBrokenEffectIndex = EntityEffect.getAssetMap().getIndex(combatConfig.staminaBrokenEffectId))
      .<Boolean>appendInherited(
         new KeyedCodec<>("DisableNPCIncomingDamage", Codec.BOOLEAN),
         (combatConfig, v) -> combatConfig.disableNpcIncomingDamage = v,
         combatConfig -> combatConfig.disableNpcIncomingDamage,
         (combatConfig, parent) -> combatConfig.disableNpcIncomingDamage = parent.disableNpcIncomingDamage
      )
      .documentation("Whether NPCs can take damage.")
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("DisablePlayerIncomingDamage", Codec.BOOLEAN),
         (combatConfig, v) -> combatConfig.disablePlayerIncomingDamage = v,
         combatConfig -> combatConfig.disablePlayerIncomingDamage,
         (combatConfig, parent) -> combatConfig.disablePlayerIncomingDamage = parent.disablePlayerIncomingDamage
      )
      .documentation("Whether players can take damage.")
      .add()
      .build();
   @Nonnull
   protected Duration outOfCombatDelay = Duration.ofMillis(5000L);
   protected String staminaBrokenEffectId = "Stamina_Broken";
   private int staminaBrokenEffectIndex;
   protected boolean displayHealthBars = true;
   protected boolean displayCombatText = true;
   protected boolean disableNpcIncomingDamage = false;
   protected boolean disablePlayerIncomingDamage = false;

   public CombatConfig() {
      this.staminaBrokenEffectIndex = EntityEffect.getAssetMap().getIndex(this.staminaBrokenEffectId);
   }

   @Nonnull
   public Duration getOutOfCombatDelay() {
      return this.outOfCombatDelay;
   }

   public int getStaminaBrokenEffectIndex() {
      return this.staminaBrokenEffectIndex;
   }

   public boolean isDisplayHealthBars() {
      return this.displayHealthBars;
   }

   public boolean isDisplayCombatText() {
      return this.displayCombatText;
   }

   public boolean isNpcIncomingDamageDisabled() {
      return this.disableNpcIncomingDamage;
   }

   public boolean isPlayerIncomingDamageDisabled() {
      return this.disablePlayerIncomingDamage;
   }
}
