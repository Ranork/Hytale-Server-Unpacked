package com.hypixel.hytale.server.core.command.commands.debug;

import com.hypixel.hytale.logger.backend.HytaleLoggerBackend;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.ParseResult;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.SingleArgumentType;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

public class LogCommand extends CommandBase {
   @Nonnull
   private static final Level[] STANDARD_LEVELS = new Level[]{
      Level.OFF, Level.SEVERE, Level.WARNING, Level.INFO, Level.CONFIG, Level.FINE, Level.FINER, Level.FINEST, Level.ALL
   };
   @Nonnull
   private static final String LEVELS_STRING = Arrays.stream(STANDARD_LEVELS).map(Level::getName).collect(Collectors.joining(", "));
   @Nonnull
   private static final SingleArgumentType<Level> LOG_LEVEL = new SingleArgumentType<Level>(
      "server.commands.parsing.argtype.logLevel.name",
      Message.translation("server.commands.parsing.argtype.logLevel.usage").param("levels", LEVELS_STRING),
      Arrays.stream(STANDARD_LEVELS).map(Level::getName).toArray(String[]::new)
   ) {
      @Nonnull
      public Level parse(@Nonnull String input, @Nonnull ParseResult parseResult) {
         try {
            return Level.parse(input.toUpperCase());
         } catch (IllegalArgumentException var4) {
            parseResult.fail(Message.translation("server.commands.log.invalidLevel").param("input", input).param("level", Level.INFO.getName()));
            return Level.INFO;
         }
      }
   };
   @Nonnull
   private final RequiredArg<String> loggerArg = this.withRequiredArg("logger", "server.commands.log.logger.desc", ArgTypes.STRING);
   @Nonnull
   private final OptionalArg<Level> levelArg = this.withOptionalArg("level", "server.commands.log.level.desc", LOG_LEVEL);
   @Nonnull
   private final FlagArg saveFlag = this.withFlagArg("save", "server.commands.log.save.desc");
   @Nonnull
   private final FlagArg resetFlag = this.withFlagArg("reset", "server.commands.log.reset.desc");

   public LogCommand() {
      super("log", "server.commands.log.desc");
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      String loggerName = this.loggerArg.get(context);
      HytaleLoggerBackend logger;
      if (loggerName.equalsIgnoreCase("global")) {
         loggerName = "global";
         logger = HytaleLoggerBackend.getLogger();
      } else {
         logger = HytaleLoggerBackend.getLogger(loggerName);
      }

      if (this.levelArg.provided(context)) {
         Level level = this.levelArg.get(context);
         logger.setLevel(level);
         boolean saved = false;
         if (this.saveFlag.get(context)) {
            Map<String, Level> logLevels = new Object2ObjectOpenHashMap(HytaleServer.get().getConfig().getLogLevels());
            logLevels.put(logger.getLoggerName(), level);
            HytaleServer.get().getConfig().setLogLevels(logLevels);
            saved = true;
         }

         context.sendMessage(
            Message.translation("server.commands.log.setLogger")
               .param("name", loggerName)
               .param("level", level.getName())
               .param("saved", saved ? " and saved to config!!" : "")
         );
      } else {
         if (this.resetFlag.get(context)) {
            Map<String, Level> logLevels = new Object2ObjectOpenHashMap(HytaleServer.get().getConfig().getLogLevels());
            logLevels.remove(logger.getLoggerName());
            HytaleServer.get().getConfig().setLogLevels(logLevels);
            context.sendMessage(Message.translation("server.commands.log.removedLogger").param("name", loggerName));
         }

         context.sendMessage(Message.translation("server.commands.log.setLoggerNoSave").param("name", loggerName).param("level", logger.getLevel().getName()));
      }
   }
}
