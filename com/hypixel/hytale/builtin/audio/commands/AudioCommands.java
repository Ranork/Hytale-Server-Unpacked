package com.hypixel.hytale.builtin.audio.commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class AudioCommands extends AbstractCommandCollection {
   public AudioCommands() {
      super("audio", "server.commands.audio.desc");
      this.setPermissionGroups("hytale:WorldEditor");
      this.addSubCommand(new AudioMusicCommands());
      this.addSubCommand(new AudioStateCommands());
   }
}
