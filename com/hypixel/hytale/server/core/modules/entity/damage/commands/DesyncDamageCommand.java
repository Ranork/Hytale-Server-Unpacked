package com.hypixel.hytale.server.core.modules.entity.damage.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import javax.annotation.Nonnull;

public class DesyncDamageCommand extends CommandBase {
   public DesyncDamageCommand() {
      super("desyncdamage", "server.commands.damage.desyncdamage.desc");
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      DamageSystems.FilterUnkillable.CAUSE_DESYNC = !DamageSystems.FilterUnkillable.CAUSE_DESYNC;
      context.sendMessage(Message.translation("server.commands.damage.desyncDamageEnabled").param("enabled", DamageSystems.FilterUnkillable.CAUSE_DESYNC));
   }
}
