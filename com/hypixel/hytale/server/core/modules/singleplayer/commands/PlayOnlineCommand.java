package com.hypixel.hytale.server.core.modules.singleplayer.commands;

import com.hypixel.hytale.protocol.packets.serveraccess.Access;
import com.hypixel.hytale.server.core.modules.singleplayer.SingleplayerModule;
import javax.annotation.Nonnull;

public class PlayOnlineCommand extends PlayCommandBase {
   public PlayOnlineCommand(@Nonnull SingleplayerModule singleplayerModule) {
      super("online", "server.commands.play.online.desc", singleplayerModule, Access.Open);
   }
}
