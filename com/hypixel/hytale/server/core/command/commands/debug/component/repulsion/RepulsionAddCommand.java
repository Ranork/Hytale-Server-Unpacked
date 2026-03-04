package com.hypixel.hytale.server.core.command.commands.debug.component.repulsion;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.EntityWrappedArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.modules.entity.repulsion.Repulsion;
import com.hypixel.hytale.server.core.modules.entity.repulsion.RepulsionConfig;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RepulsionAddCommand extends AbstractCommandCollection {
   private static final Message MESSAGE_COMMANDS_ERRORS_TARGET_NOT_IN_WORLD = Message.translation("server.commands.errors.targetNotInWorld");

   public RepulsionAddCommand() {
      super("add", "server.commands.repulsion.add.desc");
      this.addSubCommand(new RepulsionAddCommand.RepulsionAddEntityCommand());
      this.addSubCommand(new RepulsionAddCommand.RepulsionAddSelfCommand());
   }

   public static class RepulsionAddEntityCommand extends AbstractWorldCommand {
      @Nonnull
      private static final Message MESSAGE_COMMANDS_REPULSION_ADD_ALREADY_ADDED = Message.translation("server.commands.repulsion.add.alreadyAdded");
      @Nonnull
      private static final Message COMMANDS_REPULSION_ADD_SUCCESS = Message.translation("server.commands.repulsion.add.success");
      @Nonnull
      private final RequiredArg<RepulsionConfig> repulsionConfigArg = this.withRequiredArg(
         "repulsionConfig", "server.commands.repulsion.add.repulsionConfig.desc", ArgTypes.REPULSION_CONFIG
      );
      @Nonnull
      private final EntityWrappedArg entityArg = this.withRequiredArg("entity", "server.commands.repulsion.add.entityArg.desc", ArgTypes.ENTITY_ID);

      public RepulsionAddEntityCommand() {
         super("entity", "server.commands.repulsion.add.entity.desc");
      }

      @Override
      protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
         Ref<EntityStore> entityRef = this.entityArg.get(store, context);
         if (entityRef != null && entityRef.isValid()) {
            RepulsionConfig repulsionConfig = this.repulsionConfigArg.get(context);
            if (store.getArchetype(entityRef).contains(Repulsion.getComponentType())) {
               context.sendMessage(MESSAGE_COMMANDS_REPULSION_ADD_ALREADY_ADDED);
            } else {
               store.addComponent(entityRef, Repulsion.getComponentType(), new Repulsion(repulsionConfig));
               context.sendMessage(COMMANDS_REPULSION_ADD_SUCCESS);
            }
         } else {
            context.sendMessage(RepulsionAddCommand.MESSAGE_COMMANDS_ERRORS_TARGET_NOT_IN_WORLD);
         }
      }
   }

   public static class RepulsionAddSelfCommand extends AbstractTargetPlayerCommand {
      @Nonnull
      private static final Message MESSAGE_COMMANDS_REPULSION_ADD_ALREADY_ADDED = Message.translation("server.commands.repulsion.add.alreadyAdded");
      @Nonnull
      private static final Message MESSAGE_COMMANDS_REPULSION_ADD_SUCCESS = Message.translation("server.commands.repulsion.add.success");
      @Nonnull
      private final RequiredArg<RepulsionConfig> repulsionConfigArg = this.withRequiredArg(
         "repulsionConfig", "server.commands.repulsion.add.repulsionConfig.desc", ArgTypes.REPULSION_CONFIG
      );

      public RepulsionAddSelfCommand() {
         super("self", "server.commands.repulsion.add.self.desc");
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
         if (store.getArchetype(ref).contains(Repulsion.getComponentType())) {
            context.sendMessage(MESSAGE_COMMANDS_REPULSION_ADD_ALREADY_ADDED);
         } else {
            RepulsionConfig repulsionConfig = this.repulsionConfigArg.get(context);
            store.addComponent(ref, Repulsion.getComponentType(), new Repulsion(repulsionConfig));
            context.sendMessage(MESSAGE_COMMANDS_REPULSION_ADD_SUCCESS);
         }
      }
   }
}
