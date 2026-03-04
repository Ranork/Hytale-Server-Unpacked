package com.hypixel.hytale.server.npc.corecomponents;

import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderBodyMotionBase;
import com.hypixel.hytale.server.npc.instructions.BodyMotion;
import javax.annotation.Nonnull;

public abstract class BodyMotionBase extends MotionBase implements BodyMotion {
   public BodyMotionBase(@Nonnull BuilderBodyMotionBase builderMotionBase) {
   }
}
