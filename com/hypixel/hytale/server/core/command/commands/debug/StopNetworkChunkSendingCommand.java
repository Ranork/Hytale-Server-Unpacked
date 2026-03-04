package com.hypixel.hytale.server.core.command.commands.debug;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.modules.entity.player.ChunkTracker;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class StopNetworkChunkSendingCommand extends AbstractPlayerCommand {
   @Nonnull
   private final RequiredArg<Boolean> sendNetworkChunksArg = this.withRequiredArg(
      "sendNetworkChunks", "server.commands.networkChunkSending.sendNetworkChunks.desc", ArgTypes.BOOLEAN
   );

   public StopNetworkChunkSendingCommand() {
      super("networkChunkSending", "server.commands.networkChunkSending.desc");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      ChunkTracker chunkTrackerComponent = store.getComponent(ref, ChunkTracker.getComponentType());
      if (chunkTrackerComponent == null) {
         playerRef.sendMessage(Message.translation("server.commands.networkChunkSending.noComponent"));
      } else {
         PlayerRef playerRefComponent = store.getComponent(ref, PlayerRef.getComponentType());

         assert playerRefComponent != null;

         chunkTrackerComponent.setReadyForChunks(this.sendNetworkChunksArg.get(context));
         playerRef.sendMessage(
            Message.translation("server.commands.networkChunkSending.set")
               .param("username", playerRefComponent.getUsername())
               .param("enabled", this.sendNetworkChunksArg.get(context))
         );
      }
   }
}
