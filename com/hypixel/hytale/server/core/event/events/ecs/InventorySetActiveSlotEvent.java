package com.hypixel.hytale.server.core.event.events.ecs;

import com.hypixel.hytale.component.system.EcsEvent;

public class InventorySetActiveSlotEvent extends EcsEvent {
   private final int inventorySectionId;
   private final int previousSlot;
   private final byte newSlot;

   public InventorySetActiveSlotEvent(int inventorySectionId, int previousSlot, byte newSlot) {
      this.inventorySectionId = inventorySectionId;
      this.previousSlot = previousSlot;
      this.newSlot = newSlot;
   }

   public int getInventorySectionId() {
      return this.inventorySectionId;
   }

   public int getPreviousSlot() {
      return this.previousSlot;
   }

   public byte getNewSlot() {
      return this.newSlot;
   }
}
