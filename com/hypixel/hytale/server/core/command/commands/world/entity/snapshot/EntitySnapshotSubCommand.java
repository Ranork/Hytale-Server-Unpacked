package com.hypixel.hytale.server.core.command.commands.world.entity.snapshot;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class EntitySnapshotSubCommand extends AbstractCommandCollection {
   public EntitySnapshotSubCommand() {
      super("snapshot", "server.commands.entity.snapshot.desc");
      this.addAliases("snap");
      this.addSubCommand(new EntitySnapshotLengthCommand());
      this.addSubCommand(new EntitySnapshotHistoryCommand());
   }
}
