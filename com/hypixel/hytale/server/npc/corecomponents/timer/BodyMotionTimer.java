package com.hypixel.hytale.server.npc.corecomponents.timer;

import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.timer.builders.BuilderBodyMotionTimer;
import com.hypixel.hytale.server.npc.instructions.BodyMotion;
import javax.annotation.Nonnull;

public class BodyMotionTimer extends MotionTimer<BodyMotion> implements BodyMotion {
   public BodyMotionTimer(@Nonnull BuilderBodyMotionTimer builder, @Nonnull BuilderSupport builderSupport, BodyMotion motion) {
      super(builder, builderSupport, motion);
   }

   @Override
   public BodyMotion getSteeringMotion() {
      return this.motion.getSteeringMotion();
   }
}
