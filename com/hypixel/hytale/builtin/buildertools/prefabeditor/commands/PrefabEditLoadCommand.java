package com.hypixel.hytale.builtin.buildertools.prefabeditor.commands;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.PrefabEditorCreationSettings;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.enums.PrefabAlignment;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.enums.PrefabRootDirectory;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.enums.PrefabRowSplitMode;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.enums.PrefabStackingAxis;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.enums.WorldGenType;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.ui.PrefabEditorLoadSettingsPage;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncPlayerCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public class PrefabEditLoadCommand extends AbstractAsyncPlayerCommand {
   public static final int DEFAULT_PASTE_LEVEL_GOAL = 55;
   public static final int DEFAULT_BLOCKS_BETWEEN_MULTI_PREFABS = 15;
   @Nonnull
   public static final WorldGenType DEFAULT_WORLD_GEN_TYPE = WorldGenType.FLAT;
   public static final int DEFAULT_BLOCKS_ABOVE_SURFACE = 0;
   @Nonnull
   public static final PrefabStackingAxis DEFAULT_PREFAB_STACKING_AXIS = PrefabStackingAxis.X;
   @Nonnull
   public static final PrefabAlignment DEFAULT_PREFAB_ALIGNMENT = PrefabAlignment.ANCHOR;
   public static final int MAX_BLOCKS_BETWEEN_EACH_PREFAB = 100;
   public static final int MAX_BLOCKS_UNTIL_SURFACE = 120;
   @Nonnull
   public static final PrefabRootDirectory DEFAULT_PREFAB_ROOT_DIRECTORY = PrefabRootDirectory.ASSET;
   @Nonnull
   public static final PrefabRowSplitMode DEFAULT_ROW_SPLIT_MODE = PrefabRowSplitMode.BY_ALL_SUBFOLDERS;
   @Nonnull
   private static final Message MESSAGE_COMMANDS_PREFAB_EDIT_SESSION_MANAGER_EXISTING_EDIT_SESSION = Message.translation(
      "server.commands.prefabeditsessionmanager.existingEditSession"
   );
   @Nonnull
   private static final Message MESSAGE_COMMANDS_EDIT_PREFAB_LOADING = Message.translation("server.commands.editprefab.loading");
   @Nonnull
   private final RequiredArg<PrefabRootDirectory> prefabPathArg = this.withRequiredArg(
      "prefabPath", "server.commands.editprefab.load.path.desc", ArgTypes.forEnum("PrefabPath", PrefabRootDirectory.class)
   );
   @Nonnull
   private final RequiredArg<List<String>> prefabNameArg = this.withListRequiredArg("prefabName", "server.commands.editprefab.load.name.desc", ArgTypes.STRING);
   @Nonnull
   private final DefaultArg<Integer> pasteLevelGoalArg = this.withDefaultArg(
         "pasteLevelGoal",
         "server.commands.editprefab.load.pasteLevelGoal.desc",
         ArgTypes.INTEGER,
         55,
         "server.commands.editprefab.load.pasteLevelGoal.default.desc"
      )
      .addValidator(Validators.range(0, 320));
   @Nonnull
   private final DefaultArg<Integer> blocksBetweenMultiPrefabsArg = this.withDefaultArg(
         "spacing", "server.commands.editprefab.load.spacing.desc", ArgTypes.INTEGER, 15, "server.commands.editprefab.load.spacing.default.desc"
      )
      .addValidator(Validators.range(0, 100));
   @Nonnull
   private final DefaultArg<WorldGenType> worldGenTypeArg = this.withDefaultArg(
      "worldgen",
      "server.commands.editprefab.load.worldGenType.desc",
      ArgTypes.forEnum("WorldGenType", WorldGenType.class),
      DEFAULT_WORLD_GEN_TYPE,
      "server.commands.editprefab.load.worldGenType.default.desc"
   );
   @Nonnull
   private final DefaultArg<Integer> flatNumBlocksBelowArg = this.withDefaultArg(
         "blocksAboveSurface",
         "server.commands.editprefab.load.numBlocksToSurface.desc",
         ArgTypes.INTEGER,
         0,
         "server.commands.editprefab.load.numBlocksToSurface.default.desc"
      )
      .addValidator(Validators.range(0, 120))
      .availableOnlyIfAll(this.worldGenTypeArg);
   @Nonnull
   private final DefaultArg<PrefabStackingAxis> axisArg = this.withDefaultArg(
         "stackingAxis",
         "server.commands.editprefab.load.axis.desc",
         ArgTypes.forEnum("Stacking Axis", PrefabStackingAxis.class),
         DEFAULT_PREFAB_STACKING_AXIS,
         "server.commands.editprefab.load.axis.default.desc"
      )
      .addAliases("axis");
   @Nonnull
   private final DefaultArg<PrefabAlignment> alignmentArg = this.withDefaultArg(
      "alignment",
      "server.commands.editprefab.load.alignment.desc",
      ArgTypes.forEnum("Alignment", PrefabAlignment.class),
      PrefabAlignment.ANCHOR,
      "server.commands.editprefab.load.alignment.default.desc"
   );
   @Nonnull
   private final FlagArg recursiveArg = this.withFlagArg("recursive", "server.commands.editprefab.load.recursive.desc");
   @Nonnull
   private final FlagArg loadChildrenArg = this.withFlagArg("loadChildren", "server.commands.editprefab.load.loadChildren.desc").addAliases("children");
   @Nonnull
   private final FlagArg loadEntitiesArg = this.withFlagArg("loadEntities", "server.commands.editprefab.load.loadEntities.desc").addAliases("entities");

   public PrefabEditLoadCommand() {
      super("load", "server.commands.editprefab.load.desc");
      this.addUsageVariant(
         new AbstractPlayerCommand("server.commands.editprefab.load.desc") {
            @Nonnull
            private static final Message MESSAGE_COMMANDS_PREFAB_EDIT_SESSION_MANAGER_EXISTING_EDIT_SESSION = Message.translation(
               "server.commands.prefabeditsessionmanager.existingEditSession"
            );

            @Override
            protected void execute(
               @Nonnull CommandContext context,
               @Nonnull Store<EntityStore> store,
               @Nonnull Ref<EntityStore> ref,
               @Nonnull PlayerRef playerRef,
               @Nonnull World world
            ) {
               if (BuilderToolsPlugin.get().getPrefabEditSessionManager().isEditingAPrefab(playerRef.getUuid())) {
                  context.sendMessage(MESSAGE_COMMANDS_PREFAB_EDIT_SESSION_MANAGER_EXISTING_EDIT_SESSION);
               } else {
                  Player playerComponent = store.getComponent(ref, Player.getComponentType());

                  assert playerComponent != null;

                  playerComponent.getPageManager().openCustomPage(ref, store, new PrefabEditorLoadSettingsPage(playerRef));
               }
            }
         }
      );
   }

   @Nonnull
   @Override
   protected CompletableFuture<Void> executeAsync(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      if (BuilderToolsPlugin.get().getPrefabEditSessionManager().isEditingAPrefab(playerRef.getUuid())) {
         context.sendMessage(MESSAGE_COMMANDS_PREFAB_EDIT_SESSION_MANAGER_EXISTING_EDIT_SESSION);
         return CompletableFuture.completedFuture(null);
      } else {
         PrefabEditorCreationSettings prefabEditorLoadCommandSettings = new PrefabEditorCreationSettings(
            this.prefabPathArg.get(context),
            this.prefabNameArg.get(context),
            this.pasteLevelGoalArg.get(context),
            this.blocksBetweenMultiPrefabsArg.get(context),
            this.worldGenTypeArg.get(context),
            this.flatNumBlocksBelowArg.get(context),
            this.axisArg.get(context),
            this.alignmentArg.get(context),
            this.recursiveArg.get(context),
            this.loadChildrenArg.get(context),
            this.loadEntitiesArg.get(context),
            false,
            DEFAULT_ROW_SPLIT_MODE,
            "Env_Zone1_Plains",
            "#5B9E28"
         );
         Player playerComponent = store.getComponent(ref, Player.getComponentType());

         assert playerComponent != null;

         context.sendMessage(MESSAGE_COMMANDS_EDIT_PREFAB_LOADING);
         return BuilderToolsPlugin.get()
            .getPrefabEditSessionManager()
            .loadPrefabAndCreateEditSession(ref, playerComponent, prefabEditorLoadCommandSettings, store);
      }
   }
}
