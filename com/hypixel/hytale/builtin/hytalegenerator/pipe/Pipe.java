package com.hypixel.hytale.builtin.hytalegenerator.pipe;

import javax.annotation.Nonnull;

public class Pipe {
   public static final Pipe.One<?> EMPTY_ONE = (a, c) -> {};
   public static final Pipe.Two<?, ?> EMPTY_TWO = (a, b, c) -> {};

   public static <Input> Pipe.One<Input> getEmptyOne() {
      return (Pipe.One<Input>)EMPTY_ONE;
   }

   public static <InputA, InputB> Pipe.Two<InputA, InputB> getEmptyTwo() {
      return (Pipe.Two<InputA, InputB>)EMPTY_TWO;
   }

   @FunctionalInterface
   public interface One<Input> {
      void accept(@Nonnull Input var1, @Nonnull Control var2);
   }

   @FunctionalInterface
   public interface Two<InputA, InputB> {
      void accept(@Nonnull InputA var1, @Nonnull InputB var2, @Nonnull Control var3);
   }
}
