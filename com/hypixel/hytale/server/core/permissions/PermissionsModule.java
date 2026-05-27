package com.hypixel.hytale.server.core.permissions;

import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.command.system.CommandRegistry;
import com.hypixel.hytale.server.core.event.events.permissions.GroupPermissionChangeEvent;
import com.hypixel.hytale.server.core.event.events.permissions.PlayerGroupEvent;
import com.hypixel.hytale.server.core.event.events.permissions.PlayerPermissionChangeEvent;
import com.hypixel.hytale.server.core.io.handlers.game.GamePacketHandler;
import com.hypixel.hytale.server.core.permissions.commands.PermCommand;
import com.hypixel.hytale.server.core.permissions.commands.SetGroupCommand;
import com.hypixel.hytale.server.core.permissions.commands.op.OpCommand;
import com.hypixel.hytale.server.core.permissions.provider.HytalePermissionsProvider;
import com.hypixel.hytale.server.core.permissions.provider.PermissionProvider;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PermissionsModule extends JavaPlugin {
   @Nonnull
   public static final PluginManifest MANIFEST = PluginManifest.corePlugin(PermissionsModule.class).build();
   private static PermissionsModule instance;
   @Nonnull
   private final HytalePermissionsProvider standardProvider = new HytalePermissionsProvider();
   @Nonnull
   private Map<String, Set<String>> virtualGroups = Object2ObjectMaps.emptyMap();
   @Nonnull
   private final List<PermissionProvider> providers = new CopyOnWriteArrayList<PermissionProvider>() {
      {
         Objects.requireNonNull(PermissionsModule.this);
         this.add(PermissionsModule.this.standardProvider);
      }
   };
   @Nonnull
   private static final Map<String, Set<String>> registeredPermissions = new ConcurrentHashMap<>();

   public static PermissionsModule get() {
      return instance;
   }

   public PermissionsModule(@Nonnull JavaPluginInit init) {
      super(init);
      instance = this;
   }

   @Override
   protected void setup() {
      CommandRegistry commandRegistry = this.getCommandRegistry();
      commandRegistry.registerCommand(new OpCommand());
      commandRegistry.registerCommand(new PermCommand());
      commandRegistry.registerCommand(new SetGroupCommand());
   }

   @Override
   protected void start() {
      this.standardProvider.syncLoad();
      this.refreshVirtualGroups();
   }

   public static void registerPermission(@Nonnull String permission) {
      if (!PermissionValidation.isValidPermissionNode(permission)) {
         throw new IllegalArgumentException("Invalid permission node: " + permission);
      } else {
         registeredPermissions.computeIfAbsent(permission, k -> ConcurrentHashMap.newKeySet());
      }
   }

   public static void registerPermission(@Nonnull String permission, @Nonnull String... groups) {
      if (!PermissionValidation.isValidPermissionNode(permission)) {
         throw new IllegalArgumentException("Invalid permission node: " + permission);
      } else {
         for (String group : groups) {
            if (!PermissionValidation.isValidGroupName(group)) {
               throw new IllegalArgumentException("Invalid group name: " + group);
            }

            registeredPermissions.computeIfAbsent(permission, k -> ConcurrentHashMap.newKeySet()).add(group);
         }
      }
   }

   @Nonnull
   public static Map<String, Set<String>> getRegisteredPermissions() {
      return Collections.unmodifiableMap(registeredPermissions);
   }

   public void refreshVirtualGroups() {
      Map<String, Set<String>> groups = CommandManager.get().createVirtualPermissionGroups();

      for (Entry<String, Set<String>> entry : registeredPermissions.entrySet()) {
         for (String group : entry.getValue()) {
            groups.computeIfAbsent(group, k -> new HashSet<>()).add(entry.getKey());
         }
      }

      this.setVirtualGroups(groups);
   }

   public void reload() {
      this.standardProvider.syncLoad();
      this.refreshVirtualGroups();
   }

   public void addProvider(@Nonnull PermissionProvider permissionProvider) {
      this.providers.add(permissionProvider);
   }

   public void removeProvider(@Nonnull PermissionProvider provider) {
      this.providers.remove(provider);
   }

   @Nonnull
   public List<PermissionProvider> getProviders() {
      return Collections.unmodifiableList(this.providers);
   }

   public PermissionProvider getFirstPermissionProvider() {
      return this.providers.getFirst();
   }

   public boolean areProvidersTampered() {
      return this.providers.size() != 1 || this.providers.getFirst() != this.standardProvider;
   }

   public void addUserPermission(@Nonnull UUID uuid, @Nonnull Set<String> permissions) {
      this.getFirstPermissionProvider().addUserPermissions(uuid, permissions);
      HytaleServer.get()
         .getEventBus()
         .<Void, PlayerPermissionChangeEvent.PermissionsAdded>dispatchFor(PlayerPermissionChangeEvent.PermissionsAdded.class)
         .dispatch(new PlayerPermissionChangeEvent.PermissionsAdded(uuid, permissions));
   }

   public void removeUserPermission(@Nonnull UUID uuid, @Nonnull Set<String> permissions) {
      this.getFirstPermissionProvider().removeUserPermissions(uuid, permissions);
      HytaleServer.get()
         .getEventBus()
         .<Void, PlayerPermissionChangeEvent.PermissionsRemoved>dispatchFor(PlayerPermissionChangeEvent.PermissionsRemoved.class)
         .dispatch(new PlayerPermissionChangeEvent.PermissionsRemoved(uuid, permissions));
   }

   public void addGroupPermission(@Nonnull String group, @Nonnull Set<String> permissions) {
      this.getFirstPermissionProvider().addGroupPermissions(group, permissions);
      HytaleServer.get()
         .getEventBus()
         .<Void, GroupPermissionChangeEvent.Added>dispatchFor(GroupPermissionChangeEvent.Added.class)
         .dispatch(new GroupPermissionChangeEvent.Added(group, permissions));
   }

   public void removeGroupPermission(@Nonnull String group, @Nonnull Set<String> permissions) {
      this.getFirstPermissionProvider().removeGroupPermissions(group, permissions);
      HytaleServer.get()
         .getEventBus()
         .<Void, GroupPermissionChangeEvent.Removed>dispatchFor(GroupPermissionChangeEvent.Removed.class)
         .dispatch(new GroupPermissionChangeEvent.Removed(group, permissions));
   }

   public void addUserToGroup(@Nonnull UUID uuid, @Nonnull String group) {
      this.getFirstPermissionProvider().addUserToGroup(uuid, group);
      HytaleServer.get()
         .getEventBus()
         .<Void, PlayerGroupEvent.Added>dispatchFor(PlayerGroupEvent.Added.class)
         .dispatch(new PlayerGroupEvent.Added(uuid, group));
      resendCommandTreeForPlayer(uuid);
   }

   public void removeUserFromGroup(@Nonnull UUID uuid, @Nonnull String group) {
      this.getFirstPermissionProvider().removeUserFromGroup(uuid, group);
      HytaleServer.get()
         .getEventBus()
         .<Void, PlayerGroupEvent.Removed>dispatchFor(PlayerGroupEvent.Removed.class)
         .dispatch(new PlayerGroupEvent.Removed(uuid, group));
      resendCommandTreeForPlayer(uuid);
   }

   public void setUserGroup(@Nonnull UUID uuid, @Nonnull String group) {
      Set<String> oldGroups = this.getGroupsForUser(uuid);
      this.getFirstPermissionProvider().setUserGroup(uuid, group);

      for (String old : oldGroups) {
         if (!old.equals(group)) {
            HytaleServer.get()
               .getEventBus()
               .<Void, PlayerGroupEvent.Removed>dispatchFor(PlayerGroupEvent.Removed.class)
               .dispatch(new PlayerGroupEvent.Removed(uuid, old));
         }
      }

      if (!oldGroups.contains(group)) {
         HytaleServer.get()
            .getEventBus()
            .<Void, PlayerGroupEvent.Added>dispatchFor(PlayerGroupEvent.Added.class)
            .dispatch(new PlayerGroupEvent.Added(uuid, group));
      }

      resendCommandTreeForPlayer(uuid);
   }

   private static void resendCommandTreeForPlayer(@Nonnull UUID uuid) {
      PlayerRef playerRef = Universe.get().getPlayer(uuid);
      if (playerRef != null && playerRef.getPacketHandler() instanceof GamePacketHandler gameHandler) {
         gameHandler.sendCommandTree();
      }
   }

   public void setVirtualGroups(@Nonnull Map<String, Set<String>> virtualGroups) {
      this.virtualGroups = new Object2ObjectOpenHashMap(virtualGroups);
   }

   @Nonnull
   public Map<String, Set<String>> getVirtualGroups() {
      return Collections.unmodifiableMap(this.virtualGroups);
   }

   @Nonnull
   public Set<String> getGroupsForUser(@Nonnull UUID uuid) {
      Set<String> groups = null;

      for (PermissionProvider permissionProvider : this.providers) {
         Set<String> providerGroups = permissionProvider.getGroupsForUser(uuid);
         if (!providerGroups.isEmpty()) {
            if (groups == null) {
               groups = new HashSet<>();
            }

            groups.addAll(providerGroups);
         }
      }

      return groups != null ? Collections.unmodifiableSet(groups) : Collections.emptySet();
   }

   @Nonnull
   public Set<String> getAllRegisteredGroups() {
      Set<String> groups = new HashSet<>(this.virtualGroups.keySet());

      for (PermissionProvider provider : this.providers) {
         groups.addAll(provider.getAllRegisteredGroups());
      }

      return Collections.unmodifiableSet(groups);
   }

   public boolean hasPermission(@Nonnull UUID uuid, @Nonnull String id) {
      return this.hasPermission(uuid, id, false);
   }

   public boolean hasPermission(@Nonnull UUID uuid, @Nonnull String id, boolean def) {
      for (PermissionProvider permissionProvider : this.providers) {
         Boolean userResult = hasPermission(permissionProvider.getUserPermissions(uuid), id);
         if (userResult != null) {
            return userResult;
         }

         for (String group : permissionProvider.getGroupsForUser(uuid)) {
            Boolean groupResult = hasPermission(permissionProvider.getGroupPermissions(group), id);
            if (groupResult != null) {
               return groupResult;
            }

            Set<String> virtualNodes = this.virtualGroups.get(group);
            Boolean virtualResult = hasPermission(virtualNodes, id);
            if (virtualResult != null) {
               return virtualResult;
            }

            Boolean parentResult = this.checkParentChain(permissionProvider, group, id);
            if (parentResult != null) {
               return parentResult;
            }
         }
      }

      return def;
   }

   @Nullable
   private Boolean checkParentChain(@Nonnull PermissionProvider provider, @Nonnull String group, @Nonnull String id) {
      Set<String> visited = new HashSet<>();
      visited.add(group);

      for (String current = provider.getGroupParent(group); current != null && visited.add(current); current = provider.getGroupParent(current)) {
         Boolean result = hasPermission(provider.getGroupPermissions(current), id);
         if (result != null) {
            return result;
         }

         Set<String> virtualNodes = this.virtualGroups.get(current);
         Boolean virtualResult = hasPermission(virtualNodes, id);
         if (virtualResult != null) {
            return virtualResult;
         }
      }

      return null;
   }

   @Nullable
   public static Boolean hasPermission(@Nullable Set<String> nodes, @Nonnull String id) {
      if (nodes != null && !nodes.isEmpty()) {
         if (nodes.contains("-*")) {
            return Boolean.FALSE;
         } else if (nodes.contains("-" + id)) {
            return Boolean.FALSE;
         } else if (nodes.contains(id)) {
            return Boolean.TRUE;
         } else {
            String[] split = id.split("\\.");
            String[] wildcardPaths = new String[split.length];
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < split.length; i++) {
               if (i > 0) {
                  sb.append('.');
               }

               sb.append(split[i]);
               wildcardPaths[i] = sb + ".*";
            }

            for (int i = wildcardPaths.length - 1; i >= 0; i--) {
               if (nodes.contains("-" + wildcardPaths[i])) {
                  return Boolean.FALSE;
               }
            }

            if (nodes.contains("*")) {
               return Boolean.TRUE;
            } else {
               for (String wildcardPath : wildcardPaths) {
                  if (nodes.contains(wildcardPath)) {
                     return Boolean.TRUE;
                  }
               }

               return null;
            }
         }
      } else {
         return null;
      }
   }

   static void resetForTesting() {
      registeredPermissions.clear();
   }
}
