package com.hypixel.hytale.builtin.buildertools.prefabeditor.commands;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.PrefabEditSession;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.PrefabEditSessionManager;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.PrefabEditingMetadata;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

public class PrefabEditModifiedCommand extends AbstractPlayerCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_EDIT_PREFAB_NOT_IN_EDIT_SESSION = Message.translation("server.commands.editprefab.notInEditSession");

   public PrefabEditModifiedCommand() {
      super("modified", "server.commands.editprefab.modified.desc");
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
         Collection<PrefabEditingMetadata> loadedPrefabs = prefabEditSession.getLoadedPrefabMetadata().values();
         List<PrefabEditingMetadata> modifiedPrefabs = loadedPrefabs.stream().filter(metadata -> metadata.isDirty()).collect(Collectors.toList());
         if (modifiedPrefabs.isEmpty()) {
            context.sendMessage(Message.translation("server.commands.editprefab.modified.none"));
         } else {
            context.sendMessage(
               Message.translation("server.commands.editprefab.modified.header").param("count", modifiedPrefabs.size()).param("total", loadedPrefabs.size())
            );

            for (PrefabEditingMetadata prefab : modifiedPrefabs) {
               context.sendMessage(Message.translation("server.commands.editprefab.modified.entry").param("path", prefab.getPrefabPath().toString()));
            }
         }
      }
   }
}
