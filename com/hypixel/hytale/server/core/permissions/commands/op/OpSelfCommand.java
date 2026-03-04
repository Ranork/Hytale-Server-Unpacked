package com.hypixel.hytale.server.core.permissions.commands.op;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Constants;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.modules.singleplayer.SingleplayerModule;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;

public class OpSelfCommand extends AbstractPlayerCommand {
   private static final Message MESSAGE_COMMANDS_OP_ADDED = Message.translation("server.commands.op.self.added");
   private static final Message MESSAGE_COMMANDS_OP_REMOVED = Message.translation("server.commands.op.self.removed");
   private static final Message MESSAGE_COMMANDS_NON_VANILLA_PERMISSIONS = Message.translation("server.commands.op.self.nonVanillaPermissions");
   private static final Message MESSAGE_COMMANDS_SINGLEPLAYER_OWNER_REQ = Message.translation("server.commands.op.self.singleplayerOwnerReq");
   private static final Message MESSAGE_COMMANDS_CURRENTLY_OP = Message.translation("server.commands.op.self.currentlyOpNote");

   public OpSelfCommand() {
      super("self", "server.commands.op.self.desc");
   }

   @Override
   protected boolean canGeneratePermission() {
      return false;
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      if (PermissionsModule.get().areProvidersTampered()) {
         playerRef.sendMessage(MESSAGE_COMMANDS_NON_VANILLA_PERMISSIONS);
      } else if (Constants.SINGLEPLAYER && !SingleplayerModule.isOwner(playerRef)) {
         playerRef.sendMessage(MESSAGE_COMMANDS_SINGLEPLAYER_OWNER_REQ);
      } else {
         UUID uuid = playerRef.getUuid();
         PermissionsModule perms = PermissionsModule.get();
         String opGroup = "OP";
         Set<String> groups = perms.getGroupsForUser(uuid);
         boolean isOp = groups.contains("OP");
         if (!Constants.SINGLEPLAYER && !Constants.ALLOWS_SELF_OP_COMMAND) {
            playerRef.sendMessage(
               Message.translation("server.commands.op.self.multiplayerTip")
                  .param("uuidCommand", "uuid")
                  .param("permissionFile", "permissions.json")
                  .param("launchArg", "--allow-op")
            );
            if (isOp) {
               playerRef.sendMessage(MESSAGE_COMMANDS_CURRENTLY_OP);
            }
         } else {
            if (isOp) {
               perms.removeUserFromGroup(uuid, "OP");
               context.sendMessage(MESSAGE_COMMANDS_OP_REMOVED);
            } else {
               perms.addUserToGroup(uuid, "OP");
               context.sendMessage(MESSAGE_COMMANDS_OP_ADDED);
            }
         }
      }
   }
}
