package com.hypixel.hytale.server.core.plugin.commands;

import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.HytaleServerConfig;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.ParseResult;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.SingleArgumentType;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.plugin.PluginBase;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import com.hypixel.hytale.server.core.plugin.PluginState;
import com.hypixel.hytale.server.core.plugin.pages.PluginListPage;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.message.MessageFormat;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

public class PluginCommand extends AbstractCommandCollection {
   @Nonnull
   private static final SingleArgumentType<PluginIdentifier> PLUGIN_IDENTIFIER_ARG_TYPE = new SingleArgumentType<PluginIdentifier>(
      "server.commands.parsing.argtype.pluginidentifier.name", "server.commands.parsing.argtype.pluginidentifier.usage"
   ) {
      @Nonnull
      public PluginIdentifier parse(@Nonnull String input, @Nonnull ParseResult parseResult) {
         try {
            return PluginIdentifier.fromString(input);
         } catch (Exception var4) {
            parseResult.fail(
               Message.translation("server.commands.parsing.argtype.pluginidentifier.fail").param("input", input).param("error", var4.getMessage())
            );
            return null;
         }
      }
   };

   public PluginCommand() {
      super("plugin", "server.commands.plugin.desc");
      this.addAliases("plugins", "pl");
      this.addSubCommand(new PluginCommand.PluginListCommand());
      this.addSubCommand(new PluginCommand.PluginLoadCommand());
      this.addSubCommand(new PluginCommand.PluginUnloadCommand());
      this.addSubCommand(new PluginCommand.PluginReloadCommand());
      this.addSubCommand(new PluginCommand.PluginManageCommand());
   }

   private static class PluginListCommand extends CommandBase {
      public PluginListCommand() {
         super("list", "server.commands.plugin.list.desc");
         this.addAliases("ls");
      }

      @Override
      protected void executeSync(@Nonnull CommandContext context) {
         PluginManager module = PluginManager.get();
         Set<Message> plugins = module.getPlugins()
            .stream()
            .map(PluginBase::getIdentifier)
            .map(PluginIdentifier::toString)
            .map(Message::raw)
            .collect(Collectors.toSet());
         context.sendMessage(MessageFormat.list(Message.translation("server.commands.plugin.plugins"), plugins));
      }
   }

   private static class PluginLoadCommand extends CommandBase {
      @Nonnull
      private final RequiredArg<PluginIdentifier> pluginNameArg = this.withRequiredArg(
         "pluginName", "server.commands.plugin.load.pluginName.desc", PluginCommand.PLUGIN_IDENTIFIER_ARG_TYPE
      );
      @Nonnull
      private final FlagArg bootFlag = this.withFlagArg("boot", "server.commands.plugin.load.boot.desc");

      public PluginLoadCommand() {
         super("load", "server.commands.plugin.load.desc");
         this.addAliases("l");
      }

      @Override
      protected void executeSync(@Nonnull CommandContext context) {
         PluginManager module = PluginManager.get();
         PluginIdentifier identifier = this.pluginNameArg.get(context);
         PluginBase plugin = module.getPlugin(identifier);
         if (identifier != null) {
            boolean onlyBootList = this.bootFlag.get(context);
            HytaleServerConfig serverConfig = HytaleServer.get().getConfig();
            HytaleServerConfig.setBoot(serverConfig, identifier, true);
            if (serverConfig.consumeHasChanged()) {
               HytaleServerConfig.save(serverConfig).join();
            }

            context.sendMessage(Message.translation("server.commands.plugin.bootListEnabled").param("id", identifier.toString()));
            if (onlyBootList) {
               return;
            }
         }

         if (plugin != null && plugin.getState() != PluginState.DISABLED) {
            assert identifier != null;

            switch (plugin.getState()) {
               case NONE:
                  context.sendMessage(Message.translation("server.commands.plugin.failedToLoadInvalidState").param("id", identifier.toString()));
                  break;
               case SETUP:
                  context.sendMessage(Message.translation("server.commands.plugin.failedToLoadSetup").param("id", identifier.toString()));
                  break;
               case START:
                  context.sendMessage(Message.translation("server.commands.plugin.failedToLoadStarted").param("id", identifier.toString()));
                  break;
               case ENABLED:
                  context.sendMessage(Message.translation("server.commands.plugin.failedToLoadAlreadyEnabled").param("id", identifier.toString()));
                  break;
               default:
                  context.sendMessage(Message.translation("server.commands.plugin.failedPluginState").param("state", plugin.getState().toString()));
            }
         } else {
            context.sendMessage(Message.translation("server.commands.plugin.pluginLoading").param("id", identifier.toString()));
            if (module.load(identifier)) {
               context.sendMessage(Message.translation("server.commands.plugin.pluginLoaded").param("id", identifier.toString()));
            } else {
               context.sendMessage(Message.translation("server.commands.plugin.failedToLoadPlugin").param("id", identifier.toString()));
            }
         }
      }
   }

