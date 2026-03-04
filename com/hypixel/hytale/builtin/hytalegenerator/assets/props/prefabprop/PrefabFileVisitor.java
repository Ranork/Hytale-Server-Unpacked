package com.hypixel.hytale.builtin.hytalegenerator.assets.props.prefabprop;

import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.PrefabBuffer;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import javax.annotation.Nonnull;

public class PrefabFileVisitor extends SimpleFileVisitor<Path> {
   @Nonnull
   private final List<PrefabBuffer> prefabBuffers;

   public PrefabFileVisitor(@Nonnull List<PrefabBuffer> prefabBuffers) {
      this.prefabBuffers = prefabBuffers;
   }

   @Nonnull
   public FileVisitResult visitFile(@Nonnull Path file, @Nonnull BasicFileAttributes attrs) throws IOException {
      if (!attrs.isRegularFile()) {
         return FileVisitResult.CONTINUE;
      } else {
         PrefabBuffer loadedPrefab = PrefabLoader.loadPrefabBufferAt(file);
         if (loadedPrefab == null) {
            return FileVisitResult.CONTINUE;
         } else {
            this.prefabBuffers.add(loadedPrefab);
            return FileVisitResult.CONTINUE;
         }
      }
   }
}
