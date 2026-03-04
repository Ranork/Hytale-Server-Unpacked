package com.hypixel.hytale.server.core.permissions.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.permissions.provider.PermissionProvider;
import com.hypixel.hytale.server.core.util.message.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

public class PermGroupCommand extends AbstractCommandCollection {
   public PermGroupCommand() {
      super("group", "server.commands.perm.group.desc");
      this.addSubCommand(new PermGroupCommand.PermGroupListCommand());
      this.addSubCommand(new PermGroupCommand.PermGroupAddCommand());
      this.addSubCommand(new PermGroupCommand.PermGroupRemoveCommand());
   }

   private static class PermGroupAddCommand extends CommandBase {
      @Nonnull
      private final RequiredArg<String> groupArg = this.withRequiredArg("group", "server.commands.perm.group.add.group.desc", ArgTypes.STRING);
      @Nonnull
      private final RequiredArg<List<String>> permissionsArg = this.withListRequiredArg(
         "permissions", "server.commands.perm.group.add.permissions.desc", ArgTypes.STRING
      );

      public PermGroupAddCommand() {
         super("add", "server.commands.perm.group.add.desc");
      }

      @Override
      protected void executeSync(@Nonnull CommandContext context) {
         String group = this.groupArg.get(context);
         HashSet<String> permissions = new HashSet<>(this.permissionsArg.get(context));
         PermissionsModule.get().addGroupPermission(group, permissions);
         context.sendMessage(Message.translation("server.commands.perm.addPermToGroup").param("group", group));
      }
   }

   private static class PermGroupListCommand extends CommandBase {
      @Nonnull
      private final RequiredArg<String> groupArg = this.withRequiredArg("group", "server.commands.perm.group.list.group.desc", ArgTypes.STRING);

      public PermGroupListCommand() {
         super("list", "server.commands.perm.group.list.desc");
      }

      @Override
      protected void executeSync(@Nonnull CommandContext context) {
         String group = this.groupArg.get(context);

         for (PermissionProvider permissionProvider : PermissionsModule.get().getProviders()) {
            Message header = Message.raw(permissionProvider.getName());
            Set<Message> groupPermissions = permissionProvider.getGroupPermissions(group).stream().map(Message::raw).collect(Collectors.toSet());
            context.sendMessage(MessageFormat.list(header, groupPermissions));
         }
      }
   }

   private static class PermGroupRemoveCommand extends CommandBase {
      @Nonnull
      private final RequiredArg<String> groupArg = this.withRequiredArg("group", "server.commands.perm.group.remove.group.desc", ArgTypes.STRING);
      @Nonnull
      private final RequiredArg<List<String>> permissionsArg = this.withListRequiredArg(
         "permissions", "server.commands.perm.group.remove.permissions.desc", ArgTypes.STRING
      );

      public PermGroupRemoveCommand() {
         super("remove", "server.commands.perm.group.remove.desc");
      }

      @Override
      protected void executeSync(@Nonnull CommandContext context) {
         String group = this.groupArg.get(context);
         HashSet<String> permissions = new HashSet<>(this.permissionsArg.get(context));
         PermissionsModule.get().removeGroupPermission(group, permissions);
         context.sendMessage(Message.translation("server.commands.perm.permRemovedFromGroup").param("group", group));
      }
   }
}
