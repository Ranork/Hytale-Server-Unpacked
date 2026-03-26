package com.hypixel.hytale.builtin.worldgen.modifier;

import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.builtin.worldgen.modifier.event.EventType;
import com.hypixel.hytale.builtin.worldgen.modifier.event.ModifyEvent;
import com.hypixel.hytale.builtin.worldgen.modifier.op.Op;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.hypixel.hytale.server.worldgen.util.ListPool;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.nio.file.Path;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map.Entry;
import javax.annotation.Nonnull;

public final class EventHandler implements AutoCloseable {
   private static final EventHandler EMPTY = new EventHandler();
   private static final ThreadLocal<EventHandler> SCOPED_HANDLER = ThreadLocal.withInitial(() -> EMPTY);
   private static final ListPool<EventHandler.Modifier> POOL = new ListPool<>(5, EventHandler.Modifier.EMPTY_ARRAY);
   private static final ListPool<EventHandler.PriorityEntry> ENTRY_POOL = new ListPool<>(5, EventHandler.PriorityEntry.EMPTY_ARRAY);
   @Nonnull
   private final EnumMap<EventType, EventHandler.Modifier[]> events = new EnumMap<>(EventType.class);

   private EventHandler() {
   }

   private EventHandler(@Nonnull String root) {
      try (ListPool.Resource<EventHandler.PriorityEntry> entries = ENTRY_POOL.acquire()) {
         List<AssetPack> packs = AssetModule.get().getAssetPacks();
         Object2IntOpenHashMap<String> packPriorities = new Object2IntOpenHashMap();

         for (int i = 0; i < packs.size(); i++) {
            packPriorities.put(packs.get(i).getName(), i);
         }

         for (Entry<String, WorldGenModifier> entry : WorldGenModifier.ASSET_MAP.getAssetMap().entrySet()) {
            if (entry.getValue().getTarget().matchesRoot(root)) {
               String pack = WorldGenModifier.ASSET_MAP.getAssetPack(entry.getKey());
               int priority = packPriorities.getOrDefault(pack, 0);
               entries.add(new EventHandler.PriorityEntry(entry.getValue(), priority));
            }
         }

         Collections.sort(entries);

         for (EventType type : EventType.VALUES) {
            try (ListPool.Resource<EventHandler.Modifier> modifiers = POOL.acquire()) {
               for (int i = 0; i < entries.size(); i++) {
                  EventHandler.PriorityEntry entryx = (EventHandler.PriorityEntry)entries.get(i);
                  Op[] ops = entryx.modifier.getOperations(type);
                  if (ops.length != 0) {
                     modifiers.add(new EventHandler.Modifier(entryx.modifier.target, ops));
                  }
               }

               this.events.put(type, modifiers.toArray());
            }
         }
      }
   }

   @Nonnull
   public EventHandler.Modifier[] get(@Nonnull EventType type) {
      return this.events.getOrDefault(type, EventHandler.Modifier.EMPTY_ARRAY);
   }

   @Override
   public void close() {
      this.events.clear();
      SCOPED_HANDLER.set(EMPTY);
   }

   public static <T> void handle(@Nonnull ModifyEvent<T> event) {
      EventHandler handler = SCOPED_HANDLER.get();
      String contentPath = event.file().getContentPath();

      for (EventHandler.Modifier modifier : handler.get(event.type())) {
         if (modifier.target().matchesRule(contentPath)) {
            for (Op op : modifier.ops()) {
               op.apply(event);
            }
         }
      }
   }

   public static EventHandler acquire(@Nonnull Path root) {
      assert SCOPED_HANDLER.get() == EMPTY : "EventHandler already open or was not closed!";

      EventHandler handler = new EventHandler(root.getFileName().toString());
      SCOPED_HANDLER.set(handler);
      return handler;
   }

   public record Modifier(@Nonnull Target target, @Nonnull Op[] ops) {
      public static final EventHandler.Modifier[] EMPTY_ARRAY = new EventHandler.Modifier[0];
   }

   public record PriorityEntry(WorldGenModifier modifier, int packPriority) implements Comparable<EventHandler.PriorityEntry> {
      public static final EventHandler.PriorityEntry[] EMPTY_ARRAY = new EventHandler.PriorityEntry[0];

      public int compareTo(EventHandler.PriorityEntry o) {
         return this.modifier.priority == o.modifier.priority
            ? Integer.compare(this.packPriority, o.packPriority)
            : Integer.compare(this.modifier.priority.getValue(), o.modifier.priority.getValue());
      }
   }
}
