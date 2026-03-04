package com.hypixel.hytale.server.core.universe.world.commands.world;

import com.hypixel.hytale.common.util.CompletableFutureUtil;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.universe.Universe;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class WorldLoadCommand extends CommandBase {
   @Nonnull
   private final RequiredArg<String> nameArg = this.withRequiredArg("name", "server.commands.loadworld.arg.name.desc", ArgTypes.STRING);

   public WorldLoadCommand() {
      super("load", "server.commands.loadworld.desc");
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      CommandSender sender = context.sender();
      String name = context.get(this.nameArg);
      if (Universe.get().getWorld(name) != null) {
         sender.sendMessage(Message.translation("server.universe.loadWorld.alreadyExists").param("worldName", name));
      } else if (!Universe.get().isWorldLoadable(name)) {
         sender.sendMessage(Message.translation("server.universe.loadWorld.notExist").param("worldName", name));
      } else {
         CompletableFutureUtil._catch(
            Universe.get()
               .loadWorld(name)
               .thenRun(() -> sender.sendMessage(Message.translation("server.universe.loadWorld.worldCreated").param("worldName", name)))
               .exceptionally(
                  throwable -> {
                     ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(throwable)).log("Failed to load world '%s'", name);
                     sender.sendMessage(
                        Message.translation("server.universe.loadWorld.failed")
                           .param("worldName", name)
                           .param("error", throwable.getCause() != null ? throwable.getCause().getMessage() : throwable.getMessage())
                     );
                     return null;
                  }
               )
         );
      }
   }
}
