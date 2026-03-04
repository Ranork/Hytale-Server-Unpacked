package com.hypixel.hytale.plugin.early;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ClassTransformer {
   default int priority() {
      return 0;
   }

   @Nullable
   byte[] transform(@Nonnull String var1, @Nonnull String var2, @Nonnull byte[] var3);
}
