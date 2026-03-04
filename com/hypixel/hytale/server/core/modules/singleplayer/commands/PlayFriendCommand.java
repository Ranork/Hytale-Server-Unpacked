package com.hypixel.hytale.server.core.modules.singleplayer.commands;

import com.hypixel.hytale.protocol.packets.serveraccess.Access;
import com.hypixel.hytale.server.core.modules.singleplayer.SingleplayerModule;
import javax.annotation.Nonnull;

public class PlayFriendCommand extends PlayCommandBase {
   public PlayFriendCommand(@Nonnull SingleplayerModule singleplayerModule) {
      super("friend", "server.commands.play.friend.desc", singleplayerModule, Access.Friend);
   }
}
