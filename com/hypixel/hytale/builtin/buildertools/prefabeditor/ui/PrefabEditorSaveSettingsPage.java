package com.hypixel.hytale.builtin.buildertools.prefabeditor.ui;

import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.BuilderToolsUserData;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.PrefabEditSession;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.PrefabEditSessionManager;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.PrefabEditingMetadata;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.saving.PrefabSaver;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.saving.PrefabSaverSettings;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.common.util.StringCompareUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.prefab.PrefabStore;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.browser.AssetPackSaveBrowser;
import com.hypixel.hytale.server.core.ui.browser.AssetPackSaveBrowserConfig;
import com.hypixel.hytale.server.core.ui.browser.AssetPackSaveBrowserEventData;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

public class PrefabEditorSaveSettingsPage extends InteractiveCustomUIPage<PrefabEditorSaveSettingsPage.PageData> {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private static final Value<String> BUTTON_HIGHLIGHTED = Value.ref("Pages/BasicTextButton.ui", "SelectedLabelStyle");
   private static final Value<String> BUTTON_SELECTED = Value.ref("Pages/BasicTextButton.ui", "SelectedLabelStyle");
   @Nonnull
   private final PrefabEditSession prefabEditSession;
   private final AssetPackSaveBrowser packBrowser = new AssetPackSaveBrowser(AssetPackSaveBrowserConfig.defaults());
   private final boolean exitOnSave;
   private volatile boolean isSaving = false;
   private volatile long lastProgressUpdateTime = 0L;
   private static final long PROGRESS_UPDATE_INTERVAL_MS = 100L;
   @Nonnull
   private String browserSearchQuery = "";
   private final Set<UUID> selectedPrefabUuids = new HashSet<>();

   public PrefabEditorSaveSettingsPage(@Nonnull PlayerRef playerRef, @Nonnull PrefabEditSession prefabEditSession) {
      this(playerRef, prefabEditSession, false);
   }

