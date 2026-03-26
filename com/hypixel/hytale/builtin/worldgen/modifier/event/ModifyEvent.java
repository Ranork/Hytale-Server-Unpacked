package com.hypixel.hytale.builtin.worldgen.modifier.event;

import com.google.gson.JsonElement;
import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.loader.context.FileContext;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ModifyEvent<T> extends IEvent<EventType> {
   @Nonnull
   EventType type();

   @Nonnull
   FileContext<?> file();

   @Nonnull
   List<T> entries();

   @Nonnull
   ModifyEvent.ContentLoader<T> loader();

   static <E extends ModifyEvent<?>> void dispatch(@Nonnull Class<E> type, @Nonnull E event) throws Error {
      try {
         HytaleServer.get().getEventBus().dispatchFor(type, event.type()).dispatch(event);
      } catch (Throwable var3) {
         throw new Error(String.format("Failed to invoke ModifyEvent %s for file %s", event.type(), event.file().getContentPath()), var3);
      }
   }

   @FunctionalInterface
   public interface ContentLoader<T> {
      @Nullable
      T load(@Nonnull JsonElement var1) throws Exception;
   }

   public static class SeedGenerator<K extends SeedStringResource> {
      private final SeedString<K> seed;
      private int id = 0;

      public SeedGenerator(@Nonnull SeedString<K> seed) {
         this.seed = seed;
      }

      public SeedString<K> next() {
         return this.seed.append("-modified-" + this.id++);
      }
   }
}
