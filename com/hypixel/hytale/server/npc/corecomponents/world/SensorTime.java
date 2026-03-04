package com.hypixel.hytale.server.npc.corecomponents.world;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.corecomponents.world.builders.BuilderSensorTime;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;

public class SensorTime extends SensorBase {
   protected final double minTime;
   protected final double maxTime;
   protected final boolean checkDay;
   protected final boolean checkYear;
   protected final boolean scaleDayTimeRange;

   public SensorTime(@Nonnull BuilderSensorTime builderSensorTime, @Nonnull BuilderSupport support) {
      super(builderSensorTime);
      double[] timePeriod = builderSensorTime.getPeriod(support);
      this.minTime = timePeriod[0] / WorldTimeResource.HOURS_PER_DAY;
      this.maxTime = timePeriod[1] / WorldTimeResource.HOURS_PER_DAY;
      this.checkDay = builderSensorTime.isCheckDay();
      this.checkYear = builderSensorTime.isCheckYear();
      this.scaleDayTimeRange = builderSensorTime.isScaleDayTimeRange();
   }

   @Override
   public InfoProvider getSensorInfo() {
      return null;
   }

   @Override
   public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {
      if (!super.matches(ref, role, dt, store)) {
         return false;
      } else {
         WorldTimeResource worldTimeResource = store.getResource(WorldTimeResource.getResourceType());
         if (this.checkDay) {
            boolean withinTimeRange = this.scaleDayTimeRange
               ? worldTimeResource.isScaledDayTimeWithinRange(this.minTime, this.maxTime)
               : worldTimeResource.isDayTimeWithinRange(this.minTime, this.maxTime);
            return withinTimeRange && (!this.checkYear || worldTimeResource.isYearWithinRange(this.minTime, this.maxTime));
         } else {
            return this.checkYear ? worldTimeResource.isYearWithinRange(this.minTime, this.maxTime) : false;
         }
      }
   }
}
