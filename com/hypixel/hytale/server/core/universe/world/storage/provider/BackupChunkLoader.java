package com.hypixel.hytale.server.core.universe.world.storage.provider;

import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.IChunkLoader;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.annotation.Nonnull;

public class BackupChunkLoader implements IChunkLoader {
   private final List<IChunkLoader> loaders = new ArrayList<>();
   private final List<FileSystem> fileSystems = new ArrayList<>();
   private final List<Path> tempDirs = new ArrayList<>();

   public BackupChunkLoader(ChunkStore store, List<Path> backups) throws IOException {
      IChunkStorageProvider<?> baseProvider = store.getWorld().getWorldConfig().getChunkStorageProvider();
      Path worldPath = Universe.get().getWorldsPath();

      for (Path path : backups) {
         Path savePath = store.getWorld().getSavePath();
         String relWorldPath = worldPath.relativize(savePath).toString().replace('\\', '/');
         String expectedChunksPrefix = "worlds/" + relWorldPath + "/chunks/";
         FileSystem fs = FileSystems.newFileSystem(path);
         Path worldBackupPath = fs.getPath("worlds", worldPath.relativize(savePath).toString());
         Path loaderPath;
         if (Files.exists(worldBackupPath)) {
            this.fileSystems.add(fs);
            loaderPath = worldBackupPath;
         } else {
            fs.close();
            Path tempDir = Files.createTempDirectory("hytale-backup-recovery-");
            this.tempDirs.add(tempDir);
            Path chunksDir = tempDir.resolve("chunks");
            Files.createDirectory(chunksDir);

            try (ZipFile zipFile = new ZipFile(path.toFile())) {
               Enumeration<? extends ZipEntry> entries = zipFile.entries();

               while (entries.hasMoreElements()) {
                  ZipEntry entry = entries.nextElement();
                  String normalized = entry.getName().replace('\\', '/');
                  if (!entry.isDirectory() && normalized.startsWith(expectedChunksPrefix)) {
                     String fileName = normalized.substring(expectedChunksPrefix.length());
                     if (!fileName.contains("/")) {
                        Path target = chunksDir.resolve(fileName);

                        try (InputStream in = zipFile.getInputStream(entry)) {
                           Files.copy(in, target);
                        }
                     }
                  }
               }
            }

            loaderPath = tempDir;
         }

         IChunkLoader loader = baseProvider.getRecoveryLoader(store.getStore(), loaderPath);
         if (loader == null) {
            throw new RuntimeException("Recovery of individual chunks from backups not supported by storage type. Please restore instead.");
         }

         this.loaders.add(loader);
      }
   }

   @Nonnull
   @Override
   public CompletableFuture<Holder<ChunkStore>> loadHolder(int x, int z) {
      return this.loadChunkNext(this.loaders.iterator(), x, z);
   }

   private CompletableFuture<Holder<ChunkStore>> loadChunkNext(Iterator<IChunkLoader> iterator, int x, int z) {
      if (!iterator.hasNext()) {
         return CompletableFuture.completedFuture(null);
      } else {
         IChunkLoader loader = iterator.next();
         return loader.loadHolder(x, z).exceptionallyCompose(t -> this.loadChunkNext(iterator, x, z));
      }
   }

   @Nonnull
   @Override
   public LongSet getIndexes() throws IOException {
      return LongSet.of();
   }

   @Override
   public void close() throws IOException {
      for (IChunkLoader loader : this.loaders) {
         loader.close();
      }

      for (FileSystem fs : this.fileSystems) {
         fs.close();
      }

      for (Path tempDir : this.tempDirs) {
         try (Stream<Path> walk = Files.walk(tempDir)) {
            walk.sorted(Comparator.reverseOrder()).forEach(p -> {
               try {
                  Files.deleteIfExists(p);
               } catch (IOException var2x) {
               }
            });
         }
      }
   }
}
