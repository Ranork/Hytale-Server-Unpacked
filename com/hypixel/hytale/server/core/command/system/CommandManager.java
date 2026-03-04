package com.hypixel.hytale.server.core.command.system;

import com.hypixel.hytale.common.util.CompletableFutureUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.logger.sentry.SkipSentryException;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.particle.commands.ParticleCommand;
import com.hypixel.hytale.server.core.command.commands.debug.AssetsCommand;
import com.hypixel.hytale.server.core.command.commands.debug.DebugPlayerPositionCommand;
import com.hypixel.hytale.server.core.command.commands.debug.HitDetectionCommand;
import com.hypixel.hytale.server.core.command.commands.debug.HudManagerTestCommand;
import com.hypixel.hytale.server.core.command.commands.debug.LogCommand;
import com.hypixel.hytale.server.core.command.commands.debug.MessageTranslationTestCommand;
import com.hypixel.hytale.server.core.command.commands.debug.PIDCheckCommand;
import com.hypixel.hytale.server.core.command.commands.debug.PacketStatsCommand;
import com.hypixel.hytale.server.core.command.commands.debug.PingCommand;
import com.hypixel.hytale.server.core.command.commands.debug.ShowBuilderToolsHudCommand;
import com.hypixel.hytale.server.core.command.commands.debug.StopNetworkChunkSendingCommand;
import com.hypixel.hytale.server.core.command.commands.debug.TagPatternCommand;
import com.hypixel.hytale.server.core.command.commands.debug.VersionCommand;
import com.hypixel.hytale.server.core.command.commands.debug.component.hitboxcollision.HitboxCollisionCommand;
import com.hypixel.hytale.server.core.command.commands.debug.component.repulsion.RepulsionCommand;
import com.hypixel.hytale.server.core.command.commands.debug.packs.PacksCommand;
import com.hypixel.hytale.server.core.command.commands.debug.server.ServerCommand;
import com.hypixel.hytale.server.core.command.commands.debug.stresstest.StressTestCommand;
import com.hypixel.hytale.server.core.command.commands.player.DamageCommand;
import com.hypixel.hytale.server.core.command.commands.player.GameModeCommand;
import com.hypixel.hytale.server.core.command.commands.player.HideCommand;
import com.hypixel.hytale.server.core.command.commands.player.KillCommand;
import com.hypixel.hytale.server.core.command.commands.player.PlayerCommand;
import com.hypixel.hytale.server.core.command.commands.player.ReferCommand;
import com.hypixel.hytale.server.core.command.commands.player.SudoCommand;
import com.hypixel.hytale.server.core.command.commands.player.ToggleBlockPlacementOverrideCommand;
import com.hypixel.hytale.server.core.command.commands.player.WhereAmICommand;
import com.hypixel.hytale.server.core.command.commands.player.WhoAmICommand;
import com.hypixel.hytale.server.core.command.commands.player.inventory.GiveCommand;
import com.hypixel.hytale.server.core.command.commands.player.inventory.InventoryCommand;
import com.hypixel.hytale.server.core.command.commands.player.inventory.ItemStateCommand;
import com.hypixel.hytale.server.core.command.commands.server.KickCommand;
import com.hypixel.hytale.server.core.command.commands.server.MaxPlayersCommand;
import com.hypixel.hytale.server.core.command.commands.server.StopCommand;
import com.hypixel.hytale.server.core.command.commands.server.WhoCommand;
import com.hypixel.hytale.server.core.command.commands.server.auth.AuthCommand;
import com.hypixel.hytale.server.core.command.commands.utility.BackupCommand;
import com.hypixel.hytale.server.core.command.commands.utility.ConvertPrefabsCommand;
import com.hypixel.hytale.server.core.command.commands.utility.EventTitleCommand;
import com.hypixel.hytale.server.core.command.commands.utility.NotifyCommand;
import com.hypixel.hytale.server.core.command.commands.utility.StashCommand;
import com.hypixel.hytale.server.core.command.commands.utility.UIGalleryCommand;
import com.hypixel.hytale.server.core.command.commands.utility.ValidateCPBCommand;
import com.hypixel.hytale.server.core.command.commands.utility.git.GitCommand;
import com.hypixel.hytale.server.core.command.commands.utility.help.HelpCommand;
import com.hypixel.hytale.server.core.command.commands.utility.lighting.LightingCommand;
import com.hypixel.hytale.server.core.command.commands.utility.metacommands.CommandsCommand;
import com.hypixel.hytale.server.core.command.commands.utility.net.NetworkCommand;
import com.hypixel.hytale.server.core.command.commands.utility.sleep.SleepCommand;
import com.hypixel.hytale.server.core.command.commands.utility.sound.SoundCommand;
import com.hypixel.hytale.server.core.command.commands.utility.worldmap.WorldMapCommand;
import com.hypixel.hytale.server.core.command.commands.world.SpawnBlockCommand;
import com.hypixel.hytale.server.core.command.commands.world.chunk.ChunkCommand;
import com.hypixel.hytale.server.core.command.commands.world.entity.EntityCommand;
import com.hypixel.hytale.server.core.command.commands.world.worldgen.WorldGenCommand;
import com.hypixel.hytale.server.core.command.system.exceptions.CommandException;
import com.hypixel.hytale.server.core.command.system.exceptions.GeneralCommandException;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CommandManager implements CommandOwner {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private static CommandManager instance;
   private final Map<String, AbstractCommand> commandRegistration = new Object2ObjectOpenHashMap();
   private final Map<String, String> aliases = new Object2ObjectOpenHashMap();

   public static CommandManager get() {
      return instance;
   }

   public CommandManager() {
      instance = this;
   }

   public void shutdown() {
      this.aliases.clear();
   }

   @Nonnull
   public Map<String, AbstractCommand> getCommandRegistration() {
      return this.commandRegistration;
   }

   public void registerCommands() {
      this.registerSystemCommand(new ChunkCommand());
      this.registerSystemCommand(new LogCommand());
      this.registerSystemCommand(new PIDCheckCommand());
      this.registerSystemCommand(new PingCommand());
      this.registerSystemCommand(new WorldGenCommand());
      this.registerSystemCommand(new HitDetectionCommand());
      this.registerSystemCommand(new PacketStatsCommand());
      this.registerSystemCommand(new AssetsCommand());
      this.registerSystemCommand(new PacksCommand());
      this.registerSystemCommand(new ServerCommand());
      this.registerSystemCommand(new StressTestCommand());
      this.registerSystemCommand(new HitboxCollisionCommand());
      this.registerSystemCommand(new DebugPlayerPositionCommand());
      this.registerSystemCommand(new MessageTranslationTestCommand());
      this.registerSystemCommand(new HudManagerTestCommand());
      this.registerSystemCommand(new RepulsionCommand());
      this.registerSystemCommand(new StopNetworkChunkSendingCommand());
      this.registerSystemCommand(new ShowBuilderToolsHudCommand());
      this.registerSystemCommand(new VersionCommand());
      this.registerSystemCommand(new ParticleCommand());
      this.registerSystemCommand(new TagPatternCommand());
      this.registerSystemCommand(new GameModeCommand());
      this.registerSystemCommand(new HideCommand());
      this.registerSystemCommand(new KillCommand());
      this.registerSystemCommand(new DamageCommand());
      this.registerSystemCommand(new SudoCommand());
      this.registerSystemCommand(new WhereAmICommand());
      this.registerSystemCommand(new WhoAmICommand());
      this.registerSystemCommand(new ReferCommand());
      this.registerSystemCommand(new ToggleBlockPlacementOverrideCommand());
      this.registerSystemCommand(new GiveCommand());
      this.registerSystemCommand(new InventoryCommand());
      this.registerSystemCommand(new ItemStateCommand());
      this.registerSystemCommand(new AuthCommand());
      this.registerSystemCommand(new KickCommand());
      this.registerSystemCommand(new MaxPlayersCommand());
      this.registerSystemCommand(new StopCommand());
      this.registerSystemCommand(new WhoCommand());
      this.registerSystemCommand(new BackupCommand());
      this.registerSystemCommand(new ConvertPrefabsCommand());
      this.registerSystemCommand(new HelpCommand());
      this.registerSystemCommand(new NotifyCommand());
      this.registerSystemCommand(new EventTitleCommand());
      this.registerSystemCommand(new ValidateCPBCommand());
      this.registerSystemCommand(new WorldMapCommand());
      this.registerSystemCommand(new SoundCommand());
      this.registerSystemCommand(new StashCommand());
      this.registerSystemCommand(new SpawnBlockCommand());
      this.registerSystemCommand(new EntityCommand());
      this.registerSystemCommand(new PlayerCommand());
      this.registerSystemCommand(new LightingCommand());
      this.registerSystemCommand(new SleepCommand());
      this.registerSystemCommand(new NetworkCommand());
      this.registerSystemCommand(new CommandsCommand());
      this.registerSystemCommand(new UIGalleryCommand());
      this.registerSystemCommand(new GitCommand());
   }

   public Map<String, Set<String>> createVirtualPermissionGroups() {
      Map<String, Set<String>> permissionsByGroup = new Object2ObjectOpenHashMap();

      for (AbstractCommand command : this.commandRegistration.values()) {
         for (Entry<String, Set<String>> entry : command.getPermissionGroupsRecursive().entrySet()) {
            Set<String> permissionsForGroup = permissionsByGroup.computeIfAbsent(entry.getKey(), k -> new HashSet<>());
            permissionsForGroup.addAll(entry.getValue());
         }
      }

      return permissionsByGroup;
   }

   public void registerSystemCommand(@Nonnull AbstractCommand command) {
      command.setOwner(this);
      this.register(command);
   }

   @Nullable
   public CommandRegistration register(@Nonnull AbstractCommand command) {
      String name = command.getName();
      if (name != null && !name.isEmpty()) {
         this.commandRegistration.put(command.getName(), command);

         try {
            command.completeRegistration();
         } catch (Exception var7) {
            String errorMessage = var7.getMessage();
            if (var7 instanceof GeneralCommandException generalException) {
               String messageText = generalException.getMessageText();
               if (messageText != null) {
                  errorMessage = messageText;
               }
            }

            ((HytaleLogger.Api)HytaleLogger.getLogger().at(Level.SEVERE).withCause(var7))
               .log("Failed to register command: %s%s", command.getName(), errorMessage != null ? " - " + errorMessage : "");
            return null;
         }

         for (String alias : command.getAliases()) {
            this.aliases.put(alias, name);
         }

         return new CommandRegistration(command, () -> true, () -> {
            AbstractCommand remove = this.commandRegistration.remove(name);
            if (remove != null) {
               for (String aliasx : remove.getAliases()) {
                  this.aliases.remove(aliasx);
               }
            }
         });
      } else {
         throw new IllegalArgumentException("Registered commands must define a name");
      }
   }

   @Nonnull
   public CompletableFuture<Void> handleCommand(@Nonnull PlayerRef playerRef, @Nonnull String command) {
      Ref<EntityStore> ref = playerRef.getReference();
      if (ref == null) {
         return new CompletableFuture<>();
      } else {
         Store<EntityStore> store = ref.getStore();
         Player playerComponent = store.getComponent(ref, Player.getComponentType());
         return this.handleCommand(playerComponent, command);
      }
   }

   @Nonnull
   public CompletableFuture<Void> handleCommand(@Nonnull CommandSender commandSender, @Nonnull String commandString) {
      Objects.requireNonNull(commandSender, "Command sender must not be null!");
      Objects.requireNonNull(commandString, "Command must not be null!");
      CompletableFuture<Void> future = new CompletableFuture<>();
      ForkJoinPool.commonPool().execute(() -> {
         Thread thread = Thread.currentThread();
         String oldName = thread.getName();
         thread.setName(oldName + " -- Running: " + commandString);

         try {
            LOGGER.at(Level.FINE).log("%s sent command: %s", commandSender.getDisplayName(), commandString);
            int endIndex = commandString.indexOf(32);
            String commandName = (endIndex < 0 ? commandString : commandString.substring(0, endIndex)).toLowerCase();
            AbstractCommand command = this.commandRegistration.get(commandName);
            if (command == null) {
               String key = this.aliases.get(commandName);
               if (key != null && this.commandRegistration.containsKey(key)) {
                  command = this.commandRegistration.get(key);
               }
            }

            if (command != null) {
               this.runCommand(commandSender, commandString, command, future);
               return;
            }

            commandSender.sendMessage(Message.translation("server.modules.command.notFound").param("cmd", commandString));
            future.complete(null);
         } finally {
            thread.setName(oldName);
         }
      });
      return future;
   }

   private void runCommand(
      @Nonnull CommandSender commandSender, @Nonnull String commandInput, @Nonnull AbstractCommand abstractCommand, @Nonnull CompletableFuture<Void> future
   ) {
      try {
         LOGGER.at(Level.INFO).log("%s executed command: %s", commandSender.getDisplayName(), commandInput);
         ParseResult parseResult = new ParseResult();
         List<String> tokens = Tokenizer.parseArguments(commandInput, parseResult);
         if (parseResult.failed()) {
            parseResult.sendMessages(commandSender);
            future.complete(null);
            return;
         }

         ParserContext parserContext = ParserContext.of(tokens, parseResult);
         if (parseResult.failed()) {
            parseResult.sendMessages(commandSender);
            future.complete(null);
            return;
         }

         CompletableFuture<Void> commandFuture = abstractCommand.acceptCall(commandSender, parserContext, parseResult);
         if (parseResult.failed()) {
            parseResult.sendMessages(commandSender);
            future.complete(null);
            return;
         }

         if (commandFuture != null) {
            commandFuture.whenComplete(
               (aVoid, throwable) -> {
                  if (throwable != null) {
                     if (!CompletableFutureUtil.isCanceled(throwable) && !isInternalException(throwable)) {
                        ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(new SkipSentryException(throwable)))
                           .log("Failed to execute command %s for %s", commandInput, commandSender.getDisplayName());
                        commandSender.sendMessage(
                           Message.translation("server.modules.command.error").param("cmd", commandInput).param("msg", throwable.getMessage())
                        );
                     }

                     future.completeExceptionally(throwable);
                  } else {
                     future.complete(aVoid);
                  }
               }
            );
         } else {
            future.complete(null);
         }
      } catch (Throwable var9) {
         if (var9 instanceof CommandException commandException) {
            commandException.sendTranslatedMessage(commandSender);
         } else {
            ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(var9))
               .log("Failed to execute command %s for %s", commandInput, commandSender.getDisplayName());
            Message errorMsg = var9.getMessage() == null
               ? Message.translation("server.modules.command.noProvidedExceptionMessage")
               : Message.raw(var9.getMessage());
            commandSender.sendMessage(Message.translation("server.modules.command.error").param("cmd", commandInput).param("msg", errorMsg));
         }

         future.completeExceptionally(var9);
      }
   }

   private static boolean isInternalException(@Nonnull Throwable throwable) {
      if (throwable instanceof CommandException) {
         return true;
      } else {
         return throwable instanceof CompletionException && throwable.getCause() != null && throwable.getCause() != throwable
            ? isInternalException(throwable.getCause())
            : false;
      }
   }

   @Nonnull
   public CompletableFuture<Void> handleCommands(@Nonnull CommandSender sender, @Nonnull Deque<String> commands) {
      return this.handleCommands0(sender, commands);
   }

   @Nonnull
   private CompletableFuture<Void> handleCommands0(@Nonnull CommandSender sender, @Nonnull Deque<String> commands) {
      return commands.isEmpty()
         ? CompletableFuture.completedFuture(null)
         : this.handleCommand(sender, commands.poll()).thenCompose(aVoid -> this.handleCommands0(sender, commands));
   }

   @Nonnull
   @Override
   public String getName() {
      return "HytaleServer";
   }
}
