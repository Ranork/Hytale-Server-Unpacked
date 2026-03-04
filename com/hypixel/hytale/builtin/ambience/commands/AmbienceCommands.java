package com.hypixel.hytale.builtin.ambience.commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class AmbienceCommands extends AbstractCommandCollection {
   public AmbienceCommands() {
      super("ambience", "server.commands.ambience.desc");
      this.addAliases("ambiance");
      this.addAliases("ambient");
      this.addSubCommand(new AmbienceCommands.AmbienceEmitterCommands());
      this.addSubCommand(new AmbienceSetMusicCommand());
      this.addSubCommand(new AmbienceClearCommand());
   }

   public static class AmbienceEmitterCommands extends AbstractCommandCollection {
      public AmbienceEmitterCommands() {
         super("emitter", "server.commands.ambience.emitter.desc");
         this.addSubCommand(new AmbienceEmitterAddCommand());
      }
   }
}
