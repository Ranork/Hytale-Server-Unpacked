package com.hypixel.hytale.server.core.universe.world.commands.world.perf;

import com.hypixel.hytale.common.util.FormatUtil;
import com.hypixel.hytale.common.util.StringUtil;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.metrics.metric.HistoricMetric;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

public class WorldPerfGraphCommand extends AbstractWorldCommand {
   @Nonnull
   private final DefaultArg<Integer> widthArg = this.withDefaultArg(
      "width", "server.commands.world.perf.graph.width.desc", ArgTypes.INTEGER, 100, "server.commands.world.perf.graph.width.default"
   );
   @Nonnull
   private final DefaultArg<Integer> heightArg = this.withDefaultArg(
      "height", "server.commands.world.perf.graph.height.desc", ArgTypes.INTEGER, 10, "server.commands.world.perf.graph.height.default"
   );

   public WorldPerfGraphCommand() {
      super("graph", "server.commands.world.perf.graph.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      Integer width = this.widthArg.get(context);
      Integer height = this.heightArg.get(context);
      long startNano = System.nanoTime();
      Message msg = Message.empty();
      HistoricMetric historicMetric = world.getBufferedTickLengthMetricSet();
      long[] periods = historicMetric.getPeriodsNanos();

      for (int i = 0; i < periods.length; i++) {
         long period = periods[i];
         long[] historyTimestamps = historicMetric.getTimestamps(i);
         long[] historyValues = historicMetric.getValues(i);
         String historyLengthFormatted = FormatUtil.timeUnitToString(period, TimeUnit.NANOSECONDS, true);
         msg.insert(Message.translation("server.commands.world.perf.graph").param("time", historyLengthFormatted));
         StringBuilder sb = new StringBuilder();
         StringUtil.generateGraph(
            sb,
            width,
            height,
            startNano - period,
            startNano,
            0.0,
            world.getTps(),
            value -> String.valueOf(MathUtil.round(value, 2)),
            historyTimestamps.length,
            ii -> historyTimestamps[ii],
            ii -> WorldPerfCommand.tpsFromDelta(historyValues[ii], (long)world.getTickStepNanos())
         );
         msg.insert(sb.toString()).insert("\n");
      }

      context.sendMessage(msg);
   }
}
