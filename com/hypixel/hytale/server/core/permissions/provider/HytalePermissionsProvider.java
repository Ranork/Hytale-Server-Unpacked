package com.hypixel.hytale.server.core.permissions.provider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.permissions.PermissionValidation;
import com.hypixel.hytale.server.core.util.io.BlockingDiskFile;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class HytalePermissionsProvider extends BlockingDiskFile implements PermissionProvider {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   public static final String GROUP_NONE = "hytale:None";
   public static final String GROUP_ADVENTURER = "hytale:Adventurer";
   public static final String GROUP_BUILDER = "hytale:Builder";
   public static final String GROUP_WORLD_EDITOR = "hytale:WorldEditor";
   public static final String GROUP_SERVER_EDITOR = "hytale:ServerEditor";
   public static final String GROUP_ADMIN = "hytale:Admin";
   @Nonnull
   public static final String DEFAULT_GROUP = "hytale:Adventurer";
   @Deprecated
   @Nonnull
   public static final String OP_GROUP = "hytale:Admin";
   @Nonnull
   public static final Set<String> DEFAULT_GROUP_LIST = Set.of("hytale:Adventurer");
   private static final Map<String, String> LEGACY_GROUP_MAPPINGS;
   @Nonnull
   private static final Map<String, HytalePermissionsProvider.GroupData> BUILTIN_GROUPS;
   @Nonnull
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   @Nonnull
   public static final String PERMISSIONS_FILE_PATH = "permissions.json";
   @Nonnull
   private final Map<UUID, Set<String>> userPermissions = new Object2ObjectOpenHashMap();
   @Nonnull
   private final Map<String, HytalePermissionsProvider.GroupData> groupData = new Object2ObjectOpenHashMap();
   @Nonnull
   private final Map<UUID, Set<String>> userGroups = new Object2ObjectOpenHashMap();

   @Nonnull
   public static String resolveGroupName(@Nonnull String group) {
      String mapped = LEGACY_GROUP_MAPPINGS.get(group.toLowerCase(Locale.ROOT));
      return mapped != null ? mapped : group;
   }

   public HytalePermissionsProvider() {
      super(Paths.get("permissions.json"));
   }

   public HytalePermissionsProvider(@Nonnull Path path) {
      super(path);
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
         var3 = set == null ? Collections.emptySet() : Collections.unmodifiableSet(set);
      } finally {
         this.fileLock.readLock().unlock();
      }

      return var3;
   }

   @Override
   public void addGroupPermissions(@Nonnull String group, @Nonnull Set<String> permissions) {
      this.fileLock.writeLock().lock();

      try {
         HytalePermissionsProvider.GroupData data = this.groupData.get(group);
         if (data == null) {
            data = new HytalePermissionsProvider.GroupData(null, new HashSet<>());
            this.groupData.put(group, data);
         }

         if (data.permissions.addAll(permissions)) {
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
         HytalePermissionsProvider.GroupData data = this.groupData.get(group);
         if (data != null) {
            boolean hasChanges = data.permissions.removeAll(permissions);
            if (data.permissions.isEmpty() && data.parent == null && !BUILTIN_GROUPS.containsKey(group)) {
               this.groupData.remove(group);
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
         HytalePermissionsProvider.GroupData data = this.groupData.get(group);
         var3 = data == null ? Collections.emptySet() : Collections.unmodifiableSet(data.permissions);
      } finally {
         this.fileLock.readLock().unlock();
      }

      return var3;
   }

   @Nullable
   @Override
   public String getGroupParent(@Nonnull String group) {
      this.fileLock.readLock().lock();

      String var3;
      try {
         HytalePermissionsProvider.GroupData data = this.groupData.get(group);
         var3 = data != null ? data.parent : null;
      } finally {
         this.fileLock.readLock().unlock();
      }

      return var3;
   }

   @Nonnull
   @Override
   public Set<String> getAllRegisteredGroups() {
      this.fileLock.readLock().lock();

      Set var1;
      try {
         var1 = Collections.unmodifiableSet(new HashSet<>(this.groupData.keySet()));
      } finally {
         this.fileLock.readLock().unlock();
      }

      return var1;
   }

   @Nonnull
   @Override
   public Set<String> getEffectiveGroupPermissions(@Nonnull String group) {
      this.fileLock.readLock().lock();

      Set var16;
      try {
         List<Set<String>> layers = new ArrayList<>();
         Set<String> visited = new HashSet<>();
         String current = group;

         while (current != null && visited.add(current)) {
            HytalePermissionsProvider.GroupData data = this.groupData.get(current);
            if (data == null) {
               break;
            }

            layers.add(data.permissions);
            current = data.parent;
         }

         Set<String> effective = new HashSet<>();
         Set<String> decided = new HashSet<>();

         for (Set<String> layer : layers) {
            for (String perm : layer) {
               String base = perm.startsWith("-") ? perm.substring(1) : perm;
               if (decided.add(base)) {
                  effective.add(perm);
               }
            }
         }

         var16 = Collections.unmodifiableSet(effective);
      } finally {
         this.fileLock.readLock().unlock();
      }

      return var16;
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
         var3 = list == null ? DEFAULT_GROUP_LIST : Collections.unmodifiableSet(list);
      } finally {
         this.fileLock.readLock().unlock();
      }

      return var3;
   }

   @Override
   public void setUserGroup(@Nonnull UUID uuid, @Nonnull String group) {
      this.fileLock.writeLock().lock();

      try {
         Set<String> set = new HashSet<>();
         set.add(group);
         this.userGroups.put(uuid, set);
         this.syncSave();
      } finally {
         this.fileLock.writeLock().unlock();
      }
   }

   @Override
   protected void read(@Nonnull BufferedReader fileReader) throws IOException {
      JsonReader jsonReader = new JsonReader(fileReader);

      try {
         JsonObject root = JsonParser.parseReader(jsonReader).getAsJsonObject();
         this.userPermissions.clear();
         this.groupData.clear();
         this.userGroups.clear();
         if (root.has("users")) {
            JsonObject users = root.getAsJsonObject("users");

            for (Entry<String, JsonElement> entry : users.entrySet()) {
               UUID uuid = UUID.fromString(entry.getKey());
               JsonObject user = entry.getValue().getAsJsonObject();
               if (user.has("permissions")) {
                  Set<String> set = new HashSet<>();
                  this.userPermissions.put(uuid, set);
                  user.getAsJsonArray("permissions").forEach(e -> {
                     String node = e.getAsString();
                     if (PermissionValidation.isValidPermissionNode(node)) {
                        set.add(node);
                     } else {
                        ((HytaleLogger.Api)LOGGER.atWarning()).log("Skipping invalid user permission node '%s' for %s", node, uuid);
                     }
                  });
               }

               if (user.has("groups")) {
                  Set<String> groupSet = new HashSet<>();
                  this.userGroups.put(uuid, groupSet);
                  user.getAsJsonArray("groups").forEach(e -> {
                     String resolved = resolveGroupName(e.getAsString());
                     if (PermissionValidation.isValidGroupName(resolved)) {
                        groupSet.add(resolved);
                     } else {
                        ((HytaleLogger.Api)LOGGER.atWarning()).log("Skipping invalid group name '%s' for user %s", resolved, uuid);
                     }
                  });
               }
            }
         }

         if (root.has("groups")) {
            JsonObject groups = root.getAsJsonObject("groups");

            for (Entry<String, JsonElement> entry : groups.entrySet()) {
               String groupName = resolveGroupName(entry.getKey());
               if (!PermissionValidation.isValidGroupName(groupName)) {
                  ((HytaleLogger.Api)LOGGER.atWarning()).log("Skipping invalid group name '%s' in permissions.json", entry.getKey());
               } else {
                  JsonElement value = entry.getValue();
                  if (value.isJsonArray()) {
                     Set<String> perms = new HashSet<>();
                     value.getAsJsonArray().forEach(e -> addValidatedPermission(perms, e.getAsString(), groupName));
                     this.groupData.put(groupName, new HytalePermissionsProvider.GroupData(null, perms));
                     ((HytaleLogger.Api)LOGGER.atInfo()).log("Migrated legacy group format for '%s'", entry.getKey());
                  } else if (value.isJsonObject()) {
                     JsonObject groupObj = value.getAsJsonObject();
                     String parent = null;
                     if (groupObj.has("parent")) {
                        String resolvedParent = resolveGroupName(groupObj.get("parent").getAsString());
                        if (PermissionValidation.isValidGroupName(resolvedParent)) {
                           parent = resolvedParent;
                        } else {
                           ((HytaleLogger.Api)LOGGER.atWarning()).log("Skipping invalid parent '%s' for group '%s'", resolvedParent, groupName);
                        }
                     }

                     Set<String> perms = new HashSet<>();
                     if (groupObj.has("permissions")) {
                        groupObj.getAsJsonArray("permissions").forEach(e -> addValidatedPermission(perms, e.getAsString(), groupName));
                     }

                     this.groupData.put(groupName, new HytalePermissionsProvider.GroupData(parent, perms));
                  }
               }
            }
         }

         for (Entry<String, HytalePermissionsProvider.GroupData> builtinEntry : BUILTIN_GROUPS.entrySet()) {
            HytalePermissionsProvider.GroupData loaded = this.groupData.get(builtinEntry.getKey());
            if (loaded == null) {
               this.groupData
                  .put(
                     builtinEntry.getKey(),
                     new HytalePermissionsProvider.GroupData(builtinEntry.getValue().parent, new HashSet<>(builtinEntry.getValue().permissions))
                  );
            } else {
               Set<String> mergedPerms = new HashSet<>(builtinEntry.getValue().permissions);
               mergedPerms.addAll(loaded.permissions);
               this.groupData.put(builtinEntry.getKey(), new HytalePermissionsProvider.GroupData(builtinEntry.getValue().parent, mergedPerms));
            }
         }
      } catch (Throwable var13) {
         try {
            jsonReader.close();
         } catch (Throwable var12) {
            var13.addSuppressed(var12);
         }

         throw var13;
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

      for (Entry<String, HytalePermissionsProvider.GroupData> entry : this.groupData.entrySet()) {
         if (!isUnmodifiedBuiltin(entry.getKey(), entry.getValue())) {
            HytalePermissionsProvider.GroupData data = entry.getValue();
            JsonObject groupObj = new JsonObject();
            if (data.parent != null) {
               groupObj.addProperty("parent", data.parent);
            }

            JsonArray permsArray = new JsonArray();
            data.permissions.forEach(permsArray::add);
            groupObj.add("permissions", permsArray);
            groupsObj.add(entry.getKey(), groupObj);
         }
      }

      if (!groupsObj.isEmpty()) {
         root.add("groups", groupsObj);
      }

      fileWriter.write(GSON.toJson(root));
   }

   private static void addValidatedPermission(@Nonnull Set<String> target, @Nonnull String node, @Nonnull String groupName) {
      if (PermissionValidation.isValidPermissionNode(node)) {
         target.add(node);
      } else {
         ((HytaleLogger.Api)LOGGER.atWarning()).log("Skipping invalid permission node '%s' in group '%s'", node, groupName);
      }
   }

   private static boolean isUnmodifiedBuiltin(@Nonnull String name, @Nonnull HytalePermissionsProvider.GroupData data) {
      HytalePermissionsProvider.GroupData builtin = BUILTIN_GROUPS.get(name);
      return builtin == null ? false : Objects.equals(builtin.parent, data.parent) && builtin.permissions.equals(data.permissions);
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

   static {
      Object2ObjectOpenHashMap<String, String> m = new Object2ObjectOpenHashMap();
      m.put("op", "hytale:Admin");
      m.put("adventure", "hytale:Adventurer");
      m.put("adventurer", "hytale:Adventurer");
      m.put("creative", "hytale:WorldEditor");
      m.put("default", "hytale:Adventurer");
      LEGACY_GROUP_MAPPINGS = Collections.unmodifiableMap(m);
      LinkedHashMap<String, HytalePermissionsProvider.GroupData> mx = new LinkedHashMap<>();
      mx.put("hytale:None", new HytalePermissionsProvider.GroupData(null, Set.of()));
      mx.put("hytale:Adventurer", new HytalePermissionsProvider.GroupData("hytale:None", Set.of()));
      mx.put("hytale:Builder", new HytalePermissionsProvider.GroupData("hytale:Adventurer", Set.of()));
      mx.put("hytale:WorldEditor", new HytalePermissionsProvider.GroupData("hytale:Builder", Set.of()));
      mx.put("hytale:ServerEditor", new HytalePermissionsProvider.GroupData("hytale:WorldEditor", Set.of()));
      mx.put("hytale:Admin", new HytalePermissionsProvider.GroupData("hytale:ServerEditor", Set.of("*")));
      BUILTIN_GROUPS = Collections.unmodifiableMap(mx);
   }

   private record GroupData(@Nullable String parent, @Nonnull Set<String> permissions) {
      GroupData(@Nullable String parent, @Nonnull Set<String> permissions) {
         this.parent = parent;
         this.permissions = new HashSet<>(permissions);
      }
   }
}
