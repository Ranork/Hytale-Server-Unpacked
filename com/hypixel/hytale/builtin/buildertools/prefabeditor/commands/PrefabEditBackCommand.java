package com.hypixel.hytale.builtin.buildertools.prefabeditor.commands;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.PrefabEditSession;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.PrefabEditSessionManager;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public class PrefabEditBackCommand extends AbstractAsyncPlayerCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_EDIT_PREFAB_NOT_IN_EDIT_SESSION = Message.translation("server.commands.editprefab.notInEditSession");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_ALREADY_IN_EDIT_SESSION = Message.translation("server.commands.editprefab.alreadyInEditSession");

   public PrefabEditBackCommand() {
      super("back", "server.commands.editprefab.back.desc");
   }

   @Nonnull
   @Override
   protected CompletableFuture<Void> executeAsync(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      PrefabEditSessionManager prefabEditSessionManager = BuilderToolsPlugin.get().getPrefabEditSessionManager();
      if (!prefabEditSessionManager.isEditingAPrefab(playerRef.getUuid())) {
         context.sendMessage(MESSAGE_COMMANDS_EDIT_PREFAB_NOT_IN_EDIT_SESSION);
         return CompletableFuture.completedFuture(null);
      } else {
         PrefabEditSession prefabEditSession = prefabEditSessionManager.getPrefabEditSession(playerRef.getUuid());
         String sessionWorldName = prefabEditSession.getWorldName();
         String currentWorldName = Universe.get().getWorld(playerRef.getWorldUuid()).getName();
         if (currentWorldName.equalsIgnoreCase(sessionWorldName)) {
            Player playerComponent = ref.getStore().getComponent(ref, Player.getComponentType());
            World previousWorld = Universe.get().getWorld(prefabEditSession.getWorldArrivedFrom());
            Transform previousPosition;
            if (previousWorld != null) {
               previousPosition = playerComponent.getPlayerConfigData().getPerWorldData(previousWorld.getName()).getLastPosition();
            } else {
               previousWorld = Universe.get().getDefaultWorld();
               previousPosition = previousWorld.getWorldConfig().getSpawnProvider().getSpawnPoint(previousWorld, playerRef.getUuid());
            }

            Teleport teleportComponent = Teleport.createForPlayer(previousWorld, previousPosition);
            prefabEditSession.clearSelectedPrefab(ref, store);
            return CompletableFuture.runAsync(() -> ref.getStore().putComponent(ref, Teleport.getComponentType(), teleportComponent), world);
         } else {
            return prefabEditSessionManager.sendToEditWorld(ref, world, playerRef);
         }
      }
   }
}
