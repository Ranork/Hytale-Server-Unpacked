package com.hypixel.hytale.builtin.adventure.memories.memories;

import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.server.core.Message;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class Memory {
   @Nonnull
   public static final CodecMapCodec<Memory> CODEC = new CodecMapCodec<>();

   public abstract String getId();

   public abstract String getTitle();

   public abstract Message getTooltipText();

   @Nullable
   public abstract String getIconPath();

   public abstract Message getUndiscoveredTooltipText();

   @Override
   public boolean equals(@Nullable Object o) {
      return this == o || o != null && this.getClass() == o.getClass();
   }

   @Override
   public int hashCode() {
      return this.getClass().hashCode();
   }

   @Nonnull
   @Override
   public String toString() {
      return "Memory{}";
   }
}
