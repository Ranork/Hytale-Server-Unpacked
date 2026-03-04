package com.hypixel.hytale.server.worldgen.util.cache;

import java.lang.ref.Cleaner;
import java.util.concurrent.ScheduledFuture;

public class CleanupFutureAction implements Runnable {
   public static final Cleaner CLEANER = Cleaner.create();
   private final ScheduledFuture<?> future;

   public CleanupFutureAction(ScheduledFuture<?> future) {
      this.future = future;
   }

   @Override
   public void run() {
      this.future.cancel(false);
   }
}
