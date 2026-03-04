package com.hypixel.hytale.server.core.command.commands.debug.stresstest;

import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.common.util.FormatUtil;
import com.hypixel.hytale.common.util.StringUtil;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.event.EventRegistration;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector2d;
import com.hypixel.hytale.metrics.MetricsRegistry;
import com.hypixel.hytale.metrics.metric.HistoricMetric;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.ShutdownReason;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncWorldCommand;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.event.events.player.AddPlayerToWorldEvent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.spawn.ISpawnProvider;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.sneakythrow.SneakyThrow;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.IOException;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StressTestStartCommand extends AbstractAsyncWorldCommand {
   @Nonnull
   protected static final AtomicReference<StressTestStartCommand.StressTestState> STATE = new AtomicReference<>(
      StressTestStartCommand.StressTestState.NOT_RUNNING
   );
   @Nonnull
   private static final String NAME_PREFIX = "bot-";
   @Nonnull
   public static final List<Bot> BOTS = Collections.synchronizedList(new ObjectArrayList());
   @Nonnull
   private static final Message MESSAGE_COMMANDS_STRESS_TEST_ALREADY_STARTED = Message.translation("server.commands.stresstest.alreadyStarted");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_STRESS_TEST_STARTED = Message.translation("server.commands.stresstest.started");
   @Nullable
   static StressTestStartCommand.DumpType DUMP_TYPE;
   @Nullable
   static Path DATE_PATH;
   @Nullable
   static EventRegistration<String, AddPlayerToWorldEvent> EVENT_REGISTRATION;
   @Nullable
   static ScheduledFuture<?> STRESS_TEST_BOT_TASK;
   @Nullable
   static ScheduledFuture<?> STRESS_TEST_DUMP_TASK;
   @Nonnull
   private final OptionalArg<String> nameArg = this.withOptionalArg("name", "server.commands.stresstest.start.name.desc", ArgTypes.STRING);
   @Nonnull
   private final DefaultArg<Integer> initCountArg = this.withDefaultArg(
         "initcount", "server.commands.stresstest.start.initcount.desc", ArgTypes.INTEGER, 0, "server.commands.stresstest.start.initcount.default"
      )
      .addValidator(Validators.greaterThanOrEqual(0));
   @Nonnull
   private final DefaultArg<Double> intervalArg = this.withDefaultArg(
         "interval", "server.commands.stresstest.start.interval.desc", ArgTypes.DOUBLE, 30.0, "server.commands.stresstest.start.interval.default"
      )
      .addValidator(Validators.greaterThan(0.0));
   @Nonnull
   private final DefaultArg<StressTestStartCommand.DumpType> dumptypeArg = this.withDefaultArg(
      "dumptype",
      "server.commands.stresstest.start.dumptype.desc",
      ArgTypes.forEnum("server.commands.parsing.argtype.enum.name", StressTestStartCommand.DumpType.class),
      StressTestStartCommand.DumpType.INTERVAL,
      "server.commands.stresstest.start.dumptype.default"
   );
   @Nonnull
   private final DefaultArg<Double> dumpintervalArg = this.withDefaultArg(
         "dumpinterval", "server.commands.stresstest.start.dumpinterval.desc", ArgTypes.DOUBLE, 300.0, "server.commands.stresstest.start.dumpinterval.default"
      )
      .addValidator(Validators.greaterThan(0.0));
   @Nonnull
   private final OptionalArg<Double> thresholdArg = this.withOptionalArg("threshold", "server.commands.stresstest.start.threshold.desc", ArgTypes.DOUBLE)
      .addValidator(Validators.greaterThan(0.0));
   @Nonnull
   private final DefaultArg<Double> percentileArg = this.withDefaultArg(
         "percentile", "server.commands.stresstest.start.percentile.desc", ArgTypes.DOUBLE, 0.95, "server.commands.stresstest.start.percentile.default"
      )
      .addValidator(Validators.range(0.0, 1.0));
   @Nonnull
   private final DefaultArg<Integer> viewRadiusArg = this.withDefaultArg(
         "viewradius", "server.commands.stresstest.start.viewradius.desc", ArgTypes.INTEGER, 192, "server.commands.stresstest.start.viewradius.default"
      )
      .addValidator(Validators.greaterThanOrEqual(32));
   @Nonnull
   private final DefaultArg<Double> radiusArg = this.withDefaultArg(
         "radius", "server.commands.stresstest.start.radius.desc", ArgTypes.DOUBLE, 384.0, "server.commands.stresstest.start.radius.default"
      )
      .addValidator(Validators.greaterThan(0.0));
   @Nonnull
   private final DefaultArg<Double> yheightArg = this.withDefaultArg(
      "yheight", "server.commands.stresstest.start.yheight.desc", ArgTypes.DOUBLE, 125.0, "server.commands.stresstest.start.yheight.default"
   );
   @Nonnull
   private final OptionalArg<Double> yheightMaxArg = this.withOptionalArg("yheightmax", "server.commands.stresstest.start.yheightmax.desc", ArgTypes.DOUBLE);
   @Nonnull
   private final DefaultArg<Double> flySpeedArg = this.withDefaultArg(
         "flySpeed", "server.commands.stresstest.start.flySpeed.desc", ArgTypes.DOUBLE, 8.0, "server.commands.stresstest.start.flySpeed.default"
      )
      .addValidator(Validators.greaterThan(0.0));
   @Nonnull
   private final FlagArg shutdownFlag = this.withFlagArg("shutdown", "server.commands.stresstest.start.shutdown.desc");

   public StressTestStartCommand() {
      super("start", "server.commands.stresstest.start.desc");
   }

   @Nonnull
   @Override
   protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context, @Nonnull World world) {
      ISpawnProvider spawnProvider = world.getWorldConfig().getSpawnProvider();
      Transform spawn = spawnProvider.getSpawnPoints()[0];
      String name = this.nameArg.provided(context) ? this.nameArg.get(context) : null;
      if (name != null && !StringUtil.isAlphaNumericHyphenUnderscoreString(name)) {
         context.sendMessage(Message.translation("server.commands.stresstest.invalidName").param("name", name));
         return CompletableFuture.completedFuture(null);
      } else {
         int viewRadius = this.viewRadiusArg.get(context);
         double radius = this.radiusArg.get(context);
         double yheight = this.yheightArg.get(context);
         double yheightMax = this.yheightMaxArg.provided(context) ? this.yheightMaxArg.get(context) : yheight + 10.0;
         double flySpeed = this.flySpeedArg.get(context);
         BotConfig config = new BotConfig(radius, new Vector2d(yheight, yheightMax), flySpeed, spawn, viewRadius);
         int initCount = this.initCountArg.get(context);
         double interval = this.intervalArg.get(context);
         StressTestStartCommand.DumpType dumpType = this.dumptypeArg.get(context);
         double dumpInterval = this.dumpintervalArg.get(context);
         int thresholdNanos = this.thresholdArg.provided(context) ? MathUtil.ceil(this.thresholdArg.get(context) * 1000000.0) : world.getTickStepNanos();
         double percentile = MathUtil.round(this.percentileArg.get(context), 4);
         boolean shutdown = this.shutdownFlag.get(context);
         if (!STATE.compareAndSet(StressTestStartCommand.StressTestState.NOT_RUNNING, StressTestStartCommand.StressTestState.RUNNING)) {
            context.sendMessage(MESSAGE_COMMANDS_STRESS_TEST_ALREADY_STARTED);
            return CompletableFuture.completedFuture(null);
         } else {
            try {
               start(name, world, config, initCount, interval, dumpType, dumpInterval, thresholdNanos, percentile, shutdown);
            } catch (IOException var27) {
               throw SneakyThrow.sneakyThrow(var27);
            }

            context.sendMessage(MESSAGE_COMMANDS_STRESS_TEST_STARTED);
            return CompletableFuture.completedFuture(null);
         }
      }
   }

   private static void start(
      @Nullable String name,
      @Nonnull World world,
      @Nonnull BotConfig config,
      int initCount,
      double interval,
      StressTestStartCommand.DumpType dumpType,
      double dumpInterval,
      long thresholdNanos,
      double percentile,
      boolean shutdown
   ) throws IOException {
      EVENT_REGISTRATION = HytaleServer.get().getEventBus().register(AddPlayerToWorldEvent.class, world.getName(), event -> {
         Holder<EntityStore> holder = event.getHolder();
         PlayerRef playerRefComponent = holder.getComponent(PlayerRef.getComponentType());

         assert playerRefComponent != null;

         if (playerRefComponent.getUsername().startsWith("bot-")) {
            InteractionManager manager = holder.getComponent(InteractionModule.get().getInteractionManagerComponent());
            holder.putComponent(TransformComponent.getComponentType(), new TransformComponent(config.spawn.getPosition(), config.spawn.getRotation()));
            manager.setHasRemoteClient(false);
         }
      });
      DUMP_TYPE = dumpType;
      DATE_PATH = MetricsRegistry.createDatePath(Paths.get("stress-test-dumps"), null, name != null ? "_" + name : null);
      if (!Files.exists(DATE_PATH)) {
         Files.createDirectories(DATE_PATH);
      }

      String percentileDisplay = StringUtil.trimEnd(Double.toString(MathUtil.round(percentile * 100.0, 2)), ".0");
      Path resultsPath = DATE_PATH.resolve("results.csv");
      Files.writeString(resultsPath, "avg,min,p25,p50,p75,max,p" + percentileDisplay + ",bots\n", StandardOpenOption.CREATE_NEW);
      int tickStepNanos = world.getTickStepNanos();
      AtomicInteger counter = new AtomicInteger();

      for (int i = 0; i < initCount; i++) {
         try {
            BOTS.add(new Bot("bot-" + counter.getAndIncrement(), config, tickStepNanos));
         } catch (SocketException | InterruptedException var20) {
            Thread.currentThread().interrupt();
            throw SneakyThrow.sneakyThrow(var20);
         }
      }

      if (DUMP_TYPE == StressTestStartCommand.DumpType.INTERVAL) {
         int dumpIntervalMillis = MathUtil.ceil(dumpInterval * 1000.0);
         STRESS_TEST_DUMP_TASK = HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(() -> {
            try {
               Path path = MetricsRegistry.createDumpPath(DATE_PATH, ".dump.json");
               HytaleServer.METRICS_REGISTRY.dumpToJson(path, HytaleServer.get());
            } catch (IOException var1x) {
               ((HytaleLogger.Api)HytaleLogger.getLogger().at(Level.SEVERE).withCause(var1x)).log("Failed to save dump!");
            }
         }, dumpIntervalMillis, dumpIntervalMillis, TimeUnit.MILLISECONDS);
      }

      int intervalMillis = MathUtil.ceil(interval * 1000.0);
      STRESS_TEST_BOT_TASK = HytaleServer.SCHEDULED_EXECUTOR
         .scheduleWithFixedDelay(
            () -> CompletableFuture.runAsync(
               SneakyThrow.sneakyRunnable(
                  () -> {
                     if (DUMP_TYPE == StressTestStartCommand.DumpType.NEW_BOT) {
                        Path path = MetricsRegistry.createDumpPath(DATE_PATH, ".dump.json");
                        HytaleServer.METRICS_REGISTRY.dumpToJson(path, HytaleServer.get());
                     }

                     HistoricMetric historicMetric = world.getBufferedTickLengthMetricSet();
                     int periodIndex = 1;
                     double avg = historicMetric.getAverage(periodIndex);
                     long min = historicMetric.calculateMin(periodIndex);
                     long max = historicMetric.calculateMax(periodIndex);
                     long[] values = historicMetric.getValues(1);
                     Arrays.sort(values);
                     double p25 = MathUtil.percentile(values, 0.25);
                     double p50 = MathUtil.percentile(values, 0.5);
                     double p75 = MathUtil.percentile(values, 0.75);
                     int bots = BOTS.size();
                     double p = MathUtil.percentile(values, percentile);
                     Files.writeString(
                        resultsPath, avg + "," + min + "," + p25 + "," + p50 + "," + p75 + "," + max + "," + p + "," + bots + "\n", StandardOpenOption.APPEND
                     );
                     HytaleLogger.getLogger()
                        .at(Level.INFO)
                        .log(
                           "avg: %s, min: %s, p25: %s, p50: %s, p75: %s, max: %s, p%s: %s, bots: %s",
                           FormatUtil.timeUnitToString(MathUtil.ceil(avg), TimeUnit.NANOSECONDS),
                           FormatUtil.timeUnitToString(min, TimeUnit.NANOSECONDS),
                           FormatUtil.timeUnitToString(MathUtil.ceil(p25), TimeUnit.NANOSECONDS),
                           FormatUtil.timeUnitToString(MathUtil.ceil(p50), TimeUnit.NANOSECONDS),
                           FormatUtil.timeUnitToString(MathUtil.ceil(p75), TimeUnit.NANOSECONDS),
                           FormatUtil.timeUnitToString(max, TimeUnit.NANOSECONDS),
                           percentileDisplay,
                           FormatUtil.timeUnitToString(MathUtil.ceil(p), TimeUnit.NANOSECONDS),
                           bots
                        );
                     if (p > thresholdNanos) {
                        HytaleLogger.getLogger()
                           .at(Level.INFO)
                           .log(
                              "Stopped stress test due to p%s > threshold: %s > %s",
                              percentileDisplay,
                              FormatUtil.timeUnitToString(MathUtil.ceil(p), TimeUnit.NANOSECONDS),
                              FormatUtil.timeUnitToString(thresholdNanos, TimeUnit.NANOSECONDS)
                           );
                        if (STATE.compareAndSet(StressTestStartCommand.StressTestState.RUNNING, StressTestStartCommand.StressTestState.STOPPING)) {
                           stop();
                        }

                        if (shutdown) {
                           HytaleServer.get().shutdownServer(ShutdownReason.SHUTDOWN.withMessage("Stress test finished!"));
                        }
                     } else {
                        BOTS.add(new Bot("bot-" + counter.getAndIncrement(), config, tickStepNanos));
                     }
                  }
               )
            ),
            intervalMillis,
            intervalMillis,
            TimeUnit.MILLISECONDS
         );
      HytaleLogger.getLogger()
         .at(Level.INFO)
         .log(
            "Started stress test! Bot add interval %s with threshold %s for p%s" + (shutdown ? " and will shutdown when finished" : ""),
            FormatUtil.timeUnitToString(intervalMillis, TimeUnit.MILLISECONDS),
            FormatUtil.timeUnitToString(thresholdNanos, TimeUnit.NANOSECONDS),
            percentileDisplay
         );
   }

   static void stop() {
      if (DUMP_TYPE == StressTestStartCommand.DumpType.INTERVAL || DUMP_TYPE == StressTestStartCommand.DumpType.FINISH) {
         try {
            Path path = MetricsRegistry.createDumpPath(DATE_PATH, ".dump.json");
            HytaleServer.METRICS_REGISTRY.dumpToJson(path, HytaleServer.get());
         } catch (IOException var1) {
            throw SneakyThrow.sneakyThrow(var1);
         }
      }

      if (STRESS_TEST_BOT_TASK != null) {
         STRESS_TEST_BOT_TASK.cancel(false);
         STRESS_TEST_BOT_TASK = null;
      }

      if (STRESS_TEST_DUMP_TASK != null) {
         STRESS_TEST_DUMP_TASK.cancel(false);
         STRESS_TEST_DUMP_TASK = null;
      }

      BOTS.removeIf(bot -> {
         bot.shutdown();
         return true;
      });
      if (EVENT_REGISTRATION != null) {
         EVENT_REGISTRATION.unregister();
         EVENT_REGISTRATION = null;
      }

      DATE_PATH = null;
      DUMP_TYPE = null;
      STATE.compareAndSet(StressTestStartCommand.StressTestState.STOPPING, StressTestStartCommand.StressTestState.NOT_RUNNING);
      HytaleLogger.getLogger().at(Level.INFO).log("Stopped stress test!");
   }

   static enum DumpType {
      NEW_BOT,
      INTERVAL,
      FINISH,
      NEVER;
   }

   static enum StressTestState {
      NOT_RUNNING,
      RUNNING,
      STOPPING;
   }
}
