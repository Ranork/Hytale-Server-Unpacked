package com.hypixel.hytale.server.worldgen.util.cache;

import com.hypixel.hytale.logger.HytaleLogger;
import java.lang.ref.WeakReference;
import java.util.logging.Level;

public class CleanupRunnable<K, V> implements Runnable {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private final WeakReference<Cache<K, V>> reference;

   public CleanupRunnable(WeakReference<Cache<K, V>> reference) {
      this.reference = reference;
   }

   @Override
   public void run() {
      try {
         Cache<K, V> cache = this.reference.get();
         if (cache != null) {
            cache.cleanup();
         }
      } catch (Exception var2) {
         ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(var2)).log("Failed to run cache cleanup!");
      }
   }
}
