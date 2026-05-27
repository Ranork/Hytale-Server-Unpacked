package com.hypixel.hytale.server.npc.corecomponents.timer;

import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.timer.builders.BuilderBodyMotionTimer;
import com.hypixel.hytale.server.npc.instructions.BodyMotion;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BodyMotionTimer extends MotionTimer<BodyMotion> implements BodyMotion {
   public BodyMotionTimer(@Nonnull BuilderBodyMotionTimer builder, @Nonnull BuilderSupport builderSupport, BodyMotion motion) {
      super(builder, builderSupport, motion);
   }

   @Override
   public BodyMotion getSteeringMotion() {
      return this.motion.getSteeringMotion();
   }

   @Nullable
   @Override
   public <T extends BodyMotion> T findWrappedBodyMotion(@Nonnull Class<T> clazz) {
      T obj = BodyMotion.super.findWrappedBodyMotion(clazz);
      if (obj != null) {
         return obj;
      } else {
         return clazz.isInstance(this.motion) ? clazz.cast(this.motion) : null;
      }
   }
}
