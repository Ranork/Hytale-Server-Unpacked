package com.hypixel.hytale.server.core.command.commands.world.entity;

import com.hypixel.hytale.server.core.command.commands.world.entity.snapshot.EntitySnapshotSubCommand;
import com.hypixel.hytale.server.core.command.commands.world.entity.stats.EntityStatsSubCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class EntityCommand extends AbstractCommandCollection {
   public EntityCommand() {
      super("entity", "server.commands.entity.desc");
      this.addAliases("entities");
      this.addSubCommand(new EntityCloneCommand());
      this.addSubCommand(new EntityRemoveCommand());
      this.addSubCommand(new EntityDumpCommand());
      this.addSubCommand(new EntityCleanCommand());
      this.addSubCommand(new EntityLodCommand());
      this.addSubCommand(new EntityTrackerCommand());
      this.addSubCommand(new EntityResendCommand());
      this.addSubCommand(new EntityNameplateCommand());
      this.addSubCommand(new EntityStatsSubCommand());
      this.addSubCommand(new EntitySnapshotSubCommand());
      this.addSubCommand(new EntityEffectCommand());
      this.addSubCommand(new EntityMakeInteractableCommand());
      this.addSubCommand(new EntityIntangibleCommand());
      this.addSubCommand(new EntityInvulnerableCommand());
      this.addSubCommand(new EntityHideFromAdventurePlayersCommand());
      this.addSubCommand(new EntityCountCommand());
   }
}
