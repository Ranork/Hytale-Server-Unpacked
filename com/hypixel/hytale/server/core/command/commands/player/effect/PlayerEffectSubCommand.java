package com.hypixel.hytale.server.core.command.commands.player.effect;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class PlayerEffectSubCommand extends AbstractCommandCollection {
   public PlayerEffectSubCommand() {
      super("effect", "server.commands.player.effect.desc");
      this.setPermissionGroups("hytale:Builder");
      this.addSubCommand(new PlayerEffectApplyCommand());
      this.addSubCommand(new PlayerEffectClearCommand());
   }
}
