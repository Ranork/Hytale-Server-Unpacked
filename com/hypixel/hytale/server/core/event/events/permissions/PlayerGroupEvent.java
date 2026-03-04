package com.hypixel.hytale.server.core.event.events.permissions;

import java.util.UUID;
import javax.annotation.Nonnull;

public class PlayerGroupEvent extends PlayerPermissionChangeEvent {
   @Nonnull
   private final String groupName;

   public PlayerGroupEvent(@Nonnull UUID playerUuid, @Nonnull String groupName) {
      super(playerUuid);
      this.groupName = groupName;
   }

   @Nonnull
   public String getGroupName() {
      return this.groupName;
   }

   public static class Added extends PlayerGroupEvent {
      public Added(@Nonnull UUID playerUuid, @Nonnull String groupName) {
         super(playerUuid, groupName);
      }
   }

   public static class Removed extends PlayerGroupEvent {
      public Removed(@Nonnull UUID playerUuid, @Nonnull String groupName) {
         super(playerUuid, groupName);
      }
   }
}
