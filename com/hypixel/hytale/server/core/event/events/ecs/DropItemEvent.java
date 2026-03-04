package com.hypixel.hytale.server.core.event.events.ecs;

import com.hypixel.hytale.component.system.CancellableEcsEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import javax.annotation.Nonnull;

public class DropItemEvent extends CancellableEcsEvent {
   public static final class Drop extends DropItemEvent {
      @Nonnull
      private ItemStack itemStack;
      private float throwSpeed;

      public Drop(@Nonnull ItemStack itemStack, float throwSpeed) {
         this.itemStack = itemStack;
         this.throwSpeed = throwSpeed;
      }

      public void setThrowSpeed(float throwSpeed) {
         this.throwSpeed = throwSpeed;
      }

      public float getThrowSpeed() {
         return this.throwSpeed;
      }

      public void setItemStack(@Nonnull ItemStack itemStack) {
         this.itemStack = itemStack;
      }

      @Nonnull
      public ItemStack getItemStack() {
         return this.itemStack;
      }
   }

   public static final class PlayerRequest extends DropItemEvent {
      private final int inventorySectionId;
      private final short slotId;

      public PlayerRequest(int inventorySectionId, short slotId) {
         this.inventorySectionId = inventorySectionId;
         this.slotId = slotId;
      }

      public int getInventorySectionId() {
         return this.inventorySectionId;
      }

      public short getSlotId() {
         return this.slotId;
      }
   }
}
