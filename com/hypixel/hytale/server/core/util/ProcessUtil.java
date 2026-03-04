package com.hypixel.hytale.server.core.util;

public class ProcessUtil {
   public static boolean isProcessRunning(int pid) {
      return ProcessHandle.of(pid).isPresent();
   }
}
