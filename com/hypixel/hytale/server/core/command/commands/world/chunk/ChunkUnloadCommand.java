package com.hypixel.hytale.server.core.command.commands.world.chunk;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
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

public class ChunkUnloadCommand extends AbstractWorldCommand {
   @Nonnull
   private final RequiredArg<RelativeChunkPosition> chunkPosArg = this.withRequiredArg(
      "x z", "server.commands.chunk.unload.position.desc", ArgTypes.RELATIVE_CHUNK_POSITION
   );

   public ChunkUnloadCommand() {
      super("unload", "server.commands.chunk.unload.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      RelativeChunkPosition chunkPosition = this.chunkPosArg.get(context);
      Vector2i position = chunkPosition.getChunkPosition(context, store);
      ChunkStore chunkComponentStore = world.getChunkStore();
      long indexChunk = ChunkUtil.indexChunk(position.x, position.y);
      Ref<ChunkStore> chunkRef = chunkComponentStore.getChunkReference(indexChunk);
      if (chunkRef == null) {
         context.sendMessage(
            Message.translation("server.commands.chunk.unload.alreadyUnloaded")
               .param("chunkX", position.x)
               .param("chunkZ", position.y)
               .param("worldName", world.getName())
         );
      } else {
         chunkComponentStore.remove(chunkRef, RemoveReason.UNLOAD);
         world.getNotificationHandler().updateChunk(indexChunk);
         context.sendMessage(
            Message.translation("server.commands.chunk.unload.success")
               .param("chunkX", position.x)
               .param("chunkZ", position.y)
               .param("worldName", world.getName())
         );
      }
   }
}
