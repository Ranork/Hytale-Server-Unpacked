package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum CameraActionType {
   ForcePerspective(0),
   Orbit(1),
   Transition(2);

   public static final CameraActionType[] VALUES = values();
   private final int value;

   private CameraActionType(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static CameraActionType fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("CameraActionType", value);
      }
   }
}
