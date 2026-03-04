package com.hypixel.hytale.builtin.hytalegenerator.commands;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil;
import com.hypixel.hytale.builtin.hytalegenerator.assets.AssetManager;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.NViewport;
import com.hypixel.hytale.common.util.ExceptionUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ViewportCommand extends AbstractPlayerCommand {
   @Nonnull
   private final FlagArg deleteFlag = this.withFlagArg("delete", "server.commands.viewport.delete.desc");
   @Nonnull
   private final OptionalArg<Integer> radiusArg = this.withOptionalArg("radius", "server.commands.viewport.radius.desc", ArgTypes.INTEGER);
   @Nonnull
   private final AssetManager assetManager;
   @Nullable
   private Runnable activeTask;

   public ViewportCommand(@Nonnull AssetManager assetManager) {
      super("Viewport", "server.commands.viewport.desc");
      this.assetManager = assetManager;
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      if (this.activeTask != null) {
         this.assetManager.unregisterReloadListener(this.activeTask);
         this.activeTask = null;
      }

      if (context.get(this.deleteFlag)) {
         playerRef.sendMessage(Message.translation("server.commands.viewport.removed"));
      } else {
         Integer radius = context.get(this.radiusArg) << 5;
         Bounds3i viewportBounds_voxelGrid;
         if (radius != null) {
            Vector3d playerPosition_voxelGrid = store.getComponent(ref, TransformComponent.getComponentType()).getPosition();
            Vector3i min_voxelGrid = playerPosition_voxelGrid.clone().subtract(radius.intValue()).toVector3i();
            Vector3i max_voxelGrid = playerPosition_voxelGrid.clone().add(radius.intValue()).toVector3i().add(Vector3i.ALL_ONES);
            viewportBounds_voxelGrid = new Bounds3i(min_voxelGrid, max_voxelGrid);
         } else {
            BuilderToolsPlugin.BuilderState builderState = BuilderToolsPlugin.getState(playerComponent, playerRef);
            BlockSelection selection = builderState.getSelection();
            if (selection == null) {
               return;
            }

            viewportBounds_voxelGrid = new Bounds3i(selection.getSelectionMin(), selection.getSelectionMax());
         }

         NViewport viewport = new NViewport(viewportBounds_voxelGrid, world, context.sender());
         this.activeTask = () -> world.execute(() -> {
            try {
               viewport.refresh();
            } catch (Exception var3x) {
               String msg = "Could not refresh viewport because of the following exception:\n";
               msg = msg + ExceptionUtil.toStringWithStack(var3x);
               LoggerUtil.getLogger().severe(msg);
            }
         });
         this.activeTask.run();
         this.assetManager.registerReloadListener(this.activeTask);
         playerRef.sendMessage(Message.translation("server.commands.viewport.created"));
      }
   }
}
