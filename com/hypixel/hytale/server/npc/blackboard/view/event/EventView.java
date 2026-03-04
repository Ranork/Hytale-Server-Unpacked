package com.hypixel.hytale.server.npc.blackboard.view.event;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.function.consumer.IntObjectConsumer;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.blackboard.view.IBlackboardView;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class EventView<ViewType extends IBlackboardView<ViewType>, EventType extends Enum<EventType>, NotificationType extends EventNotification>
   implements IBlackboardView<ViewType> {
   @Nonnull
   protected final Map<EventType, EventTypeRegistration<EventType, NotificationType>> entityMapsByEventType;
   @Nonnull
   protected final World world;
   protected final EventType[] eventTypes;
   @Nullable
   protected EventRegistry eventRegistry;
   @Nullable
   protected ComponentRegistryProxy<EntityStore> entityStoreRegistry;
   protected boolean shutdown;
   protected final NotificationType reusableEventNotification;

   protected EventView(Class<EventType> type, EventType[] eventTypes, NotificationType reusableEventNotification, @Nonnull World world) {
      this.entityMapsByEventType = new EnumMap<>(type);
      this.eventTypes = eventTypes;
      this.reusableEventNotification = reusableEventNotification;
      this.world = world;
      this.entityStoreRegistry = NPCPlugin.get().getEntityStoreRegistry();
      this.eventRegistry = new EventRegistry(
         new CopyOnWriteArrayList<>(),
         () -> !this.shutdown,
         String.format("EventView for world %s is not enabled!", world.getName()),
         NPCPlugin.get().getEventRegistry()
      );
   }

   @Override
   public boolean isOutdated(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
      return false;
   }

   @Override
   public void onWorldRemoved() {
      this.shutdown = true;
      if (this.eventRegistry != null) {
         this.eventRegistry.shutdownAndCleanup(true);
         this.eventRegistry = null;
      }
   }

   @Override
   public void cleanup() {
      for (EventType eventType : this.eventTypes) {
         this.entityMapsByEventType.get(eventType).cleanup();
      }
   }

   public int getSetCount() {
      int count = 0;

      for (EventType type : this.eventTypes) {
         count += this.entityMapsByEventType.get(type).getSetCount();
      }

      return count;
   }

   public void forEach(@Nonnull IntObjectConsumer<EventType> setConsumer, @Nonnull Consumer<Ref<EntityStore>> npcConsumer) {
      for (EventType eventType : this.eventTypes) {
         this.entityMapsByEventType.get(eventType).forEach(setConsumer, npcConsumer);
      }
   }

   protected void onEvent(
      int senderTypeId,
      double x,
      double y,
      double z,
      Ref<EntityStore> initiator,
      Ref<EntityStore> skip,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor,
      EventType type
   ) {
      this.reusableEventNotification.setPosition(x, y, z);
      this.reusableEventNotification.setInitiator(initiator);
      this.entityMapsByEventType.get(type).relayEvent(senderTypeId, this.reusableEventNotification, skip, componentAccessor);
   }
}
