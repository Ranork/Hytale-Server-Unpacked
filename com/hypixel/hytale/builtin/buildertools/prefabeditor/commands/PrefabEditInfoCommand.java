package com.hypixel.hytale.builtin.buildertools.prefabeditor.commands;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.PrefabEditSession;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.PrefabEditSessionManager;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.PrefabEditingMetadata;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;
import javax.annotation.Nonnull;

public class PrefabEditInfoCommand extends AbstractPlayerCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_EDIT_PREFAB_NOT_IN_EDIT_SESSION = Message.translation("server.commands.editprefab.notInEditSession");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_EDIT_PREFAB_NO_PREFAB_SELECTED = Message.translation("server.commands.editprefab.noPrefabSelected");

   public PrefabEditInfoCommand() {
      super("info", "server.commands.editprefab.info.desc");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());

      assert uuidComponent != null;

      UUID playerUUID = uuidComponent.getUuid();
      PrefabEditSessionManager prefabEditSessionManager = BuilderToolsPlugin.get().getPrefabEditSessionManager();
      PrefabEditSession prefabEditSession = prefabEditSessionManager.getPrefabEditSession(playerUUID);
      if (prefabEditSession == null) {
         context.sendMessage(MESSAGE_COMMANDS_EDIT_PREFAB_NOT_IN_EDIT_SESSION);
      } else {
         PrefabEditingMetadata selectedPrefab = prefabEditSession.getSelectedPrefab(playerUUID);
         if (selectedPrefab == null) {
            context.sendMessage(MESSAGE_COMMANDS_EDIT_PREFAB_NO_PREFAB_SELECTED);
         } else {
            Vector3i minPoint = selectedPrefab.getMinPoint();
            Vector3i maxPoint = selectedPrefab.getMaxPoint();
            int xWidth = maxPoint.getX() - minPoint.getX();
            int zWidth = maxPoint.getZ() - minPoint.getZ();
            int yHeight = maxPoint.getY() - minPoint.getY();
            context.sendMessage(
               Message.translation("server.commands.editprefab.info.format")
                  .param("path", selectedPrefab.getPrefabPath().toString())
                  .param("dimensions", "X: " + xWidth + ", Y: " + yHeight + ", Z: " + zWidth)
                  .param(
                     "dirty",
                     selectedPrefab.isDirty()
                        ? Message.translation("server.commands.editprefab.info.dirty.yes")
                        : Message.translation("server.commands.editprefab.info.dirty.no")
                  )
            );
         }
      }
   }
}
