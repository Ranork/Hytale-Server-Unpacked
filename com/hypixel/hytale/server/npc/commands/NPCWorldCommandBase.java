package com.hypixel.hytale.server.npc.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.EntityWrappedArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import it.unimi.dsi.fastutil.Pair;
import javax.annotation.Nonnull;

public abstract class NPCWorldCommandBase extends AbstractWorldCommand {
   @Nonnull
   protected final EntityWrappedArg entityArg = this.withOptionalArg("entity", "server.commands.entity.entity.desc", ArgTypes.ENTITY_ID);

   public NPCWorldCommandBase(@Nonnull String name, @Nonnull String description) {
      super(name, description);
   }

   public NPCWorldCommandBase(@Nonnull String name, @Nonnull String description, boolean requiresConfirmation) {
      super(name, description, requiresConfirmation);
   }

   public NPCWorldCommandBase(@Nonnull String description) {
      super(description);
   }

   protected abstract void execute(
      @Nonnull CommandContext var1, @Nonnull NPCEntity var2, @Nonnull World var3, @Nonnull Store<EntityStore> var4, @Nonnull Ref<EntityStore> var5
   );

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      Pair<Ref<EntityStore>, NPCEntity> npcPair = NPCCommandUtils.getTargetNpc(context, this.entityArg, store);
      if (npcPair != null) {
         this.execute(context, (NPCEntity)npcPair.second(), world, store, (Ref<EntityStore>)npcPair.first());
      }
   }
}
