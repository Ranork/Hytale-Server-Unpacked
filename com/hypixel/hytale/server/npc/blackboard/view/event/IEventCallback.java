package com.hypixel.hytale.server.npc.blackboard.view.event;

import com.hypixel.hytale.server.npc.entities.NPCEntity;

@FunctionalInterface
public interface IEventCallback<EventType, NotificationType extends EventNotification> {
   void notify(NPCEntity var1, EventType var2, NotificationType var3);
}
