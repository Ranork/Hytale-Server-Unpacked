package com.hypixel.hytale.server.npc.corecomponents.combat.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.Feature;
import com.hypixel.hytale.server.npc.asset.builder.holder.AssetHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.asset.RootInteractionValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.corecomponents.combat.ActionEntityHitInteraction;
import java.util.EnumSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderActionEntityHitInteraction extends BuilderActionBase {
   protected final AssetHolder interaction = new AssetHolder();

   @Nonnull
   public ActionEntityHitInteraction build(@Nonnull BuilderSupport builderSupport) {
      return new ActionEntityHitInteraction(this, builderSupport);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Run an interaction against each entity collided with by the preceding charge.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Iterate the EntityCollisionProvider supplied by a SensorChargeEntityCollisions and queue the configured root interaction (as InteractionType.Collision) once per hit entity.";
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderActionEntityHitInteraction readConfig(@Nonnull JsonElement data) {
      this.requireAsset(
         data,
         "Interaction",
         this.interaction,
         RootInteractionValidator.required(),
         BuilderDescriptorState.Stable,
         "The root interaction to run on each hit entity",
         null
      );
      this.requireFeature(EnumSet.of(Feature.EntityHits));
      return this;
   }

   @Nullable
   public String getInteraction(@Nonnull BuilderSupport support) {
      String val = this.interaction.get(support.getExecutionContext());
      return val != null && !val.isEmpty() ? val : null;
   }
}
