package com.hypixel.hytale.server.npc.commands;

import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.EntityWrappedArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.entity.Frozen;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.hypixel.hytale.server.npc.components.StepComponent;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NPCStepCommand extends AbstractWorldCommand {
   @Nonnull
   private final FlagArg allArg = this.withFlagArg("all", "server.commands.npc.step.all");
   @Nonnull
   private final EntityWrappedArg entityArg = this.withOptionalArg("entity", "server.commands.entity.entity.desc", ArgTypes.ENTITY_ID);
   @Nonnull
   private final OptionalArg<Float> dtArg = this.withOptionalArg("dt", "server.commands.npc.step.dt.desc", ArgTypes.FLOAT)
      .addValidator(Validators.greaterThan(0.0F));

   public NPCStepCommand() {
      super("step", "server.commands.npc.step.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      float dt = this.dtArg.provided(context) ? this.dtArg.get(context) : 1.0F / world.getTps();
      if (this.allArg.get(context)) {
         store.forEachEntityParallel(NPCEntity.getComponentType(), (index, archetypeChunk, commandBuffer) -> {
            commandBuffer.ensureComponent(archetypeChunk.getReferenceTo(index), Frozen.getComponentType());
            commandBuffer.addComponent(archetypeChunk.getReferenceTo(index), StepComponent.getComponentType(), new StepComponent(dt));
         });
      } else {
         NPCEntity npc = this.getNPC(context, store);
         if (npc != null) {
            Ref<EntityStore> ref = npc.getReference();
            if (ref != null && ref.isValid()) {
               store.ensureComponent(ref, Frozen.getComponentType());
               store.addComponent(ref, StepComponent.getComponentType(), new StepComponent(dt));
            }
         }
      }
   }

   @Nullable
   private NPCEntity getNPC(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store) {
      Ref<EntityStore> ref;
      if (this.entityArg.provided(context)) {
         ref = this.entityArg.get(store, context);
      } else {
         if (!context.isPlayer()) {
            context.sendMessage(Message.translation("server.commands.errors.playerOrArg").param("option", "entity"));
            return null;
         }

         Ref<EntityStore> playerRef = context.senderAsPlayerRef();
         if (playerRef == null || !playerRef.isValid()) {
            context.sendMessage(Message.translation("server.commands.errors.playerOrArg").param("option", "entity"));
            return null;
         }

         ref = TargetUtil.getTargetEntity(playerRef, store);
         if (ref == null) {
            context.sendMessage(Message.translation("server.commands.errors.no_entity_in_view").param("option", "entity"));
            return null;
         }
      }

      NPCEntity npcComponent = store.getComponent(ref, NPCEntity.getComponentType());
      if (npcComponent == null) {
         UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());

         assert uuidComponent != null;

         UUID uuid = uuidComponent.getUuid();
         context.sendMessage(Message.translation("server.commands.errors.not_npc").param("uuid", uuid.toString()));
         return null;
      } else {
         return npcComponent;
      }
   }
}
