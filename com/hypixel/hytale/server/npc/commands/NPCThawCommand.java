package com.hypixel.hytale.server.npc.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.EntityWrappedArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.entity.Frozen;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import it.unimi.dsi.fastutil.Pair;
import javax.annotation.Nonnull;

public class NPCThawCommand extends AbstractWorldCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_NPC_THAW_ALL = Message.translation("server.commands.npc.thaw.all");
   @Nonnull
   private final FlagArg allArg = this.withFlagArg("all", "server.commands.npc.thaw.arg.all");
   @Nonnull
   private final EntityWrappedArg entityArg = this.withOptionalArg("entity", "server.commands.entity.entity.desc", ArgTypes.ENTITY_ID);

   public NPCThawCommand() {
      super("thaw", "server.commands.npc.thaw.desc");
      this.addAliases("unfreeze");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      if (this.allArg.get(context)) {
         store.forEachEntityParallel(
            NPCEntity.getComponentType(),
            (index, archetypeChunk, commandBuffer) -> commandBuffer.tryRemoveComponent(archetypeChunk.getReferenceTo(index), Frozen.getComponentType())
         );
         context.sendMessage(MESSAGE_COMMANDS_NPC_THAW_ALL);
      } else {
         Pair<Ref<EntityStore>, NPCEntity> targetNpcPair = NPCCommandUtils.getTargetNpc(context, this.entityArg, store);
         if (targetNpcPair != null) {
            Ref<EntityStore> targetNpcRef = (Ref<EntityStore>)targetNpcPair.first();
            NPCEntity targetNpcComponent = (NPCEntity)targetNpcPair.second();
            store.tryRemoveComponent(targetNpcRef, Frozen.getComponentType());
            context.sendMessage(Message.translation("server.commands.npc.thaw.npc").param("role", targetNpcComponent.getRoleName()));
         }
      }
   }
}
