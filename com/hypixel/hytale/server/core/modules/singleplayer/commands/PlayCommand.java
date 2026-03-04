package com.hypixel.hytale.server.core.modules.singleplayer.commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.modules.singleplayer.SingleplayerModule;
import javax.annotation.Nonnull;

public class PlayCommand extends AbstractCommandCollection {
   public PlayCommand(@Nonnull SingleplayerModule singleplayerModule) {
      super("play", "server.commands.play.desc");
      this.addSubCommand(new PlayLanCommand(singleplayerModule));
      this.addSubCommand(new PlayFriendCommand(singleplayerModule));
      this.addSubCommand(new PlayOnlineCommand(singleplayerModule));
   }
}
