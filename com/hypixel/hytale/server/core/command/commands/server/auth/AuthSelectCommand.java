package com.hypixel.hytale.server.core.command.commands.server.auth;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.auth.ServerAuthManager;
import com.hypixel.hytale.server.core.auth.SessionServiceClient;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import java.awt.Color;
import javax.annotation.Nonnull;

public class AuthSelectCommand extends CommandBase {
   @Nonnull
   private static final Message MESSAGE_NO_PENDING = Message.translation("server.commands.auth.select.noPending").color(Color.YELLOW);
   @Nonnull
   private static final Message MESSAGE_SUCCESS = Message.translation("server.commands.auth.select.success").color(Color.GREEN);
   @Nonnull
   private static final Message MESSAGE_FAILED = Message.translation("server.commands.auth.select.failed").color(Color.RED);
   @Nonnull
   private static final Message MESSAGE_AVAILABLE_PROFILES = Message.translation("server.commands.auth.select.availableProfiles").color(Color.YELLOW);
   @Nonnull
   private static final Message MESSAGE_USAGE = Message.translation("server.commands.auth.select.usage").color(Color.GRAY);

   public AuthSelectCommand() {
      super("select", "server.commands.auth.select.desc");
      this.addUsageVariant(new AuthSelectCommand.SelectProfileVariant());
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      ServerAuthManager authManager = ServerAuthManager.getInstance();
      if (!authManager.hasPendingProfiles()) {
         context.sendMessage(MESSAGE_NO_PENDING);
      } else {
         SessionServiceClient.GameProfile[] profiles = authManager.getPendingProfiles();
         if (profiles != null) {
            context.sendMessage(MESSAGE_AVAILABLE_PROFILES);
            sendProfileList(context, profiles);
            context.sendMessage(MESSAGE_USAGE);
         }
      }
   }

   static void sendProfileList(@Nonnull CommandContext context, @Nonnull SessionServiceClient.GameProfile[] profiles) {
      for (int i = 0; i < profiles.length; i++) {
         context.sendMessage(
            Message.translation("server.commands.auth.select.profileItem")
               .param("index", i + 1)
               .param("username", profiles[i].username)
               .param("uuid", profiles[i].uuid.toString())
         );
      }
   }

   private static class SelectProfileVariant extends CommandBase {
      @Nonnull
      private final RequiredArg<String> profileArg = this.withRequiredArg("profile", "server.commands.auth.select.profile.desc", ArgTypes.STRING);

      SelectProfileVariant() {
         super("server.commands.auth.select.variant.desc");
      }

      @Override
      protected void executeSync(@Nonnull CommandContext context) {
         ServerAuthManager authManager = ServerAuthManager.getInstance();
         if (!authManager.hasPendingProfiles()) {
            context.sendMessage(AuthSelectCommand.MESSAGE_NO_PENDING);
         } else {
            String selection = this.profileArg.get(context);

            boolean success;
            try {
               int index = Integer.parseInt(selection);
               success = authManager.selectPendingProfile(index);
            } catch (NumberFormatException var6) {
               success = authManager.selectPendingProfileByUsername(selection);
            }

            if (success) {
               context.sendMessage(AuthSelectCommand.MESSAGE_SUCCESS);
            } else {
               context.sendMessage(AuthSelectCommand.MESSAGE_FAILED);
            }
         }
      }
   }
}
