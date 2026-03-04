package com.hypixel.hytale.server.core.event.events.permissions;

import com.hypixel.hytale.event.IEvent;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;

public abstract class GroupPermissionChangeEvent implements IEvent<Void> {
   @Nonnull
   private final String groupName;

   protected GroupPermissionChangeEvent(@Nonnull String groupName) {
      this.groupName = groupName;
   }

   @Nonnull
   public String getGroupName() {
      return this.groupName;
   }

   public static class Added extends GroupPermissionChangeEvent {
      @Nonnull
      private final Set<String> addedPermissions;

      public Added(@Nonnull String groupName, @Nonnull Set<String> addedPermissions) {
         super(groupName);
         this.addedPermissions = addedPermissions;
      }

      @Nonnull
      public Set<String> getAddedPermissions() {
         return Collections.unmodifiableSet(this.addedPermissions);
      }
   }

   public static class Removed extends GroupPermissionChangeEvent {
      @Nonnull
      private final Set<String> removedPermissions;

      public Removed(@Nonnull String groupName, @Nonnull Set<String> removedPermissions) {
         super(groupName);
         this.removedPermissions = removedPermissions;
      }

      @Nonnull
      public Set<String> getRemovedPermissions() {
         return Collections.unmodifiableSet(this.removedPermissions);
      }
   }
}