   private static class PluginManageCommand extends AbstractPlayerCommand {
      public PluginManageCommand() {
         super("manage", "server.commands.plugin.manage.desc");
         this.addAliases("m");
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         Player playerComponent = store.getComponent(ref, Player.getComponentType());

         assert playerComponent != null;

         playerComponent.getPageManager().openCustomPage(ref, store, new PluginListPage(playerRef));
      }
   }

   private static class PluginReloadCommand extends CommandBase {
      @Nonnull
      private final RequiredArg<PluginIdentifier> pluginNameArg = this.withRequiredArg(
         "pluginName", "server.commands.plugin.reload.pluginName.desc", PluginCommand.PLUGIN_IDENTIFIER_ARG_TYPE
      );

      public PluginReloadCommand() {
         super("reload", "server.commands.plugin.reload.desc");
         this.addAliases("r");
      }

      @Override
      protected void executeSync(@Nonnull CommandContext context) {
         PluginManager module = PluginManager.get();
         PluginIdentifier identifier = this.pluginNameArg.get(context);
         PluginBase plugin = module.getPlugin(identifier);
         if (plugin != null) {
            switch (plugin.getState()) {
               case NONE:
                  context.sendMessage(Message.translation("server.commands.plugin.failedToReloadState").param("id", identifier.toString()));
                  break;
               case SETUP:
                  context.sendMessage(Message.translation("server.commands.plugin.failedToReloadSetup").param("id", identifier.toString()));
                  break;
               case START:
                  context.sendMessage(Message.translation("server.commands.plugin.failedToReloadStarted").param("id", identifier.toString()));
                  break;
               case ENABLED:
                  if (module.reload(identifier)) {
                     context.sendMessage(Message.translation("server.commands.plugin.pluginReloaded").param("id", identifier.toString()));
                  } else {
                     context.sendMessage(Message.translation("server.commands.plugin.failedToReload").param("id", identifier.toString()));
                  }
                  break;
               case DISABLED:
                  context.sendMessage(Message.translation("server.commands.plugin.failedToReloadDisabled").param("id", identifier.toString()));
                  break;
               default:
                  context.sendMessage(Message.translation("server.commands.plugin.failedPluginState").param("state", plugin.getState().toString()));
            }
         } else {
            context.sendMessage(Message.translation("server.commands.plugin.notLoaded").param("id", identifier.toString()));
         }
      }
   }

   private static class PluginUnloadCommand extends CommandBase {
      @Nonnull
      private final RequiredArg<PluginIdentifier> pluginNameArg = this.withRequiredArg(
         "pluginName", "server.commands.plugin.unload.pluginName.desc", PluginCommand.PLUGIN_IDENTIFIER_ARG_TYPE
      );
      @Nonnull
      private final FlagArg bootFlag = this.withFlagArg("boot", "server.commands.plugin.unload.boot.desc");

      public PluginUnloadCommand() {
         super("unload", "server.commands.plugin.unload.desc");
         this.addAliases("u");
      }

      @Override
      protected void executeSync(@Nonnull CommandContext context) {
         PluginManager module = PluginManager.get();
         PluginIdentifier identifier = this.pluginNameArg.get(context);
         PluginBase plugin = module.getPlugin(identifier);
         if (identifier != null) {
            boolean onlyBootList = this.bootFlag.get(context);
            HytaleServerConfig serverConfig = HytaleServer.get().getConfig();
            HytaleServerConfig.setBoot(serverConfig, identifier, false);
            if (serverConfig.consumeHasChanged()) {
               HytaleServerConfig.save(serverConfig).join();
            }

            context.sendMessage(Message.translation("server.commands.plugin.bootListDisabled").param("id", identifier.toString()));
            if (onlyBootList) {
               return;
            }
         }

         if (plugin != null) {
            switch (plugin.getState()) {
               case NONE:
                  context.sendMessage(Message.translation("server.commands.plugin.failedToUnloadState").param("id", identifier.toString()));
                  break;
               case SETUP:
                  context.sendMessage(Message.translation("server.commands.plugin.failedToUnloadSetup").param("id", identifier.toString()));
                  break;
               case START:
                  context.sendMessage(Message.translation("server.commands.plugin.failedToUnloadStarted").param("id", identifier.toString()));
                  break;
               case ENABLED:
                  context.sendMessage(Message.translation("server.commands.plugin.pluginUnloading").param("id", identifier.toString()));
                  if (module.unload(identifier)) {
                     context.sendMessage(Message.translation("server.commands.plugin.pluginUnloaded").param("id", identifier.toString()));
                  } else {
                     context.sendMessage(Message.translation("server.commands.plugin.failedToUnload").param("id", identifier.toString()));
                  }
                  break;
               case DISABLED:
                  context.sendMessage(Message.translation("server.commands.plugin.failedToUnloadDisabled").param("id", identifier.toString()));
                  break;
               default:
                  context.sendMessage(Message.translation("server.commands.plugin.failedPluginState").param("state", plugin.getState().toString()));
            }
         } else {
            context.sendMessage(Message.translation("server.commands.plugin.notLoaded").param("id", identifier.toString()));
         }
      }
   }
}
