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
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import javax.annotation.Nonnull;

public class UpdateWorldSlumberSystem extends TickingSystem<EntityStore> {
   @Nonnull
   private final ComponentType<EntityStore, PlayerSomnolence> playerSomnolenceComponentType;
   @Nonnull
   private final ResourceType<EntityStore, WorldSomnolence> worldSomnolenceResourceType;
   @Nonnull
   private final ResourceType<EntityStore, WorldTimeResource> worldTimeResourceType;

   public UpdateWorldSlumberSystem(
      @Nonnull ComponentType<EntityStore, PlayerSomnolence> playerSomnolenceComponentType,
      @Nonnull ResourceType<EntityStore, WorldSomnolence> worldSomnolenceResourceType,
      @Nonnull ResourceType<EntityStore, WorldTimeResource> worldTimeResourceType
   ) {
      this.playerSomnolenceComponentType = playerSomnolenceComponentType;
      this.worldSomnolenceResourceType = worldSomnolenceResourceType;
      this.worldTimeResourceType = worldTimeResourceType;
   }

   @Override
   public void tick(float dt, int systemIndex, @Nonnull Store<EntityStore> store) {
      World world = store.getExternalData().getWorld();
      WorldSomnolence worldSomnolenceResource = store.getResource(this.worldSomnolenceResourceType);
      if (worldSomnolenceResource.getState() instanceof WorldSlumber slumber) {
         slumber.incrementProgressSeconds(dt);
         boolean itsMorningTimeToWAKEUP = slumber.getProgressSeconds() >= slumber.getIrlDurationSeconds();
         boolean someoneIsAwake = isSomeoneAwake(store, this.playerSomnolenceComponentType);
         boolean sleepingIsOver = itsMorningTimeToWAKEUP || someoneIsAwake;
         if (sleepingIsOver) {
            worldSomnolenceResource.setState(WorldSleep.Awake.INSTANCE);
            WorldTimeResource timeResource = store.getResource(this.worldTimeResourceType);
            Instant now = timeResource.getGameTime();
            Instant wakeUpTime = computeWakeupTime(slumber);
            timeResource.setGameTime(wakeUpTime, world, store);
            store.forEachEntityParallel(this.playerSomnolenceComponentType, (index, archetypeChunk, commandBuffer) -> {
               PlayerSomnolence somnolenceComponent = archetypeChunk.getComponent(index, this.playerSomnolenceComponentType);

               assert somnolenceComponent != null;

               if (somnolenceComponent.getSleepState() instanceof PlayerSleep.Slumber) {
                  Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
                  PlayerSomnolence sleepComponent = PlayerSleep.MorningWakeUp.createComponent(itsMorningTimeToWAKEUP ? now : null);
                  commandBuffer.putComponent(ref, this.playerSomnolenceComponentType, sleepComponent);
               }
            });
         }
      }
   }

   @Nonnull
   private static Instant computeWakeupTime(@Nonnull WorldSlumber slumber) {
      float progress = slumber.getProgressSeconds() / slumber.getIrlDurationSeconds();
      long totalNanos = Duration.between(slumber.getStartInstant(), slumber.getTargetInstant()).toNanos();
      long progressNanos = (long)((float)totalNanos * progress);
      return slumber.getStartInstant().plusNanos(progressNanos);
   }

   private static boolean isSomeoneAwake(
      @Nonnull ComponentAccessor<EntityStore> store, @Nonnull ComponentType<EntityStore, PlayerSomnolence> playerSomnolenceComponentType
   ) {
      World world = store.getExternalData().getWorld();
      Collection<PlayerRef> playerRefs = world.getPlayerRefs();
      if (playerRefs.isEmpty()) {
         return false;
      } else {
         for (PlayerRef playerRef : playerRefs) {
            Ref<EntityStore> ref = playerRef.getReference();
            if (ref != null && ref.isValid()) {
               PlayerSomnolence somnolenceComponent = store.getComponent(ref, playerSomnolenceComponentType);
               if (somnolenceComponent == null) {
                  return true;
               }

               PlayerSleep sleepState = somnolenceComponent.getSleepState();
               return sleepState instanceof PlayerSleep.FullyAwake;
            }
         }

         return false;
      }
   }
}
