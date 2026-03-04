package com.hypixel.hytale.server.core.command.commands.utility.sleep;

import com.hypixel.hytale.common.util.FormatUtil;
import com.hypixel.hytale.metrics.metric.Metric;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

public class SleepTestCommand extends CommandBase {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_SLEEP_TEST_INTERRUPTED = Message.translation("server.commands.sleeptest.interrupted");
   @Nonnull
   private final DefaultArg<Integer> sleepArg = this.withDefaultArg(
      "sleep", "server.commands.sleeptest.sleep.desc", ArgTypes.INTEGER, 10, "server.commands.sleeptest.sleep.defaultDesc"
   );
   @Nonnull
   private final DefaultArg<Integer> countArg = this.withDefaultArg(
      "count", "server.commands.sleeptest.count.desc", ArgTypes.INTEGER, 1000, "server.commands.sleeptest.count.defaultDesc"
   );

   public SleepTestCommand() {
      super("test", "server.commands.sleeptest.desc");
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      int sleep = this.sleepArg.get(context);
      int count = this.countArg.get(context);
      CompletableFuture.runAsync(
         () -> {
            context.sendMessage(
               Message.translation("server.commands.sleeptest.starting")
                  .param("count", count)
                  .param("sleep", sleep)
                  .param("ms", FormatUtil.timeUnitToString((long)count * sleep, TimeUnit.MILLISECONDS))
            );

            try {
               Metric metricDelta = new Metric();
               Metric metricOffset = new Metric();

               for (int i = 0; i < count; i++) {
                  long before = System.nanoTime();
                  Thread.sleep(sleep);
                  long after = System.nanoTime();
                  long delta = after - before;
                  metricDelta.add(delta);
                  long offset = delta - sleep * 1000000L;
                  metricOffset.add(offset);
               }

               context.sendMessage(
                  Message.translation("server.commands.sleeptest.test")
                     .param("deltaMin", metricDelta.getMin())
                     .param("deltaMax", metricDelta.getMax())
                     .param("deltaAvg", metricDelta.getAverage())
                     .param("deltaTime", FormatUtil.nanosToString((long)metricDelta.getAverage()))
                     .param("offsetMin", metricOffset.getMin())
                     .param("offsetMax", metricOffset.getMax())
                     .param("offsetAvg", metricOffset.getAverage())
                     .param("offsetTime", FormatUtil.nanosToString((long)metricOffset.getAverage()))
               );
            } catch (InterruptedException var14) {
               context.sendMessage(MESSAGE_COMMANDS_SLEEP_TEST_INTERRUPTED);
               Thread.currentThread().interrupt();
            }
         }
      );
   }
}
