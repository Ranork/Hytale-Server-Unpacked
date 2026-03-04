package com.hypixel.hytale.server.core.prefab.selection;

import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nullable;

public final class SelectionManager {
   private static final AtomicReference<SelectionProvider> SELECTION_PROVIDER = new AtomicReference<>();

   private SelectionManager() {
   }

   public static void setSelectionProvider(SelectionProvider provider) {
      SELECTION_PROVIDER.set(provider);
   }

   @Nullable
   public static SelectionProvider getSelectionProvider() {
      return SELECTION_PROVIDER.get();
   }
}
