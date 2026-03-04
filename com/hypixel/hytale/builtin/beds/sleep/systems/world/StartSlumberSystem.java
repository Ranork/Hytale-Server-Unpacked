package com.hypixel.hytale.builtin.beds.sleep.systems.world;

import com.hypixel.hytale.builtin.beds.sleep.components.PlayerSleep;
import com.hypixel.hytale.builtin.beds.sleep.components.PlayerSomnolence;
import com.hypixel.hytale.builtin.beds.sleep.resources.WorldSleep;
import com.hypixel.hytale.builtin.beds.sleep.resources.WorldSlumber;
import com.hypixel.hytale.builtin.beds.sleep.resources.WorldSomnolence;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.DelayedSystem;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.gameplay.sleep.SleepConfig;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StartSlumberSystem extends DelayedSystem<EntityStore> {
   @Nonnull
   private static final Duration NODDING_OFF_DURATION = Duration.ofMillis(3200L);
   @Nonnull
   private static final Duration WAKE_UP_AUTOSLEEP_DELAY = Duration.ofHours(1L);
   private static final float SYSTEM_INTERVAL_S = 0.3F;
   @Nonnull
   private final ComponentType<EntityStore, PlayerSomnolence> playerSomnolenceComponentType;
   @Nonnull
   private final ResourceType<EntityStore, WorldSomnolence> worldSomnolenceResourceType;
   @Nonnull
   private final ResourceType<EntityStore, WorldTimeResource> worldTimeResourceType;

   public StartSlumberSystem(
      @Nonnull ComponentType<EntityStore, PlayerSomnolence> playerSomnolenceComponentType,
      @Nonnull ResourceType<EntityStore, WorldSomnolence> worldSomnolenceResourceType,
      @Nonnull ResourceType<EntityStore, WorldTimeResource> worldTimeResourceType
   ) {
      super(0.3F);
      this.playerSomnolenceComponentType = playerSomnolenceComponentType;
      this.worldSomnolenceResourceType = worldSomnolenceResourceType;
      this.worldTimeResourceType = worldTimeResourceType;
   }

   @Override
   public void delayedTick(float dt, int systemIndex, @Nonnull Store<EntityStore> store) {
      this.checkIfEveryoneIsReadyToSleep(store);
   }

   private void checkIfEveryoneIsReadyToSleep(@Nonnull Store<EntityStore> store) {
      World world = store.getExternalData().getWorld();
      Collection<PlayerRef> playerRefs = world.getPlayerRefs();
      if (!playerRefs.isEmpty()) {
         if (!CanSleepInWorld.check(world).isNegative()) {
            SleepConfig sleepConfig = world.getGameplayConfig().getWorldConfig().getSleepConfig();
            float wakeUpHour = sleepConfig.getWakeUpHour();
            WorldSomnolence worldSomnolenceResource = store.getResource(this.worldSomnolenceResourceType);
            WorldSleep worldState = worldSomnolenceResource.getState();
            if (worldState == WorldSleep.Awake.INSTANCE) {
               if (this.isEveryoneReadyToSleep(store)) {
                  WorldTimeResource timeResource = store.getResource(this.worldTimeResourceType);
                  Instant now = timeResource.getGameTime();
                  Instant target = this.computeWakeupInstant(now, wakeUpHour);
                  float irlSeconds = computeIrlSeconds(now, target);
                  worldSomnolenceResource.setState(new WorldSlumber(now, target, irlSeconds));
                  store.forEachEntityParallel(this.playerSomnolenceComponentType, (index, archetypeChunk, commandBuffer) -> {
                     Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
                     commandBuffer.putComponent(ref, this.playerSomnolenceComponentType, PlayerSleep.Slumber.createComponent(timeResource));
                     PlayerRef playerRef = archetypeChunk.getComponent(index, PlayerRef.getComponentType());
                     if (playerRef != null) {
                        SoundUtil.playSoundEvent2dToPlayer(playerRef, sleepConfig.getSounds().getSuccessIndex(), SoundCategory.UI);
                     }
                  });
                  worldSomnolenceResource.resetNotificationCooldown();
               }
            }
         }
      }
   }

   private Instant computeWakeupInstant(@Nonnull Instant now, float wakeUpHour) {
      LocalDateTime ldt = LocalDateTime.ofInstant(now, ZoneOffset.UTC);
      int hours = (int)wakeUpHour;
      float fractionalHour = wakeUpHour - hours;
      LocalDateTime wakeUpTime = ldt.toLocalDate().atTime(hours, (int)(fractionalHour * 60.0F));
      if (!ldt.isBefore(wakeUpTime)) {
         wakeUpTime = wakeUpTime.plusDays(1L);
      }

      return wakeUpTime.toInstant(ZoneOffset.UTC);
   }

   private static float computeIrlSeconds(@Nonnull Instant startInstant, @Nonnull Instant targetInstant) {
      long ms = Duration.between(startInstant, targetInstant).toMillis();
      long hours = TimeUnit.MILLISECONDS.toHours(ms);
      double seconds = Math.max(3.0, hours / 6.0);
      return (float)Math.ceil(seconds);
   }

   private boolean isEveryoneReadyToSleep(@Nonnull ComponentAccessor<EntityStore> store) {
      World world = store.getExternalData().getWorld();
      Collection<PlayerRef> playerRefs = world.getPlayerRefs();
      if (playerRefs.isEmpty()) {
         return false;
      } else {
         for (PlayerRef playerRef : playerRefs) {
            Ref<EntityStore> ref = playerRef.getReference();
            if (ref != null && ref.isValid() && !isReadyToSleep(store, ref)) {
               return false;
            }
         }

         return true;
      }
   }

   public static boolean isReadyToSleep(@Nonnull ComponentAccessor<EntityStore> store, @Nullable Ref<EntityStore> ref) {
      if (ref != null && ref.isValid()) {
         PlayerSomnolence somnolenceComponent = store.getComponent(ref, PlayerSomnolence.getComponentType());
         if (somnolenceComponent == null) {
            return false;
         } else {
            PlayerSleep sleepState = somnolenceComponent.getSleepState();

            return switch (sleepState) {
               case PlayerSleep.FullyAwake fullAwake -> false;
               case PlayerSleep.MorningWakeUp morningWakeUp -> {
                  WorldTimeResource worldTimeResource = store.getResource(WorldTimeResource.getResourceType());
                  yield morningWakeUp.isReadyToSleepAgain(worldTimeResource.getGameTime());
               }
               case PlayerSleep.NoddingOff noddingOff -> {
                  Instant sleepStart = noddingOff.realTimeStart().plus(NODDING_OFF_DURATION);
                  yield Instant.now().isAfter(sleepStart);
               }
               case PlayerSleep.Slumber ignored -> true;
               default -> throw new MatchException(null, null);
            };
         }
      } else {
         return true;
      }
   }

   public static boolean canNotifyOthersAboutTryingToSleep(@Nonnull ComponentAccessor<EntityStore> store, @Nullable Ref<EntityStore> ref) {
      if (ref != null && ref.isValid()) {
         PlayerSomnolence somnolenceComponent = store.getComponent(ref, PlayerSomnolence.getComponentType());
         if (somnolenceComponent == null) {
            return false;
         } else {
            PlayerSleep sleepState = somnolenceComponent.getSleepState();

            return switch (sleepState) {
               case PlayerSleep.FullyAwake fullAwake -> false;
               case PlayerSleep.MorningWakeUp morningWakeUp -> {
                  WorldTimeResource worldTimeResource = store.getResource(WorldTimeResource.getResourceType());
                  yield morningWakeUp.isReadyToSleepAgain(worldTimeResource.getGameTime());
               }
               case PlayerSleep.NoddingOff noddingOff -> true;
               case PlayerSleep.Slumber ignored -> true;
               default -> throw new MatchException(null, null);
            };
         }
      } else {
         return true;
      }
   }
}
