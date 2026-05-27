package com.hypixel.hytale.builtin.triggervolumes.system;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEventType;
import com.hypixel.hytale.builtin.triggervolumes.manager.VolumeEntry;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DelayedEffectScheduler {
   @Nonnull
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private final PriorityQueue<DelayedEffectScheduler.ScheduledItem> queue = new PriorityQueue<>();

   public void schedule(
      @Nonnull TriggerEffect effect,
      @Nonnull Ref<EntityStore> entityRef,
      @Nonnull UUID entityUuid,
      @Nonnull TriggerEventType eventType,
      @Nonnull VolumeEntry volume,
      long nowNanos,
      float delaySeconds
   ) {
      this.schedule(effect, entityRef, entityUuid, eventType, volume, nowNanos, delaySeconds, null);
   }

   public void schedule(
      @Nonnull TriggerEffect effect,
      @Nonnull Ref<EntityStore> entityRef,
      @Nonnull UUID entityUuid,
      @Nonnull TriggerEventType eventType,
      @Nonnull VolumeEntry volume,
      long nowNanos,
      float delaySeconds,
      @Nullable List<VolumeEntry> spatialVolumes
   ) {
      this.schedule(effect, entityRef, entityUuid, eventType, volume, nowNanos, delaySeconds, spatialVolumes, null);
   }

   public void schedule(
      @Nonnull TriggerEffect effect,
      @Nonnull Ref<EntityStore> entityRef,
      @Nonnull UUID entityUuid,
      @Nonnull TriggerEventType eventType,
      @Nonnull VolumeEntry volume,
      long nowNanos,
      float delaySeconds,
      @Nullable List<VolumeEntry> spatialVolumes,
      @Nullable DelayedEffectScheduler.ScheduledEffectCallback callback
   ) {
      long executeAt = nowNanos + (long)(delaySeconds * 1.0E9F);
      this.queue.add(new DelayedEffectScheduler.ScheduledEffect(executeAt, effect, entityRef, entityUuid, eventType, volume, spatialVolumes, callback));
   }

   public void scheduleGate(
      @Nonnull DelayedEffectScheduler.ScheduledGateAction action,
      @Nonnull Ref<EntityStore> entityRef,
      @Nonnull UUID entityUuid,
      @Nonnull TriggerEventType eventType,
      @Nonnull VolumeEntry volume,
      long nowNanos,
      float delaySeconds,
      @Nullable List<VolumeEntry> spatialVolumes
   ) {
      long executeAt = nowNanos + (long)(delaySeconds * 1.0E9F);
      this.queue.add(new DelayedEffectScheduler.ScheduledGate(executeAt, action, entityRef, entityUuid, eventType, volume, spatialVolumes));
   }

   public void tick(long nowNanos, @Nonnull Store<EntityStore> store) {
      while (!this.queue.isEmpty() && this.queue.peek().executeAtNanos() <= nowNanos) {
         DelayedEffectScheduler.ScheduledItem scheduled = this.queue.poll();
         if (scheduled != null) {
            boolean executed = false;

            try {
               if (scheduled.entityRef().isValid() && !scheduled.volume().isPendingDestroy()) {
                  executed = scheduled.execute(store, nowNanos);
               }
            } finally {
               scheduled.onFinished(executed, nowNanos);
            }
         }
      }
   }

   public void cancelForEntity(@Nonnull UUID entityUuid) {
      this.cancelIf(scheduledItem -> scheduledItem.entityUuid().equals(entityUuid));
   }

   public void cancelNonExitForEntityInVolume(@Nonnull UUID entityUuid, @Nonnull VolumeEntry volume) {
      this.cancelIf(
         scheduledItem -> scheduledItem.entityUuid().equals(entityUuid)
            && scheduledItem.volume() == volume
            && scheduledItem.eventType() != TriggerEventType.EXIT
      );
   }

   public void cancelForVolume(@Nonnull VolumeEntry volume) {
      this.cancelIf(scheduledItem -> scheduledItem.volume() == volume);
   }

   public boolean isEmpty() {
      return this.queue.isEmpty();
   }

   private void cancelIf(@Nonnull Predicate<DelayedEffectScheduler.ScheduledItem> predicate) {
      long nowNanos = System.nanoTime();
      Iterator<DelayedEffectScheduler.ScheduledItem> iterator = this.queue.iterator();

      while (iterator.hasNext()) {
         DelayedEffectScheduler.ScheduledItem scheduledItem = iterator.next();
         if (predicate.test(scheduledItem)) {
            iterator.remove();
            scheduledItem.onFinished(false, nowNanos);
         }
      }
   }

   private record ScheduledEffect(
      long executeAtNanos,
      @Nonnull TriggerEffect effect,
      @Nonnull Ref<EntityStore> entityRef,
      @Nonnull UUID entityUuid,
      @Nonnull TriggerEventType eventType,
      @Nonnull VolumeEntry volume,
      @Nullable List<VolumeEntry> spatialVolumes,
      @Nullable DelayedEffectScheduler.ScheduledEffectCallback callback
   ) implements DelayedEffectScheduler.ScheduledItem {
      @Override
      public boolean execute(@Nonnull Store<EntityStore> store, long nowNanos) {
         try {
            Ref<EntityStore> actorRef = this.volume.getProjectileSource().resolveActorRef(this.entityRef, store);
            TriggerContext context = this.spatialVolumes != null
               ? new TriggerContext(actorRef, store, this.eventType, this.volume, this.spatialVolumes)
               : new TriggerContext(actorRef, store, this.eventType, this.volume);
            this.effect.execute(context);
            return true;
         } catch (Exception var6) {
            ((HytaleLogger.Api)DelayedEffectScheduler.LOGGER.at(Level.WARNING).withCause(var6))
               .log("Error executing delayed effect %s on volume '%s'", this.effect.getClass().getSimpleName(), this.volume.getId());
            return false;
         }
      }

      @Override
      public void onFinished(boolean executed, long nowNanos) {
         if (this.callback != null) {
            try {
               this.callback.onFinished(executed, nowNanos);
            } catch (Exception var5) {
               ((HytaleLogger.Api)DelayedEffectScheduler.LOGGER.at(Level.WARNING).withCause(var5))
                  .log("Error finishing delayed effect %s on volume '%s'", this.effect.getClass().getSimpleName(), this.volume.getId());
            }
         }
      }
   }

   public interface ScheduledEffectCallback {
      void onFinished(boolean var1, long var2);
   }

   private record ScheduledGate(
      long executeAtNanos,
      @Nonnull DelayedEffectScheduler.ScheduledGateAction action,
      @Nonnull Ref<EntityStore> entityRef,
      @Nonnull UUID entityUuid,
      @Nonnull TriggerEventType eventType,
      @Nonnull VolumeEntry volume,
      @Nullable List<VolumeEntry> spatialVolumes
   ) implements DelayedEffectScheduler.ScheduledItem {
      @Override
      public boolean execute(@Nonnull Store<EntityStore> store, long nowNanos) {
         this.action.execute(this.entityRef, this.entityUuid, this.eventType, this.volume, this.spatialVolumes, store, nowNanos);
         return true;
      }
   }

   public interface ScheduledGateAction {
      void execute(
         @Nonnull Ref<EntityStore> var1,
         @Nonnull UUID var2,
         @Nonnull TriggerEventType var3,
         @Nonnull VolumeEntry var4,
         @Nullable List<VolumeEntry> var5,
         @Nonnull Store<EntityStore> var6,
         long var7
      );
   }

   private interface ScheduledItem extends Comparable<DelayedEffectScheduler.ScheduledItem> {
      long executeAtNanos();

      @Nonnull
      Ref<EntityStore> entityRef();

      @Nonnull
      UUID entityUuid();

      @Nonnull
      VolumeEntry volume();

      @Nonnull
      TriggerEventType eventType();

      boolean execute(@Nonnull Store<EntityStore> var1, long var2);

      default void onFinished(boolean executed, long nowNanos) {
      }

      default int compareTo(@Nonnull DelayedEffectScheduler.ScheduledItem other) {
         return Long.compare(this.executeAtNanos(), other.executeAtNanos());
      }
   }
}
