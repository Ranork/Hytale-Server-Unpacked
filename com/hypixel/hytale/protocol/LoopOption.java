package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum LoopOption {
   PlayOnce(0),
   Loop(1),
   LoopMirror(2);

   public static final LoopOption[] VALUES = values();
   private final int value;

   private LoopOption(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static LoopOption fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("LoopOption", value);
      }
   }
}
