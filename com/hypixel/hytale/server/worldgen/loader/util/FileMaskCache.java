package com.hypixel.hytale.server.worldgen.loader.util;

import com.google.gson.JsonElement;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nonnull;

public class FileMaskCache<T> {
   @Nonnull
   private final Map<String, T> fileCache = new HashMap<>();
   @Nonnull
   private final Map<String, JsonElement> fileElements = new HashMap<>();

   public T getIfPresentFileMask(String filename) {
      return this.fileCache.get(filename);
   }

   public void putFileMask(String filename, T value) {
      this.fileCache.put(filename, value);
   }

   public JsonElement cachedFile(String filename, @Nonnull Function<String, JsonElement> function) {
      return this.fileElements.computeIfAbsent(filename, function);
   }
}
