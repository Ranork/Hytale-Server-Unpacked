package com.hypixel.hytale.server.core.command.commands.server;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import javax.annotation.Nonnull;

public class MaxPlayersCommand extends CommandBase {
   @Nonnull
   private final OptionalArg<Integer> amountArg = this.withOptionalArg("amount", "server.commands.maxplayers.amount.desc", ArgTypes.INTEGER);

   public MaxPlayersCommand() {
      super("maxplayers", "server.commands.maxplayers.desc");
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      if (this.amountArg.provided(context)) {
         int maxPlayers = this.amountArg.get(context);
         HytaleServer.get().getConfig().setMaxPlayers(maxPlayers);
         context.sendMessage(Message.translation("server.commands.maxplayers.set").param("maxPlayers", maxPlayers));
      } else {
         int maxPlayers = HytaleServer.get().getConfig().getMaxPlayers();
         context.sendMessage(Message.translation("server.commands.maxplayers.get").param("maxPlayers", maxPlayers));
      }
   }
}
