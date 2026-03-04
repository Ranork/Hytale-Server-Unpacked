package com.hypixel.hytale.builtin.adventure.objectivereputation;

import com.hypixel.hytale.builtin.adventure.objectivereputation.assets.ReputationCompletionAsset;
import com.hypixel.hytale.builtin.adventure.objectivereputation.historydata.ReputationObjectiveRewardHistoryData;
import com.hypixel.hytale.builtin.adventure.objectives.Objective;
import com.hypixel.hytale.builtin.adventure.objectives.completion.ObjectiveCompletion;
import com.hypixel.hytale.builtin.adventure.reputation.ReputationPlugin;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class ReputationCompletion extends ObjectiveCompletion {
   public ReputationCompletion(@Nonnull ReputationCompletionAsset asset) {
      super(asset);
   }

   @Nonnull
   public ReputationCompletionAsset getAsset() {
      return (ReputationCompletionAsset)super.getAsset();
   }

   @Override
   public void handle(@Nonnull Objective objective, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      ReputationPlugin reputationModule = ReputationPlugin.get();
      objective.forEachParticipant((participantReference, asset, objectiveHistoryData) -> {
         Player playerComponent = componentAccessor.getComponent(participantReference, Player.getComponentType());
         if (playerComponent != null) {
            UUIDComponent uuidComponent = componentAccessor.getComponent(participantReference, UUIDComponent.getComponentType());
            if (uuidComponent == null) {
               return;
            }

            String reputationGroupId = asset.getReputationGroupId();
            int amount = asset.getAmount();
            reputationModule.changeReputation(playerComponent, reputationGroupId, amount, componentAccessor);
            objectiveHistoryData.addRewardForPlayerUUID(uuidComponent.getUuid(), new ReputationObjectiveRewardHistoryData(reputationGroupId, amount));
         }
      }, this.getAsset(), objective.getObjectiveHistoryData());
   }

   @Nonnull
   @Override
   public String toString() {
      return "ReputationCompletion{} " + super.toString();
   }
}
