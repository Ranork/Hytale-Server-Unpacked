package com.hypixel.hytale.builtin.triggervolumes.command;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class TriggerVolumeCommand extends AbstractCommandCollection {
   public TriggerVolumeCommand() {
      super("triggervolume", "server.commands.triggervolume.desc");
      this.setPermissionGroups("hytale:WorldEditor");
      this.addAliases("tv");
      this.addSubCommand(new TriggerVolumeCreateCommand());
      this.addSubCommand(new TriggerVolumeRemoveCommand());
      this.addSubCommand(new TriggerVolumeListCommand());
      this.addSubCommand(new TriggerVolumeInfoCommand());
      this.addSubCommand(new TriggerVolumeEnableCommand());
      this.addSubCommand(new TriggerVolumeDisableCommand());
      this.addSubCommand(new TriggerVolumeViewCommand());
      this.addSubCommand(new TriggerVolumeAssignEffectCommand());
      this.addSubCommand(new TriggerVolumeUnassignEffectCommand());
      this.addSubCommand(new TriggerVolumeAssignGroupEffectCommand());
      this.addSubCommand(new TriggerVolumeEnableTagCommand());
      this.addSubCommand(new TriggerVolumeDisableTagCommand());
      this.addSubCommand(new TriggerVolumeListTagCommand());
      this.addSubCommand(new TriggerVolumeTestCommand());
      this.addSubCommand(new TriggerVolumeEffectsCommand());
      this.addSubCommand(new TriggerVolumeBrowseCommand());
   }
}
