package com.hypixel.hytale.server.core.command.commands.player;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
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

public class WhoAmICommand extends AbstractPlayerCommand {
   public static final String UUID_ALIAS = "uuid";

   public WhoAmICommand() {
      super("whoami", "server.commands.whoami.desc");
      this.setPermissionGroup(GameMode.Adventure);
      this.addAliases("uuid");
      this.addUsageVariant(new WhoAmICommand.WhoAmIOtherCommand());
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      sendPlayerInfo(context, playerRef);
   }

   private static void sendPlayerInfo(@Nonnull CommandContext context, @Nonnull PlayerRef playerRef) {
      Message message = Message.translation("server.commands.whoami.header")
         .param("uuid", playerRef.getUuid().toString())
         .param("username", playerRef.getUsername())
         .param("language", playerRef.getLanguage());
      context.sendMessage(message);
   }

   private static class WhoAmIOtherCommand extends CommandBase {
      @Nonnull
      private static final Message MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD = Message.translation("server.commands.errors.playerNotInWorld");
      @Nonnull
      private final RequiredArg<PlayerRef> playerArg = this.withRequiredArg("player", "server.commands.argtype.player.desc", ArgTypes.PLAYER_REF);

      WhoAmIOtherCommand() {
         super("server.commands.whoami.other.desc");
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
                  WhoAmICommand.sendPlayerInfo(context, playerRef);
               }
            });
         } else {
            context.sendMessage(MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD);
         }
      }
   }
}
