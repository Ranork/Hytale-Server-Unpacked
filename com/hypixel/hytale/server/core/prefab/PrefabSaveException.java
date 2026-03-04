package com.hypixel.hytale.server.core.prefab;

public class PrefabSaveException extends RuntimeException {
   private PrefabSaveException.Type type;

   public PrefabSaveException(PrefabSaveException.Type type) {
      this.type = type;
   }

   public PrefabSaveException(PrefabSaveException.Type type, String message) {
      super(message);
      this.type = type;
   }

   public PrefabSaveException(PrefabSaveException.Type type, String message, Throwable cause) {
      super(message, cause);
      this.type = type;
   }

   public PrefabSaveException(PrefabSaveException.Type type, Throwable cause) {
      super(cause);
      this.type = type;
   }

   public PrefabSaveException.Type getType() {
      return this.type;
   }

   public static enum Type {
      ERROR,
      ALREADY_EXISTS;
   }
}
