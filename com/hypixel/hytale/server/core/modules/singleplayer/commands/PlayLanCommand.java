package com.hypixel.hytale.server.core.modules.singleplayer.commands;

import com.hypixel.hytale.protocol.packets.serveraccess.Access;
import com.hypixel.hytale.server.core.modules.singleplayer.SingleplayerModule;
import javax.annotation.Nonnull;

public class PlayLanCommand extends PlayCommandBase {
   public PlayLanCommand(@Nonnull SingleplayerModule singleplayerModule) {
      super("lan", "server.commands.play.lan.desc", singleplayerModule, Access.LAN);
   }
}
