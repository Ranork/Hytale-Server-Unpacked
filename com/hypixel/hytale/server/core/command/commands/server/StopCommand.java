package com.hypixel.hytale.server.core.command.commands.server;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.ShutdownReason;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import javax.annotation.Nonnull;

public class StopCommand extends CommandBase {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_STOP_SUCCESS = Message.translation("server.commands.stop.success");
   @Nonnull
   private final FlagArg crashFlag = this.withFlagArg("crash", "server.commands.stop.crash.desc");

   public StopCommand() {
      super("stop", "server.commands.stop.desc");
      this.addAliases("shutdown");
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      context.sendMessage(MESSAGE_COMMANDS_STOP_SUCCESS);
      if (this.crashFlag.provided(context)) {
         HytaleServer.get().shutdownServer(ShutdownReason.CRASH);
      } else {
         HytaleServer.get().shutdownServer();
      }
   }
}
