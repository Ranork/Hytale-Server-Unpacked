package com.hypixel.hytale.server.worldgen.loader.context;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map.Entry;
import javax.annotation.Nonnull;

public class FileContext<T> {
   private final int id;
   private final String name;
   private final Path filepath;
   private final T parentContext;

   public FileContext(int id, String name, Path filepath, T parentContext) {
      this.id = id;
      this.name = name;
      this.filepath = filepath;
      this.parentContext = parentContext;
   }

   public int getId() {
      return this.id;
   }

   public String getName() {
      return this.name;
   }

   public Path getPath() {
      return this.filepath;
   }

   public T getParentContext() {
      return this.parentContext;
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
}
