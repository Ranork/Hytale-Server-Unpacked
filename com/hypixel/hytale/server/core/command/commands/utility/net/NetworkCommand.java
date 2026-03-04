package com.hypixel.hytale.server.core.command.commands.utility.net;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.knockback.KnockbackSystems;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.io.netty.LatencySimulationHandler;
import com.hypixel.hytale.server.core.modules.entity.player.KnockbackPredictionSystems;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.netty.channel.Channel;
import io.netty.handler.codec.quic.QuicStreamChannel;
import io.netty.handler.codec.quic.QuicStreamPriority;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NetworkCommand extends AbstractCommandCollection {
   public NetworkCommand() {
      super("network", "server.commands.network.desc");
      this.addAliases("net");
      this.addSubCommand(new NetworkCommand.LatencySimulationCommand());
      this.addSubCommand(new NetworkCommand.StreamPriorityCommand());
      this.addSubCommand(new NetworkCommand.ServerKnockbackCommand());
      this.addSubCommand(new NetworkCommand.DebugKnockbackCommand());
   }

   static class DebugKnockbackCommand extends CommandBase {
      DebugKnockbackCommand() {
         super("debugknockback", "server.commands.network.debugknockback.desc");
      }

      @Override
      protected void executeSync(@Nonnull CommandContext context) {
         KnockbackPredictionSystems.DEBUG_KNOCKBACK_POSITION = !KnockbackPredictionSystems.DEBUG_KNOCKBACK_POSITION;
         context.sendMessage(
            Message.translation("server.commands.network.knockbackDebugEnabled").param("enabled", KnockbackPredictionSystems.DEBUG_KNOCKBACK_POSITION)
         );
      }
   }

   public static class LatencySimulationCommand extends AbstractCommandCollection {
      public LatencySimulationCommand() {
         super("latencysimulation", "server.commands.latencySimulation.desc");
         this.addAliases("latsim");
         this.addSubCommand(new NetworkCommand.LatencySimulationCommand.Set());
         this.addSubCommand(new NetworkCommand.LatencySimulationCommand.Reset());
      }

      static class Reset extends AbstractTargetPlayerCommand {
         @Nonnull
         private static final Message MESSAGE_COMMANDS_LATENCY_SIMULATION_RESET_SUCCESS = Message.translation("server.commands.latencySimulation.reset.success");

         Reset() {
            super("reset", "server.commands.latencySimulation.reset.desc");
            this.addAliases("clear");
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
            Channel channel = playerRef.getPacketHandler().getChannel();
            LatencySimulationHandler.setLatency(channel, 0L, TimeUnit.MILLISECONDS);
            context.sendMessage(MESSAGE_COMMANDS_LATENCY_SIMULATION_RESET_SUCCESS);
         }
      }

      static class Set extends AbstractTargetPlayerCommand {
         @Nonnull
         private final RequiredArg<Integer> delayArg = this.withRequiredArg("delay", "server.commands.latencySimulation.set.delay.desc", ArgTypes.INTEGER);

         Set() {
            super("set", "server.commands.latencySimulation.set.desc");
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
            int delay = this.delayArg.get(context);
            Channel channel = playerRef.getPacketHandler().getChannel();
            LatencySimulationHandler.setLatency(channel, delay, TimeUnit.MILLISECONDS);
            context.sendMessage(Message.translation("server.commands.latencySimulation.set.success").param("millis", delay));
         }
      }
   }

   static class ServerKnockbackCommand extends CommandBase {
      ServerKnockbackCommand() {
         super("serverknockback", "server.commands.network.serverknockback.desc");
      }

      @Override
      protected void executeSync(@Nonnull CommandContext context) {
         KnockbackSystems.ApplyPlayerKnockback.DO_SERVER_PREDICTION = !KnockbackSystems.ApplyPlayerKnockback.DO_SERVER_PREDICTION;
         context.sendMessage(
            Message.translation("server.commands.network.knockbackServerPredictionEnabled")
               .param("enabled", KnockbackSystems.ApplyPlayerKnockback.DO_SERVER_PREDICTION)
         );
      }
   }

   public static class StreamPriorityCommand extends AbstractCommandCollection {
      private static final Map<String, Map<NetworkChannel, QuicStreamPriority>> PRESETS = Map.of(
         "default",
         PacketHandler.DEFAULT_STREAM_PRIORITIES,
         "equal",
         priority(0, 0, 0),
         "tiered",
         priority(0, 1, 2),
         "maplow",
         priority(0, 0, 1),
         "datalow",
         priority(0, 1, 1),
         "chunkshi",
         priority(1, 0, 2),
         "maphi",
         priority(1, 2, 0)
      );

      public StreamPriorityCommand() {
         super("streampriority", "server.commands.network.streamPriority.desc");
         this.addAliases("sp");
         this.addSubCommand(new NetworkCommand.StreamPriorityCommand.Set());
         this.addSubCommand(new NetworkCommand.StreamPriorityCommand.Preset());
         this.addSubCommand(new NetworkCommand.StreamPriorityCommand.Reset());
      }

      private static void applyPriorities(@Nonnull PlayerRef playerRef, @Nonnull Map<NetworkChannel, QuicStreamPriority> priorities) {
         PacketHandler handler = playerRef.getPacketHandler();

         for (Entry<NetworkChannel, QuicStreamPriority> entry : priorities.entrySet()) {
            if (handler.getChannel(entry.getKey()) instanceof QuicStreamChannel quicStreamChannel) {
               quicStreamChannel.updatePriority(entry.getValue());
            }
         }
      }

      private static String formatPriorities(@Nonnull Map<NetworkChannel, QuicStreamPriority> priorities) {
         StringBuilder sb = new StringBuilder();

         for (NetworkChannel channel : NetworkChannel.VALUES) {
            QuicStreamPriority priority = priorities.get(channel);
            if (priority != null) {
               if (!sb.isEmpty()) {
                  sb.append(", ");
               }

               sb.append(channel.name()).append('=').append(priority.urgency());
            }
         }

         return sb.toString();
      }

      private static Map<NetworkChannel, QuicStreamPriority> priority(int defaultUrgency, int chunksUrgency, int worldMapUrgency) {
         return Map.of(
            NetworkChannel.Default,
            new QuicStreamPriority(defaultUrgency, true),
            NetworkChannel.Chunks,
            new QuicStreamPriority(chunksUrgency, true),
            NetworkChannel.WorldMap,
            new QuicStreamPriority(worldMapUrgency, true)
         );
      }

      static class Preset extends AbstractTargetPlayerCommand {
         @Nonnull
         private final RequiredArg<String> presetArg = this.withRequiredArg(
            "preset", "server.commands.network.streamPriority.preset.name.desc", ArgTypes.STRING
         );

         Preset() {
            super("preset", "server.commands.network.streamPriority.preset.desc");
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
            String name = this.presetArg.get(context).toLowerCase();
            Map<NetworkChannel, QuicStreamPriority> priorities = NetworkCommand.StreamPriorityCommand.PRESETS.get(name);
            if (priorities == null) {
               context.sendMessage(
                  Message.translation("server.commands.network.streamPriority.preset.unknown")
                     .param("name", name)
                     .param("presets", String.join(", ", NetworkCommand.StreamPriorityCommand.PRESETS.keySet()))
               );
            } else {
               NetworkCommand.StreamPriorityCommand.applyPriorities(playerRef, priorities);
               context.sendMessage(
                  Message.translation("server.commands.network.streamPriority.preset.success")
                     .param("name", name)
                     .param("priorities", NetworkCommand.StreamPriorityCommand.formatPriorities(priorities))
               );
            }
         }
      }

      static class Reset extends AbstractTargetPlayerCommand {
         Reset() {
            super("reset", "server.commands.network.streamPriority.reset.desc");
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
            NetworkCommand.StreamPriorityCommand.applyPriorities(playerRef, PacketHandler.DEFAULT_STREAM_PRIORITIES);
            context.sendMessage(
               Message.translation("server.commands.network.streamPriority.reset.success")
                  .param("priorities", NetworkCommand.StreamPriorityCommand.formatPriorities(PacketHandler.DEFAULT_STREAM_PRIORITIES))
            );
         }
      }

      static class Set extends AbstractTargetPlayerCommand {
         @Nonnull
         private final RequiredArg<NetworkChannel> channelArg = this.withRequiredArg(
            "channel",
            "server.commands.network.streamPriority.set.channel.desc",
            ArgTypes.forEnum("server.commands.network.streamPriority.channel", NetworkChannel.class)
         );
         @Nonnull
         private final RequiredArg<Integer> priorityArg = this.withRequiredArg(
            "priority", "server.commands.network.streamPriority.set.priority.desc", ArgTypes.INTEGER
         );

         Set() {
            super("set", "server.commands.network.streamPriority.set.desc");
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
            NetworkChannel networkChannel = this.channelArg.get(context);
            int priority = this.priorityArg.get(context);
            if (priority >= 0 && priority <= 255) {
               if (playerRef.getPacketHandler().getChannel(networkChannel) instanceof QuicStreamChannel quicStreamChannel) {
                  quicStreamChannel.updatePriority(new QuicStreamPriority(priority, true));
                  context.sendMessage(
                     Message.translation("server.commands.network.streamPriority.set.success")
                        .param("channel", networkChannel.name())
                        .param("priority", priority)
                  );
               } else {
                  context.sendMessage(Message.translation("server.commands.network.streamPriority.set.notQuic").param("channel", networkChannel.name()));
               }
            } else {
               context.sendMessage(Message.translation("server.commands.network.streamPriority.set.invalidPriority"));
            }
         }
      }
   }
}
