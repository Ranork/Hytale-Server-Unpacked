package com.hypixel.hytale.server.npc.corecomponents.movement.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.DoubleHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSingleValidator;
import com.hypixel.hytale.server.npc.corecomponents.movement.BodyMotionLeave;
import javax.annotation.Nonnull;

public class BuilderBodyMotionLeave extends BuilderBodyMotionFindBase {
   protected final DoubleHolder distance = new DoubleHolder();

   public BuilderBodyMotionLeave() {
      super(false);
   }

   @Nonnull
   public BodyMotionLeave build(@Nonnull BuilderSupport builderSupport) {
      return new BodyMotionLeave(this, builderSupport);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Leave place";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Get away from current position using path finding";
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Experimental;
   }

   @Nonnull
   @Override
   public BuilderBodyMotionFindBase readConfig(@Nonnull JsonElement data) {
      super.readConfig(data);
      this.requireDouble(
         data, "Distance", this.distance, DoubleSingleValidator.greater0(), BuilderDescriptorState.Experimental, "Minimum distance required", null
      );
      return this;
   }

   public double getDistance(@Nonnull BuilderSupport support) {
      return this.distance.get(support.getExecutionContext());
   }
}
