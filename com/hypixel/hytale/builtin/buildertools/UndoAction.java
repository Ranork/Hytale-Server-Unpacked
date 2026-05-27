package com.hypixel.hytale.builtin.buildertools;

import com.hypixel.hytale.server.core.Message;
import javax.annotation.Nonnull;

public interface UndoAction {
   @Nonnull
   String id();

   @Nonnull
   Message toMessage();

   default boolean marksPrefabDirty() {
      return true;
   }
}
