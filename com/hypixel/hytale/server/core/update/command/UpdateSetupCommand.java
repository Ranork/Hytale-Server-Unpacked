package com.hypixel.hytale.server.core.update.command;

import com.hypixel.hytale.common.util.java.ManifestUtil;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class UpdateSetupCommand extends CommandBase {
   @Nonnull
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private static final String RESOURCE_START_SH = "update/start.sh";
   private static final String RESOURCE_START_BAT = "update/start.bat";
   private static final String EXPECTED_DIR_NAME = "Server";
   private static final Message MSG_NOT_JAR = Message.translation("server.commands.update.setup.not_jar");
   private static final Message MSG_INVALID_DIRECTORY = Message.translation("server.commands.update.setup.invalid_directory");
   private static final Message MSG_ALREADY_EXIST = Message.translation("server.commands.update.setup.already_exist");
   private static final Message MSG_SETUP_COMPLETE = Message.translation("server.commands.update.setup.complete");
   private static final Message MSG_SETUP_FAILED = Message.translation("server.commands.update.setup.failed");
   private static final Message MSG_NO_ASSETS = Message.translation("server.commands.update.setup.no_assets");
   private final FlagArg forceFlag = this.withFlagArg("force", "server.commands.update.setup.force.desc");

   public UpdateSetupCommand() {
      super("setup", "server.commands.update.setup.desc");
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      if (!ManifestUtil.isJar()) {
         context.sendMessage(MSG_NOT_JAR);
      } else {
         boolean force = this.forceFlag.get(context);
         Path cwd = Path.of(".").toAbsolutePath().normalize();
         if (!"Server".equals(cwd.getFileName().toString()) && !force) {
            context.sendMessage(MSG_INVALID_DIRECTORY);
         } else {
            Path parent = cwd.getParent();
            Path startSh = parent.resolve("start.sh");
            Path startBat = parent.resolve("start.bat");
            boolean exists = Files.exists(startSh) || Files.exists(startBat);
            if (exists && !force) {
               context.sendMessage(MSG_ALREADY_EXIST);
            } else {
               try {
                  this.extractResource("update/start.sh", startSh);
                  this.extractResource("update/start.bat", startBat);
                  if (!System.getProperty("os.name", "").toLowerCase().contains("win")) {
                     startSh.toFile().setExecutable(true, false);
                  }

                  context.sendMessage(MSG_SETUP_COMPLETE);
                  LOGGER.at(Level.INFO).log("Wrapper scripts written to %s", parent);
                  if (!Files.exists(parent.resolve("Assets.zip"))) {
                     context.sendMessage(MSG_NO_ASSETS);
                  }
               } catch (IOException var9) {
                  ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(var9)).log("Failed to extract wrapper scripts");
                  context.sendMessage(MSG_SETUP_FAILED);
               }
            }
         }
      }
   }

   private void extractResource(@Nonnull String resourcePath, @Nonnull Path target) throws IOException {
      try (InputStream in = this.getClass().getClassLoader().getResourceAsStream(resourcePath)) {
         if (in == null) {
            throw new IOException("Resource not found in JAR: " + resourcePath);
         }

         Files.createDirectories(target.getParent());
         Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
      }
   }
}
