package com.hypixel.hytale.server.worldgen.loader.util;

import javax.annotation.Nonnull;

public class ColorUtil {
   public static int hexString(@Nonnull String s) {
      return Integer.parseInt(s.replace("#", "").replace("0x", ""), 16) & 16777215;
   }
}
