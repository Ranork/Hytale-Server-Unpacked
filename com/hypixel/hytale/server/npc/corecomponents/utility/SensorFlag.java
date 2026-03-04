package com.hypixel.hytale.server.npc.corecomponents.utility;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.corecomponents.utility.builders.BuilderSensorFlag;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;

public class SensorFlag extends SensorBase {
   protected final int flagIndex;
   protected final boolean value;

   public SensorFlag(@Nonnull BuilderSensorFlag builder, @Nonnull BuilderSupport support) {
      super(builder);
      this.flagIndex = builder.getFlagSlot(support);
      this.value = builder.getValue(support);
   }

   @Override
   public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {
      return super.matches(ref, role, dt, store) && role.isFlagSet(this.flagIndex) == this.value;
   }

   @Override
   public InfoProvider getSensorInfo() {
      return null;
   }
}
