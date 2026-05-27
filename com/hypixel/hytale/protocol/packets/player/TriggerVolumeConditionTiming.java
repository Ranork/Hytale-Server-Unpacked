package com.hypixel.hytale.protocol.packets.player;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum TriggerVolumeConditionTiming {
   BeforeVolumeDelay(0),
   AfterVolumeDelay(1);

   public static final TriggerVolumeConditionTiming[] VALUES = values();
   private final int value;

   private TriggerVolumeConditionTiming(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static TriggerVolumeConditionTiming fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("TriggerVolumeConditionTiming", value);
      }
   }
}
