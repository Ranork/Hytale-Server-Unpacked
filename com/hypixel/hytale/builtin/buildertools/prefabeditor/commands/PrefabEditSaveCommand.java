package com.hypixel.hytale.builtin.buildertools.prefabeditor.commands;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.PrefabEditSession;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.PrefabEditSessionManager;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.PrefabEditingMetadata;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.saving.PrefabSaver;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.saving.PrefabSaverSettings;
import com.hypixel.hytale.common.util.PathUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.singleplayer.SingleplayerModule;
import com.hypixel.hytale.server.core.prefab.PrefabStore;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public class PrefabEditSaveCommand extends AbstractAsyncPlayerCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_EDIT_PREFAB_NOT_IN_EDIT_SESSION = Message.translation("server.commands.editprefab.notInEditSession");
   @Nonnull
   private static final Message MESSAGE_PATH_OUTSIDE_PREFABS_DIR = Message.translation("server.builderTools.attemptedToSaveOutsidePrefabsDir");
   @Nonnull
   private final FlagArg saveAllArg = this.withFlagArg("saveAll", "server.commands.editprefab.save.saveAll.desc").addAliases("all");
   @Nonnull
   private final FlagArg noEntitiesArg = this.withFlagArg("noEntities", "server.commands.editprefab.save.noEntities.desc");
   @Nonnull
   private final FlagArg emptyArg = this.withFlagArg("empty", "server.commands.editprefab.save.empty.desc");
   @Nonnull
   private final FlagArg confirmArg = this.withFlagArg("confirm", "server.commands.editprefab.save.confirm.desc");
   @Nonnull
   private final FlagArg clearSupportArg = this.withFlagArg("clearSupport", "server.commands.editprefab.save.clearSupport.desc");

   private static boolean isPathInAllowedPrefabDirectory(@Nonnull Path path) {
      PrefabStore prefabStore = PrefabStore.get();
      if (PathUtil.isChildOf(prefabStore.getServerPrefabsPath(), path)) {
         return true;
      } else if (PathUtil.isChildOf(prefabStore.getWorldGenPrefabsPath(), path)) {
         return true;
      } else {
         AssetModule assetModule = AssetModule.get();
         return assetModule.isWithinPackSubDir(path, "Server/Prefabs") && !assetModule.isAssetPathImmutable(path);
      }
   }

   public PrefabEditSaveCommand() {
      super("save", "server.commands.editprefab.save.desc");
   }

   @Nonnull
   @Override
   protected CompletableFuture<Void> executeAsync(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      PrefabEditSessionManager prefabEditSessionManager = BuilderToolsPlugin.get().getPrefabEditSessionManager();
      PrefabEditSession prefabEditSession = prefabEditSessionManager.getPrefabEditSession(playerRef.getUuid());
      if (prefabEditSession == null) {
         context.sendMessage(MESSAGE_COMMANDS_EDIT_PREFAB_NOT_IN_EDIT_SESSION);
         return CompletableFuture.completedFuture(null);
      } else {
         PrefabSaverSettings prefabSaverSettings = new PrefabSaverSettings();
         prefabSaverSettings.setBlocks(true);
         prefabSaverSettings.setEntities(!this.noEntitiesArg.provided(context));
         prefabSaverSettings.setOverwriteExisting(true);
         prefabSaverSettings.setEmpty(this.emptyArg.get(context));
         prefabSaverSettings.setClearSupportValues(this.clearSupportArg.get(context));
         boolean confirm = this.confirmArg.provided(context);
         if (!this.saveAllArg.provided(context)) {
            PrefabEditingMetadata selectedPrefab = prefabEditSession.getSelectedPrefab(playerRef.getUuid());
            if (selectedPrefab == null) {
               context.sendMessage(Message.translation("server.commands.editprefab.noPrefabSelected"));
               return CompletableFuture.completedFuture(null);
            } else if (selectedPrefab.isReadOnly() && !confirm) {
               Path redirectPath = getWritableSavePath(selectedPrefab, true);
               context.sendMessage(
                  Message.translation("server.commands.editprefab.save.readOnlyNeedsConfirmSingle")
                     .param("path", selectedPrefab.getPrefabPath().toString())
                     .param("redirectPath", redirectPath.toString())
               );
               return CompletableFuture.completedFuture(null);
            } else {
               BlockSelection selection = BuilderToolsPlugin.getState(playerComponent, playerRef).getSelection();
               if (selectedPrefab.getMinPoint().equals(selection.getSelectionMin()) && selectedPrefab.getMaxPoint().equals(selection.getSelectionMax())) {
                  Path savePath = getWritableSavePath(selectedPrefab, confirm);
                  if (!SingleplayerModule.isOwner(playerRef) && !isPathInAllowedPrefabDirectory(savePath)) {
                     context.sendMessage(MESSAGE_PATH_OUTSIDE_PREFABS_DIR);
                     return CompletableFuture.completedFuture(null);
                  } else {
                     return PrefabSaver.savePrefab(
                           playerComponent,
                           world,
                           savePath,
                           selectedPrefab.getAnchorPoint(),
                           selectedPrefab.getMinPoint(),
                           selectedPrefab.getMaxPoint(),
                           selectedPrefab.getPastePosition(),
                           selectedPrefab.getOriginalFileAnchor(),
                           prefabSaverSettings
                        )
                        .thenAccept(
                           success -> {
                              if (success) {
                                 selectedPrefab.setDirty(false);
                              }

                              context.sendMessage(
                                 Message.translation("server.commands.editprefab.save." + (success ? "success" : "failure")).param("name", savePath.toString())
                              );
                           }
                        );
                  }
               } else {
                  context.sendMessage(Message.translation("server.commands.editprefab.save.selectionMismatch"));
                  return CompletableFuture.completedFuture(null);
               }
            }
         } else {
            PrefabEditingMetadata[] values = prefabEditSession.getLoadedPrefabMetadata().values().toArray(new PrefabEditingMetadata[0]);
            int readOnlyCount = 0;

            for (PrefabEditingMetadata value : values) {
               if (value.isReadOnly()) {
                  readOnlyCount++;
               }
            }

            if (readOnlyCount > 0 && !confirm) {
               context.sendMessage(Message.translation("server.commands.editprefab.save.readOnlyNeedsConfirm").param("count", readOnlyCount));
               return CompletableFuture.completedFuture(null);
            } else {
               if (!SingleplayerModule.isOwner(playerRef)) {
                  for (PrefabEditingMetadata valuex : values) {
                     Path savePath = getWritableSavePath(valuex, confirm);
                     if (!isPathInAllowedPrefabDirectory(savePath)) {
                        context.sendMessage(MESSAGE_PATH_OUTSIDE_PREFABS_DIR);
                        return CompletableFuture.completedFuture(null);
                     }
                  }
               }

               context.sendMessage(Message.translation("server.commands.editprefab.save.saveAll.start").param("amount", values.length));
               CompletableFuture<Boolean>[] prefabSavingFutures = new CompletableFuture[values.length];

               for (int i = 0; i < values.length; i++) {
                  PrefabEditingMetadata valuexx = values[i];
                  Path savePath = getWritableSavePath(valuexx, confirm);
                  prefabSavingFutures[i] = PrefabSaver.savePrefab(
                     playerComponent,
                     world,
                     savePath,
                     valuexx.getAnchorPoint(),
                     valuexx.getMinPoint(),
                     valuexx.getMaxPoint(),
                     valuexx.getPastePosition(),
                     valuexx.getOriginalFileAnchor(),
                     prefabSaverSettings
                  );
               }

               return CompletableFuture.allOf(prefabSavingFutures)
                  .thenAccept(
                     unused -> {
                        List<Integer> failedPrefabFutures = new IntArrayList();

                        for (int i1 = 0; i1 < prefabSavingFutures.length; i1++) {
                           if (prefabSavingFutures[i1].join()) {
                              values[i1].setDirty(false);
                           } else {
                              failedPrefabFutures.add(i1);
                           }
                        }

                        context.sendMessage(
                           Message.translation("server.commands.editprefab.save.saveAll.success")
                              .param("successes", prefabSavingFutures.length - failedPrefabFutures.size())
                              .param("failures", failedPrefabFutures.size())
                        );
                     }
                  );
            }
         }
      }
   }

   @Nonnull
   private static Path getWritableSavePath(@Nonnull PrefabEditingMetadata metadata, boolean confirm) {
      if (metadata.isReadOnly() && confirm) {
         Path originalPath = metadata.getPrefabPath();
         String fileName = originalPath.getFileName().toString();
         Path parent = originalPath.getParent();
         if (parent != null && parent.getFileName() != null) {
            String parentName = parent.getFileName().toString();
            return PrefabStore.get().getServerPrefabsPath().resolve(parentName).resolve(fileName);
         } else {
            return PrefabStore.get().getServerPrefabsPath().resolve(fileName);
         }
      } else {
         return metadata.getPrefabPath();
      }
   }
}
