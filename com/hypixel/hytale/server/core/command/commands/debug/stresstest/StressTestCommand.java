package com.hypixel.hytale.server.core.command.commands.debug.stresstest;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class StressTestCommand extends AbstractCommandCollection {
   public StressTestCommand() {
      super("stresstest", "server.commands.stresstest.desc");
      this.addSubCommand(new StressTestStartCommand());
      this.addSubCommand(new StressTestStopCommand());
   }
}
