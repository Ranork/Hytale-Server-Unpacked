package com.hypixel.hytale.builtin.hytalegenerator.assets.props.prefabprop;

import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.IPrefabBuffer;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.BiConsumer;
import javax.annotation.Nonnull;

public class PrefabFileVisitor extends SimpleFileVisitor<Path> {
   @Nonnull
   private final BiConsumer<Path, IPrefabBuffer> consumer;

   public PrefabFileVisitor(@Nonnull BiConsumer<Path, IPrefabBuffer> consumer) {
      this.consumer = consumer;
   }

   @Nonnull
   public FileVisitResult visitFile(@Nonnull Path path, @Nonnull BasicFileAttributes attrs) throws IOException {
      if (!attrs.isRegularFile()) {
         return FileVisitResult.CONTINUE;
      } else {
         IPrefabBuffer loadedPrefab = PrefabLoader.loadPrefabBufferAt(path);
         if (loadedPrefab == null) {
            return FileVisitResult.CONTINUE;
         } else {
            this.consumer.accept(path, loadedPrefab);
            return FileVisitResult.CONTINUE;
         }
      }
   }
}
