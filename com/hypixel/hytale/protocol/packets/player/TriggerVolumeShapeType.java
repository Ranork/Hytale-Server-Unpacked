package com.hypixel.hytale.protocol.packets.player;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum TriggerVolumeShapeType {
   Box(0),
   Sphere(1),
   Cylinder(2);

   public static final TriggerVolumeShapeType[] VALUES = values();
   private final int value;

   private TriggerVolumeShapeType(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static TriggerVolumeShapeType fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("TriggerVolumeShapeType", value);
      }
   }
}
