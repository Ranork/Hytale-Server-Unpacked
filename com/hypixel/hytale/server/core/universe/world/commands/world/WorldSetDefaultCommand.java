package com.hypixel.hytale.server.core.universe.world.commands.world;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.universe.Universe;
import javax.annotation.Nonnull;

public class WorldSetDefaultCommand extends CommandBase {
   @Nonnull
   private final RequiredArg<String> nameArg = this.withRequiredArg("name", "server.commands.world.setdefault.arg.name.desc", ArgTypes.STRING);

   public WorldSetDefaultCommand() {
      super("setdefault", "server.commands.world.setdefault.desc");
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      CommandSender sender = context.sender();
      String worldName = context.get(this.nameArg);
      if (Universe.get().getWorld(worldName) == null) {
         sender.sendMessage(Message.translation("server.world.notFound").param("worldName", worldName));
      } else {
         HytaleServer.get().getConfig().getDefaults().setWorld(worldName);
         sender.sendMessage(Message.translation("server.universe.defaultWorldSet").param("worldName", worldName));
      }
   }
}
