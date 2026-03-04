package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum ComponentUpdateType {
   Nameplate(0),
   UIComponents(1),
   CombatText(2),
   Model(3),
   PlayerSkin(4),
   Item(5),
   Block(6),
   Equipment(7),
   EntityStats(8),
   Transform(9),
   MovementStates(10),
   EntityEffects(11),
   Interactions(12),
   DynamicLight(13),
   Interactable(14),
   Intangible(15),
   Invulnerable(16),
   RespondToHit(17),
   HitboxCollision(18),
   Repulsion(19),
   Prediction(20),
   Audio(21),
   Mounted(22),
   NewSpawn(23),
   ActiveAnimations(24),
   Prop(25);

   public static final ComponentUpdateType[] VALUES = values();
   private final int value;

   private ComponentUpdateType(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static ComponentUpdateType fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("ComponentUpdateType", value);
      }
   }
}
