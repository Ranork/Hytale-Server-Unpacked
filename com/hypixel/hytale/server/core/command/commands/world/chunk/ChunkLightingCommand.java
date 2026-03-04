package com.hypixel.hytale.server.core.command.commands.world.chunk;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector2i;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.RelativeIntPosition;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class ChunkLightingCommand extends AbstractWorldCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_CHUNKINFO_SERIALIZED = Message.translation("server.commands.chunkinfo.serialized");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_CHUNKINFO_SERIALIZED_FAILED = Message.translation("server.commands.chunkinfo.serialized.failed");
   @Nonnull
   private final RequiredArg<RelativeIntPosition> positionArg = this.withRequiredArg(
      "x y z", "server.commands.chunk.lighting.position.desc", ArgTypes.RELATIVE_BLOCK_POSITION
   );

   public ChunkLightingCommand() {
      super("lighting", "server.commands.chunklighting.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      Vector3i position = this.positionArg.get(context).getBlockPosition(context, store);
      ChunkStore chunkStore = world.getChunkStore();
      Store<ChunkStore> chunkStoreStore = chunkStore.getStore();
      Vector2i chunkPos = new Vector2i(ChunkUtil.chunkCoordinate(position.getX()), ChunkUtil.chunkCoordinate(position.getZ()));
      long chunkIndex = ChunkUtil.indexChunk(chunkPos.x, chunkPos.y);
      Ref<ChunkStore> chunkRef = chunkStore.getChunkReference(chunkIndex);
      if (chunkRef != null && chunkRef.isValid()) {
         BlockChunk blockChunkComponent = chunkStoreStore.getComponent(chunkRef, BlockChunk.getComponentType());

         assert blockChunkComponent != null;

         try {
            BlockSection section = blockChunkComponent.getSectionAtBlockY(position.y);
            String s = section.getLocalLight().octreeToString();
            HytaleLogger.getLogger().at(Level.INFO).log("Chunk light output for (%d, %d, %d): %s", position.x, position.y, position.z, s);
            context.sendMessage(MESSAGE_COMMANDS_CHUNKINFO_SERIALIZED);
         } catch (Throwable var14) {
            HytaleLogger.getLogger().at(Level.SEVERE).log("Failed to print chunk:", var14);
            context.sendMessage(MESSAGE_COMMANDS_CHUNKINFO_SERIALIZED_FAILED);
         }
      } else {
         context.sendMessage(
            Message.translation("server.commands.errors.chunkNotLoaded")
               .param("chunkX", chunkPos.x)
               .param("chunkZ", chunkPos.y)
               .param("world", world.getName())
         );
         context.sendMessage(Message.translation("server.commands.chunkinfo.load.usage").param("chunkX", chunkPos.x).param("chunkZ", chunkPos.y));
      }
   }
}
