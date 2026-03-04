package com.hypixel.hytale.server.core.command.commands.debug;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import javax.annotation.Nonnull;

public class MessageTranslationTestCommand extends CommandBase {
   public MessageTranslationTestCommand() {
      super("messagetest", "server.commands.messagetest.desc");
      this.addAliases("msgtest");
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      Message param = Message.translation("server.commands.message.container")
         .param("content", Message.translation("server.commands.message.example").param("random", 25));
      context.sender().sendMessage(param);
   }
}
