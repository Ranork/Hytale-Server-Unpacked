package com.hypixel.hytale.protocol;

public enum NetworkChannel {
   Default(0),
   Chunks(1),
   WorldMap(2);

   public static final NetworkChannel[] VALUES = values();
   public static final int COUNT = VALUES.length;
   private final int value;

   private NetworkChannel(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static NetworkChannel fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw new IllegalArgumentException("Invalid network channel: " + value);
      }
   }
}
