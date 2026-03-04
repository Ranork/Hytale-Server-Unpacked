package com.hypixel.hytale.server.npc.corecomponents.timer;

import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.timer.builders.BuilderHeadMotionTimer;
import com.hypixel.hytale.server.npc.instructions.HeadMotion;
import javax.annotation.Nonnull;

public class HeadMotionTimer extends MotionTimer<HeadMotion> implements HeadMotion {
   public HeadMotionTimer(@Nonnull BuilderHeadMotionTimer builder, @Nonnull BuilderSupport builderSupport, HeadMotion motion) {
      super(builder, builderSupport, motion);
   }
}
