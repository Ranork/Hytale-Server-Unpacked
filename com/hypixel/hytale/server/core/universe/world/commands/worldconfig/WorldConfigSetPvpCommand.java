package com.hypixel.hytale.server.core.universe.world.commands.worldconfig;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class WorldConfigSetPvpCommand extends AbstractWorldCommand {
   @Nonnull
   private final RequiredArg<Boolean> stateArg = this.withRequiredArg("enabled", "server.commands.world.config.setpvp.stateArg.desc", ArgTypes.BOOLEAN);

   public WorldConfigSetPvpCommand() {
      super("pvp", "server.commands.setpvp.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      boolean isPvpEnabled = this.stateArg.provided(context) ? this.stateArg.get(context) : !world.getWorldConfig().isPvpEnabled();
      WorldConfig worldConfig = world.getWorldConfig();
      worldConfig.setPvpEnabled(isPvpEnabled);
      worldConfig.markChanged();
      context.sendMessage(
         Message.translation("server.universe.setpvp.info").param("enabled", isPvpEnabled ? "true" : "false").param("worldName", world.getName())
      );
   }
}
