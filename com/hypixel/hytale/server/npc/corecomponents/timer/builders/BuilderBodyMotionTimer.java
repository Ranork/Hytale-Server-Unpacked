package com.hypixel.hytale.server.npc.corecomponents.timer.builders;

import com.hypixel.hytale.server.npc.asset.builder.BuilderObjectReferenceHelper;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.timer.BodyMotionTimer;
import com.hypixel.hytale.server.npc.instructions.BodyMotion;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderBodyMotionTimer extends BuilderMotionTimer<BodyMotion> {
   public BuilderBodyMotionTimer() {
      this.motion = new BuilderObjectReferenceHelper<>(BodyMotion.class, this);
   }

   @Nullable
   public BodyMotionTimer build(@Nonnull BuilderSupport builderSupport) {
      BodyMotion motion = this.getMotion(builderSupport);
      return motion == null ? null : new BodyMotionTimer(this, builderSupport, motion);
   }

   @Nonnull
   @Override
   public final Class<BodyMotion> category() {
      return BodyMotion.class;
   }
}
