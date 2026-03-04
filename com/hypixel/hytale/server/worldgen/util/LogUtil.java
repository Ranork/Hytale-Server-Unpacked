package com.hypixel.hytale.server.worldgen.util;

import com.hypixel.hytale.logger.HytaleLogger;
import javax.annotation.Nonnull;

public class LogUtil {
   private static final HytaleLogger LOGGER = HytaleLogger.get("WorldGenerator");

   @Nonnull
   public static HytaleLogger getLogger() {
      return LOGGER;
   }
}
