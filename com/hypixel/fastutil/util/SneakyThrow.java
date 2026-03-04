package com.hypixel.fastutil.util;

public class SneakyThrow {
   public static RuntimeException sneakyThrow(Throwable t) {
      if (t == null) {
         throw new NullPointerException("t");
      } else {
         return sneakyThrow0(t);
      }
   }

   private static <T extends Throwable> T sneakyThrow0(Throwable t) throws T {
      throw t;
   }
}
