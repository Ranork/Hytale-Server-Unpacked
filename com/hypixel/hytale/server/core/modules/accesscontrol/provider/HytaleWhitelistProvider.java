package com.hypixel.hytale.server.core.modules.accesscontrol.provider;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import com.hypixel.hytale.server.core.util.io.BlockingDiskFile;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import javax.annotation.Nonnull;

public class HytaleWhitelistProvider extends BlockingDiskFile implements AccessProvider {
   @Nonnull
   private static final String WHITELIST_FILE_PATH = "whitelist.json";
   @Nonnull
   private final ReadWriteLock lock = new ReentrantReadWriteLock();
   @Nonnull
   private final Set<UUID> whitelist = new HashSet<>();
   private boolean isEnabled;

   public HytaleWhitelistProvider() {
      super(Paths.get("whitelist.json"));
   }

   @Override
   protected void read(@Nonnull BufferedReader fileReader) {
      if (JsonParser.parseReader(fileReader) instanceof JsonObject jsonObject) {
         this.isEnabled = jsonObject.get("enabled").getAsBoolean();
         jsonObject.get("list").getAsJsonArray().forEach(entry -> this.whitelist.add(UUID.fromString(entry.getAsString())));
      } else {
         throw new JsonParseException("element is not JsonObject!");
      }
   }

   @Override
   protected void write(@Nonnull BufferedWriter fileWriter) throws IOException {
      JsonObject jsonObject = new JsonObject();
      jsonObject.addProperty("enabled", this.isEnabled);
      JsonArray jsonArray = new JsonArray();

      for (UUID uuid : this.whitelist) {
         jsonArray.add(uuid.toString());
      }

      jsonObject.add("list", jsonArray);
      fileWriter.write(jsonObject.toString());
   }

   @Override
   protected void create(@Nonnull BufferedWriter fileWriter) throws IOException {
      JsonWriter jsonWriter = new JsonWriter(fileWriter);

      try {
         jsonWriter.beginObject().name("enabled").value(false).name("list").beginArray().endArray().endObject();
      } catch (Throwable var6) {
         try {
            jsonWriter.close();
         } catch (Throwable var5) {
            var6.addSuppressed(var5);
         }

         throw var6;
      }

      jsonWriter.close();
   }

   @Nonnull
   @Override
   public CompletableFuture<Optional<String>> getDisconnectReason(UUID uuid) {
      this.lock.readLock().lock();

      CompletableFuture var2;
      try {
         if (!this.isEnabled || this.whitelist.contains(uuid)) {
            return CompletableFuture.completedFuture(Optional.empty());
         }

         var2 = CompletableFuture.completedFuture(Optional.of("You are not whitelisted!"));
      } finally {
         this.lock.readLock().unlock();
      }

      return var2;
   }

   public void setEnabled(boolean isEnabled) {
      this.lock.writeLock().lock();

      try {
         this.isEnabled = isEnabled;
      } finally {
         this.lock.writeLock().unlock();
      }
   }

   public boolean modify(@Nonnull Function<Set<UUID>, Boolean> consumer) {
      this.lock.writeLock().lock();

      boolean result;
      try {
         result = consumer.apply(this.whitelist);
      } finally {
         this.lock.writeLock().unlock();
      }

      if (result) {
         this.syncSave();
      }

      return result;
   }

   @Nonnull
   public Set<UUID> getList() {
      this.lock.readLock().lock();

      Set var1;
      try {
         var1 = Collections.unmodifiableSet(this.whitelist);
      } finally {
         this.lock.readLock().unlock();
      }

      return var1;
   }

   public boolean isEnabled() {
      this.lock.readLock().lock();

      boolean var1;
      try {
         var1 = this.isEnabled;
      } finally {
         this.lock.readLock().unlock();
      }

      return var1;
   }
}
