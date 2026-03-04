package com.hypixel.hytale.server.npc.corecomponents.utility.builders;

import com.hypixel.hytale.server.npc.asset.builder.BuilderObjectListHelper;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.utility.BodyMotionSequence;
import com.hypixel.hytale.server.npc.instructions.BodyMotion;
import java.util.List;
import javax.annotation.Nonnull;

public class BuilderBodyMotionSequence extends BuilderMotionSequence<BodyMotion> {
   public static final BodyMotion[] EMPTY_SEQUENCE = new BodyMotion[0];

   public BuilderBodyMotionSequence() {
      this.steps = new BuilderObjectListHelper<>(BodyMotion.class, this);
   }

   @Nonnull
   public BodyMotionSequence build(@Nonnull BuilderSupport builderSupport) {
      return new BodyMotionSequence(this, builderSupport);
   }

   @Nonnull
   @Override
   public final Class<BodyMotion> category() {
      return BodyMotion.class;
   }

   @Nonnull
   public BodyMotion[] getSteps(@Nonnull BuilderSupport builderSupport) {
      List<BodyMotion> motions = this.steps.build(builderSupport);
      return motions == null ? EMPTY_SEQUENCE : motions.toArray(BodyMotion[]::new);
   }
}
