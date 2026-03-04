package com.hypixel.hytale.server.core.command.commands.debug.server;

import com.hypixel.hytale.common.util.FormatUtil;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import javax.annotation.Nonnull;

public class ServerStatsMemoryCommand extends CommandBase {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_SERVER_STATS_FULL_INFO_UNAVAILABLE = Message.translation("server.commands.server.stats.fullInfoUnavailable");

   public ServerStatsMemoryCommand() {
      super("memory", "server.commands.server.stats.memory.desc");
      this.addAliases("mem");
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      if (ManagementFactory.getOperatingSystemMXBean() instanceof OperatingSystemMXBean sunOSBean) {
         context.sendMessage(
            Message.translation("server.commands.server.stats.memory.fullUsageInfo")
               .param("totalPhysicalMemory", FormatUtil.bytesToString(sunOSBean.getTotalPhysicalMemorySize()))
               .param("freePhysicalMemory", FormatUtil.bytesToString(sunOSBean.getFreePhysicalMemorySize()))
               .param("totalSwapMemory", FormatUtil.bytesToString(sunOSBean.getTotalSwapSpaceSize()))
               .param("freeSwapMemory", FormatUtil.bytesToString(sunOSBean.getFreeSwapSpaceSize()))
         );
      } else {
         context.sendMessage(MESSAGE_COMMANDS_SERVER_STATS_FULL_INFO_UNAVAILABLE);
      }

      MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
      context.sendMessage(
         Message.translation("server.commands.server.stats.memory.usageInfo")
            .param("heapMemoryUsage", formatMemoryUsage(memoryMXBean.getHeapMemoryUsage()))
            .param("nonHeapMemoryUsage", formatMemoryUsage(memoryMXBean.getNonHeapMemoryUsage()))
            .param("objectsPendingFinalizationCount", memoryMXBean.getObjectPendingFinalizationCount())
      );
   }

   @Nonnull
   private static Message formatMemoryUsage(@Nonnull MemoryUsage memoryUsage) {
      return Message.translation("server.commands.server.stats.memory.usage")
         .param("init", FormatUtil.bytesToString(memoryUsage.getInit()))
         .param("used", FormatUtil.bytesToString(memoryUsage.getUsed()))
         .param("committed", FormatUtil.bytesToString(memoryUsage.getCommitted()))
         .param("max", FormatUtil.bytesToString(memoryUsage.getMax()))
         .param("free", FormatUtil.bytesToString(memoryUsage.getMax() - memoryUsage.getCommitted()));
   }
}
