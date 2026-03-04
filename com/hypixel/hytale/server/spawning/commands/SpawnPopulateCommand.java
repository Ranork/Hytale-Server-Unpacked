package com.hypixel.hytale.server.spawning.commands;

import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.environment.config.Environment;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import javax.annotation.Nonnull;

public class SpawnPopulateCommand extends AbstractWorldCommand {
   @Nonnull
   private final OptionalArg<Environment> environmentArg = this.withOptionalArg(
      "environment", "server.commands.spawning.populate.arg.environment.desc", ArgTypes.ENVIRONMENT_ASSET
   );

   public SpawnPopulateCommand() {
      super("populate", "server.commands.spawning.populate.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      int environmentIndex = this.environmentArg.provided(context)
         ? Environment.getAssetMap().getIndex(this.environmentArg.get(context).getId())
         : Integer.MIN_VALUE;
      store.forEachEntityParallel(NPCEntity.getComponentType(), (index, archetypeChunk, commandBuffer) -> {
         NPCEntity npcComponent = archetypeChunk.getComponent(index, NPCEntity.getComponentType());

         assert npcComponent != null;

         int npcEnvironment = npcComponent.getEnvironment();
         if (npcEnvironment >= 0 && (environmentIndex == Integer.MIN_VALUE || environmentIndex == npcEnvironment)) {
            commandBuffer.removeEntity(archetypeChunk.getReferenceTo(index), RemoveReason.REMOVE);
         }
      });
      WorldConfig worldConfig = world.getWorldConfig();
      worldConfig.setSpawningNPC(true);
      worldConfig.markChanged();
      context.sendMessage(Message.translation("server.commands.spawning.populate.success").param("worldName", world.getName()));
   }
}
