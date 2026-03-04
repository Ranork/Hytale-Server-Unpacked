package com.hypixel.hytale.server.core.permissions;

import javax.annotation.Nonnull;

public class HytalePermissions {
   public static final String NAMESPACE = "hytale";
   public static final String COMMAND_BASE = "hytale.command";
   public static final String ASSET_EDITOR = "hytale.editor.asset";
   public static final String ASSET_EDITOR_PACKS_CREATE = "hytale.editor.packs.create";
   public static final String ASSET_EDITOR_PACKS_EDIT = "hytale.editor.packs.edit";
   public static final String ASSET_EDITOR_PACKS_DELETE = "hytale.editor.packs.delete";
   public static final String BUILDER_TOOLS_EDITOR = "hytale.editor.builderTools";
   public static final String EDITOR_BRUSH_USE = "hytale.editor.brush.use";
   public static final String EDITOR_BRUSH_CONFIG = "hytale.editor.brush.config";
   public static final String EDITOR_PREFAB_USE = "hytale.editor.prefab.use";
   public static final String EDITOR_PREFAB_MANAGE = "hytale.editor.prefab.manage";
   public static final String EDITOR_SELECTION_USE = "hytale.editor.selection.use";
   public static final String EDITOR_SELECTION_CLIPBOARD = "hytale.editor.selection.clipboard";
   public static final String EDITOR_SELECTION_MODIFY = "hytale.editor.selection.modify";
   public static final String EDITOR_HISTORY = "hytale.editor.history";
   public static final String FLY_CAM = "hytale.camera.flycam";
   public static final String WORLD_MAP_COORDINATE_TELEPORT = "hytale.world_map.teleport.coordinate";
   public static final String WORLD_MAP_MARKER_TELEPORT = "hytale.world_map.teleport.marker";
   public static final String UPDATE_NOTIFY = "hytale.system.update.notify";
   public static final String MODS_OUTDATED_NOTIFY = "hytale.mods.outdated.notify";

   @Nonnull
   public static String fromCommand(@Nonnull String name) {
      return "hytale.command." + name;
   }

   @Nonnull
   public static String fromCommand(@Nonnull String name, @Nonnull String subCommand) {
      return "hytale.command." + name + "." + subCommand;
   }
}
