package com.hypixel.hytale.server.core.universe.world.meta.state.exceptions;

public class NoSuchBlockStateException extends Exception {
   public NoSuchBlockStateException(String message) {
      super(message);
   }

   public NoSuchBlockStateException(Throwable cause) {
      super(cause);
   }
}
