package com.hypixel.hytale.server.npc.components.messaging;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.blackboard.view.event.EventNotification;
import com.hypixel.hytale.server.npc.blackboard.view.event.block.BlockEventType;
import javax.annotation.Nonnull;

public class NPCBlockEventSupport extends EventSupport<BlockEventType, EventNotification> implements Component<EntityStore> {
   public static ComponentType<EntityStore, NPCBlockEventSupport> getComponentType() {
      return NPCPlugin.get().getNpcBlockEventSupportComponentType();
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      NPCBlockEventSupport support = new NPCBlockEventSupport();
      this.cloneTo(support);
      return support;
   }
}
