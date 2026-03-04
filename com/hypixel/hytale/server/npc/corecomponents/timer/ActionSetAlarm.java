package com.hypixel.hytale.server.npc.corecomponents.timer;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.random.RandomExtra;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.corecomponents.timer.builders.BuilderActionSetAlarm;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.util.Alarm;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import javax.annotation.Nonnull;

public class ActionSetAlarm extends ActionBase {
   protected final Alarm alarm;
   protected final TemporalAmount minDuration;
   protected final long randomVariation;
   protected final boolean cancel;

   public ActionSetAlarm(@Nonnull BuilderActionSetAlarm builder, @Nonnull BuilderSupport support) {
      super(builder);
      this.alarm = builder.getAlarm(support);
      TemporalAmount[] durations = builder.getDurationRange(support);
      this.minDuration = durations[0];
      Instant max = Instant.EPOCH.plus(durations[1]);
      this.randomVariation = max.minus(this.minDuration).getEpochSecond();
      this.cancel = max.getEpochSecond() == 0L;
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(ref, role, sensorInfo, dt, store);
      if (this.cancel) {
         this.alarm.set(ref, null, store);
      } else {
         WorldTimeResource worldTimeResource = store.getResource(WorldTimeResource.getResourceType());
         this.alarm
            .set(ref, worldTimeResource.getGameTime().plus(this.minDuration).plus(RandomExtra.randomRange(0L, this.randomVariation), ChronoUnit.SECONDS), store);
      }

      return true;
   }
}
