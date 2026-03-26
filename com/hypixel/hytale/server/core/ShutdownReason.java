package com.hypixel.hytale.server.core;

import com.hypixel.hytale.protocol.FormattedMessage;
import com.hypixel.hytale.server.core.util.MessageUtil;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
   public static final ShutdownReason VERIFY_ERROR = new ShutdownReason(10);
   private final int exitCode;
   @Nullable
   private final FormattedMessage message;

   public ShutdownReason(int exitCode) {
      this(exitCode, null);
   }

   public ShutdownReason(int exitCode, @Nullable FormattedMessage message) {
      this.exitCode = exitCode;
      this.message = message;
   }

   public int getExitCode() {
      return this.exitCode;
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
      return new ShutdownReason(this.exitCode, message.getFormattedMessage());
   }

   @Deprecated
   @Nonnull
   public ShutdownReason withMessage(@Nonnull String message) {
      return this.withMessage(Message.raw(message));
   }

   @Nonnull
   @Override
   public String toString() {
      return "ShutdownReason{exitCode=" + this.exitCode + ", message='" + this.getMessage() + "'}";
   }
}
