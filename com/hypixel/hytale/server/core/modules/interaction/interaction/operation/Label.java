package com.hypixel.hytale.server.core.modules.interaction.interaction.operation;

import javax.annotation.Nonnull;

public class Label {
   protected int index;

   protected Label(int index) {
      this.index = index;
   }

   public int getIndex() {
      return this.index;
   }

   @Nonnull
   @Override
   public String toString() {
      return "Label{index=" + this.index + "}";
   }
}
