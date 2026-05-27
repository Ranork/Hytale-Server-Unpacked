package com.hypixel.hytale.server.core.permissions.commands;

import com.hypixel.hytale.server.core.Constants;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.modules.singleplayer.SingleplayerModule;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import java.util.UUID;
import javax.annotation.Nonnull;

public class SetGroupCommand extends AbstractCommandCollection {
   public SetGroupCommand() {
      super("setgroup", "server.commands.setgroup.desc");
      this.addUsageVariant(new SetGroupCommand.SetGroupSelfVariant());
      this.addUsageVariant(new SetGroupCommand.SetGroupOtherVariant());
   }

   private static void applyGroup(@Nonnull CommandContext context, @Nonnull UUID uuid, @Nonnull String group) {
      PermissionsModule.get().setUserGroup(uuid, group);
      context.sendMessage(Message.translation("server.commands.setgroup.success").param("group", group).param("uuid", uuid.toString()));
   }

   @Override
   public boolean hasPermission(@Nonnull CommandSender sender) {
      return Constants.SINGLEPLAYER && SingleplayerModule.isOwner(sender.getUuid()) ? true : super.hasPermission(sender);
   }

   private static class SetGroupOtherVariant extends CommandBase {
      @Nonnull
      private final RequiredArg<String> groupArg = this.withRequiredArg("group", "server.commands.setgroup.group.desc", GroupArgumentType.INSTANCE);
      @Nonnull
      private final RequiredArg<PlayerRef> playerArg = this.withRequiredArg("player", "server.commands.setgroup.player.desc", ArgTypes.PLAYER_REF);

      SetGroupOtherVariant() {
         super("server.commands.setgroup.other.desc");
      }

      @Override
      public boolean hasPermission(@Nonnull CommandSender sender) {
         return Constants.SINGLEPLAYER && SingleplayerModule.isOwner(sender.getUuid()) ? true : super.hasPermission(sender);
      }

      @Override
      protected void executeSync(@Nonnull CommandContext context) {
         PlayerRef playerRef = this.playerArg.get(context);
         SetGroupCommand.applyGroup(context, playerRef.getUuid(), this.groupArg.get(context));
      }
   }

   private static class SetGroupSelfVariant extends CommandBase {
      @Nonnull
      private final RequiredArg<String> groupArg = this.withRequiredArg("group", "server.commands.setgroup.group.desc", GroupArgumentType.INSTANCE);

      SetGroupSelfVariant() {
         super("server.commands.setgroup.self.desc");
      }

      @Override
      public boolean hasPermission(@Nonnull CommandSender sender) {
         return Constants.SINGLEPLAYER && SingleplayerModule.isOwner(sender.getUuid()) ? true : super.hasPermission(sender);
      }

      @Override
      protected void executeSync(@Nonnull CommandContext context) {
         SetGroupCommand.applyGroup(context, context.sender().getUuid(), this.groupArg.get(context));
      }
   }
}
