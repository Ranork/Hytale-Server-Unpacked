package com.hypixel.hytale.event;

public enum EventPriority {
   FIRST((short)-21844),
   EARLY((short)-10922),
   NORMAL((short)0),
   LATE((short)10922),
   LAST((short)21844);

   private final short value;

   private EventPriority(final short value) {
      this.value = value;
   }

   public short getValue() {
      return this.value;
   }
}
