package com.hypixel.hytale.server.core.command.commands.world.entity;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class EntityCountCommand extends AbstractWorldCommand {
   public EntityCountCommand() {
      super("count", "server.commands.entity.count.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      context.sendMessage(Message.translation("server.commands.entity.count.count").param("count", store.getEntityCount()));
   }
}
