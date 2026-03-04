package com.hypixel.hytale.server.core.modules.accesscontrol.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.auth.ProfileServiceClient;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.modules.accesscontrol.provider.HytaleWhitelistProvider;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public class WhitelistAddCommand extends AbstractAsyncCommand {
   @Nonnull
   private final HytaleWhitelistProvider whitelistProvider;
   @Nonnull
   private final RequiredArg<ProfileServiceClient.PublicGameProfile> playerArg = this.withRequiredArg(
      "player", "server.commands.whitelist.add.player.desc", ArgTypes.GAME_PROFILE_LOOKUP
   );

   public WhitelistAddCommand(@Nonnull HytaleWhitelistProvider whitelistProvider) {
      super("add", "server.commands.whitelist.add.desc");
      this.whitelistProvider = whitelistProvider;
   }

   @Nonnull
   @Override
   protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
      ProfileServiceClient.PublicGameProfile profile = this.playerArg.get(context);
      if (profile == null) {
         return CompletableFuture.completedFuture(null);
      } else {
         UUID uuid = profile.getUuid();
         Message displayMessage = Message.raw(profile.getUsername()).bold(true);
         if (this.whitelistProvider.modify(list -> list.add(uuid))) {
            context.sendMessage(Message.translation("server.modules.whitelist.addSuccess").param("name", displayMessage));
         } else {
            context.sendMessage(Message.translation("server.modules.whitelist.alreadyWhitelisted").param("name", displayMessage));
         }

         return CompletableFuture.completedFuture(null);
      }
   }
}
