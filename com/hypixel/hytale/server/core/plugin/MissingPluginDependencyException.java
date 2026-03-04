package com.hypixel.hytale.server.core.plugin;

import javax.annotation.Nonnull;

public class MissingPluginDependencyException extends RuntimeException {
   public MissingPluginDependencyException(@Nonnull String message) {
      super(message);
   }
}
