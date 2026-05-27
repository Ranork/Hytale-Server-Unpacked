package com.hypixel.hytale.server.core.prefab.selection.standard;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public enum RotateBlockMode {
   ALL,
   NON_UNIFORM,
   NON_FULL_BLOCKS,
   NOTHING;

   @Nonnull
   public static RotateBlockMode fromString(@Nullable String value) {
      if (value == null) {
         return ALL;
      } else {
         return switch (value) {
            case "NonUniform" -> NON_UNIFORM;
            case "NonFullBlocks" -> NON_FULL_BLOCKS;
            case "Nothing" -> NOTHING;
            default -> ALL;
         };
      }
   }
}
