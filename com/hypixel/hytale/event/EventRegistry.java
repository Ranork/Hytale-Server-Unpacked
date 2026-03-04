package com.hypixel.hytale.event;

import com.hypixel.hytale.function.consumer.BooleanConsumer;
import com.hypixel.hytale.registry.Registry;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EventRegistry extends Registry<EventRegistration<?, ?>> implements IEventRegistry {
   @Nonnull
   private final IEventRegistry parent;

   public EventRegistry(
      @Nonnull List<BooleanConsumer> registrations, @Nonnull BooleanSupplier precondition, @Nullable String preconditionMessage, @Nonnull IEventRegistry parent
   ) {
      super(registrations, precondition, preconditionMessage, EventRegistration::new);
      this.parent = parent;
   }

   @Nonnull
   private IEventRegistry getParent() {
      return this.parent;
   }

   public <KeyType, EventType extends IBaseEvent<KeyType>> EventRegistration<KeyType, EventType> register(@Nonnull EventRegistration<KeyType, EventType> evt) {
      return super.register(evt);
   }

   @Override
   public <EventType extends IBaseEvent<Void>> EventRegistration<Void, EventType> register(
      @Nonnull Class<? super EventType> eventClass, @Nonnull Consumer<EventType> consumer
   ) {
      this.checkPrecondition();
      return this.register(this.getParent().register(eventClass, consumer));
   }

   @Override
   public <EventType extends IBaseEvent<Void>> EventRegistration<Void, EventType> register(
      @Nonnull EventPriority priority, @Nonnull Class<? super EventType> eventClass, @Nonnull Consumer<EventType> consumer
   ) {
      this.checkPrecondition();
      return this.register(this.getParent().register(priority, eventClass, consumer));
   }

   @Override
   public <EventType extends IBaseEvent<Void>> EventRegistration<Void, EventType> register(
      short priority, @Nonnull Class<? super EventType> eventClass, @Nonnull Consumer<EventType> consumer
   ) {
      this.checkPrecondition();
      return this.register(this.getParent().register(priority, eventClass, consumer));
   }

   @Override
   public <KeyType, EventType extends IBaseEvent<KeyType>> EventRegistration<KeyType, EventType> register(
      @Nonnull Class<? super EventType> eventClass, @Nonnull KeyType key, @Nonnull Consumer<EventType> consumer
   ) {
      this.checkPrecondition();
      return this.register(this.getParent().register(eventClass, key, consumer));
   }

   @Override
   public <KeyType, EventType extends IBaseEvent<KeyType>> EventRegistration<KeyType, EventType> register(
      @Nonnull EventPriority priority, @Nonnull Class<? super EventType> eventClass, @Nonnull KeyType key, @Nonnull Consumer<EventType> consumer
   ) {
      this.checkPrecondition();
      return this.register(this.getParent().register(priority, eventClass, key, consumer));
   }

   @Override
   public <KeyType, EventType extends IBaseEvent<KeyType>> EventRegistration<KeyType, EventType> register(
      short priority, @Nonnull Class<? super EventType> eventClass, @Nonnull KeyType key, @Nonnull Consumer<EventType> consumer
   ) {
      this.checkPrecondition();
      return this.register(this.getParent().register(priority, eventClass, key, consumer));
   }

   @Override
   public <EventType extends IAsyncEvent<Void>> EventRegistration<Void, EventType> registerAsync(
      @Nonnull Class<? super EventType> eventClass, @Nonnull Function<CompletableFuture<EventType>, CompletableFuture<EventType>> function
   ) {
      this.checkPrecondition();
      return this.register(this.getParent().registerAsync(eventClass, function));
   }

   @Override
   public <EventType extends IAsyncEvent<Void>> EventRegistration<Void, EventType> registerAsync(
      @Nonnull EventPriority priority,
      @Nonnull Class<? super EventType> eventClass,
      @Nonnull Function<CompletableFuture<EventType>, CompletableFuture<EventType>> function
   ) {
      this.checkPrecondition();
      return this.register(this.getParent().registerAsync(priority, eventClass, function));
   }

   @Override
   public <EventType extends IAsyncEvent<Void>> EventRegistration<Void, EventType> registerAsync(
      short priority, @Nonnull Class<? super EventType> eventClass, @Nonnull Function<CompletableFuture<EventType>, CompletableFuture<EventType>> function
   ) {
      this.checkPrecondition();
      return this.register(this.getParent().registerAsync(priority, eventClass, function));
   }

   @Override
   public <KeyType, EventType extends IAsyncEvent<KeyType>> EventRegistration<KeyType, EventType> registerAsync(
      @Nonnull Class<? super EventType> eventClass,
      @Nonnull KeyType key,
      @Nonnull Function<CompletableFuture<EventType>, CompletableFuture<EventType>> function
   ) {
      this.checkPrecondition();
      return this.register(this.getParent().registerAsync(eventClass, key, function));
   }

   @Override
   public <KeyType, EventType extends IAsyncEvent<KeyType>> EventRegistration<KeyType, EventType> registerAsync(
      @Nonnull EventPriority priority,
      Class<? super EventType> eventClass,
      @Nonnull KeyType key,
      @Nonnull Function<CompletableFuture<EventType>, CompletableFuture<EventType>> function
   ) {
      this.checkPrecondition();
      return this.register(this.getParent().registerAsync(priority, eventClass, key, function));
   }

   @Override
   public <KeyType, EventType extends IAsyncEvent<KeyType>> EventRegistration<KeyType, EventType> registerAsync(
      short priority,
      @Nonnull Class<? super EventType> eventClass,
      @Nonnull KeyType key,
      @Nonnull Function<CompletableFuture<EventType>, CompletableFuture<EventType>> function
   ) {
      this.checkPrecondition();
      return this.register(this.getParent().registerAsync(priority, eventClass, key, function));
   }

   @Override
   public <KeyType, EventType extends IBaseEvent<KeyType>> EventRegistration<KeyType, EventType> registerGlobal(
      @Nonnull Class<? super EventType> eventClass, @Nonnull Consumer<EventType> consumer
   ) {
      this.checkPrecondition();
      return this.register(this.getParent().registerGlobal(eventClass, consumer));
   }

   @Override
   public <KeyType, EventType extends IBaseEvent<KeyType>> EventRegistration<KeyType, EventType> registerGlobal(
      @Nonnull EventPriority priority, @Nonnull Class<? super EventType> eventClass, @Nonnull Consumer<EventType> consumer
   ) {
      this.checkPrecondition();
      return this.register(this.getParent().registerGlobal(priority, eventClass, consumer));
   }

   @Override
   public <KeyType, EventType extends IBaseEvent<KeyType>> EventRegistration<KeyType, EventType> registerGlobal(
      short priority, @Nonnull Class<? super EventType> eventClass, @Nonnull Consumer<EventType> consumer
   ) {
      this.checkPrecondition();
      return this.register(this.getParent().registerGlobal(priority, eventClass, consumer));
   }

   @Override
   public <KeyType, EventType extends IAsyncEvent<KeyType>> EventRegistration<KeyType, EventType> registerAsyncGlobal(
      @Nonnull Class<? super EventType> eventClass, @Nonnull Function<CompletableFuture<EventType>, CompletableFuture<EventType>> function
   ) {
      this.checkPrecondition();
      return this.register(this.getParent().registerAsyncGlobal(eventClass, function));
   }

   @Override
   public <KeyType, EventType extends IAsyncEvent<KeyType>> EventRegistration<KeyType, EventType> registerAsyncGlobal(
      @Nonnull EventPriority priority,
      @Nonnull Class<? super EventType> eventClass,
      @Nonnull Function<CompletableFuture<EventType>, CompletableFuture<EventType>> function
   ) {
      this.checkPrecondition();
      return this.register(this.getParent().registerAsyncGlobal(priority, eventClass, function));
   }

   @Override
   public <KeyType, EventType extends IAsyncEvent<KeyType>> EventRegistration<KeyType, EventType> registerAsyncGlobal(
      short priority, @Nonnull Class<? super EventType> eventClass, @Nonnull Function<CompletableFuture<EventType>, CompletableFuture<EventType>> function
   ) {
      this.checkPrecondition();
      return this.register(this.getParent().registerAsyncGlobal(priority, eventClass, function));
   }

   @Override
   public <KeyType, EventType extends IBaseEvent<KeyType>> EventRegistration<KeyType, EventType> registerUnhandled(
      @Nonnull Class<? super EventType> eventClass, @Nonnull Consumer<EventType> consumer
   ) {
      this.checkPrecondition();
      return this.register(this.getParent().registerUnhandled(eventClass, consumer));
   }

   @Override
   public <KeyType, EventType extends IBaseEvent<KeyType>> EventRegistration<KeyType, EventType> registerUnhandled(
      @Nonnull EventPriority priority, @Nonnull Class<? super EventType> eventClass, @Nonnull Consumer<EventType> consumer
   ) {
      this.checkPrecondition();
      return this.register(this.getParent().registerUnhandled(priority, eventClass, consumer));
   }

   @Override
   public <KeyType, EventType extends IBaseEvent<KeyType>> EventRegistration<KeyType, EventType> registerUnhandled(
      short priority, @Nonnull Class<? super EventType> eventClass, @Nonnull Consumer<EventType> consumer
   ) {
      this.checkPrecondition();
      return this.register(this.getParent().registerUnhandled(priority, eventClass, consumer));
   }

   @Override
   public <KeyType, EventType extends IAsyncEvent<KeyType>> EventRegistration<KeyType, EventType> registerAsyncUnhandled(
      @Nonnull Class<? super EventType> eventClass, @Nonnull Function<CompletableFuture<EventType>, CompletableFuture<EventType>> function
   ) {
      this.checkPrecondition();
      return this.register(this.getParent().registerAsyncUnhandled(eventClass, function));
   }

   @Override
   public <KeyType, EventType extends IAsyncEvent<KeyType>> EventRegistration<KeyType, EventType> registerAsyncUnhandled(
      @Nonnull EventPriority priority,
      @Nonnull Class<? super EventType> eventClass,
      @Nonnull Function<CompletableFuture<EventType>, CompletableFuture<EventType>> function
   ) {
      this.checkPrecondition();
      return this.register(this.getParent().registerAsyncUnhandled(priority, eventClass, function));
   }

   @Override
   public <KeyType, EventType extends IAsyncEvent<KeyType>> EventRegistration<KeyType, EventType> registerAsyncUnhandled(
      short priority, @Nonnull Class<? super EventType> eventClass, @Nonnull Function<CompletableFuture<EventType>, CompletableFuture<EventType>> function
   ) {
      this.checkPrecondition();
      return this.register(this.getParent().registerAsyncUnhandled(priority, eventClass, function));
   }
}
