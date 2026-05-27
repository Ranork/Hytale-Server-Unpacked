package com.hypixel.hytale.server.core.permissions;

import javax.annotation.Nonnull;

public class HytalePermissions {
   public static final String NAMESPACE = "hytale";
   public static final String COMMAND_BASE = "hytale.command";
   public static final String ASSET_EDITOR = register("hytale.editor.asset", "hytale:ServerEditor");
   public static final String ASSET_EDITOR_PACKS_CREATE = register("hytale.editor.packs.create", "hytale:ServerEditor");
   public static final String ASSET_EDITOR_PACKS_EDIT = register("hytale.editor.packs.edit", "hytale:ServerEditor");
   public static final String ASSET_EDITOR_PACKS_DELETE = register("hytale.editor.packs.delete", "hytale:ServerEditor");
   public static final String BUILDER_TOOLS_EDITOR = register("hytale.editor.builderTools", "hytale:WorldEditor");
   public static final String EDITOR_BRUSH_USE = register("hytale.editor.brush.use", "hytale:WorldEditor");
   public static final String EDITOR_BRUSH_CONFIG = register("hytale.editor.brush.config", "hytale:WorldEditor");
   public static final String EDITOR_PREFAB_USE = register("hytale.editor.prefab.use", "hytale:WorldEditor");
   public static final String EDITOR_PREFAB_MANAGE = register("hytale.editor.prefab.manage", "hytale:WorldEditor");
   public static final String EDITOR_SELECTION_USE = register("hytale.editor.selection.use", "hytale:WorldEditor");
   public static final String EDITOR_SELECTION_CLIPBOARD = register("hytale.editor.selection.clipboard", "hytale:WorldEditor");
   public static final String EDITOR_SELECTION_MODIFY = register("hytale.editor.selection.modify", "hytale:WorldEditor");
   public static final String EDITOR_HISTORY = register("hytale.editor.history", "hytale:WorldEditor");
   public static final String FLY_CAM = register("hytale.camera.flycam", "hytale:Builder");
   public static final String WORLD_MAP_COORDINATE_TELEPORT = register("hytale.world_map.teleport.coordinate", "hytale:WorldEditor");
   public static final String WORLD_MAP_MARKER_TELEPORT = register("hytale.world_map.teleport.marker", "hytale:Adventurer");
   public static final String UPDATE_NOTIFY = register("hytale.system.update.notify", "hytale:Admin");
   public static final String MODS_OUTDATED_NOTIFY = register("hytale.mods.outdated.notify", "hytale:Admin");

   @Nonnull
   public static String fromCommand(@Nonnull String name) {
      return "hytale.command." + name;
   }

   @Nonnull
   public static String fromCommand(@Nonnull String name, @Nonnull String subCommand) {
      return "hytale.command." + name + "." + subCommand;
   }

   private static String register(@Nonnull String permission, @Nonnull String... groups) {
      PermissionsModule.registerPermission(permission, groups);
      return permission;
   }
}
