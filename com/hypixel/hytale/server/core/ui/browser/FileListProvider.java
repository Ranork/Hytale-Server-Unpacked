package com.hypixel.hytale.server.core.ui.browser;

import java.nio.file.Path;
import java.util.List;
import javax.annotation.Nonnull;

@FunctionalInterface
public interface FileListProvider {
   @Nonnull
   List<FileListProvider.FileEntry> getFiles(@Nonnull Path var1, @Nonnull String var2);

   public record FileEntry(@Nonnull String name, @Nonnull String displayName, boolean isDirectory, boolean isTerminal, int matchScore) {
      public FileEntry(@Nonnull String name, boolean isDirectory) {
         this(name, name, isDirectory, false, 0);
      }

      public FileEntry(@Nonnull String name, @Nonnull String displayName, boolean isDirectory) {
         this(name, displayName, isDirectory, false, 0);
      }

      public FileEntry(@Nonnull String name, @Nonnull String displayName, boolean isDirectory, boolean isTerminal) {
         this(name, displayName, isDirectory, isTerminal, 0);
      }
   }
}
