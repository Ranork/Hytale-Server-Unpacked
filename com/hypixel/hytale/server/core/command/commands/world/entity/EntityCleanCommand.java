package com.hypixel.hytale.server.core.command.commands.world.entity;

import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class EntityCleanCommand extends AbstractWorldCommand {
   public EntityCleanCommand() {
      super("clean", "server.commands.entity.clean.desc", true);
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      store.forEachEntityParallel((index, archetypeChunk, commandBuffer) -> {
         if (!archetypeChunk.getArchetype().contains(Player.getComponentType())) {
            commandBuffer.removeEntity(archetypeChunk.getReferenceTo(index), RemoveReason.REMOVE);
         }
      });
   }
}
