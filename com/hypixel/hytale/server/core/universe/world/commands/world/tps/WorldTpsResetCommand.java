package com.hypixel.hytale.server.core.universe.world.commands.world.tps;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class WorldTpsResetCommand extends AbstractWorldCommand {
   public WorldTpsResetCommand() {
      super("reset", "server.commands.world.tps.reset.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      int defaultTps = 30;
      world.setTps(30);
      double defaultMs = 33.333333333333336;
      context.sendMessage(
         Message.translation("server.commands.world.tps.reset.success")
            .param("worldName", world.getName())
            .param("tps", 30)
            .param("ms", String.format("%.2f", 33.333333333333336))
      );
   }
}
