package com.hypixel.hytale.server.core.universe.world.worldmap;

import com.hypixel.hytale.common.util.ExceptionUtil;
import java.util.Objects;
import javax.annotation.Nonnull;

public class WorldMapLoadException extends Exception {
   public WorldMapLoadException(@Nonnull String message) {
      super(Objects.requireNonNull(message));
   }

   public WorldMapLoadException(@Nonnull String message, Throwable cause) {
      super(Objects.requireNonNull(message), cause);
   }

   @Nonnull
   public String getTraceMessage() {
      return this.getTraceMessage(", ");
   }

   @Nonnull
   public String getTraceMessage(@Nonnull String joiner) {
      return ExceptionUtil.combineMessages(this, joiner);
   }
}
