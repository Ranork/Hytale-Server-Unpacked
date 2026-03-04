package com.hypixel.hytale.builtin.path.commands;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class WorldPathRemoveCommand extends AbstractWorldCommand {
   @Nonnull
   private final RequiredArg<String> nameArg = this.withRequiredArg("name", "server.commands.worldpath.remove.name.desc", ArgTypes.STRING);

   public WorldPathRemoveCommand() {
      super("remove", "server.commands.worldpath.remove.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      String name = this.nameArg.get(context);
      if (world.getWorldPathConfig().removePath(name) == null) {
         context.sendMessage(Message.translation("server.universe.worldpath.noPathFound").param("path", name));
      } else {
         context.sendMessage(Message.translation("server.universe.worldpath.removed").param("path", name));
      }
   }
}
