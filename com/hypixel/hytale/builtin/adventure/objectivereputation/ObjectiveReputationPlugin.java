package com.hypixel.hytale.builtin.adventure.objectivereputation;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.builtin.adventure.objectivereputation.assets.ReputationCompletionAsset;
import com.hypixel.hytale.builtin.adventure.objectivereputation.historydata.ReputationObjectiveRewardHistoryData;
import com.hypixel.hytale.builtin.adventure.objectives.ObjectivePlugin;
import com.hypixel.hytale.builtin.adventure.objectives.config.ObjectiveAsset;
import com.hypixel.hytale.builtin.adventure.objectives.historydata.ObjectiveRewardHistoryData;
import com.hypixel.hytale.builtin.adventure.reputation.assets.ReputationGroup;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import javax.annotation.Nonnull;

public class ObjectiveReputationPlugin extends JavaPlugin {
   protected static ObjectiveReputationPlugin instance;

   public static ObjectiveReputationPlugin get() {
      return instance;
   }

   public ObjectiveReputationPlugin(@Nonnull JavaPluginInit init) {
      super(init);
   }

   @Override
   protected void setup() {
      instance = this;
      ObjectiveRewardHistoryData.CODEC.register("Reputation", ReputationObjectiveRewardHistoryData.class, ReputationObjectiveRewardHistoryData.CODEC);
      ObjectivePlugin.get().registerCompletion("Reputation", ReputationCompletionAsset.class, ReputationCompletionAsset.CODEC, ReputationCompletion::new);
      AssetRegistry.getAssetStore(ObjectiveAsset.class).injectLoadsAfter(ReputationGroup.class);
   }
}
