package com.hypixel.hytale.builtin.commandmacro;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import javax.annotation.Nonnull;

public class EchoCommand extends CommandBase {
   @Nonnull
   private final RequiredArg<String> messageArg = this.withRequiredArg("message", "server.commands.echos.message.desc", ArgTypes.STRING);

   public EchoCommand() {
      super("echo", "server.commands.echos.desc");
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      context.sender().sendMessage(Message.raw(this.messageArg.get(context).replace("\"", "")));
   }
}
