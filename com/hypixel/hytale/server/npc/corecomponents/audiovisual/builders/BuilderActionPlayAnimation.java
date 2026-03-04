package com.hypixel.hytale.server.npc.corecomponents.audiovisual.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.animations.NPCAnimationSlot;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.StringHolder;
import com.hypixel.hytale.server.npc.corecomponents.audiovisual.ActionPlayAnimation;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.validators.NPCLoadTimeValidationHelper;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderActionPlayAnimation extends BuilderActionBase {
   protected NPCAnimationSlot slot;
   protected final StringHolder animationId = new StringHolder();

   public ActionPlayAnimation build(@Nonnull BuilderSupport builderSupport) {
      return new ActionPlayAnimation(this, builderSupport);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Play an animation";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Play an animation";
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Experimental;
   }

   @Nonnull
   public BuilderActionPlayAnimation readConfig(@Nonnull JsonElement data) {
      this.requireEnum(data, "Slot", e -> this.slot = e, NPCAnimationSlot.class, BuilderDescriptorState.Stable, "The animation slot to play on", null);
      this.getString(data, "Animation", this.animationId, null, null, BuilderDescriptorState.Stable, "The animation ID to play", null);
      return this;
   }

   @Override
   protected void runLoadTimeValidationHelper0(
      String configName, @Nonnull NPCLoadTimeValidationHelper loadTimeValidationHelper, ExecutionContext context, List<String> errors
   ) {
      loadTimeValidationHelper.validateAnimation(this.animationId.get(context));
   }

   public NPCAnimationSlot getSlot() {
      return this.slot;
   }

   @Nullable
   public String getAnimationId(@Nonnull BuilderSupport support) {
      String anim = this.animationId.get(support.getExecutionContext());
      return anim != null && !anim.isEmpty() ? anim : null;
   }
}
