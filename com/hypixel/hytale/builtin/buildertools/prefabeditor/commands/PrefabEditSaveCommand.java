package com.hypixel.hytale.builtin.buildertools.prefabeditor.commands;

import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.BuilderToolsUserData;
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
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
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
import javax.annotation.Nullable;

public class PrefabEditSaveCommand extends AbstractAsyncPlayerCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_EDIT_PREFAB_NOT_IN_EDIT_SESSION = Message.translation("server.commands.editprefab.notInEditSession");
   @Nonnull
   private static final Message MESSAGE_PATH_OUTSIDE_PREFABS_DIR = Message.translation("server.builderTools.attemptedToSaveOutsidePrefabsDir");
   @Nonnull
   private static final Message MESSAGE_NOT_IN_EDIT_WORLD = Message.translation("server.commands.editprefab.notInEditWorldWarning");
   @Nonnull
   private final FlagArg saveAllArg = this.withFlagArg("saveAll", "server.commands.editprefab.save.saveAll.desc").addAliases("all");
   @Nonnull
   private final DefaultArg<String> packArg = this.withDefaultArg(
      "pack", "server.commands.editprefab.save.pack.desc", ArgTypes.STRING, "", "server.commands.editprefab.save.pack.desc"
   );
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
      } else if (!prefabEditSessionManager.isInEditWorld(playerRef, store)) {
         context.sendMessage(MESSAGE_NOT_IN_EDIT_WORLD);
         return CompletableFuture.completedFuture(null);
      } else {
         PrefabSaverSettings prefabSaverSettings = new PrefabSaverSettings();
         prefabSaverSettings.setBlocks(true);
         prefabSaverSettings.setEntities(!this.noEntitiesArg.provided(context));
         prefabSaverSettings.setOverwriteExisting(true);
         prefabSaverSettings.setEmpty(this.emptyArg.get(context));
         prefabSaverSettings.setClearSupportValues(this.clearSupportArg.get(context));
         boolean confirm = this.confirmArg.provided(context);
         String packName = this.packArg.get(context);
         AssetPack targetPack = null;
         boolean packExplicit;
         if (packName != null && !packName.isEmpty()) {
            targetPack = BuilderToolsPlugin.resolveTargetPack(packName, playerComponent, context);
            if (targetPack == null) {
               return CompletableFuture.completedFuture(null);
            }

            packExplicit = true;
         } else {
            packExplicit = false;
         }

         if (!this.saveAllArg.provided(context)) {
            PrefabEditingMetadata selectedPrefab = prefabEditSession.getSelectedPrefab(playerRef.getUuid());
            if (selectedPrefab == null) {
               context.sendMessage(Message.translation("server.commands.editprefab.noPrefabSelected"));
               return CompletableFuture.completedFuture(null);
            } else {
               if (selectedPrefab.isReadOnly() && !packExplicit) {
                  targetPack = BuilderToolsPlugin.resolveTargetPack("", selectedPrefab.getPrefabPath(), playerComponent, context);
                  if (targetPack == null) {
                     return CompletableFuture.completedFuture(null);
                  }

                  if (!confirm) {
                     Path redirectPath = getWritableSavePath(selectedPrefab, targetPack);
                     context.sendMessage(
                        Message.translation("server.commands.editprefab.save.readOnlyNeedsConfirmSingle")
                           .param("path", selectedPrefab.getPrefabPath().toString())
                           .param("redirectPath", redirectPath.toString())
                     );
                     return CompletableFuture.completedFuture(null);
                  }
               }

               BlockSelection selection = BuilderToolsPlugin.getState(playerComponent, playerRef).getSelection();
               if (selectedPrefab.getMinPoint().equals(selection.getSelectionMin()) && selectedPrefab.getMaxPoint().equals(selection.getSelectionMax())) {
                  if (targetPack != null) {
                     BuilderToolsUserData.get(playerComponent).setLastSavePack(targetPack.getName());
                  }

                  Path savePath = getWritableSavePath(selectedPrefab, targetPack);
                  if (!SingleplayerModule.isOwner(playerRef) && !isPathInAllowedPrefabDirectory(savePath)) {
                     context.sendMessage(MESSAGE_PATH_OUTSIDE_PREFABS_DIR);
                     return CompletableFuture.completedFuture(null);
                  } else {
                     AssetPack packForMessage = targetPack;
                     if (targetPack == null) {
                        packForMessage = PrefabStore.get().findAssetPackForPrefabPath(savePath);
                     }

                     AssetPack resolvedPack = packForMessage;
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
                        .thenAccept(success -> {
                           if (success) {
                              selectedPrefab.setDirty(false);
                           }

                           String keySuffix = (success ? "success" : "failure") + (resolvedPack != null ? ".pack" : "");
                           Message msg = Message.translation("server.commands.editprefab.save." + keySuffix).param("name", savePath.toString());
                           if (resolvedPack != null) {
                              msg = msg.param("pack", resolvedPack.getName());
                           }

                           context.sendMessage(msg);
                        });
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

            if (readOnlyCount > 0 && !packExplicit) {
               if (targetPack == null) {
                  targetPack = BuilderToolsPlugin.resolveTargetPack("", playerComponent, context);
                  if (targetPack == null) {
                     return CompletableFuture.completedFuture(null);
                  }
               }

               if (!confirm) {
                  context.sendMessage(Message.translation("server.commands.editprefab.save.readOnlyNeedsConfirm").param("count", readOnlyCount));
                  return CompletableFuture.completedFuture(null);
               }
            }

            if (targetPack != null) {
               BuilderToolsUserData.get(playerComponent).setLastSavePack(targetPack.getName());
            }

            if (!SingleplayerModule.isOwner(playerRef)) {
               for (PrefabEditingMetadata valuex : values) {
                  Path savePath = getWritableSavePath(valuex, targetPack);
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
               Path savePath = getWritableSavePath(valuexx, targetPack);
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

   @Nonnull
   private static Path getWritableSavePath(@Nonnull PrefabEditingMetadata metadata, @Nullable AssetPack targetPack) {
      if (targetPack != null) {
         PrefabStore prefabStore = PrefabStore.get();
         Path packPrefabsPath = prefabStore.getAssetPrefabsPathForPack(targetPack);
         return packPrefabsPath.resolve(prefabStore.getRelativePrefabPath(metadata.getPrefabPath()));
      } else {
         return metadata.getPrefabPath();
      }
   }
}
