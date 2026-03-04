package com.hypixel.hytale.server.npc.components.messaging;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public abstract class MessageSupport implements Component<EntityStore> {
   public abstract NPCMessage[] getMessageSlots();

   public boolean isMessageQueued(int messageIndex) {
      return this.getMessageSlots() == null ? false : this.getMessageSlots()[messageIndex].isActivated();
   }

   public boolean isMessageEnabled(int messageIndex) {
      return this.getMessageSlots() == null ? false : this.getMessageSlots()[messageIndex].isEnabled();
   }

   @Override
   public abstract Component<EntityStore> clone();
}
