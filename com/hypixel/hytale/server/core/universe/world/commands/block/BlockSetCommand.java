package com.hypixel.hytale.server.core.universe.world.commands.block;

import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import javax.annotation.Nonnull;

public class BlockSetCommand extends SimpleBlockCommand {
   private final RequiredArg<BlockType> blockArg = this.withRequiredArg("block", "server.commands.block.set.arg.block", ArgTypes.BLOCK_TYPE_ASSET);

   public BlockSetCommand() {
      super("set", "server.commands.block.set.desc");
      this.setPermissionGroup(GameMode.Creative);
   }

   @Override
   protected void executeWithBlock(@Nonnull CommandContext context, @Nonnull WorldChunk chunk, int x, int y, int z) {
      CommandSender sender = context.sender();
      BlockType blockType = context.get(this.blockArg);
      chunk.setBlock(x, y, z, blockType);
      sender.sendMessage(
         Message.translation("server.commands.block.set.success").param("x", x).param("y", y).param("z", z).param("id", blockType.getId().toString())
      );
   }
}
