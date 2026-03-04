package com.hypixel.hytale.builtin.buildertools.prefabeditor.commands;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.PrefabEditSession;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.PrefabEditSessionManager;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.PrefabEditingMetadata;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.ui.PrefabEditorExitConfirmPage;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

public class PrefabEditExitCommand extends AbstractAsyncPlayerCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_EDIT_PREFAB_EDIT_NOT_EDITING_A_PREFAB = Message.translation(
      "server.commands.editprefab.exit.notEditingAPrefab"
   );

   public PrefabEditExitCommand() {
      super("exit", "server.commands.editprefab.exit.desc");
   }

   @Nonnull
   @Override
   protected CompletableFuture<Void> executeAsync(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      PrefabEditSessionManager prefabEditSessionManager = BuilderToolsPlugin.get().getPrefabEditSessionManager();
      if (!prefabEditSessionManager.isEditingAPrefab(playerRef.getUuid())) {
         context.sendMessage(MESSAGE_COMMANDS_EDIT_PREFAB_EDIT_NOT_EDITING_A_PREFAB);
         return CompletableFuture.completedFuture(null);
      } else {
         PrefabEditSession prefabEditSession = prefabEditSessionManager.getPrefabEditSession(playerRef.getUuid());
         if (prefabEditSession != null) {
            List<PrefabEditingMetadata> dirtyPrefabs = prefabEditSession.getLoadedPrefabMetadata()
               .values()
               .stream()
               .filter(PrefabEditingMetadata::isDirty)
               .collect(Collectors.toList());
            if (!dirtyPrefabs.isEmpty()) {
               playerComponent.getPageManager().openCustomPage(ref, store, new PrefabEditorExitConfirmPage(playerRef, prefabEditSession, world, dirtyPrefabs));
               return CompletableFuture.completedFuture(null);
            }
         }

         CompletableFuture<Void> result = prefabEditSessionManager.exitEditSession(ref, world, playerRef, store);
         return result != null ? result : CompletableFuture.completedFuture(null);
      }
   }
}
