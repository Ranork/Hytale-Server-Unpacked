package com.hypixel.hytale.server.core.command.commands.server.auth;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.auth.ServerAuthManager;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import java.awt.Color;
import javax.annotation.Nonnull;

public class AuthLogoutCommand extends CommandBase {
   @Nonnull
   private static final Message MESSAGE_SINGLEPLAYER = Message.translation("server.commands.auth.logout.singleplayer").color(Color.RED);
   @Nonnull
   private static final Message MESSAGE_NOT_AUTHENTICATED = Message.translation("server.commands.auth.logout.notAuthenticated").color(Color.YELLOW);

   public AuthLogoutCommand() {
      super("logout", "server.commands.auth.logout.desc");
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      ServerAuthManager authManager = ServerAuthManager.getInstance();
      if (authManager.isSingleplayer()) {
         context.sendMessage(MESSAGE_SINGLEPLAYER);
      } else if (!authManager.hasIdentityToken() && !authManager.hasSessionToken()) {
         context.sendMessage(MESSAGE_NOT_AUTHENTICATED);
      } else {
         ServerAuthManager.AuthMode previousMode = authManager.getAuthMode();
         authManager.logout();
         context.sendMessage(Message.translation("server.commands.auth.logout.success").color(Color.GREEN).param("previousMode", previousMode.name()));
      }
   }
}
