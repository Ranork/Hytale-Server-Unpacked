package com.hypixel.hytale.server.core.universe.world.commands.world;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.system.WorldConfigSaveSystem;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.component.ChunkSavingSystems;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public class WorldSaveCommand extends AbstractAsyncCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_WORLD_SAVE_NO_WORLD_SPECIFIED = Message.translation("server.commands.world.save.noWorldSpecified");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_WORLD_SAVE_SAVING_ALL = Message.translation("server.commands.world.save.savingAll");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_WORLD_SAVE_SAVING_ALL_DONE = Message.translation("server.commands.world.save.savingAllDone");
   @Nonnull
   private final OptionalArg<World> worldArg = this.withOptionalArg("world", "server.commands.worldthread.arg.desc", ArgTypes.WORLD);
   @Nonnull
   private final FlagArg saveAllFlag = this.withFlagArg("all", "server.commands.world.save.all.desc");

   public WorldSaveCommand() {
      super("save", "server.commands.world.save.desc", true);
   }

   @Nonnull
   @Override
   protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
      if (this.saveAllFlag.get(context)) {
         return this.saveAllWorlds(context);
      } else if (!this.worldArg.provided(context)) {
         context.sendMessage(MESSAGE_COMMANDS_WORLD_SAVE_NO_WORLD_SPECIFIED);
         return CompletableFuture.completedFuture(null);
      } else {
         World world = this.worldArg.getProcessed(context);
         context.sendMessage(Message.translation("server.commands.world.save.saving").param("world", world.getName()));
         return CompletableFuture.runAsync(() -> saveWorld(world), world)
            .thenRun(() -> context.sendMessage(Message.translation("server.commands.world.save.savingDone").param("world", world.getName())));
      }
   }

   @Nonnull
   private CompletableFuture<Void> saveAllWorlds(@Nonnull CommandContext context) {
      context.sendMessage(MESSAGE_COMMANDS_WORLD_SAVE_SAVING_ALL);
      CompletableFuture[] completableFutures = Universe.get()
         .getWorlds()
         .values()
         .stream()
         .map(world -> CompletableFuture.runAsync(() -> saveWorld(world), world))
         .toArray(CompletableFuture[]::new);
      return CompletableFuture.allOf(completableFutures).thenRun(() -> context.sendMessage(MESSAGE_COMMANDS_WORLD_SAVE_SAVING_ALL_DONE));
   }

   @Nonnull
   private static CompletableFuture<Void> saveWorld(@Nonnull World world) {
      return CompletableFuture.allOf(
         WorldConfigSaveSystem.saveWorldConfigAndResources(world), ChunkSavingSystems.saveChunksInWorld(world.getChunkStore().getStore())
      );
   }
}
