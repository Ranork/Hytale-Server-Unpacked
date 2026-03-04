package com.hypixel.hytale.server.core.command.commands.player.inventory;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class InventoryCommand extends AbstractCommandCollection {
   public InventoryCommand() {
      super("inventory", "server.commands.inventory.desc");
      this.addAliases("inv");
      this.addSubCommand(new InventoryClearCommand());
      this.addSubCommand(new InventorySeeCommand());
      this.addSubCommand(new InventoryItemCommand());
      this.addSubCommand(new InventoryBackpackCommand());
   }
}
