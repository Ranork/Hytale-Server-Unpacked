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
}
