package com.hypixel.hytale.server.npc.commands;

import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.common.benchmark.TimeDistributionRecorder;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.util.SensorSupportBenchmark;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.Formatter;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class NPCBenchmarkCommand extends CommandBase {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_NPC_BENCHMARK_START_FAILED = Message.translation("server.commands.npc.benchmark.startFailed");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_NPC_BENCHMARK_DONE = Message.translation("server.commands.npc.benchmark.done");
   @Nonnull
   private final FlagArg roleArg = this.withFlagArg("roles", "server.commands.npc.benchmark.role.desc");
   @Nonnull
   private final FlagArg sensorSupportArg = this.withFlagArg("sensorsupport", "server.commands.npc.benchmark.sensor.desc");
   @Nonnull
   private final OptionalArg<Double> secondsArg = this.withOptionalArg("seconds", "server.commands.npc.benchmark.role.seconds", ArgTypes.DOUBLE)
      .addValidator(Validators.greaterThan(0.0));

   public NPCBenchmarkCommand() {
      super("benchmark", "server.commands.npc.benchmark.desc");
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      double seconds = this.secondsArg.provided(context) ? this.secondsArg.get(context) : 30.0;
      boolean success;
      if (this.roleArg.get(context)) {
         success = NPCPlugin.get()
            .startRoleBenchmark(
               seconds,
               distribution -> {
                  StringBuilder sb = new StringBuilder().append("Role benchmark seconds=").append(seconds).append('\n');
                  Formatter formatter = new Formatter(sb);
                  if (!distribution.isEmpty()) {
                     TimeDistributionRecorder recorder = (TimeDistributionRecorder)distribution.get(-1);
                     recorder.formatHeader(formatter);
                     sb.append('\n');
                     IntArrayList sortedIndices = new IntArrayList(distribution.keySet());
                     sortedIndices.rem(-1);
                     sortedIndices.sort(
                        (o1, o2) -> Double.compare(
                           ((TimeDistributionRecorder)distribution.get(o1)).getAverage(), ((TimeDistributionRecorder)distribution.get(o2)).getAverage()
                        )
                     );

                     for (int i = 0; i < sortedIndices.size(); i++) {
                        int role = sortedIndices.getInt(i);
                        logRoleDistribution((TimeDistributionRecorder)distribution.get(role), sb, formatter, NPCPlugin.get().getName(role));
                     }

                     logRoleDistribution((TimeDistributionRecorder)distribution.get(-1), sb, formatter, "ALL");
                  }

                  context.sendMessage(MESSAGE_COMMANDS_NPC_BENCHMARK_DONE);
                  NPCPlugin.get().getLogger().at(Level.INFO).log(sb.toString());
               }
            );
      } else if (this.sensorSupportArg.get(context)) {
         success = NPCPlugin.get().startSensorSupportBenchmark(seconds, sensorSupportData -> {
            StringBuilder sb = new StringBuilder().append("PositionCache benchmark seconds=").append(seconds).append('\n');
            Formatter formatter = new Formatter(sb);
            if (!sensorSupportData.isEmpty()) {
               IntArrayList sortedIndices = new IntArrayList(sensorSupportData.keySet());
               sortedIndices.rem(-1);
               sortedIndices.sort((o1, o2) -> NPCPlugin.get().getName(o1).compareToIgnoreCase(NPCPlugin.get().getName(o2)));
               SensorSupportBenchmark data = (SensorSupportBenchmark)sensorSupportData.get(-1);
               sb.append("PositionCache Update Times\n");
               data.formatHeaderUpdateTimes(formatter);
               sb.append('\n');

               for (int i = 0; i < sortedIndices.size(); i++) {
                  int role = sortedIndices.getInt(i);
                  SensorSupportBenchmark bm = (SensorSupportBenchmark)sensorSupportData.get(role);
                  if (bm.haveUpdateTimes()) {
                     logSensorSupportUpdateTime(bm, sb, formatter, NPCPlugin.get().getName(role));
                  }
               }

               logSensorSupportUpdateTime((SensorSupportBenchmark)sensorSupportData.get(-1), sb, formatter, "ALL");
               sb.append("PositionCache Line of sight\n");
               data.formatHeaderLoS(formatter);
               sb.append('\n');

               for (int ix = 0; ix < sortedIndices.size(); ix++) {
                  int role = sortedIndices.getInt(ix);
                  logSensorSupportLoS((SensorSupportBenchmark)sensorSupportData.get(role), sb, formatter, NPCPlugin.get().getName(role));
               }

               logSensorSupportLoS((SensorSupportBenchmark)sensorSupportData.get(-1), sb, formatter, "ALL");
            }

            context.sendMessage(MESSAGE_COMMANDS_NPC_BENCHMARK_DONE);
            NPCPlugin.get().getLogger().at(Level.INFO).log(sb.toString());
         });
      } else {
         success = false;
      }

      if (success) {
         context.sendMessage(Message.translation("server.commands.npc.benchmark.startedFor").param("seconds", seconds));
      } else {
         context.sendMessage(MESSAGE_COMMANDS_NPC_BENCHMARK_START_FAILED);
      }
   }

   private static void logRoleDistribution(@Nonnull TimeDistributionRecorder rec, @Nonnull StringBuilder sb, @Nonnull Formatter formatter, @Nonnull String name) {
      rec.formatValues(formatter, 10000L);
      sb.append("|").append(name).append('\n');
   }

   private static void logSensorSupportUpdateTime(
      @Nonnull SensorSupportBenchmark bm, @Nonnull StringBuilder sb, @Nonnull Formatter formatter, @Nonnull String name
   ) {
      bm.formatValuesUpdateTimePlayer(formatter);
      sb.append('|').append(name).append('\n');
      bm.formatValuesUpdateTimeEntity(formatter);
      sb.append('|').append(name).append('\n');
   }

   private static void logSensorSupportLoS(@Nonnull SensorSupportBenchmark bm, @Nonnull StringBuilder sb, @Nonnull Formatter formatter, @Nonnull String name) {
      if (bm.formatValuesLoS(formatter)) {
         sb.append('|').append(name).append('\n');
      }
   }
}
