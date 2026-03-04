package com.hypixel.hytale.server.core.command.commands.player.camera;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class PlayerCameraDemoSubCommand extends AbstractCommandCollection {
   public PlayerCameraDemoSubCommand() {
      super("demo", "server.commands.camera.demo.desc");
      this.addSubCommand(new PlayerCameraDemoActivateCommand());
      this.addSubCommand(new PlayerCameraDemoDeactivateCommand());
   }
}
