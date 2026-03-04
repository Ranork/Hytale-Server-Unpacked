package com.hypixel.hytale.server.core.command.commands.world.entity;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class EntityResendCommand extends AbstractWorldCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD = Message.translation("server.commands.errors.playerNotInWorld");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_ENTITY_RESET_NO_ENTITY_VIEWER_COMPONENT = Message.translation(
      "server.commands.entity.resend.noEntityViewerComponent"
   );
   @Nonnull
   private final RequiredArg<PlayerRef> playerArg = this.withRequiredArg("player", "server.commands.entity.resend.player.desc", ArgTypes.PLAYER_REF);

   public EntityResendCommand() {
      super("resend", "server.commands.entity.resend.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      PlayerRef playerRef = this.playerArg.get(context);
      Ref<EntityStore> ref = playerRef.getReference();
      if (ref != null && ref.isValid()) {
         if (!EntityTrackerSystems.despawnAll(ref, store)) {
            context.sendMessage(MESSAGE_COMMANDS_ENTITY_RESET_NO_ENTITY_VIEWER_COMPONENT);
         }
      } else {
         context.sendMessage(MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD);
      }
   }
}
