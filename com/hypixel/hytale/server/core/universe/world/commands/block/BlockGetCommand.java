package com.hypixel.hytale.server.core.universe.world.commands.block;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import javax.annotation.Nonnull;

public class BlockGetCommand extends SimpleBlockCommand {
   public BlockGetCommand() {
      super("get", "server.commands.block.get.desc");
   }

   @Override
   protected void executeWithBlock(@Nonnull CommandContext context, @Nonnull WorldChunk chunk, int x, int y, int z) {
      CommandSender sender = context.sender();
      int blockId = chunk.getBlock(x, y, z);
      BlockType blockType = BlockType.getAssetMap().getAsset(blockId);
      int support = chunk.getSupportValue(x, y, z);
      sender.sendMessage(
         Message.translation("server.commands.block.get.info")
            .param("x", x)
            .param("y", y)
            .param("z", z)
            .param("id", blockType.getId().toString())
            .param("blockId", blockId)
            .param("support", support)
      );
   }
}
