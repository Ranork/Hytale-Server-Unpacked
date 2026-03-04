package com.hypixel.hytale.server.core.command.commands.server.auth;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.auth.AuthCredentialStoreProvider;
import com.hypixel.hytale.server.core.auth.MemoryAuthCredentialStoreProvider;
import com.hypixel.hytale.server.core.auth.ServerAuthManager;
import com.hypixel.hytale.server.core.auth.SessionServiceClient;
import com.hypixel.hytale.server.core.auth.oauth.OAuthBrowserFlow;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.net.URI;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class AuthLoginBrowserCommand extends CommandBase {
   @Nonnull
   private static final Message MESSAGE_SINGLEPLAYER = Message.translation("server.commands.auth.login.singleplayer").color(Color.RED);
   @Nonnull
   private static final Message MESSAGE_ALREADY_AUTHENTICATED = Message.translation("server.commands.auth.login.alreadyAuthenticated").color(Color.YELLOW);
   @Nonnull
   private static final Message MESSAGE_STARTING = Message.translation("server.commands.auth.login.browser.starting").color(Color.YELLOW);
   @Nonnull
   private static final Message MESSAGE_SUCCESS = Message.translation("server.commands.auth.login.browser.success").color(Color.GREEN);
   @Nonnull
   private static final Message MESSAGE_FAILED = Message.translation("server.commands.auth.login.browser.failed").color(Color.RED);
   @Nonnull
   private static final Message MESSAGE_PENDING = Message.translation("server.commands.auth.login.pending").color(Color.YELLOW);

   public AuthLoginBrowserCommand() {
      super("browser", "server.commands.auth.login.browser.desc");
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
         authManager.startFlowAsync(new AuthLoginBrowserCommand.AuthFlow()).thenAccept(result -> {
            switch (result) {
               case SUCCESS:
                  context.sendMessage(MESSAGE_SUCCESS);
                  sendPersistenceFeedback(context);
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

   static void sendPersistenceFeedback(@Nonnull CommandContext context) {
      AuthCredentialStoreProvider provider = HytaleServer.get().getConfig().getAuthCredentialStoreProvider();
      if (provider instanceof MemoryAuthCredentialStoreProvider) {
         String availableTypes = String.join(", ", AuthCredentialStoreProvider.CODEC.getRegisteredIds());
         context.sendMessage(Message.translation("server.commands.auth.login.persistence.memory").color(Color.ORANGE).param("types", availableTypes));
      } else {
         String typeName = AuthCredentialStoreProvider.CODEC.getIdFor((Class<? extends AuthCredentialStoreProvider>)provider.getClass());
         context.sendMessage(Message.translation("server.commands.auth.login.persistence.saved").color(Color.GREEN).param("type", typeName));
      }
   }

   private static class AuthFlow extends OAuthBrowserFlow {
      @Override
      public void onFlowInfo(String authUrl) {
         AbstractCommand.LOGGER.at(Level.INFO).log("Starting OAuth browser flow...");
         AbstractCommand.LOGGER.at(Level.INFO).log("===================================================================");
         AbstractCommand.LOGGER.at(Level.INFO).log("Please open this URL in your browser to authenticate:");
         AbstractCommand.LOGGER.at(Level.INFO).log("%s", authUrl);
         AbstractCommand.LOGGER.at(Level.INFO).log("===================================================================");
         if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Action.BROWSE)) {
            try {
               Desktop.getDesktop().browse(new URI(authUrl));
               AbstractCommand.LOGGER.at(Level.INFO).log("Browser opened automatically.");
            } catch (Exception var3) {
               AbstractCommand.LOGGER.at(Level.INFO).log("Could not open browser automatically. Please open the URL manually.");
            }
         }
      }
   }
}
