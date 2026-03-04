package com.hypixel.hytale.server.core.event.events.ecs;

import com.hypixel.hytale.component.system.CancellableEcsEvent;

public class SwitchActiveSlotEvent extends CancellableEcsEvent {
   private final int previousSlot;
   private final int inventorySectionId;
   private byte newSlot;
   private final boolean serverRequest;

   public SwitchActiveSlotEvent(int inventorySectionId, int previousSlot, byte newSlot, boolean serverRequest) {
      this.inventorySectionId = inventorySectionId;
      this.previousSlot = previousSlot;
      this.newSlot = newSlot;
      this.serverRequest = serverRequest;
   }

   public int getPreviousSlot() {
      return this.previousSlot;
   }

   public byte getNewSlot() {
      return this.newSlot;
   }

   public void setNewSlot(byte newSlot) {
      this.newSlot = newSlot;
   }

   public boolean isServerRequest() {
      return this.serverRequest;
   }

   public boolean isClientRequest() {
      return !this.serverRequest;
   }

   public int getInventorySectionId() {
      return this.inventorySectionId;
   }
}
