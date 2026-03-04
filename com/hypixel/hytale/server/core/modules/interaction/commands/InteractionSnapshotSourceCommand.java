package com.hypixel.hytale.server.core.modules.interaction.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.none.SelectInteraction;
import javax.annotation.Nonnull;

public class InteractionSnapshotSourceCommand extends CommandBase {
   public InteractionSnapshotSourceCommand() {
      super("snapshotsource", "server.commands.interaction.snapshotSource.desc");
      this.addSubCommand(new InteractionSetSnapshotSourceCommand());
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      context.sendMessage(Message.translation("server.commands.interaction.snapshotSource.get").param("source", SelectInteraction.SNAPSHOT_SOURCE.name()));
   }
}
