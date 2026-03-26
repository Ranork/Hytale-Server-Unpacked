package com.hypixel.hytale.server.worldgen.loader.context;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map.Entry;
import javax.annotation.Nonnull;

public class FileContext<T extends FileContext<?>> {
   private final int id;
   @Nonnull
   private final String name;
   @Nonnull
   private final Path filepath;
   @Nonnull
   private final T parentContext;
   private transient String rootPath = null;
   private transient String contentPath = null;

   public FileContext(int id, @Nonnull String name, @Nonnull Path filepath, @Nonnull T parentContext) {
      this.id = id;
      this.name = name;
      this.filepath = filepath;
      this.parentContext = parentContext;
   }

   public int getId() {
      return this.id;
   }

   @Nonnull
   public String getName() {
      return this.name;
   }

   @Nonnull
   public Path getPath() {
      return this.filepath;
   }

   @Nonnull
   public String getRootPath() {
      if (this.rootPath == null) {
         this.rootPath = this.getRoot().filepath.getFileName().toString();
      }

      return this.rootPath;
   }

   @Nonnull
   public T getParentContext() {
      return this.parentContext;
   }

   @Nonnull
   public String getContentPath() {
      if (this.contentPath == null) {
         this.contentPath = toContentPath(this.filepath, this.parentContext);
      }

      return this.contentPath;
   }

   @Nonnull
   public FileContext<?> getRoot() {
      FileContext<?> context = this;

      while (context.parentContext != FileContext.RootContext.INSTANCE) {
         context = context.parentContext;
      }

      return context;
   }

   @Nonnull
   private static String toContentPath(@Nonnull Path filepath, @Nonnull FileContext<?> parent) {
      StringBuilder sb = new StringBuilder();
      int start = parent == FileContext.RootContext.INSTANCE ? 0 : parent.getRoot().filepath.getNameCount();

      for (int i = start; i < filepath.getNameCount(); i++) {
         if (i > start) {
            sb.append('.');
         }

         String name = filepath.getName(i).toString();
         int end = name.length();
         if (i == filepath.getNameCount() - 1) {
            int ext = name.lastIndexOf(46);
            if (ext != -1) {
               end = ext;
            }
         }

         sb.append(name, 0, end);
      }

      return sb.toString();
   }

   public interface Constants {
      String ERROR_MISSING_ENTRY = "Missing %s entry for key %s";
      String ERROR_DUPLICATE_ENTRY = "Duplicate %s entry registered for key %s";
   }

   public static class Registry<T> implements Iterable<Entry<String, T>> {
      private final String registryName;
      @Nonnull
      private final Object2ObjectMap<String, T> backing;

      public Registry(String name) {
         this.registryName = name;
         this.backing = new Object2ObjectLinkedOpenHashMap();
      }

      public int size() {
         return this.backing.size();
      }

      public String getName() {
         return this.registryName;
      }

      public boolean contains(String name) {
         return this.backing.containsKey(name);
      }

      @Nonnull
      public T get(String name) {
         T value = (T)this.backing.get(name);
         if (value == null) {
            throw new Error(String.format("Missing %s entry for key %s", this.registryName, name));
         } else {
            return value;
         }
      }

      public void register(String name, T biome) {
         if (this.backing.containsKey(name)) {
            throw new Error(String.format("Duplicate %s entry registered for key %s", this.registryName, name));
         } else {
            this.backing.put(name, biome);
         }
      }

      @Nonnull
      @Override
      public Iterator<Entry<String, T>> iterator() {
         return this.backing.entrySet().iterator();
      }
   }

   public static class RootContext extends FileContext<FileContext.RootContext> {
      public static final FileContext.RootContext INSTANCE = new FileContext.RootContext();

      private RootContext() {
         super(-1, ".", Paths.get("."), null);
      }
   }
}
