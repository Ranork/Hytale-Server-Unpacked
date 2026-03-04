package com.hypixel.hytale.server.core.inventory.container.filter;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.codecs.EnumCodec;

public enum FilterType {
   ALLOW_INPUT_ONLY(true, false),
   ALLOW_OUTPUT_ONLY(false, true),
   ALLOW_ALL(true, true),
   DENY_ALL(false, false);

   public static final Codec<FilterType> CODEC = new EnumCodec<>(FilterType.class);
   private final boolean input;
   private final boolean output;

   private FilterType(boolean input, boolean output) {
      this.input = input;
      this.output = output;
   }

   public boolean allowInput() {
      return this.input;
   }

   public boolean allowOutput() {
      return this.output;
   }
}
