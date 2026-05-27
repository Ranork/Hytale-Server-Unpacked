package com.hypixel.hytale.builtin.triggervolumes.ui;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.builtin.triggervolumes.EntityTargetType;
import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.builtin.triggervolumes.asset.TriggerEffectAsset;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerCondition;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEventType;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.PastePrefabEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.TaggedVolumeEffectUtil;
import com.hypixel.hytale.builtin.triggervolumes.manager.ConditionTiming;
import com.hypixel.hytale.builtin.triggervolumes.manager.CooldownMode;
import com.hypixel.hytale.builtin.triggervolumes.manager.GroupEntry;
import com.hypixel.hytale.builtin.triggervolumes.manager.ProjectileSource;
import com.hypixel.hytale.builtin.triggervolumes.manager.RejectionDelayMode;
import com.hypixel.hytale.builtin.triggervolumes.manager.TriggerVolumeManager;
import com.hypixel.hytale.builtin.triggervolumes.manager.VolumeEntry;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.EmptyExtraInfo;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.builder.BuilderField;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.common.util.StringCompareUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.EditorBlocksChange;
import com.hypixel.hytale.protocol.packets.player.HideTriggerVolumePastePrefabPreview;
import com.hypixel.hytale.protocol.packets.player.ShowTriggerVolumePastePrefabPreview;
import com.hypixel.hytale.protocol.packets.player.TriggerVolumeShapeType;
import com.hypixel.hytale.protocol.packets.player.TriggerVolumeToolSelection;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.hypixel.hytale.server.core.asset.type.buildertool.config.PrefabListAsset;
import com.hypixel.hytale.server.core.asset.type.environment.config.Environment;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.asset.util.ColorParseUtil;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.i18n.I18nModule;
import com.hypixel.hytale.server.core.prefab.PrefabStore;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;
import com.hypixel.hytale.server.core.ui.LocalizableString;
import com.hypixel.hytale.server.core.ui.PatchStyle;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.BsonUtil;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonArray;
import org.bson.BsonBoolean;
import org.bson.BsonDocument;
import org.bson.BsonDouble;
import org.bson.BsonInt32;
import org.bson.BsonInt64;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.joml.Vector3d;
import org.joml.Vector3f;

public class TriggerVolumeInspectorPage extends InteractiveCustomUIPage<TriggerVolumeInspectorPage.PageData> {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private static final Pattern VALID_ID = Pattern.compile("^[a-zA-Z0-9_]{1,64}$");
   private static final int ASSET_PICKER_MAX_RESULTS = 50;
   private static final Value<String> NORMAL_ROW_STYLE = Value.ref("Pages/TriggerVolume/TriggerVolumeBrowseGroupRow.ui", "NormalRowStyle");
   private static final Value<String> SELECTED_ROW_STYLE = Value.ref("Pages/TriggerVolume/TriggerVolumeBrowseGroupRow.ui", "SelectedRowStyle");
   private static final Value<String> NORMAL_EFFECT_ROW_STYLE = Value.ref("Pages/TriggerVolume/TriggerVolumeInspectorEffectRow.ui", "NormalRowStyle");
   private static final Value<String> SELECTED_EFFECT_ROW_STYLE = Value.ref("Pages/TriggerVolume/TriggerVolumeInspectorEffectRow.ui", "SelectedRowStyle");
   private static final Value<String> INHERITED_EFFECT_ROW_STYLE = Value.ref("Pages/TriggerVolume/TriggerVolumeInspectorEffectRow.ui", "InheritedRowStyle");
   private static final Value<String> NORMAL_EFFECT_LABEL_STYLE = Value.ref("Pages/TriggerVolume/TriggerVolumeInspectorEffectRow.ui", "NormalLabelStyle");
   private static final Value<String> SELECTED_EFFECT_LABEL_STYLE = Value.ref("Pages/TriggerVolume/TriggerVolumeInspectorEffectRow.ui", "SelectedLabelStyle");
   private static final Value<String> INHERITED_EFFECT_LABEL_STYLE = Value.ref("Pages/TriggerVolume/TriggerVolumeInspectorEffectRow.ui", "InheritedLabelStyle");
   private static final String PAGE = "Pages/TriggerVolume/TriggerVolumeInspectorPage.ui";
   private static final String GROUP_ROW = "Pages/TriggerVolume/TriggerVolumeBrowseGroupRow.ui";
   private static final String VOLUME_ROW = "Pages/TriggerVolume/TriggerVolumeBrowseVolumeRow.ui";
   private static final String TAG_ROW = "Pages/TriggerVolume/TriggerVolumeBrowseTagRow.ui";
   private static final String PROPERTY_ROW = "Pages/TriggerVolume/TriggerVolumeBrowsePropertyRow.ui";
   private static final String TAB_BUTTON = "Pages/TriggerVolume/TriggerVolumeInspectorTabButton.ui";
   private static final String EFFECT_ROW = "Pages/TriggerVolume/TriggerVolumeInspectorEffectRow.ui";
   private static final String EVENT_CATEGORY_HEADER = "Pages/TriggerVolume/TriggerVolumeInspectorEventCategoryHeader.ui";
   private static final String EVENT_SECTION_LABEL = "Pages/TriggerVolume/TriggerVolumeInspectorEventSectionLabel.ui";
   private static final String EVENT_CATEGORY_SPACER = "Pages/TriggerVolume/TriggerVolumeInspectorEventCategorySpacer.ui";
   private static final String SECTION_LABEL = "Pages/TriggerVolume/TriggerVolumeInspectorSectionLabel.ui";
   private static final String VOLUME_SECTION_LABEL = "Pages/TriggerVolume/TriggerVolumeInspectorVolumeSectionLabel.ui";
   private static final String EFFECT_OWNER_SECTION_LABEL = "Pages/TriggerVolume/TriggerVolumeInspectorEffectOwnerSectionLabel.ui";
   private static final String COMMON_TEXT_BUTTON_DOCUMENT = "Common/TextButton.ui";
   private static final String FIELD_TEXT = "Pages/TriggerVolume/TriggerVolumeInspectorTextRow.ui";
   private static final String FIELD_COLOR = "Pages/TriggerVolume/TriggerVolumeInspectorColorRow.ui";
   private static final String FIELD_NUMBER = "Pages/TriggerVolume/TriggerVolumeInspectorNumberRow.ui";
   private static final String FIELD_INT = "Pages/TriggerVolume/TriggerVolumeInspectorIntRow.ui";
   private static final String FIELD_CHECKBOX = "Pages/TriggerVolume/TriggerVolumeInspectorCheckboxRow.ui";
   private static final String FIELD_DROPDOWN = "Pages/TriggerVolume/TriggerVolumeInspectorDropdownRow.ui";
   private static final String FIELD_VEC3 = "Pages/TriggerVolume/TriggerVolumeInspectorVec3Row.ui";
   private static final String FIELD_DIMENSIONS_BOX = "Pages/TriggerVolume/TriggerVolumeInspectorDimensionsBoxRow.ui";
   private static final String FIELD_DIMENSIONS_SPHERE = "Pages/TriggerVolume/TriggerVolumeInspectorDimensionsSphereRow.ui";
   private static final String FIELD_DIMENSIONS_CYLINDER = "Pages/TriggerVolume/TriggerVolumeInspectorDimensionsCylinderRow.ui";
   private static final String FIELD_ASSET_PICKER = "Pages/TriggerVolume/TriggerVolumeInspectorAssetPickerRow.ui";
   private static final String SOUND_ASSET_PICKER_ROW = "Pages/TriggerVolume/TriggerVolumeInspectorSoundAssetRow.ui";
   private static final Set<String> NON_NEGATIVE_NUMERIC_FIELDS = Set.of(
      "CooldownCondition.Cooldown",
      "ItemCondition.Quantity",
      "PlayerCountCondition.Count",
      "GiveItem.Quantity",
      "TriggerNpcMarkers.Range",
      "TriggerNpcMarkers.Radius",
      "ModifyTags.Radius",
      "EnableVolume.Radius",
      "DisableVolume.Radius",
      "DeleteVolume.Radius",
      "TagCondition.Radius",
      "TagCondition.MinimumCount"
   );
   private static final Map<String, BsonValue> DEFAULT_FIELD_VALUES = Map.of(
      "ModifyTags.TagValue",
      new BsonString("Empty"),
      "ModifyTags.Radius",
      new BsonDouble(50.0),
      "EnableVolume.Radius",
      new BsonDouble(50.0),
      "DisableVolume.Radius",
      new BsonDouble(50.0),
      "DeleteVolume.Radius",
      new BsonDouble(50.0),
      "TagCondition.Radius",
      new BsonDouble(50.0)
   );
   private static final Color DEFAULT_PREFAB_BIOME_TINT = new Color((byte)91, (byte)-98, (byte)40);
   private static final int DEFAULT_BIOME_TINT = ColorParseUtil.colorToARGBInt(DEFAULT_PREFAB_BIOME_TINT) & 16777215;
   private static final int DEFAULT_WATER_TINT = ColorParseUtil.colorToARGBInt(Environment.getUnknownFor("").getWaterTint()) & 16777215;
   @Nonnull
   private String selectedWorld;
   @Nullable
   private String selectedId;
   private boolean selectedIsGroup;
   @Nonnull
   private TriggerVolumeInspectorPage.InspectorTab selectedTab;
   @Nonnull
   private String filterText = "";
   private final Map<String, TriggerVolumeInspectorDrafts.VolumeDraft> volumeDrafts = new LinkedHashMap<>();
   private final Map<String, TriggerVolumeInspectorDrafts.GroupDraft> groupDrafts = new LinkedHashMap<>();
   private final Set<String> deletedVolumes = new LinkedHashSet<>();
   private final Set<String> deletedGroups = new LinkedHashSet<>();
   private final List<TriggerVolumeInspectorPage.RowEntry> currentRows = new ArrayList<>();
   private final TriggerVolumeManager.SelectionObserver selectionObserver = this::onExternalSelectionChanged;
   private final TriggerVolumeManager.VolumeUpdateObserver volumeUpdateObserver = new TriggerVolumeManager.VolumeUpdateObserver() {
      {
         Objects.requireNonNull(TriggerVolumeInspectorPage.this);
      }

      @Override
      public void onVolumeUpdated(@Nonnull VolumeEntry volume) {
         TriggerVolumeInspectorPage.this.onExternalVolumeUpdated(volume);
      }

      @Override
      public void onVolumeRemoved(@Nonnull String volumeId) {
         TriggerVolumeInspectorPage.this.onExternalVolumeRemoved(volumeId);
      }
   };
   @Nullable
   private final String preSelectedVolumeId;
   private final boolean preSelectedIsGroup;
   @Nonnull
   private TriggerVolumeInspectorPage.EffectListKind selectedKind = TriggerVolumeInspectorPage.EffectListKind.EFFECT;
   @Nonnull
   private TriggerVolumeInspectorPage.EffectListKind addTargetKind = TriggerVolumeInspectorPage.EffectListKind.EFFECT;
   @Nonnull
   private TriggerEventType addEventType = TriggerEventType.ENTER;
   private int selectedEffectIndex = -1;
   @Nonnull
   private final EnumSet<TriggerEventType> collapsedVolumeEventCategories = EnumSet.noneOf(TriggerEventType.class);
   @Nonnull
   private final EnumSet<TriggerEventType> collapsedGroupEventCategories = EnumSet.allOf(TriggerEventType.class);
   private boolean suppressSelectionObserver;
   private boolean skipSaveOnDismiss;
   @Nullable
   private String pendingPickerFieldKey;
   @Nullable
   private String pendingPickerSource;
   private boolean pendingPickerMultiSelect;
   @Nonnull
   private final Set<String> pendingPickerSelections = new LinkedHashSet<>();
   @Nonnull
   private String assetPickerSearchQuery = "";
   @Nonnull
   private final Set<String> missingOptionLangKeys = new HashSet<>();

   public TriggerVolumeInspectorPage(
      @Nonnull PlayerRef playerRef,
      @Nonnull String selectedWorld,
      @Nullable String preSelectedVolumeId,
      @Nonnull TriggerVolumeInspectorPage.InspectorTab initialTab
   ) {
      this(playerRef, selectedWorld, preSelectedVolumeId, false, initialTab);
   }

   public TriggerVolumeInspectorPage(
      @Nonnull PlayerRef playerRef,
      @Nonnull String selectedWorld,
      @Nullable String preSelectedVolumeId,
      boolean preSelectedIsGroup,
      @Nonnull TriggerVolumeInspectorPage.InspectorTab initialTab
   ) {
      super(playerRef, CustomPageLifetime.CanDismiss, TriggerVolumeInspectorPage.PageData.CODEC);
      this.selectedWorld = selectedWorld;
      this.preSelectedVolumeId = preSelectedVolumeId;
      this.preSelectedIsGroup = preSelectedIsGroup;
      this.selectedTab = initialTab;
   }

