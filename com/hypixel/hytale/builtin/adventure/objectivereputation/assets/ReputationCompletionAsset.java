package com.hypixel.hytale.builtin.adventure.objectivereputation.assets;

import com.hypixel.hytale.builtin.adventure.objectives.config.completion.ObjectiveCompletionAsset;
import com.hypixel.hytale.builtin.adventure.reputation.assets.ReputationGroup;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import javax.annotation.Nonnull;

public class ReputationCompletionAsset extends ObjectiveCompletionAsset {
   @Nonnull
   public static final BuilderCodec<ReputationCompletionAsset> CODEC = BuilderCodec.builder(
         ReputationCompletionAsset.class, ReputationCompletionAsset::new, ObjectiveCompletionAsset.BASE_CODEC
      )
      .append(
         new KeyedCodec<>("ReputationGroupId", Codec.STRING),
         (reputationCompletionAsset, s) -> reputationCompletionAsset.reputationGroupId = s,
         reputationCompletionAsset -> reputationCompletionAsset.reputationGroupId
      )
      .addValidator(Validators.nonNull())
      .addValidator(ReputationGroup.VALIDATOR_CACHE.getValidator())
      .add()
      .<Integer>append(
         new KeyedCodec<>("Amount", Codec.INTEGER),
         (reputationCompletionAsset, integer) -> reputationCompletionAsset.amount = integer,
         reputationCompletionAsset -> reputationCompletionAsset.amount
      )
      .addValidator(Validators.notEqual(0))
      .add()
      .build();
   protected String reputationGroupId;
   protected int amount = 1;

   public ReputationCompletionAsset(String reputationGroupId, int amount) {
      this.reputationGroupId = reputationGroupId;
      this.amount = amount;
   }

   protected ReputationCompletionAsset() {
   }

   public String getReputationGroupId() {
      return this.reputationGroupId;
   }

   public int getAmount() {
      return this.amount;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ReputationCompletionAsset{reputationGroupId='" + this.reputationGroupId + "', amount=" + this.amount + "} " + super.toString();
   }
}
