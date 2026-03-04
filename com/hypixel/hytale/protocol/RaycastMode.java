package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum RaycastMode {
   FollowMotion(0),
   FollowLook(1);

   public static final RaycastMode[] VALUES = values();
   private final int value;

   private RaycastMode(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static RaycastMode fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("RaycastMode", value);
      }
   }
}
