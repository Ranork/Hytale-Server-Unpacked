package com.hypixel.hytale.server.core.command.commands.world.chunk;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.player.ChunkTracker;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ChunkTrackerCommand extends AbstractTargetPlayerCommand {
   public ChunkTrackerCommand() {
      super("tracker", "server.commands.chunk.tracker.desc");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context,
      @Nullable Ref<EntityStore> sourceRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull PlayerRef playerRef,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      ChunkTracker chunkTrackerComponent = store.getComponent(ref, ChunkTracker.getComponentType());

      assert chunkTrackerComponent != null;

      ChunkStore chunkStore = world.getChunkStore();
      String loadedWorldChunks = Integer.toString(chunkStore.getLoadedChunksCount());
      context.sendMessage(
         Message.translation("server.commands.chunkTracker.summary")
            .param("maxChunksPerSecond", chunkTrackerComponent.getMaxChunksPerSecond())
            .param("maxChunksPerTick", chunkTrackerComponent.getMaxChunksPerTick())
            .param("minChunkLoadedRadius", chunkTrackerComponent.getMinLoadedChunksRadius())
            .param("maxHotChunkLoadedRadius", chunkTrackerComponent.getMaxHotLoadedChunksRadius())
            .param("loadedPlayerChunks", chunkTrackerComponent.getLoadedChunksCount())
            .param("loadingPlayerChunks", chunkTrackerComponent.getLoadingChunksCount())
            .param("loadedWorldChunks", loadedWorldChunks)
      );
   }
}
