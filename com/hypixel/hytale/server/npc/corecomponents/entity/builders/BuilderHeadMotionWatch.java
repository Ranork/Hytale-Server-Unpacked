package com.hypixel.hytale.server.npc.corecomponents.entity.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.Feature;
import com.hypixel.hytale.server.npc.asset.builder.holder.DoubleHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleRangeValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderHeadMotionBase;
import com.hypixel.hytale.server.npc.corecomponents.entity.HeadMotionWatch;
import javax.annotation.Nonnull;

public class BuilderHeadMotionWatch extends BuilderHeadMotionBase {
   protected final DoubleHolder relativeTurnSpeed = new DoubleHolder();

   @Nonnull
   public HeadMotionWatch build(@Nonnull BuilderSupport builderSupport) {
      return new HeadMotionWatch(this, builderSupport);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Rotate to target";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Rotate to target.";
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderHeadMotionWatch readConfig(@Nonnull JsonElement data) {
      this.getDouble(
         data,
         "RelativeTurnSpeed",
         this.relativeTurnSpeed,
         1.0,
         DoubleRangeValidator.fromExclToIncl(0.0, 2.0),
         BuilderDescriptorState.Stable,
         "The relative turn speed modifier",
         null
      );
      this.requireFeature(Feature.AnyPosition);
      return this;
   }

   public double getRelativeTurnSpeed(@Nonnull BuilderSupport support) {
      return this.relativeTurnSpeed.get(support.getExecutionContext());
   }
}
