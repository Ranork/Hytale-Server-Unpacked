package com.hypixel.hytale.server.core.asset.monitor;

import com.hypixel.hytale.common.util.SystemUtil;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.util.io.FileUtil;
import com.hypixel.hytale.sneakythrow.SneakyThrow;
import com.sun.nio.file.ExtendedWatchEventModifier;
import com.sun.nio.file.SensitivityWatchEventModifier;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

public class PathWatcherThread implements Runnable {
   public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   public static final boolean HAS_FILE_TREE_SUPPORT = SystemUtil.TYPE == SystemUtil.SystemType.WINDOWS;
   public static final Kind<?>[] WATCH_EVENT_KINDS = new Kind[]{
      StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY
   };
   private final BiConsumer<Path, EventKind> consumer;
   @Nonnull
   private final Thread thread;
   private final WatchService service;
   private final Map<Path, WatchKey> registered = new ConcurrentHashMap<>();

   public PathWatcherThread(BiConsumer<Path, EventKind> consumer) throws IOException {
      this.consumer = consumer;
      this.thread = new Thread(this, "PathWatcher");
      this.thread.setDaemon(true);
      this.service = FileSystems.getDefault().newWatchService();
   }

   @Override
   public final void run() {
      try {
         while (!Thread.interrupted()) {
            WatchKey key = this.service.take();
            Path directory = (Path)key.watchable();

            for (WatchEvent<?> event : key.pollEvents()) {
               Kind<?> kind = event.kind();
               if (kind == StandardWatchEventKinds.OVERFLOW) {
                  LOGGER.at(Level.WARNING)
                     .log(
                        "Event Overflow, Unable to detect all file changed! This may cause server instability!! More than AbstractWatchKey.MAX_EVENT_LIST_SIZE queued events (512)"
                     );
               } else {
                  Path path = directory.resolve((Path)event.context());
                  if (!HAS_FILE_TREE_SUPPORT && event.kind() == StandardWatchEventKinds.ENTRY_CREATE && Files.isDirectory(path)) {
                     this.addPath(path);
                  }

                  this.consumer.accept(path, EventKind.parse((Kind<Path>)event.kind()));
               }
            }

            if (!key.reset()) {
               break;
            }
         }
      } catch (InterruptedException var9) {
         Thread.currentThread().interrupt();
      } catch (Throwable var10) {
         ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(var10)).log("Exception occurred when polling:");
      }

      LOGGER.at(Level.INFO).log("Stopped polling for changes in assets. Server will need to be rebooted to load changes!");

      try {
         this.service.close();
      } catch (IOException var8) {
      }

      this.registered.clear();
   }

   public void start() {
      this.thread.start();
   }

   public void shutdown() {
      this.thread.interrupt();

      try {
         this.thread.join(1000L);

         try {
            this.service.close();
         } catch (IOException var2) {
         }

         this.registered.clear();
      } catch (InterruptedException var3) {
         Thread.currentThread().interrupt();
      }
   }

   public void addPath(Path path) throws IOException {
      path = path.toAbsolutePath();
      if (Files.isRegularFile(path)) {
         path = path.getParent();
      }

      Path parent = path;

      do {
         WatchKey keys = this.registered.get(parent);
         if (keys != null) {
            if (!HAS_FILE_TREE_SUPPORT) {
               this.watchPath(path);
            }

            return;
         }
      } while ((parent = parent.getParent()) != null);

      this.watchPath(path);
   }

   private void watchPath(@Nonnull Path path) throws IOException {
      if (HAS_FILE_TREE_SUPPORT) {
         this.registered.put(path, path.register(this.service, WATCH_EVENT_KINDS, SensitivityWatchEventModifier.HIGH, ExtendedWatchEventModifier.FILE_TREE));
         LOGGER.at(Level.FINEST).log("Register path: %s", path);
      } else {
         Modifier[] modifiers = new Modifier[]{SensitivityWatchEventModifier.HIGH};

         try (Stream<Path> stream = Files.walk(path, FileUtil.DEFAULT_WALK_TREE_OPTIONS_ARRAY)) {
            stream.forEach(SneakyThrow.sneakyConsumer(childPath -> {
               if (Files.isDirectory(childPath)) {
                  this.registered.put(childPath, childPath.register(this.service, WATCH_EVENT_KINDS, modifiers));
                  LOGGER.at(Level.FINEST).log("Register path: %s", childPath);
               }
            }));
         }
      }

      LOGGER.at(Level.FINER).log("Watching path: %s", path);
   }
}
