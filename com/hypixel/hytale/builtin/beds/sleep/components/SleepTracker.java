package com.hypixel.hytale.builtin.beds.sleep.components;

import com.hypixel.hytale.builtin.beds.BedsPlugin;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.protocol.packets.world.UpdateSleepState;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SleepTracker implements Component<EntityStore> {
   @Nonnull
   private UpdateSleepState lastSentPacket = new UpdateSleepState(false, false, null, null);

   public static ComponentType<EntityStore, SleepTracker> getComponentType() {
      return BedsPlugin.getInstance().getSleepTrackerComponentType();
   }

   @Nullable
   public UpdateSleepState generatePacketToSend(@Nonnull UpdateSleepState state) {
      if (this.lastSentPacket.equals(state)) {
         return null;
      } else {
         this.lastSentPacket = state;
         return this.lastSentPacket;
      }
   }

   @Nullable
   @Override
   public Component<EntityStore> clone() {
      return new SleepTracker();
   }
}
