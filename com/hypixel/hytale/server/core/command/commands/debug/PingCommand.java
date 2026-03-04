package com.hypixel.hytale.server.core.command.commands.debug;

import com.hypixel.hytale.common.util.FormatUtil;
import com.hypixel.hytale.common.util.StringUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.metrics.metric.HistoricMetric;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.packets.connection.PongType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PingCommand extends AbstractTargetPlayerCommand {
   @Nonnull
   private final FlagArg detailFlag = this.withFlagArg("detail", "server.commands.ping.detail.desc");

   public PingCommand() {
      super("ping", "server.commands.ping.desc");
      this.setPermissionGroup(GameMode.Adventure);
      this.addSubCommand(new PingCommand.Clear());
      this.addSubCommand(new PingCommand.Graph());
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context,
      @Nullable Ref<EntityStore> sourceRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull PlayerRef playerRef,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      if (this.detailFlag.provided(context)) {
         this.sendDetailedMessage(context, playerRef);
      } else {
         this.sendShortMessage(context, playerRef);
      }
   }

   private void sendDetailedMessage(@Nonnull CommandContext context, @Nonnull PlayerRef playerRef) {
      Message msg = Message.join(Message.raw(playerRef.getUsername()), Message.raw(" ping:"));

      for (PongType pingType : PongType.values()) {
         PacketHandler.PingInfo pingInfo = playerRef.getPacketHandler().getPingInfo(pingType);
         HistoricMetric historicMetric = pingInfo.getPingMetricSet();
         long[] periods = historicMetric.getPeriodsNanos();
         msg.insert(Message.raw("\n" + pingType.name() + ":\n"));

         for (int i = 0; i < periods.length; i++) {
            String length = FormatUtil.timeUnitToString(periods[i], TimeUnit.NANOSECONDS, true);
            double average = historicMetric.getAverage(i);
            long max = historicMetric.calculateMax(i);
            long min = historicMetric.calculateMin(i);
            String value = FormatUtil.simpleTimeUnitFormat(min, average, max, PacketHandler.PingInfo.TIME_UNIT, TimeUnit.MILLISECONDS, 3);
            msg.insert(Message.raw("  (" + length + "): " + " ".repeat(Math.max(0, 24 - value.length())) + value + "\n"));
         }

         msg.insert(Message.raw("  Queue: " + FormatUtil.simpleFormat(pingInfo.getPacketQueueMetric())));
      }

      context.sendMessage(msg);
   }

   private void sendShortMessage(@Nonnull CommandContext context, @Nonnull PlayerRef playerRef) {
      String length = FormatUtil.timeUnitToString(1L, TimeUnit.SECONDS, true);
      Message msg = Message.join(Message.raw(playerRef.getUsername()), Message.raw(" ping  (" + length + "):"));

      for (PongType pingType : PongType.values()) {
         HistoricMetric historicMetric = playerRef.getPacketHandler().getPingInfo(pingType).getPingMetricSet();
         double average = historicMetric.getAverage(0);
         long max = historicMetric.calculateMax(0);
         long min = historicMetric.calculateMin(0);
         String value = FormatUtil.simpleTimeUnitFormat(min, average, max, PacketHandler.PingInfo.TIME_UNIT, TimeUnit.MILLISECONDS, 3);
         msg.insert(Message.raw("\n" + pingType.name() + ":" + " ".repeat(Math.max(0, 24 - value.length())) + value));
      }

      context.sendMessage(msg);
   }

   private static class Clear extends AbstractTargetPlayerCommand {
      @Nonnull
      private static final Message MESSAGE_COMMANDS_PING_HISTORY_CLEARED = Message.translation("server.commands.ping.historyCleared");

      public Clear() {
         super("clear", "server.commands.ping.clear.desc");
         this.setPermissionGroup(GameMode.Adventure);
         this.addAliases("reset");
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context,
         @Nullable Ref<EntityStore> sourceRef,
         @Nonnull Ref<EntityStore> ref,
         @Nonnull PlayerRef playerRef,
         @Nonnull World world,
         @Nonnull Store<EntityStore> store
      ) {
         for (PongType pingType : PongType.values()) {
            playerRef.getPacketHandler().getPingInfo(pingType).clear();
         }

         context.sendMessage(MESSAGE_COMMANDS_PING_HISTORY_CLEARED);
      }
   }

   private static class Graph extends AbstractTargetPlayerCommand {
      @Nonnull
      private final DefaultArg<Integer> widthArg = this.withDefaultArg(
         "width", "server.commands.ping.graph.width.desc", ArgTypes.INTEGER, 100, "server.commands.ping.graph.width.default"
      );
      @Nonnull
      private final DefaultArg<Integer> heightArg = this.withDefaultArg(
         "height", "server.commands.ping.graph.height.desc", ArgTypes.INTEGER, 10, "server.commands.ping.graph.height.default"
      );

      public Graph() {
         super("graph", "server.commands.ping.graph.desc");
         this.setPermissionGroup(GameMode.Adventure);
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context,
         @Nullable Ref<EntityStore> sourceRef,
         @Nonnull Ref<EntityStore> ref,
         @Nonnull PlayerRef playerRef,
         @Nonnull World world,
         @Nonnull Store<EntityStore> store
      ) {
         int width = this.widthArg.get(context);
         int height = this.heightArg.get(context);
         long startNanos = System.nanoTime();
         Message message = Message.empty();

         for (PongType pingType : PongType.values()) {
            message.insert(pingType + ":\n");
            PacketHandler.PingInfo pingInfo = playerRef.getPacketHandler().getPingInfo(pingType);
            HistoricMetric pingMetricSet = pingInfo.getPingMetricSet();
            long[] periods = pingMetricSet.getPeriodsNanos();

            for (int i = 0; i < periods.length; i++) {
               long period = periods[i];
               long max = pingMetricSet.calculateMax(i);
               long min = pingMetricSet.calculateMin(i);
               long[] historyTimestamps = pingMetricSet.getTimestamps(i);
               long[] historyValues = pingMetricSet.getValues(i);
               String historyLengthFormatted = FormatUtil.timeUnitToString(period, TimeUnit.NANOSECONDS, true);
               message.insert(Message.translation("server.commands.ping.graph.period").param("time", historyLengthFormatted));
               StringBuilder sb = new StringBuilder();
               StringUtil.generateGraph(
                  sb,
                  width,
                  height,
                  startNanos - period,
                  startNanos,
                  min,
                  max,
                  value -> FormatUtil.timeUnitToString(MathUtil.fastCeil(value), PacketHandler.PingInfo.TIME_UNIT),
                  historyTimestamps.length,
                  ii -> historyTimestamps[ii],
                  ii -> historyValues[ii]
               );
               message.insert(sb.toString());
            }
         }

         context.sendMessage(message);
      }
   }
}
