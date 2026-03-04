package com.hypixel.hytale.sneakythrow;

@FunctionalInterface
public interface ThrowableRunnable<E extends Throwable> extends Runnable {
   @Override
   default void run() {
      try {
         this.runNow();
      } catch (Throwable var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   void runNow() throws E;
}
