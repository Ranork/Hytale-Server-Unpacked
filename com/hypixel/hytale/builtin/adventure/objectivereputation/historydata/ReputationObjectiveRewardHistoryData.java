package com.hypixel.hytale.builtin.adventure.objectivereputation.historydata;

import com.hypixel.hytale.builtin.adventure.objectives.historydata.ObjectiveRewardHistoryData;
import com.hypixel.hytale.builtin.adventure.reputation.assets.ReputationGroup;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import javax.annotation.Nonnull;

public final class ReputationObjectiveRewardHistoryData extends ObjectiveRewardHistoryData {
   @Nonnull
   public static final BuilderCodec<ReputationObjectiveRewardHistoryData> CODEC = BuilderCodec.builder(
         ReputationObjectiveRewardHistoryData.class, ReputationObjectiveRewardHistoryData::new, ObjectiveRewardHistoryData.BASE_CODEC
      )
      .append(
         new KeyedCodec<>("ReputationGroupId", Codec.STRING),
         (reputationObjectiveRewardDetails, s) -> reputationObjectiveRewardDetails.reputationGroupId = s,
         reputationObjectiveRewardDetails -> reputationObjectiveRewardDetails.reputationGroupId
      )
      .addValidator(Validators.nonNull())
      .addValidator(ReputationGroup.VALIDATOR_CACHE.getValidator())
      .add()
      .append(
         new KeyedCodec<>("Amount", Codec.INTEGER),
         (reputationObjectiveRewardHistoryData, integer) -> reputationObjectiveRewardHistoryData.amount = integer,
         reputationObjectiveRewardHistoryData -> reputationObjectiveRewardHistoryData.amount
      )
      .add()
      .build();
   protected String reputationGroupId;
   protected int amount;

   public ReputationObjectiveRewardHistoryData(String reputationGroupId, int amount) {
      this.reputationGroupId = reputationGroupId;
      this.amount = amount;
   }

   protected ReputationObjectiveRewardHistoryData() {
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
      return "ReputationObjectiveRewardHistoryData{reputationGroupId='" + this.reputationGroupId + "', amount=" + this.amount + "} " + super.toString();
   }
}
