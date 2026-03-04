package com.hypixel.hytale.server.core.util.concurrent;

import java.security.Permission;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.annotation.Nonnull;

public class ThreadUtil {
   public static void forceTimeHighResolution() {
      Thread t = new Thread(() -> {
         try {
            while (!Thread.interrupted()) {
               Thread.sleep(Long.MAX_VALUE);
            }
         } catch (InterruptedException var1) {
            Thread.currentThread().interrupt();
         }
      }, "ForceTimeHighResolution");
      t.setDaemon(true);
      t.start();
   }

   public static void createKeepAliveThread(@Nonnull Semaphore alive) {
      Thread t = new Thread(() -> {
         try {
            alive.acquire();
         } catch (InterruptedException var2) {
            Thread.currentThread().interrupt();
         }
      }, "KeepAlive");
      t.setDaemon(false);
      t.start();
   }

   @Nonnull
   public static ExecutorService newCachedThreadPool(int maximumPoolSize, @Nonnull ThreadFactory threadFactory) {
      return new ThreadPoolExecutor(0, maximumPoolSize, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(), threadFactory);
   }

   @Nonnull
   public static ThreadFactory daemon(@Nonnull String name) {
      return r -> {
         Thread t = new Thread(r, name);
         t.setDaemon(true);
         return t;
      };
   }

   @Nonnull
   public static ThreadFactory daemonCounted(@Nonnull String name) {
      AtomicLong count = new AtomicLong();
      return r -> {
         Thread t = new Thread(r, String.format(name, count.incrementAndGet()));
         t.setDaemon(true);
         return t;
      };
   }

   static class ThreadWatcher extends SecurityManager {
      private final Predicate<Thread> predicate;
      private final Consumer<Thread> action;

      public ThreadWatcher(Predicate<Thread> predicate, Consumer<Thread> action) {
         this.predicate = predicate;
         this.action = action;
      }

      @Override
      public void checkPermission(Permission perm) {
      }

      @Override
      public void checkPermission(Permission perm, Object context) {
      }

      @Override
      public void checkAccess(ThreadGroup g) {
         Thread creatingThread = Thread.currentThread();
         if (this.predicate.test(creatingThread)) {
            this.action.accept(creatingThread);
         }
      }
   }
}
