package com.hypixel.hytale.server.core.update.command;

import com.hypixel.hytale.common.util.java.ManifestUtil;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.auth.ServerAuthManager;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.update.UpdateModule;
import com.hypixel.hytale.server.core.update.UpdateService;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public class UpdateCheckCommand extends AbstractAsyncCommand {
   private static final Message MSG_CHECKING = Message.translation("server.commands.update.checking");
   private static final Message MSG_NOT_AUTHENTICATED = Message.translation("server.commands.update.not_authenticated");
   private static final Message MSG_CHECK_FAILED = Message.translation("server.commands.update.check_failed");

   public UpdateCheckCommand() {
      super("check", "server.commands.update.check.desc");
   }

   @Nonnull
   @Override
   protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
      ServerAuthManager authManager = ServerAuthManager.getInstance();
      if (!authManager.hasSessionToken()) {
         context.sendMessage(MSG_NOT_AUTHENTICATED);
         return CompletableFuture.completedFuture(null);
      } else {
         context.sendMessage(MSG_CHECKING);
         UpdateService updateService = new UpdateService();
         return updateService.checkForUpdate(UpdateService.getEffectivePatchline())
            .thenAccept(
               manifest -> {
                  if (manifest == null) {
                     context.sendMessage(MSG_CHECK_FAILED);
                  } else {
                     UpdateModule updateModule = UpdateModule.get();
                     if (updateModule != null) {
                        updateModule.setLatestKnownVersion(manifest);
                     }

                     String currentVersion = ManifestUtil.getImplementationVersion();
                     if (currentVersion != null && currentVersion.equals(manifest.version)) {
                        context.sendMessage(Message.translation("server.commands.update.already_latest").param("version", currentVersion));
                     } else {
                        context.sendMessage(
                           Message.translation("server.commands.update.update_available")
                              .param("current", currentVersion != null ? currentVersion : "unknown")
                              .param("latest", manifest.version)
                        );
                     }
                  }
               }
            );
      }
   }
}
