package com.hypixel.hytale.server.core.command.commands.world.chunk;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector2i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.RelativeChunkPosition;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class ChunkRegenerateCommand extends AbstractWorldCommand {
   @Nonnull
   private final RequiredArg<RelativeChunkPosition> chunkPosArg = this.withRequiredArg(
      "x z", "server.commands.chunk.regenerate.position.desc", ArgTypes.RELATIVE_CHUNK_POSITION
   );

   public ChunkRegenerateCommand() {
      super("regenerate", "server.commands.chunk.regenerate.desc", true);
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      Vector2i chunkPosition = this.chunkPosArg.get(context).getChunkPosition(context, store);
      long chunkIndex = ChunkUtil.indexChunk(chunkPosition.x, chunkPosition.y);
      ChunkStore chunkStore = world.getChunkStore();
      chunkStore.getChunkReferenceAsync(chunkIndex, 9)
         .thenAccept(
            chunkRef -> world.execute(
               () -> context.sendMessage(
                  Message.translation("server.commands.chunk.regenerate.success")
                     .param("chunkX", chunkPosition.x)
                     .param("chunkZ", chunkPosition.y)
                     .param("worldName", world.getName())
               )
            )
         );
   }
}
