package com.hypixel.hytale.server.core.modules.accesscontrol.provider;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import com.hypixel.hytale.server.core.modules.accesscontrol.AccessControlModule;
import com.hypixel.hytale.server.core.modules.accesscontrol.ban.Ban;
import com.hypixel.hytale.server.core.util.io.BlockingDiskFile;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import javax.annotation.Nonnull;

public class HytaleBanProvider extends BlockingDiskFile implements AccessProvider {
   private final Map<UUID, Ban> bans = new Object2ObjectOpenHashMap();

   public HytaleBanProvider() {
      super(Paths.get("bans.json"));
   }

   @Nonnull
   @Override
   public CompletableFuture<Optional<String>> getDisconnectReason(UUID uuid) {
      Ban ban = this.bans.get(uuid);
      if (ban != null && !ban.isInEffect()) {
         this.bans.remove(uuid);
         ban = null;
      }

      return ban != null ? ban.getDisconnectReason(uuid) : CompletableFuture.completedFuture(Optional.empty());
   }

   @Override
   protected void read(@Nonnull BufferedReader fileReader) {
      JsonParser.parseReader(fileReader).getAsJsonArray().forEach(entry -> {
         JsonObject jsonObject = entry.getAsJsonObject();

         try {
            Ban ban = AccessControlModule.get().parseBan(jsonObject.get("type").getAsString(), jsonObject);
            Objects.requireNonNull(ban.getBy(), "Ban has null getBy");
            Objects.requireNonNull(ban.getTarget(), "Ban has null getTarget");
            if (ban.isInEffect()) {
               this.bans.put(ban.getTarget(), ban);
            }
         } catch (Exception var4) {
            throw new RuntimeException("Failed to parse ban!", var4);
         }
      });
   }

   @Override
   protected void write(@Nonnull BufferedWriter fileWriter) throws IOException {
      JsonArray array = new JsonArray();
      this.bans.forEach((key, value) -> array.add(value.toJsonObject()));
      fileWriter.write(array.toString());
   }

   @Override
   protected void create(@Nonnull BufferedWriter fileWriter) throws IOException {
      JsonWriter jsonWriter = new JsonWriter(fileWriter);

      try {
         jsonWriter.beginArray().endArray();
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

   public boolean hasBan(UUID uuid) {
      this.fileLock.readLock().lock();

      boolean var2;
      try {
         var2 = this.bans.containsKey(uuid);
      } finally {
         this.fileLock.readLock().unlock();
      }

      return var2;
   }

   public boolean modify(@Nonnull Function<Map<UUID, Ban>, Boolean> function) {
      this.fileLock.writeLock().lock();

      boolean modified;
      try {
         modified = function.apply(this.bans);
      } finally {
         this.fileLock.writeLock().unlock();
      }

      if (modified) {
         this.syncSave();
      }

      return modified;
   }
}
