package com.hypixel.hytale.server.core.universe.world.commands.block;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import javax.annotation.Nonnull;

public class BlockSetTickingCommand extends SimpleBlockCommand {
   public BlockSetTickingCommand() {
      super("setticking", "server.commands.block.setticking.desc");
      this.setPermissionGroup(null);
   }

   @Override
   protected void executeWithBlock(@Nonnull CommandContext context, @Nonnull WorldChunk chunk, int x, int y, int z) {
      CommandSender sender = context.sender();
      chunk.setTicking(x, y, z, true);
      sender.sendMessage(Message.translation("server.commands.block.setticking.success").param("x", x).param("y", y).param("z", z));
   }
}
