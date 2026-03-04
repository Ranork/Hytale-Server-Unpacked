package com.hypixel.hytale.server.core.universe.world.commands;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class SetTickingCommand extends AbstractWorldCommand {
   @Nonnull
   private final RequiredArg<Boolean> tickingArg = this.withRequiredArg("ticking", "server.commands.setticking.ticking.desc", ArgTypes.BOOLEAN);

   public SetTickingCommand() {
      super("setticking", "server.commands.setticking.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      boolean isTicking = this.tickingArg.get(context);
      world.setTicking(isTicking);
      context.sendMessage(Message.translation("server.universe.settick.info").param("status", isTicking).param("worldName", world.getName()));
   }
}
