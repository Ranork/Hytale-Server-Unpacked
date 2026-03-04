package com.hypixel.hytale.server.core.command.commands.server.auth;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.auth.ServerAuthManager;
import com.hypixel.hytale.server.core.auth.SessionServiceClient;
import com.hypixel.hytale.server.core.auth.oauth.OAuthDeviceFlow;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import java.awt.Color;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class AuthLoginDeviceCommand extends CommandBase {
   @Nonnull
   private static final Message MESSAGE_SINGLEPLAYER = Message.translation("server.commands.auth.login.singleplayer").color(Color.RED);
   @Nonnull
   private static final Message MESSAGE_ALREADY_AUTHENTICATED = Message.translation("server.commands.auth.login.alreadyAuthenticated").color(Color.YELLOW);
   @Nonnull
   private static final Message MESSAGE_STARTING = Message.translation("server.commands.auth.login.device.starting").color(Color.YELLOW);
   @Nonnull
   private static final Message MESSAGE_SUCCESS = Message.translation("server.commands.auth.login.device.success").color(Color.GREEN);
   @Nonnull
   private static final Message MESSAGE_FAILED = Message.translation("server.commands.auth.login.device.failed").color(Color.RED);
   @Nonnull
   private static final Message MESSAGE_PENDING = Message.translation("server.commands.auth.login.pending").color(Color.YELLOW);

   public AuthLoginDeviceCommand() {
      super("device", "server.commands.auth.login.device.desc");
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      ServerAuthManager authManager = ServerAuthManager.getInstance();
      if (authManager.isSingleplayer()) {
         context.sendMessage(MESSAGE_SINGLEPLAYER);
      } else if (authManager.hasSessionToken() && authManager.hasIdentityToken()) {
         context.sendMessage(MESSAGE_ALREADY_AUTHENTICATED);
      } else {
         context.sendMessage(MESSAGE_STARTING);
         authManager.startFlowAsync(new AuthLoginDeviceCommand.AuthFlow()).thenAccept(result -> {
            switch (result) {
               case SUCCESS:
                  context.sendMessage(MESSAGE_SUCCESS);
                  AuthLoginBrowserCommand.sendPersistenceFeedback(context);
                  break;
               case PENDING_PROFILE_SELECTION:
                  context.sendMessage(MESSAGE_PENDING);
                  SessionServiceClient.GameProfile[] profiles = authManager.getPendingProfiles();
                  if (profiles != null) {
                     AuthSelectCommand.sendProfileList(context, profiles);
                  }
                  break;
               case FAILED:
                  context.sendMessage(MESSAGE_FAILED);
            }
         });
      }
   }

   private static class AuthFlow extends OAuthDeviceFlow {
      @Override
      public void onFlowInfo(String userCode, String verificationUri, String verificationUriComplete, int expiresIn) {
         AbstractCommand.LOGGER.at(Level.INFO).log("===================================================================");
         AbstractCommand.LOGGER.at(Level.INFO).log("DEVICE AUTHORIZATION");
         AbstractCommand.LOGGER.at(Level.INFO).log("===================================================================");
         AbstractCommand.LOGGER.at(Level.INFO).log("Visit: %s", verificationUri);
         AbstractCommand.LOGGER.at(Level.INFO).log("Enter code: %s", userCode);
         if (verificationUriComplete != null) {
            AbstractCommand.LOGGER.at(Level.INFO).log("Or visit: %s", verificationUriComplete);
         }

         AbstractCommand.LOGGER.at(Level.INFO).log("===================================================================");
         AbstractCommand.LOGGER.at(Level.INFO).log("Waiting for authorization (expires in %d seconds)...", expiresIn);
      }
   }
}
