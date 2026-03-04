package com.hypixel.hytale.server.core.permissions.commands.op;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.permissions.HytalePermissions;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;

public class OpRemoveCommand extends CommandBase {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_OP_REMOVED_TARGET = Message.translation("server.commands.op.removed.target");
   @Nonnull
   private final RequiredArg<UUID> playerArg = this.withRequiredArg("player", "server.commands.op.remove.player.desc", ArgTypes.PLAYER_UUID);

   public OpRemoveCommand() {
      super("remove", "server.commands.op.remove.desc");
      this.requirePermission(HytalePermissions.fromCommand("op.remove"));
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      UUID uuid = this.playerArg.get(context);
      PermissionsModule permissionsModule = PermissionsModule.get();
      String opGroup = "OP";
      PlayerRef playerRef = Universe.get().getPlayer(uuid);
      String displayName = playerRef != null ? playerRef.getUsername() : uuid.toString();
      Message displayMessage = Message.raw(displayName).bold(true);
      Set<String> groups = permissionsModule.getGroupsForUser(uuid);
      if (groups.contains("OP")) {
         permissionsModule.removeUserFromGroup(uuid, "OP");
         context.sendMessage(Message.translation("server.commands.op.removed").param("username", displayMessage));
         if (playerRef != null) {
            playerRef.sendMessage(MESSAGE_COMMANDS_OP_REMOVED_TARGET);
         }
      } else {
         context.sendMessage(Message.translation("server.commands.op.notOp").param("username", displayMessage));
      }
   }
}
