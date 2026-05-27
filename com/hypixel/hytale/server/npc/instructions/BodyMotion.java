package com.hypixel.hytale.server.npc.instructions;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.movement.constraints.RelaxedConstraint;
import java.util.EnumSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface BodyMotion extends Motion {
   @Nullable
   default BodyMotion getSteeringMotion() {
      return this;
   }

   default double getDesiredTargetDistance() {
      return Double.MAX_VALUE;
   }

   @Nullable
   default Ref<EntityStore> getDesiredTargetEntity() {
      return null;
   }

   @Nullable
   default <T extends BodyMotion> T findWrappedBodyMotion(@Nonnull Class<T> clazz) {
      return clazz.isInstance(this) ? clazz.cast(this) : null;
   }

   @Nullable
   default EnumSet<RelaxedConstraint> getRelaxedConstraints() {
      return null;
   }
}
