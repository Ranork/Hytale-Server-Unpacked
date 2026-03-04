package com.hypixel.hytale.server.core.asset.monitor;

import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent.Kind;
import javax.annotation.Nonnull;

public enum EventKind {
   ENTRY_CREATE,
   ENTRY_DELETE,
   ENTRY_MODIFY;

   @Nonnull
   public static EventKind parse(Kind<Path> kind) {
      if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
         return ENTRY_CREATE;
      } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
         return ENTRY_DELETE;
      } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
         return ENTRY_MODIFY;
      } else {
         throw new IllegalStateException("Unknown type: " + kind);
      }
   }
}
