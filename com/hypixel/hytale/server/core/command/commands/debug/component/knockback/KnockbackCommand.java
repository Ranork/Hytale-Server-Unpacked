package com.hypixel.hytale.server.core.command.commands.debug.component.knockback;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class KnockbackCommand extends AbstractCommandCollection {
   public KnockbackCommand() {
      super("knockback", "server.commands.knockback.desc");
      this.setPermissionGroups("hytale:WorldEditor");
      this.addSubCommand(new KnockbackApplyCommand());
   }
}
