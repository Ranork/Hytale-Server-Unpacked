package com.hypixel.hytale.server.core.command.commands.debug.server;

import com.hypixel.hytale.common.util.FormatUtil;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

public class ServerStatsCpuCommand extends CommandBase {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_SERVER_STATS_FULL_INFO_UNAVAILABLE = Message.translation("server.commands.server.stats.fullInfoUnavailable");

   public ServerStatsCpuCommand() {
      super("cpu", "server.commands.server.stats.cpu.desc");
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
      OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
      if (operatingSystemMXBean instanceof com.sun.management.OperatingSystemMXBean sunOSBean) {
         context.sendMessage(
            Message.translation("server.commands.server.stats.cpu.fullUsageInfo")
               .param("systemLoad", sunOSBean.getSystemCpuLoad())
               .param("processLoad", sunOSBean.getProcessCpuLoad())
         );
      } else {
         context.sendMessage(MESSAGE_COMMANDS_SERVER_STATS_FULL_INFO_UNAVAILABLE);
      }

      context.sendMessage(
         Message.translation("server.commands.server.stats.cpu.usageInfo")
            .param("loadAverage", operatingSystemMXBean.getSystemLoadAverage())
            .param("processUptime", FormatUtil.timeUnitToString(runtimeMXBean.getUptime(), TimeUnit.MILLISECONDS))
      );
   }
}
