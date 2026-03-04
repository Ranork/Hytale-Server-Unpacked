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
import com.hypixel.hytale.server.core.modules.entity.component.Interactable;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import it.unimi.dsi.fastutil.Pair;
import javax.annotation.Nonnull;

public class NPCFreezeCommand extends AbstractWorldCommand {
   @Nonnull
   private final FlagArg allArg = this.withFlagArg("all", "server.commands.npc.freeze.all");
   @Nonnull
   private final FlagArg toggleArg = this.withFlagArg("toggle", "server.commands.npc.freeze.toggle");
   @Nonnull
   private final EntityWrappedArg entityArg = this.withOptionalArg("entity", "server.commands.entity.entity.desc", ArgTypes.ENTITY_ID);

   public NPCFreezeCommand() {
      super("freeze", "server.commands.npc.freeze.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      if (this.allArg.get(context)) {
         store.forEachEntityParallel(
            NPCEntity.getComponentType(),
            (index, archetypeChunk, commandBuffer) -> commandBuffer.ensureComponent(archetypeChunk.getReferenceTo(index), Frozen.getComponentType())
         );
         store.forEachEntityParallel(ItemComponent.getComponentType(), (index, archetypeChunk, commandBuffer) -> {
            Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
            commandBuffer.ensureComponent(ref, Frozen.getComponentType());
            commandBuffer.ensureComponent(ref, Interactable.getComponentType());
         });
      } else {
         Pair<Ref<EntityStore>, NPCEntity> targetNpcPair = NPCCommandUtils.getTargetNpc(context, this.entityArg, store);
         if (targetNpcPair != null) {
            Ref<EntityStore> targetNpcRef = (Ref<EntityStore>)targetNpcPair.first();
            String roleName = ((NPCEntity)targetNpcPair.second()).getRoleName();
            if (this.toggleArg.get(context)) {
               boolean wasFrozen = store.getArchetype(targetNpcRef).contains(Frozen.getComponentType());
               if (wasFrozen) {
                  store.tryRemoveComponent(targetNpcRef, Frozen.getComponentType());
                  context.sendMessage(Message.translation("server.commands.npc.thaw.npc").param("role", roleName));
               } else {
                  store.ensureComponent(targetNpcRef, Frozen.getComponentType());
                  context.sendMessage(Message.translation("server.commands.npc.freeze.npc").param("role", roleName));
               }
            } else {
               store.ensureComponent(targetNpcRef, Frozen.getComponentType());
               context.sendMessage(Message.translation("server.commands.npc.freeze.npc").param("role", roleName));
            }
         }
      }
   }
}
