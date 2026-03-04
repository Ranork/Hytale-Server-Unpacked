package com.hypixel.hytale.server.core.console;

import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.logger.backend.HytaleConsole;
import com.hypixel.hytale.server.core.Constants;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.ShutdownReason;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.console.command.SayCommand;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import java.io.IOError;
import java.io.IOException;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

public class ConsoleModule extends JavaPlugin {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   public static final PluginManifest MANIFEST = PluginManifest.corePlugin(ConsoleModule.class).build();
   private static ConsoleModule instance;
   private static Terminal terminal;
   private ConsoleModule.ConsoleRunnable consoleRunnable;

   public static ConsoleModule get() {
      return instance;
   }

   public ConsoleModule(@Nonnull JavaPluginInit init) {
      super(init);
   }

   public static void initializeTerminal() {
      try {
         TerminalBuilder builder = TerminalBuilder.builder();
         if (Constants.SINGLEPLAYER) {
            builder.dumb(true);
         } else {
            builder.color(true);
         }

         terminal = builder.build();
         HytaleConsole.INSTANCE.setTerminal(terminal.getType());
      } catch (IOException var1) {
         ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(var1)).log("Failed to start console reader");
      }
   }

   @Override
   protected void setup() {
      instance = this;
      this.getCommandRegistry().registerCommand(new SayCommand());
      LineReader lineReader = LineReaderBuilder.builder().terminal(terminal).build();
      this.consoleRunnable = new ConsoleModule.ConsoleRunnable(lineReader, ConsoleSender.INSTANCE);
      this.getLogger().at(Level.INFO).log("Setup console with type: %s", terminal.getType());
   }

   @Override
   protected void shutdown() {
      this.getLogger().at(Level.INFO).log("Restoring terminal...");

      try {
         terminal.close();
      } catch (IOException var2) {
         ((HytaleLogger.Api)HytaleLogger.getLogger().at(Level.SEVERE).withCause(var2)).log("Failed to restore terminal!");
      }

      this.consoleRunnable.interrupt();
   }

   public Terminal getTerminal() {
      return terminal;
   }

   private static class ConsoleRunnable implements Runnable {
      private final LineReader lineReader;
      private final ConsoleSender consoleSender;
      @Nonnull
      private final Thread consoleThread;

      public ConsoleRunnable(LineReader lineReader, ConsoleSender consoleSender) {
         this.lineReader = lineReader;
         this.consoleSender = consoleSender;
         this.consoleThread = new Thread(this, "ConsoleThread");
         this.consoleThread.setDaemon(true);
         this.consoleThread.start();
      }

      @Override
      public void run() {
         try {
            String terminalType = this.lineReader.getTerminal().getType();
            boolean isDumb = "dumb".equals(terminalType) || "dumb-color".equals(terminalType);

            while (!this.consoleThread.isInterrupted()) {
               String command = this.lineReader.readLine(isDumb ? null : "> ");
               if (command == null) {
                  break;
               }

               command = command.trim();
               if (!command.isEmpty() && command.charAt(0) == '/') {
                  command = command.substring(1);
               }

               CommandManager.get().handleCommand(this.consoleSender, command);
            }
         } catch (UserInterruptException var4) {
            HytaleServer.get().shutdownServer(ShutdownReason.SIGINT);
         } catch (IOError | EndOfFileException var5) {
         }
      }

      public void interrupt() {
         this.consoleThread.interrupt();
      }
   }
}
