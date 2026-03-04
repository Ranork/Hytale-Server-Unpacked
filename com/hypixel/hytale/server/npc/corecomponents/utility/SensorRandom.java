package com.hypixel.hytale.server.npc.corecomponents.utility;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.random.RandomExtra;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.corecomponents.utility.builders.BuilderSensorRandom;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;

public class SensorRandom extends SensorBase {
   protected final double minFalseDuration;
   protected final double maxFalseDuration;
   protected final double minTrueDuration;
   protected final double maxTrueDuration;
   protected double remainingDuration;
   protected boolean state;

   public SensorRandom(@Nonnull BuilderSensorRandom builder, @Nonnull BuilderSupport support) {
      super(builder);
      double[] falseDuration = builder.getFalseRange(support);
      this.minFalseDuration = falseDuration[0];
      this.maxFalseDuration = falseDuration[1];
      double[] trueDuration = builder.getTrueRange(support);
      this.minTrueDuration = trueDuration[0];
      this.maxTrueDuration = trueDuration[1];
      this.state = RandomExtra.randomBoolean();
      this.remainingDuration = this.pickNextDuration();
   }

   @Override
   public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {
      if (!super.matches(ref, role, dt, store)) {
         return false;
      } else {
         if ((this.remainingDuration -= dt) <= 0.0) {
            this.state = !this.state;
            this.remainingDuration = this.pickNextDuration();
         }

         return this.state;
      }
   }

   @Override
   public InfoProvider getSensorInfo() {
      return null;
   }

   protected double pickNextDuration() {
      return this.state
         ? RandomExtra.randomRange(this.minTrueDuration, this.maxTrueDuration)
         : RandomExtra.randomRange(this.minFalseDuration, this.maxFalseDuration);
   }
}
