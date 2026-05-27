package com.hypixel.hytale.server.npc.commands;

import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.EntityWrappedArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.entity.Frozen;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.components.StepComponent;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import it.unimi.dsi.fastutil.Pair;
import javax.annotation.Nonnull;

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
         Pair<Ref<EntityStore>, NPCEntity> npcPair = NPCCommandUtils.getTargetNpc(context, this.entityArg, store);
         if (npcPair != null) {
            Ref<EntityStore> ref = (Ref<EntityStore>)npcPair.first();
            store.ensureComponent(ref, Frozen.getComponentType());
            store.addComponent(ref, StepComponent.getComponentType(), new StepComponent(dt));
         }
      }
   }
}
