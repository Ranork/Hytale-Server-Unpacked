package com.hypixel.hytale.server.core.command.commands.player;

import com.hypixel.hytale.server.core.command.commands.player.camera.PlayerCameraSubCommand;
import com.hypixel.hytale.server.core.command.commands.player.effect.PlayerEffectSubCommand;
import com.hypixel.hytale.server.core.command.commands.player.stats.PlayerStatsSubCommand;
import com.hypixel.hytale.server.core.command.commands.player.viewradius.PlayerViewRadiusSubCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class PlayerCommand extends AbstractCommandCollection {
   public PlayerCommand() {
      super("player", "server.commands.player.desc");
      this.addSubCommand(new PlayerResetCommand());
      this.addSubCommand(new PlayerStatsSubCommand());
      this.addSubCommand(new PlayerEffectSubCommand());
      this.addSubCommand(new PlayerRespawnCommand());
      this.addSubCommand(new PlayerCameraSubCommand());
      this.addSubCommand(new PlayerViewRadiusSubCommand());
      this.addSubCommand(new PlayerZoneCommand());
   }
}
