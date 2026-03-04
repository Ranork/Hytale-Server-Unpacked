package com.hypixel.hytale.builtin.commandmacro;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.ParseResult;
import com.hypixel.hytale.server.core.command.system.arguments.system.AbstractOptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.Argument;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgumentType;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.console.ConsoleSender;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MacroCommandBase extends AbstractAsyncCommand {
   @Nonnull
   public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   @Nonnull
   private static final Pattern regexBracketPattern = Pattern.compile("\\{(.*?)}");
   @Nonnull
   private static final Pattern PATTERN = Pattern.compile("\\\\\\{");
   @Nonnull
   private final Map<String, Argument<?, ?>> arguments = new Object2ObjectOpenHashMap();
   @Nonnull
   private final List<Pair<String, List<MacroCommandReplacement>>> commandReplacements = new ObjectArrayList();
   private final Map<String, String> defaultValueStrings = new Object2ObjectOpenHashMap();

   public MacroCommandBase(
      @Nonnull String name, @Nullable String[] aliases, @Nonnull String description, @Nullable MacroCommandParameter[] parameters, @Nonnull String[] commands
   ) {
      super(name, description);
      if (aliases != null) {
         this.addAliases(aliases);
      }

      if (parameters != null) {
         ParseResult parseResult = new ParseResult();

         for (MacroCommandParameter parameter : parameters) {
            this.arguments
               .put(
                  parameter.getName(),
                  switch (parameter.getRequirement()) {
                     case REQUIRED -> this.withRequiredArg(parameter.getName(), parameter.getDescription(), parameter.getArgumentType().getArgumentType());
                     case OPTIONAL -> this.withOptionalArg(parameter.getName(), parameter.getDescription(), parameter.getArgumentType().getArgumentType());
                     case FLAG -> this.withFlagArg(parameter.getName(), parameter.getDescription());
                     case DEFAULT -> this.withDefaultArg(
                        parameter.getName(),
                        parameter.getDescription(),
                        parameter.getArgumentType().getArgumentType(),
                        parameter.getDefaultValue(),
                        parameter.getDefaultValueDescription(),
                        parseResult
                     );
                     default -> throw new IllegalStateException("Unexpected value for Requirement: " + parameter.getRequirement());
                  }
               );
         }

         if (parseResult.failed()) {
            parseResult.sendMessages(ConsoleSender.INSTANCE);
            return;
         }
      }

      Matcher matcher = regexBracketPattern.matcher("");

      for (int i = 0; i < commands.length; i++) {
         String command = commands[i];
         ObjectArrayList<MacroCommandReplacement> replacements = new ObjectArrayList();
         Matcher reset = matcher.reset(command);

         while (reset.find()) {
            String result = reset.group(1);
            String[] splitByColons = result.split(":");
            if (command.charAt(matcher.start(1) - 2) != '\\') {
               String replacementSubstring = command.substring(matcher.start(1) - 1, matcher.end(1) + 1);

               MacroCommandReplacement replacement = switch (splitByColons.length) {
                  case 1 -> new MacroCommandReplacement(result, replacementSubstring);
                  case 2 -> new MacroCommandReplacement(splitByColons[1], replacementSubstring, splitByColons[0]);
                  default -> throw new IllegalArgumentException("Cannot have more than one colon in a macro command parameter: '" + result + "'");
               };
               if (!this.arguments.containsKey(replacement.getNameOfReplacingArg())) {
                  throw new IllegalArgumentException(
                     "Cannot define command with replacement token that does not refer to an argument: " + replacement.getNameOfReplacingArg()
                  );
               }

               replacements.add(replacement);
            }
         }

         command = PATTERN.matcher(command).replaceAll("{");
         commands[i] = command;
         this.commandReplacements.add(Pair.of(command, replacements));
      }
   }

   @Nullable
   private <D> Argument<?, ?> withDefaultArg(
      String name,
      String description,
      @Nonnull ArgumentType<D> argumentType,
      @Nonnull String defaultValue,
      String defaultValueDescription,
      @Nonnull ParseResult parseResult
   ) {
      D parsedData = argumentType.parse(defaultValue.split(" "), parseResult);
      if (parseResult.failed()) {
         LOGGER.at(Level.WARNING).log("Could not parse default argument value for argument: '" + name + "' on Macro Command: '" + this.getName() + "'.");
         parseResult.sendMessages(ConsoleSender.INSTANCE);
         return null;
      } else {
         this.defaultValueStrings.put(name, defaultValue);
         return this.withDefaultArg(name, description, argumentType, parsedData, defaultValueDescription);
      }
   }

   @Nonnull
   @Override
   protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
      List<String> commandsToExecute = new ObjectArrayList();
      CommandSender commandSender = context.sender();
      String macro = context.getCalledCommand().getName();
      LOGGER.at(Level.INFO).log("%s expanding command macro: %s", commandSender.getDisplayName(), macro);

      for (Pair<String, List<MacroCommandReplacement>> stringListPair : this.commandReplacements) {
         String command = (String)stringListPair.key();

         for (MacroCommandReplacement replacement : (List)stringListPair.value()) {
            String stringToInject = "";
            boolean shouldInject = true;
            Argument<? extends Argument<?, ?>, ?> argument = (Argument<? extends Argument<?, ?>, ?>)this.arguments.get(replacement.getNameOfReplacingArg());
            if (!(argument instanceof AbstractOptionalArg) || context.provided(argument)) {
               stringToInject = String.join(" ", context.getInput(this.arguments.get(replacement.getNameOfReplacingArg())));
            } else if (argument instanceof DefaultArg) {
               stringToInject = this.defaultValueStrings.get(argument.getName());
            } else {
               shouldInject = false;
            }

            if (shouldInject && replacement.getOptionalArgumentKey() != null) {
               stringToInject = replacement.getOptionalArgumentKey() + stringToInject;
            }

            command = command.replace(replacement.getStringToReplaceWithValue(), shouldInject ? stringToInject : "");
         }

         commandsToExecute.add(command);
      }

      CompletableFuture<Void> completableFuture = CompletableFuture.completedFuture(null);

      for (String command : commandsToExecute) {
         completableFuture = completableFuture.thenCompose(VOID -> CommandManager.get().handleCommand(commandSender, command));
      }

      return completableFuture;
   }
}