   public PrefabEditorSaveSettingsPage(@Nonnull PlayerRef playerRef, @Nonnull PrefabEditSession prefabEditSession, boolean exitOnSave) {
      super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, PrefabEditorSaveSettingsPage.PageData.CODEC);
      this.prefabEditSession = prefabEditSession;
      this.exitOnSave = exitOnSave;
   }

   @Override
   public void build(
      @Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store
   ) {
      commandBuilder.append("Pages/PrefabEditorSaveSettings.ui");
      if (this.exitOnSave) {
         commandBuilder.set("#MainPage #TitleLabel.Text", Message.translation("server.customUI.prefabEditorSaveSettings.titleSaveAndExit"));
         commandBuilder.set("#MainPage #SaveButton.Text", Message.translation("server.customUI.prefabEditorSaveSettings.saveAndExit"));
      }

      Player playerComponent = store.getComponent(ref, Player.getComponentType());
      PrefabEditingMetadata selectedPrefab = this.prefabEditSession.getSelectedPrefab(this.playerRef.getUuid());
      if (selectedPrefab != null) {
         String prefabPath = this.toDisplayPath(selectedPrefab);
         commandBuilder.set("#MainPage #PrefabsToSave #Input.Value", prefabPath);
         this.selectedPrefabUuids.add(selectedPrefab.getUuid());
      }

      if (playerComponent != null) {
         String defaultPackKey = null;
         if (selectedPrefab != null) {
            AssetPack sourcePack = PrefabStore.get().findAssetPackForPrefabPath(selectedPrefab.getPrefabPath());
            if (sourcePack != null) {
               if (!sourcePack.isImmutable()) {
                  defaultPackKey = sourcePack.getName();
               }
            } else if (!selectedPrefab.isReadOnly()) {
               defaultPackKey = BuilderToolsUserData.get(playerComponent).getLastSavePack();
            }
         } else {
            defaultPackKey = BuilderToolsUserData.get(playerComponent).getLastSavePack();
         }

         this.packBrowser.setSelectedPackKey(defaultPackKey);
         if (defaultPackKey != null && !this.packBrowser.hasSelectedPack()) {
            this.playerRef.sendMessage(Message.translation("server.customUI.assetPackBrowser.packNoLongerAvailable"));
         }
      }

      commandBuilder.set("#MainPage #Entities #CheckBox.Value", true);
      commandBuilder.set("#MainPage #Empty #CheckBox.Value", false);
      commandBuilder.set("#MainPage #Overwrite #CheckBox.Value", true);
      commandBuilder.set("#MainPage #ClearSupport #CheckBox.Value", false);
      if (this.packBrowser.hasSelectedPack()) {
         commandBuilder.set("#MainPage #SelectedPackLabel.Text", this.packBrowser.getSelectedPackDisplayName());
      }

      commandBuilder.set("#PackBrowserPage.Visible", false);
      commandBuilder.set("#CreatePackPage.Visible", false);
      commandBuilder.set("#SavingPage.Visible", false);
      commandBuilder.set("#SavingPage #ProgressBar.Value", 0.0F);
      commandBuilder.set("#SavingPage #StatusText.TextSpans", Message.translation("server.commands.editprefab.save.saving"));
      commandBuilder.set("#SavingPage #ErrorText.Visible", false);
      eventBuilder.addEventBinding(
         CustomUIEventBindingType.Activating,
         "#MainPage #SaveButton",
         new EventData()
            .append("Action", PrefabEditorSaveSettingsPage.Action.Save.name())
            .append("@PrefabsToSave", "#MainPage #PrefabsToSave #Input.Value")
            .append("@Entities", "#MainPage #Entities #CheckBox.Value")
            .append("@Empty", "#MainPage #Empty #CheckBox.Value")
            .append("@Overwrite", "#MainPage #Overwrite #CheckBox.Value")
            .append("@ClearSupport", "#MainPage #ClearSupport #CheckBox.Value"),
         false
      );
      eventBuilder.addEventBinding(
         CustomUIEventBindingType.Activating, "#MainPage #CancelButton", new EventData().append("Action", PrefabEditorSaveSettingsPage.Action.Cancel.name())
      );
      eventBuilder.addEventBinding(
         CustomUIEventBindingType.Activating,
         "#MainPage #PrefabsToSave #BrowseButton",
         new EventData().append("Action", PrefabEditorSaveSettingsPage.Action.OpenBrowser.name())
      );
      eventBuilder.addEventBinding(
         CustomUIEventBindingType.Activating,
         "#MainPage #PrefabsToSave #SelectAllButton",
         new EventData().append("Action", PrefabEditorSaveSettingsPage.Action.SelectAll.name())
      );
      eventBuilder.addEventBinding(
         CustomUIEventBindingType.Activating,
         "#MainPage #PrefabsToSave #SelectEditedButton",
         new EventData().append("Action", PrefabEditorSaveSettingsPage.Action.SelectEdited.name())
      );
      eventBuilder.addEventBinding(
         CustomUIEventBindingType.ValueChanged,
         "#BrowserPage #SearchInput",
         new EventData().append("Action", PrefabEditorSaveSettingsPage.Action.BrowserSearch.name()).append("@BrowserSearch", "#BrowserPage #SearchInput.Value"),
         false
      );
      eventBuilder.addEventBinding(
         CustomUIEventBindingType.Activating,
         "#BrowserPage #BrowserButtons #SelectAllBrowserButton",
         new EventData().append("Action", PrefabEditorSaveSettingsPage.Action.BrowserSelectAll.name())
      );
      eventBuilder.addEventBinding(
         CustomUIEventBindingType.Activating,
         "#BrowserPage #BrowserButtons #ConfirmButton",
         new EventData().append("Action", PrefabEditorSaveSettingsPage.Action.ConfirmBrowser.name())
      );
      eventBuilder.addEventBinding(
         CustomUIEventBindingType.Activating,
         "#BrowserPage #BrowserButtons #CancelButton",
         new EventData().append("Action", PrefabEditorSaveSettingsPage.Action.CancelBrowser.name())
      );
      eventBuilder.addEventBinding(
         CustomUIEventBindingType.Activating,
         "#SavingPage #BackButton",
         new EventData().append("Action", PrefabEditorSaveSettingsPage.Action.BackFromSaving.name())
      );
      this.packBrowser.buildEventBindings(eventBuilder, "#MainPage #BrowsePackButton");
      this.packBrowser.buildUI(commandBuilder, eventBuilder);
      commandBuilder.set("#BrowserPage.Visible", false);
   }

   public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull PrefabEditorSaveSettingsPage.PageData data) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      AssetPackSaveBrowser.ActionResult packResult = this.packBrowser
         .handleAction(data.action != null ? data.action.name() : null, data.packBrowserData, "#MainPage #SelectedPackLabel");
      if (packResult != null) {
         if (packResult.errorKey() != null) {
            this.playerRef.sendMessage(Message.translation(packResult.errorKey()));
         }

         if (packResult.packConfirmed() && this.packBrowser.hasSelectedPack()) {
            BuilderToolsUserData.get(playerComponent).setLastSavePack(this.packBrowser.getSelectedPack().getName());
         }

         this.sendUpdate(packResult.commandBuilder(), packResult.eventBuilder(), false);
      } else {
         switch (data.action) {
            case Save:
               if (this.isSaving) {
                  return;
               }

               if (this.packBrowser.getSelectedPack() == null) {
                  this.playerRef.sendMessage(Message.translation("server.customUI.assetPackBrowser.packRequired"));
                  return;
               }

               if (!AssetModule.get().validatePackExistsOnDisk(this.packBrowser.getSelectedPack())) {
                  this.playerRef.sendMessage(Message.translation("server.customUI.assetPackBrowser.packRequired"));
                  return;
               }

               if (playerComponent != null) {
                  BuilderToolsUserData.get(playerComponent).setLastSavePack(this.packBrowser.getSelectedPack().getName());
               }

               String prefabsToSaveStr = data.prefabsToSave;
               if (prefabsToSaveStr == null || prefabsToSaveStr.isBlank()) {
                  this.playerRef.sendMessage(Message.translation("server.commands.editprefab.save.noPrefabsSelected"));
                  return;
               }

               this.isSaving = true;
               UICommandBuilder showSavingBuilder = new UICommandBuilder();
               showSavingBuilder.set("#MainPage.Visible", false);
               showSavingBuilder.set("#BrowserPage.Visible", false);
               showSavingBuilder.set("#SavingPage.Visible", true);
               showSavingBuilder.set("#SavingPage #ProgressBar.Value", 0.0F);
               showSavingBuilder.set("#SavingPage #StatusText.TextSpans", Message.translation("server.commands.editprefab.save.saving"));
               showSavingBuilder.set("#SavingPage #ErrorText.Visible", false);
               showSavingBuilder.set("#SavingPage #BackButton.Visible", false);
               this.sendUpdate(showSavingBuilder);
               PrefabSaverSettings prefabSaverSettings = new PrefabSaverSettings();
               prefabSaverSettings.setBlocks(true);
               prefabSaverSettings.setEntities(data.entities);
               prefabSaverSettings.setEmpty(data.empty);
               prefabSaverSettings.setOverwriteExisting(data.overwrite);
               prefabSaverSettings.setClearSupportValues(data.clearSupport);
               String[] prefabPaths = prefabsToSaveStr.split("[,\\n]");
               List<PrefabEditingMetadata> prefabsToSave = new ObjectArrayList();

               for (String pathStr : prefabPaths) {
                  pathStr = pathStr.trim();
                  if (!pathStr.isEmpty()) {
                     for (PrefabEditingMetadata metadatax : this.prefabEditSession.getLoadedPrefabMetadata().values()) {
                        String prefabPath = metadatax.getPrefabPath().toString().replace('\\', '/');
                        if (prefabPath.equals(pathStr) || prefabPath.endsWith(pathStr)) {
                           prefabsToSave.add(metadatax);
                           break;
                        }
                     }
                  }
               }

               if (prefabsToSave.isEmpty()) {
                  this.onSavingFailed(Message.translation("server.commands.editprefab.save.noPrefabsFound"));
                  return;
               }

               int readOnlyCount = 0;

               for (PrefabEditingMetadata metadataxx : prefabsToSave) {
                  if (metadataxx.isReadOnly()) {
                     readOnlyCount++;
                  }
               }

               if (readOnlyCount > 0) {
                  this.playerRef.sendMessage(Message.translation("server.commands.editprefab.save.readOnlyRedirect").param("count", readOnlyCount));
               }

               World world = store.getExternalData().getWorld();
               PrefabEditSessionManager editSessionManager = BuilderToolsPlugin.get().getPrefabEditSessionManager();
               World editSessionWorld = Universe.get().getWorld(editSessionManager.getPrefabEditSession(this.playerRef.getUuid()).getWorldName());
               int totalPrefabs = prefabsToSave.size();
               CompletableFuture<Boolean>[] saveFutures = new CompletableFuture[totalPrefabs];
               AtomicInteger completedCount = new AtomicInteger(0);
               this.lastProgressUpdateTime = 0L;

               for (int i = 0; i < totalPrefabs; i++) {
                  PrefabEditingMetadata metadataxxx = prefabsToSave.get(i);
                  Path savePath = this.getWritableSavePath(metadataxxx);
                  saveFutures[i] = PrefabSaver.savePrefab(
                        playerComponent,
                        editSessionWorld,
                        savePath,
                        metadataxxx.getAnchorPoint(),
                        metadataxxx.getMinPoint(),
                        metadataxxx.getMaxPoint(),
                        metadataxxx.getPastePosition(),
                        metadataxxx.getOriginalFileAnchor(),
                        prefabSaverSettings
                     )
                     .thenApply(
                        success -> {
                           int completed = completedCount.incrementAndGet();
                           long now = System.currentTimeMillis();
                           if (now - this.lastProgressUpdateTime >= 100L || completed == totalPrefabs) {
                              this.lastProgressUpdateTime = now;
                              float progress = (float)completed / totalPrefabs;
                              UICommandBuilder progressBuilder = new UICommandBuilder();
                              progressBuilder.set("#SavingPage #ProgressBar.Value", progress);
                              progressBuilder.set(
                                 "#SavingPage #StatusText.TextSpans",
                                 Message.translation("server.commands.editprefab.save.progress").param("current", completed).param("total", totalPrefabs)
                              );
                              this.sendUpdate(progressBuilder);
                           }

                           return (Boolean)success;
                        }
                     );
               }

               CompletableFuture.allOf(saveFutures)
                  .thenAccept(
                     unused -> {
                        int successes = 0;
                        int failures = 0;

                        for (CompletableFuture<Boolean> future : saveFutures) {
                           if (future.join()) {
                              successes++;
                           } else {
                              failures++;
                           }
                        }

                        this.isSaving = false;
                        if (failures == 0) {
                           this.playerRef
                              .sendMessage(
                                 Message.translation("server.commands.editprefab.save.saveAll.success")
                                    .param("successes", successes)
                                    .param("failures", failures)
                              );
                           playerComponent.getPageManager().setPage(ref, store, Page.None);
                           if (this.exitOnSave) {
                              editSessionManager.exitEditSession(ref, world, this.playerRef, store);
                           }
                        } else {
                           this.onSavingFailed(
                              Message.translation("server.commands.editprefab.save.saveAll.success").param("successes", successes).param("failures", failures)
                           );
                        }
                     }
                  )
                  .exceptionally(throwable -> {
                     this.isSaving = false;
                     ((HytaleLogger.Api)((HytaleLogger.Api)LOGGER.atWarning()).withCause(throwable)).log("Error saving prefabs");
                     this.onSavingFailed(Message.raw(throwable.getMessage() != null ? throwable.getMessage() : "Unknown error"));
                     return null;
                  });
               break;
            case Cancel:
               playerComponent.getPageManager().setPage(ref, store, Page.None);
               break;
            case SelectAll: {
               String allPaths = this.prefabEditSession.getLoadedPrefabMetadata().values().stream().map(this::toDisplayPath).collect(Collectors.joining("\n"));
               UICommandBuilder commandBuilder = new UICommandBuilder();
               commandBuilder.set("#MainPage #PrefabsToSave #Input.Value", allPaths);
               this.sendUpdate(commandBuilder);
               break;
            }
            case SelectEdited: {
               String editedPaths = this.prefabEditSession
                  .getLoadedPrefabMetadata()
                  .values()
                  .stream()
                  .filter(PrefabEditingMetadata::isDirty)
                  .map(this::toDisplayPath)
                  .collect(Collectors.joining("\n"));
               UICommandBuilder commandBuilder = new UICommandBuilder();
               commandBuilder.set("#MainPage #PrefabsToSave #Input.Value", editedPaths);
               this.sendUpdate(commandBuilder);
               if (editedPaths.isEmpty()) {
                  this.playerRef.sendMessage(Message.translation("server.commands.editprefab.save.noEditedPrefabs"));
               }
               break;
            }
            case OpenBrowser: {
               this.browserSearchQuery = "";
               this.selectedPrefabUuids.clear();
               UICommandBuilder commandBuilder = new UICommandBuilder();
               UIEventBuilder eventBuilder = new UIEventBuilder();
               commandBuilder.set("#MainPage.Visible", false);
               commandBuilder.set("#BrowserPage.Visible", true);
               commandBuilder.set("#BrowserPage #SearchInput.Value", "");
               this.buildPrefabList(commandBuilder, eventBuilder);
               this.sendUpdate(commandBuilder, eventBuilder, false);
               break;
            }
            case BrowserSearch: {
               this.browserSearchQuery = data.browserSearchStr != null ? data.browserSearchStr.trim().toLowerCase() : "";
               UICommandBuilder commandBuilder = new UICommandBuilder();
               UIEventBuilder eventBuilder = new UIEventBuilder();
               this.buildPrefabList(commandBuilder, eventBuilder);
               this.sendUpdate(commandBuilder, eventBuilder, false);
               break;
            }
            case BrowserTogglePrefab:
               if (data.prefabUuid != null) {
                  try {
                     UUID uuid = UUID.fromString(data.prefabUuid);
                     if (this.selectedPrefabUuids.contains(uuid)) {
                        this.selectedPrefabUuids.remove(uuid);
                     } else {
                        this.selectedPrefabUuids.add(uuid);
                     }

                     UICommandBuilder commandBuilderx = new UICommandBuilder();
                     UIEventBuilder eventBuilderx = new UIEventBuilder();
                     this.buildPrefabList(commandBuilderx, eventBuilderx);
                     this.sendUpdate(commandBuilderx, eventBuilderx, false);
                  } catch (IllegalArgumentException var21) {
                  }
               }
               break;
            case BrowserSelectAll: {
               this.selectedPrefabUuids.clear();

               for (PrefabEditingMetadata metadatax : this.prefabEditSession.getLoadedPrefabMetadata().values()) {
                  this.selectedPrefabUuids.add(metadatax.getUuid());
               }

               UICommandBuilder commandBuilder = new UICommandBuilder();
               UIEventBuilder eventBuilder = new UIEventBuilder();
               this.buildPrefabList(commandBuilder, eventBuilder);
               this.sendUpdate(commandBuilder, eventBuilder, false);
               break;
            }
            case ConfirmBrowser: {
               List<String> selectedPaths = new ObjectArrayList();

               for (PrefabEditingMetadata metadata : this.prefabEditSession.getLoadedPrefabMetadata().values()) {
                  if (this.selectedPrefabUuids.contains(metadata.getUuid())) {
                     selectedPaths.add(this.toDisplayPath(metadata));
                  }
               }

               UICommandBuilder commandBuilder = new UICommandBuilder();
               commandBuilder.set("#MainPage #PrefabsToSave #Input.Value", String.join("\n", selectedPaths));
               commandBuilder.set("#BrowserPage.Visible", false);
               commandBuilder.set("#MainPage.Visible", true);
               this.sendUpdate(commandBuilder);
               break;
            }
            case CancelBrowser: {
               UICommandBuilder commandBuilder = new UICommandBuilder();
               commandBuilder.set("#BrowserPage.Visible", false);
               commandBuilder.set("#MainPage.Visible", true);
               this.sendUpdate(commandBuilder);
               break;
            }
            case BackFromSaving: {
               this.isSaving = false;
               UICommandBuilder commandBuilder = new UICommandBuilder();
               commandBuilder.set("#SavingPage.Visible", false);
               commandBuilder.set("#MainPage.Visible", true);
               this.sendUpdate(commandBuilder);
            }
            case OpenPackBrowser:
            case ConfirmPackBrowser:
            case CancelPackBrowser:
            case OpenCreatePack:
            case CreatePack:
            case CancelCreatePack:
            case PackSearch:
            case PackSelect:
         }
      }
   }

   private void buildPrefabList(@Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder) {
      commandBuilder.clear("#BrowserPage #FileList");
      Map<UUID, PrefabEditingMetadata> loadedPrefabs = this.prefabEditSession.getLoadedPrefabMetadata();
      if (!loadedPrefabs.isEmpty()) {
         List<PrefabEditingMetadata> prefabsToDisplay;
         if (!this.browserSearchQuery.isEmpty()) {
            Object2IntMap<PrefabEditingMetadata> matchScores = new Object2IntOpenHashMap(loadedPrefabs.size());

            for (PrefabEditingMetadata metadata : loadedPrefabs.values()) {
               String fileName = metadata.getPrefabPath().getFileName().toString();
               String baseName = fileName.endsWith(".prefab.json") ? fileName.substring(0, fileName.length() - ".prefab.json".length()) : fileName;
               int fuzzyDistance = StringCompareUtil.getFuzzyDistance(baseName, this.browserSearchQuery, Locale.ENGLISH);
               if (fuzzyDistance > 0) {
                  matchScores.put(metadata, fuzzyDistance);
               }
            }

            prefabsToDisplay = matchScores.keySet().stream().sorted(Comparator.comparingInt(matchScores::getInt).reversed()).collect(Collectors.toList());
         } else {
            prefabsToDisplay = loadedPrefabs.values()
               .stream()
               .sorted(Comparator.comparing(m -> m.getPrefabPath().getFileName().toString().toLowerCase()))
               .collect(Collectors.toList());
         }

         int buttonIndex = 0;

         for (PrefabEditingMetadata metadatax : prefabsToDisplay) {
            Path prefabPath = metadatax.getPrefabPath();
            String fileName = prefabPath.getFileName().toString();
            String displayName = fileName.endsWith(".prefab.json") ? fileName.substring(0, fileName.length() - ".prefab.json".length()) : fileName;
            boolean isSelected = this.selectedPrefabUuids.contains(metadatax.getUuid());
            boolean isReadOnly = metadatax.isReadOnly();
            String checkPrefix = isSelected ? "[x] " : "[ ] ";
            String relativePath = prefabPath.toString().replace('\\', '/');
            String tooltipText = isReadOnly ? relativePath + "\n(Will save to Server/Prefabs)" : relativePath;
            commandBuilder.append("#BrowserPage #FileList", "Pages/BasicTextButton.ui");
            String fileSelector = "#BrowserPage #FileList[" + buttonIndex + "]";
            if (isReadOnly) {
               commandBuilder.set(
                  fileSelector + ".Text", Message.raw(checkPrefix + displayName + " ").insert(Message.translation("server.customUI.assetPackBrowser.readOnly"))
               );
            } else {
               commandBuilder.set(fileSelector + ".Text", checkPrefix + displayName);
            }

            commandBuilder.set(fileSelector + ".TooltipText", tooltipText);
            if (isSelected) {
               commandBuilder.set(fileSelector + ".Style", BUTTON_SELECTED);
            }

            eventBuilder.addEventBinding(
               CustomUIEventBindingType.Activating,
               fileSelector,
               new EventData()
                  .append("Action", PrefabEditorSaveSettingsPage.Action.BrowserTogglePrefab.name())
                  .append("PrefabUuid", metadatax.getUuid().toString())
            );
            buttonIndex++;
         }
      }
   }

   @Nonnull
   private String toDisplayPath(@Nonnull PrefabEditingMetadata metadata) {
      return PrefabStore.get().getRelativePrefabPath(metadata.getPrefabPath()).toString().replace('\\', '/');
   }

   @Nonnull
   private Path getWritableSavePath(@Nonnull PrefabEditingMetadata metadata) {
      AssetPack selectedPack = this.packBrowser.getSelectedPack();

      assert selectedPack != null;

      PrefabStore prefabStore = PrefabStore.get();
      Path packPrefabsPath = prefabStore.getAssetPrefabsPathForPack(selectedPack);
      return packPrefabsPath.resolve(prefabStore.getRelativePrefabPath(metadata.getPrefabPath()));
   }

   private void onSavingFailed(@Nonnull Message errorMessage) {
      this.isSaving = false;
      UICommandBuilder builder = new UICommandBuilder();
      builder.set("#SavingPage #ProgressBar.Value", 0.0F);
      builder.set("#SavingPage #StatusText.TextSpans", Message.translation("server.commands.editprefab.save.error"));
      builder.set("#SavingPage #ErrorText.Visible", true);
      builder.set("#SavingPage #ErrorText.TextSpans", errorMessage);
      builder.set("#SavingPage #BackButton.Visible", true);
      this.sendUpdate(builder);
   }

   public static enum Action {
      Save,
      Cancel,
      SelectAll,
      SelectEdited,
      OpenBrowser,
      BrowserSearch,
      BrowserTogglePrefab,
      BrowserSelectAll,
      ConfirmBrowser,
      CancelBrowser,
      BackFromSaving,
      OpenPackBrowser,
      ConfirmPackBrowser,
      CancelPackBrowser,
      OpenCreatePack,
      CreatePack,
      CancelCreatePack,
      PackSearch,
      PackSelect;
   }

   protected static class PageData {
      public static final String PREFABS_TO_SAVE = "@PrefabsToSave";
      public static final String ENTITIES = "@Entities";
      public static final String EMPTY = "@Empty";
      public static final String OVERWRITE = "@Overwrite";
      public static final String CLEAR_SUPPORT = "@ClearSupport";
      public static final String BROWSER_SEARCH = "@BrowserSearch";
      public static final String PREFAB_UUID = "PrefabUuid";
      public static final BuilderCodec<PrefabEditorSaveSettingsPage.PageData> CODEC = BuilderCodec.builder(
            PrefabEditorSaveSettingsPage.PageData.class, PrefabEditorSaveSettingsPage.PageData::new
         )
         .append(
            new KeyedCodec<>("Action", new EnumCodec<>(PrefabEditorSaveSettingsPage.Action.class, EnumCodec.EnumStyle.LEGACY)),
            (o, action) -> o.action = action,
            o -> o.action
         )
         .add()
         .append(new KeyedCodec<>("@PrefabsToSave", Codec.STRING), (o, prefabsToSave) -> o.prefabsToSave = prefabsToSave, o -> o.prefabsToSave)
         .add()
         .append(new KeyedCodec<>("@Entities", Codec.BOOLEAN), (o, entities) -> o.entities = entities, o -> o.entities)
         .add()
         .append(new KeyedCodec<>("@Empty", Codec.BOOLEAN), (o, empty) -> o.empty = empty, o -> o.empty)
         .add()
         .append(new KeyedCodec<>("@Overwrite", Codec.BOOLEAN), (o, overwrite) -> o.overwrite = overwrite, o -> o.overwrite)
         .add()
         .append(new KeyedCodec<>("@ClearSupport", Codec.BOOLEAN), (o, clearSupport) -> o.clearSupport = clearSupport, o -> o.clearSupport)
         .add()
         .append(new KeyedCodec<>("@BrowserSearch", Codec.STRING), (o, browserSearchStr) -> o.browserSearchStr = browserSearchStr, o -> o.browserSearchStr)
         .add()
         .append(new KeyedCodec<>("PrefabUuid", Codec.STRING), (o, prefabUuid) -> o.prefabUuid = prefabUuid, o -> o.prefabUuid)
         .add()
         .append(new KeyedCodec<>("Pack", Codec.STRING), (o, s) -> o.packBrowserData.pack = s, o -> o.packBrowserData.pack)
         .add()
         .append(new KeyedCodec<>("@PackSearch", Codec.STRING), (o, s) -> o.packBrowserData.search = s, o -> o.packBrowserData.search)
         .add()
         .append(new KeyedCodec<>("@CreateName", Codec.STRING), (o, s) -> o.packBrowserData.createName = s, o -> o.packBrowserData.createName)
         .add()
         .append(new KeyedCodec<>("@CreateGroup", Codec.STRING), (o, s) -> o.packBrowserData.createGroup = s, o -> o.packBrowserData.createGroup)
         .add()
         .append(
            new KeyedCodec<>("@CreateDescription", Codec.STRING), (o, s) -> o.packBrowserData.createDescription = s, o -> o.packBrowserData.createDescription
         )
         .add()
         .append(new KeyedCodec<>("@CreateVersion", Codec.STRING), (o, s) -> o.packBrowserData.createVersion = s, o -> o.packBrowserData.createVersion)
         .add()
         .append(new KeyedCodec<>("@CreateWebsite", Codec.STRING), (o, s) -> o.packBrowserData.createWebsite = s, o -> o.packBrowserData.createWebsite)
         .add()
         .append(new KeyedCodec<>("@CreateAuthorName", Codec.STRING), (o, s) -> o.packBrowserData.createAuthorName = s, o -> o.packBrowserData.createAuthorName)
         .add()
         .append(new KeyedCodec<>("ValidateCreate", Codec.STRING), (o, s) -> o.packBrowserData.validateCreate = s, o -> o.packBrowserData.validateCreate)
         .add()
         .build();
      public PrefabEditorSaveSettingsPage.Action action;
      public String prefabsToSave;
      public boolean entities = true;
      public boolean empty = false;
      public boolean overwrite = true;
      public boolean clearSupport = false;
      public String browserSearchStr;
      public String prefabUuid;
      public final AssetPackSaveBrowserEventData packBrowserData = new AssetPackSaveBrowserEventData();

      public PageData() {
      }
   }
}
