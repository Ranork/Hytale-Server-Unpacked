package com.hypixel.hytale.server.core.discovery.command;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.discovery.DiscoveryModule;

public class DiscoveryCommand extends AbstractCommandCollection {
   public DiscoveryCommand(DiscoveryModule module) {
      super("discovery", "server.commands.discovery.desc");
      this.addSubCommand(new DiscoveryLinkCommand(module));
      this.addSubCommand(new DiscoveryUnlinkCommand(module));
   }
}
