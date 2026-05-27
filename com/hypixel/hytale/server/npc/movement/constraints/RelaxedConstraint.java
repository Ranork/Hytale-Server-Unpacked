package com.hypixel.hytale.server.npc.movement.constraints;

import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

public enum RelaxedConstraint implements Supplier<String> {
   WADE("Wade"),
   DAMAGE("Damage"),
   DROP("Drop"),
   BREATHE("Breathe"),
   FENCE("Fence");

   private final String displayName;
   @Nonnull
   public static final Set<RelaxedConstraint> DEFAULT_WHEN_RELAXED = Set.of(WADE);

   private RelaxedConstraint(String displayName) {
      this.displayName = displayName;
   }

   public String get() {
      return this.displayName;
   }
}
