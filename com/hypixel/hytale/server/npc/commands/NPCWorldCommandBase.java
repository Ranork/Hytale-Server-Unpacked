package com.hypixel.hytale.server.npc.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.EntityWrappedArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class NPCWorldCommandBase extends AbstractWorldCommand {
   @Nonnull
   protected static final Message MESSAGE_COMMANDS_ERRORS_PLAYER_OR_ARG = Message.translation("server.commands.errors.playerOrArg").param("option", "entity");
   @Nonnull
   protected static final Message MESSAGE_COMMANDS_ERRORS_NO_ENTITY_IN_VIEW = Message.translation("server.commands.errors.no_entity_in_view")
      .param("option", "entity");
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

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      NPCEntity npc = this.getNPC(context, store);
      if (npc != null) {
         Ref<EntityStore> ref = npc.getReference();

         assert ref != null;

         assert ref.isValid();

         this.execute(context, npc, world, store, ref);
      }
   }

   protected abstract void execute(
      @Nonnull CommandContext var1, @Nonnull NPCEntity var2, @Nonnull World var3, @Nonnull Store<EntityStore> var4, @Nonnull Ref<EntityStore> var5
   );

   @Nullable
   private NPCEntity getNPC(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store) {
      Ref<EntityStore> ref;
      if (this.entityArg.provided(context)) {
         ref = this.entityArg.get(store, context);
      } else {
         if (!context.isPlayer()) {
            context.sendMessage(MESSAGE_COMMANDS_ERRORS_PLAYER_OR_ARG);
            return null;
         }

         Ref<EntityStore> playerRef = context.senderAsPlayerRef();
         if (playerRef == null || !playerRef.isValid()) {
            context.sendMessage(MESSAGE_COMMANDS_ERRORS_PLAYER_OR_ARG);
            return null;
         }

         ref = TargetUtil.getTargetEntity(playerRef, store);
         if (ref == null) {
            context.sendMessage(MESSAGE_COMMANDS_ERRORS_NO_ENTITY_IN_VIEW);
            return null;
         }
      }

      return ref == null ? null : ensureIsNPC(context, store, ref);
   }

   @Nullable
   protected static NPCEntity ensureIsNPC(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, Ref<EntityStore> ref) {
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
