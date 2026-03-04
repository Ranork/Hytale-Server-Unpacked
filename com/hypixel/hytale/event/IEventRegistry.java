package com.hypixel.hytale.event;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IEventRegistry {
   @Nullable
   <EventType extends IBaseEvent<Void>> EventRegistration<Void, EventType> register(@Nonnull Class<? super EventType> var1, @Nonnull Consumer<EventType> var2);

   @Nullable
   <EventType extends IBaseEvent<Void>> EventRegistration<Void, EventType> register(
      @Nonnull EventPriority var1, @Nonnull Class<? super EventType> var2, @Nonnull Consumer<EventType> var3
   );

   @Nullable
   <EventType extends IBaseEvent<Void>> EventRegistration<Void, EventType> register(
      short var1, @Nonnull Class<? super EventType> var2, @Nonnull Consumer<EventType> var3
   );

   @Nullable
   <KeyType, EventType extends IBaseEvent<KeyType>> EventRegistration<KeyType, EventType> register(
      @Nonnull Class<? super EventType> var1, @Nonnull KeyType var2, @Nonnull Consumer<EventType> var3
   );

   @Nullable
   <KeyType, EventType extends IBaseEvent<KeyType>> EventRegistration<KeyType, EventType> register(
      @Nonnull EventPriority var1, @Nonnull Class<? super EventType> var2, @Nonnull KeyType var3, @Nonnull Consumer<EventType> var4
   );

   @Nullable
   <KeyType, EventType extends IBaseEvent<KeyType>> EventRegistration<KeyType, EventType> register(
      short var1, @Nonnull Class<? super EventType> var2, @Nonnull KeyType var3, @Nonnull Consumer<EventType> var4
   );

   @Nullable
   <EventType extends IAsyncEvent<Void>> EventRegistration<Void, EventType> registerAsync(
      @Nonnull Class<? super EventType> var1, @Nonnull Function<CompletableFuture<EventType>, CompletableFuture<EventType>> var2
   );

   @Nullable
   <EventType extends IAsyncEvent<Void>> EventRegistration<Void, EventType> registerAsync(
      @Nonnull EventPriority var1, @Nonnull Class<? super EventType> var2, @Nonnull Function<CompletableFuture<EventType>, CompletableFuture<EventType>> var3
   );

   @Nullable
   <EventType extends IAsyncEvent<Void>> EventRegistration<Void, EventType> registerAsync(
      short var1, Class<? super EventType> var2, @Nonnull Function<CompletableFuture<EventType>, CompletableFuture<EventType>> var3
   );

   @Nullable
   <KeyType, EventType extends IAsyncEvent<KeyType>> EventRegistration<KeyType, EventType> registerAsync(
      @Nonnull Class<? super EventType> var1, @Nonnull KeyType var2, @Nonnull Function<CompletableFuture<EventType>, CompletableFuture<EventType>> var3
   );

   @Nullable
   <KeyType, EventType extends IAsyncEvent<KeyType>> EventRegistration<KeyType, EventType> registerAsync(
      @Nonnull EventPriority var1,
      Class<? super EventType> var2,
      @Nonnull KeyType var3,
      @Nonnull Function<CompletableFuture<EventType>, CompletableFuture<EventType>> var4
   );

   @Nullable
   <KeyType, EventType extends IAsyncEvent<KeyType>> EventRegistration<KeyType, EventType> registerAsync(
      short var1,
      @Nonnull Class<? super EventType> var2,
      @Nonnull KeyType var3,
      @Nonnull Function<CompletableFuture<EventType>, CompletableFuture<EventType>> var4
   );

   @Nullable
   <KeyType, EventType extends IBaseEvent<KeyType>> EventRegistration<KeyType, EventType> registerGlobal(
      @Nonnull Class<? super EventType> var1, @Nonnull Consumer<EventType> var2
   );

   @Nullable
   <KeyType, EventType extends IBaseEvent<KeyType>> EventRegistration<KeyType, EventType> registerGlobal(
      @Nonnull EventPriority var1, @Nonnull Class<? super EventType> var2, @Nonnull Consumer<EventType> var3
   );

   @Nullable
   <KeyType, EventType extends IBaseEvent<KeyType>> EventRegistration<KeyType, EventType> registerGlobal(
      short var1, @Nonnull Class<? super EventType> var2, @Nonnull Consumer<EventType> var3
   );

   @Nullable
   <KeyType, EventType extends IAsyncEvent<KeyType>> EventRegistration<KeyType, EventType> registerAsyncGlobal(
      @Nonnull Class<? super EventType> var1, @Nonnull Function<CompletableFuture<EventType>, CompletableFuture<EventType>> var2
   );

   @Nullable
   <KeyType, EventType extends IAsyncEvent<KeyType>> EventRegistration<KeyType, EventType> registerAsyncGlobal(
      @Nonnull EventPriority var1, @Nonnull Class<? super EventType> var2, @Nonnull Function<CompletableFuture<EventType>, CompletableFuture<EventType>> var3
   );

   @Nullable
   <KeyType, EventType extends IAsyncEvent<KeyType>> EventRegistration<KeyType, EventType> registerAsyncGlobal(
      short var1, @Nonnull Class<? super EventType> var2, @Nonnull Function<CompletableFuture<EventType>, CompletableFuture<EventType>> var3
   );

   @Nullable
   <KeyType, EventType extends IBaseEvent<KeyType>> EventRegistration<KeyType, EventType> registerUnhandled(
      @Nonnull Class<? super EventType> var1, @Nonnull Consumer<EventType> var2
   );

   @Nullable
   <KeyType, EventType extends IBaseEvent<KeyType>> EventRegistration<KeyType, EventType> registerUnhandled(
      @Nonnull EventPriority var1, @Nonnull Class<? super EventType> var2, @Nonnull Consumer<EventType> var3
   );

   @Nullable
   <KeyType, EventType extends IBaseEvent<KeyType>> EventRegistration<KeyType, EventType> registerUnhandled(
      short var1, @Nonnull Class<? super EventType> var2, @Nonnull Consumer<EventType> var3
   );

   @Nullable
   <KeyType, EventType extends IAsyncEvent<KeyType>> EventRegistration<KeyType, EventType> registerAsyncUnhandled(
      @Nonnull Class<? super EventType> var1, @Nonnull Function<CompletableFuture<EventType>, CompletableFuture<EventType>> var2
   );

   @Nullable
   <KeyType, EventType extends IAsyncEvent<KeyType>> EventRegistration<KeyType, EventType> registerAsyncUnhandled(
      @Nonnull EventPriority var1, @Nonnull Class<? super EventType> var2, @Nonnull Function<CompletableFuture<EventType>, CompletableFuture<EventType>> var3
   );

   @Nullable
   <KeyType, EventType extends IAsyncEvent<KeyType>> EventRegistration<KeyType, EventType> registerAsyncUnhandled(
      short var1, @Nonnull Class<? super EventType> var2, @Nonnull Function<CompletableFuture<EventType>, CompletableFuture<EventType>> var3
   );
}
