package com.hypixel.hytale.builtin.buildertools.prefabeditor.commands;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.PrefabEditSession;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.PrefabEditSessionManager;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.PrefabEditingMetadata;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.enums.PrefabRootDirectory;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.saving.PrefabSaver;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.saving.PrefabSaverSettings;
import com.hypixel.hytale.common.util.PathUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.singleplayer.SingleplayerModule;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public class PrefabEditSaveAsCommand extends AbstractAsyncPlayerCommand {
   private final RequiredArg<String> fileNameArg = this.withRequiredArg("fileNameArg", "server.commands.editprefab.save.saveAs.desc", ArgTypes.STRING);
   private final DefaultArg<PrefabRootDirectory> prefabPathArg = this.withDefaultArg(
      "prefabPath",
      "server.commands.editprefab.save.path.desc",
      ArgTypes.forEnum("PrefabPath", PrefabRootDirectory.class),
      PrefabRootDirectory.SERVER,
      "server.commands.editprefab.save.path.default.desc"
   );
   private final FlagArg noEntitiesArg = this.withFlagArg("noEntities", "server.commands.editprefab.save.noEntities.desc");
   private final FlagArg overwriteArg = this.withFlagArg("overwrite", "server.commands.editprefab.save.overwrite.desc");
   private final FlagArg emptyArg = this.withFlagArg("empty", "server.commands.editprefab.save.empty.desc");
   private final FlagArg noUpdateArg = this.withFlagArg("noUpdate", "server.commands.editprefab.saveAs.noUpdate.desc");
   private final FlagArg clearSupportArg = this.withFlagArg("clearSupport", "server.commands.editprefab.save.clearSupport.desc");

   public PrefabEditSaveAsCommand() {
      super("saveas", "server.commands.editprefab.saveAs.desc");
   }

   @Nonnull
   @Override
   protected CompletableFuture<Void> executeAsync(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      UUID uuid = playerRef.getUuid();
      PrefabEditSessionManager prefabEditSessionManager = BuilderToolsPlugin.get().getPrefabEditSessionManager();
      PrefabEditSession prefabEditSession = prefabEditSessionManager.getPrefabEditSession(uuid);
      if (prefabEditSession == null) {
         context.sendMessage(Message.translation("server.commands.editprefab.notInEditSession"));
         return CompletableFuture.completedFuture(null);
      } else {
         PrefabSaverSettings prefabSaverSettings = new PrefabSaverSettings();
         prefabSaverSettings.setBlocks(true);
         prefabSaverSettings.setEntities(!this.noEntitiesArg.provided(context));
         prefabSaverSettings.setOverwriteExisting(this.overwriteArg.get(context));
         prefabSaverSettings.setEmpty(this.emptyArg.get(context));
         prefabSaverSettings.setClearSupportValues(this.clearSupportArg.get(context));
         Path prefabRootPath = this.prefabPathArg.get(context).getPrefabPath();
         if (!PathUtil.isChildOf(prefabRootPath, prefabRootPath.resolve(this.fileNameArg.get(context))) && !SingleplayerModule.isOwner(playerRef)) {
            context.sendMessage(Message.translation("server.builderTools.attemptedToSaveOutsidePrefabsDir"));
            return CompletableFuture.completedFuture(null);
         } else {
            Path prefabSavePath = prefabRootPath.resolve(this.fileNameArg.get(context));
            if (prefabSavePath.toString().endsWith("/")) {
               context.sendMessage(Message.translation("server.commands.editprefab.saveAs.errors.notAFile"));
               return CompletableFuture.completedFuture(null);
            } else {
               if (!prefabEditSession.toString().endsWith(".prefab.json")) {
                  prefabSavePath = Path.of(prefabSavePath + ".prefab.json");
               }

               PrefabEditingMetadata selectedPrefab = prefabEditSession.getSelectedPrefab(uuid);
               if (selectedPrefab == null) {
                  context.sendMessage(Message.translation("server.commands.editprefab.noPrefabSelected"));
                  return CompletableFuture.completedFuture(null);
               } else {
                  BlockSelection selection = BuilderToolsPlugin.getState(playerComponent, playerRef).getSelection();
                  if (selectedPrefab.getMinPoint().equals(selection.getSelectionMin()) && selectedPrefab.getMaxPoint().equals(selection.getSelectionMax())) {
                     if (!this.noUpdateArg.provided(context)) {
                        prefabEditSessionManager.updatePathOfLoadedPrefab(selectedPrefab.getPrefabPath(), prefabSavePath);
                        selectedPrefab.setPrefabPath(prefabSavePath);
                     }

                     return PrefabSaver.savePrefab(
                           playerComponent,
                           world,
                           prefabSavePath,
                           selectedPrefab.getAnchorPoint(),
                           selectedPrefab.getMinPoint(),
                           selectedPrefab.getMaxPoint(),
                           selectedPrefab.getPastePosition(),
                           selectedPrefab.getOriginalFileAnchor(),
                           prefabSaverSettings
                        )
                        .thenAccept(
                           success -> context.sendMessage(
                              Message.translation("server.commands.editprefab.save." + (success ? "success" : "failure"))
                                 .param("name", selectedPrefab.getPrefabPath().toString())
                           )
                        );
                  } else {
                     context.sendMessage(Message.translation("server.commands.editprefab.save.selectionMismatch"));
                     return CompletableFuture.completedFuture(null);
                  }
               }
            }
         }
      }
   }
}
