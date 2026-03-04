package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum MouseInputType {
   LookAtTarget(0),
   LookAtTargetBlock(1),
   LookAtTargetEntity(2),
   LookAtPlane(3);

   public static final MouseInputType[] VALUES = values();
   private final int value;

   private MouseInputType(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static MouseInputType fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("MouseInputType", value);
      }
   }
}
