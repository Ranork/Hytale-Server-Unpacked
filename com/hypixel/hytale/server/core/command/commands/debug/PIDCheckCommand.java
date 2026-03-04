package com.hypixel.hytale.server.core.command.commands.debug;

import com.hypixel.hytale.server.core.Constants;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.Options;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.util.ProcessUtil;
import javax.annotation.Nonnull;

public class PIDCheckCommand extends CommandBase {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_PID_CHECK_SINGLEPLAYER_ONLY = Message.translation("server.commands.pidcheck.singlePlayerOnly");
   @Nonnull
   private final FlagArg singleplayerFlag = this.withFlagArg("singleplayer", "server.commands.pidcheck.singleplayer.desc");
   @Nonnull
   private final OptionalArg<Integer> pidArg = this.withOptionalArg("pid", "server.commands.pidcheck.pid.desc", ArgTypes.INTEGER);

   public PIDCheckCommand() {
      super("pidcheck", "server.commands.pidcheck.desc");
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      if (this.singleplayerFlag.get(context)) {
         if (!Constants.SINGLEPLAYER) {
            context.sendMessage(MESSAGE_COMMANDS_PID_CHECK_SINGLEPLAYER_ONLY);
         } else {
            int pid = (Integer)Options.getOptionSet().valueOf(Options.CLIENT_PID);
            Message runningMessage = Message.translation(
               ProcessUtil.isProcessRunning(pid) ? "server.commands.pidcheck.isRunning" : "server.commands.pidcheck.isNotRunning"
            );
            context.sendMessage(Message.translation("server.commands.pidcheck.clientPIDRunning").param("pid", pid).param("running", runningMessage));
         }
      } else if (!this.pidArg.provided(context)) {
         context.sendMessage(Message.translation("server.commands.pidcheck.pidRequired"));
      } else {
         int pid = this.pidArg.get(context);
         Message runningMessage = Message.translation(
            ProcessUtil.isProcessRunning(pid) ? "server.commands.pidcheck.isRunning" : "server.commands.pidcheck.isNotRunning"
         );
         context.sendMessage(Message.translation("server.commands.pidcheck.PIDRunning").param("pid", pid).param("running", runningMessage));
      }
   }
}
