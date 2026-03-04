package com.hypixel.hytale.server.core.universe.world.commands.block;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.RelativeIntPosition;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public abstract class SimpleBlockCommand extends AbstractWorldCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_ERROR_EXCEPTION = Message.translation("server.commands.error.exception");
   @Nonnull
   private final RequiredArg<RelativeIntPosition> coordsArg = this.withRequiredArg(
      "x y z", "server.commands.block.set.arg.coords", ArgTypes.RELATIVE_BLOCK_POSITION
   );

   public SimpleBlockCommand(@Nonnull String name, @Nonnull String description) {
      super(name, description);
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      CommandSender sender = context.sender();
      RelativeIntPosition coords = context.get(this.coordsArg);
      Vector3i blockPos = coords.getBlockPosition(context, store);
      int x = blockPos.x;
      int y = blockPos.y;
      int z = blockPos.z;
      long chunkIndex = ChunkUtil.indexChunkFromBlock(x, z);
      world.getChunkAsync(chunkIndex).thenAcceptAsync(chunk -> this.executeWithBlock(context, chunk, x, y, z), world).exceptionally(t -> {
         ((HytaleLogger.Api)HytaleLogger.getLogger().at(Level.SEVERE).withCause(t)).log("Error getting chunk for command");
         sender.sendMessage(MESSAGE_COMMANDS_ERROR_EXCEPTION);
         return null;
      });
   }

   protected abstract void executeWithBlock(@Nonnull CommandContext var1, @Nonnull WorldChunk var2, int var3, int var4, int var5);
}
