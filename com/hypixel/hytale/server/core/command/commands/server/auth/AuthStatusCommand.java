package com.hypixel.hytale.server.core.command.commands.server.auth;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.Options;
import com.hypixel.hytale.server.core.auth.ServerAuthManager;
import com.hypixel.hytale.server.core.auth.SessionServiceClient;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import java.awt.Color;
import java.time.Instant;
import javax.annotation.Nonnull;

public class AuthStatusCommand extends CommandBase {
   @Nonnull
   private static final Message MESSAGE_STATUS_CONNECTION_MODE_AUTHENTICATED = Message.translation("server.commands.auth.status.connectionMode.authenticated")
      .color(Color.GREEN);
   @Nonnull
   private static final Message MESSAGE_STATUS_CONNECTION_MODE_OFFLINE = Message.translation("server.commands.auth.status.connectionMode.offline")
      .color(Color.YELLOW);
   @Nonnull
   private static final Message MESSAGE_STATUS_CONNECTION_MODE_INSECURE = Message.translation("server.commands.auth.status.connectionMode.insecure")
      .color(Color.ORANGE);
   @Nonnull
   private static final Message MESSAGE_STATUS_MODE_NONE = Message.translation("server.commands.auth.status.mode.none").color(Color.RED);
   @Nonnull
   private static final Message MESSAGE_STATUS_MODE_SINGLEPLAYER = Message.translation("server.commands.auth.status.mode.singleplayer").color(Color.GREEN);
   @Nonnull
   private static final Message MESSAGE_STATUS_MODE_EXTERNAL = Message.translation("server.commands.auth.status.mode.external").color(Color.GREEN);
   @Nonnull
   private static final Message MESSAGE_STATUS_MODE_OAUTH_BROWSER = Message.translation("server.commands.auth.status.mode.oauthBrowser").color(Color.GREEN);
   @Nonnull
   private static final Message MESSAGE_STATUS_MODE_OAUTH_DEVICE = Message.translation("server.commands.auth.status.mode.oauthDevice").color(Color.GREEN);
   @Nonnull
   private static final Message MESSAGE_STATUS_MODE_OAUTH_STORE = Message.translation("server.commands.auth.status.mode.oauthStore").color(Color.GREEN);
   @Nonnull
   private static final Message MESSAGE_STATUS_TOKEN_PRESENT = Message.translation("server.commands.auth.status.tokenPresent").color(Color.GREEN);
   @Nonnull
   private static final Message MESSAGE_STATUS_TOKEN_MISSING = Message.translation("server.commands.auth.status.tokenMissing").color(Color.RED);
   @Nonnull
   private static final Message MESSAGE_STATUS_HELP = Message.translation("server.commands.auth.status.help").color(Color.YELLOW);
   @Nonnull
   private static final Message MESSAGE_STATUS_PENDING = Message.translation("server.commands.auth.status.pending").color(Color.YELLOW);

   public AuthStatusCommand() {
      super("status", "server.commands.auth.status.desc");
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      ServerAuthManager authManager = ServerAuthManager.getInstance();

      Message connectionModeMessage = switch ((Options.AuthMode)Options.getOptionSet().valueOf(Options.AUTH_MODE)) {
         case AUTHENTICATED -> MESSAGE_STATUS_CONNECTION_MODE_AUTHENTICATED;
         case OFFLINE -> MESSAGE_STATUS_CONNECTION_MODE_OFFLINE;
         case INSECURE -> MESSAGE_STATUS_CONNECTION_MODE_INSECURE;
      };
      ServerAuthManager.AuthMode mode = authManager.getAuthMode();

      Message modeMessage = switch (mode) {
         case NONE -> MESSAGE_STATUS_MODE_NONE;
         case SINGLEPLAYER -> MESSAGE_STATUS_MODE_SINGLEPLAYER;
         case EXTERNAL_SESSION -> MESSAGE_STATUS_MODE_EXTERNAL;
         case OAUTH_BROWSER -> MESSAGE_STATUS_MODE_OAUTH_BROWSER;
         case OAUTH_DEVICE -> MESSAGE_STATUS_MODE_OAUTH_DEVICE;
         case OAUTH_STORE -> MESSAGE_STATUS_MODE_OAUTH_STORE;
      };
      String profileInfo = "";
      SessionServiceClient.GameProfile profile = authManager.getSelectedProfile();
      if (profile != null) {
         String name = profile.username != null ? profile.username : "Unknown";
         profileInfo = name + " (" + profile.uuid + ")";
      }

      Message sessionTokenStatus = authManager.hasSessionToken() ? MESSAGE_STATUS_TOKEN_PRESENT : MESSAGE_STATUS_TOKEN_MISSING;
      Message identityTokenStatus = authManager.hasIdentityToken() ? MESSAGE_STATUS_TOKEN_PRESENT : MESSAGE_STATUS_TOKEN_MISSING;
      Message expiryStatus = Message.empty();
      Instant expiry = authManager.getTokenExpiry();
      if (expiry != null) {
         long secondsRemaining = expiry.getEpochSecond() - Instant.now().getEpochSecond();
         if (secondsRemaining > 0L) {
            long hours = secondsRemaining / 3600L;
            long minutes = secondsRemaining % 3600L / 60L;
            long seconds = secondsRemaining % 60L;
            expiryStatus = Message.translation("server.commands.auth.status.remaining")
               .param("hours", String.format("%02d", hours))
               .param("minutes", String.format("%02d", minutes))
               .param("seconds", String.format("%02d", seconds));
         } else {
            expiryStatus = Message.translation("server.commands.auth.status.expired");
         }
      }

      Message certificateStatus;
      if (authManager.getServerCertificate() != null) {
         String fingerprint = authManager.getServerCertificateFingerprint();
         certificateStatus = fingerprint != null
            ? Message.raw(fingerprint.substring(0, 16) + "...")
            : Message.translation("server.commands.auth.status.certificate.unknown");
      } else {
         certificateStatus = Message.translation("server.commands.auth.status.certificate.notLoaded");
      }

      context.sendMessage(
         Message.translation("server.commands.auth.status.format")
            .param("connectionMode", connectionModeMessage)
            .param("tokenMode", modeMessage)
            .param("profile", profileInfo)
            .param("sessionToken", sessionTokenStatus)
            .param("identityToken", identityTokenStatus)
            .param("expiry", expiryStatus)
            .param("certificate", certificateStatus)
      );
      if (mode == ServerAuthManager.AuthMode.NONE && !authManager.isSingleplayer()) {
         if (authManager.hasPendingProfiles()) {
            context.sendMessage(MESSAGE_STATUS_PENDING);
            SessionServiceClient.GameProfile[] profiles = authManager.getPendingProfiles();
            if (profiles != null) {
               AuthSelectCommand.sendProfileList(context, profiles);
            }
         } else {
            context.sendMessage(MESSAGE_STATUS_HELP);
         }
      }
   }
}
