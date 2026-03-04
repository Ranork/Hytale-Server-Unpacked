package com.hypixel.hytale.server.core.command.commands.debug.component.repulsion;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.EntityWrappedArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.modules.entity.repulsion.Repulsion;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RepulsionRemoveCommand extends AbstractCommandCollection {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_ERRORS_TARGET_NOT_IN_WORLD = Message.translation("server.commands.errors.targetNotInWorld");

   public RepulsionRemoveCommand() {
      super("remove", "server.commands.repulsion.remove.desc");
      this.addSubCommand(new RepulsionRemoveCommand.RepulsionRemoveEntityCommand());
      this.addSubCommand(new RepulsionRemoveCommand.RepulsionRemoveSelfCommand());
   }

   public static class RepulsionRemoveEntityCommand extends AbstractWorldCommand {
      @Nonnull
      private static final Message MESSAGE_COMMANDS_REPULSION_REMOVE_SUCCESS = Message.translation("server.commands.repulsion.remove.success");
      @Nonnull
      private final EntityWrappedArg entityArg = this.withRequiredArg("entity", "server.commands.repulsion.remove.entityArg.desc", ArgTypes.ENTITY_ID);

      public RepulsionRemoveEntityCommand() {
         super("entity", "server.commands.repulsion.remove.entity.desc");
      }

      @Override
      protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
         Ref<EntityStore> entityRef = this.entityArg.get(store, context);
         if (entityRef != null && entityRef.isValid()) {
            store.tryRemoveComponent(entityRef, Repulsion.getComponentType());
            context.sendMessage(MESSAGE_COMMANDS_REPULSION_REMOVE_SUCCESS);
         } else {
            context.sendMessage(RepulsionRemoveCommand.MESSAGE_COMMANDS_ERRORS_TARGET_NOT_IN_WORLD);
         }
      }
   }

   public static class RepulsionRemoveSelfCommand extends AbstractTargetPlayerCommand {
      @Nonnull
      private static final Message MESSAGE_COMMANDS_REPULSION_REMOVE_SUCCESS = Message.translation("server.commands.repulsion.remove.success");

      public RepulsionRemoveSelfCommand() {
         super("self", "server.commands.repulsion.remove.self.desc");
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context,
         @Nullable Ref<EntityStore> sourceRef,
         @Nonnull Ref<EntityStore> ref,
         @Nonnull PlayerRef playerRef,
         @Nonnull World world,
         @Nonnull Store<EntityStore> store
      ) {
         store.tryRemoveComponent(ref, Repulsion.getComponentType());
         context.sendMessage(MESSAGE_COMMANDS_REPULSION_REMOVE_SUCCESS);
      }
   }
}
