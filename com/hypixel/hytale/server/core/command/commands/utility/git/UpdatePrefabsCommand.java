package com.hypixel.hytale.server.core.command.commands.utility.git;

import com.hypixel.hytale.common.util.PathUtil;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.prefab.PrefabStore;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public class UpdatePrefabsCommand extends AbstractCommandCollection {
   public UpdatePrefabsCommand() {
      super("prefabs", "server.commands.git.prefabs.desc");
      this.addSubCommand(new UpdatePrefabsCommand.UpdatePrefabsStatusCommand());
      this.addSubCommand(new UpdatePrefabsCommand.UpdatePrefabsCommitCommand());
      this.addSubCommand(new UpdatePrefabsCommand.UpdatePrefabsPullCommand());
      this.addSubCommand(new UpdatePrefabsCommand.UpdatePrefabsPushCommand());
      this.addSubCommand(new UpdatePrefabsCommand.UpdatePrefabsAllCommand());
   }

   private static class UpdatePrefabsAllCommand extends UpdatePrefabsCommand.UpdatePrefabsGitCommand {
      public UpdatePrefabsAllCommand() {
         super("all", "server.commands.git.prefabs.all.desc");
      }

      @Nonnull
      @Override
      protected String[][] getCommands(@Nonnull String senderDisplayName) {
         return new String[][]{
            {"git", "submodule", "foreach", "git", "add", "--all", "."},
            {"git", "submodule", "foreach", "git", "commit", "-am", "\"Update prefabs by " + senderDisplayName + "\""},
            {"git", "submodule", "foreach", "git", "pull"},
            {"git", "submodule", "foreach", "git", "push"},
            {"git", "add", "--all", "."},
            {"git", "commit", "-am", "Update prefabs by " + senderDisplayName},
            {"git", "pull"},
            {"git", "push"}
         };
      }
   }

   private static class UpdatePrefabsCommitCommand extends UpdatePrefabsCommand.UpdatePrefabsGitCommand {
      public UpdatePrefabsCommitCommand() {
         super("commit", "server.commands.git.prefabs.commit.desc");
      }

      @Nonnull
      @Override
      protected String[][] getCommands(@Nonnull String senderDisplayName) {
         return new String[][]{
            {"git", "add", "--all", "."},
            {"git", "commit", "-am", "Update prefabs by " + senderDisplayName},
            {"git", "submodule", "foreach", "git", "add", "--all", "."},
            {"git", "submodule", "foreach", "git", "commit", "-am", "\"Update prefabs by " + senderDisplayName + "\""}
         };
      }
   }

   private abstract static class UpdatePrefabsGitCommand extends AbstractAsyncCommand {
      protected UpdatePrefabsGitCommand(@Nonnull String name, @Nonnull String description) {
         super(name, description);
      }

      @Nonnull
      protected abstract String[][] getCommands(@Nonnull String var1);

      @Nonnull
      @Override
      protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
         return CompletableFuture.runAsync(
            () -> {
               Path prefabsPath = PrefabStore.get().getServerPrefabsPath();
               Path gitPath = null;
               if (Files.isDirectory(prefabsPath.resolve(".git"))) {
                  gitPath = prefabsPath;
               } else {
                  Path parent = PathUtil.getParent(prefabsPath);
                  if (Files.isDirectory(parent.resolve(".git"))) {
                     gitPath = parent;
                  }
               }

               if (gitPath == null) {
                  context.sendMessage(Message.translation("server.general.pathNotGitRepo").param("path", prefabsPath.toString()));
               } else {
                  String senderDisplayName = context.sender().getDisplayName().replaceAll("[^a-zA-Z0-9 ._-]", "");
                  if (senderDisplayName.isEmpty()) {
                     senderDisplayName = "Unknown";
                  }

                  String[][] cmds = this.getCommands(senderDisplayName);

                  for (String[] processCommand : cmds) {
                     try {
                        String commandDisplay = String.join(" ", processCommand);
                        context.sendMessage(Message.translation("server.commands.git.runningCmd").param("cmd", commandDisplay));
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
                        } catch (InterruptedException var15) {
                           Thread.currentThread().interrupt();
                           break;
                        }
                     } catch (IOException var16) {
                        context.sendMessage(
                           Message.translation("server.commands.git.failed").param("cmd", String.join(" ", processCommand)).param("msg", var16.getMessage())
                        );
                        break;
                     }
                  }
               }
            }
         );
      }
   }

   private static class UpdatePrefabsPullCommand extends UpdatePrefabsCommand.UpdatePrefabsGitCommand {
      public UpdatePrefabsPullCommand() {
         super("pull", "server.commands.git.prefabs.pull.desc");
      }

      @Nonnull
      @Override
      protected String[][] getCommands(@Nonnull String senderDisplayName) {
         return new String[][]{{"git", "pull"}, {"git", "submodule", "foreach", "git", "pull"}};
      }
   }

   private static class UpdatePrefabsPushCommand extends UpdatePrefabsCommand.UpdatePrefabsGitCommand {
      public UpdatePrefabsPushCommand() {
         super("push", "server.commands.git.prefabs.push.desc");
      }

      @Nonnull
      @Override
      protected String[][] getCommands(@Nonnull String senderDisplayName) {
         return new String[][]{{"git", "push", "origin", "master"}, {"git", "submodule", "foreach", "git", "push"}};
      }
   }

   private static class UpdatePrefabsStatusCommand extends UpdatePrefabsCommand.UpdatePrefabsGitCommand {
      public UpdatePrefabsStatusCommand() {
         super("status", "server.commands.git.prefabs.status.desc");
      }

      @Nonnull
      @Override
      protected String[][] getCommands(@Nonnull String senderDisplayName) {
         return new String[][]{{"git", "status"}, {"git", "submodule", "foreach", "git", "status"}};
      }
   }
}
