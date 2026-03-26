package com.hypixel.hytale.server.npc.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public class NPCDescriptorsCommand extends AbstractAsyncPlayerCommand {
   public NPCDescriptorsCommand() {
      super("descriptors", "server.commands.npc.descriptors.desc");
   }

   @Nonnull
   @Override
   protected CompletableFuture<Void> executeAsync(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      return this.runAsync(
         context,
         () -> {
            NPCPlugin npcPlugin = NPCPlugin.get();
            npcPlugin.generateDescriptors();
            npcPlugin.saveDescriptors();
            context.sendMessage(
               Message.translation("server.commands.npc.descriptors.saved").param("path", Path.of("npc_descriptors.json").toAbsolutePath().toString())
            );
         },
         world
      );
   }
}
