package com.hypixel.hytale.protocol.io;

import javax.annotation.Nonnull;

public class ProtocolException extends RuntimeException {
   public ProtocolException(@Nonnull String message) {
      super(message);
   }

   public ProtocolException(@Nonnull String message, @Nonnull Throwable cause) {
      super(message, cause);
   }

   @Nonnull
   public static ProtocolException arrayTooLong(@Nonnull String fieldName, int actual, int max) {
      return new ProtocolException(fieldName + ": array length " + actual + " exceeds maximum " + max);
   }

   @Nonnull
   public static ProtocolException stringTooLong(@Nonnull String fieldName, int actual, int max) {
      return new ProtocolException(fieldName + ": string length " + actual + " exceeds maximum " + max);
   }

   @Nonnull
   public static ProtocolException dictionaryTooLarge(@Nonnull String fieldName, int actual, int max) {
      return new ProtocolException(fieldName + ": dictionary count " + actual + " exceeds maximum " + max);
   }

   @Nonnull
   public static ProtocolException bufferTooSmall(@Nonnull String fieldName, int required, int available) {
      return new ProtocolException(fieldName + ": buffer too small, need " + required + " bytes but only " + available + " available");
   }

   @Nonnull
   public static ProtocolException invalidVarInt(@Nonnull String fieldName) {
      return new ProtocolException(fieldName + ": invalid or incomplete VarInt");
   }

   @Nonnull
   public static ProtocolException negativeLength(@Nonnull String fieldName, int value) {
      return new ProtocolException(fieldName + ": negative length " + value);
   }

   @Nonnull
   public static ProtocolException invalidOffset(@Nonnull String fieldName, int offset, int bufferLength) {
      return new ProtocolException(fieldName + ": offset " + offset + " is out of bounds (buffer length: " + bufferLength + ")");
   }

   @Nonnull
   public static ProtocolException unknownPolymorphicType(@Nonnull String typeName, int typeId) {
      return new ProtocolException(typeName + ": unknown polymorphic type ID " + typeId);
   }

   @Nonnull
   public static ProtocolException duplicateKey(@Nonnull String fieldName, @Nonnull Object key) {
      return new ProtocolException(fieldName + ": duplicate key '" + key + "'");
   }

   @Nonnull
   public static ProtocolException invalidEnumValue(@Nonnull String enumName, int value) {
      return new ProtocolException(enumName + ": invalid enum value " + value);
   }

   @Nonnull
   public static ProtocolException arrayTooShort(@Nonnull String fieldName, int actual, int min) {
      return new ProtocolException(fieldName + ": array length " + actual + " is below minimum " + min);
   }

   @Nonnull
   public static ProtocolException stringTooShort(@Nonnull String fieldName, int actual, int min) {
      return new ProtocolException(fieldName + ": string length " + actual + " is below minimum " + min);
   }

   @Nonnull
   public static ProtocolException dictionaryTooSmall(@Nonnull String fieldName, int actual, int min) {
      return new ProtocolException(fieldName + ": dictionary count " + actual + " is below minimum " + min);
   }

   @Nonnull
   public static ProtocolException valueOutOfRange(@Nonnull String fieldName, @Nonnull Object value, double min, double max) {
      return new ProtocolException(fieldName + ": value " + value + " is outside allowed range [" + min + ", " + max + "]");
   }

   @Nonnull
   public static ProtocolException valueBelowMinimum(@Nonnull String fieldName, @Nonnull Object value, double min) {
      return new ProtocolException(fieldName + ": value " + value + " is below minimum " + min);
   }

   @Nonnull
   public static ProtocolException valueAboveMaximum(@Nonnull String fieldName, @Nonnull Object value, double max) {
      return new ProtocolException(fieldName + ": value " + value + " exceeds maximum " + max);
   }
}
