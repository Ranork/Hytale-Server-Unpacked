package com.hypixel.hytale.server.core.modules.interaction.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.EnumArgumentType;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.none.SelectInteraction;
import javax.annotation.Nonnull;

public class InteractionSetSnapshotSourceCommand extends CommandBase {
   @Nonnull
   private static final EnumArgumentType<SelectInteraction.SnapshotSource> SNAPSHOT_SOURCE_ARG_TYPE = new EnumArgumentType<>(
      "server.commands.parsing.argtype.snapshotsource.name", SelectInteraction.SnapshotSource.class
   );
   @Nonnull
   private final RequiredArg<SelectInteraction.SnapshotSource> snapshotSourceArg = this.withRequiredArg(
      "snapshotSource", "server.commands.interaction.snapshotSource.set.snapshotSource.desc", SNAPSHOT_SOURCE_ARG_TYPE
   );

   public InteractionSetSnapshotSourceCommand() {
      super("set", "server.commands.interaction.snapshotSource.set.desc");
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      SelectInteraction.SnapshotSource source = this.snapshotSourceArg.get(context);
      SelectInteraction.SNAPSHOT_SOURCE = source;
      context.sendMessage(Message.translation("server.commands.interaction.snapshotSource.set").param("source", source.name()));
   }
}
