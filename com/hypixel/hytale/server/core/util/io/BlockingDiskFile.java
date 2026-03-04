package com.hypixel.hytale.server.core.util.io;

import com.hypixel.hytale.server.core.Options;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.Nonnull;

public abstract class BlockingDiskFile {
   protected final ReadWriteLock fileLock = new ReentrantReadWriteLock();
   protected final Path path;

   public BlockingDiskFile(Path path) {
      this.path = path;
   }

   protected abstract void read(BufferedReader var1) throws IOException;

   protected abstract void write(BufferedWriter var1) throws IOException;

   protected abstract void create(BufferedWriter var1) throws IOException;

   public void syncLoad() {
      this.fileLock.writeLock().lock();

      try {
         File file = this.toLocalFile();

         try {
            if (!file.exists()) {
               if (Options.getOptionSet().has(Options.BARE)) {
                  byte[] bytes;
                  try (
                     ByteArrayOutputStream out = new ByteArrayOutputStream();
                     BufferedWriter buf = new BufferedWriter(new OutputStreamWriter(out));
                  ) {
                     this.create(buf);
                     bytes = out.toByteArray();
                  }

                  try (BufferedReader var34 = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)))) {
                     this.read(var34);
                     return;
                  }
               }

               try (BufferedWriter fileWriter = Files.newBufferedWriter(file.toPath())) {
                  this.create(fileWriter);
               }
            }

            try (BufferedReader fileReader = Files.newBufferedReader(file.toPath())) {
               this.read(fileReader);
            }
         } catch (Exception var30) {
            throw new RuntimeException("Failed to syncLoad() " + file.getAbsolutePath(), var30);
         }
      } finally {
         this.fileLock.writeLock().unlock();
      }
   }

   public void syncSave() {
      File file = null;
      this.fileLock.readLock().lock();

      try {
         file = this.toLocalFile();

         try (BufferedWriter fileWriter = Files.newBufferedWriter(file.toPath())) {
            this.write(fileWriter);
         }
      } catch (Exception var12) {
         throw new RuntimeException("Failed to syncSave() " + (file != null ? file.getAbsolutePath() : null), var12);
      } finally {
         this.fileLock.readLock().unlock();
      }
   }

   @Nonnull
   protected File toLocalFile() {
      return this.path.toFile();
   }
}
