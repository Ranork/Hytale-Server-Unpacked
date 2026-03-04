package com.hypixel.hytale.server.core.command.commands.utility.git;

import com.hypixel.hytale.common.util.PathUtil;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.util.AssetUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public class UpdateAssetsCommand extends AbstractCommandCollection {
   public UpdateAssetsCommand() {
      super("assets", "server.commands.git.assets.desc");
      this.addSubCommand(new UpdateAssetsCommand.UpdateAssetsStatusCommand());
      this.addSubCommand(new UpdateAssetsCommand.UpdateAssetsResetCommand());
      this.addSubCommand(new UpdateAssetsCommand.UpdateAssetsPullCommand());
   }

   private abstract static class UpdateAssetsGitCommand extends AbstractAsyncCommand {
      protected UpdateAssetsGitCommand(@Nonnull String name, @Nonnull String description) {
         super(name, description);
      }

      @Nonnull
      protected abstract String[] getCommand(@Nonnull Path var1);

      @Nonnull
      @Override
      protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
         return CompletableFuture.runAsync(() -> {
            Path assetPath = AssetUtil.getHytaleAssetsPath();
            Path gitPath = null;
            if (Files.exists(assetPath.resolve(".git"))) {
               gitPath = assetPath;
            } else {
               Path parent = PathUtil.getParent(assetPath.toAbsolutePath());
               if (Files.exists(parent.resolve(".git"))) {
                  gitPath = parent;
               }
            }

            if (gitPath == null) {
               context.sendMessage(Message.translation("server.general.pathNotGitRepo").param("path", assetPath.toString()));
            } else {
               String[] processCommand = this.getCommand(gitPath);
               String commandDisplay = String.join(" ", processCommand);

               try {
                  context.sendMessage(Message.translation("server.commands.git.running").param("cmd", commandDisplay));
                  Process process = new ProcessBuilder(processCommand).directory(gitPath.toFile()).start();

                  try {
                     process.waitFor();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));

                     String line;
                     while ((line = reader.readLine()) != null) {
                        context.sendMessage(Message.translation("server.commands.git.runningStdOut").param("cmd", commandDisplay).param("line", line));
                     }

                     reader = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8));

                     while ((line = reader.readLine()) != null) {
                        context.sendMessage(Message.translation("server.commands.git.runningStdErr").param("cmd", commandDisplay).param("line", line));
                     }

                     context.sendMessage(Message.translation("server.commands.git.done").param("cmd", commandDisplay));
                  } catch (InterruptedException var9) {
                     Thread.currentThread().interrupt();
                  }
               } catch (IOException var10) {
                  context.sendMessage(Message.translation("server.commands.git.failed").param("cmd", commandDisplay).param("msg", var10.getMessage()));
               }
            }
         });
      }
   }

   private static class UpdateAssetsPullCommand extends UpdateAssetsCommand.UpdateAssetsGitCommand {
      public UpdateAssetsPullCommand() {
         super("pull", "server.commands.git.assets.pull.desc");
      }

      @Nonnull
      @Override
      protected String[] getCommand(@Nonnull Path gitPath) {
         Path script = gitPath.resolve("../../updateAssets.sh");
         if (Files.exists(script)) {
            Path relative = gitPath.relativize(script);
            return new String[]{"sh", relative.toString()};
         } else {
            return new String[]{"git", "pull"};
         }
      }
   }

   private static class UpdateAssetsResetCommand extends UpdateAssetsCommand.UpdateAssetsGitCommand {
      public UpdateAssetsResetCommand() {
         super("reset", "server.commands.git.assets.reset.desc");
      }

      @Nonnull
      @Override
      protected String[] getCommand(@Nonnull Path gitPath) {
         return new String[]{"git", "reset", "--hard", "head"};
      }
   }

   private static class UpdateAssetsStatusCommand extends UpdateAssetsCommand.UpdateAssetsGitCommand {
      public UpdateAssetsStatusCommand() {
         super("status", "server.commands.git.assets.status.desc");
      }

      @Nonnull
      @Override
      protected String[] getCommand(@Nonnull Path gitPath) {
         return new String[]{"git", "status"};
      }
   }
}
