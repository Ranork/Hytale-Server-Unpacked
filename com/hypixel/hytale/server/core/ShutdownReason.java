package com.hypixel.hytale.server.core;

import com.hypixel.hytale.protocol.FormattedMessage;
import com.hypixel.hytale.server.core.util.MessageUtil;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ShutdownReason {
   public static final ShutdownReason SIGINT = new ShutdownReason(130, "sigint");
   public static final ShutdownReason SHUTDOWN = new ShutdownReason(0, "normal");
   public static final ShutdownReason CRASH = new ShutdownReason(1, "crash");
   public static final ShutdownReason AUTH_FAILED = new ShutdownReason(2, "auth_failed");
   public static final ShutdownReason WORLD_GEN = new ShutdownReason(3, "world_gen_error");
   public static final ShutdownReason CLIENT_GONE = new ShutdownReason(4, "client_gone");
   public static final ShutdownReason MISSING_REQUIRED_PLUGIN = new ShutdownReason(5, "missing_plugin");
   public static final ShutdownReason VALIDATE_ERROR = new ShutdownReason(6, "validation_error");
   public static final ShutdownReason MISSING_ASSETS = new ShutdownReason(7, "missing_assets");
   public static final ShutdownReason UPDATE = new ShutdownReason(8, "update");
   public static final ShutdownReason MOD_ERROR = new ShutdownReason(9, "mod_error");
   public static final ShutdownReason VERIFY_ERROR = new ShutdownReason(10, "verify_error");
   private final int exitCode;
   @Nonnull
   private final String name;
   @Nullable
   private final FormattedMessage message;

   public ShutdownReason(int exitCode, @Nonnull String name) {
      this(exitCode, name, null);
   }

   public ShutdownReason(int exitCode, @Nonnull String name, @Nullable FormattedMessage message) {
      this.exitCode = exitCode;
      this.name = name;
      this.message = message;
   }

   public int getExitCode() {
      return this.exitCode;
   }

   @Nonnull
   public String getName() {
      return this.name;
   }

   @Nullable
   public String getMessage() {
      return this.message != null ? MessageUtil.formatMessageToPlainString(this.message) : null;
   }

   @Nullable
   public FormattedMessage getFormattedMessage() {
      return this.message;
   }

   @Nonnull
   public ShutdownReason withMessage(@Nonnull Message message) {
      return new ShutdownReason(this.exitCode, this.name, message.getFormattedMessage());
   }

   public String toTelemetryString() {
      return this.message != null ? this.name + ": " + this.getMessage() : this.name;
   }

   @Override
   public String toString() {
      return "ShutdownReason{exitCode=" + this.exitCode + ", name='" + this.name + "', message=" + this.getMessage() + "}";
   }
}
