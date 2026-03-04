package com.hypixel.hytale.server.core.universe.world.commands.block;

import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import javax.annotation.Nonnull;

public class BlockSetStateCommand extends SimpleBlockCommand {
   private final RequiredArg<String> stateArg = this.withRequiredArg("state", "server.commands.block.setstate.arg.state", ArgTypes.STRING);

   public BlockSetStateCommand() {
      super("setstate", "server.commands.block.setstate.desc");
   }

   @Override
   protected void executeWithBlock(@Nonnull CommandContext context, @Nonnull WorldChunk chunk, int x, int y, int z) {
      int blockId = chunk.getBlock(x, y, z);
      BlockType blockType = BlockType.getAssetMap().getAsset(blockId);
      String state = context.get(this.stateArg);
      chunk.setBlockInteractionState(x, y, z, blockType, state, true);
   }
}
