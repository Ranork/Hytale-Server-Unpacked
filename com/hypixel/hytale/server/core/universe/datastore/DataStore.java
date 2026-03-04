package com.hypixel.hytale.server.core.universe.datastore;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface DataStore<T> {
   BuilderCodec<T> getCodec();

   @Nullable
   T load(String var1) throws IOException;

   void save(String var1, T var2);

   void remove(String var1) throws IOException;

   List<String> list() throws IOException;

   @Nonnull
   default Map<String, T> loadAll() throws IOException {
      Map<String, T> map = new Object2ObjectOpenHashMap();

      for (String id : this.list()) {
         T value = this.load(id);
         if (value != null) {
            map.put(id, value);
         }
      }

      return map;
   }

   default void saveAll(@Nonnull Map<String, T> objectsToSave) {
      for (Entry<String, T> entry : objectsToSave.entrySet()) {
         this.save(entry.getKey(), entry.getValue());
      }
   }

   default void removeAll() throws IOException {
      for (String id : this.list()) {
         this.remove(id);
      }
   }
}
