package com.hypixel.hytale.server.core.permissions.provider;

import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;

public interface PermissionProvider {
   @Nonnull
   String getName();

   void addUserPermissions(@Nonnull UUID var1, @Nonnull Set<String> var2);

   void removeUserPermissions(@Nonnull UUID var1, @Nonnull Set<String> var2);

   Set<String> getUserPermissions(@Nonnull UUID var1);

   void addGroupPermissions(@Nonnull String var1, @Nonnull Set<String> var2);

   void removeGroupPermissions(@Nonnull String var1, @Nonnull Set<String> var2);

   Set<String> getGroupPermissions(@Nonnull String var1);

   void addUserToGroup(@Nonnull UUID var1, @Nonnull String var2);

   void removeUserFromGroup(@Nonnull UUID var1, @Nonnull String var2);

   Set<String> getGroupsForUser(@Nonnull UUID var1);
}
