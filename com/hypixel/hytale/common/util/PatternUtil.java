package com.hypixel.hytale.common.util;

import javax.annotation.Nonnull;

public class PatternUtil {
   @Nonnull
   public static String replaceBackslashWithForwardSlash(@Nonnull String name) {
      return name.replace("\\", "/");
   }
}
