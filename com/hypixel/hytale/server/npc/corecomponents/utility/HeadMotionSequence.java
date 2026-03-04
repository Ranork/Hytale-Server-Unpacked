package com.hypixel.hytale.server.npc.corecomponents.utility;

import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.utility.builders.BuilderHeadMotionSequence;
import com.hypixel.hytale.server.npc.instructions.HeadMotion;
import com.hypixel.hytale.server.npc.util.IAnnotatedComponentCollection;
import javax.annotation.Nonnull;

public class HeadMotionSequence extends MotionSequence<HeadMotion> implements HeadMotion, IAnnotatedComponentCollection {
   public HeadMotionSequence(@Nonnull BuilderHeadMotionSequence builder, @Nonnull BuilderSupport support) {
      super(builder, builder.getSteps(support));
   }
}
