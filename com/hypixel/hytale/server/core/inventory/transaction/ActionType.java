package com.hypixel.hytale.server.core.inventory.transaction;

public enum ActionType {
   SET(true, false, true),
   ADD(true, false, false),
   REMOVE(false, true, false),
   REPLACE(true, true, false);

   private final boolean add;
   private final boolean remove;
   private final boolean destroy;

   private ActionType(boolean add, boolean remove, boolean destroy) {
      this.add = add;
      this.remove = remove;
      this.destroy = destroy;
   }

   public boolean isAdd() {
      return this.add;
   }

   public boolean isRemove() {
      return this.remove;
   }

   public boolean isDestroy() {
      return this.destroy;
   }
}
