package com.hypixel.hytale.server.core.modules.camera;

import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.event.EventBus;
import com.hypixel.hytale.protocol.packets.camera.SetFlyCameraMode;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.event.events.permissions.GroupPermissionChangeEvent;
import com.hypixel.hytale.server.core.event.events.permissions.PlayerGroupEvent;
import com.hypixel.hytale.server.core.event.events.permissions.PlayerPermissionChangeEvent;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;

public class FlyCameraModule extends JavaPlugin {
   @Nonnull
   public static final PluginManifest MANIFEST = PluginManifest.corePlugin(FlyCameraModule.class).depends(PermissionsModule.class).build();

   public FlyCameraModule(@Nonnull JavaPluginInit init) {
      super(init);
   }

   @Override
   protected void setup() {
      EventBus eventBus = HytaleServer.get().getEventBus();
      eventBus.register(PlayerPermissionChangeEvent.PermissionsRemoved.class, this::handlePlayerPermissionsRemoved);
      eventBus.register(PlayerGroupEvent.Removed.class, this::handlePlayerGroupRemoved);
      eventBus.register(GroupPermissionChangeEvent.Removed.class, this::handleGroupPermissionsRemoved);
   }

   private void handlePlayerPermissionsRemoved(@Nonnull PlayerPermissionChangeEvent.PermissionsRemoved event) {
      if (PermissionsModule.hasPermission(event.getRemovedPermissions(), "hytale.camera.flycam") == Boolean.TRUE) {
         this.checkAndEnforceFlyCameraPermission(event.getPlayerUuid());
      }
   }

   private void handlePlayerGroupRemoved(@Nonnull PlayerGroupEvent.Removed event) {
      this.checkAndEnforceFlyCameraPermission(event.getPlayerUuid());
   }

   private void handleGroupPermissionsRemoved(@Nonnull GroupPermissionChangeEvent.Removed event) {
      if (PermissionsModule.hasPermission(event.getRemovedPermissions(), "hytale.camera.flycam") == Boolean.TRUE) {
         String groupName = event.getGroupName();
         PermissionsModule permissionsModule = PermissionsModule.get();

         for (PlayerRef playerRef : Universe.get().getPlayers()) {
            UUID uuid = playerRef.getUuid();
            Set<String> groups = permissionsModule.getGroupsForUser(uuid);
            if (groups.contains(groupName)) {
               this.checkAndEnforceFlyCameraPermission(uuid);
            }
         }
      }
   }

   private void checkAndEnforceFlyCameraPermission(@Nonnull UUID uuid) {
      PlayerRef playerRef = Universe.get().getPlayer(uuid);
      if (playerRef != null) {
         boolean hasPermission = PermissionsModule.get().hasPermission(uuid, "hytale.camera.flycam");
         if (!hasPermission) {
            playerRef.getPacketHandler().writeNoCache(new SetFlyCameraMode(false));
         }
      }
   }
}
