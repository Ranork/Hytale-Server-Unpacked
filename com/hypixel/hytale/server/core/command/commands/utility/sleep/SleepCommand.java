package com.hypixel.hytale.server.core.command.commands.utility.sleep;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class SleepCommand extends AbstractCommandCollection {
   public SleepCommand() {
      super("sleep", "server.commands.sleep.desc");
      this.addSubCommand(new SleepOffsetCommand());
      this.addSubCommand(new SleepTestCommand());
   }
}
