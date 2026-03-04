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

public class WorldRemoveCommand extends CommandBase {
   public static final Message MESSAGE_UNIVERSE_REMOVE_WORLD_NOT_FOUND = Message.translation("server.universe.removeworld.notFound");
   public static final Message MESSAGE_UNIVERSE_REMOVE_WORLD_ONLY_ONE_WORLD_LOADED = Message.translation("server.universe.removeworld.onlyOneWorldLoaded");
   public static final Message MESSAGE_UNIVERSE_REMOVE_WORLD_CHANGE_DEFAULT_WORLD = Message.translation("server.universe.removeworld.changeDefaultWorld");
   @Nonnull
   private final RequiredArg<String> nameArg = this.withRequiredArg("name", "server.commands.removeworld.arg.name.desc", ArgTypes.STRING);

   public WorldRemoveCommand() {
      super("remove", "server.commands.removeworld.desc");
      this.addAliases("rm");
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      CommandSender sender = context.sender();
      String name = context.get(this.nameArg);
      if (Universe.get().getWorld(name) == null) {
         sender.sendMessage(MESSAGE_UNIVERSE_REMOVE_WORLD_NOT_FOUND);
      } else if (Universe.get().getWorlds().size() == 1) {
         sender.sendMessage(MESSAGE_UNIVERSE_REMOVE_WORLD_ONLY_ONE_WORLD_LOADED);
      } else if (name.equalsIgnoreCase(HytaleServer.get().getConfig().getDefaults().getWorld())) {
         sender.sendMessage(MESSAGE_UNIVERSE_REMOVE_WORLD_CHANGE_DEFAULT_WORLD);
      } else {
         Universe.get().removeWorld(name);
         sender.sendMessage(Message.translation("server.universe.removeworld.success").param("worldName", name));
      }
   }
}
