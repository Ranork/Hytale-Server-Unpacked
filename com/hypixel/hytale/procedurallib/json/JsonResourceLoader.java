package com.hypixel.hytale.procedurallib.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParser;
import com.google.gson.Strictness;
import com.google.gson.stream.JsonReader;
import com.hypixel.hytale.procedurallib.file.AssetLoader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nonnull;

public class JsonResourceLoader<T extends JsonElement> implements AssetLoader<T> {
   private final Class<T> type;
   private final Predicate<JsonElement> predicate;
   private final Function<JsonElement, T> mapper;

   public JsonResourceLoader(@Nonnull Class<T> type, @Nonnull Predicate<JsonElement> predicate, @Nonnull Function<JsonElement, T> mapper) {
      this.type = type;
      this.predicate = predicate;
      this.mapper = mapper;
   }

   @Override
   public Class<T> type() {
      return this.type;
   }

   @Nonnull
   public T load(@Nonnull InputStream in) throws IOException {
      JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(in)));

      JsonElement var4;
      try {
         reader.setStrictness(Strictness.LENIENT);
         JsonElement el = JsonParser.parseReader(reader);
         if (el == JsonNull.INSTANCE) {
            throw new IOException("Invalid JSON element: null");
         }

         if (!this.predicate.test(el)) {
            throw new IOException("Invalid JSON element type. Expected: " + this.type.getSimpleName());
         }

         var4 = this.mapper.apply(el);
      } catch (Throwable var6) {
         try {
            reader.close();
         } catch (Throwable var5) {
            var6.addSuppressed(var5);
         }

         throw var6;
      }

      reader.close();
      return (T)var4;
   }
}
