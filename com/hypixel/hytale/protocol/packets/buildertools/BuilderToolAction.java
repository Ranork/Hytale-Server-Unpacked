package com.hypixel.hytale.protocol.packets.buildertools;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum BuilderToolAction {
   SelectionPosition1(0),
   SelectionPosition2(1),
   SelectionCopy(2),
   HistoryUndo(3),
   HistoryRedo(4),
   ActivateToolMode(5),
   DeactivateToolMode(6);

   public static final BuilderToolAction[] VALUES = values();
   private final int value;

   private BuilderToolAction(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static BuilderToolAction fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("BuilderToolAction", value);
      }
   }
}
