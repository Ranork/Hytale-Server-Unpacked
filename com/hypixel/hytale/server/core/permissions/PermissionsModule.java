package com.hypixel.hytale.server.core.permissions;

import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.command.system.CommandRegistry;
import com.hypixel.hytale.server.core.event.events.permissions.GroupPermissionChangeEvent;
import com.hypixel.hytale.server.core.event.events.permissions.PlayerGroupEvent;
import com.hypixel.hytale.server.core.event.events.permissions.PlayerPermissionChangeEvent;
import com.hypixel.hytale.server.core.permissions.commands.PermCommand;
import com.hypixel.hytale.server.core.permissions.commands.op.OpCommand;
import com.hypixel.hytale.server.core.permissions.provider.HytalePermissionsProvider;
import com.hypixel.hytale.server.core.permissions.provider.PermissionProvider;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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
         this.add(PermissionsModule.this.standardProvider);
      }
   };

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
   }

   @Override
   protected void start() {
      Map<String, Set<String>> virtualGroups = CommandManager.get().createVirtualPermissionGroups();
      virtualGroups.computeIfAbsent(GameMode.Creative.toString(), k -> new HashSet<>()).add("hytale.editor.builderTools");
      this.setVirtualGroups(virtualGroups);
      this.standardProvider.syncLoad();
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
   }

   public void removeUserFromGroup(@Nonnull UUID uuid, @Nonnull String group) {
      this.getFirstPermissionProvider().removeUserFromGroup(uuid, group);
      HytaleServer.get()
         .getEventBus()
         .<Void, PlayerGroupEvent.Removed>dispatchFor(PlayerGroupEvent.Removed.class)
         .dispatch(new PlayerGroupEvent.Removed(uuid, group));
   }

   public void setVirtualGroups(@Nonnull Map<String, Set<String>> virtualGroups) {
      this.virtualGroups = new Object2ObjectOpenHashMap(virtualGroups);
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

   public boolean hasPermission(@Nonnull UUID uuid, @Nonnull String id) {
      return this.hasPermission(uuid, id, false);
   }

   public boolean hasPermission(@Nonnull UUID uuid, @Nonnull String id, boolean def) {
      for (PermissionProvider permissionProvider : this.providers) {
         Set<String> userNodes = permissionProvider.getUserPermissions(uuid);
         Boolean userHasPerm = hasPermission(userNodes, id);
         if (userHasPerm != null) {
            return userHasPerm;
         }

         for (String group : permissionProvider.getGroupsForUser(uuid)) {
            Set<String> groupNodes = permissionProvider.getGroupPermissions(group);
            Boolean groupHasPerm = hasPermission(groupNodes, id);
            if (groupHasPerm != null) {
               return groupHasPerm;
            }

            Set<String> virtualNodes = this.virtualGroups.get(group);
            Boolean virtualHasPerm = hasPermission(virtualNodes, id);
            if (virtualHasPerm != null) {
               return virtualHasPerm;
            }
         }
      }

      return def;
   }

   @Nullable
   public static Boolean hasPermission(@Nullable Set<String> nodes, @Nonnull String id) {
      if (nodes == null) {
         return null;
      } else if (nodes.contains("*")) {
         return Boolean.TRUE;
      } else if (nodes.contains("-*")) {
         return Boolean.FALSE;
      } else if (nodes.contains(id)) {
         return Boolean.TRUE;
      } else if (nodes.contains("-" + id)) {
         return Boolean.FALSE;
      } else {
         String[] split = id.split("\\.");
         StringBuilder completeTrace = new StringBuilder();

         for (int i = 0; i < split.length; i++) {
            if (i > 0) {
               completeTrace.append('.');
            }

            completeTrace.append(split[i]);
            if (nodes.contains(completeTrace + ".*")) {
               return Boolean.TRUE;
            }

            if (nodes.contains("-" + completeTrace.toString() + ".*")) {
               return Boolean.FALSE;
            }
         }

         return null;
      }
   }
}
