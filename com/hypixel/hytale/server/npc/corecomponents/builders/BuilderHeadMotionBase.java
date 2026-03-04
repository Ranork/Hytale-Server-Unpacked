package com.hypixel.hytale.server.npc.corecomponents.builders;

import com.hypixel.hytale.server.npc.instructions.HeadMotion;
import javax.annotation.Nonnull;

public abstract class BuilderHeadMotionBase extends BuilderMotionBase<HeadMotion> {
   @Nonnull
   @Override
   public final Class<HeadMotion> category() {
      return HeadMotion.class;
   }
}
