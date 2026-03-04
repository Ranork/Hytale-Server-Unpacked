package com.hypixel.hytale.common.util;

import javax.annotation.Nonnull;

public class SystemUtil {
   public static final SystemUtil.SystemType TYPE = getSystemType();

   @Nonnull
   private static SystemUtil.SystemType getSystemType() {
      String osName = System.getProperty("os.name");
      if (osName.startsWith("Windows")) {
         return SystemUtil.SystemType.WINDOWS;
      } else if (osName.startsWith("Mac OS X")) {
         return SystemUtil.SystemType.MACOS;
      } else if (osName.startsWith("Linux")) {
         return SystemUtil.SystemType.LINUX;
      } else {
         return osName.startsWith("LINUX") ? SystemUtil.SystemType.LINUX : SystemUtil.SystemType.OTHER;
      }
   }

   public static boolean getEnvBoolean(@Nonnull String name) {
      String value = System.getenv(name);
      return value == null ? false : !"false".equalsIgnoreCase(value) && !"0".equals(value) && !"no".equalsIgnoreCase(value);
   }

   public static enum SystemType {
      WINDOWS,
      MACOS,
      LINUX,
      OTHER;
   }
}
