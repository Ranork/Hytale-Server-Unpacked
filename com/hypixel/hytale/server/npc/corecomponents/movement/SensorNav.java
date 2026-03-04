package com.hypixel.hytale.server.npc.corecomponents.movement;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.corecomponents.movement.builders.BuilderSensorNav;
import com.hypixel.hytale.server.npc.movement.NavState;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import java.util.EnumSet;
import javax.annotation.Nonnull;

public class SensorNav extends SensorBase {
   protected final EnumSet<NavState> navStates;
   protected final double throttleDuration;
   protected final double targetDeltaSquared;

   public SensorNav(@Nonnull BuilderSensorNav builderSensorNav, @Nonnull BuilderSupport builderSupport) {
      super(builderSensorNav);
      this.navStates = builderSensorNav.getNavStates(builderSupport);
      this.throttleDuration = builderSensorNav.getThrottleDuration(builderSupport);
      double targetDelta = builderSensorNav.getTargetDelta(builderSupport);
      this.targetDeltaSquared = targetDelta * targetDelta;
   }

   @Override
   public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {
      if (!super.matches(ref, role, dt, store)) {
         return false;
      } else {
         MotionController motionController = role.getActiveMotionController();
         return (this.throttleDuration == 0.0 || motionController.getThrottleDuration() >= this.throttleDuration)
            && (this.targetDeltaSquared == 0.0 || motionController.getTargetDeltaSquared() >= this.targetDeltaSquared)
            && (this.navStates.isEmpty() || this.navStates.contains(motionController.getNavState()));
      }
   }

   @Override
   public InfoProvider getSensorInfo() {
      return null;
   }
}
