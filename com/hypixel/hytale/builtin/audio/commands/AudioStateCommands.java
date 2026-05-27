package com.hypixel.hytale.builtin.audio.commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class AudioStateCommands extends AbstractCommandCollection {
   public AudioStateCommands() {
      super("state", "server.commands.audio.state.desc");
      this.addSubCommand(new AudioStateSetCommand());
   }
}
