package com.hypixel.hytale.builtin.instances.command;

import com.hypixel.hytale.builtin.instances.InstancesPlugin;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class InstanceExitCommand extends AbstractPlayerCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_INSTANCES_EXIT_FAIL = Message.translation("server.commands.instances.exit.fail");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_INSTANCES_EXIT_SUCCESS_SELF = Message.translation("server.commands.instances.exit.success.self");

   public InstanceExitCommand() {
      super("exit", "server.commands.instances.exit.desc");
      this.addAliases("leave");
      this.addUsageVariant(new InstanceExitCommand.InstanceExitOtherCommand());
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      try {
         InstancesPlugin.exitInstance(ref, store);
         context.sendMessage(MESSAGE_COMMANDS_INSTANCES_EXIT_SUCCESS_SELF);
      } catch (IllegalArgumentException var7) {
         context.sendMessage(MESSAGE_COMMANDS_INSTANCES_EXIT_FAIL);
      }
   }

   private static class InstanceExitOtherCommand extends CommandBase {
      @Nonnull
      private static final Message MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD = Message.translation("server.commands.errors.playerNotInWorld");
      @Nonnull
      private static final Message MESSAGE_COMMANDS_INSTANCES_EXIT_FAIL = Message.translation("server.commands.instances.exit.fail");
      @Nonnull
      private final RequiredArg<PlayerRef> playerArg = this.withRequiredArg("player", "server.commands.argtype.player.desc", ArgTypes.PLAYER_REF);

      InstanceExitOtherCommand() {
         super("server.commands.instances.exit.other.desc");
      }

      @Override
      protected void executeSync(@Nonnull CommandContext context) {
         PlayerRef playerRef = this.playerArg.get(context);
         Ref<EntityStore> ref = playerRef.getReference();
         if (ref != null && ref.isValid()) {
            Store<EntityStore> store = ref.getStore();
            World world = store.getExternalData().getWorld();
            world.execute(() -> {
               Player playerComponent = store.getComponent(ref, Player.getComponentType());
               if (playerComponent == null) {
                  context.sendMessage(MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD);
               } else {
                  try {
                     InstancesPlugin.exitInstance(ref, store);
                     context.sendMessage(Message.translation("server.commands.instances.exit.success.other").param("username", playerRef.getUsername()));
                  } catch (IllegalArgumentException var6) {
                     context.sendMessage(MESSAGE_COMMANDS_INSTANCES_EXIT_FAIL);
                  }
               }
            });
         } else {
            context.sendMessage(MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD);
         }
      }
   }
}
