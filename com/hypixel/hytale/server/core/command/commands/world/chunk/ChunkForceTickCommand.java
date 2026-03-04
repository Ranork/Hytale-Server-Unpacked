package com.hypixel.hytale.server.core.command.commands.world.chunk;

import com.hypixel.hytale.component.Ref;
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
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class ChunkForceTickCommand extends AbstractWorldCommand {
   @Nonnull
   private final RequiredArg<RelativeChunkPosition> chunkPosArg = this.withRequiredArg(
      "x z", "server.commands.chunk.forcetick.position.desc", ArgTypes.RELATIVE_CHUNK_POSITION
   );

   public ChunkForceTickCommand() {
      super("forcetick", "server.commands.chunk.forcetick.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      RelativeChunkPosition chunkPosition = this.chunkPosArg.get(context);
      Vector2i position = chunkPosition.getChunkPosition(context, store);
      ChunkStore chunkStore = world.getChunkStore();
      Store<ChunkStore> chunkStoreStore = chunkStore.getStore();
      long chunkIndex = ChunkUtil.indexChunk(position.x, position.y);
      Ref<ChunkStore> chunkRef = chunkStore.getChunkReference(chunkIndex);
      if (chunkRef != null && chunkRef.isValid()) {
         BlockChunk blockChunkComponent = chunkStoreStore.getComponent(chunkRef, BlockChunk.getComponentType());

         assert blockChunkComponent != null;

         for (int x = 0; x < 32; x++) {
            for (int y = 0; y < 320; y++) {
               for (int z = 0; z < 32; z++) {
                  blockChunkComponent.setTicking(x, y, z, true);
               }
            }
         }

         context.sendMessage(Message.translation("server.commands.forcechunktick.blocksInChunkTick").param("chunkX", position.x).param("chunkZ", position.y));
      } else {
         context.sendMessage(
            Message.translation("server.commands.errors.chunkNotLoaded")
               .param("chunkX", position.x)
               .param("chunkZ", position.y)
               .param("world", world.getName())
         );
         context.sendMessage(Message.translation("server.commands.forcechunktick.chunkLoadUsage").param("chunkX", position.x).param("chunkZ", position.y));
      }
   }
}
