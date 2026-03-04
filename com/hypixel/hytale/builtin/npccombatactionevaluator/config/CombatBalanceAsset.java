package com.hypixel.hytale.builtin.npccombatactionevaluator.config;

import com.hypixel.hytale.builtin.npccombatactionevaluator.evaluator.CombatActionEvaluatorConfig;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.server.npc.config.balancing.BalanceAsset;
import javax.annotation.Nonnull;

public class CombatBalanceAsset extends BalanceAsset {
   @Nonnull
   public static final BuilderCodec<CombatBalanceAsset> CODEC = BuilderCodec.builder(CombatBalanceAsset.class, CombatBalanceAsset::new, BASE_CODEC)
      .documentation("A balance asset which also configures a combat action evaluator.")
      .<Float>appendInherited(
         new KeyedCodec<>("TargetMemoryDuration", Codec.FLOAT),
         (balanceAsset, f) -> balanceAsset.targetMemoryDuration = f,
         balanceAsset -> balanceAsset.targetMemoryDuration,
         (balanceAsset, parent) -> balanceAsset.targetMemoryDuration = parent.targetMemoryDuration
      )
      .documentation("How long the target should remain in the NPCs list of potential targets after last being spotted")
      .addValidator(Validators.greaterThan(0.0F))
      .add()
      .<CombatActionEvaluatorConfig>appendInherited(
         new KeyedCodec<>("CombatActionEvaluator", CombatActionEvaluatorConfig.CODEC),
         (balanceAsset, o) -> balanceAsset.evaluatorConfig = o,
         balanceAsset -> balanceAsset.evaluatorConfig,
         (balanceAsset, parent) -> balanceAsset.targetMemoryDuration = parent.targetMemoryDuration
      )
      .addValidator(Validators.nonNull())
      .documentation("The combat action evaluator complete with combat action definitions and conditions.")
      .add()
      .build();
   protected float targetMemoryDuration = 15.0F;
   protected CombatActionEvaluatorConfig evaluatorConfig;

   public float getTargetMemoryDuration() {
      return this.targetMemoryDuration;
   }

   public CombatActionEvaluatorConfig getEvaluatorConfig() {
      return this.evaluatorConfig;
   }

   @Nonnull
   @Override
   public String toString() {
      return "CombatBalanceAsset{TargetMemoryDuration='" + this.targetMemoryDuration + "'}";
   }
}
