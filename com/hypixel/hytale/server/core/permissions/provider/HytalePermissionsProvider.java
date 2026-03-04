package com.hypixel.hytale.server.core.permissions.provider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.hypixel.hytale.server.core.util.io.BlockingDiskFile;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import javax.annotation.Nonnull;

public final class HytalePermissionsProvider extends BlockingDiskFile implements PermissionProvider {
   @Nonnull
   public static final String DEFAULT_GROUP = "Default";
   @Nonnull
   public static final Set<String> DEFAULT_GROUP_LIST = Set.of("Default");
   @Nonnull
   public static final String OP_GROUP = "OP";
   @Nonnull
   public static final Map<String, Set<String>> DEFAULT_GROUPS = Map.ofEntries(Map.entry("OP", Set.of("*")), Map.entry("Default", Set.of()));
   @Nonnull
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   @Nonnull
   public static final String PERMISSIONS_FILE_PATH = "permissions.json";
   @Nonnull
   private final Map<UUID, Set<String>> userPermissions = new Object2ObjectOpenHashMap();
   @Nonnull
   private final Map<String, Set<String>> groupPermissions = new Object2ObjectOpenHashMap();
   @Nonnull
   private final Map<UUID, Set<String>> userGroups = new Object2ObjectOpenHashMap();

   public HytalePermissionsProvider() {
      super(Paths.get("permissions.json"));
   }

   @Nonnull
   @Override
   public String getName() {
      return "HytalePermissionsProvider";
   }

   @Override
   public void addUserPermissions(@Nonnull UUID uuid, @Nonnull Set<String> permissions) {
      this.fileLock.writeLock().lock();

      try {
         Set<String> set = this.userPermissions.computeIfAbsent(uuid, k -> new HashSet<>());
         if (set.addAll(permissions)) {
            this.syncSave();
         }
      } finally {
         this.fileLock.writeLock().unlock();
      }
   }

   @Override
   public void removeUserPermissions(@Nonnull UUID uuid, @Nonnull Set<String> permissions) {
      this.fileLock.writeLock().lock();

      try {
         Set<String> set = this.userPermissions.get(uuid);
         if (set != null) {
            boolean hasChanges = set.removeAll(permissions);
            if (set.isEmpty()) {
               this.userPermissions.remove(uuid);
            }

            if (hasChanges) {
               this.syncSave();
            }
         }
      } finally {
         this.fileLock.writeLock().unlock();
      }
   }

   @Nonnull
   @Override
   public Set<String> getUserPermissions(@Nonnull UUID uuid) {
      this.fileLock.readLock().lock();

      Set var3;
      try {
         Set<String> set = this.userPermissions.get(uuid);
         if (set != null) {
            return Collections.unmodifiableSet(set);
         }

         var3 = Collections.emptySet();
      } finally {
         this.fileLock.readLock().unlock();
      }

      return var3;
   }

   @Override
   public void addGroupPermissions(@Nonnull String group, @Nonnull Set<String> permissions) {
      this.fileLock.writeLock().lock();

      try {
         Set<String> set = this.groupPermissions.computeIfAbsent(group, k -> new HashSet<>());
         if (set.addAll(permissions)) {
            this.syncSave();
         }
      } finally {
         this.fileLock.writeLock().unlock();
      }
   }

   @Override
   public void removeGroupPermissions(@Nonnull String group, @Nonnull Set<String> permissions) {
      this.fileLock.writeLock().lock();

      try {
         Set<String> set = this.groupPermissions.get(group);
         if (set != null) {
            boolean hasChanges = set.removeAll(permissions);
            if (set.isEmpty()) {
               this.groupPermissions.remove(group);
            }

            if (hasChanges) {
               this.syncSave();
            }
         }
      } finally {
         this.fileLock.writeLock().unlock();
      }
   }

   @Nonnull
   @Override
   public Set<String> getGroupPermissions(@Nonnull String group) {
      this.fileLock.readLock().lock();

      Set var3;
      try {
         Set<String> set = this.groupPermissions.get(group);
         if (set != null) {
            return Collections.unmodifiableSet(set);
         }

         var3 = Collections.emptySet();
      } finally {
         this.fileLock.readLock().unlock();
      }

      return var3;
   }

   @Override
   public void addUserToGroup(@Nonnull UUID uuid, @Nonnull String group) {
      this.fileLock.writeLock().lock();

      try {
         Set<String> list = this.userGroups.computeIfAbsent(uuid, k -> new HashSet<>());
         if (list.add(group)) {
            this.syncSave();
         }
      } finally {
         this.fileLock.writeLock().unlock();
      }
   }

