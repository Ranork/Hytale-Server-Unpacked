package com.hypixel.hytale.server.npc.corecomponents.combat.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.Feature;
import com.hypixel.hytale.server.npc.asset.builder.holder.AssetHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.asset.RootInteractionValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.corecomponents.combat.ActionBlockHitInteraction;
import java.util.EnumSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderActionBlockHitInteraction extends BuilderActionBase {
   protected final AssetHolder interaction = new AssetHolder();

   @Nonnull
   public ActionBlockHitInteraction build(@Nonnull BuilderSupport builderSupport) {
      return new ActionBlockHitInteraction(this, builderSupport);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Run an interaction against each block collided with by the preceding charge.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Iterate the BlockCollisionProvider supplied by a SensorChargeBlockCollisions and queue the configured root interaction (as InteractionType.Collision) once per hit block position.";
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderActionBlockHitInteraction readConfig(@Nonnull JsonElement data) {
      this.requireAsset(
         data,
         "Interaction",
         this.interaction,
         RootInteractionValidator.required(),
         BuilderDescriptorState.Stable,
         "The root interaction to run on each hit block",
         null
      );
      this.requireFeature(EnumSet.of(Feature.BlockHits));
      return this;
   }

   @Nullable
   public String getInteraction(@Nonnull BuilderSupport support) {
      String val = this.interaction.get(support.getExecutionContext());
      return val != null && !val.isEmpty() ? val : null;
   }
}
