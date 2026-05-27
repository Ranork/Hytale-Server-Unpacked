package com.hypixel.hytale.builtin.hytalegenerator.commands;

import com.hypixel.hytale.builtin.hytalegenerator.plugin.editor.BiomeEditorPage;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public class CreateCommand extends AbstractAsyncPlayerCommand {
   public CreateCommand() {
      super("create", "server.commands.worldgen2.create.desc");
      this.addAliases("editor");
      this.addAliases("setup");
   }

   @Nonnull
   @Override
   protected CompletableFuture<Void> executeAsync(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      return BiomeEditorPage.open(ref);
   }
}
