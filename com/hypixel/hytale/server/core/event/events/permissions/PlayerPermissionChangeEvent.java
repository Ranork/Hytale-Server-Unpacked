package com.hypixel.hytale.server.core.event.events.permissions;

import com.hypixel.hytale.event.IEvent;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;

public abstract class PlayerPermissionChangeEvent implements IEvent<Void> {
   @Nonnull
   private final UUID playerUuid;

   protected PlayerPermissionChangeEvent(@Nonnull UUID playerUuid) {
      this.playerUuid = playerUuid;
   }

   @Nonnull
   public UUID getPlayerUuid() {
      return this.playerUuid;
   }

   public static class GroupAdded extends PlayerPermissionChangeEvent {
      @Nonnull
      private final String groupName;

      public GroupAdded(@Nonnull UUID playerUuid, @Nonnull String groupName) {
         super(playerUuid);
         this.groupName = groupName;
      }

      @Nonnull
      public String getGroupName() {
         return this.groupName;
      }
   }

   public static class GroupRemoved extends PlayerPermissionChangeEvent {
      @Nonnull
      private final String groupName;

      public GroupRemoved(@Nonnull UUID playerUuid, @Nonnull String groupName) {
         super(playerUuid);
         this.groupName = groupName;
      }

      @Nonnull
      public String getGroupName() {
         return this.groupName;
      }
   }

   public static class PermissionsAdded extends PlayerPermissionChangeEvent {
      @Nonnull
      private final Set<String> addedPermissions;

      public PermissionsAdded(@Nonnull UUID playerUuid, @Nonnull Set<String> addedPermissions) {
         super(playerUuid);
         this.addedPermissions = addedPermissions;
      }

      @Nonnull
      public Set<String> getAddedPermissions() {
         return Collections.unmodifiableSet(this.addedPermissions);
      }
   }

   public static class PermissionsRemoved extends PlayerPermissionChangeEvent {
      @Nonnull
      private final Set<String> removedPermissions;

      public PermissionsRemoved(@Nonnull UUID playerUuid, @Nonnull Set<String> removedPermissions) {
         super(playerUuid);
         this.removedPermissions = removedPermissions;
      }

      @Nonnull
      public Set<String> getRemovedPermissions() {
         return Collections.unmodifiableSet(this.removedPermissions);
      }
   }
}
