package com.hypixel.hytale.server.npc.corecomponents.utility.builders;

import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderHeadMotionBase;
import com.hypixel.hytale.server.npc.corecomponents.utility.HeadMotionNothing;
import javax.annotation.Nonnull;

public class BuilderHeadMotionNothing extends BuilderHeadMotionBase {
   @Nonnull
   public HeadMotionNothing build(BuilderSupport builderSupport) {
      return new HeadMotionNothing(this);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Do nothing";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Do nothing";
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }
}
