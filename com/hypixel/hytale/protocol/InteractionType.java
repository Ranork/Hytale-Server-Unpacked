package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum InteractionType {
   Primary(0),
   Secondary(1),
   Ability1(2),
   Ability2(3),
   Ability3(4),
   Use(5),
   Pick(6),
   Pickup(7),
   CollisionEnter(8),
   CollisionLeave(9),
   Collision(10),
   EntityStatEffect(11),
   SwapTo(12),
   SwapFrom(13),
   Death(14),
   Wielding(15),
   ProjectileSpawn(16),
   ProjectileHit(17),
   ProjectileMiss(18),
   ProjectileBounce(19),
   Held(20),
   HeldOffhand(21),
   Equipped(22),
   Dodge(23),
   GameModeSwap(24);

   public static final InteractionType[] VALUES = values();
   private final int value;

   private InteractionType(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static InteractionType fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("InteractionType", value);
      }
   }
}
