package com.hypixel.hytale.server.core.universe;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.StampedLock;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StorageManager {
   private final BooleanSupplier isSavingLocked;
   private final StampedLock lock = new StampedLock();
   private final Map<Path, StorageManager.PendingOp> pendingOps = new HashMap<>();

   public StorageManager(BooleanSupplier isSavingLocked) {
      this.isSavingLocked = isSavingLocked;
   }

   public boolean hasQueuedSave(@Nonnull Path path) {
      path = path.normalize();
      long stamp = this.lock.readLock();

      boolean var5;
      try {
         StorageManager.PendingOp pending = this.pendingOps.get(path);
         var5 = pending != null && pending.activeLoads == 0;
      } finally {
         this.lock.unlockRead(stamp);
      }

      return var5;
   }

   private void clearSave(@Nonnull Path path) {
      long stamp = this.lock.writeLock();

      try {
         this.pendingOps.remove(path);
      } finally {
         this.lock.unlockWrite(stamp);
      }
   }

   public CompletableFuture<Void> doSave(@Nonnull Path path, @Nonnull Supplier<CompletableFuture<Void>> saveFunc) {
      path = path.normalize();
      long stamp = this.lock.writeLock();

      CompletableFuture<Void> completedFuture;
      try {
         StorageManager.PendingOp pending = this.pendingOps.get(path);
         if (pending == null) {
            completedFuture = new CompletableFuture<>();
            if (this.isSavingLocked.getAsBoolean()) {
               pending = new StorageManager.PendingOp(completedFuture, saveFunc);
            } else {
               saveFunc.get().whenCompleteAsync((v, e) -> {
                  this.clearSave(path);
                  if (e != null) {
                     completedFuture.completeExceptionally(e);
                  } else {
                     completedFuture.complete(v);
                  }
               });
               pending = new StorageManager.PendingOp(completedFuture, null);
            }

            this.pendingOps.put(path, pending);
            return completedFuture;
         }

         if (pending.doSave == null) {
            return pending.onComplete = pending.onComplete.thenCompose(v -> this.doSave(path, saveFunc));
         }

         pending.doSave = saveFunc;
         completedFuture = pending.onComplete;
      } finally {
         this.lock.unlockWrite(stamp);
      }

      return completedFuture;
   }

   public <T> CompletableFuture<T> doLoad(@Nonnull Path path, @Nonnull Supplier<CompletableFuture<T>> loadFunc) {
      path = path.normalize();
      long stamp = this.lock.writeLock();

      CompletableFuture<Void> loadBarrier;
      try {
         StorageManager.PendingOp pending = this.pendingOps.get(path);
         if (pending == null || pending.activeLoads != 0) {
            CompletableFuture<T> loadResult = loadFunc.get();
            if (pending == null) {
               loadBarrier = new CompletableFuture<>();
               pending = new StorageManager.PendingOp(loadBarrier, null);
               pending.loadBarrier = loadBarrier;
               pending.activeLoads = 1;
               this.pendingOps.put(path, pending);
            } else {
               pending.activeLoads++;
            }

            StorageManager.PendingOp finalPending = pending;
            loadResult.whenCompleteAsync((v, e) -> {
               CompletableFuture<Void> barrier = null;
               long s = this.lock.writeLock();

               try {
                  finalPending.activeLoads--;
                  if (finalPending.activeLoads == 0) {
                     if (this.pendingOps.get(path) == finalPending) {
                        this.pendingOps.remove(path);
                     }

                     barrier = finalPending.loadBarrier;
                  }
               } finally {
                  this.lock.unlockWrite(s);
               }

               if (barrier != null) {
                  barrier.complete(null);
               }
            });
            return loadResult;
         }

         loadBarrier = pending.onComplete.thenCompose(v -> this.doLoad(path, loadFunc));
      } finally {
         this.lock.unlockWrite(stamp);
      }

      return (CompletableFuture<T>)loadBarrier;
   }

   public CompletableFuture<Void> pendingOperations() {
      long stamp = this.lock.readLock();

      CompletableFuture var9;
      try {
         ArrayList<CompletableFuture<Void>> futures = new ArrayList<>();

         for (StorageManager.PendingOp pending : this.pendingOps.values()) {
            if (pending.doSave == null && pending.activeLoads <= 0) {
               futures.add(pending.onComplete);
            }
         }

         var9 = CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
      } finally {
         this.lock.unlockRead(stamp);
      }

      return var9;
   }

   public void onSavingRelease() {
      long stamp = this.lock.writeLock();

      try {
         for (Entry<Path, StorageManager.PendingOp> e : this.pendingOps.entrySet()) {
            Path path = e.getKey();
            StorageManager.PendingOp pending = e.getValue();
            if (pending.doSave != null) {
               Supplier<CompletableFuture<Void>> save = pending.doSave;
               pending.doSave = null;
               CompletableFuture<Void> completedFuture = pending.onComplete;
               save.get().whenCompleteAsync((v, ex) -> {
                  this.clearSave(path);
                  if (ex != null) {
                     completedFuture.completeExceptionally(ex);
                  } else {
                     completedFuture.complete(v);
                  }
               });
            }
         }
      } finally {
         this.lock.unlockWrite(stamp);
      }
   }

   private static final class PendingOp {
      @Nonnull
      private CompletableFuture<Void> onComplete;
      @Nullable
      private Supplier<CompletableFuture<Void>> doSave;
      @Nullable
      private CompletableFuture<Void> loadBarrier;
      private int activeLoads;

      private PendingOp(@Nonnull CompletableFuture<Void> onComplete, @Nullable Supplier<CompletableFuture<Void>> doSave) {
         this.onComplete = onComplete;
         this.doSave = doSave;
      }
   }
}
