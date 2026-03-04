package com.hypixel.hytale.event;

import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IEventBus extends IEventRegistry {
   default <KeyType, EventType extends IEvent<KeyType>> EventType dispatch(@Nonnull Class<EventType> eventClass) {
      return this.dispatchFor(eventClass, null).dispatch(null);
   }

   default <EventType extends IAsyncEvent<Void>> CompletableFuture<EventType> dispatchAsync(@Nonnull Class<EventType> eventClass) {
      return this.<Void, EventType>dispatchForAsync(eventClass).dispatch(null);
   }

   default <KeyType, EventType extends IEvent<KeyType>> IEventDispatcher<EventType, EventType> dispatchFor(@Nonnull Class<? super EventType> eventClass) {
      return this.dispatchFor(eventClass, null);
   }

   <KeyType, EventType extends IEvent<KeyType>> IEventDispatcher<EventType, EventType> dispatchFor(
      @Nonnull Class<? super EventType> var1, @Nullable KeyType var2
   );

   default <KeyType, EventType extends IAsyncEvent<KeyType>> IEventDispatcher<EventType, CompletableFuture<EventType>> dispatchForAsync(
      @Nonnull Class<? super EventType> eventClass
   ) {
      return this.dispatchForAsync(eventClass, null);
   }

   <KeyType, EventType extends IAsyncEvent<KeyType>> IEventDispatcher<EventType, CompletableFuture<EventType>> dispatchForAsync(
      @Nonnull Class<? super EventType> var1, @Nullable KeyType var2
   );
}
