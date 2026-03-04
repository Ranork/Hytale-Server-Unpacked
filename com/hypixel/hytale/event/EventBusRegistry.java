package com.hypixel.hytale.event;

import com.hypixel.fastutil.shorts.Short2ObjectConcurrentHashMap;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.metrics.metric.Metric;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class EventBusRegistry<KeyType, EventType extends IBaseEvent<KeyType>, ConsumerMapType extends EventBusRegistry.EventConsumerMap<EventType, ?, ?>> {
   @Nonnull
   protected static final Object NULL = new Object();
   @Nonnull
   protected final HytaleLogger logger;
   @Nonnull
   protected final Class<EventType> eventClass;
   @Nonnull
   protected final Map<KeyType, ConsumerMapType> map = new ConcurrentHashMap<>();
   @Nonnull
   protected final ConsumerMapType global;
   @Nonnull
   protected final ConsumerMapType unhandled;
   protected boolean timeEvents;
   protected boolean shutdown;

   public EventBusRegistry(
      @Nonnull HytaleLogger logger, @Nonnull Class<EventType> eventClass, @Nonnull ConsumerMapType global, @Nonnull ConsumerMapType unhandled
   ) {
      this.logger = logger;
      this.eventClass = eventClass;
      this.global = global;
      this.unhandled = unhandled;
   }

   @Nonnull
   public Class<EventType> getEventClass() {
      return this.eventClass;
   }

   public boolean isTimeEvents() {
      return this.timeEvents;
   }

   public void setTimeEvents(boolean timeEvents) {
      this.timeEvents = timeEvents;
   }

   public void shutdown() {
      this.shutdown = true;
      this.map.clear();
   }

   public boolean isAlive() {
      return !this.shutdown;
   }

   public abstract EventRegistration<KeyType, EventType> register(short var1, @Nullable KeyType var2, @Nonnull Consumer<EventType> var3);

   public abstract EventRegistration<KeyType, EventType> registerGlobal(short var1, @Nonnull Consumer<EventType> var2);

   public abstract EventRegistration<KeyType, EventType> registerUnhandled(short var1, @Nonnull Consumer<EventType> var2);

   public abstract IEventDispatcher<EventType, ?> dispatchFor(KeyType var1);

   public abstract static class EventConsumer {
      @Nonnull
      protected static final AtomicInteger consumerIndex = new AtomicInteger();
      protected final int index;
      protected final short priority;
      @Nonnull
      protected final String consumerString;
      @Nonnull
      protected final Metric timer = new Metric();

      public EventConsumer(short priority, @Nonnull String consumerString) {
         this.priority = priority;
         this.consumerString = consumerString;
         this.index = consumerIndex.getAndIncrement();
      }

      public int getIndex() {
         return this.index;
      }

      public short getPriority() {
         return this.priority;
      }

      @Nonnull
      public String getConsumerString() {
         return this.consumerString;
      }

      @Nonnull
      public Metric getTimer() {
         return this.timer;
      }

      @Nonnull
      @Override
      public String toString() {
         return "EventConsumer{index="
            + this.index
            + ", priority="
            + this.priority
            + ", consumerString='"
            + this.consumerString
            + "', timer="
            + this.timer
            + "}";
      }
   }

   public abstract static class EventConsumerMap<EventType extends IBaseEvent, ConsumerType extends EventBusRegistry.EventConsumer, ReturnType>
      implements IEventDispatcher<EventType, ReturnType> {
      private static final short[] EMPTY_SHORT_ARRAY = new short[0];
      private final AtomicReference<short[]> prioritiesRef = new AtomicReference<>(EMPTY_SHORT_ARRAY);
      @Nonnull
      private final Short2ObjectConcurrentHashMap<List<ConsumerType>> map = new Short2ObjectConcurrentHashMap<>(true, (short)-32768);

      public boolean isEmpty() {
         return this.map.isEmpty();
      }

      public void add(@Nonnull ConsumerType eventConsumer) {
         short priority = eventConsumer.getPriority();
         boolean[] wasPriorityAdded = new boolean[]{false};
         this.map.computeIfAbsent(priority, s -> {
            wasPriorityAdded[0] = true;
            return new CopyOnWriteArrayList<>();
         }).add(eventConsumer);
         if (wasPriorityAdded[0]) {
            this.addPriority(priority);
         }
      }

      public boolean remove(@Nonnull ConsumerType consumer) {
         short priority = consumer.getPriority();
         boolean[] wasRemoved = new boolean[]{false, false};
         this.map.computeIfPresent(priority, (key, obj) -> {
            wasRemoved[0] = obj.remove(consumer);
            if (!obj.isEmpty()) {
               return obj;
            } else {
               wasRemoved[1] = true;
               return null;
            }
         });
         if (wasRemoved[1]) {
            this.removePriority(priority);
         }

         return wasRemoved[0];
      }

      public short[] getPriorities() {
         return this.prioritiesRef.get();
      }

      @Nullable
      public List<ConsumerType> get(short priority) {
         return this.map.get(priority);
      }

      private void addPriority(short priority) {
         while (this.map.containsKey(priority)) {
            short[] currentPriorities = this.prioritiesRef.get();
            int index = Arrays.binarySearch(currentPriorities, priority);
            if (index >= 0) {
               return;
            }

            int insertionPoint = -(index + 1);
            int newLength = currentPriorities.length + 1;
            short[] newPriorities = new short[newLength];
            System.arraycopy(currentPriorities, 0, newPriorities, 0, insertionPoint);
            newPriorities[insertionPoint] = priority;
            System.arraycopy(currentPriorities, insertionPoint, newPriorities, insertionPoint + 1, currentPriorities.length - insertionPoint);
            if (this.prioritiesRef.compareAndSet(currentPriorities, newPriorities)) {
               return;
            }
         }
      }

      private void removePriority(short priority) {
         while (!this.map.containsKey(priority)) {
            short[] currentPriorities = this.prioritiesRef.get();
            int index = Arrays.binarySearch(currentPriorities, priority);
            if (index < 0) {
               return;
            }

            int newLength = currentPriorities.length - 1;
            short[] newPriorities = new short[newLength];
            System.arraycopy(currentPriorities, 0, newPriorities, 0, index);
            System.arraycopy(currentPriorities, index + 1, newPriorities, index, newLength - index);
            if (this.prioritiesRef.compareAndSet(currentPriorities, newPriorities)) {
               return;
            }
         }
      }
   }
}
