package com.hypixel.hytale.server.npc.util.expression;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface Scope {
   Supplier<String> getStringSupplier(String var1);

   DoubleSupplier getNumberSupplier(String var1);

   BooleanSupplier getBooleanSupplier(String var1);

   Supplier<String[]> getStringArraySupplier(String var1);

   Supplier<double[]> getNumberArraySupplier(String var1);

   Supplier<boolean[]> getBooleanArraySupplier(String var1);

   Scope.Function getFunction(String var1);

   default String getString(String name) {
      return this.getStringSupplier(name).get();
   }

   default double getNumber(String name) {
      return this.getNumberSupplier(name).getAsDouble();
   }

   default boolean getBoolean(String name) {
      return this.getBooleanSupplier(name).getAsBoolean();
   }

   default String[] getStringArray(String name) {
      return this.getStringArraySupplier(name).get();
   }

   default double[] getNumberArray(String name) {
      return this.getNumberArraySupplier(name).get();
   }

   default boolean[] getBooleanArray(String name) {
      return this.getBooleanArraySupplier(name).get();
   }

   boolean isConstant(String var1);

   @Nullable
   ValueType getType(String var1);

   @Nonnull
   static String encodeFunctionName(@Nonnull String name, @Nonnull ValueType[] values) {
      StringBuilder stringBuilder = new StringBuilder(name).append('@');

      for (int i = 0; i < values.length; i++) {
         stringBuilder.append(encodeType(values[i]));
      }

      return stringBuilder.toString();
   }

   static char encodeType(@Nonnull ValueType type) {
      return switch (type) {
         case NUMBER -> 'n';
         case STRING -> 's';
         case BOOLEAN -> 'b';
         case NUMBER_ARRAY -> 'N';
         case STRING_ARRAY -> 'S';
         case BOOLEAN_ARRAY -> 'B';
         default -> throw new IllegalStateException("Type cannot be encoded for function name: " + type);
      };
   }

   @FunctionalInterface
   public interface Function {
      void call(ExecutionContext var1, int var2);
   }
}
