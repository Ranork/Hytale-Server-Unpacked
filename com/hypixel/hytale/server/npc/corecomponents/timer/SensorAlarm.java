package com.hypixel.hytale.server.npc.corecomponents.timer;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.corecomponents.timer.builders.BuilderSensorAlarm;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.util.Alarm;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

public class SensorAlarm extends SensorBase {
   protected final Alarm alarm;
   protected final SensorAlarm.State state;
   protected final boolean clear;

   public SensorAlarm(@Nonnull BuilderSensorAlarm builder, @Nonnull BuilderSupport support) {
      super(builder);
      this.alarm = builder.getAlarm(support);
      this.state = builder.getState(support);
      this.clear = builder.isClear(support);
   }

   @Override
   public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {
      if (!super.matches(ref, role, dt, store)) {
         return false;
      } else {
         switch (this.state) {
            case SET:
               WorldTimeResource worldTimeResource = store.getResource(WorldTimeResource.getResourceType());
               return this.alarm.isSet() && !this.alarm.hasPassed(worldTimeResource.getGameTime());
            case UNSET:
               return !this.alarm.isSet();
            case PASSED:
               WorldTimeResource worldTimeResource = store.getResource(WorldTimeResource.getResourceType());
               boolean passed = this.alarm.hasPassed(worldTimeResource.getGameTime());
               if (passed && this.clear) {
                  this.alarm.set(ref, null, store);
               }

               return passed;
            default:
               return false;
         }
      }
   }

   @Override
   public InfoProvider getSensorInfo() {
      return null;
   }

   public static enum State implements Supplier<String> {
      SET("Set"),
      UNSET("Not set"),
      PASSED("Passed");

      private final String asText;

      private State(String text) {
         this.asText = text;
      }

      public String asText() {
         return this.asText;
      }

      public String get() {
         return this.asText;
      }
   }
}
