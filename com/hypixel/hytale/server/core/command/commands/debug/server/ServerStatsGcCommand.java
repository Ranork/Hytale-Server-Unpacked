package com.hypixel.hytale.server.core.command.commands.debug.server;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class ServerStatsGcCommand extends CommandBase {
   public ServerStatsGcCommand() {
      super("gc", "server.commands.server.stats.gc.desc");
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      for (GarbageCollectorMXBean garbageCollectorMXBean : ManagementFactory.getGarbageCollectorMXBeans()) {
         context.sendMessage(
            Message.translation("server.commands.server.stats.gc.usageInfo")
               .param("name", garbageCollectorMXBean.getName())
               .param("poolNames", Arrays.toString((Object[])garbageCollectorMXBean.getMemoryPoolNames()))
               .param("collectionCount", garbageCollectorMXBean.getCollectionCount())
               .param("collectionTime", garbageCollectorMXBean.getCollectionTime())
         );
      }
   }
}
