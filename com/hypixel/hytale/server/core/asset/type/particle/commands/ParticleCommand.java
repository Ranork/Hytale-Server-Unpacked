package com.hypixel.hytale.server.core.asset.type.particle.commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class ParticleCommand extends AbstractCommandCollection {
   public ParticleCommand() {
      super("particle", "server.commands.particle.desc");
      this.addSubCommand(new ParticleSpawnCommand());
   }
}
