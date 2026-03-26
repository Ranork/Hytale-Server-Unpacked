package com.hypixel.hytale.protocol.packets.voice;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum VoiceCodec {
   Opus(0);

   public static final VoiceCodec[] VALUES = values();
   private final int value;

   private VoiceCodec(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static VoiceCodec fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("VoiceCodec", value);
      }
   }
}
