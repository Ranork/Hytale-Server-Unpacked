package com.hypixel.hytale.server.core.command.commands.world.entity;

import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.EntityWrappedArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;
import javax.annotation.Nonnull;

public class EntityCloneCommand extends AbstractWorldCommand {
   @Nonnull
   private static final Message MESSAGE_GENERAL_NO_ENTITY_IN_VIEW = Message.translation("server.general.noEntityInView");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_ENTITY_CLONE_CLONED = Message.translation("server.commands.entity.clone.cloned");
   @Nonnull
   private final EntityWrappedArg entityArg = this.withOptionalArg("entity", "server.commands.entity.clone.entity.desc", ArgTypes.ENTITY_ID);
   @Nonnull
   private final DefaultArg<Integer> countArg = this.withDefaultArg(
         "count", "server.commands.entity.clone.count.desc", ArgTypes.INTEGER, 1, "server.commands.entity.clone.count.default"
      )
      .addValidator(Validators.greaterThan(0));

   public EntityCloneCommand() {
      super("clone", "server.commands.entity.clone.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      Ref<EntityStore> entityRef = this.entityArg.get(store, context);
      if (entityRef != null && entityRef.isValid()) {
         CommandSender sender = context.sender();
         int count = this.countArg.get(context);

         for (int i = 0; i < count; i++) {
            Holder<EntityStore> copy = store.copyEntity(entityRef);
            copy.replaceComponent(UUIDComponent.getComponentType(), new UUIDComponent(UUID.randomUUID()));
            store.addEntity(copy, AddReason.SPAWN);
         }

         if (count == 1) {
            sender.sendMessage(MESSAGE_COMMANDS_ENTITY_CLONE_CLONED);
         } else {
            sender.sendMessage(Message.translation("server.commands.entity.clone.cloned.multiple").param("count", count));
         }
      } else {
         context.sendMessage(MESSAGE_GENERAL_NO_ENTITY_IN_VIEW);
      }
   }

   public static void cloneEntity(@Nonnull CommandSender sender, @Nonnull Ref<EntityStore> entityReference, @Nonnull Store<EntityStore> store) {
      Holder<EntityStore> copy = store.copyEntity(entityReference);
      if (copy.getComponent(UUIDComponent.getComponentType()) != null) {
         copy.replaceComponent(UUIDComponent.getComponentType(), new UUIDComponent(UUID.randomUUID()));
      }

      store.addEntity(copy, AddReason.SPAWN);
   }
}
