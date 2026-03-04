package com.hypixel.hytale.server.core.universe.world.commands.world.perf;

import com.hypixel.hytale.common.util.FormatUtil;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.metrics.metric.HistoricMetric;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

public class WorldPerfCommand extends AbstractWorldCommand {
   public static final double PRECISION = 1000.0;
   @Nonnull
   private final FlagArg allFlag = this.withFlagArg("all", "server.commands.world.perf.all.desc");
   @Nonnull
   private final FlagArg deltaFlag = this.withFlagArg("delta", "server.commands.world.perf.delta.desc");

   public WorldPerfCommand() {
      super("perf", "server.commands.world.perf.desc");
      this.addSubCommand(new WorldPerfGraphCommand());
      this.addSubCommand(new WorldPerfResetCommand());
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      HistoricMetric historicMetric = world.getBufferedTickLengthMetricSet();
      long[] periods = historicMetric.getPeriodsNanos();
      int tickStepNanos = world.getTickStepNanos();
      Message msg = Message.empty();
      boolean showDelta = this.deltaFlag.provided(context);
      boolean showAll = this.allFlag.provided(context);
      if (context.sender() instanceof Player) {
         for (int i = 0; i < periods.length; i++) {
            String length = FormatUtil.timeUnitToString(periods[i], TimeUnit.NANOSECONDS, true);
            double average = historicMetric.getAverage(i);
            long min = historicMetric.calculateMin(i);
            long max = historicMetric.calculateMax(i);
            if (showDelta) {
               String value = FormatUtil.simpleTimeUnitFormat(min, average, max, TimeUnit.NANOSECONDS, TimeUnit.MILLISECONDS, 3);
               String padding = " ".repeat(Math.max(0, 24 - value.length()));
               msg.insert(
                  Message.translation("server.commands.world.perf.period").param("length", length).param("padding", padding).param("value", value).insert("\n")
               );
            } else {
               msg.insert(
                  Message.translation("server.commands.world.perf.tpsTime")
                     .param("time", length)
                     .param("tps", FormatUtil.simpleFormat(min, average, max, d1 -> tpsFromDelta(d1, (long)tickStepNanos), 2))
                     .insert("\n")
               );
            }
         }
      } else {
         String tickLimitFormatted = FormatUtil.simpleTimeUnitFormat(tickStepNanos, TimeUnit.NANOSECONDS, 3);
         msg.insert(Message.translation("server.commands.world.perf.tickLimit").param("tickLimit", tickLimitFormatted).insert("\n"));

         for (int ix = 0; ix < periods.length; ix++) {
            String length = FormatUtil.timeUnitToString(periods[ix], TimeUnit.NANOSECONDS, true);
            double average = historicMetric.getAverage(ix);
            long min = historicMetric.calculateMin(ix);
            long max = historicMetric.calculateMax(ix);
            if (showDelta) {
               String value = FormatUtil.simpleTimeUnitFormat(min, average, max, TimeUnit.NANOSECONDS, TimeUnit.MILLISECONDS, 3);
               String padding = " ".repeat(Math.max(0, 24 - value.length()));
               msg.insert(
                  Message.translation("server.commands.world.perf.period").param("length", length).param("padding", padding).param("value", value).insert("\n")
               );
            } else {
               msg.insert(
                  Message.translation("server.commands.world.perf.tpsMinMaxMetric")
                     .param("time", length)
                     .param("min", tpsFromDelta(max, (long)tickStepNanos))
                     .param("avg", tpsFromDelta(average, (long)tickStepNanos))
                     .param("max", tpsFromDelta(min, (long)tickStepNanos))
                     .insert("\n")
               );
            }

            if (showAll) {
               msg.insert(
                  Message.translation("server.commands.world.perf.deltaMinMaxMetric")
                     .param("time", length)
                     .param("min", min)
                     .param("avg", (long)average)
                     .param("max", max)
                     .insert("\n")
               );
            }
         }
      }

      context.sendMessage(msg);
   }

   public static double tpsFromDelta(long delta, long min) {
      long adjustedDelta = delta;
      if (delta < min) {
         adjustedDelta = min;
      }

      return Math.round(1.0 / adjustedDelta * 1.0E9 * 1000.0) / 1000.0;
   }

   public static double tpsFromDelta(double delta, long min) {
      double adjustedDelta = delta;
      if (delta < min) {
         adjustedDelta = min;
      }

      return Math.round(1.0 / adjustedDelta * 1.0E9 * 1000.0) / 1000.0;
   }
}
