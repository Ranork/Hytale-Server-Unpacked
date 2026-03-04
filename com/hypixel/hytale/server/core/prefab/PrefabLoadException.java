package com.hypixel.hytale.server.core.prefab;

import javax.annotation.Nonnull;

public class PrefabLoadException extends RuntimeException {
   private PrefabLoadException.Type type;

   public PrefabLoadException(@Nonnull PrefabLoadException.Type type) {
      super(type.name());
      this.type = type;
   }

   public PrefabLoadException(PrefabLoadException.Type type, String message) {
      super(message);
      this.type = type;
   }

   public PrefabLoadException(PrefabLoadException.Type type, String message, Throwable cause) {
      super(message, cause);
      this.type = type;
   }

   public PrefabLoadException(PrefabLoadException.Type type, Throwable cause) {
      super(cause);
      this.type = type;
   }

   public PrefabLoadException.Type getType() {
      return this.type;
   }

   public static enum Type {
      ERROR,
      NOT_FOUND;
   }
}
