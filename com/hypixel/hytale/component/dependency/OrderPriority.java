package com.hypixel.hytale.component.dependency;

public enum OrderPriority {
   CLOSEST(-1431655764),
   CLOSE(-715827882),
   NORMAL(0),
   FURTHER(715827882),
   FURTHEST(1431655764);

   private final int value;

   private OrderPriority(final int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }
}
