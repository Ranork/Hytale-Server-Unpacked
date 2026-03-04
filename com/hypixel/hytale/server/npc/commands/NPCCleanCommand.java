package com.hypixel.hytale.server.npc.commands;

import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import javax.annotation.Nonnull;

public class NPCCleanCommand extends AbstractWorldCommand {
   public NPCCleanCommand() {
      super("clean", "server.commands.npc.clean.desc", true);
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      store.forEachEntityParallel(
         NPCEntity.getComponentType(),
         (index, archetypeChunk, commandBuffer) -> commandBuffer.removeEntity(archetypeChunk.getReferenceTo(index), RemoveReason.REMOVE)
      );
   }
}
