package com.hypixel.hytale.server.core.command.system.pages;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.OpenChatWithCommand;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.command.system.MatchResult;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgumentType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CommandListPage extends InteractiveCustomUIPage<CommandListPage.CommandListPageEventData> {
   private static final Value<String> BUTTON_LABEL_STYLE = Value.ref("Pages/BasicTextButton.ui", "LabelStyle");
   private static final Value<String> BUTTON_LABEL_STYLE_SELECTED = Value.ref("Pages/BasicTextButton.ui", "SelectedLabelStyle");
   private final List<String> visibleCommands = new ObjectArrayList();
   @Nonnull
   private String searchQuery = "";
   private String selectedCommand;
   @Nullable
   private String selectedSubcommand;
   @Nullable
   private Integer selectedVariantIndex;
   private final List<String> subcommandBreadcrumb = new ObjectArrayList();
   @Nullable
   private final String initialCommand;

   public CommandListPage(@Nonnull PlayerRef playerRef) {
      this(playerRef, null);
   }

   public CommandListPage(@Nonnull PlayerRef playerRef, @Nullable String initialCommand) {
      super(playerRef, CustomPageLifetime.CanDismiss, CommandListPage.CommandListPageEventData.CODEC);
      this.initialCommand = initialCommand;
   }

   @Override
   public void build(
      @Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store
   ) {
      commandBuilder.append("Pages/CommandListPage.ui");
      eventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#SearchInput", EventData.of("@SearchQuery", "#SearchInput.Value"), false);
      eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#BackButton", EventData.of("NavigateUp", "true"));
      eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#SendToChatButton", EventData.of("SendToChat", "true"));
      this.buildCommandList(ref, commandBuilder, eventBuilder, store);
      String commandToSelect = this.visibleCommands.getFirst();
      if (this.initialCommand != null && this.visibleCommands.contains(this.initialCommand)) {
         commandToSelect = this.initialCommand;
      }

      this.selectCommand(ref, commandToSelect, commandBuilder, eventBuilder, store);
   }

   public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull CommandListPage.CommandListPageEventData data) {
      if (data.searchQuery != null) {
         this.searchQuery = data.searchQuery.trim().toLowerCase();
         UICommandBuilder commandBuilder = new UICommandBuilder();
         UIEventBuilder eventBuilder = new UIEventBuilder();
         this.buildCommandList(ref, commandBuilder, eventBuilder, store);
         this.sendUpdate(commandBuilder, eventBuilder, false);
      } else if (data.command != null) {
         UICommandBuilder commandBuilder = new UICommandBuilder();
         UIEventBuilder eventBuilder = new UIEventBuilder();
         this.selectCommand(ref, data.command, commandBuilder, eventBuilder, store);
         this.sendUpdate(commandBuilder, eventBuilder, false);
      } else if (data.navigateUp != null) {
         UICommandBuilder commandBuilder = new UICommandBuilder();
         UIEventBuilder eventBuilder = new UIEventBuilder();
         this.navigateUp(ref, commandBuilder, eventBuilder, store);
         this.sendUpdate(commandBuilder, eventBuilder, false);
      } else if (data.subcommand != null) {
         UICommandBuilder commandBuilder = new UICommandBuilder();
         UIEventBuilder eventBuilder = new UIEventBuilder();
         this.selectSubcommand(ref, data.subcommand, commandBuilder, eventBuilder, store);
         this.sendUpdate(commandBuilder, eventBuilder, false);
      } else if (data.variantIndex != null) {
         UICommandBuilder commandBuilder = new UICommandBuilder();
         UIEventBuilder eventBuilder = new UIEventBuilder();

         try {
            int variantIdx = Integer.parseInt(data.variantIndex);
            this.selectVariant(ref, variantIdx, commandBuilder, eventBuilder, store);
            this.sendUpdate(commandBuilder, eventBuilder, false);
         } catch (NumberFormatException var7) {
         }
      } else if (data.sendToChat != null) {
         this.handleSendToChat(ref, store);
      }
   }

   private void handleSendToChat(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());
      if (playerComponent != null) {
         String command = this.buildCurrentCommandString();
         playerComponent.getPageManager().setPage(ref, store, Page.None);
         this.playerRef.getPacketHandler().write(new OpenChatWithCommand(command));
      }
   }

   @Nonnull
   private String buildCurrentCommandString() {
      StringBuilder sb = new StringBuilder("/");
      sb.append(this.selectedCommand);

      for (String part : this.subcommandBreadcrumb) {
         sb.append(" ").append(part);
      }

      AbstractCommand currentContext = CommandManager.get().getCommandRegistration().get(this.selectedCommand);
      if (currentContext != null) {
         for (String part : this.subcommandBreadcrumb) {
            Map<String, AbstractCommand> subcommands = currentContext.getSubCommands();
            currentContext = subcommands.get(part);
            if (currentContext == null) {
               break;
            }
         }

         if (currentContext != null) {
            for (RequiredArg<?> arg : currentContext.getRequiredArguments()) {
               sb.append(" <").append(arg.getName()).append(">");
            }
         }
      }

      return sb.toString();
   }

   private void buildCommandList(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull UICommandBuilder commandBuilder,
      @Nonnull UIEventBuilder eventBuilder,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      commandBuilder.clear("#CommandList");
      Map<String, AbstractCommand> commands = new Object2ObjectOpenHashMap(CommandManager.get().getCommandRegistration());
      Player playerComponent = componentAccessor.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      commands.values().removeIf(commandx -> !commandx.hasPermission(playerComponent));
      if (this.searchQuery.isEmpty()) {
         this.visibleCommands.clear();
         this.visibleCommands.addAll(commands.keySet());
         Collections.sort(this.visibleCommands);
      } else {
         ObjectArrayList<CommandListPage.SearchResult> results = new ObjectArrayList();

         for (Entry<String, AbstractCommand> entry : commands.entrySet()) {
            if (entry.getValue() != null) {
               results.add(new CommandListPage.SearchResult(entry.getKey(), MatchResult.EXACT));
            }
         }

         String[] terms = this.searchQuery.split(" ");

         for (int termIndex = 0; termIndex < terms.length; termIndex++) {
            String term = terms[termIndex];

            for (int cmdIndex = results.size() - 1; cmdIndex >= 0; cmdIndex--) {
               CommandListPage.SearchResult result = (CommandListPage.SearchResult)results.get(cmdIndex);
               AbstractCommand command = commands.get(result.name);
               MatchResult match;
               if (command != null) {
                  match = command.matches(this.playerRef.getLanguage(), term, termIndex);
               } else {
                  match = MatchResult.NONE;
               }

               if (match == MatchResult.NONE) {
                  results.remove(cmdIndex);
               } else {
                  result.match = result.match.min(match);
               }
            }
         }

         results.sort(CommandListPage.SearchResult.COMPARATOR);
         this.visibleCommands.clear();

         for (int i = 0; i < results.size(); i++) {
            this.visibleCommands.add(((CommandListPage.SearchResult)results.get(i)).name);
         }
      }

      for (int i = 0; i < this.visibleCommands.size(); i++) {
         String name = this.visibleCommands.get(i);
         commandBuilder.append("#CommandList", "Pages/BasicTextButton.ui");
         commandBuilder.set("#CommandList[" + i + "].TextSpans", Message.raw(name));
         eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CommandList[" + i + "]", EventData.of("Command", name));
         if (name.equals(this.selectedCommand)) {
            commandBuilder.set("#CommandList[" + i + "].Style", BUTTON_LABEL_STYLE_SELECTED);
         }
      }
   }

   private void selectCommand(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull String commandName,
      @Nonnull UICommandBuilder commandBuilder,
      @Nonnull UIEventBuilder eventBuilder,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      AbstractCommand command = CommandManager.get().getCommandRegistration().get(commandName);
      if (command == null) {
         throw new IllegalArgumentException("Unknown command: " + commandName);
      } else {
         commandBuilder.set("#CommandName.TextSpans", Message.raw(commandName));
         String description = command.getDescription();
         Player playerComponent = componentAccessor.getComponent(ref, Player.getComponentType());
         commandBuilder.set("#CommandDescription.TextSpans", description != null ? Message.translation(description) : Message.empty());
         this.selectedSubcommand = null;
         this.selectedVariantIndex = null;
         this.subcommandBreadcrumb.clear();
         this.buildSubcommandTabs(command, playerComponent, commandBuilder, eventBuilder);
         this.displayCommandInfo(command, playerComponent, commandBuilder, eventBuilder);
         if (this.selectedCommand != null && this.visibleCommands.contains(this.selectedCommand)) {
            commandBuilder.set("#CommandList[" + this.visibleCommands.indexOf(this.selectedCommand) + "].Style", BUTTON_LABEL_STYLE);
         }

         commandBuilder.set("#CommandList[" + this.visibleCommands.indexOf(commandName) + "].Style", BUTTON_LABEL_STYLE_SELECTED);
         this.selectedCommand = commandName;
      }
   }

   private void selectSubcommand(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull String subcommandName,
      @Nonnull UICommandBuilder commandBuilder,
      @Nonnull UIEventBuilder eventBuilder,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      AbstractCommand currentContext = CommandManager.get().getCommandRegistration().get(this.selectedCommand);
      if (currentContext != null) {
         for (String breadcrumbPart : this.subcommandBreadcrumb) {
            Map<String, AbstractCommand> subcommands = currentContext.getSubCommands();
            currentContext = subcommands.get(breadcrumbPart);
            if (currentContext == null) {
               return;
            }
         }

         Player playerComponent = componentAccessor.getComponent(ref, Player.getComponentType());
         if (playerComponent != null) {
            Map<String, AbstractCommand> subcommands = currentContext.getSubCommands();
            AbstractCommand subcommand = subcommands.get(subcommandName);
            if (subcommand != null) {
               this.subcommandBreadcrumb.add(subcommandName);
               this.selectedSubcommand = subcommandName;
               this.selectedVariantIndex = null;
               this.updateTitleWithBreadcrumb(commandBuilder);
               String description = subcommand.getDescription();
               commandBuilder.set("#CommandDescription.TextSpans", description != null ? Message.translation(description) : Message.empty());
               this.buildSubcommandTabs(subcommand, playerComponent, commandBuilder, eventBuilder);
               commandBuilder.set("#BackButton.Visible", true);
               this.displayCommandInfo(subcommand, playerComponent, commandBuilder, eventBuilder);
            }
         }
      }
   }

   private void selectVariant(
      @Nonnull Ref<EntityStore> ref,
      int variantIndex,
      @Nonnull UICommandBuilder commandBuilder,
      @Nonnull UIEventBuilder eventBuilder,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      AbstractCommand currentContext = CommandManager.get().getCommandRegistration().get(this.selectedCommand);
      if (currentContext != null) {
         for (String breadcrumbPart : this.subcommandBreadcrumb) {
            Map<String, AbstractCommand> subcommands = currentContext.getSubCommands();
            currentContext = subcommands.get(breadcrumbPart);
            if (currentContext == null) {
               return;
            }
         }

         Player playerComponent = componentAccessor.getComponent(ref, Player.getComponentType());

         try {
            Field variantsField = AbstractCommand.class.getDeclaredField("variantCommands");
            variantsField.setAccessible(true);
            Int2ObjectMap<AbstractCommand> variants = (Int2ObjectMap<AbstractCommand>)variantsField.get(currentContext);
            AbstractCommand variant = (AbstractCommand)variants.get(variantIndex);
            if (variant == null || !variant.hasPermission(playerComponent)) {
               return;
            }

            this.selectedVariantIndex = variantIndex;
            this.updateTitleWithVariantSuffix(commandBuilder);
            commandBuilder.set("#VariantsSection.Visible", false);
            commandBuilder.set("#BackButton.Visible", true);
            commandBuilder.set("#CommandUsageLabel.TextSpans", this.getSimplifiedUsage(variant, playerComponent));
            this.buildParametersSection(variant, playerComponent, commandBuilder);
            this.buildArgumentTypesSection(variant, playerComponent, commandBuilder);
         } catch (Exception var11) {
         }
      }
   }

   private void navigateUp(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull UICommandBuilder commandBuilder,
      @Nonnull UIEventBuilder eventBuilder,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (this.selectedVariantIndex != null) {
         this.selectedVariantIndex = null;
         AbstractCommand currentContext = CommandManager.get().getCommandRegistration().get(this.selectedCommand);
         if (currentContext != null) {
            for (String breadcrumbPart : this.subcommandBreadcrumb) {
               Map<String, AbstractCommand> subcommands = currentContext.getSubCommands();
               currentContext = subcommands.get(breadcrumbPart);
               if (currentContext == null) {
                  return;
               }
            }

            Player playerComponent = componentAccessor.getComponent(ref, Player.getComponentType());
            this.updateTitleWithBreadcrumb(commandBuilder);
            this.displayCommandInfo(currentContext, playerComponent, commandBuilder, eventBuilder);
            commandBuilder.set("#BackButton.Visible", !this.subcommandBreadcrumb.isEmpty());
         }
      } else if (!this.subcommandBreadcrumb.isEmpty()) {
         this.subcommandBreadcrumb.remove(this.subcommandBreadcrumb.size() - 1);
         AbstractCommand currentContext = CommandManager.get().getCommandRegistration().get(this.selectedCommand);
         if (currentContext != null) {
            for (String breadcrumbPartx : this.subcommandBreadcrumb) {
               Map<String, AbstractCommand> subcommands = currentContext.getSubCommands();
               currentContext = subcommands.get(breadcrumbPartx);
               if (currentContext == null) {
                  return;
               }
            }

            Player playerComponent = componentAccessor.getComponent(ref, Player.getComponentType());
            this.selectedSubcommand = this.subcommandBreadcrumb.isEmpty() ? null : this.subcommandBreadcrumb.get(this.subcommandBreadcrumb.size() - 1);
            this.updateTitleWithBreadcrumb(commandBuilder);
            this.buildSubcommandTabs(currentContext, playerComponent, commandBuilder, eventBuilder);
            this.displayCommandInfo(currentContext, playerComponent, commandBuilder, eventBuilder);
         }
      }
   }

   private void buildSubcommandTabs(
      @Nonnull AbstractCommand command, @Nonnull Player playerComponent, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder
   ) {
      commandBuilder.clear("#SubcommandCards");
      Map<String, AbstractCommand> subcommands = command.getSubCommands();
      if (subcommands.isEmpty()) {
         commandBuilder.set("#SubcommandSection.Visible", false);
      } else {
         commandBuilder.set("#SubcommandSection.Visible", true);
         int cardIndex = 0;
         int rowIndex = 0;
         int cardsInCurrentRow = 0;

         for (Entry<String, AbstractCommand> entry : subcommands.entrySet()) {
            AbstractCommand subcommand = entry.getValue();
            if (subcommand.hasPermission(playerComponent)) {
               if (cardsInCurrentRow == 0) {
                  commandBuilder.appendInline("#SubcommandCards", "Group { LayoutMode: Left; Anchor: (Bottom: 0); }");
               }

               commandBuilder.append("#SubcommandCards[" + rowIndex + "]", "Pages/SubcommandCard.ui");
               String subCommandDescription = subcommand.getDescription();
               commandBuilder.set("#SubcommandCards[" + rowIndex + "][" + cardsInCurrentRow + "] #SubcommandName.TextSpans", Message.raw(entry.getKey()));
               commandBuilder.set(
                  "#SubcommandCards[" + rowIndex + "][" + cardsInCurrentRow + "] #SubcommandUsage.TextSpans",
                  this.getSimplifiedUsage(subcommand, playerComponent)
               );
               commandBuilder.set(
                  "#SubcommandCards[" + rowIndex + "][" + cardsInCurrentRow + "] #SubcommandDescription.TextSpans",
                  subCommandDescription != null ? Message.translation(subCommandDescription) : Message.empty()
               );
               eventBuilder.addEventBinding(
                  CustomUIEventBindingType.Activating,
                  "#SubcommandCards[" + rowIndex + "][" + cardsInCurrentRow + "]",
                  EventData.of("Subcommand", entry.getKey())
               );
               cardsInCurrentRow++;
               cardIndex++;
               if (cardsInCurrentRow >= 3) {
                  cardsInCurrentRow = 0;
                  rowIndex++;
               }
            }
         }
      }

      commandBuilder.set("#BackButton.Visible", !this.subcommandBreadcrumb.isEmpty());
   }

   private void updateTitleWithBreadcrumb(@Nonnull UICommandBuilder commandBuilder) {
      StringBuilder titleText = new StringBuilder(this.selectedCommand);

      for (String part : this.subcommandBreadcrumb) {
         titleText.append(" > ").append(part);
      }

      commandBuilder.set("#CommandName.TextSpans", Message.raw(titleText.toString()));
   }

   private void updateTitleWithVariantSuffix(@Nonnull UICommandBuilder commandBuilder) {
      StringBuilder titleText = new StringBuilder(this.selectedCommand);

      for (String part : this.subcommandBreadcrumb) {
         titleText.append(" > ").append(part);
      }

      titleText.append(" [Variant]");
      commandBuilder.set("#CommandName.TextSpans", Message.raw(titleText.toString()));
   }

   private void buildAliasesSection(@Nonnull AbstractCommand command, @Nonnull UICommandBuilder commandBuilder) {
      Set<String> aliases = command.getAliases();
      if (aliases != null && !aliases.isEmpty()) {
         commandBuilder.set("#AliasesSection.Visible", true);
         commandBuilder.set("#AliasesList.TextSpans", Message.raw(String.join(", ", aliases)));
      } else {
         commandBuilder.set("#AliasesSection.Visible", false);
      }
   }

   private void buildPermissionSection(@Nonnull AbstractCommand command, @Nonnull UICommandBuilder commandBuilder) {
      String permission = command.getPermission();
      if (permission != null && !permission.isEmpty()) {
         commandBuilder.set("#PermissionSection.Visible", true);
         commandBuilder.set("#PermissionLabel.TextSpans", Message.raw(permission));
      } else {
         commandBuilder.set("#PermissionSection.Visible", false);
      }
   }

   private void buildVariantsSection(
      @Nonnull AbstractCommand command, @Nonnull Player playerComponent, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder
   ) {
      commandBuilder.clear("#VariantsList");

      try {
         Field variantsField = AbstractCommand.class.getDeclaredField("variantCommands");
         variantsField.setAccessible(true);
         Int2ObjectMap<AbstractCommand> variants = (Int2ObjectMap<AbstractCommand>)variantsField.get(command);
         if (variants.isEmpty()) {
            commandBuilder.set("#VariantsSection.Visible", false);
            return;
         }

         commandBuilder.set("#VariantsSection.Visible", true);
         int displayIndex = 0;
         ObjectIterator var8 = variants.int2ObjectEntrySet().iterator();

         while (var8.hasNext()) {
            it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<AbstractCommand> entry = (it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<AbstractCommand>)var8.next();
            AbstractCommand variant = (AbstractCommand)entry.getValue();
            int variantIndex = entry.getIntKey();
            if (variant.hasPermission(playerComponent)) {
               commandBuilder.append("#VariantsList", "Pages/VariantCard.ui");
               commandBuilder.set("#VariantsList[" + displayIndex + "] #VariantUsage.TextSpans", this.getSimplifiedUsage(variant, playerComponent));
               eventBuilder.addEventBinding(
                  CustomUIEventBindingType.Activating, "#VariantsList[" + displayIndex + "]", EventData.of("Variant", String.valueOf(variantIndex))
               );
               displayIndex++;
            }
         }

         if (displayIndex == 0) {
            commandBuilder.set("#VariantsSection.Visible", false);
         }
      } catch (Exception var12) {
         commandBuilder.set("#VariantsSection.Visible", false);
      }
   }

   private void displayCommandInfo(
      @Nonnull AbstractCommand command, @Nonnull Player playerComponent, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder
   ) {
      this.buildVariantsSection(command, playerComponent, commandBuilder, eventBuilder);
      this.buildAliasesSection(command, commandBuilder);
      this.buildPermissionSection(command, commandBuilder);
      commandBuilder.set("#CommandUsageLabel.TextSpans", this.getSimplifiedUsage(command, playerComponent));
      this.buildParametersSection(command, playerComponent, commandBuilder);
      this.buildArgumentTypesSection(command, playerComponent, commandBuilder);
   }

   private Message getSimplifiedUsage(@Nonnull AbstractCommand command, @Nonnull Player playerComponent) {
      Message message = Message.raw("/").insert(command.getFullyQualifiedName());

      try {
         Field requiredArgsField = AbstractCommand.class.getDeclaredField("requiredArguments");
         requiredArgsField.setAccessible(true);

         for (RequiredArg<?> arg : (List)requiredArgsField.get(command)) {
            message.insert(" <").insert(Message.translation(arg.getName())).insert(">");
         }
      } catch (Exception var9) {
      }

      try {
         Field optionalArgsField = AbstractCommand.class.getDeclaredField("optionalArguments");
         optionalArgsField.setAccessible(true);
         Map<String, ?> optionalArgs = (Map<String, ?>)optionalArgsField.get(command);
         if (!optionalArgs.isEmpty()) {
            message.insert(" [").insert(Message.translation("server.customUI.commandListPage.optionsIndicator")).insert("]");
         }
      } catch (Exception var8) {
      }

      return message;
   }

   private void buildParametersSection(@Nonnull AbstractCommand command, @Nonnull Player playerComponent, @Nonnull UICommandBuilder commandBuilder) {
      commandBuilder.clear("#RequiredArgumentsList");
      commandBuilder.clear("#OptionalArgumentsList");
      commandBuilder.clear("#DefaultArgumentsList");
      commandBuilder.clear("#FlagArgumentsList");
      boolean hasAnyParameters = false;

      try {
         Field requiredArgsField = AbstractCommand.class.getDeclaredField("requiredArguments");
         requiredArgsField.setAccessible(true);
         List<RequiredArg<?>> requiredArgs = (List<RequiredArg<?>>)requiredArgsField.get(command);
         if (!requiredArgs.isEmpty()) {
            hasAnyParameters = true;

            for (int i = 0; i < requiredArgs.size(); i++) {
               RequiredArg<?> arg = requiredArgs.get(i);
               commandBuilder.append("#RequiredArgumentsList", "Pages/ParameterItem.ui");
               commandBuilder.set("#RequiredArgumentsList[" + i + "] #ParamName.TextSpans", Message.raw(arg.getName()));
               commandBuilder.set("#RequiredArgumentsList[" + i + "] #ParamTag.TextSpans", Message.translation("server.customUI.commandListPage.required"));
               commandBuilder.set(
                  "#RequiredArgumentsList[" + i + "] #ParamType.TextSpans",
                  Message.translation("server.customUI.commandListPage.paramType").param("type", arg.getArgumentType().getName())
               );
               commandBuilder.set(
                  "#RequiredArgumentsList[" + i + "] #ParamDescription.TextSpans",
                  arg.getDescription() != null
                     ? Message.translation(arg.getDescription())
                     : Message.translation("server.customUI.commandListPage.noDescription")
               );
            }
         }
      } catch (Exception var17) {
      }

      try {
         Field optionalArgsField = AbstractCommand.class.getDeclaredField("optionalArguments");
         optionalArgsField.setAccessible(true);
         Map<String, ?> optionalArgs = (Map<String, ?>)optionalArgsField.get(command);
         if (!optionalArgs.isEmpty()) {
            hasAnyParameters = true;
         }

         int optIndex = 0;
         int defIndex = 0;
         int flagIndex = 0;

         for (Entry<String, ?> entry : optionalArgs.entrySet()) {
            Object arg = entry.getValue();
            if (arg instanceof OptionalArg<?> optArg) {
               if (optArg.getPermission() == null || playerComponent.hasPermission(optArg.getPermission())) {
                  commandBuilder.append("#OptionalArgumentsList", "Pages/ParameterItem.ui");
                  commandBuilder.set(
                     "#OptionalArgumentsList[" + optIndex + "] #ParamName.TextSpans", Message.raw("--" + optArg.getName() + " <" + optArg.getName() + ">")
                  );
                  commandBuilder.set(
                     "#OptionalArgumentsList[" + optIndex + "] #ParamTag.TextSpans", Message.translation("server.customUI.commandListPage.optional")
                  );
                  commandBuilder.set(
                     "#OptionalArgumentsList[" + optIndex + "] #ParamType.TextSpans",
                     Message.translation("server.customUI.commandListPage.paramType").param("type", optArg.getArgumentType().getName())
                  );
                  commandBuilder.set(
                     "#OptionalArgumentsList[" + optIndex + "] #ParamDescription.TextSpans",
                     optArg.getDescription() != null
                        ? Message.translation(optArg.getDescription())
                        : Message.translation("server.customUI.commandListPage.noDescription")
                  );
                  optIndex++;
               }
            } else if (arg instanceof DefaultArg<?> defArg) {
               if (defArg.getPermission() == null || playerComponent.hasPermission(defArg.getPermission())) {
                  commandBuilder.append("#DefaultArgumentsList", "Pages/ParameterItem.ui");
                  commandBuilder.set(
                     "#DefaultArgumentsList[" + defIndex + "] #ParamName.TextSpans", Message.raw("--" + defArg.getName() + " <" + defArg.getName() + ">")
                  );
                  commandBuilder.set(
                     "#DefaultArgumentsList[" + defIndex + "] #ParamTag.TextSpans", Message.translation("server.customUI.commandListPage.default")
                  );
                  commandBuilder.set(
                     "#DefaultArgumentsList[" + defIndex + "] #ParamType.TextSpans",
                     Message.translation("server.customUI.commandListPage.paramTypeDefault")
                        .param("type", defArg.getArgumentType().getName())
                        .param("default", Message.translation(defArg.getDefaultValueDescription()))
                  );
                  commandBuilder.set(
                     "#DefaultArgumentsList[" + defIndex + "] #ParamDescription.TextSpans",
                     defArg.getDescription() != null
                        ? Message.translation(defArg.getDescription())
                        : Message.translation("server.customUI.commandListPage.noDescription")
                  );
                  defIndex++;
               }
            } else if (arg instanceof FlagArg flagArg && (flagArg.getPermission() == null || playerComponent.hasPermission(flagArg.getPermission()))) {
               commandBuilder.append("#FlagArgumentsList", "Pages/ParameterItem.ui");
               commandBuilder.set("#FlagArgumentsList[" + flagIndex + "] #ParamName.TextSpans", Message.raw("--" + flagArg.getName()));
               commandBuilder.set("#FlagArgumentsList[" + flagIndex + "] #ParamTag.TextSpans", Message.translation("server.customUI.commandListPage.flag"));
               commandBuilder.set(
                  "#FlagArgumentsList[" + flagIndex + "] #ParamType.TextSpans", Message.translation("server.customUI.commandListPage.paramTypeFlag")
               );
               commandBuilder.set(
                  "#FlagArgumentsList[" + flagIndex + "] #ParamDescription.TextSpans",
                  flagArg.getDescription() != null
                     ? Message.translation(flagArg.getDescription())
                     : Message.translation("server.customUI.commandListPage.noDescription")
               );
               flagIndex++;
            }
         }
      } catch (Exception var16) {
      }

      commandBuilder.set("#ParametersSection.Visible", hasAnyParameters);
   }

   private void buildArgumentTypesSection(@Nonnull AbstractCommand command, @Nonnull Player playerComponent, @Nonnull UICommandBuilder commandBuilder) {
      commandBuilder.clear("#ArgumentTypesList");
      HashSet<ArgumentType<?>> allArgumentTypes = new HashSet<>();

      try {
         Field requiredArgsField = AbstractCommand.class.getDeclaredField("requiredArguments");
         requiredArgsField.setAccessible(true);

         for (RequiredArg<?> arg : (List)requiredArgsField.get(command)) {
            allArgumentTypes.add(arg.getArgumentType());
         }

         Field optionalArgsField = AbstractCommand.class.getDeclaredField("optionalArguments");
         optionalArgsField.setAccessible(true);
         Map<String, ?> optionalArgs = (Map<String, ?>)optionalArgsField.get(command);

         for (Object entry : optionalArgs.values()) {
            if (entry instanceof OptionalArg) {
               allArgumentTypes.add(((OptionalArg)entry).getArgumentType());
            } else if (entry instanceof DefaultArg) {
               allArgumentTypes.add(((DefaultArg)entry).getArgumentType());
            }
         }
      } catch (Exception var11) {
      }

      if (allArgumentTypes.isEmpty()) {
         commandBuilder.set("#ArgumentTypesSection.Visible", false);
      } else {
         commandBuilder.set("#ArgumentTypesSection.Visible", true);
         int index = 0;

         for (ArgumentType<?> argType : allArgumentTypes) {
            commandBuilder.append("#ArgumentTypesList", "Pages/ArgumentTypeItem.ui");
            commandBuilder.set("#ArgumentTypesList[" + index + "] #TypeName.TextSpans", argType.getName());
            commandBuilder.set("#ArgumentTypesList[" + index + "] #TypeDescription.TextSpans", argType.getArgumentUsage());
            String[] examples = argType.getExamples();
            if (examples != null && examples.length > 0) {
               commandBuilder.set(
                  "#ArgumentTypesList[" + index + "] #TypeExamples.TextSpans",
                  Message.translation("server.customUI.commandListPage.examples").param("examples", String.join("', '", examples))
               );
            } else {
               commandBuilder.set("#ArgumentTypesList[" + index + "] #TypeExamples.Visible", false);
            }

            index++;
         }
      }
   }

   public static class CommandListPageEventData {
      static final String KEY_COMMAND = "Command";
      static final String KEY_SUBCOMMAND = "Subcommand";
      static final String KEY_SEARCH_QUERY = "@SearchQuery";
      static final String KEY_NAVIGATE_UP = "NavigateUp";
      static final String KEY_VARIANT = "Variant";
      static final String KEY_SEND_TO_CHAT = "SendToChat";
      public static final BuilderCodec<CommandListPage.CommandListPageEventData> CODEC = BuilderCodec.builder(
            CommandListPage.CommandListPageEventData.class, CommandListPage.CommandListPageEventData::new
         )
         .addField(new KeyedCodec<>("Command", Codec.STRING), (entry, s) -> entry.command = s, entry -> entry.command)
         .addField(new KeyedCodec<>("Subcommand", Codec.STRING), (entry, s) -> entry.subcommand = s, entry -> entry.subcommand)
         .addField(new KeyedCodec<>("@SearchQuery", Codec.STRING), (entry, s) -> entry.searchQuery = s, entry -> entry.searchQuery)
         .addField(new KeyedCodec<>("NavigateUp", Codec.STRING), (entry, s) -> entry.navigateUp = s, entry -> entry.navigateUp)
         .addField(new KeyedCodec<>("Variant", Codec.STRING), (entry, s) -> entry.variantIndex = s, entry -> entry.variantIndex)
         .addField(new KeyedCodec<>("SendToChat", Codec.STRING), (entry, s) -> entry.sendToChat = s, entry -> entry.sendToChat)
         .build();
      private String command;
      private String subcommand;
      private String searchQuery;
      private String navigateUp;
      private String variantIndex;
      private String sendToChat;
   }

   private static class SearchResult {
      public static final Comparator<CommandListPage.SearchResult> COMPARATOR = Comparator.comparing(o -> o.match);
      private final String name;
      private MatchResult match;

      public SearchResult(String name, MatchResult match) {
         this.name = name;
         this.match = match;
      }
   }
}
