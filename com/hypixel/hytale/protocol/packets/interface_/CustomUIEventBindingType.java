package com.hypixel.hytale.protocol.packets.interface_;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum CustomUIEventBindingType {
   Activating(0),
   RightClicking(1),
   DoubleClicking(2),
   MouseEntered(3),
   MouseExited(4),
   ValueChanged(5),
   ElementReordered(6),
   Validating(7),
   Dismissing(8),
   FocusGained(9),
   FocusLost(10),
   KeyDown(11),
   MouseButtonReleased(12),
   SlotClicking(13),
   SlotDoubleClicking(14),
   SlotMouseEntered(15),
   SlotMouseExited(16),
   DragCancelled(17),
   Dropped(18),
   SlotMouseDragCompleted(19),
   SlotMouseDragExited(20),
   SlotClickReleaseWhileDragging(21),
   SlotClickPressWhileDragging(22),
   SelectedTabChanged(23);

   public static final CustomUIEventBindingType[] VALUES = values();
   private final int value;

   private CustomUIEventBindingType(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static CustomUIEventBindingType fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("CustomUIEventBindingType", value);
      }
   }
}
