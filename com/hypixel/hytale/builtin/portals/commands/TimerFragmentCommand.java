package com.hypixel.hytale.builtin.portals.commands;

import com.hypixel.hytale.builtin.portals.resources.PortalWorld;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class TimerFragmentCommand extends PortalWorldCommandBase {
   @Nonnull
   private final RequiredArg<Integer> remainingSecondsArg = this.withRequiredArg("seconds", "server.commands.fragment.timer.arg.seconds.desc", ArgTypes.INTEGER);

   public TimerFragmentCommand() {
      super("timer", "server.commands.fragment.timer.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull PortalWorld portalWorld, @Nonnull Store<EntityStore> store) {
      int before = (int)portalWorld.getRemainingSeconds(world);
      int desired = this.remainingSecondsArg.get(context);
      PortalWorld.setRemainingSeconds(world, desired);
      context.sendMessage(Message.translation("server.commands.fragment.timer.success").param("before", before).param("after", desired));
   }
}
