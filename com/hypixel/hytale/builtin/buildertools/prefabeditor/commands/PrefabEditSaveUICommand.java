package com.hypixel.hytale.builtin.buildertools.prefabeditor.commands;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.PrefabEditSession;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.PrefabEditSessionManager;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.ui.PrefabEditorSaveSettingsPage;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class PrefabEditSaveUICommand extends AbstractPlayerCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_EDIT_PREFAB_NOT_IN_EDIT_SESSION = Message.translation("server.commands.editprefab.notInEditSession");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_EDIT_PREFAB_NO_PREFABS_LOADED = Message.translation("server.commands.editprefab.save.noPrefabsLoaded");

   public PrefabEditSaveUICommand() {
      super("saveui", "server.commands.editprefab.saveui.desc");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      PrefabEditSessionManager prefabEditSessionManager = BuilderToolsPlugin.get().getPrefabEditSessionManager();
      PrefabEditSession prefabEditSession = prefabEditSessionManager.getPrefabEditSession(playerRef.getUuid());
      if (prefabEditSession == null) {
         context.sendMessage(MESSAGE_COMMANDS_EDIT_PREFAB_NOT_IN_EDIT_SESSION);
      } else if (prefabEditSession.getLoadedPrefabMetadata().isEmpty()) {
         context.sendMessage(MESSAGE_COMMANDS_EDIT_PREFAB_NO_PREFABS_LOADED);
      } else {
         Player playerComponent = store.getComponent(ref, Player.getComponentType());

         assert playerComponent != null;

         playerComponent.getPageManager().openCustomPage(ref, store, new PrefabEditorSaveSettingsPage(playerRef, prefabEditSession));
      }
   }
}
