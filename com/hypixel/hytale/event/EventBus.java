package com.hypixel.hytale.event;

import com.hypixel.hytale.logger.HytaleLogger;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EventBus implements IEventBus {
   @Nonnull
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   @Nonnull
   private final Map<Class<? extends IBaseEvent<?>>, EventBusRegistry<?, ?, ?>> registryMap = new ConcurrentHashMap<>();
   private final boolean timeEvents;

   public EventBus(boolean timeEvents) {
      this.timeEvents = timeEvents;
   }

   public void shutdown() {
      this.registryMap.values().forEach(EventBusRegistry::shutdown);
   }

   @Nonnull
   public Set<Class<? extends IBaseEvent<?>>> getRegisteredEventClasses() {
      return new HashSet<>(this.registryMap.keySet());
   }

   @Nonnull
   public Set<String> getRegisteredEventClassNames() {
      Set<String> classNames = new HashSet<>();

      for (Class<?> aClass : this.registryMap.keySet()) {
         classNames.add(aClass.getSimpleName());
      }

      return classNames;
   }

   @Nullable
   public EventBusRegistry<?, ?, ?> getRegistry(@Nonnull String eventName) {
      Class<? extends IBaseEvent> eventClass = null;

      for (Class<? extends IBaseEvent<?>> aClass : this.registryMap.keySet()) {
         if (aClass.getSimpleName().equalsIgnoreCase(eventName) || aClass.getName().equalsIgnoreCase(eventName)) {
            eventClass = aClass;
         }
      }

      return eventClass == null ? null : this.getRegistry(eventClass);
   }

   @Nonnull
   public <KeyType, EventType extends IBaseEvent<KeyType>> EventBusRegistry<KeyType, EventType, ?> getRegistry(@Nonnull Class<? super EventType> eventClass) {
      return (EventBusRegistry<KeyType, EventType, ?>)(IAsyncEvent.class.isAssignableFrom(eventClass)
         ? this.getAsyncRegistry(eventClass)
         : this.getSyncRegistry(eventClass));
   }

   @Nonnull
   public <KeyType, EventType extends IEvent<KeyType>> EventBusRegistry<KeyType, EventType, ?> getSyncRegistry(@Nonnull Class<? super EventType> eventClass) {
      EventBusRegistry<?, ? extends IBaseEvent<?>, ? extends EventBusRegistry.EventConsumerMap<? extends IBaseEvent<?>, ?, ?>> registry = (EventBusRegistry<?, ? extends IBaseEvent<?>, ? extends EventBusRegistry.EventConsumerMap<? extends IBaseEvent<?>, ?, ?>>)this.registryMap
         .computeIfAbsent((Class<? extends IBaseEvent<?>>)eventClass, aClass -> new SyncEventBusRegistry(LOGGER, (Class<EventType>)aClass));
      if (this.timeEvents) {
         registry.setTimeEvents(true);
      }

      return (EventBusRegistry<KeyType, EventType, ?>)registry;
   }

   @Nonnull
   private <KeyType, EventType extends IAsyncEvent<KeyType>> AsyncEventBusRegistry<KeyType, EventType> getAsyncRegistry(
      @Nonnull Class<? super EventType> eventClass
   ) {
      EventBusRegistry<?, ? extends IBaseEvent<?>, ? extends EventBusRegistry.EventConsumerMap<? extends IBaseEvent<?>, ?, ?>> registry = (EventBusRegistry<?, ? extends IBaseEvent<?>, ? extends EventBusRegistry.EventConsumerMap<? extends IBaseEvent<?>, ?, ?>>)this.registryMap
         .computeIfAbsent((Class<? extends IBaseEvent<?>>)eventClass, aClass -> new AsyncEventBusRegistry(LOGGER, (Class<EventType>)aClass));
      if (this.timeEvents) {
         registry.setTimeEvents(true);
      }

      return (AsyncEventBusRegistry<KeyType, EventType>)registry;
   }

   @Override
   public <EventType extends IBaseEvent<Void>> EventRegistration<Void, EventType> register(
      @Nonnull Class<? super EventType> eventClass, @Nonnull Consumer<EventType> consumer
   ) {
      return this.register((short)0, eventClass, null, consumer);
   }

   @Override
   public <EventType extends IBaseEvent<Void>> EventRegistration<Void, EventType> register(
      @Nonnull EventPriority priority, @Nonnull Class<? super EventType> eventClass, @Nonnull Consumer<EventType> consumer
   ) {
      return this.register(priority.getValue(), eventClass, null, consumer);
   }

   @Override
   public <EventType extends IBaseEvent<Void>> EventRegistration<Void, EventType> register(
      short priority, @Nonnull Class<? super EventType> eventClass, @Nonnull Consumer<EventType> consumer
   ) {
      return this.register(priority, eventClass, null, consumer);
   }

   @Override
   public <KeyType, EventType extends IBaseEvent<KeyType>> EventRegistration<KeyType, EventType> register(
      @Nonnull Class<? super EventType> eventClass, @Nonnull KeyType key, @Nonnull Consumer<EventType> consumer
   ) {
      return this.register((short)0, eventClass, key, consumer);
   }

   @Override
   public <KeyType, EventType extends IBaseEvent<KeyType>> EventRegistration<KeyType, EventType> register(
      @Nonnull EventPriority priority, @Nonnull Class<? super EventType> eventClass, @Nonnull KeyType key, @Nonnull Consumer<EventType> consumer
   ) {
      return this.register(priority.getValue(), eventClass, key, consumer);
   }

   @Override
   public <KeyType, EventType extends IBaseEvent<KeyType>> EventRegistration<KeyType, EventType> register(
      short priority, @Nonnull Class<? super EventType> eventClass, @Nullable KeyType key, @Nonnull Consumer<EventType> consumer
   ) {
      return this.<KeyType, EventType>getRegistry(eventClass).register(priority, key, consumer);
   }

   @Override
   public <EventType extends IAsyncEvent<Void>> EventRegistration<Void, EventType> registerAsync(
      @Nonnull Class<? super EventType> eventClass, @Nonnull Function<CompletableFuture<EventType>, CompletableFuture<EventType>> function
   ) {
      return this.registerAsync((short)0, eventClass, null, function);
   }

   @Override
   public <EventType extends IAsyncEvent<Void>> EventRegistration<Void, EventType> registerAsync(
      @Nonnull EventPriority priority,
      @Nonnull Class<? super EventType> eventClass,
      @Nonnull Function<CompletableFuture<EventType>, CompletableFuture<EventType>> function
   ) {
      return this.registerAsync(priority.getValue(), eventClass, null, function);
   }

   @Override
   public <EventType extends IAsyncEvent<Void>> EventRegistration<Void, EventType> registerAsync(
      short priority, Class<? super EventType> eventClass, @Nonnull Function<CompletableFuture<EventType>, CompletableFuture<EventType>> function
   ) {
      return this.registerAsync(priority, eventClass, null, function);
   }

   @Override
   public <KeyType, EventType extends IAsyncEvent<KeyType>> EventRegistration<KeyType, EventType> registerAsync(
      @Nonnull Class<? super EventType> eventClass,
      @Nonnull KeyType key,
      @Nonnull Function<CompletableFuture<EventType>, CompletableFuture<EventType>> function
   ) {
      return this.registerAsync((short)0, eventClass, key, function);
   }

   @Override
   public <KeyType, EventType extends IAsyncEvent<KeyType>> EventRegistration<KeyType, EventType> registerAsync(
      @Nonnull EventPriority priority,
      Class<? super EventType> eventClass,
      @Nonnull KeyType key,
      @Nonnull Function<CompletableFuture<EventType>, CompletableFuture<EventType>> function
   ) {
      return this.registerAsync(priority.getValue(), eventClass, key, function);
   }

   @Override
   public <KeyType, EventType extends IAsyncEvent<KeyType>> EventRegistration<KeyType, EventType> registerAsync(
      short priority,
      @Nonnull Class<? super EventType> eventClass,
      @Nullable KeyType key,
      @Nonnull Function<CompletableFuture<EventType>, CompletableFuture<EventType>> function
   ) {
      return this.<KeyType, EventType>getAsyncRegistry(eventClass).registerAsync(priority, key, function);
   }

   @Override
   public <KeyType, EventType extends IBaseEvent<KeyType>> EventRegistration<KeyType, EventType> registerGlobal(
      @Nonnull Class<? super EventType> eventClass, @Nonnull Consumer<EventType> consumer
   ) {
      return this.registerGlobal((short)0, eventClass, consumer);
   }

   @Override
   public <KeyType, EventType extends IBaseEvent<KeyType>> EventRegistration<KeyType, EventType> registerGlobal(
      @Nonnull EventPriority priority, @Nonnull Class<? super EventType> eventClass, @Nonnull Consumer<EventType> consumer
   ) {
      return this.registerGlobal(priority.getValue(), eventClass, consumer);
   }

   @Override
   public <KeyType, EventType extends IBaseEvent<KeyType>> EventRegistration<KeyType, EventType> registerGlobal(
      short priority, @Nonnull Class<? super EventType> eventClass, @Nonnull Consumer<EventType> consumer
   ) {
      return this.<KeyType, EventType>getRegistry(eventClass).registerGlobal(priority, consumer);
   }

   @Override
   public <KeyType, EventType extends IAsyncEvent<KeyType>> EventRegistration<KeyType, EventType> registerAsyncGlobal(
      @Nonnull Class<? super EventType> eventClass, @Nonnull Function<CompletableFuture<EventType>, CompletableFuture<EventType>> function
   ) {
      return this.registerAsyncGlobal((short)0, eventClass, function);
   }

   @Override
   public <KeyType, EventType extends IAsyncEvent<KeyType>> EventRegistration<KeyType, EventType> registerAsyncGlobal(
      @Nonnull EventPriority priority,
      @Nonnull Class<? super EventType> eventClass,
      @Nonnull Function<CompletableFuture<EventType>, CompletableFuture<EventType>> function
   ) {
      return this.registerAsyncGlobal(priority.getValue(), eventClass, function);
   }

   @Override
   public <KeyType, EventType extends IAsyncEvent<KeyType>> EventRegistration<KeyType, EventType> registerAsyncGlobal(
      short priority, @Nonnull Class<? super EventType> eventClass, @Nonnull Function<CompletableFuture<EventType>, CompletableFuture<EventType>> function
   ) {
      return this.<KeyType, EventType>getAsyncRegistry(eventClass).registerAsyncGlobal(priority, function);
   }

   @Override
   public <KeyType, EventType extends IBaseEvent<KeyType>> EventRegistration<KeyType, EventType> registerUnhandled(
      @Nonnull Class<? super EventType> eventClass, @Nonnull Consumer<EventType> consumer
   ) {
      return this.registerUnhandled((short)0, eventClass, consumer);
   }

   @Override
   public <KeyType, EventType extends IBaseEvent<KeyType>> EventRegistration<KeyType, EventType> registerUnhandled(
      @Nonnull EventPriority priority, @Nonnull Class<? super EventType> eventClass, @Nonnull Consumer<EventType> consumer
   ) {
      return this.registerUnhandled(priority.getValue(), eventClass, consumer);
   }

   @Override
   public <KeyType, EventType extends IBaseEvent<KeyType>> EventRegistration<KeyType, EventType> registerUnhandled(
      short priority, @Nonnull Class<? super EventType> eventClass, @Nonnull Consumer<EventType> consumer
   ) {
      return this.<KeyType, EventType>getRegistry(eventClass).registerUnhandled(priority, consumer);
   }

   @Override
   public <KeyType, EventType extends IAsyncEvent<KeyType>> EventRegistration<KeyType, EventType> registerAsyncUnhandled(
      @Nonnull Class<? super EventType> eventClass, @Nonnull Function<CompletableFuture<EventType>, CompletableFuture<EventType>> function
   ) {
      return this.registerAsyncUnhandled((short)0, eventClass, function);
   }

   @Override
   public <KeyType, EventType extends IAsyncEvent<KeyType>> EventRegistration<KeyType, EventType> registerAsyncUnhandled(
      @Nonnull EventPriority priority,
      @Nonnull Class<? super EventType> eventClass,
      @Nonnull Function<CompletableFuture<EventType>, CompletableFuture<EventType>> function
   ) {
      return this.registerAsyncUnhandled(priority.getValue(), eventClass, function);
   }

   @Override
   public <KeyType, EventType extends IAsyncEvent<KeyType>> EventRegistration<KeyType, EventType> registerAsyncUnhandled(
      short priority, @Nonnull Class<? super EventType> eventClass, @Nonnull Function<CompletableFuture<EventType>, CompletableFuture<EventType>> function
   ) {
      return this.<KeyType, EventType>getAsyncRegistry(eventClass).registerAsyncUnhandled(priority, function);
   }

   @Nonnull
   @Override
   public <KeyType, EventType extends IEvent<KeyType>> IEventDispatcher<EventType, EventType> dispatchFor(
      @Nonnull Class<? super EventType> eventClass, KeyType key
   ) {
      SyncEventBusRegistry<KeyType, EventType> registry = (SyncEventBusRegistry<KeyType, EventType>)this.registryMap.get(eventClass);
      return registry == null ? SyncEventBusRegistry.NO_OP : registry.dispatchFor(key);
   }

   @Nonnull
   @Override
   public <KeyType, EventType extends IAsyncEvent<KeyType>> IEventDispatcher<EventType, CompletableFuture<EventType>> dispatchForAsync(
      @Nonnull Class<? super EventType> eventClass, KeyType key
   ) {
      AsyncEventBusRegistry<KeyType, EventType> registry = (AsyncEventBusRegistry<KeyType, EventType>)this.registryMap.get(eventClass);
      return registry == null ? AsyncEventBusRegistry.NO_OP : registry.dispatchFor(key);
   }
}
