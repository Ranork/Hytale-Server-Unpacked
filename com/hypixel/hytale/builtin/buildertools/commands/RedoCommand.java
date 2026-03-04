package com.hypixel.hytale.builtin.buildertools.commands;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class RedoCommand extends AbstractPlayerCommand {
   public RedoCommand() {
      super("redo", "server.commands.redo.desc");
      this.setPermissionGroup(GameMode.Creative);
      this.requirePermission("hytale.editor.history");
      this.addAliases("r");
      this.addUsageVariant(new RedoCommand.RedoWithCountCommand());
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      executeRedo(store, ref, 1);
   }

   private static void executeRedo(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, int count) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      PlayerRef playerRefComponent = store.getComponent(ref, PlayerRef.getComponentType());

      assert playerRefComponent != null;

      BuilderToolsPlugin.addToQueue(playerComponent, playerRefComponent, (r, s, componentAccessor) -> s.redo(r, count, componentAccessor));
   }

   private static class RedoWithCountCommand extends AbstractPlayerCommand {
      @Nonnull
      private final RequiredArg<Integer> countArg = this.withRequiredArg("count", "server.commands.redo.count.desc", ArgTypes.INTEGER);

      public RedoWithCountCommand() {
         super("server.commands.redo.desc");
         this.setPermissionGroup(GameMode.Creative);
         this.requirePermission("hytale.editor.history");
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         RedoCommand.executeRedo(store, ref, this.countArg.get(context));
      }
   }
}
