package com.hypixel.hytale.server.core.command.system;

import com.hypixel.hytale.protocol.packets.interface_.CommandArgInfo;
import com.hypixel.hytale.protocol.packets.interface_.CommandOptionalArgEntry;
import com.hypixel.hytale.protocol.packets.interface_.CommandSuggestionOverride;
import com.hypixel.hytale.protocol.packets.interface_.CommandTreeEntry;
import com.hypixel.hytale.protocol.packets.interface_.CommandTreeSync;
import com.hypixel.hytale.protocol.packets.interface_.CommandVariantEntry;
import com.hypixel.hytale.server.core.command.system.arguments.system.AbstractOptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.Argument;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class CommandTreeBuilder {
   private static final Comparator<AbstractCommand> BY_ARG_COUNT = Comparator.comparingInt(cmd -> cmd.getRequiredArguments().size());

   @Nonnull
   CommandTreeSync build(@Nonnull CommandSender sender, @Nonnull Collection<AbstractCommand> commands) {
      ArrayList<CommandTreeEntry> entries = new ArrayList<>();

      for (AbstractCommand command : commands) {
         if (command.hasPermission(sender)) {
            entries.add(this.buildTreeEntry(sender, command));
         }
      }

      CommandTreeSync packet = new CommandTreeSync();
      packet.commands = entries.toArray(new CommandTreeEntry[0]);
      return packet;
   }

   @Nonnull
   private CommandTreeEntry buildTreeEntry(@Nonnull CommandSender sender, @Nonnull AbstractCommand command) {
      return this.buildTreeEntry(sender, command, "/" + command.getName());
   }

   private CommandTreeEntry buildTreeEntry(@Nonnull CommandSender sender, @Nonnull AbstractCommand command, @Nonnull String prefix) {
      CommandTreeEntry entry = new CommandTreeEntry();
      entry.name = command.getName();
      entry.aliases = command.getAliases().toArray(new String[0]);
      entry.description = command.getDescription();
      entry.usageText = this.buildUsageText(command, prefix);
      entry.variants = this.buildVariantEntries(command, prefix);
      ArrayList<CommandTreeEntry> subEntries = new ArrayList<>();
      ArrayList<String> subHints = new ArrayList<>();

      for (AbstractCommand subCmd : command.getSubCommands().values()) {
         if (subCmd.hasPermission(sender)) {
            subEntries.add(this.buildTreeEntry(sender, subCmd, prefix + " " + subCmd.getName()));
            subHints.add(this.buildSubcommandHint(subCmd));
         }
      }

      entry.subcommands = subEntries.toArray(new CommandTreeEntry[0]);
      entry.subcommandHints = subHints.toArray(new String[0]);
      entry.requiredArgs = buildArgInfoArray(command.getRequiredArguments());
      entry.suggestionOverrides = buildSuggestionOverrides(command);
      ArrayList<CommandOptionalArgEntry> optArgs = new ArrayList<>();

      for (Entry<String, AbstractOptionalArg<?, ?>> optEntry : command.getOptionalArguments().entrySet()) {
         AbstractOptionalArg<? extends Argument<?, ?>, ?> arg = (AbstractOptionalArg<? extends Argument<?, ?>, ?>)optEntry.getValue();
         if (arg.hasPermission(sender)) {
            CommandOptionalArgEntry opt = new CommandOptionalArgEntry();
            opt.name = "--" + optEntry.getKey();
            opt.description = arg.getDescription();
            if (arg instanceof FlagArg) {
               opt.hint = "";
               opt.argTypeId = null;
            } else {
               opt.hint = "=<" + arg.getName() + ">";
               opt.argTypeId = arg.getArgumentType().getSuggestionTypeId();
            }

            optArgs.add(opt);
         }
      }

      entry.optionalArgs = optArgs.toArray(new CommandOptionalArgEntry[0]);
      return entry;
   }

   @Nonnull
   private CommandVariantEntry[] buildVariantEntries(@Nonnull AbstractCommand command, @Nonnull String prefix) {
      if (command.getVariantCommands().isEmpty()) {
         return new CommandVariantEntry[0];
      } else {
         ArrayList<AbstractCommand> allCommands = new ArrayList<>();
         allCommands.add(command);
         allCommands.addAll(command.getVariantCommands());
         allCommands.sort(BY_ARG_COUNT);
         ArrayList<CommandVariantEntry> entries = new ArrayList<>();

         for (AbstractCommand variant : allCommands) {
            if (variant != command
               || !(variant.getRequiredArguments().isEmpty() && command instanceof AbstractCommandCollection collection)
               || collection.isNoArgVariant()) {
               CommandVariantEntry variantEntry = new CommandVariantEntry();
               if (variant.getRequiredArguments().isEmpty()) {
                  variantEntry.pattern = command.getName();
               } else {
                  variantEntry.pattern = this.buildVariantArgPattern(variant);
               }

               variantEntry.description = variant.getDescription();
               variantEntry.requiredArgs = buildArgInfoArray(variant.getRequiredArguments());
               variantEntry.suggestionOverrides = buildSuggestionOverrides(variant);
               variantEntry.usageText = buildArgUsageText(prefix, variant.getRequiredArguments());
               entries.add(variantEntry);
            }
         }

         return entries.toArray(new CommandVariantEntry[0]);
      }
   }

   @Nonnull
   private static CommandArgInfo[] buildArgInfoArray(@Nonnull List<RequiredArg<?>> args) {
      CommandArgInfo[] result = new CommandArgInfo[args.size()];

      for (int i = 0; i < args.size(); i++) {
         RequiredArg<?> arg = args.get(i);
         CommandArgInfo info = new CommandArgInfo();
         info.name = arg.getName();
         info.description = arg.getDescription();
         info.argTypeId = arg.getArgumentType().getSuggestionTypeId();
         info.argTypeName = arg.getArgumentType().getName().getMessageId();
         info.valueCount = arg.getArgumentType().getSuggestionValueCount();
         result[i] = info;
      }

      return result;
   }

   @Nullable
   private static CommandSuggestionOverride[] buildSuggestionOverrides(@Nonnull AbstractCommand command) {
      List<AbstractCommand.SuggestionOverrideEntry> overrides = command.getSuggestionOverrides();
      if (overrides == null) {
         return null;
      } else {
         CommandSuggestionOverride[] result = new CommandSuggestionOverride[overrides.size()];

         for (int i = 0; i < overrides.size(); i++) {
            AbstractCommand.SuggestionOverrideEntry source = overrides.get(i);
            CommandSuggestionOverride entry = new CommandSuggestionOverride();
            entry.argStart = source.argStart();
            entry.argCount = source.argCount();
            entry.argTypeId = source.argTypeId();
            result[i] = entry;
         }

         return result;
      }
   }

   @Nonnull
   private String buildSubcommandHint(@Nonnull AbstractCommand subCmd) {
      if (!subCmd.getSubCommands().isEmpty() || !subCmd.getVariantCommands().isEmpty()) {
         return " ...";
      } else {
         return !subCmd.getRequiredArguments().isEmpty() ? this.buildVariantArgPattern(subCmd) : "";
      }
   }

   @Nonnull
   private String buildVariantArgPattern(@Nonnull AbstractCommand variant) {
      List<RequiredArg<?>> args = variant.getRequiredArguments();
      StringBuilder builder = new StringBuilder();

      for (int i = 0; i < args.size(); i++) {
         if (i > 0) {
            builder.append(" ");
         }

         builder.append("<").append(args.get(i).getName()).append(">");
      }

      return builder.toString();
   }

   @Nonnull
   private String buildUsageText(@Nonnull AbstractCommand command, @Nonnull String commandPrefix) {
      Collection<AbstractCommand> variants = command.getVariantCommands();
      if (!variants.isEmpty()) {
         ArrayList<AbstractCommand> allCommands = new ArrayList<>();
         allCommands.add(command);
         allCommands.addAll(variants);
         allCommands.sort(BY_ARG_COUNT);
         return buildArgUsageText(commandPrefix, allCommands.getLast().getRequiredArguments());
      } else {
         return buildArgUsageText(commandPrefix, command.getRequiredArguments());
      }
   }

   @Nonnull
   private static String buildArgUsageText(@Nonnull String prefix, @Nonnull List<RequiredArg<?>> args) {
      StringBuilder builder = new StringBuilder(prefix);

      for (RequiredArg<?> arg : args) {
         builder.append(" <").append(arg.getName()).append(">");
      }

      return builder.toString();
   }
}