   @Override
   public void removeUserFromGroup(@Nonnull UUID uuid, @Nonnull String group) {
      this.fileLock.writeLock().lock();

      try {
         Set<String> list = this.userGroups.get(uuid);
         if (list != null) {
            boolean hasChanges = list.remove(group);
            if (list.isEmpty()) {
               this.userGroups.remove(uuid);
            }

            if (hasChanges) {
               this.syncSave();
            }
         }
      } finally {
         this.fileLock.writeLock().unlock();
      }
   }

   @Nonnull
   @Override
   public Set<String> getGroupsForUser(@Nonnull UUID uuid) {
      this.fileLock.readLock().lock();

      Set var3;
      try {
         Set<String> list = this.userGroups.get(uuid);
         if (list != null) {
            return Collections.unmodifiableSet(list);
         }

         var3 = DEFAULT_GROUP_LIST;
      } finally {
         this.fileLock.readLock().unlock();
      }

      return var3;
   }

   @Override
   protected void read(@Nonnull BufferedReader fileReader) throws IOException {
      JsonReader jsonReader = new JsonReader(fileReader);

      try {
         JsonObject root = JsonParser.parseReader(jsonReader).getAsJsonObject();
         this.userPermissions.clear();
         this.groupPermissions.clear();
         this.userGroups.clear();
         if (root.has("users")) {
            JsonObject users = root.getAsJsonObject("users");

            for (Entry<String, JsonElement> entry : users.entrySet()) {
               UUID uuid = UUID.fromString(entry.getKey());
               JsonObject user = entry.getValue().getAsJsonObject();
               if (user.has("permissions")) {
                  Set<String> set = new HashSet<>();
                  this.userPermissions.put(uuid, set);
                  user.getAsJsonArray("permissions").forEach(e -> set.add(e.getAsString()));
               }

               if (user.has("groups")) {
                  Set<String> list = new HashSet<>();
                  this.userGroups.put(uuid, list);
                  user.getAsJsonArray("groups").forEach(e -> list.add(e.getAsString()));
               }
            }
         }

         if (root.has("groups")) {
            JsonObject groups = root.getAsJsonObject("groups");

            for (Entry<String, JsonElement> entry : groups.entrySet()) {
               Set<String> set = new HashSet<>();
               this.groupPermissions.put(entry.getKey(), set);
               entry.getValue().getAsJsonArray().forEach(e -> set.add(e.getAsString()));
            }
         }

         for (Entry<String, Set<String>> entry : DEFAULT_GROUPS.entrySet()) {
            this.groupPermissions.put(entry.getKey(), new HashSet<>(entry.getValue()));
         }
      } catch (Throwable var11) {
         try {
            jsonReader.close();
         } catch (Throwable var10) {
            var11.addSuppressed(var10);
         }

         throw var11;
      }

      jsonReader.close();
   }

   @Override
   protected void write(@Nonnull BufferedWriter fileWriter) throws IOException {
      JsonObject root = new JsonObject();
      JsonObject usersObj = new JsonObject();

      for (Entry<UUID, Set<String>> entry : this.userPermissions.entrySet()) {
         JsonArray asArray = new JsonArray();
         entry.getValue().forEach(asArray::add);
         String memberName = entry.getKey().toString();
         if (!usersObj.has(memberName)) {
            usersObj.add(memberName, new JsonObject());
         }

         usersObj.getAsJsonObject(memberName).add("permissions", asArray);
      }

      for (Entry<UUID, Set<String>> entry : this.userGroups.entrySet()) {
         JsonArray asArray = new JsonArray();
         entry.getValue().forEach(asArray::add);
         String memberName = entry.getKey().toString();
         if (!usersObj.has(memberName)) {
            usersObj.add(memberName, new JsonObject());
         }

         usersObj.getAsJsonObject(memberName).add("groups", asArray);
      }

      if (!usersObj.isEmpty()) {
         root.add("users", usersObj);
      }

      JsonObject groupsObj = new JsonObject();

      for (Entry<String, Set<String>> entry : this.groupPermissions.entrySet()) {
         JsonArray asArray = new JsonArray();
         entry.getValue().forEach(asArray::add);
         groupsObj.add(entry.getKey(), asArray);
      }

      if (!groupsObj.isEmpty()) {
         root.add("groups", groupsObj);
      }

      fileWriter.write(GSON.toJson(root));
   }

   @Override
   protected void create(@Nonnull BufferedWriter fileWriter) throws IOException {
      JsonWriter jsonWriter = new JsonWriter(fileWriter);

      try {
         jsonWriter.beginObject();
         jsonWriter.endObject();
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
}
