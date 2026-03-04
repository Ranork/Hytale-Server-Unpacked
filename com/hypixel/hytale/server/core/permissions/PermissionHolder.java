package com.hypixel.hytale.server.core.permissions;

import javax.annotation.Nonnull;

public interface PermissionHolder {
   boolean hasPermission(@Nonnull String var1);

   boolean hasPermission(@Nonnull String var1, boolean var2);
}
