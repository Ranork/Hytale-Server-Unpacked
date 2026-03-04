package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum CameraNode {
   None(0),
   Head(1),
   LShoulder(2),
   RShoulder(3),
   Belly(4);

   public static final CameraNode[] VALUES = values();
   private final int value;

   private CameraNode(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static CameraNode fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("CameraNode", value);
      }
   }
}
