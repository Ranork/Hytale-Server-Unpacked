package com.hypixel.hytale.event;

import javax.annotation.Nullable;

public interface IEventDispatcher<EventType extends IBaseEvent, ReturnType> {
   default boolean hasListener() {
      return true;
   }

   ReturnType dispatch(@Nullable EventType var1);
}
