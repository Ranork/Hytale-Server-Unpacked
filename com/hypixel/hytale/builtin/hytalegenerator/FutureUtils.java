package com.hypixel.hytale.builtin.hytalegenerator;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public class FutureUtils {
   @Nonnull
   public static <T> CompletableFuture<Void> allOf(@Nonnull List<CompletableFuture<T>> tasks) {
      return CompletableFuture.allOf(tasks.toArray(new CompletableFuture[tasks.size()]));
   }
}
