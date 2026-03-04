package com.hypixel.hytale.server.npc.corecomponents.timer.builders;

import com.hypixel.hytale.server.npc.asset.builder.BuilderObjectReferenceHelper;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.timer.HeadMotionTimer;
import com.hypixel.hytale.server.npc.instructions.HeadMotion;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderHeadMotionTimer extends BuilderMotionTimer<HeadMotion> {
   public BuilderHeadMotionTimer() {
      this.motion = new BuilderObjectReferenceHelper<>(HeadMotion.class, this);
   }

   @Nullable
   public HeadMotionTimer build(@Nonnull BuilderSupport builderSupport) {
      HeadMotion motion = this.getMotion(builderSupport);
      return motion == null ? null : new HeadMotionTimer(this, builderSupport, motion);
   }

   @Nonnull
   @Override
   public final Class<HeadMotion> category() {
      return HeadMotion.class;
   }
}
