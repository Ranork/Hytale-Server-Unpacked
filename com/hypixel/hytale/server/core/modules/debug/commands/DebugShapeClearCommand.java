package com.hypixel.hytale.server.core.modules.debug.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.modules.debug.DebugUtils;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class DebugShapeClearCommand extends AbstractPlayerCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_DEBUG_SHAPE_CLEAR_SUCCESS = Message.translation("server.commands.debug.shape.clear.success");

   public DebugShapeClearCommand() {
      super("clear", "server.commands.debug.shape.clear.desc");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      DebugUtils.clear(world);
      context.sendMessage(MESSAGE_COMMANDS_DEBUG_SHAPE_CLEAR_SUCCESS);
   }
}
