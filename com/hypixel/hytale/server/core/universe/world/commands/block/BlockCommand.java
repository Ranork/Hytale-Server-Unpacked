package com.hypixel.hytale.server.core.universe.world.commands.block;

import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.universe.world.commands.block.bulk.BlockBulkCommand;

public class BlockCommand extends AbstractCommandCollection {
   public BlockCommand() {
      super("block", "server.commands.block.desc");
      this.setPermissionGroup(GameMode.Creative);
      this.addAliases("blocks");
      this.addSubCommand(new BlockSetCommand());
      this.addSubCommand(new BlockSetTickingCommand());
      this.addSubCommand(new BlockGetCommand());
      this.addSubCommand(new BlockGetStateCommand());
      this.addSubCommand(new BlockRowCommand());
      this.addSubCommand(new BlockBulkCommand());
      this.addSubCommand(new BlockInspectPhysicsCommand());
      this.addSubCommand(new BlockInspectFillerCommand());
      this.addSubCommand(new BlockInspectRotationCommand());
      this.addSubCommand(new BlockSetStateCommand());
   }
}
