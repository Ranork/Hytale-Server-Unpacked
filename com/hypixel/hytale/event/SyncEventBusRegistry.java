package com.hypixel.hytale.event;

import com.hypixel.hytale.logger.HytaleLogger;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SyncEventBusRegistry<KeyType, EventType extends IEvent<KeyType>>
   extends EventBusRegistry<KeyType, EventType, SyncEventBusRegistry.SyncEventConsumerMap<EventType>> {
   public static final IEventDispatcher NO_OP = new IEventDispatcher<IBaseEvent, IBaseEvent>() {
      @Override
      public boolean hasListener() {
         return false;
      }

      public IBaseEvent dispatch(IBaseEvent event) {
         return event;
      }
   };
   private final IEventDispatcher<EventType, EventType> globalDispatcher = event -> {
      if (!this.dispatchGlobal(event)) {
         this.dispatchUnhandled(event);
      }

      return event;
   };

   public SyncEventBusRegistry(HytaleLogger logger, Class<EventType> eventClass) {
      super(logger, eventClass, new SyncEventBusRegistry.SyncEventConsumerMap<>(null), new SyncEventBusRegistry.SyncEventConsumerMap<>(null));
      this.global.registry = this.unhandled.registry = this;
   }

   @Nonnull
   @Override
   public EventRegistration<KeyType, EventType> register(short priority, @Nullable KeyType key, @Nonnull Consumer<EventType> consumer) {
      if (this.shutdown) {
         throw new IllegalArgumentException("EventRegistry is shutdown!");
      } else {
         KeyType k = (KeyType)(key != null ? key : NULL);
         SyncEventBusRegistry.SyncEventConsumerMap<EventType> eventMap = this.map
            .computeIfAbsent(k, o -> new SyncEventBusRegistry.SyncEventConsumerMap<>(this));
         SyncEventBusRegistry.SyncEventConsumer<EventType> eventConsumer = new SyncEventBusRegistry.SyncEventConsumer<>(priority, consumer);
         eventMap.add(eventConsumer);
         return new EventRegistration<>(this.eventClass, this::isAlive, () -> this.unregister(key, eventConsumer));
      }
   }

   private void unregister(@Nullable KeyType key, @Nonnull SyncEventBusRegistry.SyncEventConsumer<EventType> consumer) {
      if (this.shutdown) {
         throw new IllegalArgumentException("EventRegistry is shutdown!");
      } else {
         KeyType k = (KeyType)(key != null ? key : NULL);
         SyncEventBusRegistry.SyncEventConsumerMap<EventType> eventMap = this.map.get(k);
         if (eventMap != null && !eventMap.remove(consumer)) {
            throw new IllegalArgumentException(String.valueOf(consumer));
         }
      }
   }

   @Nonnull
   @Override
   public EventRegistration<KeyType, EventType> registerGlobal(short priority, @Nonnull Consumer<EventType> consumer) {
      if (this.shutdown) {
         throw new IllegalArgumentException("EventRegistry is shutdown!");
      } else {
         SyncEventBusRegistry.SyncEventConsumer<EventType> eventConsumer = new SyncEventBusRegistry.SyncEventConsumer<>(priority, consumer);
         this.global.add(eventConsumer);
         return new EventRegistration<>(this.eventClass, this::isAlive, () -> this.unregisterGlobal(eventConsumer));
      }
   }

   private void unregisterGlobal(@Nonnull SyncEventBusRegistry.SyncEventConsumer<EventType> consumer) {
      if (this.shutdown) {
         throw new IllegalArgumentException("EventRegistry is shutdown!");
      } else if (!this.global.remove(consumer)) {
         throw new IllegalArgumentException(String.valueOf(consumer));
      }
   }

   @Nonnull
   @Override
   public EventRegistration<KeyType, EventType> registerUnhandled(short priority, @Nonnull Consumer<EventType> consumer) {
      if (this.shutdown) {
         throw new IllegalArgumentException("EventRegistry is shutdown!");
      } else {
         SyncEventBusRegistry.SyncEventConsumer<EventType> eventConsumer = new SyncEventBusRegistry.SyncEventConsumer<>(priority, consumer);
         this.unhandled.add(eventConsumer);
         return new EventRegistration<>(this.eventClass, this::isAlive, () -> this.unregisterUnhandled(eventConsumer));
      }
   }

   private void unregisterUnhandled(@Nonnull SyncEventBusRegistry.SyncEventConsumer<EventType> consumer) {
      if (this.shutdown) {
         throw new IllegalArgumentException("EventRegistry is shutdown!");
      } else if (!this.unhandled.remove(consumer)) {
         throw new IllegalArgumentException(String.valueOf(consumer));
      }
   }

   @Nonnull
   @Override
   public IEventDispatcher<EventType, EventType> dispatchFor(@Nullable KeyType key) {
      if (this.shutdown) {
         throw new IllegalArgumentException("EventRegistry is shutdown!");
      } else {
         KeyType k = (KeyType)(key != null ? key : NULL);
         SyncEventBusRegistry.SyncEventConsumerMap<EventType> eventMap = this.map.get(k);
         if (eventMap != null && !eventMap.isEmpty()) {
            return eventMap;
         } else {
            return this.global.isEmpty() && this.unhandled.isEmpty() ? NO_OP : this.globalDispatcher;
         }
      }
   }

   private boolean dispatchGlobal(EventType event) {
      return this.dispatchEventMap(event, this.global, "Failed to dispatch event (global)");
   }

   private boolean dispatchUnhandled(EventType event) {
      return this.dispatchEventMap(event, this.unhandled, "Failed to dispatch event (unhandled)");
   }

   private boolean dispatchEventMap(EventType event, @Nonnull SyncEventBusRegistry.SyncEventConsumerMap<EventType> eventMap, String s) {
      boolean handled = false;

      for (short priority : eventMap.getPriorities()) {
         List<SyncEventBusRegistry.SyncEventConsumer<EventType>> consumers = eventMap.get(priority);
         if (consumers != null) {
            for (SyncEventBusRegistry.SyncEventConsumer<EventType> consumer : consumers) {
               try {
                  Consumer<EventType> theConsumer = this.timeEvents ? consumer.getTimedConsumer() : consumer.getConsumer();
                  theConsumer.accept(event);
                  if (event instanceof IProcessedEvent processedEvent) {
                     processedEvent.processEvent(consumer.getConsumerString());
                  }

                  handled = true;
               } catch (Throwable var14) {
                  ((HytaleLogger.Api)this.logger.at(Level.SEVERE).withCause(var14)).log("%s %s to %s", s, event, consumer);
               }
            }
         }
      }

      return handled;
   }

   protected static class SyncEventConsumer<EventType extends IEvent> extends EventBusRegistry.EventConsumer {
      @Nonnull
      private final Consumer<EventType> consumer;
      @Nonnull
      private final Consumer<EventType> timedConsumer;

      public SyncEventConsumer(short priority, @Nonnull Consumer<EventType> consumer) {
         super(priority, consumer.toString());
         this.consumer = consumer;
         this.timedConsumer = t -> {
            long before = System.nanoTime();
            consumer.accept(t);
            long after = System.nanoTime();
            this.timer.add(after - before);
         };
      }

      @Nonnull
      protected Consumer<EventType> getConsumer() {
         return this.consumer;
      }

      @Nonnull
      public Consumer<EventType> getTimedConsumer() {
         return this.timedConsumer;
      }

      @Nonnull
      @Override
      public String toString() {
         return "SyncEventConsumer{consumer=" + this.consumer + ", timedConsumer=" + this.timedConsumer + "} " + super.toString();
      }
   }

   protected static class SyncEventConsumerMap<EventType extends IEvent>
      extends EventBusRegistry.EventConsumerMap<EventType, SyncEventBusRegistry.SyncEventConsumer<EventType>, EventType> {
      protected SyncEventBusRegistry registry;

      public SyncEventConsumerMap(SyncEventBusRegistry registry) {
         this.registry = registry;
      }

      public EventType dispatch(EventType event) {
         boolean handled = this.registry.dispatchEventMap(event, this, "Failed to dispatch event");
         if (!this.registry.dispatchGlobal(event) && !handled) {
            this.registry.dispatchUnhandled(event);
         }

         return event;
      }
   }
}
