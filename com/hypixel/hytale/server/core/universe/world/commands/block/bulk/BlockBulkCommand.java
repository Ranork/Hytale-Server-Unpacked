package com.hypixel.hytale.server.core.universe.world.commands.block.bulk;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class BlockBulkCommand extends AbstractCommandCollection {
   public BlockBulkCommand() {
      super("bulk", "server.commands.block.bulk.desc");
      this.setPermissionGroup(null);
      this.addSubCommand(new BlockBulkFindCommand());
      this.addSubCommand(new BlockBulkFindHereCommand());
      this.addSubCommand(new BlockBulkReplaceCommand());
   }
}
