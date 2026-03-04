package com.hypixel.hytale.builtin.mounts.commands;

import com.hypixel.hytale.builtin.mounts.MountedComponent;
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

public class DismountCommand extends AbstractPlayerCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_DISMOUNT_DISMOUNT_ATTEMPTED = Message.translation("server.commands.dismount.dismountAttempted");

   public DismountCommand() {
      super("dismount", "server.commands.dismount.desc");
      this.addUsageVariant(new DismountCommand.DismountOtherCommand());
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      store.tryRemoveComponent(ref, MountedComponent.getComponentType());
      context.sendMessage(MESSAGE_COMMANDS_DISMOUNT_DISMOUNT_ATTEMPTED);
   }

   private static class DismountOtherCommand extends CommandBase {
      @Nonnull
      private static final Message MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD = Message.translation("server.commands.errors.playerNotInWorld");
      @Nonnull
      private final RequiredArg<PlayerRef> playerArg = this.withRequiredArg("player", "server.commands.argtype.player.desc", ArgTypes.PLAYER_REF);

      DismountOtherCommand() {
         super("server.commands.dismount.other.desc");
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
                  store.tryRemoveComponent(ref, MountedComponent.getComponentType());
                  context.sendMessage(Message.translation("server.commands.dismount.dismountOther").param("username", playerRef.getUsername()));
               }
            });
         } else {
            context.sendMessage(MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD);
         }
      }
   }
}
