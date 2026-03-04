package com.hypixel.hytale.server.core.command.commands.world.chunk;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector2i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.RelativeChunkPosition;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class ChunkLoadCommand extends AbstractWorldCommand {
   @Nonnull
   private final RequiredArg<RelativeChunkPosition> chunkPosArg = this.withRequiredArg(
      "x z", "server.commands.chunk.load.position.desc", ArgTypes.RELATIVE_CHUNK_POSITION
   );
   @Nonnull
   private final FlagArg markDirtyArg = this.withFlagArg("markdirty", "server.commands.chunk.load.markdirty.desc");

   public ChunkLoadCommand() {
      super("load", "server.commands.chunk.load.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      RelativeChunkPosition chunkPosition = this.chunkPosArg.get(context);
      Vector2i position = chunkPosition.getChunkPosition(context, store);
      ChunkStore chunkStore = world.getChunkStore();
      long chunkIndex = ChunkUtil.indexChunk(position.x, position.y);
      Ref<ChunkStore> chunkRef = chunkStore.getChunkReference(chunkIndex);
      if (chunkRef != null && chunkRef.isValid()) {
         context.sendMessage(
            Message.translation("server.commands.chunk.load.alreadyLoaded")
               .param("chunkX", position.x)
               .param("chunkZ", position.y)
               .param("worldName", world.getName())
         );
      } else {
         context.sendMessage(
            Message.translation("server.commands.chunk.load.loading")
               .param("chunkX", position.x)
               .param("chunkZ", position.y)
               .param("worldName", world.getName())
         );
         world.getChunkAsync(position.x, position.y)
            .thenAccept(
               worldChunk -> world.execute(
                  () -> {
                     if (this.markDirtyArg.provided(context)) {
                        worldChunk.markNeedsSaving();
                     }

                     context.sendMessage(
                        Message.translation("server.commands.chunk.load.loaded")
                           .param("chunkX", position.x)
                           .param("chunkZ", position.y)
                           .param("worldName", world.getName())
                     );
                  }
               )
            );
      }
   }
}
