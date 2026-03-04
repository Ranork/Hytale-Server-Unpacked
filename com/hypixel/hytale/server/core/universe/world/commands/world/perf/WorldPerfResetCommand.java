package com.hypixel.hytale.server.core.universe.world.commands.world.perf;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class WorldPerfResetCommand extends AbstractWorldCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_WORLD_PERF_RESET_ALL = Message.translation("server.commands.world.perf.reset.all");
   private final FlagArg allFlag = this.withFlagArg("all", "server.commands.world.perf.reset.all.desc");

   public WorldPerfResetCommand() {
      super("reset", "server.commands.world.perf.reset.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      if (this.allFlag.provided(context)) {
         Universe.get().getWorlds().forEach((name, w) -> w.clearMetrics());
         context.sendMessage(MESSAGE_COMMANDS_WORLD_PERF_RESET_ALL);
      } else {
         world.clearMetrics();
         context.sendMessage(Message.translation("server.commands.world.perf.reset").param("worldName", world.getName()));
      }
   }
}
