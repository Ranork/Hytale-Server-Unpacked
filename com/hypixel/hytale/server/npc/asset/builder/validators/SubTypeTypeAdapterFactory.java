package com.hypixel.hytale.server.npc.asset.builder.validators;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SubTypeTypeAdapterFactory implements TypeAdapterFactory {
   private final Class<?> baseClassType;
   private final String typeFieldName;
   private final Map<Class<?>, String> classToName = new HashMap<>();

   private SubTypeTypeAdapterFactory(Class<?> baseClassType, String typeFieldName) {
      this.baseClassType = baseClassType;
      this.typeFieldName = typeFieldName;
   }

   @Nonnull
   public static SubTypeTypeAdapterFactory of(Class<?> baseClass, String typeFieldName) {
      return new SubTypeTypeAdapterFactory(baseClass, typeFieldName);
   }

   @Nonnull
   public SubTypeTypeAdapterFactory registerSubType(Class<?> clazz, String name) {
      if (this.classToName.containsKey(clazz)) {
         throw new IllegalArgumentException();
      } else if (this.classToName.containsValue(name)) {
         throw new IllegalArgumentException();
      } else {
         this.classToName.put(clazz, name);
         return this;
      }
   }

   @Nullable
   public <T> TypeAdapter<T> create(@Nonnull Gson gson, @Nonnull TypeToken<T> type) {
      if (type.getRawType() != this.baseClassType) {
         return null;
      } else {
         final Map<Class<?>, Entry<String, TypeAdapter<?>>> delegateMap = new HashMap<>();
         this.classToName.forEach((aClass, name) -> delegateMap.put((Class<?>)aClass, Map.entry(name, gson.getDelegateAdapter(this, TypeToken.get(aClass)))));
         return (new TypeAdapter<T>() {
               public void write(JsonWriter out, @Nonnull T value) throws IOException {
                  Entry<String, TypeAdapter<?>> entry = delegateMap.get(value.getClass());
                  if (entry == null) {
                     throw new IllegalArgumentException();
                  } else {
                     JsonObject result = new JsonObject();
                     JsonObject obj = entry.getValue().toJsonTree(value).getAsJsonObject();
                     result.addProperty(SubTypeTypeAdapterFactory.this.typeFieldName, entry.getKey());
                     obj.entrySet()
                        .forEach(stringJsonElementEntry -> result.add((String)stringJsonElementEntry.getKey(), (JsonElement)stringJsonElementEntry.getValue()));
                     Streams.write(result, out);
                  }
               }

               public T read(JsonReader in) {
                  throw new RuntimeException("Unsupported");
               }
            })
            .nullSafe();
      }
   }
}
