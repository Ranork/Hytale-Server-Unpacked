package com.hypixel.hytale.logger.util;

import javax.annotation.Nonnull;

public class GithubMessageUtil {
   private static final String CI = System.getenv("CI");

   public static boolean isGithub() {
      return CI != null;
   }

   @Nonnull
   public static String messageError(@Nonnull String file, int line, int column, @Nonnull String message) {
      return "::error file=%s,line=%d,col=%d::%s\n".formatted(file, line, column, message.replace("\n", "%0A"));
   }

   @Nonnull
   public static String messageError(@Nonnull String file, @Nonnull String message) {
      return "::error file=%s::%s\n".formatted(file, message.replace("\n", "%0A"));
   }

   @Nonnull
   public static String messageWarning(@Nonnull String file, int line, int column, @Nonnull String message) {
      return "::warning file=%s,line=%d,col=%d::%s\n".formatted(file, line, column, message.replace("\n", "%0A"));
   }

   @Nonnull
   public static String messageWarning(@Nonnull String file, @Nonnull String message) {
      return "::warning file=%s::%s\n".formatted(file, message.replace("\n", "%0A"));
   }
}
