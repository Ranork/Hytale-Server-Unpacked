package com.hypixel.hytale.server.core.command.system;

import com.hypixel.hytale.common.util.StringUtil;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Constants;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.arguments.system.AbstractOptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.ArgWrapper;
import com.hypixel.hytale.server.core.command.system.arguments.system.Argument;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.WrappedArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgumentType;
import com.hypixel.hytale.server.core.command.system.arguments.types.ListArgumentType;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.exceptions.GeneralCommandException;
import com.hypixel.hytale.server.core.modules.i18n.I18nModule;
import com.hypixel.hytale.server.core.plugin.PluginBase;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;
import it.unimi.dsi.fastutil.objects.ObjectBooleanPair;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class AbstractCommand {
   @Nonnull
   public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   public static final String[] EMPTY_STRING_ARRAY = new String[0];
   @Nonnull
   private static final Message MESSAGE_COMMANDS_HELP_NO_PERMISSIBLE_SUB_COMMAND = Message.translation("server.commands.help.noPermissibleSubCommand");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_PARSING_ERROR_NO_PERMISSION_FOR_COMMAND = Message.translation(
      "server.commands.parsing.error.noPermissionForCommand"
   );
   @Nonnull
   private static final Message MESSAGE_COMMANDS_PARSING_ERROR_ATTEMPTED_UNSAFE = Message.translation("server.commands.parsing.error.attemptedUnsafe");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_PARSING_USAGE_REQUIRES_CONFIRMATION = Message.translation("server.commands.parsing.usage.requiresConfirmation");
   @Nonnull
   private static final Message MESSAGE_COMMAND_SINGLEPLAYER = Message.translation("server.commands.parsing.error.unavailableInSingleplayer");
   @Nonnull
   static final String CONFIRM_ARG_TAG = "confirm";
   @Nonnull
   private static final String COLOR_STRING_ARG_REQUIRED = "#C1E0FF";
   @Nonnull
   private static final String COLOR_STRING_ARG_OPTIONAL = "#7E9EBC";
   private AbstractCommand parentCommand;
   @Nullable
   private final String name;
   @Nonnull
   private final Set<String> aliases = new HashSet<>();
   @Nullable
   private final String description;
   @Nonnull
   private final List<RequiredArg<?>> requiredArguments = new ObjectArrayList();
   @Nonnull
   private final Map<String, AbstractOptionalArg<?, ?>> optionalArguments = new Object2ObjectOpenHashMap();
   private AbbreviationMap<AbstractOptionalArg<?, ?>> argumentAbbreviationMap;
   @Nonnull
   private final Map<String, AbstractCommand> subCommands = new LinkedHashMap<>();
   @Nonnull
   private final Map<String, String> subCommandsAliases = new LinkedHashMap<>();
   @Nonnull
   private final Int2ObjectMap<AbstractCommand> variantCommands = new Int2ObjectOpenHashMap();
   @Nullable
   private CommandOwner owner;
   @Nullable
   private String permission;
   @Nullable
   private List<String> permissionGroups;
   private int totalNumRequiredParameters;
   private final boolean requiresConfirmation;
   private boolean unavailableInSingleplayer;
   private boolean allowsExtraArguments;
   private boolean hasBeenRegistered;

   protected AbstractCommand(@Nullable String name, @Nullable String description, boolean requiresConfirmation) {
      this.name = name == null ? null : name.toLowerCase();
      this.description = description;
      this.requiresConfirmation = requiresConfirmation;
      if (requiresConfirmation) {
         this.registerOptionalArg(new FlagArg(this, "confirm", ""));
      }
   }

   protected AbstractCommand(@Nullable String name, @Nullable String description) {
      this(name, description, false);
   }

   protected AbstractCommand(@Nullable String description) {
      this(null, description);
   }

   public void setOwner(@Nonnull CommandOwner owner) {
      this.owner = owner;
      if (this.permission == null && this.canGeneratePermission()) {
         this.permission = this.generatePermission();
      }

      for (AbstractCommand subCommand : this.subCommands.values()) {
         subCommand.setOwner(owner);
      }

      ObjectIterator var4 = this.variantCommands.values().iterator();

      while (var4.hasNext()) {
         AbstractCommand variantCommand = (AbstractCommand)var4.next();
         variantCommand.setOwner(owner);
      }
   }

   protected boolean canGeneratePermission() {
      return true;
   }

   @Nullable
   protected String generatePermissionNode() {
      return this.name == null ? null : this.name.toLowerCase();
   }

   @Nonnull
   private String generatePermission() {
      String selfNode = this.generatePermissionNode();
      if (this.parentCommand != null) {
         String parentPermission = this.parentCommand.permission == null ? this.parentCommand.generatePermission() : this.parentCommand.permission;
         String generatedPermission;
         if (selfNode != null && !selfNode.isEmpty()) {
            generatedPermission = parentPermission + "." + selfNode;
         } else {
            generatedPermission = parentPermission;
         }

         ((HytaleLogger.Api)LOGGER.atFine()).log("Generated missing permission '" + generatedPermission + "'.");
         return generatedPermission;
      } else if (this.owner instanceof PluginBase plugin) {
         return plugin.getBasePermission() + ".command." + selfNode;
      } else if (this.owner instanceof CommandManager) {
         return "hytale.system.command." + selfNode;
      } else {
         throw new IllegalArgumentException("Unknown owner type, please use PluginBase or CommandManager");
      }
   }

   @Nullable
   public List<String> getPermissionGroups() {
      return this.permissionGroups;
   }

   protected void setPermissionGroups(@Nonnull String... groups) {
      this.permissionGroups = Arrays.asList(groups);
   }

   protected void setPermissionGroup(@Nullable GameMode gameMode) {
      this.setPermissionGroups(gameMode == null ? null : gameMode.toString());
   }

   @Nonnull
   public Map<String, Set<String>> getPermissionGroupsRecursive() {
      Map<String, Set<String>> permissionsByGroup = new Object2ObjectOpenHashMap();
      this.putRecursivePermissionGroups(permissionsByGroup);
      return permissionsByGroup;
   }

   public void putRecursivePermissionGroups(@Nonnull Map<String, Set<String>> permissionsByGroup) {
      List<String> permissionGroups = this.permissionGroups;
      if (permissionGroups == null && this.parentCommand != null) {
         permissionGroups = this.parentCommand.permissionGroups;
      }

      if (permissionGroups != null && this.permission != null) {
         for (String group : permissionGroups) {
            if (group != null) {
               permissionsByGroup.computeIfAbsent(group, k -> new HashSet<>()).add(this.permission);
            }
         }
      }

      for (AbstractCommand subCommand : this.subCommands.values()) {
         subCommand.putRecursivePermissionGroups(permissionsByGroup);
      }
   }

   protected void setUnavailableInSingleplayer(boolean unavailableInSingleplayer) {
      this.unavailableInSingleplayer = unavailableInSingleplayer;
   }

   public void setAllowsExtraArguments(boolean allowsExtraArguments) {
      this.allowsExtraArguments = allowsExtraArguments;
   }

   @Nonnull
   public MatchResult matches(@Nonnull String language, @Nonnull String search, int termDepth) {
      return this.matches(language, search, termDepth, 0);
   }

   @Nonnull
   private MatchResult matches(@Nonnull String language, @Nonnull String search, int termDepth, int depth) {
      if (this.name != null && this.name.contains(search)) {
         return MatchResult.of(termDepth, depth, 0, this.name, search);
      } else {
         for (String alias : this.aliases) {
            if (alias.contains(search)) {
               return MatchResult.of(termDepth, depth, 1, alias, search);
            }
         }

         for (AbstractOptionalArg<?, ?> opt : this.optionalArguments.values()) {
            if (opt.getName().contains(search)) {
               return MatchResult.of(termDepth, depth, 3, opt.getName(), search);
            }

            for (String aliasx : opt.getAliases()) {
               if (aliasx.contains(search)) {
                  return MatchResult.of(termDepth, depth, 3, aliasx, search);
               }
            }
         }

         for (RequiredArg<?> opt : this.requiredArguments) {
            if (opt.getName().contains(search)) {
               return MatchResult.of(termDepth, depth, 3, opt.getName(), search);
            }
         }

         if (this.description != null) {
            String descriptionMessage = I18nModule.get().getMessage(language, this.description);
            if (descriptionMessage != null && descriptionMessage.contains(search)) {
               return MatchResult.of(termDepth, depth, 4, descriptionMessage, search);
            }
         }

         for (AbstractOptionalArg<?, ?> optx : this.optionalArguments.values()) {
            String description = optx.getDescription();
            if (description != null) {
               String usageDescription = I18nModule.get().getMessage(language, description);
               if (usageDescription != null && usageDescription.contains(search)) {
                  return MatchResult.of(termDepth, depth, 5, usageDescription, search);
               }
            }
         }

         for (AbstractCommand subCommand : this.subCommands.values()) {
            MatchResult result = subCommand.matches(language, search, termDepth, depth + 1);
            if (result != MatchResult.NONE) {
               return result;
            }
         }

         ObjectIterator var14 = this.variantCommands.values().iterator();

         while (var14.hasNext()) {
            AbstractCommand variantCommand = (AbstractCommand)var14.next();
            MatchResult result = variantCommand.matches(language, search, termDepth, depth + 1);
            if (result != MatchResult.NONE) {
               return result;
            }
         }

         return MatchResult.NONE;
      }
   }

   public void completeRegistration() throws GeneralCommandException {
      this.hasBeenRegistered = true;

      for (AbstractCommand command : this.subCommands.values()) {
         command.completeRegistration();
      }

      ObjectIterator var3 = this.variantCommands.values().iterator();

      while (var3.hasNext()) {
         AbstractCommand command = (AbstractCommand)var3.next();
         command.completeRegistration();
      }

      this.validateVariantNumberOfRequiredParameters(new ParseResult(true));
      this.validateDefaultArguments(new ParseResult(true));
      this.createOptionalArgumentAbbreviationMap();
   }

   private void createOptionalArgumentAbbreviationMap() {
      AbbreviationMap.AbbreviationMapBuilder<AbstractOptionalArg<?, ?>> abbreviationMapBuilder = AbbreviationMap.create();

      for (AbstractOptionalArg<?, ?> abstractOptionalArg : this.optionalArguments.values()) {
         abbreviationMapBuilder.put(abstractOptionalArg.getName(), abstractOptionalArg);

         for (String alias : abstractOptionalArg.getAliases()) {
            abbreviationMapBuilder.put(alias, abstractOptionalArg);
         }
      }

      this.argumentAbbreviationMap = abbreviationMapBuilder.build();
   }

   private void validateVariantNumberOfRequiredParameters(@Nonnull ParseResult result) {
      ObjectIterator var2 = this.variantCommands.int2ObjectEntrySet().iterator();

      while (var2.hasNext()) {
         Entry<AbstractCommand> entry = (Entry<AbstractCommand>)var2.next();
         if (this.totalNumRequiredParameters == ((AbstractCommand)entry.getValue()).totalNumRequiredParameters) {
            result.fail(
               Message.raw(
                  "Command '"
                     + this.getFullyQualifiedName()
                     + "' and its variant '"
                     + ((AbstractCommand)entry.getValue()).toString()
                     + "' both have "
                     + this.totalNumRequiredParameters
                     + " required parameters. Variants must have different numbers of required parameters."
               )
            );
            return;
         }
      }
   }

   private void validateDefaultArguments(@Nonnull ParseResult parseResult) {
      for (AbstractOptionalArg<?, ?> value : this.optionalArguments.values()) {
         if (value instanceof DefaultArg<?> defaultArg) {
            defaultArg.validateDefaultValue(parseResult);
         }
      }
   }

   public void requirePermission(@Nonnull String permission) {
      this.permission = permission;
   }

   @Nullable
   public String getFullyQualifiedName() {
      if (this.parentCommand != null) {
         return this.isVariant() ? this.parentCommand.getFullyQualifiedName() : this.parentCommand.getFullyQualifiedName() + " " + this.name;
      } else {
         return this.name;
      }
   }

   public int countParents() {
      return this.parentCommand == null ? 0 : this.parentCommand.countParents() + 1;
   }

   public void addAliases(@Nonnull String... aliases) {
      if (this.hasBeenRegistered) {
         throw new IllegalStateException("Cannot add aliases when a command has already completed registration");
      } else if (this.name == null) {
         throw new IllegalStateException("Cannot add aliases to a command with no name");
      } else {
         for (String alias : aliases) {
            this.aliases.add(alias.toLowerCase());
         }
      }
   }

   public void addSubCommand(@Nonnull AbstractCommand command) {
      if (this.hasBeenRegistered) {
         throw new IllegalStateException("Cannot add new subcommands when a command has already completed registration");
      } else if (this.isVariant()) {
         throw new IllegalStateException("Cannot add a subcommand to a variant command, can only add subcommands to named commands");
      } else if (command.name == null) {
         throw new IllegalArgumentException("Cannot add a subcommand with no name");
      } else if (command.parentCommand != null) {
         throw new IllegalArgumentException("Cannot re-use subcommands. Only one parent command allowed for each subcommand");
      } else {
         command.parentCommand = this;
         if (this.subCommands.containsKey(command.name)) {
            throw new IllegalArgumentException("Cannot have multiple subcommands with the same name");
         } else if (this.subCommandsAliases.containsKey(command.name)) {
            throw new IllegalArgumentException("Command has same name as existing command alias for command: " + command.name);
         } else {
            this.subCommands.put(command.name, command);

            for (String alias : command.aliases) {
               if (this.subCommandsAliases.containsKey(alias) || this.subCommands.containsKey(alias)) {
                  throw new IllegalArgumentException("Cannot specify a subcommand alias with the same name as an existing command or alias: " + alias);
               }

               this.subCommandsAliases.put(alias, command.name);
            }

            command.hasBeenRegistered = true;
         }
      }
   }

   public void addUsageVariant(@Nonnull AbstractCommand command) {
      if (this.hasBeenRegistered) {
         throw new IllegalStateException("Cannot add new variants when a command has already completed registration");
      } else if (this.isVariant()) {
         throw new IllegalStateException("Cannot add a command variant to a variant command, can only add variants to named commands");
      } else if (command.name != null) {
         throw new IllegalArgumentException("Cannot add a variant command with a name, use the description-only constructor");
      } else if (command.parentCommand != null) {
         throw new IllegalArgumentException("Cannot re-use variant commands. Only one parent command allowed for each variant command");
      } else {
         AbstractCommand variantWithSameNumRequiredParameters = (AbstractCommand)this.variantCommands.put(command.totalNumRequiredParameters, command);
         if (variantWithSameNumRequiredParameters != null) {
            throw new IllegalArgumentException(
               "You have already registered a variant command with "
                  + command.totalNumRequiredParameters
                  + " required parameters. Command's class name: "
                  + variantWithSameNumRequiredParameters.getClass().getName()
            );
         } else {
            command.parentCommand = this;
            command.hasBeenRegistered = true;
         }
      }
   }

   @Nullable
   public CompletableFuture<Void> acceptCall(@Nonnull CommandSender sender, @Nonnull ParserContext parserContext, @Nonnull ParseResult parseResult) {
      parserContext.convertToSubCommand();
      return this.acceptCall0(sender, parserContext, parseResult);
   }

   @Nullable
   private CompletableFuture<Void> acceptCall0(@Nonnull CommandSender sender, @Nonnull ParserContext parserContext, @Nonnull ParseResult parseResult) {
      int numberOfPreOptionalTokens = parserContext.getNumPreOptionalTokens();
      ObjectBooleanPair<CompletableFuture<Void>> completableFutureBooleanPair = this.checkForExecutingSubcommands(
         sender, parserContext, parseResult, numberOfPreOptionalTokens
      );
      if (parseResult.failed()) {
         return null;
      } else if (completableFutureBooleanPair.rightBoolean()) {
         return (CompletableFuture<Void>)completableFutureBooleanPair.left();
      } else if (!this.hasPermission(sender)) {
         parseResult.fail(MESSAGE_COMMANDS_PARSING_ERROR_NO_PERMISSION_FOR_COMMAND);
         return null;
      } else if (this instanceof AbstractCommandCollection && numberOfPreOptionalTokens != 0) {
         HashSet<String> commandNames = new HashSet<>(this.subCommands.keySet());
         commandNames.addAll(this.subCommandsAliases.keySet());
         String firstToken = parserContext.getFirstToken();
         String commandSuggestionPrefix = "\n/" + this.getFullyQualifiedName() + " ";
         String suggestedCommands = commandSuggestionPrefix + String.join(commandSuggestionPrefix, StringUtil.sortByFuzzyDistance(firstToken, commandNames, 5));
         parseResult.fail(
            Message.translation("server.commands.parsing.error.commandCollectionSubcommandNotFound")
               .param("subcommand", firstToken)
               .param("suggestions", suggestedCommands)
         );
         return null;
      } else if (parserContext.isHelpSpecified()) {
         sender.sendMessage(this.getUsageString(sender));
         return null;
      } else if (this.unavailableInSingleplayer && Constants.SINGLEPLAYER) {
         parseResult.fail(MESSAGE_COMMAND_SINGLEPLAYER);
         return null;
      } else if (this.requiresConfirmation && !parserContext.isConfirmationSpecified()) {
         parseResult.fail(MESSAGE_COMMANDS_PARSING_ERROR_ATTEMPTED_UNSAFE);
         return null;
      } else {
         if (this.allowsExtraArguments) {
            if (numberOfPreOptionalTokens < this.totalNumRequiredParameters) {
               parseResult.fail(
                  Message.translation("server.commands.parsing.error.wrongNumberRequiredParameters")
                     .param("expected", this.totalNumRequiredParameters)
                     .param("actual", numberOfPreOptionalTokens)
                     .insert("\n")
                     .insert(Message.translation("server.commands.help.usagecolon").param("usage", this.getUsageShort(sender, true)))
                     .insert("\n")
                     .insert(Message.translation("server.commands.help.useHelpToLearnMore").param("command", this.getFullyQualifiedName()))
               );
               return null;
            }
         } else if (this.totalNumRequiredParameters != numberOfPreOptionalTokens) {
            parseResult.fail(
               Message.translation("server.commands.parsing.error.wrongNumberRequiredParameters")
                  .param("expected", this.totalNumRequiredParameters)
                  .param("actual", numberOfPreOptionalTokens)
                  .insert("\n")
                  .insert(Message.translation("server.commands.help.usagecolon").param("usage", this.getUsageShort(sender, true)))
                  .insert("\n")
                  .insert(Message.translation("server.commands.help.useHelpToLearnMore").param("command", this.getFullyQualifiedName()))
            );
            return null;
         }

         CommandContext commandContext = new CommandContext(this, sender, parserContext.getInputString());
         this.processRequiredArguments(parserContext, parseResult, commandContext);
         if (parseResult.failed()) {
            return null;
         } else {
            this.processOptionalArguments(parserContext, parseResult, commandContext);
            return parseResult.failed() ? null : this.execute(commandContext);
         }
      }
   }

   public boolean hasPermission(@Nonnull CommandSender sender) {
      String permission = this.getPermission();
      if (permission == null) {
         return true;
      } else if (sender.hasPermission(permission)) {
         return this.parentCommand == null ? true : this.parentCommand.hasPermission(sender);
      } else {
         return false;
      }
   }

   @Nonnull
   private ObjectBooleanPair<CompletableFuture<Void>> checkForExecutingSubcommands(
      @Nonnull CommandSender sender, @Nonnull ParserContext parserContext, @Nonnull ParseResult parseResult, int numberOfPreOptionalTokens
   ) {
      if (parserContext.getNumPreOptSingleValueTokensBeforeListTokens() >= 0) {
         if (!this.subCommands.isEmpty()) {
            String subCommandName = parserContext.getPreOptionalSingleValueToken(0);
            if (subCommandName != null) {
               subCommandName = subCommandName.toLowerCase();
            }

            AbstractCommand subCommand = this.subCommands.get(subCommandName);
            if (subCommand != null) {
               parserContext.convertToSubCommand();
               return ObjectBooleanPair.of(subCommand.acceptCall0(sender, parserContext, parseResult), true);
            }

            String alias = this.subCommandsAliases.get(subCommandName);
            if (alias != null) {
               parserContext.convertToSubCommand();
               return ObjectBooleanPair.of(this.subCommands.get(alias).acceptCall0(sender, parserContext, parseResult), true);
            }
         }

         AbstractCommand commandVariant = (AbstractCommand)this.variantCommands.get(numberOfPreOptionalTokens);
         if (this.totalNumRequiredParameters != numberOfPreOptionalTokens && commandVariant != null) {
            return ObjectBooleanPair.of(commandVariant.acceptCall0(sender, parserContext, parseResult), true);
         }
      }

      return ObjectBooleanPair.of(null, false);
   }

   private void processRequiredArguments(@Nonnull ParserContext parserContext, @Nonnull ParseResult parseResult, @Nonnull CommandContext commandContext) {
      int currentReqArgIndex = 0;

      for (RequiredArg<?> requiredArgument : this.requiredArguments) {
         if (requiredArgument.getArgumentType().isListArgument() && parserContext.isListToken(currentReqArgIndex)) {
            ParserContext.PreOptionalListContext preOptionalTokenContext = parserContext.getPreOptionalListToken(currentReqArgIndex);
            currentReqArgIndex++;
            commandContext.appendArgumentData(requiredArgument, preOptionalTokenContext.getTokens(), true, parseResult);
         } else {
            String[] argParameters = new String[requiredArgument.getArgumentType().getNumberOfParameters()];

            for (int i = 0; i < requiredArgument.getArgumentType().getNumberOfParameters(); i++) {
               if (parserContext.isListToken(currentReqArgIndex)) {
                  parseResult.fail(Message.translation("server.commands.parsing.error.notAList").param("name", requiredArgument.getName()));
                  return;
               }

               argParameters[i] = parserContext.getPreOptionalSingleValueToken(currentReqArgIndex);
               currentReqArgIndex++;
            }

            commandContext.appendArgumentData(requiredArgument, argParameters, false, parseResult);
            if (parseResult.failed()) {
               return;
            }
         }
      }
   }

   private void processOptionalArguments(@Nonnull ParserContext parserContext, @Nonnull ParseResult parseResult, @Nonnull CommandContext commandContext) {
      ObjectBidirectionalIterator var4 = parserContext.getOptionalArgs().iterator();

      while (var4.hasNext()) {
         java.util.Map.Entry<String, List<List<String>>> optionalArgContext = (java.util.Map.Entry<String, List<List<String>>>)var4.next();
         AbstractOptionalArg<? extends Argument<?, ?>, ?> optionalArg = (AbstractOptionalArg<? extends Argument<?, ?>, ?>)this.argumentAbbreviationMap
            .get(optionalArgContext.getKey());
         if (optionalArg == null) {
            parseResult.fail(Message.translation("server.commands.parsing.error.couldNotFindOptionalArgName").param("input", optionalArgContext.getKey()));
            return;
         }

         if (optionalArg.getPermission() != null && !commandContext.sender().hasPermission(optionalArg.getPermission())) {
            parseResult.fail(Message.translation("server.commands.parsing.error.noPermissionForOptional").param("argument", optionalArgContext.getKey()));
            return;
         }

         List<List<String>> optionalArgValues = optionalArgContext.getValue();
         if (optionalArg.getArgumentType().isListArgument() && optionalArgValues.size() > 1) {
            String[] optionalArgParseValues = new String[optionalArgValues.size() * optionalArgValues.getFirst().size()];

            for (int i = 0; i < optionalArgValues.size(); i++) {
               List<String> values = optionalArgValues.get(i);

               for (int k = 0; k < values.size(); k++) {
                  optionalArgParseValues[i * values.size() + k] = values.get(k);
               }
            }

            commandContext.appendArgumentData(optionalArg, optionalArgParseValues, true, parseResult);
         } else {
            commandContext.appendArgumentData(
               optionalArg, optionalArgValues.isEmpty() ? EMPTY_STRING_ARRAY : optionalArgValues.getFirst().toArray(EMPTY_STRING_ARRAY), false, parseResult
            );
         }
      }

      for (AbstractOptionalArg<?, ?> optionalArgx : this.optionalArguments.values()) {
         optionalArgx.verifyArgumentDependencies(commandContext, parseResult);
      }
   }

   @Nullable
   protected abstract CompletableFuture<Void> execute(@Nonnull CommandContext var1);

   @Nonnull
   public Message getUsageString(@Nonnull CommandSender sender) {
      Message requiredArgsMessage = Message.raw("");

      for (RequiredArg<?> requiredArgument : this.requiredArguments) {
         requiredArgsMessage.insert(" ").insert(requiredArgument.getUsageMessageWithoutDescription());
         if (requiredArgument.getArgumentType().isListArgument()) {
            requiredArgsMessage.insert("/...");
         }
      }

      Message requiresConfirmationMessage = this.requiresConfirmation ? MESSAGE_COMMANDS_PARSING_USAGE_REQUIRES_CONFIRMATION : Message.raw("");
      Message requiredArgs = Message.raw("");
      boolean requiredArgsShown = false;

      for (RequiredArg<?> requiredArgumentx : this.requiredArguments) {
         requiredArgsShown = true;
         requiredArgs.insert("\n    ").insert(requiredArgumentx.getUsageMessage());
      }

      Message optionalArgs = Message.raw("");
      boolean optionalArgsShown = false;
      Message defaultArgs = Message.raw("");
      boolean defaultArgsShown = false;
      Message flagArgs = Message.raw("");
      boolean flagArgsShown = false;

      for (java.util.Map.Entry<String, AbstractOptionalArg<?, ?>> entry : this.optionalArguments.entrySet()) {
         AbstractOptionalArg<? extends Argument<?, ?>, ?> arg = (AbstractOptionalArg<? extends Argument<?, ?>, ?>)entry.getValue();
         if (arg.getPermission() == null || sender.hasPermission(arg.getPermission())) {
            switch (arg) {
               case OptionalArg ignored:
                  optionalArgsShown = true;
                  optionalArgs.insert("\n    ").insert(arg.getUsageMessage());
                  break;
               case DefaultArg ignoredx:
                  defaultArgsShown = true;
                  defaultArgs.insert("\n    ").insert(arg.getUsageMessage());
                  break;
               case FlagArg ignoredxx:
                  flagArgsShown = true;
                  flagArgs.insert("\n    ").insert(arg.getUsageMessage());
                  break;
               default:
            }
         }
      }

      if (requiredArgsShown) {
         requiredArgs = Message.translation("server.commands.parsing.usage.requiredArgs").param("args", requiredArgs);
      }

      if (optionalArgsShown) {
         optionalArgs = Message.translation("server.commands.parsing.usage.optionalArgs").param("args", optionalArgs);
      }

      if (flagArgsShown) {
         flagArgs = Message.translation("server.commands.parsing.usage.flagArgs").param("args", flagArgs);
      }

      if (defaultArgsShown) {
         defaultArgs = Message.translation("server.commands.parsing.usage.defaultArgs").param("args", defaultArgs);
      }

      Message variantsMessage = Message.raw("");
      ObjectIterator var25 = this.variantCommands.values().iterator();

      while (var25.hasNext()) {
         AbstractCommand value = (AbstractCommand)var25.next();
         if (value.hasPermission(sender)) {
            variantsMessage.insert(
               Message.translation("server.commands.parsing.usage.subcommands")
                  .param("separator", Message.translation("server.commands.parsing.usage.subcommandSeparator"))
                  .param("usage", value.getUsageString(sender))
            );
         }
      }

      Message subcommandsMessage = Message.raw("");

      for (AbstractCommand value : this.subCommands.values()) {
         if (value.hasPermission(sender)) {
            subcommandsMessage.insert(
               Message.translation("server.commands.parsing.usage.subcommands")
                  .param("separator", Message.translation("server.commands.parsing.usage.subcommandSeparator"))
                  .param("usage", value.getUsageString(sender))
            );
         }
      }

      Message argTypesMessage = Message.raw("\nArgument Types:");
      HashSet<ArgumentType<?>> allArgumentTypes = new HashSet<>();

      for (RequiredArg<?> requiredArgumentx : this.requiredArguments) {
         allArgumentTypes.add(requiredArgumentx.getArgumentType());
      }

      for (AbstractOptionalArg<?, ?> optionalArgument : this.optionalArguments.values()) {
         allArgumentTypes.add(optionalArgument.getArgumentType());
      }

      for (ArgumentType<?> argumentType : allArgumentTypes) {
         argTypesMessage.insert("\n    ")
            .insert(argumentType.getName())
            .insert(": ")
            .insert(argumentType.getArgumentUsage())
            .insert("\n        Examples: ")
            .insert("'")
            .insert(String.join("', '", argumentType.getExamples()))
            .insert("'.");
      }

      return Message.translation("server.commands.parsing.usage.header")
         .param("fullyQualifiedName", this.getFullyQualifiedName())
         .param("description", this.description != null ? Message.translation(this.description) : Message.empty())
         .param("listOfRequiredArgs", requiredArgsMessage)
         .param("requiresConfirmation", requiresConfirmationMessage)
         .param("requiredArgs", requiredArgs)
         .param("optionalArgs", optionalArgs)
         .param("defaultArgs", defaultArgs)
         .param("flagArgs", flagArgs)
         .param("argTypes", argTypesMessage)
         .param("variants", variantsMessage)
         .param("subcommands", subcommandsMessage);
   }

   @Nonnull
   public Message getUsageShort(@Nonnull CommandSender sender, boolean fullyQualify) {
      String indent = " ".repeat(this.countParents());
      if (this.subCommands.isEmpty() && this.variantCommands.isEmpty()) {
         String fullyQualifiedName = this.getFullyQualifiedName();
         Message message = Message.raw(indent)
            .insert(fullyQualify ? (fullyQualifiedName != null ? fullyQualifiedName : "???") : (this.name != null ? this.name : ""));

         for (RequiredArg<?> requiredArgument : this.requiredArguments) {
            message.insert(" ").insert(requiredArgument.getUsageOneLiner().color("#C1E0FF"));
         }

         for (AbstractOptionalArg<?, ?> optionalArgument : this.optionalArguments.values()) {
            if (optionalArgument.hasPermission(sender)) {
               message.insert(" ").insert(optionalArgument.getUsageOneLiner().color("#7E9EBC"));
            }
         }

         return message;
      } else {
         String prefix = this.parentCommand == null ? "/" : indent;
         Message message = Message.raw(prefix).insert(this.name);
         boolean anyPermissible = false;
         ObjectIterator optionalArgumentx = this.variantCommands.values().iterator();

         while (optionalArgumentx.hasNext()) {
            AbstractCommand variantCommand = (AbstractCommand)optionalArgumentx.next();
            if (variantCommand.hasPermission(sender)) {
               message.insert("\n ").insert(indent).insert(variantCommand.getUsageShort(sender, fullyQualify));
               anyPermissible = true;
            }
         }

         for (AbstractCommand subCommand : this.subCommands.values()) {
            if (subCommand.hasPermission(sender)) {
               message.insert("\n ").insert(indent).insert(subCommand.getUsageShort(sender, fullyQualify));
               anyPermissible = true;
            }
         }

         if (!anyPermissible) {
            message.insert("\n ").insert(MESSAGE_COMMANDS_HELP_NO_PERMISSIBLE_SUB_COMMAND);
         }

         return message;
      }
   }

   @Nonnull
   private <R extends RequiredArg<D>, D> R registerRequiredArg(@Nonnull R requiredArgument) {
      if (this.hasBeenRegistered) {
         throw new IllegalStateException("Cannot add new arguments when a command has already completed registration");
      } else if (requiredArgument.getCommandRegisteredTo().equals(this) && !this.requiredArguments.contains(requiredArgument)) {
         this.totalNumRequiredParameters = this.totalNumRequiredParameters + requiredArgument.getArgumentType().getNumberOfParameters();
         this.requiredArguments.add(requiredArgument);
         return requiredArgument;
      } else {
         throw new IllegalArgumentException("Cannot re-use arguments");
      }
   }

   @Nonnull
   private <R extends AbstractOptionalArg<?, D>, D> R registerOptionalArg(@Nonnull R optionalArgument) {
      if (this.hasBeenRegistered) {
         throw new IllegalStateException("Cannot add new arguments when a command has already completed registration");
      } else if (optionalArgument.getCommandRegisteredTo().equals(this) && !this.optionalArguments.containsKey(optionalArgument.getName().toLowerCase())) {
         this.optionalArguments.put(optionalArgument.getName().toLowerCase(), optionalArgument);
         return optionalArgument;
      } else {
         throw new IllegalArgumentException("Cannot re-use arguments");
      }
   }

   @Nonnull
   public <D> RequiredArg<D> withRequiredArg(@Nonnull String name, @Nonnull String description, @Nonnull ArgumentType<D> argType) {
      return this.registerRequiredArg(new RequiredArg<>(this, name, description, argType));
   }

   public <W extends WrappedArg<D>, D> W withRequiredArg(@Nonnull String name, @Nonnull String description, @Nonnull ArgWrapper<W, D> wrapper) {
      return wrapper.wrapArg(this.registerRequiredArg(new RequiredArg<>(this, name, description, wrapper.argumentType())));
   }

   @Nonnull
   public <D> RequiredArg<List<D>> withListRequiredArg(@Nonnull String name, @Nonnull String description, @Nonnull ArgumentType<D> argType) {
      return this.registerRequiredArg(new RequiredArg<>(this, name, description, new ListArgumentType<>(argType)));
   }

   @Nonnull
   public <D> DefaultArg<D> withDefaultArg(String name, String description, ArgumentType<D> argType, @Nullable D defaultValue, String defaultValueDescription) {
      return this.registerOptionalArg(new DefaultArg<>(this, name, description, argType, defaultValue, defaultValueDescription));
   }

   public <W extends WrappedArg<D>, D> W withDefaultArg(
      @Nonnull String name, @Nonnull String description, @Nonnull ArgWrapper<W, D> wrapper, D defaultValue, @Nonnull String defaultValueDescription
   ) {
      return wrapper.wrapArg(this.registerOptionalArg(new DefaultArg<>(this, name, description, wrapper.argumentType(), defaultValue, defaultValueDescription)));
   }

   @Nonnull
   public <D> DefaultArg<List<D>> withListDefaultArg(
      @Nonnull String name, @Nonnull String description, @Nonnull ArgumentType<D> argType, List<D> defaultValue, @Nonnull String defaultValueDescription
   ) {
      return this.registerOptionalArg(new DefaultArg<>(this, name, description, new ListArgumentType<>(argType), defaultValue, defaultValueDescription));
   }

   @Nonnull
   public <D> OptionalArg<D> withOptionalArg(@Nonnull String name, @Nonnull String description, @Nonnull ArgumentType<D> argType) {
      return this.registerOptionalArg(new OptionalArg<>(this, name, description, argType));
   }

   public <W extends WrappedArg<D>, D> W withOptionalArg(@Nonnull String name, @Nonnull String description, @Nonnull ArgWrapper<W, D> wrapper) {
      return wrapper.wrapArg(this.registerOptionalArg(new OptionalArg<>(this, name, description, wrapper.argumentType())));
   }

   @Nonnull
   public <D> OptionalArg<List<D>> withListOptionalArg(@Nonnull String name, @Nonnull String description, @Nonnull ArgumentType<D> argType) {
      return this.registerOptionalArg(new OptionalArg<>(this, name, description, new ListArgumentType<>(argType)));
   }

   @Nonnull
   public FlagArg withFlagArg(@Nonnull String name, @Nonnull String description) {
      return this.registerOptionalArg(new FlagArg(this, name, description));
   }

   public boolean isVariant() {
      return this.name == null;
   }

   @Nullable
   public String getName() {
      return this.name;
   }

   @Nonnull
   public Set<String> getAliases() {
      return this.aliases;
   }

   @Nullable
   public String getDescription() {
      return this.description;
   }

   @Nullable
   public CommandOwner getOwner() {
      return this.owner;
   }

   @Nullable
   public String getPermission() {
      return this.permission;
   }

   @Nonnull
   public Map<String, AbstractCommand> getSubCommands() {
      return this.subCommands;
   }

   @Nonnull
   public List<RequiredArg<?>> getRequiredArguments() {
      return this.requiredArguments;
   }

   public boolean hasBeenRegistered() {
      return this.hasBeenRegistered;
   }
}
