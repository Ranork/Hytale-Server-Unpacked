package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum Cosmetic {
   Haircut(0),
   FacialHair(1),
   Undertop(2),
   Overtop(3),
   Pants(4),
   Overpants(5),
   Shoes(6),
   Gloves(7),
   Cape(8),
   HeadAccessory(9),
   FaceAccessory(10),
   EarAccessory(11),
   Ear(12);

   public static final Cosmetic[] VALUES = values();
   private final int value;

   private Cosmetic(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static Cosmetic fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("Cosmetic", value);
      }
   }
}
