package com.hypixel.hytale.server.core.command.commands.debug.component.hitboxcollision;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class HitboxCollisionCommand extends AbstractCommandCollection {
   public HitboxCollisionCommand() {
      super("hitboxcollision", "server.commands.hitboxcollision.desc");
      this.addSubCommand(new HitboxCollisionAddCommand());
      this.addSubCommand(new HitboxCollisionRemoveCommand());
   }
}
