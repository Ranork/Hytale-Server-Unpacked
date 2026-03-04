package com.hypixel.hytale.builtin.adventure.reputation.choices;

import com.hypixel.hytale.builtin.adventure.reputation.ReputationPlugin;
import com.hypixel.hytale.builtin.adventure.reputation.assets.ReputationGroup;
import com.hypixel.hytale.builtin.adventure.reputation.assets.ReputationRank;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceRequirement;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class ReputationRequirement extends ChoiceRequirement {
   @Nonnull
   public static final BuilderCodec<ReputationRequirement> CODEC = BuilderCodec.builder(
         ReputationRequirement.class, ReputationRequirement::new, ChoiceRequirement.BASE_CODEC
      )
      .append(
         new KeyedCodec<>("ReputationGroupId", Codec.STRING),
         (reputationRequirement, s) -> reputationRequirement.reputationGroupId = s,
         reputationRequirement -> reputationRequirement.reputationGroupId
      )
      .addValidator(ReputationGroup.VALIDATOR_CACHE.getValidator())
      .add()
      .<String>append(
         new KeyedCodec<>("MinRequiredRankId", Codec.STRING),
         (reputationRequirement, s) -> reputationRequirement.minRequiredRankId = s,
         reputationRequirement -> reputationRequirement.minRequiredRankId
      )
      .addValidator(ReputationRank.VALIDATOR_CACHE.getValidator())
      .add()
      .build();
   protected String reputationGroupId;
   protected String minRequiredRankId;

   public ReputationRequirement(String reputationGroupId, String minRequiredRankId) {
      this.reputationGroupId = reputationGroupId;
      this.minRequiredRankId = minRequiredRankId;
   }

   protected ReputationRequirement() {
   }

   @Override
   public boolean canFulfillRequirement(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef) {
      ReputationPlugin reputationModule = ReputationPlugin.get();
      int playerReputationValue = reputationModule.getReputationValue(store, ref, this.reputationGroupId);
      if (playerReputationValue == Integer.MIN_VALUE) {
         return false;
      } else {
         ReputationRank minReputationRank = ReputationRank.getAssetMap().getAsset(this.minRequiredRankId);
         return minReputationRank == null ? false : playerReputationValue >= minReputationRank.getMinValue();
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "ReputationRequirement{reputationGroupId='"
         + this.reputationGroupId
         + "', minRequiredRankId='"
         + this.minRequiredRankId
         + "'} "
         + super.toString();
   }
}
