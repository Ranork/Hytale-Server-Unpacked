package com.hypixel.hytale.server.core.permissions.commands;

import com.hypixel.hytale.common.util.StringUtil;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.CommandUtil;
import com.hypixel.hytale.server.core.command.system.ParseResult;
import com.hypixel.hytale.server.core.command.system.arguments.types.SingleArgumentType;
import com.hypixel.hytale.server.core.command.system.suggestion.SuggestionResult;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.permissions.provider.HytalePermissionsProvider;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GroupArgumentType extends SingleArgumentType<String> {
   public static final GroupArgumentType INSTANCE = new GroupArgumentType();

   private GroupArgumentType() {
      super("group", "server.commands.setgroup.group.desc", "Admin", "hytale:Admin", "Adventurer");
   }

   @Override
   public void suggest(@Nonnull CommandSender sender, @Nonnull String textAlreadyEntered, int numParametersTyped, @Nonnull SuggestionResult result) {
      Set<String> allGroups = PermissionsModule.get().getAllRegisteredGroups();
      Map<String, List<String>> shortNameToFull = buildShortNameMap(allGroups);
      String lowerInput = textAlreadyEntered.toLowerCase();

      for (Entry<String, List<String>> entry : shortNameToFull.entrySet()) {
         List<String> fullNames = entry.getValue();
         if (fullNames.size() == 1) {
            String shortName = entry.getKey();
            if (shortName.toLowerCase().startsWith(lowerInput)) {
               result.suggest(shortName);
            }
         } else {
            for (String fullName : fullNames) {
               if (fullName.toLowerCase().startsWith(lowerInput)) {
                  result.suggest(fullName);
               }
            }
         }
      }
   }

   @Nullable
   public String parse(@Nonnull String input, @Nonnull ParseResult parseResult) {
      String resolved = resolveGroup(input);
      if (resolved != null) {
         return resolved;
      } else {
         Set<String> allGroups = PermissionsModule.get().getAllRegisteredGroups();
         List<String> names = new ObjectArrayList(allGroups);
         parseResult.fail(
            Message.empty()
               .insert(Message.translation("server.commands.setgroup.unknownGroup").param("group", input))
               .insert(Message.raw(" "))
               .insert(
                  Message.translation("server.general.failed.didYouMean")
                     .param("choices", StringUtil.sortByFuzzyDistance(input.toLowerCase(), names, CommandUtil.RECOMMEND_COUNT).toString())
               )
         );
         return null;
      }
   }

   @Nullable
   private static String resolveGroup(@Nonnull String input) {
      String legacyResolved = HytalePermissionsProvider.resolveGroupName(input);
      Set<String> allGroups = PermissionsModule.get().getAllRegisteredGroups();
      if (allGroups.contains(legacyResolved)) {
         return legacyResolved;
      } else {
         for (String group : allGroups) {
            if (group.equalsIgnoreCase(legacyResolved)) {
               return group;
            }
         }

         String hytaleScoped = "hytale:" + input;

         for (String groupx : allGroups) {
            if (groupx.equalsIgnoreCase(hytaleScoped)) {
               return groupx;
            }
         }

         for (String groupxx : allGroups) {
            int colonIdx = groupxx.indexOf(58);
            if (colonIdx >= 0) {
               String shortName = groupxx.substring(colonIdx + 1);
               if (shortName.equalsIgnoreCase(input)) {
                  return groupxx;
               }
            }
         }

         return null;
      }
   }

   @Nonnull
   private static Map<String, List<String>> buildShortNameMap(@Nonnull Set<String> allGroups) {
      Map<String, List<String>> map = new HashMap<>();

      for (String group : allGroups) {
         int colonIdx = group.indexOf(58);
         String shortName = colonIdx >= 0 ? group.substring(colonIdx + 1) : group;
         map.computeIfAbsent(shortName, k -> new ObjectArrayList()).add(group);
      }

      return map;
   }
}