   @Override
   public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder cmd, @Nonnull UIEventBuilder evt, @Nonnull Store<EntityStore> store) {
      cmd.append("Pages/TriggerVolume/TriggerVolumeInspectorPage.ui");
      this.registerSelectionObserver();
      this.registerVolumeUpdateObserver();
      this.clearPastePrefabPreviewIfFromDifferentWorld();
      this.buildWorldDropdown(cmd);
      this.buildTabs(cmd, evt);
      this.buildList(cmd, evt);
      if (this.preSelectedVolumeId != null) {
         this.applyPreSelection(cmd);
      }

      this.buildSelectedPane(cmd, evt);
      this.bindStaticEvents(evt);
   }

   @Override
   public void onDismiss(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
      TriggerVolumeManager manager = getManagerForWorld(this.selectedWorld);
      if (manager != null) {
         if (!this.skipSaveOnDismiss) {
            this.saveDrafts(manager, false);
         }

         manager.clearSelectionObserver(this.playerRef.getUuid(), this.selectionObserver);
         manager.clearVolumeUpdateObserver(this.playerRef.getUuid(), this.volumeUpdateObserver);
      }

      this.skipSaveOnDismiss = false;
   }

   public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull TriggerVolumeInspectorPage.PageData data) {
      if (data.action != null) {
         switch (data.action) {
            case Select:
               this.onSelect(data);
               break;
            case ChangeWorld:
               this.onChangeWorld(data);
               break;
            case FilterChanged:
               this.onFilterChanged(data);
               break;
            case ChangeTab:
               this.onChangeTab(data);
               break;
            case UpdateVolumeField:
               this.onUpdateVolumeField(data);
               break;
            case UpdateTag:
               this.onUpdateTag(data);
               break;
            case RemoveTag:
               this.onRemoveTag(data);
               break;
            case DeleteSelection:
               this.onDeleteSelection();
               break;
            case Save:
               this.onSave();
               break;
            case Discard:
               this.onDiscard();
               break;
            case SelectEffect:
               this.onSelectEffect(data);
               break;
            case AddEffect:
               this.onAddEffect(data);
               break;
            case RemoveEffect:
               this.onRemoveEffect();
               break;
            case DuplicateEffect:
               this.onDuplicateEffect();
               break;
            case MoveEffectUp:
               this.onMoveEffect(-1);
               break;
            case MoveEffectDown:
               this.onMoveEffect(1);
               break;
            case UpdateAddTarget:
               this.onUpdateAddTarget(data);
               break;
            case UpdateAddEventType:
               this.onUpdateAddEventType(data);
               break;
            case ToggleEventCategory:
               this.onToggleEventCategory(data);
               break;
            case UpdateParameter:
               this.onUpdateParameter(data);
               break;
            case TogglePrefabPreview:
               this.onTogglePrefabPreview();
               break;
            case OpenPresetSave:
               this.onOpenPresetSave();
               break;
            case PresetNameChanged:
               this.onPresetNameChanged(data);
               break;
            case ConfirmSavePreset:
               this.onConfirmSavePreset(data);
               break;
            case CancelPresetSave:
               this.onCancelPresetSave();
               break;
            case OpenPresetLoad:
               this.onOpenPresetLoad();
               break;
            case LoadPreset:
               this.onLoadPreset(data);
               break;
            case CancelPresetLoad:
               this.onCancelPresetLoad();
               break;
            case OpenAssetPicker:
               this.onOpenAssetPicker(data);
               break;
            case AssetPickerSearch:
               this.onAssetPickerSearch(data);
               break;
            case AssetPickerSelect:
               this.onAssetPickerSelect(data);
               break;
            case ConfirmAssetPicker:
               this.onConfirmAssetPicker();
               break;
            case PreviewSound:
               this.onPreviewSound(data, store);
               break;
            case CancelAssetPicker:
               this.onCancelAssetPicker();
         }
      }
   }

   private void buildWorldDropdown(@Nonnull UICommandBuilder cmd) {
      ObjectArrayList<DropdownEntryInfo> entries = new ObjectArrayList();

      for (World world : Universe.get().getWorlds().values()) {
         String name = world.getName().toLowerCase(Locale.ROOT);
         entries.add(new DropdownEntryInfo(LocalizableString.fromString(name), name));
      }

      cmd.set("#WorldDropdown.Entries", entries);
      cmd.set("#WorldDropdown.Value", this.selectedWorld);
      cmd.set("#FilterField.Value", this.filterText);
   }

   private void buildTabs(@Nonnull UICommandBuilder cmd, @Nonnull UIEventBuilder evt) {
      cmd.clear("#TabButtons");
      int idx = 0;

      for (TriggerVolumeInspectorPage.InspectorTab tab : TriggerVolumeInspectorPage.InspectorTab.values()) {
         String sel = "#TabButtons[" + idx + "]";
         cmd.append("#TabButtons", "Pages/TriggerVolume/TriggerVolumeInspectorTabButton.ui");
         cmd.set(sel + ".Text", tab.label());
         cmd.set(sel + ".TooltipText", tab.tooltip());
         cmd.set(sel + ".Disabled", this.selectedTab == tab);
         evt.addEventBinding(
            CustomUIEventBindingType.Activating,
            sel,
            new EventData().append("Action", TriggerVolumeInspectorPage.Action.ChangeTab.name()).append("Tab", tab.name())
         );
         idx++;
      }
   }

   private void buildList(@Nonnull UICommandBuilder cmd, @Nonnull UIEventBuilder evt) {
      cmd.clear("#ListContainer");
      this.currentRows.clear();
      TriggerVolumeManager manager = getManagerForWorld(this.selectedWorld);
      if (manager != null && (!manager.getVolumesMap().isEmpty() || !manager.getGroupsMap().isEmpty())) {
         LinkedHashMap<String, List<VolumeEntry>> groupedVolumes = new LinkedHashMap<>();

         for (GroupEntry group : manager.getGroupsMap().values()) {
            if (!this.deletedGroups.contains(group.getId())) {
               groupedVolumes.put(group.getId(), new ArrayList<>());
            }
         }

         ArrayList<VolumeEntry> ungrouped = new ArrayList<>();

         for (VolumeEntry volume : manager.getVolumesMap().values()) {
            if (!this.deletedVolumes.contains(volume.getId())) {
               TriggerVolumeInspectorDrafts.VolumeDraft draft = this.volumeDrafts.get(volume.getId());
               String groupId = draft != null ? draft.groupId : volume.getGroupId();
               if (groupId != null && groupedVolumes.containsKey(groupId)) {
                  groupedVolumes.get(groupId).add(volume);
               } else {
                  ungrouped.add(volume);
               }
            }
         }

         int idx = 0;

         for (GroupEntry groupx : manager.getGroupsMap().values()) {
            if (!this.deletedGroups.contains(groupx.getId())) {
               TriggerVolumeInspectorDrafts.GroupDraft draft = this.draftForGroup(groupx);
               if (this.matchesFilter(draft.id)) {
                  idx = this.appendGroupRow(cmd, evt, idx, groupx.getId(), draft.id, draft.color);

                  for (VolumeEntry volumex : groupedVolumes.getOrDefault(groupx.getId(), List.of())) {
                     TriggerVolumeInspectorDrafts.VolumeDraft volumeDraft = this.draftForVolume(volumex);
                     if (this.matchesFilter(volumeDraft.id)) {
                        idx = this.appendVolumeRow(cmd, evt, idx, volumex.getId(), volumeDraft.id, true, draft.color);
                     }
                  }
               }
            }
         }

         List<VolumeEntry> visibleUngrouped = ungrouped.stream().filter(volumexx -> this.matchesFilter(this.draftForVolume(volumexx).id)).toList();
         if (!visibleUngrouped.isEmpty()) {
            cmd.append("#ListContainer", "Pages/TriggerVolume/TriggerVolumeInspectorSectionLabel.ui");
            cmd.set("#ListContainer[" + idx + "].Text", Message.translation("server.customUI.triggerVolumeBrowse.ungrouped"));
            idx++;

            for (VolumeEntry volumexx : visibleUngrouped) {
               TriggerVolumeInspectorDrafts.VolumeDraft draft = this.draftForVolume(volumexx);
               idx = this.appendVolumeRow(cmd, evt, idx, volumexx.getId(), draft.id, false, 0);
            }
         }

         if (idx == 0) {
            this.appendListMessage(cmd, Message.translation("server.customUI.triggerVolumeBrowse.emptyState"));
         } else {
            this.scrollSelectedRowToView(cmd);
         }
      } else {
         this.appendListMessage(cmd, Message.translation("server.customUI.triggerVolumeBrowse.emptyState"));
      }
   }

   private int appendGroupRow(@Nonnull UICommandBuilder cmd, @Nonnull UIEventBuilder evt, int idx, @Nonnull String originalId, @Nonnull String label, int color) {
      String sel = "#ListContainer[" + idx + "]";
      cmd.append("#ListContainer", "Pages/TriggerVolume/TriggerVolumeBrowseGroupRow.ui");
      cmd.set(sel + " #Label.Text", label);
      cmd.setObject(sel + " #ColorSwatch.Background", colorPatch(color));
      cmd.set(sel + ".Style", this.selectedIsGroup && originalId.equals(this.selectedId) ? SELECTED_ROW_STYLE : NORMAL_ROW_STYLE);
      evt.addEventBinding(
         CustomUIEventBindingType.Activating,
         sel,
         new EventData().append("Action", TriggerVolumeInspectorPage.Action.Select.name()).append("Id", originalId).append("IsGroup", "true"),
         false
      );
      this.currentRows.add(new TriggerVolumeInspectorPage.RowEntry(originalId, true, idx));
      return idx + 1;
   }

   private int appendVolumeRow(
      @Nonnull UICommandBuilder cmd, @Nonnull UIEventBuilder evt, int idx, @Nonnull String originalId, @Nonnull String label, boolean indented, int groupColor
   ) {
      String sel = "#ListContainer[" + idx + "]";
      cmd.append("#ListContainer", "Pages/TriggerVolume/TriggerVolumeBrowseVolumeRow.ui");
      cmd.set(sel + " #Label.Text", label);
      cmd.set(sel + " #Indent.Visible", indented);
      cmd.set(sel + " #ColorBar.Visible", indented);
      if (indented) {
         cmd.setObject(sel + " #ColorBar.Background", colorPatch(groupColor));
      }

      cmd.set(sel + ".Style", !this.selectedIsGroup && originalId.equals(this.selectedId) ? SELECTED_ROW_STYLE : NORMAL_ROW_STYLE);
      evt.addEventBinding(
         CustomUIEventBindingType.Activating,
         sel,
         new EventData().append("Action", TriggerVolumeInspectorPage.Action.Select.name()).append("Id", originalId).append("IsGroup", "false"),
         false
      );
      this.currentRows.add(new TriggerVolumeInspectorPage.RowEntry(originalId, false, idx));
      return idx + 1;
   }

   private void appendListMessage(@Nonnull UICommandBuilder cmd, @Nonnull Message text) {
      cmd.append("#ListContainer", "Pages/TriggerVolume/TriggerVolumeInspectorSectionLabel.ui");
      cmd.set("#ListContainer[0].Text", text);
   }

   private void applyPreSelection(@Nonnull UICommandBuilder cmd) {
      for (TriggerVolumeInspectorPage.RowEntry row : this.currentRows) {
         if (row.isGroup == this.preSelectedIsGroup && row.id.equals(this.preSelectedVolumeId)) {
            this.selectedId = row.id;
            this.selectedIsGroup = row.isGroup;
            cmd.set("#ListContainer[" + row.listIndex + "].Style", SELECTED_ROW_STYLE);
            cmd.set("#ListContainer.ScrollChildIndexIntoView", row.listIndex);
            return;
         }
      }
   }

   private void scrollSelectedRowToView(@Nonnull UICommandBuilder cmd) {
      if (this.selectedId != null) {
         for (TriggerVolumeInspectorPage.RowEntry row : this.currentRows) {
            if (row.isGroup == this.selectedIsGroup && row.id.equals(this.selectedId)) {
               cmd.set("#ListContainer.ScrollChildIndexIntoView", row.listIndex);
               return;
            }
         }
      }
   }

   private void buildSelectedPane(@Nonnull UICommandBuilder cmd, @Nonnull UIEventBuilder evt) {
      cmd.clear("#VolumeTab");
      cmd.clear("#TagsList");
      cmd.clear("#EffectListContainer");
      cmd.clear("#EffectDetailPanel");
      cmd.set("#NoSelectionLabel.Visible", this.selectedId == null);
      cmd.set("#VolumeTab.Visible", this.selectedId != null && this.selectedTab == TriggerVolumeInspectorPage.InspectorTab.VOLUME);
      cmd.set("#TagsTab.Visible", this.selectedId != null && this.selectedTab == TriggerVolumeInspectorPage.InspectorTab.TAGS);
      cmd.set("#EffectsTab.Visible", this.selectedId != null && this.selectedTab == TriggerVolumeInspectorPage.InspectorTab.EFFECTS);
      cmd.set("#DeleteButton.Disabled", this.selectedId == null);
      cmd.set("#SavePresetButton.Disabled", this.selectedId == null);
      cmd.set("#LoadPresetButton.Disabled", this.selectedId == null);
      if (this.selectedId != null) {
         switch (this.selectedTab) {
            case VOLUME:
               this.buildVolumeTab(cmd, evt);
               break;
            case EFFECTS:
               this.buildEffectsTab(cmd, evt);
               break;
            case TAGS:
               this.buildTagsTab(cmd, evt);
         }
      }
   }

   private void buildVolumeTab(@Nonnull UICommandBuilder cmd, @Nonnull UIEventBuilder evt) {
      int row = 0;
      if (this.selectedIsGroup) {
         TriggerVolumeInspectorDrafts.GroupDraft draft = this.selectedGroupDraft();
         if (draft == null) {
            return;
         }

         row = this.addVolumeSectionLabel(cmd, row, "identity");
         row = this.addVolumeTextRow(cmd, evt, row, "id", draft.id);
         row = this.addVolumeColorRow(cmd, evt, row, "color", colorToHex(draft.color));
         row = this.addVolumeSectionLabel(cmd, row, "transform");
         row = this.addVolumeVec3Row(cmd, evt, row, "position", draft.origin);
         row = this.addVolumeSectionLabel(cmd, row, "behavior");
         row = this.addVolumeDropdownRow(cmd, evt, row, "targetTypes", targetTypeEntries(), targetTypesValue(draft.targetTypes));
         row = this.addVolumeCheckboxRow(cmd, evt, row, "enabled", draft.enabled);
         row = this.addVolumeSectionLabel(cmd, row, "timing");
         row = this.addVolumeDropdownRow(cmd, evt, row, "conditionTiming", conditionTimingEntries(), draft.conditionTiming.name());
         row = this.addVolumeDropdownRow(cmd, evt, row, "rejectionDelayMode", rejectionDelayModeEntries(), draft.rejectionDelayMode.name());
         row = this.addReadonlyTextRow(cmd, row, "members", String.join(", ", draft.memberVolumeIds));
      } else {
         TriggerVolumeInspectorDrafts.VolumeDraft draft = this.selectedVolumeDraft();
         if (draft == null) {
            return;
         }

         row = this.addVolumeSectionLabel(cmd, row, "identity");
         row = this.addVolumeTextRow(cmd, evt, row, "id", draft.id);
         row = this.addVolumeColorRow(cmd, evt, row, "color", draft.color != null ? colorToHex(draft.color) : "#00CCCC");
         row = this.addVolumeSectionLabel(cmd, row, "transform");
         row = this.addVolumeDropdownRow(cmd, evt, row, "shape", shapeEntries(), draft.shapeType.name());
         row = this.addVolumeVec3Row(cmd, evt, row, "position", draft.position);
         row = this.addVolumeDimensionsRow(cmd, evt, row, draft.shapeType, draft.dimensions);
         row = this.addVolumeSectionLabel(cmd, row, "behavior");
         row = this.addVolumeDropdownRow(cmd, evt, row, "targetTypes", targetTypeEntries(), targetTypesValue(draft.targetTypes));
         row = this.addVolumeDropdownRow(cmd, evt, row, "projectileSource", projectileSourceEntries(), draft.projectileSource.name());
         row = this.addVolumeCheckboxRow(cmd, evt, row, "enabled", draft.enabled);
         row = this.addVolumeCheckboxRow(cmd, evt, row, "keepLoaded", draft.keepLoaded);
         row = this.addVolumeCheckboxRow(cmd, evt, row, "cancelDelayedOnExit", draft.cancelDelayedOnExit);
         row = this.addVolumeSectionLabel(cmd, row, "timing");
         row = this.addVolumeNumberRow(cmd, evt, row, "activationDelay", String.valueOf(draft.activationDelay), 2);
         row = this.addVolumeNumberRow(cmd, evt, row, "cooldown", String.valueOf(draft.cooldown), 2);
         row = this.addVolumeDropdownRow(cmd, evt, row, "cooldownMode", cooldownModeEntries(), draft.cooldownMode.name());
         row = this.addVolumeDropdownRow(cmd, evt, row, "conditionTiming", conditionTimingEntries(), draft.conditionTiming.name());
         row = this.addVolumeDropdownRow(cmd, evt, row, "rejectionDelayMode", rejectionDelayModeEntries(), draft.rejectionDelayMode.name());
      }
   }

   private int addVolumeSectionLabel(@Nonnull UICommandBuilder cmd, int row, @Nonnull String sectionKey) {
      String selector = "#VolumeTab[" + row + "]";
      cmd.append("#VolumeTab", "Pages/TriggerVolume/TriggerVolumeInspectorVolumeSectionLabel.ui");
      cmd.set(selector + " #Title.Text", Message.translation("server.customUI.triggerVolumeInspector.section." + sectionKey));
      return row + 1;
   }

   private int addVolumeTextRow(@Nonnull UICommandBuilder cmd, @Nonnull UIEventBuilder evt, int row, @Nonnull String key, @Nonnull String value) {
      String sel = "#VolumeTab[" + row + "]";
      cmd.append("#VolumeTab", "Pages/TriggerVolume/TriggerVolumeInspectorTextRow.ui");
      cmd.set(sel + " #Label.Text", fieldLabel(key));
      cmd.set(sel + " #Label.TooltipText", volumeFieldTooltip(key));
      cmd.set(sel + " #Input.Value", value);
      this.setIdValidation(cmd, sel, key, value);
      evt.addEventBinding(CustomUIEventBindingType.ValueChanged, sel + " #Input", volumeFieldEvent(key).append("@ParamValue", sel + " #Input.Value"), false);
      return row + 1;
   }

   private void setIdValidation(@Nonnull UICommandBuilder cmd, @Nonnull String selector, @Nonnull String key, @Nonnull String value) {
      Message validationMessage = "id".equals(key) ? this.idValidationMessage(value) : null;
      if (validationMessage == null) {
         cmd.set(selector + " #ValidationLabel.Visible", false);
      } else {
         cmd.set(selector + " #ValidationLabel.Visible", true);
         cmd.set(selector + " #ValidationLabel.Text", validationMessage);
      }
   }

   private int addVolumeColorRow(@Nonnull UICommandBuilder cmd, @Nonnull UIEventBuilder evt, int row, @Nonnull String key, @Nonnull String value) {
      String sel = "#VolumeTab[" + row + "]";
      cmd.append("#VolumeTab", "Pages/TriggerVolume/TriggerVolumeInspectorColorRow.ui");
      cmd.set(sel + " #Label.Text", fieldLabel(key));
      cmd.set(sel + " #Label.TooltipText", volumeFieldTooltip(key));
      cmd.set(sel + " #Input.Color", value);
      evt.addEventBinding(CustomUIEventBindingType.ValueChanged, sel + " #Input", volumeFieldEvent(key).append("@ParamValue", sel + " #Input.Color"), false);
      return row + 1;
   }

   private int addReadonlyTextRow(@Nonnull UICommandBuilder cmd, int row, @Nonnull String key, @Nonnull String value) {
      String sel = "#VolumeTab[" + row + "]";
      cmd.append("#VolumeTab", "Pages/TriggerVolume/TriggerVolumeBrowsePropertyRow.ui");
      cmd.set(sel + " #Key.Text", fieldLabel(key));
      cmd.set(sel + " #Key.TooltipText", volumeFieldTooltip(key));
      cmd.set(sel + " #Value.Text", value);
      return row + 1;
   }

   private int addVolumeNumberRow(@Nonnull UICommandBuilder cmd, @Nonnull UIEventBuilder evt, int row, @Nonnull String key, @Nonnull String value, int decimals) {
      String sel = "#VolumeTab[" + row + "]";
      cmd.append("#VolumeTab", decimals > 0 ? "Pages/TriggerVolume/TriggerVolumeInspectorNumberRow.ui" : "Pages/TriggerVolume/TriggerVolumeInspectorIntRow.ui");
      cmd.set(sel + " #Label.Text", fieldLabel(key));
      cmd.set(sel + " #Label.TooltipText", volumeFieldTooltip(key));

      try {
         cmd.set(sel + " #Input.Value", Double.parseDouble(value));
      } catch (NumberFormatException var9) {
         cmd.set(sel + " #Input.Value", 0.0);
      }

      evt.addEventBinding(
         CustomUIEventBindingType.ValueChanged, sel + " #Input", volumeFieldEvent(key).append("@ParamNumericValue", sel + " #Input.Value"), false
      );
      return row + 1;
   }

   private int addVolumeCheckboxRow(@Nonnull UICommandBuilder cmd, @Nonnull UIEventBuilder evt, int row, @Nonnull String key, boolean value) {
      String sel = "#VolumeTab[" + row + "]";
      cmd.append("#VolumeTab", "Pages/TriggerVolume/TriggerVolumeInspectorCheckboxRow.ui");
      cmd.set(sel + " #Label.Text", fieldLabel(key));
      cmd.set(sel + " #Label.TooltipText", volumeFieldTooltip(key));
      cmd.set(sel + " #Input.Value", value);
      evt.addEventBinding(CustomUIEventBindingType.ValueChanged, sel + " #Input", volumeFieldEvent(key).append("@ParamBool", sel + " #Input.Value"), false);
      return row + 1;
   }

   private int addVolumeDropdownRow(
      @Nonnull UICommandBuilder cmd, @Nonnull UIEventBuilder evt, int row, @Nonnull String key, @Nonnull List<DropdownEntryInfo> entries, @Nonnull String value
   ) {
      String sel = "#VolumeTab[" + row + "]";
      cmd.append("#VolumeTab", "Pages/TriggerVolume/TriggerVolumeInspectorDropdownRow.ui");
      cmd.set(sel + " #Label.Text", fieldLabel(key));
      cmd.set(sel + " #Label.TooltipText", volumeFieldTooltip(key));
      cmd.set(sel + " #Input.Entries", new ObjectArrayList(entries));
      cmd.set(sel + " #Input.Value", value);
      evt.addEventBinding(CustomUIEventBindingType.ValueChanged, sel + " #Input", volumeFieldEvent(key).append("@ParamValue", sel + " #Input.Value"), false);
      return row + 1;
   }

   private int addVolumeVec3Row(@Nonnull UICommandBuilder cmd, @Nonnull UIEventBuilder evt, int row, @Nonnull String key, @Nonnull Vector3d value) {
      String sel = "#VolumeTab[" + row + "]";
      cmd.append("#VolumeTab", "Pages/TriggerVolume/TriggerVolumeInspectorVec3Row.ui");
      cmd.set(sel + " #Label.Text", fieldLabel(key));
      cmd.set(sel + " #Label.TooltipText", volumeFieldTooltip(key));
      cmd.set(sel + " #X.Value", value.x());
      cmd.set(sel + " #Y.Value", value.y());
      cmd.set(sel + " #Z.Value", value.z());

      for (String comp : List.of("X", "Y", "Z")) {
         evt.addEventBinding(
            CustomUIEventBindingType.ValueChanged,
            sel + " #" + comp,
            volumeFieldEvent(key).append("@VecX", sel + " #X.Value").append("@VecY", sel + " #Y.Value").append("@VecZ", sel + " #Z.Value"),
            false
         );
      }

      return row + 1;
   }

   private int addVolumeDimensionsRow(
      @Nonnull UICommandBuilder cmd, @Nonnull UIEventBuilder evt, int row, @Nonnull TriggerVolumeShapeType shapeType, @Nonnull Vector3d value
   ) {
      return switch (shapeType) {
         case Box -> this.addBoxDimensionsRow(cmd, evt, row, value);
         case Sphere -> this.addSphereDimensionsRow(cmd, evt, row, value);
         case Cylinder -> this.addCylinderDimensionsRow(cmd, evt, row, value);
      };
   }

   private int addBoxDimensionsRow(@Nonnull UICommandBuilder cmd, @Nonnull UIEventBuilder evt, int row, @Nonnull Vector3d value) {
      String sel = "#VolumeTab[" + row + "]";
      cmd.append("#VolumeTab", "Pages/TriggerVolume/TriggerVolumeInspectorDimensionsBoxRow.ui");
      setDimensionsHeader(cmd, sel);
      cmd.set(sel + " #XLabel.Text", Message.translation("server.customUI.triggerVolumeInspector.field.dimensions.x"));
      cmd.set(sel + " #YLabel.Text", Message.translation("server.customUI.triggerVolumeInspector.field.dimensions.y"));
      cmd.set(sel + " #ZLabel.Text", Message.translation("server.customUI.triggerVolumeInspector.field.dimensions.z"));
      cmd.set(sel + " #X.Value", value.x());
      cmd.set(sel + " #Y.Value", value.y());
      cmd.set(sel + " #Z.Value", value.z());

      for (String component : List.of("X", "Y", "Z")) {
         evt.addEventBinding(
            CustomUIEventBindingType.ValueChanged,
            sel + " #" + component,
            volumeFieldEvent("dimensions").append("@VecX", sel + " #X.Value").append("@VecY", sel + " #Y.Value").append("@VecZ", sel + " #Z.Value"),
            false
         );
      }

      return row + 1;
   }

   private int addSphereDimensionsRow(@Nonnull UICommandBuilder cmd, @Nonnull UIEventBuilder evt, int row, @Nonnull Vector3d value) {
      String sel = "#VolumeTab[" + row + "]";
      cmd.append("#VolumeTab", "Pages/TriggerVolume/TriggerVolumeInspectorDimensionsSphereRow.ui");
      setDimensionsHeader(cmd, sel);
      cmd.set(sel + " #RadiusLabel.Text", Message.translation("server.customUI.triggerVolumeInspector.field.dimensions.radius"));
      cmd.set(sel + " #Radius.Value", value.x());
      evt.addEventBinding(
         CustomUIEventBindingType.ValueChanged,
         sel + " #Radius",
         volumeFieldEvent("dimensionsRadius").append("@ParamNumericValue", sel + " #Radius.Value"),
         false
      );
      return row + 1;
   }

   private int addCylinderDimensionsRow(@Nonnull UICommandBuilder cmd, @Nonnull UIEventBuilder evt, int row, @Nonnull Vector3d value) {
      String sel = "#VolumeTab[" + row + "]";
      cmd.append("#VolumeTab", "Pages/TriggerVolume/TriggerVolumeInspectorDimensionsCylinderRow.ui");
      setDimensionsHeader(cmd, sel);
      cmd.set(sel + " #RadiusLabel.Text", Message.translation("server.customUI.triggerVolumeInspector.field.dimensions.radius"));
      cmd.set(sel + " #HeightLabel.Text", Message.translation("server.customUI.triggerVolumeInspector.field.dimensions.height"));
      cmd.set(sel + " #Radius.Value", value.x());
      cmd.set(sel + " #Height.Value", value.y());

      for (String component : List.of("Radius", "Height")) {
         evt.addEventBinding(
            CustomUIEventBindingType.ValueChanged,
            sel + " #" + component,
            volumeFieldEvent("dimensionsCylinder").append("@VecX", sel + " #Radius.Value").append("@VecY", sel + " #Height.Value"),
            false
         );
      }

      return row + 1;
   }

   private static void setDimensionsHeader(@Nonnull UICommandBuilder cmd, @Nonnull String selector) {
      cmd.set(selector + " #Label.Text", fieldLabel("dimensions"));
      cmd.set(selector + " #Label.TooltipText", volumeFieldTooltip("dimensions"));
   }

   @Nonnull
   private static EventData volumeFieldEvent(@Nonnull String key) {
      return new EventData().append("Action", TriggerVolumeInspectorPage.Action.UpdateVolumeField.name()).append("ParamKey", key);
   }

   private void buildTagsTab(@Nonnull UICommandBuilder cmd, @Nonnull UIEventBuilder evt) {
      Map<String, String> tags = this.selectedIsGroup ? this.selectedGroupDraft().tags : this.selectedVolumeDraft().tags;
      int idx = 0;

      for (Entry<String, String> entry : tags.entrySet()) {
         String sel = "#TagsList[" + idx + "]";
         cmd.append("#TagsList", "Pages/TriggerVolume/TriggerVolumeBrowseTagRow.ui");
         cmd.set(sel + " #TagLabel.Text", entry.getKey() + ": " + entry.getValue());
         evt.addEventBinding(
            CustomUIEventBindingType.Activating,
            sel + " #RemoveButton",
            new EventData().append("Action", TriggerVolumeInspectorPage.Action.RemoveTag.name()).append("RemoveTagKey", entry.getKey())
         );
         idx++;
      }
   }

   private void buildEffectsTab(@Nonnull UICommandBuilder cmd, @Nonnull UIEventBuilder evt) {
      this.buildAddEventTypeDropdown(cmd);
      this.buildAddTargetDropdown(cmd);
      this.buildAddEffectDropdown(cmd);
      this.buildEffectList(cmd, evt);
      this.buildEffectDetailPanel(cmd, evt);
   }

   private void bindStaticEvents(@Nonnull UIEventBuilder evt) {
      evt.addEventBinding(
         CustomUIEventBindingType.ValueChanged,
         "#WorldDropdown",
         new EventData().append("Action", TriggerVolumeInspectorPage.Action.ChangeWorld.name()).append("@WorldName", "#WorldDropdown.Value"),
         false
      );
      evt.addEventBinding(
         CustomUIEventBindingType.ValueChanged,
         "#FilterField",
         new EventData().append("Action", TriggerVolumeInspectorPage.Action.FilterChanged.name()).append("@FilterText", "#FilterField.Value"),
         false
      );
      evt.addEventBinding(
         CustomUIEventBindingType.Activating,
         "#AddTagButton",
         new EventData()
            .append("Action", TriggerVolumeInspectorPage.Action.UpdateTag.name())
            .append("@TagKey", "#TagKeyField.Value")
            .append("@TagValues", "#TagValuesField.Value")
      );
      evt.addEventBinding(
         CustomUIEventBindingType.Activating, "#DeleteButton", new EventData().append("Action", TriggerVolumeInspectorPage.Action.DeleteSelection.name())
      );
      evt.addEventBinding(CustomUIEventBindingType.Activating, "#SaveButton", new EventData().append("Action", TriggerVolumeInspectorPage.Action.Save.name()));
      evt.addEventBinding(
         CustomUIEventBindingType.Activating, "#DiscardButton", new EventData().append("Action", TriggerVolumeInspectorPage.Action.Discard.name())
      );
      evt.addEventBinding(
         CustomUIEventBindingType.Activating,
         "#AddEffectButton",
         new EventData()
            .append("Action", TriggerVolumeInspectorPage.Action.AddEffect.name())
            .append("@EffectType", "#AddEffectDropdown.Value")
            .append("@AddTargetKind", "#AddTargetDropdown.Value")
            .append("@AddEventType", "#AddEventTypeDropdown.Value")
      );
      evt.addEventBinding(
         CustomUIEventBindingType.ValueChanged,
         "#AddTargetDropdown",
         new EventData().append("Action", TriggerVolumeInspectorPage.Action.UpdateAddTarget.name()).append("@AddTargetKind", "#AddTargetDropdown.Value"),
         false
      );
      evt.addEventBinding(
         CustomUIEventBindingType.ValueChanged,
         "#AddEventTypeDropdown",
         new EventData().append("Action", TriggerVolumeInspectorPage.Action.UpdateAddEventType.name()).append("@AddEventType", "#AddEventTypeDropdown.Value"),
         false
      );
      evt.addEventBinding(
         CustomUIEventBindingType.Activating, "#RemoveEffectButton", new EventData().append("Action", TriggerVolumeInspectorPage.Action.RemoveEffect.name())
      );
      evt.addEventBinding(
         CustomUIEventBindingType.Activating,
         "#DuplicateEffectButton",
         new EventData().append("Action", TriggerVolumeInspectorPage.Action.DuplicateEffect.name())
      );
      evt.addEventBinding(
         CustomUIEventBindingType.Activating, "#MoveEffectUpButton", new EventData().append("Action", TriggerVolumeInspectorPage.Action.MoveEffectUp.name())
      );
      evt.addEventBinding(
         CustomUIEventBindingType.Activating,
         "#MoveEffectDownButton",
         new EventData().append("Action", TriggerVolumeInspectorPage.Action.MoveEffectDown.name())
      );
      evt.addEventBinding(
         CustomUIEventBindingType.Activating, "#SavePresetButton", new EventData().append("Action", TriggerVolumeInspectorPage.Action.OpenPresetSave.name())
      );
      evt.addEventBinding(
         CustomUIEventBindingType.Activating, "#LoadPresetButton", new EventData().append("Action", TriggerVolumeInspectorPage.Action.OpenPresetLoad.name())
      );
      evt.addEventBinding(
         CustomUIEventBindingType.ValueChanged,
         "#PresetName #Input",
         new EventData().append("Action", TriggerVolumeInspectorPage.Action.PresetNameChanged.name()).append("@PresetName", "#PresetName #Input.Value"),
         false
      );
      evt.addEventBinding(
         CustomUIEventBindingType.Activating,
         "#ConfirmSavePresetButton",
         new EventData().append("Action", TriggerVolumeInspectorPage.Action.ConfirmSavePreset.name()).append("@PresetName", "#PresetName #Input.Value")
      );
      evt.addEventBinding(
         CustomUIEventBindingType.Activating,
         "#CancelSavePresetButton",
         new EventData().append("Action", TriggerVolumeInspectorPage.Action.CancelPresetSave.name())
      );
      evt.addEventBinding(
         CustomUIEventBindingType.Activating,
         "#CancelLoadPresetButton",
         new EventData().append("Action", TriggerVolumeInspectorPage.Action.CancelPresetLoad.name())
      );
      evt.addEventBinding(
         CustomUIEventBindingType.ValueChanged,
         "#AssetPickerPage #SearchInput",
         new EventData()
            .append("Action", TriggerVolumeInspectorPage.Action.AssetPickerSearch.name())
            .append("@AssetPickerQuery", "#AssetPickerPage #SearchInput.Value"),
         false
      );
      evt.addEventBinding(
         CustomUIEventBindingType.Activating,
         "#ConfirmAssetPickerButton",
         new EventData().append("Action", TriggerVolumeInspectorPage.Action.ConfirmAssetPicker.name())
      );
      evt.addEventBinding(
         CustomUIEventBindingType.Activating,
         "#CancelAssetPickerButton",
         new EventData().append("Action", TriggerVolumeInspectorPage.Action.CancelAssetPicker.name())
      );
   }

   private void onSelect(@Nonnull TriggerVolumeInspectorPage.PageData data) {
      if (data.id != null) {
         this.selectedId = data.id;
         this.selectedIsGroup = "true".equals(data.isGroup);
         this.selectedEffectIndex = -1;
         this.syncSelectionToTool();
         this.rebuildAll();
      }
   }

   private void onChangeWorld(@Nonnull TriggerVolumeInspectorPage.PageData data) {
      if (data.worldName != null && !data.worldName.equals(this.selectedWorld)) {
         TriggerVolumeManager oldManager = getManagerForWorld(this.selectedWorld);
         if (oldManager != null && !this.saveDrafts(oldManager, false)) {
            this.revertRejectedDraftIds();
            this.rebuildAll();
         } else {
            this.hidePastePrefabPreview();
            if (oldManager != null) {
               oldManager.clearSelectionObserver(this.playerRef.getUuid(), this.selectionObserver);
               oldManager.clearVolumeUpdateObserver(this.playerRef.getUuid(), this.volumeUpdateObserver);
            }

            this.clearDraftState();
            this.selectedWorld = data.worldName;
            this.selectedId = null;
            this.selectedIsGroup = false;
            this.selectedEffectIndex = -1;
            this.registerSelectionObserver();
            this.registerVolumeUpdateObserver();
            this.rebuildAll();
         }
      }
   }

   private void onFilterChanged(@Nonnull TriggerVolumeInspectorPage.PageData data) {
      this.filterText = data.filterText != null ? data.filterText.trim().toLowerCase(Locale.ROOT) : "";
      this.rebuildAll();
   }

   private void onChangeTab(@Nonnull TriggerVolumeInspectorPage.PageData data) {
      if (data.tab != null) {
         try {
            this.selectedTab = TriggerVolumeInspectorPage.InspectorTab.valueOf(data.tab);
            this.rebuildAll();
         } catch (IllegalArgumentException var3) {
         }
      }
   }

   private void onUpdateVolumeField(@Nonnull TriggerVolumeInspectorPage.PageData data) {
      if (data.paramKey != null && this.selectedId != null) {
         boolean shouldRebuild = !this.selectedIsGroup && "shape".equals(data.paramKey);
         if (this.selectedIsGroup) {
            TriggerVolumeInspectorDrafts.GroupDraft draft = this.selectedGroupDraft();
            if (draft == null) {
               return;
            }

            this.updateGroupField(draft, data);
            if ("id".equals(data.paramKey)) {
               this.updateIdValidation(draft.id);
               return;
            }
         } else {
            TriggerVolumeInspectorDrafts.VolumeDraft draftx = this.selectedVolumeDraft();
            if (draftx == null) {
               return;
            }

            this.updateVolumeField(draftx, data);
            if ("id".equals(data.paramKey)) {
               this.updateIdValidation(draftx.id);
               return;
            }
         }

         if (shouldRebuild) {
            this.rebuildAll();
         }

         if ("position".equals(data.paramKey)) {
            this.refreshActivePastePrefabPreviewPosition();
         }
      }
   }

   private void updateVolumeField(@Nonnull TriggerVolumeInspectorDrafts.VolumeDraft draft, @Nonnull TriggerVolumeInspectorPage.PageData data) {
      boolean updated = true;
      String var4 = data.paramKey;
      switch (var4) {
         case "id":
            this.updateDraftId(draft, data.paramValue);
            break;
         case "shape":
            draft.shapeType = parseEnum(TriggerVolumeShapeType.class, data.paramValue, draft.shapeType);
            break;
         case "position":
            setVec(draft.position, data);
            break;
         case "dimensions": {
            Vector3d oldDimensions = new Vector3d(draft.dimensions);
            setBoxDimensions(draft.dimensions, data);
            draft.rescaleAnchorOffset(oldDimensions);
            break;
         }
         case "dimensionsRadius": {
            Vector3d oldDimensions = new Vector3d(draft.dimensions);
            setSphereDimensions(draft.dimensions, data);
            draft.rescaleAnchorOffset(oldDimensions);
            break;
         }
         case "dimensionsCylinder": {
            Vector3d oldDimensions = new Vector3d(draft.dimensions);
            setCylinderDimensions(draft.dimensions, data);
            draft.rescaleAnchorOffset(oldDimensions);
            break;
         }
         case "color":
            draft.color = parseColor(data.paramValue);
            break;
         case "targetTypes":
            draft.targetTypes = parseTargetTypes(data.paramValue);
            break;
         case "projectileSource":
            draft.projectileSource = parseEnum(ProjectileSource.class, data.paramValue, draft.projectileSource);
            break;
         case "enabled":
            draft.enabled = Boolean.TRUE.equals(data.paramBool);
            break;
         case "keepLoaded":
            draft.keepLoaded = Boolean.TRUE.equals(data.paramBool);
            break;
         case "cancelDelayedOnExit":
            draft.cancelDelayedOnExit = Boolean.TRUE.equals(data.paramBool);
            break;
         case "activationDelay":
            draft.activationDelay = data.paramNumericValue != null ? Math.max(0.0F, data.paramNumericValue.floatValue()) : draft.activationDelay;
            break;
         case "cooldown":
            draft.cooldown = data.paramNumericValue != null ? Math.max(0.0F, data.paramNumericValue.floatValue()) : draft.cooldown;
            break;
         case "cooldownMode":
            draft.cooldownMode = parseEnum(CooldownMode.class, data.paramValue, draft.cooldownMode);
            break;
         case "conditionTiming":
            draft.conditionTiming = parseEnum(ConditionTiming.class, data.paramValue, draft.conditionTiming);
            break;
         case "rejectionDelayMode":
            draft.rejectionDelayMode = parseEnum(RejectionDelayMode.class, data.paramValue, draft.rejectionDelayMode);
            break;
         default:
            updated = false;
      }

      if (updated) {
         draft.markDirty();
      }
   }

   private void updateDraftId(@Nonnull TriggerVolumeInspectorDrafts.VolumeDraft draft, @Nullable String value) {
      draft.id = value != null ? value.trim() : draft.id;
   }

   private void updateIdValidation(@Nonnull String value) {
      UICommandBuilder cmd = new UICommandBuilder();
      this.setIdValidation(cmd, "#VolumeTab[1]", "id", value);
      this.sendUpdate(cmd, false);
   }

   private void updateGroupField(@Nonnull TriggerVolumeInspectorDrafts.GroupDraft draft, @Nonnull TriggerVolumeInspectorPage.PageData data) {
      boolean updated = true;
      String var4 = data.paramKey;
      switch (var4) {
         case "id":
            this.updateDraftId(draft, data.paramValue);
            break;
         case "position":
            setVec(draft.origin, data);
            break;
         case "color":
            draft.color = parsePackedColor(data.paramValue, draft.color);
            break;
         case "targetTypes":
            draft.targetTypes = parseTargetTypes(data.paramValue);
            break;
         case "enabled":
            draft.enabled = Boolean.TRUE.equals(data.paramBool);
            break;
         case "conditionTiming":
            draft.conditionTiming = parseEnum(ConditionTiming.class, data.paramValue, draft.conditionTiming);
            break;
         case "rejectionDelayMode":
            draft.rejectionDelayMode = parseEnum(RejectionDelayMode.class, data.paramValue, draft.rejectionDelayMode);
            break;
         default:
            updated = false;
      }

      if (updated) {
         draft.markDirty();
      }
   }

   private void updateDraftId(@Nonnull TriggerVolumeInspectorDrafts.GroupDraft draft, @Nullable String value) {
      draft.id = value != null ? value.trim() : draft.id;
   }

   private void onUpdateTag(@Nonnull TriggerVolumeInspectorPage.PageData data) {
      if (this.selectedId != null && data.tagKey != null) {
         String key = data.tagKey.trim();
         if (!key.isEmpty()) {
            this.currentTags().put(key, TaggedVolumeEffectUtil.normalizeTagValue(data.tagValues));
            this.markSelectedDraftDirty();
            UICommandBuilder cmd = new UICommandBuilder();
            UIEventBuilder evt = new UIEventBuilder();
            cmd.clear("#TagsList");
            this.buildTagsTab(cmd, evt);
            cmd.set("#TagKeyField.Value", "");
            cmd.set("#TagValuesField.Value", "");
            this.sendUpdate(cmd, evt, false);
         }
      }
   }

   private void onRemoveTag(@Nonnull TriggerVolumeInspectorPage.PageData data) {
      if (data.removeTagKey != null) {
         if (this.currentTags().remove(data.removeTagKey) != null) {
            this.markSelectedDraftDirty();
            this.rebuildAll();
         }
      }
   }

   private void onDeleteSelection() {
      if (this.selectedId != null) {
         this.hidePastePrefabPreview();
         if (this.selectedIsGroup) {
            this.deletedGroups.add(this.selectedId);
         } else {
            this.deletedVolumes.add(this.selectedId);
         }

         this.selectedId = null;
         this.rebuildAll();
      }
   }

   private void onSave() {
      TriggerVolumeManager manager = getManagerForWorld(this.selectedWorld);
      if (manager != null && this.saveDrafts(manager, true)) {
         this.skipSaveOnDismiss = true;
         this.close();
      } else {
         this.revertRejectedDraftIds();
         this.rebuildAll();
      }
   }

   private void clearDraftState() {
      this.volumeDrafts.clear();
      this.groupDrafts.clear();
      this.deletedVolumes.clear();
      this.deletedGroups.clear();
   }

   private void onDiscard() {
      this.skipSaveOnDismiss = true;
      this.close();
   }

   private boolean saveDrafts(@Nonnull TriggerVolumeManager manager, boolean notifyPlayer) {
      if (!this.validateDraftIds(manager)) {
         return false;
      } else {
         this.applyDeletes(manager);
         this.applyGroupDrafts(manager);
         this.applyVolumeDrafts(manager);
         manager.markSpatialDirty();
         manager.notifyViewers();
         if (notifyPlayer) {
            this.playerRef.sendMessage(Message.translation("server.customUI.triggerVolumeInspector.saved"));
         }

         return true;
      }
   }

   private void revertRejectedDraftIds() {
      for (TriggerVolumeInspectorDrafts.GroupDraft draft : this.groupDrafts.values()) {
         if (!this.deletedGroups.contains(draft.originalId) && !this.isDraftIdGood(draft.id, draft.originalId, true)) {
            draft.id = draft.originalId;
            draft.markDirty();
         }
      }

      for (TriggerVolumeInspectorDrafts.VolumeDraft draftx : this.volumeDrafts.values()) {
         if (!this.deletedVolumes.contains(draftx.originalId) && !this.isDraftIdGood(draftx.id, draftx.originalId, false)) {
            draftx.id = draftx.originalId;
            draftx.markDirty();
         }
      }
   }

   private boolean validateDraftIds(@Nonnull TriggerVolumeManager manager) {
      HashSet<String> used = new HashSet<>();

      for (TriggerVolumeInspectorDrafts.GroupDraft draft : this.groupDrafts.values()) {
         if (!this.deletedGroups.contains(draft.originalId)) {
            if (!this.isValidDraftId(draft.id)) {
               return false;
            }

            if (!used.add(draft.id) || manager.hasVolume(draft.id) || manager.hasGroup(draft.id) && !draft.originalId.equals(draft.id)) {
               this.playerRef.sendMessage(Message.translation("server.customUI.triggerVolumeInspector.idCollision").param("id", draft.id));
               return false;
            }
         }
      }

      for (TriggerVolumeInspectorDrafts.VolumeDraft draftx : this.volumeDrafts.values()) {
         if (!this.deletedVolumes.contains(draftx.originalId)) {
            if (!this.isValidDraftId(draftx.id)) {
               return false;
            }

            if (!used.add(draftx.id) || manager.hasGroup(draftx.id) || manager.hasVolume(draftx.id) && !draftx.originalId.equals(draftx.id)) {
               this.playerRef.sendMessage(Message.translation("server.customUI.triggerVolumeInspector.idCollision").param("id", draftx.id));
               return false;
            }
         }
      }

      return true;
   }

   private boolean isValidDraftId(@Nonnull String id) {
      if (isDraftIdFormatValid(id)) {
         return true;
      } else {
         this.playerRef.sendMessage(Message.translation("server.customUI.triggerVolumeInspector.invalidId"));
         return false;
      }
   }

   private static boolean isDraftIdFormatValid(@Nonnull String id) {
      return VALID_ID.matcher(id).matches();
   }

   @Nullable
   private Message idValidationMessage(@Nonnull String id) {
      if (!isDraftIdFormatValid(id)) {
         return Message.translation("server.customUI.triggerVolumeInspector.invalidId");
      } else {
         return this.selectedId != null && this.hasDraftIdCollision(id, this.selectedId, this.selectedIsGroup)
            ? Message.translation("server.customUI.triggerVolumeInspector.idCollision").param("id", id)
            : null;
      }
   }

   private boolean isDraftIdGood(@Nonnull String id, @Nonnull String originalId, boolean group) {
      return isDraftIdFormatValid(id) && !this.hasDraftIdCollision(id, originalId, group);
   }

   private boolean hasDraftIdCollision(@Nonnull String id, @Nonnull String originalId, boolean group) {
      TriggerVolumeManager manager = this.getSelectedManager();
      if (manager == null) {
         return false;
      } else {
         for (TriggerVolumeInspectorDrafts.GroupDraft draft : this.groupDrafts.values()) {
            if (!this.deletedGroups.contains(draft.originalId) && (!group || !draft.originalId.equals(originalId)) && id.equals(draft.id)) {
               return true;
            }
         }

         for (TriggerVolumeInspectorDrafts.VolumeDraft draftx : this.volumeDrafts.values()) {
            if (!this.deletedVolumes.contains(draftx.originalId) && (group || !draftx.originalId.equals(originalId)) && id.equals(draftx.id)) {
               return true;
            }
         }

         return group
            ? manager.hasVolume(id) || manager.hasGroup(id) && !originalId.equals(id)
            : manager.hasGroup(id) || manager.hasVolume(id) && !originalId.equals(id);
      }
   }

   private void applyDeletes(@Nonnull TriggerVolumeManager manager) {
      for (String volumeId : this.deletedVolumes) {
         VolumeEntry volume = manager.getVolume(volumeId);
         if (volume != null) {
            if (volume.getGroupId() != null) {
               GroupEntry group = manager.getGroup(volume.getGroupId());
               if (group != null) {
                  group.removeMember(volumeId);
               }
            }

            manager.unregister(volumeId);
            manager.notifyViewersRemove(volumeId);
         }
      }

      for (String groupId : this.deletedGroups) {
         GroupEntry group = manager.getGroup(groupId);
         if (group != null) {
            for (String memberId : new ArrayList<>(group.getMemberVolumeIds())) {
               VolumeEntry volume = manager.getVolume(memberId);
               if (volume != null) {
                  volume.setGroupId(null);
                  manager.notifyViewersAdd(volume);
               }
            }

            manager.unregisterGroup(groupId);
         }
      }
   }

   private void applyGroupDrafts(@Nonnull TriggerVolumeManager manager) {
      for (TriggerVolumeInspectorDrafts.GroupDraft draft : this.groupDrafts.values()) {
         if (!this.deletedGroups.contains(draft.originalId) && draft.dirty) {
            GroupEntry group = manager.getGroup(draft.originalId);
            if (group != null) {
               if (!draft.originalId.equals(draft.id)) {
                  manager.unregisterGroup(draft.originalId);
                  manager.registerGroup(draft.id, group);

                  for (VolumeEntry volume : manager.getVolumesMap().values()) {
                     if (draft.originalId.equals(volume.getGroupId())) {
                        volume.setGroupId(draft.id);
                     }
                  }

                  TriggerVolumeInspectorDrafts.remapVolumeGroupId(this.volumeDrafts.values(), draft.originalId, draft.id);
               }

               draft.applyTo(group);
            }
         }
      }
   }

   private void applyVolumeDrafts(@Nonnull TriggerVolumeManager manager) {
      for (TriggerVolumeInspectorDrafts.VolumeDraft draft : this.volumeDrafts.values()) {
         if (!this.deletedVolumes.contains(draft.originalId) && draft.dirty) {
            VolumeEntry volume = manager.getVolume(draft.originalId);
            if (volume != null) {
               String oldId = draft.originalId;
               String oldGroupId = volume.getGroupId();
               if (!oldId.equals(draft.id)) {
                  volume = manager.renameVolume(oldId, draft.id);
                  if (volume == null) {
                     continue;
                  }

                  manager.notifyViewersRemove(oldId);
               }

               draft.applyTo(volume);
               if (oldGroupId != null && !oldGroupId.equals(draft.groupId)) {
                  GroupEntry oldGroup = manager.getGroup(oldGroupId);
                  if (oldGroup != null) {
                     oldGroup.removeMember(oldId);
                  }
               }

               if (draft.groupId != null) {
                  GroupEntry group = manager.getGroup(draft.groupId);
                  if (group != null) {
                     group.addMember(draft.id);
                  }
               }

               manager.notifyViewersAdd(volume);
            }
         }
      }
   }

   private void buildAddEffectDropdown(@Nonnull UICommandBuilder cmd) {
      boolean isCondition = this.addTargetKind == TriggerVolumeInspectorPage.EffectListKind.CONDITION;
      List<String> typeIds = isCondition ? getSortedConditionTypeIds() : getSortedTypeIds();
      ObjectArrayList<DropdownEntryInfo> entries = new ObjectArrayList();

      for (String typeId : typeIds) {
         entries.add(new DropdownEntryInfo(this.typeDropdownLabel(typeId, isCondition), typeId));
      }

      cmd.set("#AddEffectDropdown.Entries", entries);
      if (!typeIds.isEmpty()) {
         cmd.set("#AddEffectDropdown.Value", typeIds.get(0));
      }
   }

   private void buildAddEventTypeDropdown(@Nonnull UICommandBuilder cmd) {
      ObjectArrayList<DropdownEntryInfo> entries = new ObjectArrayList();

      for (TriggerEventType eventType : TriggerEventType.values()) {
         entries.add(
            new DropdownEntryInfo(
               LocalizableString.fromMessageId("server.customUI.triggerVolumeEffectEditor.addEventType." + eventType.name()), eventType.name()
            )
         );
      }

      cmd.set("#AddEventTypeDropdown.Entries", entries);
      cmd.set("#AddEventTypeDropdown.Value", this.addEventType.name());
   }

   private void buildAddTargetDropdown(@Nonnull UICommandBuilder cmd) {
      ObjectArrayList<DropdownEntryInfo> entries = new ObjectArrayList();
      entries.add(
         new DropdownEntryInfo(
            LocalizableString.fromMessageId("server.customUI.triggerVolumeEffectEditor.addTarget.conditions"),
            TriggerVolumeInspectorPage.EffectListKind.CONDITION.name()
         )
      );
      entries.add(
         new DropdownEntryInfo(
            LocalizableString.fromMessageId("server.customUI.triggerVolumeEffectEditor.addTarget.successEffects"),
            TriggerVolumeInspectorPage.EffectListKind.EFFECT.name()
         )
      );
      entries.add(
         new DropdownEntryInfo(
            LocalizableString.fromMessageId("server.customUI.triggerVolumeEffectEditor.addTarget.rejectionEffects"),
            TriggerVolumeInspectorPage.EffectListKind.REJECTION_EFFECT.name()
         )
      );
      cmd.set("#AddTargetDropdown.Entries", entries);
      cmd.set("#AddTargetDropdown.Value", this.addTargetKind.name());
   }

   private void buildEffectList(@Nonnull UICommandBuilder cmd, @Nonnull UIEventBuilder evt) {
      cmd.clear("#EffectListContainer");
      int childIndex = 0;
      childIndex = this.appendInheritedGroupSections(cmd, evt, childIndex);
      childIndex = this.appendVolumeEffectsSection(cmd, childIndex, childIndex > 0 || this.selectedIsGroup);
      this.appendEventCategoryGroups(cmd, evt, childIndex, TriggerVolumeInspectorPage.EventCategoryScope.VOLUME, null);
      Object selectedItem = this.getSelectedItem();
      cmd.set("#DuplicateEffectButton.Disabled", selectedItem == null);
      cmd.set("#RemoveEffectButton.Disabled", selectedItem == null);
      cmd.set("#MoveEffectUpButton.Disabled", selectedItem == null || this.selectedEffectIndex <= 0);
      cmd.set("#MoveEffectDownButton.Disabled", selectedItem == null || this.selectedEffectIndex >= this.getItemCount(this.selectedKind) - 1);
   }

   @Nonnull
   private Message effectSectionLabel(@Nonnull TriggerVolumeInspectorPage.EffectListKind kind, boolean inherited) {
      String baseKey = switch (kind) {
         case CONDITION -> "conditions";
         case EFFECT -> "successEffects";
         case REJECTION_EFFECT -> "rejectionEffects";
      };
      String ownerKey = inherited ? "group" : "volume";
      return Message.translation("server.customUI.triggerVolumeEffectEditor." + ownerKey + "." + baseKey);
   }

   @Nonnull
   private Message effectRowLabel(int index, @Nonnull String typeId, boolean isCondition) {
      return Message.raw(index + ". ").insert(this.typeMessage(typeId, isCondition));
   }

   @Nonnull
   private Message typeMessage(@Nonnull String typeId, boolean isCondition) {
      String langKey = typeLangKey(typeId, isCondition);
      return messageExists(langKey) ? Message.translation(langKey) : Message.raw(humanizeTypeId(typeId));
   }

   @Nonnull
   private LocalizableString typeDropdownLabel(@Nonnull String typeId, boolean isCondition) {
      String langKey = typeLangKey(typeId, isCondition);
      return messageExists(langKey) ? LocalizableString.fromMessageId(langKey) : LocalizableString.fromString(humanizeTypeId(typeId));
   }

   @Nonnull
   private static String typeLangKey(@Nonnull String typeId, boolean isCondition) {
      return "server.customUI.triggerVolumeEffectEditor." + (isCondition ? "conditionType." : "effectType.") + typeId;
   }

   private int appendInheritedGroupSections(@Nonnull UICommandBuilder cmd, @Nonnull UIEventBuilder evt, int childIndex) {
      TriggerVolumeInspectorDrafts.GroupDraft groupDraft = this.selectedInheritedGroupDraft();
      if (groupDraft != null && (!groupDraft.conditions.isEmpty() || !groupDraft.rejectionEffects.isEmpty() || !groupDraft.effects.isEmpty())) {
         cmd.append("#EffectListContainer", "Pages/TriggerVolume/TriggerVolumeInspectorEffectOwnerSectionLabel.ui");
         cmd.set(
            "#EffectListContainer[" + childIndex + "].Text",
            Message.translation("server.customUI.triggerVolumeEffectEditor.inheritedFromGroup").param("group", groupDraft.id)
         );
         return this.appendEventCategoryGroups(cmd, evt, ++childIndex, TriggerVolumeInspectorPage.EventCategoryScope.GROUP, groupDraft);
      } else {
         return childIndex;
      }
   }

   private int appendVolumeEffectsSection(@Nonnull UICommandBuilder cmd, int childIndex, boolean showLabel) {
      if (this.currentConditions().isEmpty()
         && this.currentEffects(TriggerVolumeInspectorPage.EffectListKind.REJECTION_EFFECT).isEmpty()
         && this.currentEffects(TriggerVolumeInspectorPage.EffectListKind.EFFECT).isEmpty()) {
         return childIndex;
      } else if (!showLabel) {
         return childIndex;
      } else {
         if (childIndex > 0 && !this.selectedIsGroup) {
            childIndex = this.appendEventCategorySpacer(cmd, childIndex);
         }

         cmd.append("#EffectListContainer", "Pages/TriggerVolume/TriggerVolumeInspectorEffectOwnerSectionLabel.ui");
         Message label = this.selectedIsGroup
            ? Message.translation("server.customUI.triggerVolumeEffectEditor.groupEffects")
            : Message.translation("server.customUI.triggerVolumeEffectEditor.volumeEffects");
         cmd.set("#EffectListContainer[" + childIndex + "].Text", label);
         return childIndex + 1;
      }
   }

   private int appendEventCategoryGroups(
      @Nonnull UICommandBuilder cmd,
      @Nonnull UIEventBuilder evt,
      int childIndex,
      @Nonnull TriggerVolumeInspectorPage.EventCategoryScope scope,
      @Nullable TriggerVolumeInspectorDrafts.GroupDraft groupDraft
   ) {
      for (TriggerEventType eventType : TriggerEventType.values()) {
         int totalCount = this.getEventCategoryItemCount(scope, groupDraft, eventType);
         if (totalCount != 0) {
            childIndex = this.appendEventCategoryHeader(cmd, evt, childIndex, scope, eventType, totalCount);
            if (!this.isEventCategoryCollapsed(scope, eventType)) {
               childIndex = this.appendEventSection(cmd, evt, childIndex, scope, groupDraft, eventType, TriggerVolumeInspectorPage.EffectListKind.CONDITION);
               childIndex = this.appendEventSection(
                  cmd, evt, childIndex, scope, groupDraft, eventType, TriggerVolumeInspectorPage.EffectListKind.REJECTION_EFFECT
               );
               childIndex = this.appendEventSection(cmd, evt, childIndex, scope, groupDraft, eventType, TriggerVolumeInspectorPage.EffectListKind.EFFECT);
               childIndex = this.appendEventCategorySpacer(cmd, childIndex);
            }
         }
      }

      return childIndex;
   }

   private int appendEventCategoryHeader(
      @Nonnull UICommandBuilder cmd,
      @Nonnull UIEventBuilder evt,
      int childIndex,
      @Nonnull TriggerVolumeInspectorPage.EventCategoryScope scope,
      @Nonnull TriggerEventType eventType,
      int totalCount
   ) {
      String selector = "#EffectListContainer[" + childIndex + "]";
      String togglePrefix = this.isEventCategoryCollapsed(scope, eventType) ? ">" : "v";
      cmd.append("#EffectListContainer", "Pages/TriggerVolume/TriggerVolumeInspectorEventCategoryHeader.ui");
      cmd.set(selector + ".Text", eventCategoryLabel(scope, eventType).param("state", togglePrefix).param("count", totalCount));
      evt.addEventBinding(
         CustomUIEventBindingType.Activating,
         selector,
         new EventData()
            .append("Action", TriggerVolumeInspectorPage.Action.ToggleEventCategory.name())
            .append("EventType", eventType.name())
            .append("EventCategoryScope", scope.name())
      );
      return childIndex + 1;
   }

   private int appendEventSection(
      @Nonnull UICommandBuilder cmd,
      @Nonnull UIEventBuilder evt,
      int childIndex,
      @Nonnull TriggerVolumeInspectorPage.EventCategoryScope scope,
      @Nullable TriggerVolumeInspectorDrafts.GroupDraft groupDraft,
      @Nonnull TriggerEventType eventType,
      @Nonnull TriggerVolumeInspectorPage.EffectListKind kind
   ) {
      List<Integer> indices = this.getEventItemIndices(scope, groupDraft, eventType, kind);
      if (indices.isEmpty()) {
         return childIndex;
      } else {
         cmd.append("#EffectListContainer", "Pages/TriggerVolume/TriggerVolumeInspectorEventSectionLabel.ui");
         cmd.set(
            "#EffectListContainer[" + childIndex + "] #Label.Text", this.effectSectionLabel(kind, scope == TriggerVolumeInspectorPage.EventCategoryScope.GROUP)
         );
         childIndex++;

         for (int i = 0; i < indices.size(); i++) {
            Integer itemIndex = indices.get(i);
            String selector = "#EffectListContainer[" + childIndex + "]";
            String typeId = scope == TriggerVolumeInspectorPage.EventCategoryScope.GROUP
               ? getGroupItemTypeId(kind, itemIndex, groupDraft)
               : this.getItemTypeId(kind, itemIndex);
            Message label = this.effectRowLabel(i + 1, typeId, kind == TriggerVolumeInspectorPage.EffectListKind.CONDITION);
            cmd.append("#EffectListContainer", "Pages/TriggerVolume/TriggerVolumeInspectorEffectRow.ui");
            cmd.set(selector + " #Label.TextSpans", label);
            if (scope == TriggerVolumeInspectorPage.EventCategoryScope.GROUP) {
               cmd.set(selector + ".Style", INHERITED_EFFECT_ROW_STYLE);
               cmd.set(selector + " #Label.Style", INHERITED_EFFECT_LABEL_STYLE);
            } else {
               boolean selectedEffect = kind == this.selectedKind && itemIndex == this.selectedEffectIndex;
               cmd.set(selector + ".Style", selectedEffect ? SELECTED_EFFECT_ROW_STYLE : NORMAL_EFFECT_ROW_STYLE);
               cmd.set(selector + " #Label.Style", selectedEffect ? SELECTED_EFFECT_LABEL_STYLE : NORMAL_EFFECT_LABEL_STYLE);
               evt.addEventBinding(
                  CustomUIEventBindingType.Activating,
                  selector,
                  new EventData()
                     .append("Action", TriggerVolumeInspectorPage.Action.SelectEffect.name())
                     .append("EffectListKind", kind.name())
                     .append("EffectIndex", String.valueOf(itemIndex))
               );
            }

            childIndex++;
         }

         return childIndex;
      }
   }

   private int appendEventCategorySpacer(@Nonnull UICommandBuilder cmd, int childIndex) {
      cmd.append("#EffectListContainer", "Pages/TriggerVolume/TriggerVolumeInspectorEventCategorySpacer.ui");
      return childIndex + 1;
   }

   @Nonnull
   private static Message eventCategoryLabel(@Nonnull TriggerVolumeInspectorPage.EventCategoryScope scope, @Nonnull TriggerEventType eventType) {
      Message label = Message.translation("server.customUI.triggerVolumeEffectEditor.eventCategory." + eventType.name());
      return scope == TriggerVolumeInspectorPage.EventCategoryScope.GROUP
         ? label.insert(Message.translation("server.customUI.triggerVolumeEffectEditor.eventCategoryInheritedSuffix"))
         : label;
   }

   private int getEventCategoryItemCount(
      @Nonnull TriggerVolumeInspectorPage.EventCategoryScope scope,
      @Nullable TriggerVolumeInspectorDrafts.GroupDraft groupDraft,
      @Nonnull TriggerEventType eventType
   ) {
      int totalCount = 0;

      for (TriggerVolumeInspectorPage.EffectListKind kind : TriggerVolumeInspectorPage.EffectListKind.values()) {
         totalCount += this.getEventItemIndices(scope, groupDraft, eventType, kind).size();
      }

      return totalCount;
   }

   @Nonnull
   private List<Integer> getEventItemIndices(
      @Nonnull TriggerVolumeInspectorPage.EventCategoryScope scope,
      @Nullable TriggerVolumeInspectorDrafts.GroupDraft groupDraft,
      @Nonnull TriggerEventType eventType,
      @Nonnull TriggerVolumeInspectorPage.EffectListKind kind
   ) {
      ArrayList<Integer> indices = new ArrayList<>();
      if (scope == TriggerVolumeInspectorPage.EventCategoryScope.GROUP && groupDraft == null) {
         return indices;
      } else {
         int count = scope == TriggerVolumeInspectorPage.EventCategoryScope.GROUP ? getGroupItemCount(kind, groupDraft) : this.getItemCount(kind);

         for (int i = 0; i < count; i++) {
            TriggerEventType itemEventType = scope == TriggerVolumeInspectorPage.EventCategoryScope.GROUP
               ? getGroupItemEventType(kind, i, groupDraft)
               : this.getItemEventType(kind, i);
            if (normalizeEventType(itemEventType) == eventType) {
               indices.add(i);
            }
         }

         return indices;
      }
   }

   private boolean isEventCategoryCollapsed(@Nonnull TriggerVolumeInspectorPage.EventCategoryScope scope, @Nonnull TriggerEventType eventType) {
      return this.collapsedEventCategories(scope).contains(eventType);
   }

   private void setEventCategoryExpanded(@Nonnull TriggerVolumeInspectorPage.EventCategoryScope scope, @Nonnull TriggerEventType eventType) {
      this.collapsedEventCategories(scope).remove(eventType);
   }

   @Nonnull
   private EnumSet<TriggerEventType> collapsedEventCategories(@Nonnull TriggerVolumeInspectorPage.EventCategoryScope scope) {
      return scope == TriggerVolumeInspectorPage.EventCategoryScope.GROUP ? this.collapsedGroupEventCategories : this.collapsedVolumeEventCategories;
   }

   @Nonnull
   private static TriggerEventType normalizeEventType(@Nullable TriggerEventType eventType) {
      return eventType != null ? eventType : TriggerEventType.ENTER;
   }

   private void buildEffectDetailPanel(@Nonnull UICommandBuilder cmd, @Nonnull UIEventBuilder evt) {
      cmd.clear("#EffectDetailPanel");
      Object selected = this.getSelectedItem();
      if (selected == null) {
         cmd.set("#NoEffectSelectionLabel.Visible", true);
         cmd.set("#EffectDetailPanel.Visible", false);
      } else {
         cmd.set("#NoEffectSelectionLabel.Visible", false);
         cmd.set("#EffectDetailPanel.Visible", true);
         int row = 0;
         if (selected instanceof TriggerCondition condition) {
            String typeId = getConditionTypeId(condition);
            row = this.addEffectDropdownRow(
               cmd,
               evt,
               row,
               Message.translation("server.customUI.triggerVolumeEffectEditor.baseField.event"),
               "Event",
               Message.translation("server.customUI.triggerVolumeEffectEditor.baseField.event.tooltip"),
               Arrays.stream(TriggerEventType.values()).map(Enum::name).toList(),
               condition.getEventType() != null ? condition.getEventType().name() : TriggerEventType.ENTER.name()
            );
            BuilderCodec<TriggerCondition> codec = getConditionBuilderCodecFor(typeId);
            if (codec != null) {
               BsonDocument encoded = encodeCondition(codec, condition);

               for (Entry<String, List<BuilderField<TriggerCondition, ?>>> entry : codec.getEntries().entrySet()) {
                  String key = entry.getKey();
                  if (!"Event".equals(key) && !entry.getValue().isEmpty()) {
                     BuilderField<TriggerCondition, ?> field = entry.getValue().getLast();
                     row = this.addEffectFieldRow(cmd, evt, row, typeId, key, field.getCodec().getChildCodec(), encoded.get(key));
                  }
               }
            }
         } else {
            TriggerEffect effect = (TriggerEffect)selected;
            String typeId = getTypeId(effect);
            row = this.addEffectDropdownRow(
               cmd,
               evt,
               row,
               Message.translation("server.customUI.triggerVolumeEffectEditor.baseField.event"),
               "Event",
               Message.translation("server.customUI.triggerVolumeEffectEditor.baseField.event.tooltip"),
               Arrays.stream(TriggerEventType.values()).map(Enum::name).toList(),
               effect.getEventType() != null ? effect.getEventType().name() : TriggerEventType.ENTER.name()
            );
            row = this.addEffectNumberRow(
               cmd,
               evt,
               row,
               Message.translation("server.customUI.triggerVolumeEffectEditor.baseField.interval"),
               "Interval",
               Message.translation("server.customUI.triggerVolumeEffectEditor.baseField.interval.tooltip"),
               String.valueOf(effect.getInterval()),
               2
            );
            row = this.addEffectNumberRow(
               cmd,
               evt,
               row,
               Message.translation("server.customUI.triggerVolumeEffectEditor.effectDelay"),
               "Delay",
               Message.translation("server.customUI.triggerVolumeEffectEditor.effectDelay.tooltip"),
               String.valueOf(effect.getDelay()),
               1
            );
            BuilderCodec<TriggerEffect> codec = getBuilderCodecFor(typeId);
            if (codec != null) {
               BsonDocument encoded = encodeEffect(codec, effect);

               for (Entry<String, List<BuilderField<TriggerEffect, ?>>> entryx : codec.getEntries().entrySet()) {
                  String key = entryx.getKey();
                  if (!"Event".equals(key) && !"Interval".equals(key) && !"Delay".equals(key) && !entryx.getValue().isEmpty()) {
                     BuilderField<TriggerEffect, ?> field = entryx.getValue().getLast();
                     row = this.addEffectFieldRow(cmd, evt, row, typeId, key, field.getCodec().getChildCodec(), encoded.get(key));
                  }
               }
            }

            if (effect instanceof PastePrefabEffect) {
               this.addPastePrefabPreviewButtonRow(cmd, evt, row);
            }
         }
      }
   }

   private int addEffectFieldRow(
      @Nonnull UICommandBuilder cmd,
      @Nonnull UIEventBuilder evt,
      int row,
      @Nonnull String typeId,
      @Nonnull String key,
      @Nonnull Codec<?> childCodec,
      @Nullable BsonValue bsonValue
   ) {
      if (childCodec == Codec.BOOLEAN) {
         boolean value = bsonValue instanceof BsonBoolean bsonBoolean && bsonBoolean.getValue();
         return this.addEffectCheckboxRow(cmd, evt, row, typeId, key, value);
      } else if (childCodec == Codec.FLOAT) {
         String value = bsonValue instanceof BsonDouble bsonDouble ? String.valueOf((float)bsonDouble.getValue()) : "0.0";
         return this.addEffectNumberRow(cmd, evt, row, typeId, key, value, 2);
      } else if (childCodec == Codec.DOUBLE) {
         String value = bsonValue instanceof BsonDouble bsonDouble ? String.valueOf(bsonDouble.getValue()) : "0.0";
         return this.addEffectNumberRow(cmd, evt, row, typeId, key, value, 2);
      } else if (childCodec == Codec.INTEGER) {
         String value = bsonValue instanceof BsonInt32 bsonInt ? String.valueOf(bsonInt.getValue()) : "0";
         return this.addEffectNumberRow(cmd, evt, row, typeId, key, value, 0);
      } else if (childCodec == Codec.LONG) {
         String value = bsonValue instanceof BsonInt64 bsonLong ? String.valueOf(bsonLong.getValue()) : "0";
         return this.addEffectNumberRow(cmd, evt, row, typeId, key, value, 0);
      } else if (childCodec == Vector3dUtil.CODEC) {
         double x = 0.0;
         double y = 0.0;
         double z = 0.0;
         if (bsonValue instanceof BsonDocument doc) {
            x = doc.get("X", new BsonDouble(0.0)).asDouble().getValue();
            y = doc.get("Y", new BsonDouble(0.0)).asDouble().getValue();
            z = doc.get("Z", new BsonDouble(0.0)).asDouble().getValue();
         }

         return this.addEffectVec3Row(cmd, evt, row, typeId, key, x, y, z);
      } else if (!(childCodec instanceof EnumCodec<?> enumCodec)) {
         if (childCodec == Codec.STRING) {
            String value = bsonValue instanceof BsonString bsonString ? bsonString.getValue() : "";
            return getAssetSourceForField(typeId, key) != null
               ? this.addAssetPickerRow(cmd, evt, row, typeId, key, value)
               : this.addEffectTextRow(cmd, evt, row, typeId, key, value);
         } else if (childCodec == Codec.STRING_ARRAY) {
            String value = bsonValue instanceof BsonArray bsonArray
               ? String.join(", ", bsonArray.stream().filter(BsonString.class::isInstance).map(BsonString.class::cast).map(BsonString::getValue).toList())
               : "";
            return getAssetSourceForField(typeId, key) != null
               ? this.addAssetPickerRow(cmd, evt, row, typeId, key, value)
               : this.addEffectTextRow(cmd, evt, row, typeId, key, value);
         } else {
            String value = bsonValue != null ? bsonValueToString(bsonValue) : "";
            return this.addEffectTextRow(cmd, evt, row, typeId, key, value);
         }
      } else {
         String value = "";
         if (bsonValue != null) {
            try {
               Enum<?> decoded = enumCodec.decode(bsonValue, ExtraInfo.THREAD_LOCAL.get());
               value = enumCodec.getEnumKeys()[decoded.ordinal()];
            } catch (Exception var16) {
               value = bsonValue instanceof BsonString bsonString ? bsonString.getValue() : "";
            }
         }

         return this.addEffectDropdownRow(cmd, evt, row, typeId, key, List.of(enumCodec.getEnumKeys()), value);
      }
   }

   private int addEffectTextRow(
      @Nonnull UICommandBuilder cmd, @Nonnull UIEventBuilder evt, int row, @Nonnull String typeId, @Nonnull String key, @Nonnull String value
   ) {
      String sel = "#EffectDetailPanel[" + row + "]";
      cmd.append("#EffectDetailPanel", "Pages/TriggerVolume/TriggerVolumeInspectorTextRow.ui");
      cmd.set(sel + " #Label.Text", fieldLabel(typeId, key));
      setEffectFieldTooltip(cmd, sel, typeId, key);
      setEffectFieldPlaceholder(cmd, sel, typeId, key);
      cmd.set(sel + " #Input.Value", value);
      evt.addEventBinding(CustomUIEventBindingType.ValueChanged, sel + " #Input", paramEvent(key, sel + " #Input.Value"), false);
      return row + 1;
   }

   private int addEffectNumberRow(
      @Nonnull UICommandBuilder cmd, @Nonnull UIEventBuilder evt, int row, @Nonnull String typeId, @Nonnull String key, @Nonnull String value, int decimals
   ) {
      return this.addEffectNumberRow(cmd, evt, row, typeId, key, fieldLabel(typeId, key), null, value, decimals);
   }

   private int addEffectNumberRow(
      @Nonnull UICommandBuilder cmd,
      @Nonnull UIEventBuilder evt,
      int row,
      @Nonnull Message label,
      @Nonnull String key,
      @Nullable Message tooltip,
      @Nonnull String value,
      int decimals
   ) {
      return this.addEffectNumberRow(cmd, evt, row, "", key, label, tooltip, value, decimals);
   }

   private int addEffectNumberRow(
      @Nonnull UICommandBuilder cmd,
      @Nonnull UIEventBuilder evt,
      int row,
      @Nonnull String typeId,
      @Nonnull String key,
      @Nonnull Object label,
      @Nullable Message tooltip,
      @Nonnull String value,
      int decimals
   ) {
      String sel = "#EffectDetailPanel[" + row + "]";
      cmd.append(
         "#EffectDetailPanel", decimals > 0 ? "Pages/TriggerVolume/TriggerVolumeInspectorNumberRow.ui" : "Pages/TriggerVolume/TriggerVolumeInspectorIntRow.ui"
      );
      if (label instanceof Message message) {
         cmd.set(sel + " #Label.Text", message);
      } else {
         cmd.set(sel + " #Label.Text", label.toString());
      }

      if (tooltip != null) {
         cmd.set(sel + " #Label.TooltipText", tooltip);
      } else {
         setEffectFieldTooltip(cmd, sel, typeId, key);
      }

      if (isNonNegativeNumericField(typeId, key)) {
         cmd.set(sel + " #Input.Format.MinValue", 0.0);
      }

      try {
         cmd.set(sel + " #Input.Value", Double.parseDouble(value));
      } catch (NumberFormatException var12) {
         cmd.set(sel + " #Input.Value", 0.0);
      }

      evt.addEventBinding(CustomUIEventBindingType.ValueChanged, sel + " #Input", numericParamEvent(key, sel + " #Input.Value"), false);
      return row + 1;
   }

   private int addEffectCheckboxRow(
      @Nonnull UICommandBuilder cmd, @Nonnull UIEventBuilder evt, int row, @Nonnull String typeId, @Nonnull String key, boolean value
   ) {
      String sel = "#EffectDetailPanel[" + row + "]";
      cmd.append("#EffectDetailPanel", "Pages/TriggerVolume/TriggerVolumeInspectorCheckboxRow.ui");
      cmd.set(sel + " #Label.Text", fieldLabel(typeId, key));
      setEffectFieldTooltip(cmd, sel, typeId, key);
      cmd.set(sel + " #Input.Value", value);
      evt.addEventBinding(CustomUIEventBindingType.ValueChanged, sel + " #Input", boolParamEvent(key, sel + " #Input.Value"), false);
      return row + 1;
   }

   private int addEffectDropdownRow(
      @Nonnull UICommandBuilder cmd,
      @Nonnull UIEventBuilder evt,
      int row,
      @Nonnull String typeId,
      @Nonnull String key,
      @Nonnull List<String> options,
      @Nonnull String value
   ) {
      String sel = "#EffectDetailPanel[" + row + "]";
      cmd.append("#EffectDetailPanel", "Pages/TriggerVolume/TriggerVolumeInspectorDropdownRow.ui");
      cmd.set(sel + " #Label.Text", fieldLabel(typeId, key));
      setEffectFieldTooltip(cmd, sel, typeId, key);
      ObjectArrayList<DropdownEntryInfo> entries = new ObjectArrayList();

      for (String opt : options) {
         entries.add(new DropdownEntryInfo(this.optionLabel(typeId, key, opt), opt, this.optionTooltip(typeId, key, opt)));
      }

      cmd.set(sel + " #Input.Entries", entries);
      cmd.set(sel + " #Input.Value", value);
      evt.addEventBinding(CustomUIEventBindingType.ValueChanged, sel + " #Input", paramEvent(key, sel + " #Input.Value"), false);
      return row + 1;
   }

   private int addEffectDropdownRow(
      @Nonnull UICommandBuilder cmd,
      @Nonnull UIEventBuilder evt,
      int row,
      @Nonnull Message label,
      @Nonnull String key,
      @Nullable Message tooltip,
      @Nonnull List<String> options,
      @Nonnull String value
   ) {
      String sel = "#EffectDetailPanel[" + row + "]";
      cmd.append("#EffectDetailPanel", "Pages/TriggerVolume/TriggerVolumeInspectorDropdownRow.ui");
      cmd.set(sel + " #Label.Text", label);
      if (tooltip != null) {
         cmd.set(sel + " #Label.TooltipText", tooltip);
      }

      ObjectArrayList<DropdownEntryInfo> entries = new ObjectArrayList();

      for (String opt : options) {
         entries.add(new DropdownEntryInfo(this.optionLabel("baseField", key, opt), opt, this.optionTooltip("baseField", key, opt)));
      }

      cmd.set(sel + " #Input.Entries", entries);
      cmd.set(sel + " #Input.Value", value);
      evt.addEventBinding(CustomUIEventBindingType.ValueChanged, sel + " #Input", paramEvent(key, sel + " #Input.Value"), false);
      return row + 1;
   }

   @Nonnull
   private LocalizableString optionLabel(@Nonnull String typeId, @Nonnull String key, @Nonnull String value) {
      for (String optionKey : optionLangKeys(typeId, key, value, "")) {
         if (messageExists(optionKey)) {
            return LocalizableString.fromMessageId(optionKey);
         }
      }

      this.logMissingOptionKey(optionLangKeys(typeId, key, value, "").getFirst());
      return LocalizableString.fromString(humanizeEnumValue(value));
   }

   @Nullable
   private LocalizableString optionTooltip(@Nonnull String typeId, @Nonnull String key, @Nonnull String value) {
      for (String optionKey : optionLangKeys(typeId, key, value, ".tooltip")) {
         if (messageExists(optionKey)) {
            return LocalizableString.fromMessageId(optionKey);
         }
      }

      return null;
   }

   @Nonnull
   private static List<String> optionLangKeys(@Nonnull String typeId, @Nonnull String key, @Nonnull String value, @Nonnull String suffix) {
      String upperSnakeValue = toUpperSnake(value);
      String upperValue = value.toUpperCase(Locale.ROOT);
      ArrayList<String> keys = new ArrayList<>();
      appendOptionLangKeys(
         keys, "server.customUI.triggerVolumeEffectEditor.field." + typeId + "." + key + ".option.", value, upperSnakeValue, upperValue, suffix
      );
      appendOptionLangKeys(keys, "server.customUI.triggerVolumeEffectEditor.field.common." + key + ".option.", value, upperSnakeValue, upperValue, suffix);
      return keys;
   }

   private static void appendOptionLangKeys(
      @Nonnull List<String> keys,
      @Nonnull String prefix,
      @Nonnull String value,
      @Nonnull String upperSnakeValue,
      @Nonnull String upperValue,
      @Nonnull String suffix
   ) {
      keys.add(prefix + value + suffix);
      if (!upperSnakeValue.equals(value)) {
         keys.add(prefix + upperSnakeValue + suffix);
      }

      if (!upperValue.equals(value) && !upperValue.equals(upperSnakeValue)) {
         keys.add(prefix + upperValue + suffix);
      }
   }

   private static boolean messageExists(@Nonnull String langKey) {
      I18nModule i18n = I18nModule.get();
      return i18n != null && i18n.getMessage("en-US", langKey) != null;
   }

   private void logMissingOptionKey(@Nonnull String langKey) {
      if (this.missingOptionLangKeys.add(langKey)) {
         LOGGER.at(Level.FINE).log("Missing trigger volume dropdown option label '%s'", langKey);
      }
   }

   @Nonnull
   private static String humanizeEnumValue(@Nonnull String value) {
      String normalized = toUpperSnake(value);
      if (normalized.startsWith("PERCENT") && normalized.length() > "PERCENT".length()) {
         normalized = "PERCENT_" + normalized.substring("PERCENT".length());
      }

      String[] words = normalized.toLowerCase(Locale.ROOT).split("_+");
      StringBuilder result = new StringBuilder();

      for (String word : words) {
         if (!word.isEmpty()) {
            if (!result.isEmpty()) {
               result.append(' ');
            }

            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
         }
      }

      return result.isEmpty() ? value : result.toString();
   }

   @Nonnull
   private static String toUpperSnake(@Nonnull String value) {
      StringBuilder result = new StringBuilder();
      char previous = 0;

      for (int charIndex = 0; charIndex < value.length(); charIndex++) {
         char character = value.charAt(charIndex);
         if (character == '_') {
            if (!result.isEmpty() && result.charAt(result.length() - 1) != '_') {
               result.append('_');
            }
         } else {
            if (charIndex > 0 && Character.isUpperCase(character) && (Character.isLowerCase(previous) || Character.isDigit(previous))) {
               result.append('_');
            }

            result.append(Character.toUpperCase(character));
         }

         previous = character;
      }

      return result.toString();
   }

   private int addEffectVec3Row(
      @Nonnull UICommandBuilder cmd, @Nonnull UIEventBuilder evt, int row, @Nonnull String typeId, @Nonnull String key, double x, double y, double z
   ) {
      String sel = "#EffectDetailPanel[" + row + "]";
      cmd.append("#EffectDetailPanel", "Pages/TriggerVolume/TriggerVolumeInspectorVec3Row.ui");
      cmd.set(sel + " #Label.Text", fieldLabel(typeId, key));
      setEffectFieldTooltip(cmd, sel, typeId, key);
      cmd.set(sel + " #X.Value", x);
      cmd.set(sel + " #Y.Value", y);
      cmd.set(sel + " #Z.Value", z);

      for (String comp : List.of("X", "Y", "Z")) {
         evt.addEventBinding(
            CustomUIEventBindingType.ValueChanged,
            sel + " #" + comp,
            new EventData()
               .append("Action", TriggerVolumeInspectorPage.Action.UpdateParameter.name())
               .append("ParamKey", key)
               .append("@VecX", sel + " #X.Value")
               .append("@VecY", sel + " #Y.Value")
               .append("@VecZ", sel + " #Z.Value"),
            false
         );
      }

      return row + 1;
   }

   private int addAssetPickerRow(
      @Nonnull UICommandBuilder cmd, @Nonnull UIEventBuilder evt, int row, @Nonnull String typeId, @Nonnull String key, @Nonnull String value
   ) {
      String sel = "#EffectDetailPanel[" + row + "]";
      cmd.append("#EffectDetailPanel", "Pages/TriggerVolume/TriggerVolumeInspectorAssetPickerRow.ui");
      cmd.set(sel + " #Label.Text", fieldLabel(typeId, key));
      setEffectFieldTooltip(cmd, sel, typeId, key);
      if (value.isEmpty()) {
         cmd.set(sel + " #PickerLabel.Text", Message.translation("server.customUI.triggerVolumeEffectEditor.assetPicker.none"));
      } else {
         cmd.set(sel + " #PickerLabel.Text", value);
      }

      evt.addEventBinding(
         CustomUIEventBindingType.Activating,
         sel + " #PickerButton",
         new EventData().append("Action", TriggerVolumeInspectorPage.Action.OpenAssetPicker.name()).append("ParamKey", key)
      );
      return row + 1;
   }

   private int addPastePrefabPreviewButtonRow(@Nonnull UICommandBuilder cmd, @Nonnull UIEventBuilder evt, int row) {
      String sel = "#EffectDetailPanel[" + row + "]";
      cmd.append("#EffectDetailPanel", "Common/TextButton.ui");
      Message label = this.isPreviewingSelectedEffect()
         ? Message.translation("server.customUI.triggerVolumeEffectEditor.pastePrefab.preview.hide")
         : Message.translation("server.customUI.triggerVolumeEffectEditor.pastePrefab.preview.show");
      cmd.set(sel + " #Button.Text", label);
      cmd.set(sel + " #Button.TooltipText", Message.translation("server.customUI.triggerVolumeEffectEditor.pastePrefab.preview.tooltip"));
      evt.addEventBinding(
         CustomUIEventBindingType.Activating, sel + " #Button", new EventData().append("Action", TriggerVolumeInspectorPage.Action.TogglePrefabPreview.name())
      );
      return row + 1;
   }

   private void onSelectEffect(@Nonnull TriggerVolumeInspectorPage.PageData data) {
      try {
         TriggerVolumeInspectorPage.EffectListKind newKind = data.effectListKind != null
            ? TriggerVolumeInspectorPage.EffectListKind.valueOf(data.effectListKind)
            : TriggerVolumeInspectorPage.EffectListKind.EFFECT;
         int newIndex = Integer.parseInt(data.effectIndex);
         this.selectedKind = newKind;
         this.selectedEffectIndex = newIndex;
         this.rebuildAll();
      } catch (IllegalArgumentException var4) {
      }
   }

   private void onAddEffect(@Nonnull TriggerVolumeInspectorPage.PageData data) {
      if (data.effectType != null && !data.effectType.isBlank()) {
         TriggerVolumeInspectorPage.EffectListKind target = data.addTargetKind != null
            ? parseEffectListKind(data.addTargetKind, TriggerVolumeInspectorPage.EffectListKind.EFFECT)
            : this.addTargetKind;
         this.addTargetKind = target;
         TriggerEventType eventType = data.addEventType != null ? parseTriggerEventType(data.addEventType, this.addEventType) : this.addEventType;
         this.addEventType = eventType;
         if (target == TriggerVolumeInspectorPage.EffectListKind.CONDITION) {
            BuilderCodec<TriggerCondition> codec = getConditionBuilderCodecFor(data.effectType);
            if (codec == null) {
               this.playerRef.sendMessage(Message.translation("server.customUI.triggerVolumeEffectEditor.unknownType"));
            } else {
               TriggerCondition condition = codec.getSupplier().get();
               condition.setEventType(eventType);
               materializeConditionDefaults(codec, condition);
               this.currentConditions().add(condition);
               this.markSelectedDraftDirty();
               this.selectedKind = TriggerVolumeInspectorPage.EffectListKind.CONDITION;
               this.selectedEffectIndex = this.currentConditions().size() - 1;
               this.setEventCategoryExpanded(TriggerVolumeInspectorPage.EventCategoryScope.VOLUME, eventType);
               this.rebuildAll();
            }
         } else {
            BuilderCodec<TriggerEffect> codec = getBuilderCodecFor(data.effectType);
            if (codec == null) {
               this.playerRef.sendMessage(Message.translation("server.customUI.triggerVolumeEffectEditor.unknownType"));
            } else {
               TriggerEffect effect = codec.getSupplier().get();
               effect.setEventType(eventType);
               materializeDefaults(codec, effect);
               this.currentEffects(target).add(effect);
               this.markSelectedDraftDirty();
               this.selectedKind = target;
               this.selectedEffectIndex = this.currentEffects(target).size() - 1;
               this.setEventCategoryExpanded(TriggerVolumeInspectorPage.EventCategoryScope.VOLUME, eventType);
               this.rebuildAll();
            }
         }
      }
   }

   private void onRemoveEffect() {
      if (this.selectedEffectIndex >= 0 && this.selectedEffectIndex < this.getItemCount(this.selectedKind)) {
         if (this.isPreviewingSelectedEffect()) {
            this.hidePastePrefabPreview();
         }

         if (this.selectedKind == TriggerVolumeInspectorPage.EffectListKind.CONDITION) {
            this.currentConditions().remove(this.selectedEffectIndex);
         } else {
            this.currentEffects(this.selectedKind).remove(this.selectedEffectIndex);
         }

         this.markSelectedDraftDirty();
         if (this.selectedEffectIndex >= this.getItemCount(this.selectedKind)) {
            this.selectedEffectIndex = this.getItemCount(this.selectedKind) - 1;
         }

         this.rebuildAll();
      }
   }

   private void onDuplicateEffect() {
      if (this.selectedEffectIndex >= 0 && this.selectedEffectIndex < this.getItemCount(this.selectedKind)) {
         this.hidePastePrefabPreview();
         if (this.selectedKind == TriggerVolumeInspectorPage.EffectListKind.CONDITION) {
            List<TriggerCondition> conditions = this.currentConditions();
            conditions.add(this.selectedEffectIndex + 1, TriggerCondition.deepCopy(conditions.get(this.selectedEffectIndex)));
         } else {
            List<TriggerEffect> effects = this.currentEffects(this.selectedKind);
            effects.add(this.selectedEffectIndex + 1, TriggerEffect.deepCopy(effects.get(this.selectedEffectIndex)));
         }

         this.markSelectedDraftDirty();
         this.selectedEffectIndex++;
         this.rebuildAll();
      }
   }

   private void onMoveEffect(int direction) {
      if (this.selectedEffectIndex >= 0 && this.selectedEffectIndex < this.getItemCount(this.selectedKind)) {
         this.hidePastePrefabPreview();
         int targetIndex = this.selectedEffectIndex + direction;
         if (targetIndex >= 0 && targetIndex < this.getItemCount(this.selectedKind)) {
            if (this.selectedKind == TriggerVolumeInspectorPage.EffectListKind.CONDITION) {
               Collections.swap(this.currentConditions(), this.selectedEffectIndex, targetIndex);
            } else {
               Collections.swap(this.currentEffects(this.selectedKind), this.selectedEffectIndex, targetIndex);
            }

            this.markSelectedDraftDirty();
            this.selectedEffectIndex = targetIndex;
            this.rebuildAll();
         }
      }
   }

   private void onUpdateAddTarget(@Nonnull TriggerVolumeInspectorPage.PageData data) {
      if (data.addTargetKind != null) {
         this.addTargetKind = parseEffectListKind(data.addTargetKind, TriggerVolumeInspectorPage.EffectListKind.EFFECT);
         this.rebuildAll();
      }
   }

   private void onUpdateAddEventType(@Nonnull TriggerVolumeInspectorPage.PageData data) {
      if (data.addEventType != null) {
         this.addEventType = parseTriggerEventType(data.addEventType, this.addEventType);
         this.rebuildAll();
      }
   }

   private void onToggleEventCategory(@Nonnull TriggerVolumeInspectorPage.PageData data) {
      if (data.eventType != null && data.eventCategoryScope != null) {
         try {
            TriggerEventType eventType = TriggerEventType.valueOf(data.eventType);
            TriggerVolumeInspectorPage.EventCategoryScope scope = TriggerVolumeInspectorPage.EventCategoryScope.valueOf(data.eventCategoryScope);
            EnumSet<TriggerEventType> collapsedCategories = this.collapsedEventCategories(scope);
            if (!collapsedCategories.remove(eventType)) {
               collapsedCategories.add(eventType);
            }

            this.rebuildAll();
         } catch (IllegalArgumentException var5) {
         }
      }
   }

   private void onUpdateParameter(@Nonnull TriggerVolumeInspectorPage.PageData data) {
      Object selected = this.getSelectedItem();
      if (selected != null && data.paramKey != null) {
         String key = data.paramKey;
         if ("Event".equals(key)) {
            try {
               TriggerEventType eventType = TriggerEventType.valueOf(data.paramValue);
               if (selected instanceof TriggerCondition condition) {
                  condition.setEventType(eventType);
               } else {
                  ((TriggerEffect)selected).setEventType(eventType);
               }

               this.markSelectedDraftDirty();
               this.setEventCategoryExpanded(TriggerVolumeInspectorPage.EventCategoryScope.VOLUME, eventType);
            } catch (IllegalArgumentException var6) {
            }

            this.rebuildAll();
         } else if (selected instanceof TriggerCondition condition) {
            BuilderCodec<TriggerCondition> codec = getConditionBuilderCodecFor(getConditionTypeId(condition));
            if (codec != null) {
               this.applyCodecField(codec, condition, key, data);
               this.markSelectedDraftDirty();
            }
         } else {
            TriggerEffect effect = (TriggerEffect)selected;
            if ("Interval".equals(key)) {
               if (data.paramNumericValue != null) {
                  effect.setInterval(data.paramNumericValue.floatValue());
                  this.markSelectedDraftDirty();
               }
            } else if ("Delay".equals(key)) {
               if (data.paramNumericValue != null) {
                  effect.setDelay(data.paramNumericValue.floatValue());
                  this.markSelectedDraftDirty();
               }
            } else {
               BuilderCodec<TriggerEffect> codec = getBuilderCodecFor(getTypeId(effect));
               if (codec != null) {
                  this.applyCodecField(codec, effect, key, data);
                  this.markSelectedDraftDirty();
               }

               if (effect instanceof PastePrefabEffect && this.isPreviewingSelectedEffect()) {
                  if ("Position".equals(key) || "AtVolumeOrigin".equals(key)) {
                     this.refreshActivePastePrefabPreviewPosition();
                  } else if ("Prefab".equals(key) || "PrefabList".equals(key)) {
                     this.refreshActivePastePrefabPreview();
                  }
               }
            }
         }
      }
   }

   private void onTogglePrefabPreview() {
      if (this.isPreviewingSelectedEffect()) {
         this.hidePastePrefabPreview();
         this.rebuildAll();
      } else {
         if (this.getActivePastePrefabPreviewState() != null) {
            this.hidePastePrefabPreview();
         }

         if (this.getSelectedItem() instanceof PastePrefabEffect pastePrefabEffect) {
            Vector3d previewPosition = this.getPastePrefabPreviewPosition(pastePrefabEffect);
            if (previewPosition == null) {
               this.playerRef.sendMessage(Message.translation("server.customUI.triggerVolumeEffectEditor.pastePrefab.preview.noPrefab"));
            } else if (this.sendPastePrefabPreview(pastePrefabEffect, true)) {
               TriggerVolumesPlugin.get()
                  .setPastePrefabPreviewState(
                     this.playerRef.getUuid(),
                     new TriggerVolumesPlugin.PastePrefabPreviewState(
                        this.selectedWorld, this.selectedId, this.selectedIsGroup, this.selectedKind.name(), this.selectedEffectIndex, previewPosition
                     )
                  );
               this.rebuildAll();
            }
         }
      }
   }

   private boolean sendPastePrefabPreview(@Nonnull PastePrefabEffect effect, boolean includePrefabData) {
      Vector3d previewPosition = this.getPastePrefabPreviewPosition(effect);
      return previewPosition == null ? false : this.sendPastePrefabPreview(effect, previewPosition, includePrefabData);
   }

   private boolean sendPastePrefabPreview(@Nonnull PastePrefabEffect effect, @Nonnull Vector3d previewPosition, boolean includePrefabData) {
      ShowTriggerVolumePastePrefabPreview packet = new ShowTriggerVolumePastePrefabPreview();
      packet.position = new Vector3f((float)previewPosition.x(), (float)previewPosition.y(), (float)previewPosition.z());
      if (includePrefabData) {
         Path prefabPath = this.resolvePastePrefabPath(effect);
         if (prefabPath == null) {
            this.playerRef.sendMessage(Message.translation("server.customUI.triggerVolumeEffectEditor.pastePrefab.preview.noPrefab"));
            return false;
         }

         BlockSelection selection;
         try {
            selection = PrefabStore.get().getPrefab(prefabPath);
         } catch (Exception var8) {
            LOGGER.at(Level.WARNING).log("Failed to load PastePrefab preview '%s'", prefabPath, var8);
            this.playerRef.sendMessage(Message.translation("server.customUI.triggerVolumeEffectEditor.pastePrefab.preview.loadFailed"));
            return false;
         }

         if (selection == null) {
            this.playerRef.sendMessage(Message.translation("server.customUI.triggerVolumeEffectEditor.pastePrefab.preview.loadFailed"));
            return false;
         }

         this.fillPreviewPacket(packet, selection);
      }

      this.playerRef.getPacketHandler().write(packet);
      TriggerVolumesPlugin.PastePrefabPreviewState activeState = this.getActivePastePrefabPreviewState();
      if (activeState != null) {
         activeState.setLastSentPosition(previewPosition);
      }

      return true;
   }

   private void fillPreviewPacket(@Nonnull ShowTriggerVolumePastePrefabPreview packet, @Nonnull BlockSelection selection) {
      EditorBlocksChange editorPacket = selection.toPacket();
      packet.blocksChange = editorPacket.blocksChange;
      packet.fluidsChange = editorPacket.fluidsChange;
      packet.entityChanges = editorPacket.entityChanges;
      this.applyTintFromPlayerPosition(packet);
   }

   private void applyTintFromPlayerPosition(@Nonnull ShowTriggerVolumePastePrefabPreview packet) {
      Ref<EntityStore> playerEntityRef = this.playerRef.getReference();
      if (playerEntityRef == null) {
         packet.biomeTint = DEFAULT_BIOME_TINT;
         packet.waterTint = DEFAULT_WATER_TINT;
      } else {
         Store<EntityStore> store = playerEntityRef.getStore();
         World world = store.getExternalData().getWorld();
         if (world == null) {
            packet.biomeTint = DEFAULT_BIOME_TINT;
            packet.waterTint = DEFAULT_WATER_TINT;
         } else {
            Vector3d playerPosition = this.playerRef.getTransform().getPosition();
            int blockX = MathUtil.floor(playerPosition.x);
            int blockY = MathUtil.floor(playerPosition.y);
            int blockZ = MathUtil.floor(playerPosition.z);
            long chunkIndex = ChunkUtil.indexChunkFromBlock(blockX, blockZ);
            WorldChunk chunk = world.getNonTickingChunk(chunkIndex);
            if (chunk != null && chunk.getBlockChunk() != null) {
               BlockChunk blockChunk = chunk.getBlockChunk();
               packet.biomeTint = blockChunk.getTint(blockX, blockZ);
               int environmentId = blockChunk.getEnvironment(blockX, blockY, blockZ);
               Environment environment = Environment.getAssetMap().getAsset(environmentId);
               if (environment != null) {
                  Color waterColor = environment.getWaterTint();
                  if (waterColor != null) {
                     packet.waterTint = (waterColor.red & 255) << 16 | (waterColor.green & 255) << 8 | waterColor.blue & 255;
                     return;
                  }
               }

               packet.waterTint = DEFAULT_WATER_TINT;
            } else {
               packet.biomeTint = DEFAULT_BIOME_TINT;
               packet.waterTint = DEFAULT_WATER_TINT;
            }
         }
      }
   }

   @Nullable
   private Path resolvePastePrefabPath(@Nonnull PastePrefabEffect effect) {
      String directPrefab = effect.getPrefabRelPath();
      if (directPrefab != null && !directPrefab.isBlank()) {
         return PastePrefabEffect.resolveDirectPrefabPath(directPrefab.trim());
      } else {
         String prefabListId = effect.getPrefabListId();
         if (prefabListId != null && !prefabListId.isBlank()) {
            PrefabListAsset prefabListAsset = PrefabListAsset.getAssetMap().getAsset(prefabListId);
            return prefabListAsset != null ? prefabListAsset.getRandomPrefab() : null;
         } else {
            return null;
         }
      }
   }

   @Nullable
   private Vector3d getPastePrefabPreviewPosition(@Nonnull PastePrefabEffect effect) {
      Vector3d origin = this.getCurrentPreviewOrigin();
      return origin == null ? null : this.getPastePrefabPreviewPosition(effect, origin);
   }

   @Nonnull
   private Vector3d getPastePrefabPreviewPosition(@Nonnull PastePrefabEffect effect, @Nonnull Vector3d origin) {
      Vector3d effectPosition = effect.getPosition();
      Vector3d previewPosition;
      if (effect.isAtVolumeOrigin()) {
         previewPosition = new Vector3d(origin);
         if (effectPosition != null) {
            previewPosition.add(effectPosition);
         }
      } else {
         previewPosition = effectPosition != null ? new Vector3d(effectPosition) : new Vector3d();
      }

      return new Vector3d(Math.floor(previewPosition.x()), Math.floor(previewPosition.y()), Math.floor(previewPosition.z()));
   }

   @Nullable
   private Vector3d getCurrentPreviewOrigin() {
      if (this.selectedIsGroup) {
         TriggerVolumeInspectorDrafts.GroupDraft groupDraft = this.selectedGroupDraft();
         return groupDraft != null ? groupDraft.origin : null;
      } else {
         TriggerVolumeInspectorDrafts.VolumeDraft volumeDraft = this.selectedVolumeDraft();
         return volumeDraft != null ? volumeDraft.position : null;
      }
   }

   @Nullable
   private Vector3d getPreviewOriginForState(@Nonnull TriggerVolumesPlugin.PastePrefabPreviewState state) {
      String selectedPreviewId = state.selectedId();
      if (selectedPreviewId == null) {
         return null;
      } else {
         TriggerVolumeManager manager = getManagerForWorld(state.worldName());
         if (manager == null) {
            return null;
         } else if (state.selectedIsGroup()) {
            GroupEntry group = manager.getGroup(selectedPreviewId);
            return group == null ? null : this.draftForGroup(group).origin;
         } else {
            VolumeEntry volume = manager.getVolume(selectedPreviewId);
            return volume == null ? null : this.draftForVolume(volume).position;
         }
      }
   }

   @Nullable
   private PastePrefabEffect getPastePrefabEffectForState(@Nonnull TriggerVolumesPlugin.PastePrefabPreviewState state) {
      String selectedPreviewId = state.selectedId();
      if (selectedPreviewId == null) {
         return null;
      } else {
         TriggerVolumeInspectorPage.EffectListKind previewKind;
         try {
            previewKind = TriggerVolumeInspectorPage.EffectListKind.valueOf(state.effectListKind());
         } catch (IllegalArgumentException var9) {
            return null;
         }

         if (previewKind == TriggerVolumeInspectorPage.EffectListKind.CONDITION) {
            return null;
         } else {
            TriggerVolumeManager manager = getManagerForWorld(state.worldName());
            if (manager == null) {
               return null;
            } else {
               List<TriggerEffect> effects;
               if (state.selectedIsGroup()) {
                  GroupEntry group = manager.getGroup(selectedPreviewId);
                  if (group == null) {
                     return null;
                  }

                  TriggerVolumeInspectorDrafts.GroupDraft groupDraft = this.draftForGroup(group);
                  effects = previewKind == TriggerVolumeInspectorPage.EffectListKind.REJECTION_EFFECT ? groupDraft.rejectionEffects : groupDraft.effects;
               } else {
                  VolumeEntry volume = manager.getVolume(selectedPreviewId);
                  if (volume == null) {
                     return null;
                  }

                  TriggerVolumeInspectorDrafts.VolumeDraft volumeDraft = this.draftForVolume(volume);
                  effects = previewKind == TriggerVolumeInspectorPage.EffectListKind.REJECTION_EFFECT ? volumeDraft.rejectionEffects : volumeDraft.effects;
               }

               int effectIndex = state.effectIndex();
               if (effectIndex >= 0 && effectIndex < effects.size()) {
                  TriggerEffect effect = effects.get(effectIndex);
                  return effect instanceof PastePrefabEffect pastePrefabEffect ? pastePrefabEffect : null;
               } else {
                  return null;
               }
            }
         }
      }
   }

   private void refreshActivePastePrefabPreviewPosition() {
      TriggerVolumesPlugin.PastePrefabPreviewState activeState = this.getActivePastePrefabPreviewState();
      if (activeState != null && activeState.worldName().equals(this.selectedWorld)) {
         PastePrefabEffect pastePrefabEffect = this.getPastePrefabEffectForState(activeState);
         if (pastePrefabEffect == null) {
            this.hidePastePrefabPreview();
         } else {
            Vector3d origin = this.getPreviewOriginForState(activeState);
            if (origin == null) {
               this.hidePastePrefabPreview();
            } else {
               Vector3d previewPosition = this.getPastePrefabPreviewPosition(pastePrefabEffect, origin);
               if (previewPosition == null) {
                  this.hidePastePrefabPreview();
               } else {
                  Vector3d lastSentPosition = activeState.lastSentPosition();
                  if (lastSentPosition == null || !lastSentPosition.equals(previewPosition)) {
                     this.sendPastePrefabPreview(pastePrefabEffect, previewPosition, false);
                  }
               }
            }
         }
      }
   }

   private void refreshActivePastePrefabPreview() {
      if (this.getActivePastePrefabPreviewState() != null && this.isPreviewingSelectedEffect()) {
         if (!(this.getSelectedItem() instanceof PastePrefabEffect pastePrefabEffect && this.sendPastePrefabPreview(pastePrefabEffect, true))) {
            this.hidePastePrefabPreview();
         }
      }
   }

   private static boolean isPastePrefabPreviewAssetField(@Nullable String fieldKey) {
      return "Prefab".equals(fieldKey) || "PrefabList".equals(fieldKey);
   }

   private void hidePastePrefabPreview() {
      if (this.getActivePastePrefabPreviewState() != null) {
         this.playerRef.getPacketHandler().write(new HideTriggerVolumePastePrefabPreview());
      }

      TriggerVolumesPlugin.get().clearPastePrefabPreviewState(this.playerRef.getUuid());
   }

   private void clearPastePrefabPreviewIfFromDifferentWorld() {
      TriggerVolumesPlugin.PastePrefabPreviewState activeState = this.getActivePastePrefabPreviewState();
      if (activeState != null && !activeState.worldName().equals(this.selectedWorld)) {
         this.hidePastePrefabPreview();
      }
   }

   private boolean isPreviewingSelectedEffect() {
      TriggerVolumesPlugin.PastePrefabPreviewState activeState = this.getActivePastePrefabPreviewState();
      return activeState != null
         && activeState.matches(this.selectedWorld, this.selectedId, this.selectedIsGroup, this.selectedKind.name(), this.selectedEffectIndex);
   }

   @Nullable
   private TriggerVolumesPlugin.PastePrefabPreviewState getActivePastePrefabPreviewState() {
      return TriggerVolumesPlugin.get().getPastePrefabPreviewState(this.playerRef.getUuid());
   }

   private <T> void applyCodecField(@Nonnull BuilderCodec<T> codec, @Nonnull T target, @Nonnull String key, @Nonnull TriggerVolumeInspectorPage.PageData data) {
      List<BuilderField<T, ?>> fieldList = codec.getEntries().get(key);
      if (fieldList != null && !fieldList.isEmpty()) {
         BuilderField field = fieldList.getLast();
         Codec childCodec = field.getCodec().getChildCodec();
         ExtraInfo extraInfo = ExtraInfo.THREAD_LOCAL.get();
         if (data.vecX != null && data.vecY != null && data.vecZ != null) {
            BsonDocument vecDoc = new BsonDocument();
            vecDoc.put("X", new BsonDouble(data.vecX));
            vecDoc.put("Y", new BsonDouble(data.vecY));
            vecDoc.put("Z", new BsonDouble(data.vecZ));
            BsonDocument doc = new BsonDocument();
            doc.put(key, vecDoc);
            field.decode(doc, target, extraInfo);
         } else if (data.paramBool != null && childCodec == Codec.BOOLEAN) {
            BsonDocument doc = new BsonDocument();
            doc.put(key, new BsonBoolean(data.paramBool));
            field.decode(doc, target, extraInfo);
         } else if (data.paramNumericValue != null) {
            double numericValue = isNonNegativeNumericField(getCodecTypeId(target), key) ? Math.max(0.0, data.paramNumericValue) : data.paramNumericValue;
            BsonValue bsonValue;
            if (childCodec == Codec.FLOAT || childCodec == Codec.DOUBLE) {
               bsonValue = new BsonDouble(numericValue);
            } else if (childCodec == Codec.INTEGER) {
               bsonValue = new BsonInt32((int)numericValue);
            } else if (childCodec == Codec.LONG) {
               bsonValue = new BsonInt64((long)numericValue);
            } else if (childCodec == Codec.BOOLEAN) {
               bsonValue = new BsonBoolean(numericValue != 0.0);
            } else {
               bsonValue = new BsonDouble(numericValue);
            }

            BsonDocument doc = new BsonDocument();
            doc.put(key, bsonValue);
            field.decode(doc, target, extraInfo);
         } else {
            try {
               BsonValue bsonValue = stringToBsonValue(childCodec, data.paramValue);
               if (bsonValue != null) {
                  BsonDocument doc = new BsonDocument();
                  doc.put(key, bsonValue);
                  field.decode(doc, target, extraInfo);
               }
            } catch (Exception var13) {
               LOGGER.at(Level.WARNING).log("Failed to parse value '%s' for field '%s'", data.paramValue, key, var13);
            }
         }
      }
   }

   private void onOpenPresetSave() {
      UICommandBuilder cmd = new UICommandBuilder();
      cmd.set("#MainPage.Visible", false);
      cmd.set("#PresetSavePage.Visible", true);
      cmd.set("#PresetName #Input.Value", "");
      cmd.set("#ConfirmSavePresetButton.Disabled", true);
      this.sendUpdate(cmd);
   }

   private void onPresetNameChanged(@Nonnull TriggerVolumeInspectorPage.PageData data) {
      UICommandBuilder cmd = new UICommandBuilder();
      cmd.set("#ConfirmSavePresetButton.Disabled", data.presetName == null || data.presetName.isBlank());
      this.sendUpdate(cmd);
   }

   private void onConfirmSavePreset(@Nonnull TriggerVolumeInspectorPage.PageData data) {
      if (data.presetName != null && !data.presetName.isBlank()) {
         try {
            Path assetRoot = AssetModule.get().getAssetPacks().get(0).getRoot();
            Path path = assetRoot.resolve("Server").resolve("TriggerVolumes").resolve("Effects").resolve(data.presetName + ".json");
            Files.createDirectories(path.getParent());
            TriggerEffectAsset asset = TriggerEffectAsset.create(
               data.presetName,
               this.currentConditions().toArray(TriggerCondition[]::new),
               this.currentEffects(TriggerVolumeInspectorPage.EffectListKind.EFFECT).toArray(TriggerEffect[]::new),
               this.currentEffects(TriggerVolumeInspectorPage.EffectListKind.REJECTION_EFFECT).toArray(TriggerEffect[]::new),
               this.currentConditionTiming()
            );
            BsonUtil.writeSync(path, TriggerEffectAsset.CODEC, asset, LOGGER);
            this.playerRef.sendMessage(Message.translation("server.customUI.triggerVolumeEffectEditor.presetSaved").param("name", data.presetName));
         } catch (Exception var5) {
            LOGGER.at(Level.SEVERE).log("Failed to save effect preset '%s'", data.presetName, var5);
            this.playerRef.sendMessage(Message.translation("server.customUI.triggerVolumeEffectEditor.presetSaveError").param("error", var5.getMessage()));
         }

         UICommandBuilder cmd = new UICommandBuilder();
         cmd.set("#PresetSavePage.Visible", false);
         cmd.set("#MainPage.Visible", true);
         this.sendUpdate(cmd);
      }
   }

   private void onCancelPresetSave() {
      UICommandBuilder cmd = new UICommandBuilder();
      cmd.set("#PresetSavePage.Visible", false);
      cmd.set("#MainPage.Visible", true);
      this.sendUpdate(cmd);
   }

   private void onOpenPresetLoad() {
      UICommandBuilder cmd = new UICommandBuilder();
      UIEventBuilder evt = new UIEventBuilder();
      cmd.set("#MainPage.Visible", false);
      cmd.set("#PresetLoadPage.Visible", true);
      cmd.clear("#PresetList");
      AssetStore<String, TriggerEffectAsset, DefaultAssetMap<String, TriggerEffectAsset>> store = AssetRegistry.getAssetStore(TriggerEffectAsset.class);
      if (store != null) {
         int idx = 0;

         for (String assetId : ((DefaultAssetMap)store.getAssetMap()).getAssetMap().keySet()) {
            String sel = "#PresetList[" + idx + "]";
            cmd.append("#PresetList", "Common/TextButton.ui");
            cmd.set(sel + " #Button.Text", assetId);
            evt.addEventBinding(
               CustomUIEventBindingType.Activating,
               sel + " #Button",
               new EventData().append("Action", TriggerVolumeInspectorPage.Action.LoadPreset.name()).append("PresetId", assetId)
            );
            idx++;
         }
      }

      this.sendUpdate(cmd, evt, false);
   }

   private void onLoadPreset(@Nonnull TriggerVolumeInspectorPage.PageData data) {
      if (data.presetId != null && !data.presetId.isBlank()) {
         this.hidePastePrefabPreview();
         AssetStore<String, TriggerEffectAsset, DefaultAssetMap<String, TriggerEffectAsset>> store = AssetRegistry.getAssetStore(TriggerEffectAsset.class);
         if (store != null) {
            TriggerEffectAsset effectAsset = (TriggerEffectAsset)((DefaultAssetMap)store.getAssetMap()).getAsset(data.presetId);
            if (effectAsset == null) {
               this.playerRef.sendMessage(Message.translation("server.customUI.triggerVolumeEffectEditor.presetNotFound").param("name", data.presetId));
            } else {
               this.currentConditions().clear();
               this.currentConditions().addAll(TriggerCondition.deepCopyList(Arrays.asList(effectAsset.getConditions())));
               this.currentEffects(TriggerVolumeInspectorPage.EffectListKind.EFFECT).clear();
               this.currentEffects(TriggerVolumeInspectorPage.EffectListKind.EFFECT)
                  .addAll(TriggerEffect.deepCopyList(Arrays.asList(effectAsset.getEffects())));
               this.currentEffects(TriggerVolumeInspectorPage.EffectListKind.REJECTION_EFFECT).clear();
               this.currentEffects(TriggerVolumeInspectorPage.EffectListKind.REJECTION_EFFECT)
                  .addAll(TriggerEffect.deepCopyList(Arrays.asList(effectAsset.getRejectionEffects())));
               this.setCurrentConditionTiming(effectAsset.getConditionTiming());
               this.markSelectedDraftDirty();
               this.selectedKind = !this.currentConditions().isEmpty()
                  ? TriggerVolumeInspectorPage.EffectListKind.CONDITION
                  : (
                     !this.currentEffects(TriggerVolumeInspectorPage.EffectListKind.EFFECT).isEmpty()
                        ? TriggerVolumeInspectorPage.EffectListKind.EFFECT
                        : TriggerVolumeInspectorPage.EffectListKind.REJECTION_EFFECT
                  );
               this.selectedEffectIndex = this.getItemCount(this.selectedKind) == 0 ? -1 : 0;
               this.playerRef.sendMessage(Message.translation("server.customUI.triggerVolumeEffectEditor.presetLoaded").param("name", data.presetId));
               UICommandBuilder cmd = new UICommandBuilder();
               cmd.set("#PresetLoadPage.Visible", false);
               cmd.set("#MainPage.Visible", true);
               this.sendUpdate(cmd);
               this.rebuildAll();
            }
         }
      }
   }

   private void onCancelPresetLoad() {
      UICommandBuilder cmd = new UICommandBuilder();
      cmd.set("#PresetLoadPage.Visible", false);
      cmd.set("#MainPage.Visible", true);
      this.sendUpdate(cmd);
   }

   private void onOpenAssetPicker(@Nonnull TriggerVolumeInspectorPage.PageData data) {
      if (data.paramKey != null) {
         Object selected = this.getSelectedItem();
         if (selected != null) {
            String typeId = selected instanceof TriggerCondition condition ? getConditionTypeId(condition) : getTypeId((TriggerEffect)selected);
            String source = getAssetSourceForField(typeId, data.paramKey);
            if (source != null) {
               this.pendingPickerFieldKey = data.paramKey;
               this.pendingPickerSource = source;
               this.pendingPickerMultiSelect = this.isPickerFieldMultiSelect(selected, data.paramKey);
               this.pendingPickerSelections.clear();
               if (this.pendingPickerMultiSelect) {
                  this.pendingPickerSelections.addAll(this.currentPickerArrayValues(selected, data.paramKey));
               }

               this.assetPickerSearchQuery = "";
               UICommandBuilder cmd = new UICommandBuilder();
               UIEventBuilder evt = new UIEventBuilder();
               cmd.set("#MainPage.Visible", false);
               cmd.set("#AssetPickerPage.Visible", true);
               cmd.set("#ConfirmAssetPickerButton.Visible", this.pendingPickerMultiSelect);
               cmd.set("#AssetPickerFieldLabel.Text", data.paramKey);
               this.buildAssetPickerList(cmd, evt);
               this.bindStaticEvents(evt);
               this.sendUpdate(cmd, evt, false);
            }
         }
      }
   }

   private void onAssetPickerSearch(@Nonnull TriggerVolumeInspectorPage.PageData data) {
      if (data.assetPickerQuery != null) {
         this.assetPickerSearchQuery = data.assetPickerQuery.trim().toLowerCase(Locale.ROOT);
      }

      UICommandBuilder cmd = new UICommandBuilder();
      UIEventBuilder evt = new UIEventBuilder();
      this.buildAssetPickerList(cmd, evt);
      this.bindStaticEvents(evt);
      this.sendUpdate(cmd, evt, false);
   }

   private void onAssetPickerSelect(@Nonnull TriggerVolumeInspectorPage.PageData data) {
      if (data.assetPickerSelection != null && this.pendingPickerFieldKey != null) {
         if (this.pendingPickerMultiSelect) {
            if (data.assetPickerSelection.isEmpty()) {
               this.pendingPickerSelections.clear();
            } else if (!this.pendingPickerSelections.remove(data.assetPickerSelection)) {
               this.pendingPickerSelections.add(data.assetPickerSelection);
            }

            UICommandBuilder cmd = new UICommandBuilder();
            UIEventBuilder evt = new UIEventBuilder();
            this.buildAssetPickerList(cmd, evt);
            this.bindStaticEvents(evt);
            this.sendUpdate(cmd, evt, false);
         } else {
            Object selected = this.getSelectedItem();
            if (selected instanceof TriggerCondition condition) {
               BuilderCodec<TriggerCondition> codec = getConditionBuilderCodecFor(getConditionTypeId(condition));
               if (codec != null) {
                  this.applyPickerValue(codec, condition, this.pendingPickerFieldKey, data.assetPickerSelection.isEmpty() ? null : data.assetPickerSelection);
                  this.markSelectedDraftDirty();
               }
            } else if (selected instanceof TriggerEffect effect) {
               BuilderCodec<TriggerEffect> codec = getBuilderCodecFor(getTypeId(effect));
               if (codec != null) {
                  this.applyPickerValue(codec, effect, this.pendingPickerFieldKey, data.assetPickerSelection.isEmpty() ? null : data.assetPickerSelection);
                  this.markSelectedDraftDirty();
               }
            }

            if (isPastePrefabPreviewAssetField(this.pendingPickerFieldKey)) {
               this.refreshActivePastePrefabPreview();
            }

            this.pendingPickerFieldKey = null;
            this.pendingPickerSource = null;
            this.assetPickerSearchQuery = "";
            UICommandBuilder cmd = new UICommandBuilder();
            cmd.set("#AssetPickerPage.Visible", false);
            cmd.set("#MainPage.Visible", true);
            this.sendUpdate(cmd);
            this.rebuildAll();
         }
      }
   }

   private void onConfirmAssetPicker() {
      if (this.pendingPickerMultiSelect && this.pendingPickerFieldKey != null) {
         Object selected = this.getSelectedItem();
         if (selected instanceof TriggerCondition condition) {
            BuilderCodec<TriggerCondition> codec = getConditionBuilderCodecFor(getConditionTypeId(condition));
            if (codec != null) {
               this.applyPickerValues(codec, condition, this.pendingPickerFieldKey, this.pendingPickerSelections);
               this.markSelectedDraftDirty();
            }
         } else if (selected instanceof TriggerEffect effect) {
            BuilderCodec<TriggerEffect> codec = getBuilderCodecFor(getTypeId(effect));
            if (codec != null) {
               this.applyPickerValues(codec, effect, this.pendingPickerFieldKey, this.pendingPickerSelections);
               this.markSelectedDraftDirty();
            }
         }

         if (isPastePrefabPreviewAssetField(this.pendingPickerFieldKey)) {
            this.refreshActivePastePrefabPreview();
         }

         this.closeAssetPicker();
      }
   }

   private void onPreviewSound(@Nonnull TriggerVolumeInspectorPage.PageData data, @Nonnull Store<EntityStore> store) {
      if ("SoundEvent".equals(this.pendingPickerSource) && data.assetPickerSelection != null && !data.assetPickerSelection.isBlank()) {
         int soundEventIndex = SoundEvent.getAssetMap().getIndex(data.assetPickerSelection);
         if (soundEventIndex != Integer.MIN_VALUE && soundEventIndex != 0) {
            Ref<EntityStore> playerEntityRef = this.playerRef.getReference();
            if (playerEntityRef != null && playerEntityRef.isValid()) {
               TransformComponent transform = store.getComponent(playerEntityRef, TransformComponent.getComponentType());
               if (transform != null) {
                  Vector3d position = transform.getPosition();
                  SoundUtil.playSoundEvent3d(soundEventIndex, SoundCategory.SFX, position.x(), position.y(), position.z(), 1.0F, 1.0F, store);
               }
            }
         }
      }
   }

   private void onCancelAssetPicker() {
      this.closeAssetPicker();
   }

   private void closeAssetPicker() {
      this.pendingPickerFieldKey = null;
      this.pendingPickerSource = null;
      this.pendingPickerMultiSelect = false;
      this.pendingPickerSelections.clear();
      this.assetPickerSearchQuery = "";
      UICommandBuilder cmd = new UICommandBuilder();
      cmd.set("#ConfirmAssetPickerButton.Visible", false);
      cmd.set("#AssetPickerPage.Visible", false);
      cmd.set("#MainPage.Visible", true);
      this.sendUpdate(cmd);
      this.rebuildAll();
   }

   private void registerSelectionObserver() {
      TriggerVolumeManager manager = getManagerForWorld(this.selectedWorld);
      if (manager != null) {
         manager.setSelectionObserver(this.playerRef.getUuid(), this.selectionObserver);
      }
   }

   private void registerVolumeUpdateObserver() {
      TriggerVolumeManager manager = getManagerForWorld(this.selectedWorld);
      if (manager != null) {
         manager.setVolumeUpdateObserver(this.playerRef.getUuid(), this.volumeUpdateObserver);
      }
   }

   private void onExternalSelectionChanged(@Nullable String volumeId) {
      if (!this.suppressSelectionObserver) {
         if (!Objects.equals(this.selectedId, volumeId) || this.selectedIsGroup) {
            TriggerVolumeManager manager = getManagerForWorld(this.selectedWorld);
            if (volumeId == null || manager != null && manager.getVolume(volumeId) != null) {
               this.selectedId = volumeId;
               this.selectedIsGroup = false;
               this.selectedEffectIndex = -1;
               this.rebuildAll();
            }
         }
      }
   }

   private void onExternalVolumeUpdated(@Nonnull VolumeEntry volume) {
      TriggerVolumesPlugin.PastePrefabPreviewState activeState = this.getActivePastePrefabPreviewState();
      if (activeState != null) {
         if (activeState.selectedId() != null && activeState.worldName().equals(this.selectedWorld)) {
            if (activeState.selectedIsGroup()) {
               TriggerVolumeManager manager = getManagerForWorld(this.selectedWorld);
               GroupEntry group = manager != null ? manager.getGroup(activeState.selectedId()) : null;
               if (group == null || !group.getMemberVolumeIds().contains(volume.getId())) {
                  return;
               }

               TriggerVolumeInspectorDrafts.GroupDraft groupDraft = this.groupDrafts.get(activeState.selectedId());
               if (groupDraft != null) {
                  groupDraft.origin = new Vector3d(group.getOrigin());
               }
            } else {
               if (!activeState.selectedId().equals(volume.getId())) {
                  return;
               }

               TriggerVolumeInspectorDrafts.VolumeDraft volumeDraft = this.volumeDrafts.get(activeState.selectedId());
               if (volumeDraft != null) {
                  volumeDraft.position = new Vector3d(volume.getPosition());
               }
            }

            this.refreshActivePastePrefabPreviewPosition();
         }
      }
   }

   private void onExternalVolumeRemoved(@Nonnull String volumeId) {
      TriggerVolumesPlugin.PastePrefabPreviewState activeState = this.getActivePastePrefabPreviewState();
      if (activeState != null && activeState.selectedId() != null) {
         if (!activeState.selectedIsGroup() && activeState.selectedId().equals(volumeId)) {
            this.hidePastePrefabPreview();
         } else {
            if (activeState.selectedIsGroup()) {
               TriggerVolumeInspectorDrafts.GroupDraft groupDraft = this.groupDrafts.get(activeState.selectedId());
               if (groupDraft != null && groupDraft.memberVolumeIds.contains(volumeId)) {
                  this.hidePastePrefabPreview();
               }
            }
         }
      }
   }

   private void syncSelectionToTool() {
      TriggerVolumeToolSelection packet = new TriggerVolumeToolSelection();
      TriggerVolumeManager manager = getManagerForWorld(this.selectedWorld);
      if (this.selectedId != null && manager != null) {
         if (this.selectedIsGroup) {
            GroupEntry group = manager.getGroup(this.selectedId);
            if (group == null) {
               return;
            }

            String[] ids = group.getMemberVolumeIds().stream().filter(id -> manager.getVolume(id) != null).toArray(String[]::new);
            packet.volumeIds = ids;
            packet.primaryVolumeId = ids.length > 0 ? ids[0] : null;
            this.suppressSelectionObserver = true;
            manager.setPlayerSelection(this.playerRef.getUuid(), packet.primaryVolumeId);
            this.suppressSelectionObserver = false;
         } else {
            packet.primaryVolumeId = this.selectedId;
            packet.volumeIds = new String[]{this.selectedId};
            this.suppressSelectionObserver = true;
            manager.setPlayerSelection(this.playerRef.getUuid(), this.selectedId);
            this.suppressSelectionObserver = false;
         }

         this.playerRef.getPacketHandler().write(packet);
      } else {
         if (manager != null) {
            this.suppressSelectionObserver = true;
            manager.setPlayerSelection(this.playerRef.getUuid(), null);
            this.suppressSelectionObserver = false;
         }

         this.playerRef.getPacketHandler().write(packet);
      }
   }

   private void applyPickerValue(@Nonnull BuilderCodec<?> codec, @Nonnull Object target, @Nonnull String fieldKey, @Nullable String value) {
      List<? extends BuilderField<?, ?>> fieldList = codec.getEntries().get(fieldKey);
      if (fieldList != null && !fieldList.isEmpty()) {
         BuilderField field = fieldList.getLast();
         BsonDocument doc = new BsonDocument();
         Codec childCodec = field.getCodec().getChildCodec();
         if (childCodec == Codec.STRING_ARRAY) {
            doc.put(fieldKey, this.pickerArrayValue(value));
         } else if (value != null) {
            doc.put(fieldKey, new BsonString(value));
         }

         try {
            field.decode(doc, target, ExtraInfo.THREAD_LOCAL.get());
         } catch (Exception var10) {
            LOGGER.at(Level.WARNING).log("Failed to apply picker value '%s' for field '%s'", value, fieldKey, var10);
         }
      }
   }

   private void applyPickerValues(@Nonnull BuilderCodec<?> codec, @Nonnull Object target, @Nonnull String fieldKey, @Nonnull Collection<String> values) {
      List<? extends BuilderField<?, ?>> fieldList = codec.getEntries().get(fieldKey);
      if (fieldList != null && !fieldList.isEmpty()) {
         BuilderField field = fieldList.getLast();
         BsonDocument doc = new BsonDocument();
         doc.put(fieldKey, this.pickerArrayValue(values));

         try {
            field.decode(doc, target, ExtraInfo.THREAD_LOCAL.get());
         } catch (Exception var9) {
            LOGGER.at(Level.WARNING).log("Failed to apply picker values for field '%s'", fieldKey, var9);
         }
      }
   }

   @Nonnull
   private BsonArray pickerArrayValue(@Nullable String value) {
      return value != null && !value.isBlank() ? this.pickerArrayValue(List.of(value)) : new BsonArray();
   }

   @Nonnull
   private BsonArray pickerArrayValue(@Nonnull Collection<String> values) {
      BsonArray array = new BsonArray();

      for (String stringValue : values) {
         array.add(new BsonString(stringValue));
      }

      return array;
   }

   private boolean isPickerFieldMultiSelect(@Nonnull Object selected, @Nonnull String fieldKey) {
      BuilderCodec<? extends Object> codec = selected instanceof TriggerCondition condition
         ? getConditionBuilderCodecFor(getConditionTypeId(condition))
         : getBuilderCodecFor(getTypeId((TriggerEffect)selected));
      if (codec == null) {
         return false;
      } else {
         List<? extends BuilderField<?, ?>> fieldList = codec.getEntries().get(fieldKey);
         if (fieldList != null && !fieldList.isEmpty()) {
            BuilderField field = fieldList.getLast();
            return field.getCodec().getChildCodec() == Codec.STRING_ARRAY;
         } else {
            return false;
         }
      }
   }

   @Nonnull
   private Collection<String> currentPickerArrayValues(@Nonnull Object selected, @Nonnull String fieldKey) {
      BuilderCodec<? extends Object> codec = selected instanceof TriggerCondition condition
         ? getConditionBuilderCodecFor(getConditionTypeId(condition))
         : getBuilderCodecFor(getTypeId((TriggerEffect)selected));
      if (codec == null) {
         return List.of();
      } else {
         BsonDocument encoded;
         try {
            encoded = codec.encode(selected, EmptyExtraInfo.EMPTY);
         } catch (Exception var11) {
            return List.of();
         }

         LinkedHashSet<String> values = new LinkedHashSet<>();
         BsonValue currentValue = encoded.get(fieldKey);
         if (currentValue instanceof BsonArray) {
            for (BsonValue element : (BsonArray)currentValue) {
               if (element instanceof BsonString bsonString) {
                  values.add(bsonString.getValue());
               }
            }
         }

         return values;
      }
   }

   private void buildAssetPickerList(@Nonnull UICommandBuilder cmd, @Nonnull UIEventBuilder evt) {
      cmd.clear("#AssetPickerList");
      Collection<String> allIds = getAssetIdsForSource(this.pendingPickerSource);
      List<String> filtered;
      if (!this.assetPickerSearchQuery.isEmpty()) {
         Object2IntMap<String> scored = new Object2IntOpenHashMap(allIds.size());

         for (String assetId : allIds) {
            if (assetId.toLowerCase(Locale.ROOT).contains(this.assetPickerSearchQuery)) {
               scored.put(assetId, StringCompareUtil.getFuzzyDistance(assetId, this.assetPickerSearchQuery, Locale.ROOT));
            }
         }

         filtered = scored.keySet().stream().sorted().sorted(Comparator.comparingInt(scored::getInt).reversed()).limit(50L).toList();
      } else {
         filtered = allIds.stream().sorted().limit(50L).toList();
      }

      cmd.set("#AssetPickerNoResults.Visible", filtered.isEmpty());
      cmd.append("#AssetPickerList", "Common/TextButton.ui");
      cmd.set("#AssetPickerList[0] #Button.Text", Message.translation("server.customUI.triggerVolumeEffectEditor.assetPicker.clearEntry"));
      evt.addEventBinding(
         CustomUIEventBindingType.Activating,
         "#AssetPickerList[0] #Button",
         new EventData().append("Action", TriggerVolumeInspectorPage.Action.AssetPickerSelect.name()).append("AssetPickerSelection", "")
      );
      boolean soundPicker = "SoundEvent".equals(this.pendingPickerSource);

      for (int i = 0; i < filtered.size(); i++) {
         String assetIdx = filtered.get(i);
         String sel = "#AssetPickerList[" + (i + 1) + "]";
         String label = this.pendingPickerMultiSelect && this.pendingPickerSelections.contains(assetIdx) ? "[x] " + assetIdx : assetIdx;
         if (soundPicker) {
            cmd.append("#AssetPickerList", "Pages/TriggerVolume/TriggerVolumeInspectorSoundAssetRow.ui");
            cmd.set(sel + " #SelectButton.Text", label);
            evt.addEventBinding(
               CustomUIEventBindingType.Activating,
               sel + " #SelectButton",
               new EventData().append("Action", TriggerVolumeInspectorPage.Action.AssetPickerSelect.name()).append("AssetPickerSelection", assetIdx)
            );
            evt.addEventBinding(
               CustomUIEventBindingType.Activating,
               sel + " #PreviewButton",
               new EventData().append("Action", TriggerVolumeInspectorPage.Action.PreviewSound.name()).append("AssetPickerSelection", assetIdx),
               false
            );
         } else {
            cmd.append("#AssetPickerList", "Common/TextButton.ui");
            cmd.set(sel + " #Button.Text", label);
            evt.addEventBinding(
               CustomUIEventBindingType.Activating,
               sel + " #Button",
               new EventData().append("Action", TriggerVolumeInspectorPage.Action.AssetPickerSelect.name()).append("AssetPickerSelection", assetIdx)
            );
         }
      }
   }

   private void rebuildAll() {
      this.revertRejectedDraftIds();
      UICommandBuilder cmd = new UICommandBuilder();
      UIEventBuilder evt = new UIEventBuilder();
      this.buildWorldDropdown(cmd);
      this.buildTabs(cmd, evt);
      this.buildList(cmd, evt);
      this.buildSelectedPane(cmd, evt);
      this.bindStaticEvents(evt);
      this.refreshActivePastePrefabPreviewPosition();
      this.sendUpdate(cmd, evt, false);
   }

   @Nullable
   private TriggerVolumeManager getSelectedManager() {
      return getManagerForWorld(this.selectedWorld);
   }

   @Nullable
   private TriggerVolumeInspectorDrafts.VolumeDraft selectedVolumeDraft() {
      TriggerVolumeManager manager = this.getSelectedManager();
      if (manager != null && this.selectedId != null) {
         VolumeEntry volume = manager.getVolume(this.selectedId);
         return volume != null ? this.draftForVolume(volume) : null;
      } else {
         return null;
      }
   }

   @Nullable
   private TriggerVolumeInspectorDrafts.GroupDraft selectedGroupDraft() {
      TriggerVolumeManager manager = this.getSelectedManager();
      if (manager != null && this.selectedId != null) {
         GroupEntry group = manager.getGroup(this.selectedId);
         return group != null ? this.draftForGroup(group) : null;
      } else {
         return null;
      }
   }

   @Nullable
   private TriggerVolumeInspectorDrafts.GroupDraft selectedInheritedGroupDraft() {
      if (this.selectedIsGroup) {
         return null;
      } else {
         TriggerVolumeManager manager = this.getSelectedManager();
         TriggerVolumeInspectorDrafts.VolumeDraft volumeDraft = this.selectedVolumeDraft();
         if (manager != null && volumeDraft != null && volumeDraft.groupId != null) {
            GroupEntry group = manager.getGroup(volumeDraft.groupId);
            return group != null ? this.draftForGroup(group) : null;
         } else {
            return null;
         }
      }
   }

   @Nonnull
   private TriggerVolumeInspectorDrafts.VolumeDraft draftForVolume(@Nonnull VolumeEntry volume) {
      return this.volumeDrafts.computeIfAbsent(volume.getId(), ignored -> TriggerVolumeInspectorDrafts.VolumeDraft.from(volume));
   }

   @Nonnull
   private TriggerVolumeInspectorDrafts.GroupDraft draftForGroup(@Nonnull GroupEntry group) {
      return this.groupDrafts.computeIfAbsent(group.getId(), ignored -> TriggerVolumeInspectorDrafts.GroupDraft.from(group));
   }

   @Nonnull
   private Map<String, String> currentTags() {
      return this.selectedIsGroup ? this.selectedGroupDraft().tags : this.selectedVolumeDraft().tags;
   }

   @Nonnull
   private List<TriggerCondition> currentConditions() {
      return this.selectedIsGroup ? this.selectedGroupDraft().conditions : this.selectedVolumeDraft().conditions;
   }

   @Nonnull
   private List<TriggerEffect> currentEffects(@Nonnull TriggerVolumeInspectorPage.EffectListKind kind) {
      if (this.selectedIsGroup) {
         TriggerVolumeInspectorDrafts.GroupDraft draft = this.selectedGroupDraft();
         return kind == TriggerVolumeInspectorPage.EffectListKind.REJECTION_EFFECT ? draft.rejectionEffects : draft.effects;
      } else {
         TriggerVolumeInspectorDrafts.VolumeDraft draft = this.selectedVolumeDraft();
         return kind == TriggerVolumeInspectorPage.EffectListKind.REJECTION_EFFECT ? draft.rejectionEffects : draft.effects;
      }
   }

   @Nonnull
   private ConditionTiming currentConditionTiming() {
      return this.selectedIsGroup ? this.selectedGroupDraft().conditionTiming : this.selectedVolumeDraft().conditionTiming;
   }

   private void setCurrentConditionTiming(@Nonnull ConditionTiming timing) {
      if (this.selectedIsGroup) {
         TriggerVolumeInspectorDrafts.GroupDraft groupDraft = this.selectedGroupDraft();
         groupDraft.conditionTiming = timing;
         groupDraft.markDirty();
      } else {
         TriggerVolumeInspectorDrafts.VolumeDraft volumeDraft = this.selectedVolumeDraft();
         volumeDraft.conditionTiming = timing;
         volumeDraft.markDirty();
      }
   }

   private void markSelectedDraftDirty() {
      if (this.selectedIsGroup) {
         TriggerVolumeInspectorDrafts.GroupDraft groupDraft = this.selectedGroupDraft();
         if (groupDraft != null) {
            groupDraft.markDirty();
         }
      } else {
         TriggerVolumeInspectorDrafts.VolumeDraft volumeDraft = this.selectedVolumeDraft();
         if (volumeDraft != null) {
            volumeDraft.markDirty();
         }
      }
   }

   @Nullable
   private Object getSelectedItem() {
      if (this.selectedId != null && this.selectedEffectIndex >= 0 && this.selectedEffectIndex < this.getItemCount(this.selectedKind)) {
         return this.selectedKind == TriggerVolumeInspectorPage.EffectListKind.CONDITION
            ? this.currentConditions().get(this.selectedEffectIndex)
            : this.currentEffects(this.selectedKind).get(this.selectedEffectIndex);
      } else {
         return null;
      }
   }

   private int getItemCount(@Nonnull TriggerVolumeInspectorPage.EffectListKind kind) {
      return kind == TriggerVolumeInspectorPage.EffectListKind.CONDITION ? this.currentConditions().size() : this.currentEffects(kind).size();
   }

   private static int getGroupItemCount(@Nonnull TriggerVolumeInspectorPage.EffectListKind kind, @Nonnull TriggerVolumeInspectorDrafts.GroupDraft groupDraft) {
      return switch (kind) {
         case CONDITION -> groupDraft.conditions.size();
         case EFFECT -> groupDraft.effects.size();
         case REJECTION_EFFECT -> groupDraft.rejectionEffects.size();
      };
   }

   @Nullable
   private TriggerEventType getItemEventType(@Nonnull TriggerVolumeInspectorPage.EffectListKind kind, int index) {
      return kind == TriggerVolumeInspectorPage.EffectListKind.CONDITION
         ? this.currentConditions().get(index).getEventType()
         : this.currentEffects(kind).get(index).getEventType();
   }

   @Nullable
   private static TriggerEventType getGroupItemEventType(
      @Nonnull TriggerVolumeInspectorPage.EffectListKind kind, int index, @Nonnull TriggerVolumeInspectorDrafts.GroupDraft groupDraft
   ) {
      return switch (kind) {
         case CONDITION -> groupDraft.conditions.get(index).getEventType();
         case EFFECT -> groupDraft.effects.get(index).getEventType();
         case REJECTION_EFFECT -> groupDraft.rejectionEffects.get(index).getEventType();
      };
   }

   @Nonnull
   private String getItemTypeId(@Nonnull TriggerVolumeInspectorPage.EffectListKind kind, int index) {
      return kind == TriggerVolumeInspectorPage.EffectListKind.CONDITION
         ? getConditionTypeId(this.currentConditions().get(index))
         : getTypeId(this.currentEffects(kind).get(index));
   }

   @Nonnull
   private static String getGroupItemTypeId(
      @Nonnull TriggerVolumeInspectorPage.EffectListKind kind, int index, @Nonnull TriggerVolumeInspectorDrafts.GroupDraft groupDraft
   ) {
      return switch (kind) {
         case CONDITION -> getConditionTypeId(groupDraft.conditions.get(index));
         case EFFECT -> getTypeId(groupDraft.effects.get(index));
         case REJECTION_EFFECT -> getTypeId(groupDraft.rejectionEffects.get(index));
      };
   }

   private boolean matchesFilter(@Nonnull String value) {
      return this.filterText.isEmpty() || value.toLowerCase(Locale.ROOT).contains(this.filterText);
   }

   @Nullable
   private static TriggerVolumeManager getManagerForWorld(@Nonnull String worldName) {
      for (World world : Universe.get().getWorlds().values()) {
         if (world.getName().equalsIgnoreCase(worldName)) {
            return world.getEntityStore().getStore().getResource(TriggerVolumesPlugin.get().getManagerResourceType());
         }
      }

      return null;
   }

   @Nonnull
   private static PatchStyle colorPatch(int rgb) {
      return new PatchStyle().setColor(Value.of(colorToHex(rgb)));
   }

   @Nonnull
   private static String colorToHex(int rgb) {
      int r = rgb >> 16 & 0xFF;
      int g = rgb >> 8 & 0xFF;
      int b = rgb & 0xFF;
      return String.format("#%02X%02X%02X", r, g, b);
   }

   @Nonnull
   private static String colorToHex(@Nonnull Vector3f color) {
      int r = Math.round(color.x() * 255.0F);
      int g = Math.round(color.y() * 255.0F);
      int b = Math.round(color.z() * 255.0F);
      return String.format("#%02X%02X%02X", r, g, b);
   }

   @Nullable
   private static Vector3f parseColor(@Nullable String value) {
      if (value != null && !value.isBlank()) {
         int packed = parsePackedColor(value, 52428);
         return new Vector3f((packed >> 16 & 0xFF) / 255.0F, (packed >> 8 & 0xFF) / 255.0F, (packed & 0xFF) / 255.0F);
      } else {
         return null;
      }
   }

   private static int parsePackedColor(@Nullable String value, int fallback) {
      if (value == null) {
         return fallback;
      } else {
         String trimmed = value.trim();
         if (trimmed.startsWith("#")) {
            trimmed = trimmed.substring(1);
         }

         if (trimmed.length() > 6) {
            trimmed = trimmed.substring(0, 6);
         }

         try {
            return Integer.parseInt(trimmed, 16) & 16777215;
         } catch (NumberFormatException var4) {
            return fallback;
         }
      }
   }

   private static void setVec(@Nonnull Vector3d target, @Nonnull TriggerVolumeInspectorPage.PageData data) {
      if (data.vecX != null && data.vecY != null && data.vecZ != null) {
         target.set(data.vecX, data.vecY, data.vecZ);
      }
   }

   private static void setBoxDimensions(@Nonnull Vector3d target, @Nonnull TriggerVolumeInspectorPage.PageData data) {
      if (data.vecX != null && data.vecY != null && data.vecZ != null) {
         target.set(clampDimension(data.vecX, 0.25), clampDimension(data.vecY, 0.25), clampDimension(data.vecZ, 0.25));
      }
   }

   private static void setSphereDimensions(@Nonnull Vector3d target, @Nonnull TriggerVolumeInspectorPage.PageData data) {
      if (data.paramNumericValue != null) {
         target.set(clampDimension(data.paramNumericValue, 0.25), 0.0, 0.0);
      }
   }

   private static void setCylinderDimensions(@Nonnull Vector3d target, @Nonnull TriggerVolumeInspectorPage.PageData data) {
      if (data.vecX != null && data.vecY != null) {
         target.set(clampDimension(data.vecX, 0.25), clampDimension(data.vecY, 0.5), 0.0);
      }
   }

   private static double clampDimension(double value, double minValue) {
      return Math.min(1000.0, Math.max(minValue, value));
   }

   @Nonnull
   private static <T extends Enum<T>> T parseEnum(@Nonnull Class<T> type, @Nullable String value, @Nonnull T fallback) {
      if (value == null) {
         return fallback;
      } else {
         try {
            return Enum.valueOf(type, value);
         } catch (IllegalArgumentException var4) {
            return fallback;
         }
      }
   }

   @Nonnull
   private static Set<EntityTargetType> parseTargetTypes(@Nullable String value) {
      String var1 = value != null ? value : "PLAYER";

      return switch (var1) {
         case "NONE" -> EnumSet.noneOf(EntityTargetType.class);
         case "NPC" -> EnumSet.of(EntityTargetType.NPC);
         case "ITEM_DROP" -> EnumSet.of(EntityTargetType.ITEM_DROP);
         case "PROJECTILE" -> EnumSet.of(EntityTargetType.PROJECTILE);
         case "ALL" -> EnumSet.allOf(EntityTargetType.class);
         default -> EnumSet.of(EntityTargetType.PLAYER);
      };
   }

   @Nonnull
   private static String targetTypesValue(@Nonnull Set<EntityTargetType> targetTypes) {
      if (targetTypes.containsAll(EnumSet.allOf(EntityTargetType.class))) {
         return "ALL";
      } else if (targetTypes.contains(EntityTargetType.PROJECTILE)) {
         return "PROJECTILE";
      } else if (targetTypes.contains(EntityTargetType.ITEM_DROP)) {
         return "ITEM_DROP";
      } else if (targetTypes.contains(EntityTargetType.NPC)) {
         return "NPC";
      } else {
         return targetTypes.contains(EntityTargetType.PLAYER) ? "PLAYER" : "NONE";
      }
   }

   @Nonnull
   private static List<DropdownEntryInfo> targetTypeEntries() {
      return List.of(
         option("server.customUI.triggerVolumeInspector.option.none", "NONE"),
         option("server.customUI.triggerVolumeInspector.option.player", "PLAYER"),
         option("server.customUI.triggerVolumeInspector.option.npc", "NPC"),
         option("server.customUI.triggerVolumeInspector.option.itemDrop", "ITEM_DROP"),
         option("server.customUI.triggerVolumeInspector.option.projectile", "PROJECTILE"),
         option("server.customUI.triggerVolumeInspector.option.all", "ALL")
      );
   }

   @Nonnull
   private static List<DropdownEntryInfo> projectileSourceEntries() {
      return List.of(
         option(
            "server.customUI.triggerVolumeInspector.option.projectileSource.shooter",
            ProjectileSource.SHOOTER.name(),
            "server.customUI.triggerVolumeInspector.option.projectileSource.shooter.tooltip"
         ),
         option(
            "server.customUI.triggerVolumeInspector.option.projectileSource.projectile",
            ProjectileSource.PROJECTILE.name(),
            "server.customUI.triggerVolumeInspector.option.projectileSource.projectile.tooltip"
         )
      );
   }

   @Nonnull
   private static List<DropdownEntryInfo> shapeEntries() {
      return List.of(
         option("server.customUI.triggerVolumeInspector.option.box", TriggerVolumeShapeType.Box.name()),
         option("server.customUI.triggerVolumeInspector.option.sphere", TriggerVolumeShapeType.Sphere.name()),
         option("server.customUI.triggerVolumeInspector.option.cylinder", TriggerVolumeShapeType.Cylinder.name())
      );
   }

   @Nonnull
   private static List<DropdownEntryInfo> cooldownModeEntries() {
      return List.of(
         option(
            "server.customUI.triggerVolumeInspector.option.perEntity",
            CooldownMode.PER_ENTITY.name(),
            "server.customUI.triggerVolumeInspector.option.perEntity.tooltip"
         ),
         option("server.customUI.triggerVolumeInspector.option.total", CooldownMode.TOTAL.name(), "server.customUI.triggerVolumeInspector.option.total.tooltip")
      );
   }

   @Nonnull
   private static List<DropdownEntryInfo> conditionTimingEntries() {
      return List.of(
         option(
            "server.customUI.triggerVolumeInspector.option.beforeVolumeDelay",
            ConditionTiming.BEFORE_VOLUME_DELAY.name(),
            "server.customUI.triggerVolumeInspector.option.beforeVolumeDelay.tooltip"
         ),
         option(
            "server.customUI.triggerVolumeInspector.option.afterVolumeDelay",
            ConditionTiming.AFTER_VOLUME_DELAY.name(),
            "server.customUI.triggerVolumeInspector.option.afterVolumeDelay.tooltip"
         )
      );
   }

   @Nonnull
   private static List<DropdownEntryInfo> rejectionDelayModeEntries() {
      return List.of(
         option(
            "server.customUI.triggerVolumeInspector.option.rejectionImmediate",
            RejectionDelayMode.IMMEDIATE.name(),
            "server.customUI.triggerVolumeInspector.option.rejectionImmediate.tooltip"
         ),
         option(
            "server.customUI.triggerVolumeInspector.option.rejectionUseVolumeDelay",
            RejectionDelayMode.USE_VOLUME_DELAY.name(),
            "server.customUI.triggerVolumeInspector.option.rejectionUseVolumeDelay.tooltip"
         )
      );
   }

   @Nonnull
   private static DropdownEntryInfo option(@Nonnull String labelKey, @Nonnull String value) {
      return new DropdownEntryInfo(LocalizableString.fromMessageId(labelKey), value);
   }

   @Nonnull
   private static DropdownEntryInfo option(@Nonnull String labelKey, @Nonnull String value, @Nonnull String tooltipKey) {
      return new DropdownEntryInfo(LocalizableString.fromMessageId(labelKey), value, LocalizableString.fromMessageId(tooltipKey));
   }

   @Nonnull
   private static Message fieldLabel(@Nonnull String fieldKey) {
      return Message.translation("server.customUI.triggerVolumeInspector.field." + fieldKey);
   }

   @Nonnull
   private static Message volumeFieldTooltip(@Nonnull String fieldKey) {
      return Message.translation("server.customUI.triggerVolumeInspector.field." + fieldKey + ".tooltip");
   }

   @Nonnull
   private static Message fieldLabel(@Nonnull String typeId, @Nonnull String fieldKey) {
      String key = "server.customUI.triggerVolumeEffectEditor.field." + typeId + "." + fieldKey;
      if (messageExists(key)) {
         return Message.translation(key);
      } else {
         String commonKey = "server.customUI.triggerVolumeEffectEditor.field.common." + fieldKey;
         return messageExists(commonKey) ? Message.translation(commonKey) : Message.translation(key);
      }
   }

   @Nonnull
   private static Message effectFieldTooltip(@Nonnull String typeId, @Nonnull String fieldKey) {
      String key = "server.customUI.triggerVolumeEffectEditor.field." + typeId + "." + fieldKey + ".tooltip";
      if (messageExists(key)) {
         return Message.translation(key);
      } else {
         String commonKey = "server.customUI.triggerVolumeEffectEditor.field.common." + fieldKey + ".tooltip";
         return messageExists(commonKey) ? Message.translation(commonKey) : Message.translation(key);
      }
   }

   private static void setEffectFieldTooltip(@Nonnull UICommandBuilder cmd, @Nonnull String selector, @Nonnull String typeId, @Nonnull String fieldKey) {
      if (!typeId.isEmpty()) {
         cmd.set(selector + " #Label.TooltipText", effectFieldTooltip(typeId, fieldKey));
      }
   }

   private static void setEffectFieldPlaceholder(@Nonnull UICommandBuilder cmd, @Nonnull String selector, @Nonnull String typeId, @Nonnull String fieldKey) {
      if (!typeId.isEmpty()) {
         String key = "server.customUI.triggerVolumeEffectEditor.field." + typeId + "." + fieldKey + ".placeholder";
         I18nModule i18n = I18nModule.get();
         if (i18n != null && i18n.getMessage("en-US", key) != null) {
            cmd.set(selector + " #Input.PlaceholderText", Message.translation(key));
         }
      }
   }

   private static boolean isNonNegativeNumericField(@Nonnull String typeId, @Nonnull String fieldKey) {
      return NON_NEGATIVE_NUMERIC_FIELDS.contains(typeId + "." + fieldKey);
   }

   @Nonnull
   private static String getCodecTypeId(@Nonnull Object target) {
      return target instanceof TriggerCondition condition ? getConditionTypeId(condition) : getTypeId((TriggerEffect)target);
   }

   @Nonnull
   private static EventData paramEvent(@Nonnull String key, @Nonnull String valueRef) {
      return new EventData().append("Action", TriggerVolumeInspectorPage.Action.UpdateParameter.name()).append("ParamKey", key).append("@ParamValue", valueRef);
   }

   @Nonnull
   private static EventData boolParamEvent(@Nonnull String key, @Nonnull String valueRef) {
      return new EventData().append("Action", TriggerVolumeInspectorPage.Action.UpdateParameter.name()).append("ParamKey", key).append("@ParamBool", valueRef);
   }

   @Nonnull
   private static EventData numericParamEvent(@Nonnull String key, @Nonnull String valueRef) {
      return new EventData()
         .append("Action", TriggerVolumeInspectorPage.Action.UpdateParameter.name())
         .append("ParamKey", key)
         .append("@ParamNumericValue", valueRef);
   }

   @Nonnull
   private static TriggerVolumeInspectorPage.EffectListKind parseEffectListKind(
      @Nonnull String value, @Nonnull TriggerVolumeInspectorPage.EffectListKind fallback
   ) {
      try {
         return TriggerVolumeInspectorPage.EffectListKind.valueOf(value);
      } catch (IllegalArgumentException var3) {
         return fallback;
      }
   }

   @Nonnull
   private static TriggerEventType parseTriggerEventType(@Nonnull String value, @Nonnull TriggerEventType fallback) {
      try {
         return TriggerEventType.valueOf(value);
      } catch (IllegalArgumentException var3) {
         return fallback;
      }
   }

   @Nonnull
   private static String humanizeTypeId(@Nonnull String typeId) {
      if (typeId.isEmpty()) {
         return typeId;
      } else {
         StringBuilder builder = new StringBuilder(typeId.length() + 4);
         builder.append(typeId.charAt(0));

         for (int i = 1; i < typeId.length(); i++) {
            char currentChar = typeId.charAt(i);
            if (Character.isUpperCase(currentChar) && !Character.isUpperCase(typeId.charAt(i - 1))) {
               builder.append(' ');
            }

            builder.append(currentChar);
         }

         return builder.toString();
      }
   }

   @Nonnull
   private static List<String> getSortedTypeIds() {
      ArrayList<String> ids = new ArrayList<>(TriggerEffect.CODEC.getRegisteredIds());
      Collections.sort(ids);
      return ids;
   }

   @Nonnull
   private static List<String> getSortedConditionTypeIds() {
      ArrayList<String> ids = new ArrayList<>(TriggerCondition.CODEC.getRegisteredIds());
      Collections.sort(ids);
      return ids;
   }

   @Nonnull
   private static String getTypeId(@Nonnull TriggerEffect effect) {
      String typeId = TriggerEffect.CODEC.getIdFor((Class<? extends TriggerEffect>)effect.getClass());
      return typeId != null ? typeId : "unknown";
   }

   @Nonnull
   private static String getConditionTypeId(@Nonnull TriggerCondition condition) {
      String typeId = TriggerCondition.CODEC.getIdFor((Class<? extends TriggerCondition>)condition.getClass());
      return typeId != null ? typeId : "unknown";
   }

   @Nullable
   private static BuilderCodec<TriggerEffect> getBuilderCodecFor(@Nonnull String typeId) {
      return (BuilderCodec<TriggerEffect>)(TriggerEffect.CODEC.getCodecFor(typeId) instanceof BuilderCodec<?> builderCodec ? builderCodec : null);
   }

   @Nullable
   private static BuilderCodec<TriggerCondition> getConditionBuilderCodecFor(@Nonnull String typeId) {
      return (BuilderCodec<TriggerCondition>)(TriggerCondition.CODEC.getCodecFor(typeId) instanceof BuilderCodec<?> builderCodec ? builderCodec : null);
   }

   @Nonnull
   private static BsonDocument encodeEffect(@Nonnull BuilderCodec<TriggerEffect> codec, @Nonnull TriggerEffect effect) {
      try {
         return codec.encode(effect, EmptyExtraInfo.EMPTY);
      } catch (Exception var3) {
         return new BsonDocument();
      }
   }

   @Nonnull
   private static BsonDocument encodeCondition(@Nonnull BuilderCodec<TriggerCondition> codec, @Nonnull TriggerCondition condition) {
      try {
         return codec.encode(condition, EmptyExtraInfo.EMPTY);
      } catch (Exception var3) {
         return new BsonDocument();
      }
   }

   @Nullable
   private static BsonValue stringToBsonValue(@Nonnull Codec<?> childCodec, @Nullable String value) {
      if (value == null) {
         return null;
      } else if (childCodec == Codec.STRING) {
         return new BsonString(value);
      } else if (childCodec == Codec.STRING_ARRAY) {
         return stringsToBsonArray(value);
      } else if (childCodec == Codec.FLOAT || childCodec == Codec.DOUBLE) {
         return new BsonDouble(Double.parseDouble(value));
      } else if (childCodec == Codec.INTEGER) {
         return new BsonInt32(Integer.parseInt(value));
      } else if (childCodec == Codec.LONG) {
         return new BsonInt64(Long.parseLong(value));
      } else if (childCodec == Codec.BOOLEAN) {
         return new BsonBoolean(Boolean.parseBoolean(value));
      } else {
         return childCodec instanceof EnumCodec ? new BsonString(value) : new BsonString(value);
      }
   }

   @Nonnull
   private static BsonArray stringsToBsonArray(@Nonnull String value) {
      BsonArray array = new BsonArray();
      Arrays.stream(value.split(",")).map(String::trim).filter(stringValue -> !stringValue.isEmpty()).map(BsonString::new).forEach(array::add);
      return array;
   }

   @Nonnull
   private static String bsonValueToString(@Nonnull BsonValue value) {
      if (value instanceof BsonString bsonString) {
         return bsonString.getValue();
      } else if (value instanceof BsonBoolean bsonBoolean) {
         return String.valueOf(bsonBoolean.getValue());
      } else if (value instanceof BsonDouble bsonDouble) {
         return String.valueOf(bsonDouble.getValue());
      } else if (value instanceof BsonInt32 bsonInt) {
         return String.valueOf(bsonInt.getValue());
      } else {
         return value instanceof BsonInt64 bsonLong ? String.valueOf(bsonLong.getValue()) : value.toString();
      }
   }

   private static void materializeDefaults(@Nonnull BuilderCodec<TriggerEffect> codec, @Nonnull TriggerEffect effect) {
      BsonDocument encoded = encodeEffect(codec, effect);
      ExtraInfo extraInfo = ExtraInfo.THREAD_LOCAL.get();

      for (Entry<String, List<BuilderField<TriggerEffect, ?>>> entry : codec.getEntries().entrySet()) {
         String key = entry.getKey();
         if (!"Event".equals(key) && !"Interval".equals(key) && !encoded.containsKey(key) && !entry.getValue().isEmpty()) {
            BuilderField field = entry.getValue().getLast();
            BsonValue defaultValue = getDefaultBsonValue(getTypeId(effect), key, field.getCodec().getChildCodec());
            if (defaultValue != null) {
               BsonDocument doc = new BsonDocument();
               doc.put(key, defaultValue);

               try {
                  field.decode(doc, effect, extraInfo);
               } catch (Exception var11) {
               }
            }
         }
      }
   }

   private static void materializeConditionDefaults(@Nonnull BuilderCodec<TriggerCondition> codec, @Nonnull TriggerCondition condition) {
      BsonDocument encoded = encodeCondition(codec, condition);
      ExtraInfo extraInfo = ExtraInfo.THREAD_LOCAL.get();

      for (Entry<String, List<BuilderField<TriggerCondition, ?>>> entry : codec.getEntries().entrySet()) {
         String key = entry.getKey();
         if (!"Event".equals(key) && !encoded.containsKey(key) && !entry.getValue().isEmpty()) {
            BuilderField field = entry.getValue().getLast();
            BsonValue defaultValue = getDefaultBsonValue(getConditionTypeId(condition), key, field.getCodec().getChildCodec());
            if (defaultValue != null) {
               BsonDocument doc = new BsonDocument();
               doc.put(key, defaultValue);

               try {
                  field.decode(doc, condition, extraInfo);
               } catch (Exception var11) {
               }
            }
         }
      }
   }

   @Nullable
   private static BsonValue getDefaultBsonValue(@Nonnull String typeId, @Nonnull String fieldKey, @Nonnull Codec<?> childCodec) {
      BsonValue defaultValue = DEFAULT_FIELD_VALUES.get(typeId + "." + fieldKey);
      return defaultValue != null ? defaultValue : getDefaultBsonValue(childCodec);
   }

   @Nullable
   private static BsonValue getDefaultBsonValue(@Nonnull Codec<?> childCodec) {
      if (childCodec == Codec.BOOLEAN) {
         return new BsonBoolean(false);
      } else if (childCodec == Codec.FLOAT || childCodec == Codec.DOUBLE) {
         return new BsonDouble(0.0);
      } else if (childCodec == Codec.INTEGER) {
         return new BsonInt32(0);
      } else if (childCodec == Codec.LONG) {
         return new BsonInt64(0L);
      } else if (childCodec == Codec.STRING) {
         return new BsonString("");
      } else if (childCodec == Codec.STRING_ARRAY) {
         return new BsonArray();
      } else if (childCodec == Vector3dUtil.CODEC) {
         BsonDocument doc = new BsonDocument();
         doc.put("X", new BsonDouble(0.0));
         doc.put("Y", new BsonDouble(0.0));
         doc.put("Z", new BsonDouble(0.0));
         return doc;
      } else {
         return null;
      }
   }

   @Nonnull
   private static Collection<String> getAssetIdsForSource(@Nullable String sourceId) {
      return (Collection<String>)(sourceId == null ? List.of() : TriggerVolumesPlugin.get().getAssetIds(sourceId));
   }

   @Nullable
   private static String getAssetSourceForField(@Nonnull String typeId, @Nonnull String fieldKey) {
      return TriggerVolumesPlugin.get().getAssetSourceForField(typeId, fieldKey);
   }

   public static enum Action {
      Select,
      ChangeWorld,
      FilterChanged,
      ChangeTab,
      UpdateVolumeField,
      UpdateTag,
      RemoveTag,
      DeleteSelection,
      Save,
      Discard,
      SelectEffect,
      AddEffect,
      RemoveEffect,
      DuplicateEffect,
      MoveEffectUp,
      MoveEffectDown,
      UpdateAddTarget,
      UpdateAddEventType,
      ToggleEventCategory,
      UpdateParameter,
      TogglePrefabPreview,
      OpenPresetSave,
      PresetNameChanged,
      ConfirmSavePreset,
      CancelPresetSave,
      OpenPresetLoad,
      LoadPreset,
      CancelPresetLoad,
      OpenAssetPicker,
      AssetPickerSearch,
      AssetPickerSelect,
      ConfirmAssetPicker,
      PreviewSound,
      CancelAssetPicker;
   }

   private static enum EffectListKind {
      CONDITION,
      EFFECT,
      REJECTION_EFFECT;
   }

   private static enum EventCategoryScope {
      GROUP,
      VOLUME;
   }

   public static enum InspectorTab {
      VOLUME("server.customUI.triggerVolumeInspector.tab.volume", "server.customUI.triggerVolumeInspector.tab.volume.tooltip"),
      EFFECTS("server.customUI.triggerVolumeInspector.tab.effects", "server.customUI.triggerVolumeInspector.tab.effects.tooltip"),
      TAGS("server.customUI.triggerVolumeInspector.tab.tags", "server.customUI.triggerVolumeInspector.tab.tags.tooltip");

      private final String labelKey;
      private final String tooltipKey;

      private InspectorTab(@Nonnull String labelKey, @Nonnull String tooltipKey) {
         this.labelKey = labelKey;
         this.tooltipKey = tooltipKey;
      }

      @Nonnull
      Message label() {
         return Message.translation(this.labelKey);
      }

      @Nonnull
      Message tooltip() {
         return Message.translation(this.tooltipKey);
      }
   }

   public static class PageData {
      public static final BuilderCodec<TriggerVolumeInspectorPage.PageData> CODEC = BuilderCodec.builder(
            TriggerVolumeInspectorPage.PageData.class, TriggerVolumeInspectorPage.PageData::new
         )
         .append(
            new KeyedCodec<>("Action", new EnumCodec<>(TriggerVolumeInspectorPage.Action.class, EnumCodec.EnumStyle.LEGACY)),
            (o, v) -> o.action = v,
            o -> o.action
         )
         .add()
         .append(new KeyedCodec<>("Id", Codec.STRING, false), (o, v) -> o.id = v, o -> o.id)
         .add()
         .append(new KeyedCodec<>("IsGroup", Codec.STRING, false), (o, v) -> o.isGroup = v, o -> o.isGroup)
         .add()
         .append(new KeyedCodec<>("Tab", Codec.STRING, false), (o, v) -> o.tab = v, o -> o.tab)
         .add()
         .append(new KeyedCodec<>("@WorldName", Codec.STRING, false), (o, v) -> o.worldName = v, o -> o.worldName)
         .add()
         .append(new KeyedCodec<>("@FilterText", Codec.STRING, false), (o, v) -> o.filterText = v, o -> o.filterText)
         .add()
         .append(new KeyedCodec<>("@TagKey", Codec.STRING, false), (o, v) -> o.tagKey = v, o -> o.tagKey)
         .add()
         .append(new KeyedCodec<>("@TagValues", Codec.STRING, false), (o, v) -> o.tagValues = v, o -> o.tagValues)
         .add()
         .append(new KeyedCodec<>("RemoveTagKey", Codec.STRING, false), (o, v) -> o.removeTagKey = v, o -> o.removeTagKey)
         .add()
         .append(new KeyedCodec<>("EffectIndex", Codec.STRING, false), (o, v) -> o.effectIndex = v, o -> o.effectIndex)
         .add()
         .append(new KeyedCodec<>("EffectListKind", Codec.STRING, false), (o, v) -> o.effectListKind = v, o -> o.effectListKind)
         .add()
         .append(new KeyedCodec<>("@EffectType", Codec.STRING, false), (o, v) -> o.effectType = v, o -> o.effectType)
         .add()
         .append(new KeyedCodec<>("@AddTargetKind", Codec.STRING, false), (o, v) -> o.addTargetKind = v, o -> o.addTargetKind)
         .add()
         .append(new KeyedCodec<>("@AddEventType", Codec.STRING, false), (pageData, value) -> pageData.addEventType = value, pageData -> pageData.addEventType)
         .add()
         .append(new KeyedCodec<>("EventType", Codec.STRING, false), (o, v) -> o.eventType = v, o -> o.eventType)
         .add()
         .append(new KeyedCodec<>("EventCategoryScope", Codec.STRING, false), (o, v) -> o.eventCategoryScope = v, o -> o.eventCategoryScope)
         .add()
         .append(new KeyedCodec<>("ParamKey", Codec.STRING, false), (o, v) -> o.paramKey = v, o -> o.paramKey)
         .add()
         .append(new KeyedCodec<>("@ParamValue", Codec.STRING, false), (o, v) -> o.paramValue = v, o -> o.paramValue)
         .add()
         .append(new KeyedCodec<>("@ParamBool", Codec.BOOLEAN, false), (o, v) -> o.paramBool = v, o -> o.paramBool)
         .add()
         .append(new KeyedCodec<>("@ParamNumericValue", Codec.DOUBLE, false), (o, v) -> o.paramNumericValue = v, o -> o.paramNumericValue)
         .add()
         .append(new KeyedCodec<>("@VecX", Codec.DOUBLE, false), (o, v) -> o.vecX = v, o -> o.vecX)
         .add()
         .append(new KeyedCodec<>("@VecY", Codec.DOUBLE, false), (o, v) -> o.vecY = v, o -> o.vecY)
         .add()
         .append(new KeyedCodec<>("@VecZ", Codec.DOUBLE, false), (o, v) -> o.vecZ = v, o -> o.vecZ)
         .add()
         .append(new KeyedCodec<>("@PresetName", Codec.STRING, false), (o, v) -> o.presetName = v, o -> o.presetName)
         .add()
         .append(new KeyedCodec<>("PresetId", Codec.STRING, false), (o, v) -> o.presetId = v, o -> o.presetId)
         .add()
         .append(new KeyedCodec<>("@AssetPickerQuery", Codec.STRING, false), (o, v) -> o.assetPickerQuery = v, o -> o.assetPickerQuery)
         .add()
         .append(new KeyedCodec<>("AssetPickerSelection", Codec.STRING, false), (o, v) -> o.assetPickerSelection = v, o -> o.assetPickerSelection)
         .add()
         .build();
      public TriggerVolumeInspectorPage.Action action;
      public String id;
      public String isGroup;
      public String tab;
      public String worldName;
      public String filterText;
      public String tagKey;
      public String tagValues;
      public String removeTagKey;
      public String effectIndex;
      public String effectListKind;
      public String effectType;
      public String addTargetKind;
      public String addEventType;
      public String eventType;
      public String eventCategoryScope;
      public String paramKey;
      public String paramValue;
      public Boolean paramBool;
      public Double paramNumericValue;
      public Double vecX;
      public Double vecY;
      public Double vecZ;
      public String presetName;
      public String presetId;
      public String assetPickerQuery;
      public String assetPickerSelection;
   }

   private record RowEntry(@Nonnull String id, boolean isGroup, int listIndex) {
   }
}
