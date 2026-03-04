package com.hypixel.hytale.server.core.command.commands.world.entity.snapshot;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.modules.entity.system.SnapshotSystems;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

public class EntitySnapshotLengthCommand extends CommandBase {
   @Nonnull
   private final RequiredArg<Integer> lengthArg = this.withRequiredArg("length", "server.commands.entity.snapshot.length.length.desc", ArgTypes.INTEGER);

   public EntitySnapshotLengthCommand() {
      super("length", "server.commands.entity.snapshot.length.desc");
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      int lengthMs = this.lengthArg.get(context);
      SnapshotSystems.HISTORY_LENGTH_NS = TimeUnit.MILLISECONDS.toNanos(lengthMs);
      long millis = TimeUnit.NANOSECONDS.toMillis(SnapshotSystems.HISTORY_LENGTH_NS);
      context.sendMessage(Message.translation("server.commands.entity.snapshot.lengthSet").param("millis", millis));
   }
}
