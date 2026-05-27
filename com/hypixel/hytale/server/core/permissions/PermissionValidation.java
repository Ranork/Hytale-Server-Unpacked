package com.hypixel.hytale.server.core.permissions;

import java.util.regex.Pattern;
import javax.annotation.Nonnull;

public final class PermissionValidation {
   private static final Pattern GROUP_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9_:\\-]*$");
   private static final Pattern PERMISSION_NODE_PATTERN = Pattern.compile("^-?\\w[\\w-]*(\\.[\\w*][\\w*-]*)*$");

   private PermissionValidation() {
   }

   public static boolean isValidGroupName(@Nonnull String name) {
      return GROUP_NAME_PATTERN.matcher(name).matches();
   }

   public static boolean isValidPermissionNode(@Nonnull String node) {
      return PERMISSION_NODE_PATTERN.matcher(node).matches();
   }
}
