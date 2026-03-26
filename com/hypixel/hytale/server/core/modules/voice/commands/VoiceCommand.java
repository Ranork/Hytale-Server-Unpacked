package com.hypixel.hytale.server.core.modules.voice.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.auth.ProfileServiceClient;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.modules.voice.VoiceModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class VoiceCommand extends AbstractCommandCollection {
   public VoiceCommand() {
      super("voice", "server.commands.voice.desc");
      this.addSubCommand(new VoiceCommand.VoiceEnabledCommand());
      this.addSubCommand(new VoiceCommand.VoiceMaxDistanceCommand());
      this.addSubCommand(new VoiceCommand.VoiceFullVolumeDistanceCommand());
      this.addSubCommand(new VoiceCommand.VoiceMuteCommand());
      this.addSubCommand(new VoiceCommand.VoiceUnmuteCommand());
      this.addSubCommand(new VoiceCommand.VoiceMutedListCommand());
      this.addSubCommand(new VoiceCommand.VoiceStatusCommand());
   }

   private class VoiceEnabledCommand extends AbstractCommand {
      @Nonnull
      private final RequiredArg<Boolean> enabledArg = this.withRequiredArg("enabled", "server.commands.voice.enabled.arg.desc", ArgTypes.BOOLEAN);

      public VoiceEnabledCommand() {
         super("enabled", "server.commands.voice.enabled.desc");
         this.setPermissionGroup(null);
      }

      @Nullable
      @Override
      protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
         boolean enabled = this.enabledArg.get(context);
         VoiceModule.get().setVoiceEnabled(enabled);
         String messageId = enabled ? "server.commands.voice.status.enabled" : "server.commands.voice.status.disabled";
         context.sendMessage(Message.translation(messageId));
         return null;
      }
   }

   private class VoiceFullVolumeDistanceCommand extends AbstractCommand {
      @Nonnull
      private final RequiredArg<Float> distanceArg = this.withRequiredArg("blocks", "server.commands.voice.fullvolumedistance.arg.desc", ArgTypes.FLOAT);

      public VoiceFullVolumeDistanceCommand() {
         super("fullvolumedistance", "server.commands.voice.fullvolumedistance.desc");
         this.setPermissionGroup(null);
      }

      @Nullable
      @Override
      protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
         float distance = this.distanceArg.get(context);
         if (distance <= 0.0F) {
            context.sendMessage(Message.translation("server.commands.voice.fullvolumedistance.invalid"));
            return null;
         } else {
            VoiceModule.get().setReferenceDistance(distance);
            context.sendMessage(Message.translation("server.commands.voice.fullvolumedistance.set").param("distance", distance));
            return null;
         }
      }
   }

   private class VoiceMaxDistanceCommand extends AbstractCommand {
      @Nonnull
      private final RequiredArg<Float> distanceArg = this.withRequiredArg("blocks", "server.commands.voice.maxdistance.arg.desc", ArgTypes.FLOAT);

      public VoiceMaxDistanceCommand() {
         super("maxdistance", "server.commands.voice.maxdistance.desc");
         this.setPermissionGroup(null);
      }

      @Nullable
      @Override
      protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
         float distance = this.distanceArg.get(context);
         if (distance <= 0.0F) {
            context.sendMessage(Message.translation("server.commands.voice.maxdistance.invalid"));
            return null;
         } else {
            VoiceModule.get().setMaxHearingDistance(distance);
            context.sendMessage(Message.translation("server.commands.voice.maxdistance.set").param("distance", distance));
            return null;
         }
      }
   }

   private class VoiceMuteCommand extends AbstractAsyncCommand {
      @Nonnull
      private final RequiredArg<ProfileServiceClient.PublicGameProfile> playerArg = this.withRequiredArg(
         "player", "server.commands.voice.mute.arg.desc", ArgTypes.GAME_PROFILE_LOOKUP
      );

      public VoiceMuteCommand() {
         super("mute", "server.commands.voice.mute.desc");
         this.setPermissionGroup(null);
      }

      @Nonnull
      @Override
      protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
         ProfileServiceClient.PublicGameProfile profile = this.playerArg.get(context);
         if (profile == null) {
            return CompletableFuture.completedFuture(null);
         } else {
            UUID uuid = profile.getUuid();
            Message displayName = Message.raw(profile.getUsername()).bold(true);
            if (VoiceModule.get().isPlayerMuted(uuid)) {
               context.sendMessage(Message.translation("server.commands.voice.mute.already").param("player", displayName));
               return CompletableFuture.completedFuture(null);
            } else {
               VoiceModule.get().mutePlayer(uuid);
               context.sendMessage(Message.translation("server.commands.voice.mute.success").param("player", displayName));
               return CompletableFuture.completedFuture(null);
            }
         }
      }
   }

   private class VoiceMutedListCommand extends AbstractCommand {
      public VoiceMutedListCommand() {
         super("muted", "server.commands.voice.muted.desc");
         this.setPermissionGroup(null);
      }

      @Nullable
      @Override
      protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
         Set<UUID> mutedPlayers = VoiceModule.get().getGloballyMutedPlayers();
         if (mutedPlayers.isEmpty()) {
            context.sendMessage(Message.translation("server.commands.voice.muted.empty"));
            return null;
         } else {
            String playerList = mutedPlayers.stream().map(uuid -> {
               PlayerRef playerRef = Universe.get().getPlayer(uuid);
               return playerRef != null ? playerRef.getUsername() + " (" + uuid + ")" : uuid.toString();
            }).collect(Collectors.joining(", "));
            context.sendMessage(Message.translation("server.commands.voice.muted.list").param("count", mutedPlayers.size()).param("players", playerList));
            return null;
         }
      }
   }

   private class VoiceStatusCommand extends AbstractCommand {
      public VoiceStatusCommand() {
         super("status", "server.commands.voice.status.desc");
         this.setPermissionGroup(null);
      }

      @Nullable
      @Override
      protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
         VoiceModule voiceModule = VoiceModule.get();
         int mutedCount = voiceModule.getGloballyMutedPlayers().size();
         String messageId = voiceModule.isVoiceEnabled() ? "server.commands.voice.status.enabledInfo" : "server.commands.voice.status.disabledInfo";
         context.sendMessage(
            Message.translation(messageId)
               .param("maxDistance", voiceModule.getMaxHearingDistance())
               .param("fullVolumeDistance", voiceModule.getReferenceDistance())
               .param("mutedCount", mutedCount)
         );
         return null;
      }
   }

   private class VoiceUnmuteCommand extends AbstractAsyncCommand {
      @Nonnull
      private final RequiredArg<ProfileServiceClient.PublicGameProfile> playerArg = this.withRequiredArg(
         "player", "server.commands.voice.unmute.arg.desc", ArgTypes.GAME_PROFILE_LOOKUP
      );

      public VoiceUnmuteCommand() {
         super("unmute", "server.commands.voice.unmute.desc");
         this.setPermissionGroup(null);
      }

      @Nonnull
      @Override
      protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
         ProfileServiceClient.PublicGameProfile profile = this.playerArg.get(context);
         if (profile == null) {
            return CompletableFuture.completedFuture(null);
         } else {
            UUID uuid = profile.getUuid();
            Message displayName = Message.raw(profile.getUsername()).bold(true);
            if (!VoiceModule.get().isPlayerMuted(uuid)) {
               context.sendMessage(Message.translation("server.commands.voice.unmute.notmuted").param("player", displayName));
               return CompletableFuture.completedFuture(null);
            } else {
               VoiceModule.get().unmutePlayer(uuid);
               context.sendMessage(Message.translation("server.commands.voice.unmute.success").param("player", displayName));
               return CompletableFuture.completedFuture(null);
            }
         }
      }
   }
}
