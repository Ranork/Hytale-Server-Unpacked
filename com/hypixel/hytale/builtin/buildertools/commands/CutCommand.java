package com.hypixel.hytale.builtin.buildertools.commands;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.PrefabCopyException;
import com.hypixel.hytale.builtin.buildertools.PrototypePlayerBuilderToolSettings;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TempAssetIdUtil;
import javax.annotation.Nonnull;

public class CutCommand extends AbstractPlayerCommand {
   @Nonnull
   private static final Message MESSAGE_BUILDER_TOOLS_COPY_CUT_NO_SELECTION = Message.translation("server.builderTools.copycut.noSelection");
   @Nonnull
   private final FlagArg noEntitiesFlag = this.withFlagArg("noEntities", "server.commands.cut.noEntities.desc");
   @Nonnull
   private final FlagArg entitiesOnlyFlag = this.withFlagArg("onlyEntities", "server.commands.cut.entitiesonly.desc");
   @Nonnull
   private final FlagArg emptyFlag = this.withFlagArg("empty", "server.commands.cut.empty.desc");
   @Nonnull
   private final FlagArg keepAnchorsFlag = this.withFlagArg("keepanchors", "server.commands.cut.keepanchors.desc");

   public CutCommand() {
      super("cut", "server.commands.cut.desc");
      this.setPermissionGroup(GameMode.Creative);
      this.requirePermission("hytale.editor.selection.clipboard");
      this.addUsageVariant(new CutCommand.CutRegionCommand());
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      if (PrototypePlayerBuilderToolSettings.isOkayToDoCommandsOnSelection(ref, playerComponent, store)) {
         BuilderToolsPlugin.BuilderState builderState = BuilderToolsPlugin.getState(playerComponent, playerRef);
         boolean entitiesOnly = this.entitiesOnlyFlag.get(context);
         boolean noEntities = this.noEntitiesFlag.get(context);
         int settings = 2;
         if (!entitiesOnly) {
            settings |= 8;
         }

         if (this.emptyFlag.get(context)) {
            settings |= 4;
         }

         if (this.keepAnchorsFlag.get(context)) {
            settings |= 64;
         }

         if (!noEntities || entitiesOnly) {
            settings |= 16;
         }

         int settingsFinal = settings;
         BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, s, componentAccessor) -> {
            try {
               BlockSelection selection = builderState.getSelection();
               if (selection == null || !selection.hasSelectionBounds()) {
                  context.sendMessage(MESSAGE_BUILDER_TOOLS_COPY_CUT_NO_SELECTION);
                  return;
               }

               Vector3i min = selection.getSelectionMin();
               Vector3i max = selection.getSelectionMax();
               builderState.copyOrCut(r, min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ(), settingsFinal, componentAccessor);
            } catch (PrefabCopyException var9x) {
               context.sendMessage(Message.translation("server.builderTools.copycut.copyFailedReason").param("reason", var9x.getMessage()));
            }
         });
      }
   }

   private static class CutRegionCommand extends AbstractPlayerCommand {
      @Nonnull
      private final RequiredArg<Integer> xMinArg = this.withRequiredArg("xMin", "server.commands.cut.xMin.desc", ArgTypes.INTEGER);
      @Nonnull
      private final RequiredArg<Integer> yMinArg = this.withRequiredArg("yMin", "server.commands.cut.yMin.desc", ArgTypes.INTEGER);
      @Nonnull
      private final RequiredArg<Integer> zMinArg = this.withRequiredArg("zMin", "server.commands.cut.zMin.desc", ArgTypes.INTEGER);
      @Nonnull
      private final RequiredArg<Integer> xMaxArg = this.withRequiredArg("xMax", "server.commands.cut.xMax.desc", ArgTypes.INTEGER);
      @Nonnull
      private final RequiredArg<Integer> yMaxArg = this.withRequiredArg("yMax", "server.commands.cut.yMax.desc", ArgTypes.INTEGER);
      @Nonnull
      private final RequiredArg<Integer> zMaxArg = this.withRequiredArg("zMax", "server.commands.cut.zMax.desc", ArgTypes.INTEGER);
      @Nonnull
      private final FlagArg noEntitiesFlag = this.withFlagArg("noEntities", "server.commands.cut.noEntities.desc");
      @Nonnull
      private final FlagArg entitiesOnlyFlag = this.withFlagArg("onlyEntities", "server.commands.cut.entitiesonly.desc");
      @Nonnull
      private final FlagArg emptyFlag = this.withFlagArg("empty", "server.commands.cut.empty.desc");
      @Nonnull
      private final FlagArg keepAnchorsFlag = this.withFlagArg("keepanchors", "server.commands.cut.keepanchors.desc");

      public CutRegionCommand() {
         super("server.commands.cut.desc");
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         Player playerComponent = store.getComponent(ref, Player.getComponentType());

         assert playerComponent != null;

         if (PrototypePlayerBuilderToolSettings.isOkayToDoCommandsOnSelection(ref, playerComponent, store)) {
            BuilderToolsPlugin.BuilderState builderState = BuilderToolsPlugin.getState(playerComponent, playerRef);
            boolean entitiesOnly = this.entitiesOnlyFlag.get(context);
            boolean noEntities = this.noEntitiesFlag.get(context);
            int settings = 2;
            if (!entitiesOnly) {
               settings |= 8;
            }

            if (this.emptyFlag.get(context)) {
               settings |= 4;
            }

            if (this.keepAnchorsFlag.get(context)) {
               settings |= 64;
            }

            if (!noEntities || entitiesOnly) {
               settings |= 16;
            }

            int xMin = this.xMinArg.get(context);
            int yMin = this.yMinArg.get(context);
            int zMin = this.zMinArg.get(context);
            int xMax = this.xMaxArg.get(context);
            int yMax = this.yMaxArg.get(context);
            int zMax = this.zMaxArg.get(context);
            int cutSettings = settings;
            BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, s, componentAccessor) -> {
               try {
                  builderState.copyOrCut(r, xMin, yMin, zMin, xMax, yMax, zMax, cutSettings, componentAccessor);
               } catch (PrefabCopyException var13x) {
                  context.sendMessage(Message.translation("server.builderTools.copycut.copyFailedReason").param("reason", var13x.getMessage()));
                  SoundUtil.playSoundEvent2d(r, TempAssetIdUtil.getSoundEventIndex("CREATE_ERROR"), SoundCategory.UI, componentAccessor);
               }
            });
         }
      }
   }
}
