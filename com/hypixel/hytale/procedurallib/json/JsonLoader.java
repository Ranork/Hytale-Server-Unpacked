package com.hypixel.hytale.procedurallib.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hypixel.hytale.procedurallib.file.FileIO;
import java.io.File;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class JsonLoader<K extends SeedResource, T> extends Loader<K, T> {
   public static final JsonResourceLoader<JsonElement> JSON_LOADER = new JsonResourceLoader<>(JsonElement.class, e -> !e.isJsonNull(), Function.identity());
   public static final JsonResourceLoader<JsonArray> JSON_ARR_LOADER = new JsonResourceLoader(
      (Class<T>)JsonArray.class, JsonElement::isJsonArray, JsonElement::getAsJsonArray
   );
   public static final JsonResourceLoader<JsonObject> JSON_OBJ_LOADER = new JsonResourceLoader(
      (Class<T>)JsonObject.class, JsonElement::isJsonObject, JsonElement::getAsJsonObject
   );
   @Nullable
   protected final JsonElement json;

   public JsonLoader(SeedString<K> seed, Path dataFolder, @Nullable JsonElement json) {
      super(seed, dataFolder);
      if (json != null && json.isJsonObject() && json.getAsJsonObject().has("File")) {
         this.json = this.loadFileConstructor(json.getAsJsonObject().get("File").getAsString());
      } else {
         this.json = json;
      }
   }

   public boolean has(String name) {
      return this.json != null && this.json.isJsonObject() && this.json.getAsJsonObject().has(name);
   }

   @Nonnull
   public JsonElement getOrLoad(@Nonnull JsonElement element) {
      if (element.isJsonObject()) {
         JsonObject obj = element.getAsJsonObject();
         JsonElement path = obj.get("File");
         if (path != null && path.isJsonPrimitive() && path.getAsJsonPrimitive().isString()) {
            JsonElement loaded = this.loadFileElem(path.getAsString());
            element = Objects.requireNonNullElse(loaded, element);
         }
      }

      return element;
   }

   @Nullable
   public JsonElement get(String name) {
      if (this.json != null && this.json.isJsonObject()) {
         JsonElement element = this.json.getAsJsonObject().get(name);
         if (element != null && element.isJsonObject()) {
            element = this.getOrLoad(element);
         }

         return element;
      } else {
         return null;
      }
   }

   @Nullable
   public JsonElement getRaw(String name) {
      return this.json != null && this.json.isJsonObject() ? this.json.getAsJsonObject().get(name) : null;
   }

   protected JsonElement loadFile(@Nonnull String filePath) {
      Path file = this.dataFolder.resolve(filePath.replace('.', File.separatorChar) + ".json");
      if (!file.normalize().startsWith(this.dataFolder.normalize())) {
         throw new IllegalArgumentException("Invalid file reference: " + filePath);
      } else {
         try {
            return FileIO.load(file, JSON_LOADER);
         } catch (Throwable var4) {
            throw new Error("Error while loading file reference." + file.toString(), var4);
         }
      }
   }

   protected JsonElement loadFileElem(@Nonnull String filePath) {
      return this.loadFile(filePath);
   }

   protected JsonElement loadFileConstructor(@Nonnull String filePath) {
      return this.loadFile(filePath);
   }

   @Nonnull
   protected JsonObject mustGetObject(@Nonnull String key, @Nullable JsonObject defaultValue) {
      return this.mustGet(key, defaultValue, JsonObject.class, JsonElement::isJsonObject, JsonElement::getAsJsonObject);
   }

   @Nonnull
   protected JsonArray mustGetArray(@Nonnull String key, @Nullable JsonArray defaultValue) {
      return this.mustGet(key, defaultValue, JsonArray.class, JsonElement::isJsonArray, JsonElement::getAsJsonArray);
   }

   @Nonnull
   protected String mustGetString(@Nonnull String key, @Nullable String defaultValue) {
      return this.mustGet(key, defaultValue, String.class, JsonLoader::isString, JsonElement::getAsString);
   }

   @Nonnull
   protected Boolean mustGetBool(@Nonnull String key, @Nullable Boolean defaultValue) {
      return this.mustGet(key, defaultValue, Boolean.class, JsonLoader::isBoolean, JsonElement::getAsBoolean);
   }

   @Nonnull
   protected Number mustGetNumber(@Nonnull String key, @Nullable Number defaultValue) {
      return this.mustGet(key, defaultValue, Number.class, JsonLoader::isNumber, JsonElement::getAsNumber);
   }

   protected <V> V mustGet(
      @Nonnull String key,
      @Nullable V defaultValue,
      @Nonnull Class<V> type,
      @Nonnull Predicate<JsonElement> predicate,
      @Nonnull Function<JsonElement, V> mapper
   ) {
      return mustGet(key, this.get(key), defaultValue, type, predicate, mapper);
   }

   protected static <V> V mustGet(
      @Nonnull String key,
      @Nullable JsonElement element,
      @Nullable V defaultValue,
      @Nonnull Class<V> type,
      @Nonnull Predicate<JsonElement> predicate,
      @Nonnull Function<JsonElement, V> mapper
   ) {
      if (element == null) {
         if (defaultValue != null) {
            return defaultValue;
         } else {
            throw error("Missing property '%s'", key);
         }
      } else if (!predicate.test(element)) {
         throw error("Property '%s' must be of type '%s'", key, type.getSimpleName());
      } else {
         return mapper.apply(element);
      }
   }

   protected static Error error(String format, Object... args) {
      return new Error(String.format(format, args));
   }

   protected static Error error(Throwable parent, String format, Object... args) {
      return new Error(String.format(format, args), parent);
   }

   private static boolean isString(JsonElement element) {
      return element.isJsonPrimitive() && element.getAsJsonPrimitive().isString();
   }

   protected static boolean isNumber(JsonElement element) {
      return element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber();
   }

   protected static boolean isBoolean(JsonElement element) {
      return element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean();
   }

   public interface Constants {
      char JSON_FILEPATH_SEPARATOR = '.';
      String KEY_FILE = "File";
   }
}
