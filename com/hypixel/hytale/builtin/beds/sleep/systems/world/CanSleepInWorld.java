package com.hypixel.hytale.builtin.beds.sleep.systems.world;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.gameplay.sleep.SleepConfig;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.LocalDateTime;
import javax.annotation.Nonnull;

public final class CanSleepInWorld {
   @Nonnull
   public static CanSleepInWorld.Result check(@Nonnull World world) {
      if (world.getWorldConfig().isGameTimePaused()) {
         return CanSleepInWorld.Status.GAME_TIME_PAUSED;
      } else {
         Store<EntityStore> store = world.getEntityStore().getStore();
         LocalDateTime worldTime = store.getResource(WorldTimeResource.getResourceType()).getGameDateTime();
         SleepConfig sleepConfig = world.getGameplayConfig().getWorldConfig().getSleepConfig();
         return (CanSleepInWorld.Result)(!sleepConfig.isWithinSleepHoursRange(worldTime)
            ? new CanSleepInWorld.NotDuringSleepHoursRange(worldTime, sleepConfig)
            : CanSleepInWorld.Status.CAN_SLEEP);
      }
   }

   public record NotDuringSleepHoursRange(LocalDateTime worldTime, SleepConfig sleepConfig) implements CanSleepInWorld.Result {
      @Override
      public boolean isNegative() {
         return true;
      }
   }

   public sealed interface Result permits CanSleepInWorld.NotDuringSleepHoursRange, CanSleepInWorld.Status {
      boolean isNegative();
   }

   public static enum Status implements CanSleepInWorld.Result {
      CAN_SLEEP,
      GAME_TIME_PAUSED;

      @Override
      public boolean isNegative() {
         return this != CAN_SLEEP;
      }
   }
}
