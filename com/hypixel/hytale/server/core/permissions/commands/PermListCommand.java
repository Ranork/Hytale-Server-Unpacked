package com.hypixel.hytale.server.core.permissions.commands;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.permissions.provider.PermissionProvider;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class PermListCommand extends CommandBase {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   @Nonnull
   private final FlagArg allFlag = this.withFlagArg("all", "server.commands.perm.list.all.desc");
   @Nonnull
   private final FlagArg consoleFlag = this.withFlagArg("console", "server.commands.perm.list.console.desc");

   public PermListCommand() {
      super("list", "server.commands.perm.list.desc");
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      boolean toConsole = this.consoleFlag.get(context);
      String output;
      if (this.allFlag.get(context)) {
         output = buildAllPermissions();
      } else {
         output = buildOwnPermissions(context);
      }

      if (toConsole) {
         LOGGER.at(Level.INFO).log("%s", output);
         context.sendMessage(Message.translation("server.commands.perm.list.printedToConsole"));
      } else {
         context.sendMessage(Message.raw(output));
      }
   }

   @Nonnull
   private static String buildAllPermissions() {
      Map<String, Set<String>> registered = PermissionsModule.getRegisteredPermissions();
      StringBuilder sb = new StringBuilder();
      sb.append("Registered Permissions (").append(registered.size()).append("):\n");

      for (Entry<String, Set<String>> entry : registered.entrySet()) {
         sb.append("  ").append(entry.getKey());
         if (!entry.getValue().isEmpty()) {
            sb.append(" -> ").append(entry.getValue());
         }

         sb.append('\n');
      }

      return sb.toString();
   }

   @Nonnull
   private static String buildOwnPermissions(@Nonnull CommandContext context) {
      PermissionsModule perms = PermissionsModule.get();
      UUID uuid = context.sender().getUuid();
      Set<String> groups = perms.getGroupsForUser(uuid);
      Map<String, Set<String>> virtualGroups = perms.getVirtualGroups();
      StringBuilder sb = new StringBuilder();
      sb.append("Your Permissions:\n");
      sb.append("  Groups: ").append(groups.isEmpty() ? "(none)" : String.join(", ", groups)).append('\n');
      Set<String> effectivePerms = new TreeSet<>();

      for (PermissionProvider provider : perms.getProviders()) {
         Set<String> userPerms = provider.getUserPermissions(uuid);
         if (userPerms != null) {
            effectivePerms.addAll(userPerms);
         }

         for (String group : groups) {
            collectGroupPermissions(provider, virtualGroups, group, effectivePerms, new HashSet<>());
         }
      }

      sb.append("  Effective (").append(effectivePerms.size()).append("):\n");

      for (String perm : effectivePerms) {
         sb.append("    ").append(perm).append('\n');
      }

      return sb.toString();
   }

   private static void collectGroupPermissions(
      @Nonnull PermissionProvider provider,
      @Nonnull Map<String, Set<String>> virtualGroups,
      @Nonnull String group,
      @Nonnull Set<String> out,
      @Nonnull Set<String> visited
   ) {
      if (visited.add(group)) {
         Set<String> direct = provider.getGroupPermissions(group);
         if (direct != null) {
            out.addAll(direct);
         }

         Set<String> virtual = virtualGroups.get(group);
         if (virtual != null) {
            out.addAll(virtual);
         }

         String parent = provider.getGroupParent(group);
         if (parent != null) {
            collectGroupPermissions(provider, virtualGroups, parent, out, visited);
         }
      }
   }
}
