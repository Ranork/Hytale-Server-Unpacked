package com.hypixel.hytale.builtin.audio.commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class AudioMusicCommands extends AbstractCommandCollection {
   public AudioMusicCommands() {
      super("music", "server.commands.audio.music.desc");
      this.addSubCommand(new AudioMusicForceCommand());
      this.addSubCommand(new AudioMusicClearCommand());
   }
}
