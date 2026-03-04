package com.hypixel.hytale.server.core.command.commands.debug.server;

import com.hypixel.hytale.common.util.FormatUtil;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import javax.annotation.Nonnull;

public class ServerGCCommand extends CommandBase {
   public ServerGCCommand() {
      super("gc", "server.commands.server.gc.desc");
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      long before = Runtime.getRuntime().freeMemory();
      System.gc();
      long after = Runtime.getRuntime().freeMemory();
      long freedBytes = before - after;
      if (freedBytes >= 0L) {
         context.sendMessage(Message.translation("server.commands.server.gc.forcedgc").param("bytes", FormatUtil.bytesToString(freedBytes)));
      } else {
         context.sendMessage(Message.translation("server.commands.server.gc.forcedgc.increased").param("bytes", FormatUtil.bytesToString(-freedBytes)));
      }
   }
}
