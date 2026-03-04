package com.hypixel.hytale.builtin.beds.sleep.systems.player;

import com.hypixel.hytale.builtin.beds.sleep.components.PlayerSomnolence;
import com.hypixel.hytale.builtin.beds.sleep.resources.WorldSleep;
import com.hypixel.hytale.builtin.beds.sleep.resources.WorldSlumber;
import com.hypixel.hytale.builtin.beds.sleep.resources.WorldSomnolence;
import com.hypixel.hytale.builtin.beds.sleep.systems.world.CanSleepInWorld;
import com.hypixel.hytale.builtin.beds.sleep.systems.world.StartSlumberSystem;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.DelayedSystem;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.gameplay.sleep.SleepSoundsConfig;
import com.hypixel.hytale.server.core.modules.entity.component.DisplayNameComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class SleepNotificationSystem extends DelayedSystem<EntityStore> {
   public static final int SMALL_SERVER_PLAYER_COUNT = 4;
   public static final double BIG_SERVER_SLEEPERS_RATIO = 0.5;
   public static final String COLOR = "#5AB5B5";

   public SleepNotificationSystem() {
      super(1.0F);
   }

   @Override
   public void delayedTick(float dt, int systemIndex, @NonNullDecl Store<EntityStore> store) {
      World world = store.getExternalData().getWorld();
      SleepSoundsConfig sleepSounds = world.getGameplayConfig().getWorldConfig().getSleepConfig().getSounds();
      if (sleepSounds.isNotificationLoopEnabled()) {
         maybeDoNotification(store, true);
      }
   }

   public static void maybeDoNotification(Store<EntityStore> store, boolean fromAutoLoop) {
      World world = store.getExternalData().getWorld();
      SleepSoundsConfig sleepSounds = world.getGameplayConfig().getWorldConfig().getSleepConfig().getSounds();
      SleepNotificationSystem.NotificationState state = getNotificationState(world);
      if (state != SleepNotificationSystem.NotReady.INSTANCE) {
         long now = System.currentTimeMillis();
         WorldSomnolence worldSomnolence = store.getResource(WorldSomnolence.getResourceType());
         if (worldSomnolence.useSleepNotificationCooldown(now, sleepSounds.getNotificationLoopCooldownMs())) {
            int soundIndex = fromAutoLoop ? sleepSounds.getNotificationLoopIndex() : sleepSounds.getNotificationIndex();
            Collection<PlayerRef> playerRefs = world.getPlayerRefs();

            for (PlayerRef playerRef : playerRefs) {
               Ref<EntityStore> ref = playerRef.getReference();
               if (ref != null && !StartSlumberSystem.canNotifyOthersAboutTryingToSleep(store, ref)) {
                  SoundUtil.playSoundEvent2dToPlayer(playerRef, soundIndex, SoundCategory.UI);
                  if (state instanceof SleepNotificationSystem.SmallServer) {
                     if (state.readyToSleep() == 1) {
                        Message sleeperName = playerRefs.stream()
                           .map(PlayerRef::getReference)
                           .filter(r -> StartSlumberSystem.canNotifyOthersAboutTryingToSleep(store, (Ref<EntityStore>)r))
                           .map(SleepNotificationSystem::getSleeperName)
                           .findFirst()
                           .orElseGet(() -> Message.raw("???"));
                        String msgKey = fromAutoLoop ? "server.interactions.sleep.notificationSingle" : "server.interactions.sleep.notificationSingleEnter";
                        playerRef.sendMessage(Message.translation(msgKey).color("#5AB5B5").param("player", sleeperName));
                     } else {
                        playerRef.sendMessage(
                           Message.translation("server.interactions.sleep.notificationSmall").color("#5AB5B5").param("sleepers", state.readyToSleep())
                        );
                     }
                  } else if (state instanceof SleepNotificationSystem.BigServer) {
                     playerRef.sendMessage(
                        Message.translation("server.interactions.sleep.notificationBig")
                           .color("#5AB5B5")
                           .param("sleepers", state.readyToSleep())
                           .param("total", state.playerCount())
                     );
                  }
               }
            }
         }
      }
   }

   private static SleepNotificationSystem.NotificationState getNotificationState(World world) {
      if (CanSleepInWorld.check(world).isNegative()) {
         return SleepNotificationSystem.NotReady.INSTANCE;
      } else {
         Store<EntityStore> store = world.getEntityStore().getStore();
         WorldSleep worldSleep = store.getResource(WorldSomnolence.getResourceType()).getState();
         if (worldSleep instanceof WorldSlumber) {
            return SleepNotificationSystem.NotReady.INSTANCE;
         } else {
            List<Ref<EntityStore>> refs = world.getPlayerRefs().stream().map(PlayerRef::getReference).filter(Objects::nonNull).toList();
            int playerCount = refs.size();
            int readyToSleep = countReadyToSleep(refs, store);
            if (playerCount <= 4) {
               boolean ready = readyToSleep > 0;
               return (SleepNotificationSystem.NotificationState)(ready
                  ? new SleepNotificationSystem.SmallServer(readyToSleep, playerCount)
                  : SleepNotificationSystem.NotReady.INSTANCE);
            } else {
               double sleepersRatio = (double)readyToSleep / playerCount;
               boolean ready = sleepersRatio >= 0.5;
               return (SleepNotificationSystem.NotificationState)(ready
                  ? new SleepNotificationSystem.BigServer(sleepersRatio, readyToSleep, playerCount)
                  : SleepNotificationSystem.NotReady.INSTANCE);
            }
         }
      }
   }

   private static int countReadyToSleep(Collection<Ref<EntityStore>> playerRefs, ComponentAccessor<EntityStore> store) {
      int count = 0;

      for (Ref<EntityStore> ref : playerRefs) {
         PlayerSomnolence somnolence = store.getComponent(ref, PlayerSomnolence.getComponentType());
         if (somnolence != null) {
            boolean readyToSleep = StartSlumberSystem.canNotifyOthersAboutTryingToSleep(store, ref);
            if (readyToSleep) {
               count++;
            }
         }
      }

      return count;
   }

   public static Message getSleeperName(@Nullable Ref<EntityStore> ref) {
      if (ref != null && ref.isValid()) {
         Store<EntityStore> store = ref.getStore();
         DisplayNameComponent displayNameComponent = store.getComponent(ref, DisplayNameComponent.getComponentType());
         Message lastSleeperDisplay = displayNameComponent == null ? null : displayNameComponent.getDisplayName();
         if (lastSleeperDisplay != null) {
            return lastSleeperDisplay;
         } else {
            PlayerRef sleeperPlayerRef = store.getComponent(ref, PlayerRef.getComponentType());
            return Message.raw(sleeperPlayerRef == null ? "???" : sleeperPlayerRef.getUsername());
         }
      } else {
         return Message.raw("???");
      }
   }

   private record BigServer(double ratio, int readyToSleep, int playerCount) implements SleepNotificationSystem.NotificationState {
   }

   private static enum NotReady implements SleepNotificationSystem.NotificationState {
      INSTANCE;

      @Override
      public int readyToSleep() {
         return 0;
      }

      @Override
      public int playerCount() {
         return 0;
      }
   }

   private sealed interface NotificationState permits SleepNotificationSystem.NotReady, SleepNotificationSystem.SmallServer, SleepNotificationSystem.BigServer {
      int readyToSleep();

      int playerCount();
   }

   private record SmallServer(int readyToSleep, int playerCount) implements SleepNotificationSystem.NotificationState {
   }
}
