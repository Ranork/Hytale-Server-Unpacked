package com.hypixel.hytale.server.core.universe.world.commands.world;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.util.message.MessageFormat;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

public class WorldListCommand extends CommandBase {
   public WorldListCommand() {
      super("list", "server.commands.worlds.desc");
      this.addAliases("ls");
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      Set<Message> worlds = Universe.get().getWorlds().keySet().stream().map(Message::raw).collect(Collectors.toSet());
      Message message = MessageFormat.list(Message.translation("server.commands.worlds.header"), worlds);
      context.sender().sendMessage(message);
   }
}
