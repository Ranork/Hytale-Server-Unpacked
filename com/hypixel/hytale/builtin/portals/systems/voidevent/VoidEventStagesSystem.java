package com.hypixel.hytale.builtin.portals.systems.voidevent;

import com.hypixel.hytale.builtin.portals.components.voidevent.VoidEvent;
import com.hypixel.hytale.builtin.portals.components.voidevent.config.VoidEventConfig;
import com.hypixel.hytale.builtin.portals.components.voidevent.config.VoidEventStage;
import com.hypixel.hytale.builtin.portals.resources.PortalWorld;
import com.hypixel.hytale.builtin.weather.resources.WeatherResource;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.DelayedEntitySystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class VoidEventStagesSystem extends DelayedEntitySystem<EntityStore> {
   public VoidEventStagesSystem() {
      super(1.5F);
   }

   @Override
   public void tick(
      float dt,
      int index,
      @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      PortalWorld portalWorld = store.getResource(PortalWorld.getResourceType());
      if (portalWorld.exists()) {
         Ref<EntityStore> eventRef = portalWorld.getVoidEventRef();
         VoidEvent voidEvent = eventRef == null ? null : commandBuffer.getComponent(eventRef, VoidEvent.getComponentType());
         if (voidEvent != null) {
            World world = store.getExternalData().getWorld();
            VoidEventConfig voidEventConfig = portalWorld.getVoidEventConfig();
            if (voidEventConfig != null) {
               double elapsedSecondsInPortal = portalWorld.getElapsedSeconds(world);
               int timeLimitSeconds = portalWorld.getTimeLimitSeconds();
               int shouldStartAfter = voidEventConfig.getShouldStartAfterSeconds(timeLimitSeconds);
               int elapsedSecondsInEvent = (int)Math.max(0.0, elapsedSecondsInPortal - shouldStartAfter);
               VoidEventStage currentStage = voidEvent.getActiveStage();
               VoidEventStage desiredStage = computeAppropriateStage(voidEventConfig, elapsedSecondsInEvent);
               if (currentStage != desiredStage) {
                  if (currentStage != null) {
                     stopStage(currentStage, store, commandBuffer);
                  }

                  if (desiredStage != null) {
                     startStage(desiredStage, store, commandBuffer);
                  }

                  voidEvent.setActiveStage(desiredStage);
               }
            }
         }
      }
   }

   @Nullable
   private static VoidEventStage computeAppropriateStage(@Nonnull VoidEventConfig config, int elapsedSeconds) {
      List<VoidEventStage> stages = config.getStagesSortedByStartTime();

      for (int i = stages.size() - 1; i >= 0; i--) {
         VoidEventStage stage = stages.get(i);
         if (elapsedSeconds > stage.getSecondsInto()) {
            return stage;
         }
      }

      return null;
   }

   public static void startStage(@Nonnull VoidEventStage stage, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
      HytaleLogger.getLogger().at(Level.INFO).log("Starting stage SecondsInto=" + stage.getSecondsInto() + " in portal void event");
      String forcedWeatherId = stage.getForcedWeatherId();
      if (forcedWeatherId != null) {
         WeatherResource weatherResource = store.getResource(WeatherResource.getResourceType());
         weatherResource.setForcedWeather(forcedWeatherId);
      }
   }

   public static void stopStage(@Nonnull VoidEventStage stage, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
      HytaleLogger.getLogger().at(Level.INFO).log("Stopping stage SecondsInto=" + stage.getSecondsInto() + " in portal void event");
      String forcedWeatherId = stage.getForcedWeatherId();
      if (forcedWeatherId != null) {
         WeatherResource weatherResource = store.getResource(WeatherResource.getResourceType());
         weatherResource.setForcedWeather(null);
      }
   }

   @Nullable
   @Override
   public Query<EntityStore> getQuery() {
      return VoidEvent.getComponentType();
   }
}
