package com.hypixel.hytale.server.npc.corecomponents.utility;

import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.utility.builders.BuilderBodyMotionSequence;
import com.hypixel.hytale.server.npc.instructions.BodyMotion;
import com.hypixel.hytale.server.npc.util.IAnnotatedComponentCollection;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BodyMotionSequence extends MotionSequence<BodyMotion> implements BodyMotion, IAnnotatedComponentCollection {
   public BodyMotionSequence(@Nonnull BuilderBodyMotionSequence builder, @Nonnull BuilderSupport support) {
      super(builder, builder.getSteps(support));
   }

   @Nullable
   @Override
   public BodyMotion getSteeringMotion() {
      return this.activeMotion == null ? null : this.activeMotion.getSteeringMotion();
   }

   @Nullable
   @Override
   public <T extends BodyMotion> T findWrappedBodyMotion(@Nonnull Class<T> clazz) {
      T obj = BodyMotion.super.findWrappedBodyMotion(clazz);
      if (obj != null) {
         return obj;
      } else {
         for (BodyMotion step : this.steps) {
            if (clazz.isInstance(step)) {
               return clazz.cast(step);
            }

            T wrapped = step.findWrappedBodyMotion(clazz);
            if (wrapped != null) {
               return wrapped;
            }
         }

         return null;
      }
   }
}
