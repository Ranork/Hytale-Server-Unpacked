package com.hypixel.hytale.builtin.adventure.objectiveshop;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.builtin.adventure.objectives.config.ObjectiveAsset;
import com.hypixel.hytale.builtin.adventure.shop.ShopAsset;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceInteraction;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceRequirement;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import javax.annotation.Nonnull;

public class ObjectiveShopPlugin extends JavaPlugin {
   protected static ObjectiveShopPlugin instance;

   public static ObjectiveShopPlugin get() {
      return instance;
   }

   public ObjectiveShopPlugin(@Nonnull JavaPluginInit init) {
      super(init);
   }

   @Override
   protected void setup() {
      instance = this;
      ChoiceInteraction.CODEC.register("StartObjective", StartObjectiveInteraction.class, StartObjectiveInteraction.CODEC);
      ChoiceRequirement.CODEC.register("CanStartObjective", CanStartObjectiveRequirement.class, CanStartObjectiveRequirement.CODEC);
      AssetRegistry.getAssetStore(ShopAsset.class).injectLoadsAfter(ObjectiveAsset.class);
   }
}
