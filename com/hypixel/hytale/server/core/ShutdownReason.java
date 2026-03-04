package com.hypixel.hytale.server.core;

import javax.annotation.Nonnull;

public class ShutdownReason {
   public static final ShutdownReason SIGINT = new ShutdownReason(130);
   public static final ShutdownReason SHUTDOWN = new ShutdownReason(0);
   public static final ShutdownReason CRASH = new ShutdownReason(1);
   public static final ShutdownReason AUTH_FAILED = new ShutdownReason(2);
   public static final ShutdownReason WORLD_GEN = new ShutdownReason(3);
   public static final ShutdownReason CLIENT_GONE = new ShutdownReason(4);
   public static final ShutdownReason MISSING_REQUIRED_PLUGIN = new ShutdownReason(5);
   public static final ShutdownReason VALIDATE_ERROR = new ShutdownReason(6);
   public static final ShutdownReason MISSING_ASSETS = new ShutdownReason(7);
   public static final ShutdownReason UPDATE = new ShutdownReason(8);
   public static final ShutdownReason MOD_ERROR = new ShutdownReason(9);
   private final int exitCode;
   private final String message;

   public ShutdownReason(int exitCode) {
      this(exitCode, null);
   }

   public ShutdownReason(int exitCode, String message) {
      this.exitCode = exitCode;
      this.message = message;
   }

   public int getExitCode() {
      return this.exitCode;
   }

   public String getMessage() {
      return this.message;
   }

   @Nonnull
   public ShutdownReason withMessage(String message) {
      return new ShutdownReason(this.exitCode, message);
   }

   @Nonnull
   @Override
   public String toString() {
      return "ShutdownReason{exitCode=" + this.exitCode + ", message='" + this.message + "'}";
   }
}
