package com.hypixel.hytale.server.npc.corecomponents;

import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderBodyMotionBase;
import com.hypixel.hytale.server.npc.instructions.BodyMotion;
import com.hypixel.hytale.server.npc.movement.constraints.RelaxedConstraint;
import com.hypixel.hytale.server.npc.movement.controllers.ProbeMoveData;
import com.hypixel.hytale.server.npc.role.Role;
import java.util.EnumSet;
import javax.annotation.Nonnull;

public abstract class BodyMotionBase extends MotionBase implements BodyMotion {
   public BodyMotionBase(@Nonnull BuilderBodyMotionBase builderMotionBase) {
   }

   @Nonnull
   protected static EnumSet<RelaxedConstraint> computeEffectiveRelaxedConstraints(
      boolean usesLegacyConstraintMode,
      @Nonnull EnumSet<RelaxedConstraint> relaxedConstraints,
      boolean isLegacyRelaxedMoveConstraints,
      boolean isLegacyAvoidingBlockDamage
   ) {
      if (!usesLegacyConstraintMode) {
         return relaxedConstraints;
      } else {
         EnumSet<RelaxedConstraint> effectiveConstraints = isLegacyRelaxedMoveConstraints
            ? EnumSet.copyOf(RelaxedConstraint.DEFAULT_WHEN_RELAXED)
            : EnumSet.noneOf(RelaxedConstraint.class);
         if (!isLegacyAvoidingBlockDamage) {
            effectiveConstraints.add(RelaxedConstraint.DAMAGE);
         }

         return effectiveConstraints;
      }
   }

   protected static boolean applyEscapeConstraints(@Nonnull Role role, @Nonnull ProbeMoveData probeMoveData) {
      if (!role.couldBreatheCached()) {
         probeMoveData.getRelaxedConstraints().add(RelaxedConstraint.BREATHE);
         probeMoveData.getRelaxedConstraints().add(RelaxedConstraint.WADE);
         return true;
      } else {
         return false;
      }
   }
}
