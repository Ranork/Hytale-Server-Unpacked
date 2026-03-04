package com.hypixel.hytale.server.core.universe.world.commands.world;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class WorldPruneCommand extends AbstractAsyncCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_WORLD_PRUNE_NONE_TO_PRUNE = Message.translation("server.commands.world.prune.noneToPrune");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_WORLD_PRUNE_PRUNE_ERROR = Message.translation("server.commands.world.prune.pruneError");

   public WorldPruneCommand() {
      super("prune", "server.commands.world.prune.desc", true);
   }

   @Nonnull
   @Override
   protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
      CommandSender sender = context.sender();
      World defaultWorld = Universe.get().getDefaultWorld();
      Map<String, World> worlds = Universe.get().getWorlds();
      Set<String> toRemove = new HashSet<>();
      worlds.forEach((worldKey, world) -> {
         if (world != defaultWorld && world.getPlayerCount() == 0) {
            toRemove.add(worldKey);
            world.getWorldConfig().setDeleteOnRemove(true);
         }
      });
      if (toRemove.isEmpty()) {
         sender.sendMessage(MESSAGE_COMMANDS_WORLD_PRUNE_NONE_TO_PRUNE);
         return CompletableFuture.completedFuture(null);
      } else {
         return CompletableFuture.runAsync(() -> {
            toRemove.forEach(worldKey -> {
               try {
                  boolean removed = Universe.get().removeWorld(worldKey);
                  String msgKey = removed ? "server.commands.world.prune.prunedWorld" : "server.commands.world.prune.pruneFailed";
                  sender.sendMessage(Message.translation(msgKey).param("world", worldKey));
               } catch (Throwable var4x) {
                  sender.sendMessage(MESSAGE_COMMANDS_WORLD_PRUNE_PRUNE_ERROR);
                  ((HytaleLogger.Api)HytaleLogger.getLogger().at(Level.SEVERE).withCause(var4x)).log("Error pruning world " + worldKey);
               }
            });
            sender.sendMessage(Message.translation("server.commands.world.prune.done").param("count", toRemove.size()));
         });
      }
   }
}
