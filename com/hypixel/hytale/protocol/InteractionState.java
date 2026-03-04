package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum InteractionState {
   Finished(0),
   Skip(1),
   ItemChanged(2),
   Failed(3),
   NotFinished(4);

   public static final InteractionState[] VALUES = values();
   private final int value;

   private InteractionState(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static InteractionState fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("InteractionState", value);
      }
   }
}
