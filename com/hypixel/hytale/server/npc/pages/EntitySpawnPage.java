package com.hypixel.hytale.server.npc.pages;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.common.util.StringCompareUtil;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.NonSerialized;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.command.system.exceptions.GeneralCommandException;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.BlockEntity;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.EntityScaleComponent;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.component.PropComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.modules.entity.item.PreventItemMerging;
import com.hypixel.hytale.server.core.modules.entity.item.PreventPickup;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.ui.ItemGridSlot;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderInfo;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.spawning.ISpawnableWithModel;
import com.hypixel.hytale.server.spawning.SpawningContext;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntitySpawnPage extends InteractiveCustomUIPage<EntitySpawnPage.EntitySpawnPageEventData> {
   private static final String COMMON_TEXT_BUTTON_DOCUMENT = "Common/TextButton.ui";
   private static final String COMMON_UI_DOCUMENT = "Common.ui";
   private static final Value<String> BUTTON_LABEL_STYLE = Value.ref("Common/TextButton.ui", "LabelStyle");
   private static final Value<String> BUTTON_LABEL_STYLE_SELECTED = Value.ref("Common/TextButton.ui", "SelectedLabelStyle");
   private static final Value<String> TAB_STYLE_ACTIVE = Value.ref("Common.ui", "DefaultTextButtonStyle");
   private static final Value<String> TAB_STYLE_INACTIVE = Value.ref("Common.ui", "SecondaryTextButtonStyle");
   private static final String TAB_NPC = "NPC";
   private static final String TAB_ITEMS = "Items";
   private static final String TAB_MODEL = "Model";
   private static final String KEY_SELECT_AN_ITEM = "server.customUI.entitySpawnPage.selectAnItem";
   private static final String KEY_SELECT_AN_NPC = "server.customUI.entitySpawnPage.selectAnNpc";
   private static final String KEY_SELECT_A_MODEL = "server.customUI.entitySpawnPage.selectAModel";
   private static final int MAX_SPAWN_COUNT = 100;
   private static final float BLOCK_ENTITY_BASE_SCALE = 2.0F;
   private static final int LOOK_RAYCAST_DISTANCE = 4;
   private static final int FALLBACK_RAYCAST_DOWN_DISTANCE = 3;
   private static final double FALLBACK_RAYCAST_Y_OFFSET = 0.5;
   @Nonnull
   private String activeTab = "NPC";
   @Nonnull
   private String searchQuery = "";
   private List<String> npcRoles;
   @Nullable
   private String selectedNpcRole;
   private List<String> modelIds;
   @Nullable
   private String selectedModelId;
   @Nullable
   private String selectedItemId;
   @Nullable
   private Ref<EntityStore> modelPreview;
   private Vector3d position;
   private Vector3f rotation;
   private float currentRotationOffset = 0.0F;
   private float currentScale = 1.0F;
   private float lastPreviewScale = 1.0F;
   private long lastScaleUpdateTime = 0L;

   public EntitySpawnPage(@Nonnull PlayerRef playerRef) {
      super(playerRef, CustomPageLifetime.CanDismiss, EntitySpawnPage.EntitySpawnPageEventData.CODEC);
   }

   @Override
   public void build(
      @Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store
   ) {
      commandBuilder.append("Pages/EntitySpawnPage.ui");
      eventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#SearchInput", EventData.of("@SearchQuery", "#SearchInput.Value"), false);
      eventBuilder.addEventBinding(
         CustomUIEventBindingType.ValueChanged,
         "#RotationOffset",
         new EventData().append("Type", "UpdateRotationOffset").append("@RotationOffset", "#RotationOffset.Value"),
         false
      );
      eventBuilder.addEventBinding(
         CustomUIEventBindingType.ValueChanged, "#ScaleSlider", new EventData().append("Type", "UpdateScale").append("@Scale", "#ScaleSlider.Value"), false
      );
      eventBuilder.addEventBinding(
         CustomUIEventBindingType.MouseButtonReleased,
         "#ScaleSlider",
         new EventData().append("Type", "ScaleReleased").append("@Scale", "#ScaleSlider.Value"),
         false
      );
      eventBuilder.addEventBinding(
         CustomUIEventBindingType.Activating,
         "#Spawn",
         new EventData().append("Type", "Spawn").append("@Count", "#Count.Value").append("@Scale", "#ScaleSlider.Value"),
         false
      );
      eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#TabNPC", new EventData().append("Type", "TabSwitch").append("Tab", "NPC"), false);
      eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#TabItems", new EventData().append("Type", "TabSwitch").append("Tab", "Items"), false);
      eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#TabModel", new EventData().append("Type", "TabSwitch").append("Tab", "Model"), false);
      eventBuilder.addEventBinding(CustomUIEventBindingType.Dropped, "#ItemMaterialSlot", new EventData().append("Type", "SetItemMaterial"), false);
      eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#ClearMaterial", new EventData().append("Type", "ClearMaterial"), false);
      this.buildList(ref, store, commandBuilder, eventBuilder);
   }

   public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull EntitySpawnPage.EntitySpawnPageEventData data) {
      if (data.searchQuery != null) {
         this.searchQuery = data.searchQuery.trim().toLowerCase();
         UICommandBuilder commandBuilder = new UICommandBuilder();
         UIEventBuilder eventBuilder = new UIEventBuilder();
         this.buildList(ref, store, commandBuilder, eventBuilder);
         this.sendUpdate(commandBuilder, eventBuilder, false);
      } else {
         String var9 = data.type;
         switch (var9) {
            case "TabSwitch":
               if (data.tab != null && !data.tab.equals(this.activeTab)) {
                  this.activeTab = data.tab;
                  this.searchQuery = "";
                  this.clearPreview(store);
                  UICommandBuilder commandBuilder = new UICommandBuilder();
                  UIEventBuilder eventBuilder = new UIEventBuilder();
                  this.updateTabVisibility(commandBuilder);
                  commandBuilder.set("#SearchInput.Value", "");
                  this.buildList(ref, store, commandBuilder, eventBuilder);
                  this.sendUpdate(commandBuilder, eventBuilder, false);
               }
               break;
            case "Select":
               this.handleSelect(ref, store, data);
               break;
            case "SetItemMaterial":
               this.handleSetItemMaterial(ref, store, data);
               break;
            case "ClearMaterial":
               this.clearSelectedItem(ref, store);
               break;
            case "UpdateRotationOffset":
               this.currentRotationOffset = (float)Math.toRadians(data.rotationOffset);
               if (this.modelPreview != null && this.modelPreview.isValid()) {
                  TransformComponent transform = store.getComponent(this.modelPreview, TransformComponent.getComponentType());
                  transform.getRotation().setYaw(this.rotation.getYaw() + this.currentRotationOffset);
                  HeadRotation headRotation = store.getComponent(this.modelPreview, HeadRotation.getComponentType());
                  if (headRotation != null) {
                     headRotation.getRotation().setYaw(this.rotation.getYaw() + this.currentRotationOffset);
                  }
               }
               break;
            case "UpdateScale":
               if (data.scale != null && data.scale >= 0.1F) {
                  this.currentScale = data.scale;
                  UICommandBuilder commandBuilder = new UICommandBuilder();
                  commandBuilder.set("#ScaleValue.Text", String.format("%.1f", this.currentScale));
                  this.sendUpdate(commandBuilder, null, false);
                  long now = System.currentTimeMillis();
                  if (this.modelPreview != null
                     && this.modelPreview.isValid()
                     && this.currentScale != this.lastPreviewScale
                     && now - this.lastScaleUpdateTime >= 200L) {
                     this.lastScaleUpdateTime = now;
                     this.lastPreviewScale = this.currentScale;
                     this.updatePreviewScale(ref, store);
                  }
               }
               break;
            case "ScaleReleased":
               if (data.scale != null && data.scale >= 0.1F) {
                  this.currentScale = data.scale;
                  if ("Model".equals(this.activeTab) && this.selectedModelId != null) {
                     ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset(this.selectedModelId);
                     if (modelAsset != null) {
                        UICommandBuilder commandBuilder = new UICommandBuilder();
                        this.createOrUpdatePreview(ref, store, commandBuilder, Model.createStaticScaledModel(modelAsset, this.currentScale));
                     }
                  } else if ("Items".equals(this.activeTab) && this.selectedItemId != null) {
                     Item item = Item.getAssetMap().getAsset(this.selectedItemId);
                     if (item != null) {
                        Model model = this.getItemModel(item);
                        if (model != null) {
                           UICommandBuilder commandBuilder = new UICommandBuilder();
                           this.createOrUpdatePreview(ref, store, commandBuilder, model);
                        } else if (item.hasBlockType()) {
                           this.createOrUpdateBlockPreview(ref, store, this.selectedItemId);
                        } else {
                           this.createOrUpdateItemPreview(ref, store, this.selectedItemId);
                        }
                     }
                  }
               }
               break;
            case "Spawn":
               this.handleSpawn(ref, store, data);
         }
      }
   }

   private void handleSelect(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull EntitySpawnPage.EntitySpawnPageEventData data) {
      UICommandBuilder commandBuilder = new UICommandBuilder();
      String var5 = this.activeTab;
      switch (var5) {
         case "NPC":
            if (data.npcRole != null) {
               this.selectNPCRole(ref, store, data.npcRole, commandBuilder);
            }
            break;
         case "Items":
            if (data.itemId != null) {
               this.selectItem(ref, store, data.itemId, commandBuilder);
            }
            break;
         case "Model":
            if (data.modelId != null) {
               this.selectModel(ref, store, data.modelId, commandBuilder);
            }
      }

      this.sendUpdate(commandBuilder, null, false);
   }

   private void handleSetItemMaterial(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull EntitySpawnPage.EntitySpawnPageEventData data) {
      if (data.itemStackId != null) {
         UICommandBuilder commandBuilder = new UICommandBuilder();
         this.selectItem(ref, store, data.itemStackId, commandBuilder);
         this.sendUpdate(commandBuilder, null, false);
      }
   }

   private void clearSelectedItem(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
      this.selectedItemId = null;
      this.clearPreview(store);
      UICommandBuilder commandBuilder = new UICommandBuilder();
      commandBuilder.set("#SelectedName.Text", Message.translation("server.customUI.entitySpawnPage.selectAnItem"));
      commandBuilder.set("#ItemMaterialSlot.Slots", new ItemGridSlot[]{new ItemGridSlot()});
      commandBuilder.set("#ClearMaterial.Visible", false);
      commandBuilder.set("#DropIndicator.Visible", true);
      this.sendUpdate(commandBuilder, null, false);
   }

   private void handleSpawn(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull EntitySpawnPage.EntitySpawnPageEventData data) {
      String var4 = this.activeTab;
      switch (var4) {
         case "NPC":
            this.spawnNPC(ref, store, data.count);
            break;
         case "Items":
            this.spawnItem(ref, store, data.count);
            break;
         case "Model":
            this.spawnModel(ref, store, data.count);
      }
   }

   private void spawnNPC(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, int count) {
      if (this.selectedNpcRole != null && this.position != null && this.rotation != null) {
         if (count >= 1 && count <= 100) {
            this.clearPreview(store);
            Vector3f spawnRotation = this.rotation.clone();
            spawnRotation.setYaw(this.rotation.getYaw() + this.currentRotationOffset);

            for (int i = 0; i < count; i++) {
               NPCPlugin.get().spawnNPC(store, this.selectedNpcRole, null, this.position, spawnRotation);
            }

            store.getComponent(ref, Player.getComponentType()).getPageManager().setPage(ref, store, Page.None);
            this.playerRef
               .sendMessage(
                  Message.translation(count == 1 ? "server.npc.spawn.spawnedOne" : "server.npc.spawn.spawnedMany")
                     .param("quantity", count)
                     .param("type", this.selectedNpcRole)
               );
         }
      }
   }

   private void spawnModel(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, int count) {
      if (this.selectedModelId != null && this.position != null && this.rotation != null) {
         if (count >= 1 && count <= 100) {
            this.clearPreview(store);
            ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset(this.selectedModelId);
            if (modelAsset != null) {
               Model model = Model.createStaticScaledModel(modelAsset, this.currentScale);
               Vector3f spawnRotation = this.rotation.clone();
               spawnRotation.setYaw(this.rotation.getYaw() + this.currentRotationOffset);

               for (int i = 0; i < count; i++) {
                  Holder<EntityStore> holder = store.getRegistry().newHolder();
                  holder.addComponent(NetworkId.getComponentType(), new NetworkId(store.getExternalData().takeNextNetworkId()));
                  holder.addComponent(TransformComponent.getComponentType(), new TransformComponent(this.position, spawnRotation));
                  holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
                  holder.addComponent(
                     PersistentModel.getComponentType(), new PersistentModel(new Model.ModelReference(this.selectedModelId, this.currentScale, null, true))
                  );
                  holder.addComponent(HeadRotation.getComponentType(), new HeadRotation(spawnRotation));
                  holder.addComponent(PropComponent.getComponentType(), PropComponent.get());
                  holder.ensureComponent(UUIDComponent.getComponentType());
                  store.addEntity(holder, AddReason.SPAWN);
               }

               store.getComponent(ref, Player.getComponentType()).getPageManager().setPage(ref, store, Page.None);
               this.playerRef
                  .sendMessage(
                     Message.translation("server.customUI.entitySpawnPage.spawnedModel").param("quantity", count).param("model", this.selectedModelId)
                  );
            }
         }
      }
   }

   @Override
   public void onDismiss(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
      this.clearPreview(store);
   }

   private void clearPreview(@Nonnull Store<EntityStore> store) {
      if (this.modelPreview != null && this.modelPreview.isValid()) {
         store.removeEntity(this.modelPreview, RemoveReason.REMOVE);
      }

      this.modelPreview = null;
   }

   private void updateTabVisibility(@Nonnull UICommandBuilder commandBuilder) {
      commandBuilder.set("#NPCContent.Visible", this.activeTab.equals("NPC"));
      commandBuilder.set("#ItemsContent.Visible", this.activeTab.equals("Items"));
      commandBuilder.set("#ModelContent.Visible", this.activeTab.equals("Model"));
      commandBuilder.set("#TabNPC.Style", this.activeTab.equals("NPC") ? TAB_STYLE_ACTIVE : TAB_STYLE_INACTIVE);
      commandBuilder.set("#TabItems.Style", this.activeTab.equals("Items") ? TAB_STYLE_ACTIVE : TAB_STYLE_INACTIVE);
      commandBuilder.set("#TabModel.Style", this.activeTab.equals("Model") ? TAB_STYLE_ACTIVE : TAB_STYLE_INACTIVE);
      commandBuilder.set("#RotationGroup.Visible", this.activeTab.equals("Model") || this.activeTab.equals("NPC") || this.activeTab.equals("Items"));
      commandBuilder.set("#ScaleGroup.Visible", this.activeTab.equals("Model") || this.activeTab.equals("Items"));
      if (this.activeTab.equals("Model") || this.activeTab.equals("Items")) {
         commandBuilder.set("#ScaleSlider.Value", this.currentScale);
         commandBuilder.set("#ScaleValue.Text", String.format("%.1f", this.currentScale));
      }
   }

   private void buildList(
      @Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder
   ) {
      this.updateTabVisibility(commandBuilder);
      String var5 = this.activeTab;
      switch (var5) {
         case "NPC":
            this.buildNPCList(ref, store, commandBuilder, eventBuilder);
            break;
         case "Items":
            this.buildItemsContent(ref, store, commandBuilder);
            break;
         case "Model":
            this.buildModelList(ref, store, commandBuilder, eventBuilder);
      }
   }

   private void buildNPCList(
      @Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder
   ) {
      commandBuilder.clear("#NPCList");
      List<String> roleTemplateNames = NPCPlugin.get().getRoleTemplateNames(true);
      if (!this.searchQuery.isEmpty()) {
         Object2IntMap<String> map = new Object2IntOpenHashMap(roleTemplateNames.size());

         for (String value : roleTemplateNames) {
            int fuzzyDistance = StringCompareUtil.getFuzzyDistance(value, this.searchQuery, Locale.ENGLISH);
            if (fuzzyDistance > 0) {
               map.put(value, fuzzyDistance);
            }
         }

         this.npcRoles = map.keySet().stream().sorted().sorted(Comparator.comparingInt(map::getInt).reversed()).limit(20L).collect(Collectors.toList());
      } else {
         roleTemplateNames.sort(String::compareTo);
         this.npcRoles = roleTemplateNames;
      }

      int i = 0;

      for (int bound = this.npcRoles.size(); i < bound; i++) {
         String id = this.npcRoles.get(i);
         String selector = "#NPCList[" + i + "]";
         commandBuilder.append("#NPCList", "Common/TextButton.ui");
         commandBuilder.set(selector + " #Button.Text", id);
         eventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating, selector + " #Button", new EventData().append("Type", "Select").append("NPCRole", id), false
         );
      }

      if (!this.npcRoles.isEmpty() && this.selectedNpcRole != null) {
         if (this.npcRoles.contains(this.selectedNpcRole)) {
            this.selectNPCRole(ref, store, this.selectedNpcRole, commandBuilder);
         } else {
            this.selectedNpcRole = null;
            this.clearPreview(store);
            commandBuilder.set("#SelectedName.Text", Message.translation("server.customUI.entitySpawnPage.selectAnNpc"));
         }
      } else if (this.selectedNpcRole == null) {
         commandBuilder.set("#SelectedName.Text", Message.translation("server.customUI.entitySpawnPage.selectAnNpc"));
      }
   }

   private void buildModelList(
      @Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder
   ) {
      commandBuilder.clear("#ModelList");
      Set<String> itemAndBlockModelPaths = new HashSet<>();

      for (Item item : Item.getAssetMap().getAssetMap().values()) {
         if (item.getModel() != null) {
            itemAndBlockModelPaths.add(item.getModel());
         }
      }

      for (BlockType blockType : BlockType.getAssetMap().getAssetMap().values()) {
         if (blockType.getCustomModel() != null) {
            itemAndBlockModelPaths.add(blockType.getCustomModel());
         }
      }

      Set<String> allModels = new HashSet<>();

      for (Entry<String, ModelAsset> entry : ModelAsset.getAssetMap().getAssetMap().entrySet()) {
         String modelPath = entry.getValue().getModel();
         if (modelPath != null && !itemAndBlockModelPaths.contains(modelPath) && !modelPath.startsWith("Items/Projectiles/")) {
            allModels.add(entry.getKey());
         }
      }

      if (!this.searchQuery.isEmpty()) {
         Object2IntMap<String> map = new Object2IntOpenHashMap(allModels.size());

         for (String value : allModels) {
            int fuzzyDistance = StringCompareUtil.getFuzzyDistance(value, this.searchQuery, Locale.ENGLISH);
            if (fuzzyDistance > 0) {
               map.put(value, fuzzyDistance);
            }
         }

         this.modelIds = map.keySet().stream().sorted().sorted(Comparator.comparingInt(map::getInt).reversed()).limit(20L).collect(Collectors.toList());
      } else {
         this.modelIds = allModels.stream().sorted().collect(Collectors.toList());
      }

      int i = 0;

      for (int bound = this.modelIds.size(); i < bound; i++) {
         String id = this.modelIds.get(i);
         String selector = "#ModelList[" + i + "]";
         commandBuilder.append("#ModelList", "Common/TextButton.ui");
         commandBuilder.set(selector + " #Button.Text", id);
         eventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating, selector + " #Button", new EventData().append("Type", "Select").append("ModelId", id), false
         );
      }

      if (!this.modelIds.isEmpty() && this.selectedModelId != null) {
         if (this.modelIds.contains(this.selectedModelId)) {
            this.selectModel(ref, store, this.selectedModelId, commandBuilder);
         } else {
            this.selectedModelId = null;
            this.clearPreview(store);
            commandBuilder.set("#SelectedName.Text", Message.translation("server.customUI.entitySpawnPage.selectAModel"));
         }
      } else if (this.selectedModelId == null) {
         commandBuilder.set("#SelectedName.Text", Message.translation("server.customUI.entitySpawnPage.selectAModel"));
      }
   }

   private void buildItemsContent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull UICommandBuilder commandBuilder) {
      if (this.selectedItemId != null) {
         Item item = Item.getAssetMap().getAsset(this.selectedItemId);
         if (item != null) {
            commandBuilder.set("#SelectedName.Text", this.selectedItemId);
            commandBuilder.set("#ItemMaterialSlot.Slots", new ItemGridSlot[]{new ItemGridSlot(new ItemStack(this.selectedItemId, 1))});
            commandBuilder.set("#ClearMaterial.Visible", true);
            commandBuilder.set("#DropIndicator.Visible", false);
         }
      } else {
         commandBuilder.set("#SelectedName.Text", Message.translation("server.customUI.entitySpawnPage.selectAnItem"));
         commandBuilder.set("#ItemMaterialSlot.Slots", new ItemGridSlot[]{new ItemGridSlot()});
         commandBuilder.set("#ClearMaterial.Visible", false);
         commandBuilder.set("#DropIndicator.Visible", true);
      }
   }

   private void selectItem(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull String itemId, @Nonnull UICommandBuilder commandBuilder) {
      Item item = Item.getAssetMap().getAsset(itemId);
      if (item != null) {
         this.selectedItemId = itemId;
         commandBuilder.set("#SelectedName.Text", itemId);
         commandBuilder.set("#ItemMaterialSlot.Slots", new ItemGridSlot[]{new ItemGridSlot(new ItemStack(itemId, 1))});
         commandBuilder.set("#ClearMaterial.Visible", true);
         commandBuilder.set("#DropIndicator.Visible", false);
         Model model = this.getItemModel(item);
         if (model != null) {
            this.createOrUpdatePreview(ref, store, commandBuilder, model);
         } else if (item.hasBlockType()) {
            this.createOrUpdateBlockPreview(ref, store, itemId);
         } else {
            this.createOrUpdateItemPreview(ref, store, itemId);
         }
      }
   }

   private void spawnItem(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, int count) {
      if (this.selectedItemId != null && this.position != null && this.rotation != null) {
         if (count >= 1 && count <= 100) {
            Item item = Item.getAssetMap().getAsset(this.selectedItemId);
            if (item != null) {
               this.clearPreview(store);
               Vector3f spawnRotation = this.rotation.clone();
               spawnRotation.setYaw(this.rotation.getYaw() + this.currentRotationOffset);
               Model model = this.getItemModel(item);
               if (model != null) {
                  String modelId = this.getItemModelId(item);

                  for (int i = 0; i < count; i++) {
                     Holder<EntityStore> holder = store.getRegistry().newHolder();
                     holder.addComponent(NetworkId.getComponentType(), new NetworkId(store.getExternalData().takeNextNetworkId()));
                     holder.addComponent(TransformComponent.getComponentType(), new TransformComponent(this.position, spawnRotation));
                     holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
                     holder.addComponent(
                        PersistentModel.getComponentType(), new PersistentModel(new Model.ModelReference(modelId, this.currentScale, null, true))
                     );
                     ItemStack itemStack = new ItemStack(this.selectedItemId, 1);
                     itemStack.setOverrideDroppedItemAnimation(true);
                     holder.addComponent(ItemComponent.getComponentType(), new ItemComponent(itemStack));
                     holder.addComponent(PreventPickup.getComponentType(), PreventPickup.INSTANCE);
                     holder.addComponent(PreventItemMerging.getComponentType(), PreventItemMerging.INSTANCE);
                     holder.addComponent(HeadRotation.getComponentType(), new HeadRotation(spawnRotation));
                     holder.addComponent(PropComponent.getComponentType(), PropComponent.get());
                     holder.ensureComponent(UUIDComponent.getComponentType());
                     store.addEntity(holder, AddReason.SPAWN);
                  }
               } else if (item.hasBlockType()) {
                  for (int i = 0; i < count; i++) {
                     Holder<EntityStore> holder = store.getRegistry().newHolder();
                     holder.addComponent(BlockEntity.getComponentType(), new BlockEntity(this.selectedItemId));
                     holder.addComponent(TransformComponent.getComponentType(), new TransformComponent(this.position, spawnRotation));
                     holder.addComponent(EntityScaleComponent.getComponentType(), new EntityScaleComponent(this.currentScale * 2.0F));
                     ItemStack itemStack = new ItemStack(this.selectedItemId, 1);
                     itemStack.setOverrideDroppedItemAnimation(true);
                     holder.addComponent(ItemComponent.getComponentType(), new ItemComponent(itemStack));
                     holder.addComponent(PreventPickup.getComponentType(), PreventPickup.INSTANCE);
                     holder.addComponent(PreventItemMerging.getComponentType(), PreventItemMerging.INSTANCE);
                     holder.addComponent(HeadRotation.getComponentType(), new HeadRotation(spawnRotation));
                     holder.addComponent(PropComponent.getComponentType(), PropComponent.get());
                     holder.ensureComponent(UUIDComponent.getComponentType());
                     store.addEntity(holder, AddReason.SPAWN);
                  }
               } else {
                  for (int i = 0; i < count; i++) {
                     Holder<EntityStore> holder = store.getRegistry().newHolder();
                     holder.addComponent(NetworkId.getComponentType(), new NetworkId(store.getExternalData().takeNextNetworkId()));
                     holder.addComponent(TransformComponent.getComponentType(), new TransformComponent(this.position, spawnRotation));
                     ItemStack itemStack = new ItemStack(this.selectedItemId, 1);
                     itemStack.setOverrideDroppedItemAnimation(true);
                     holder.addComponent(ItemComponent.getComponentType(), new ItemComponent(itemStack));
                     holder.addComponent(EntityScaleComponent.getComponentType(), new EntityScaleComponent(this.currentScale));
                     holder.addComponent(PreventPickup.getComponentType(), PreventPickup.INSTANCE);
                     holder.addComponent(PreventItemMerging.getComponentType(), PreventItemMerging.INSTANCE);
                     holder.addComponent(HeadRotation.getComponentType(), new HeadRotation(spawnRotation));
                     holder.addComponent(PropComponent.getComponentType(), PropComponent.get());
                     store.addEntity(holder, AddReason.SPAWN);
                  }
               }

               store.getComponent(ref, Player.getComponentType()).getPageManager().setPage(ref, store, Page.None);
               this.playerRef
                  .sendMessage(Message.translation("server.customUI.entitySpawnPage.spawnedItem").param("quantity", count).param("item", this.selectedItemId));
            }
         }
      }
   }

   @Nullable
   private String getItemModelId(@Nonnull Item item) {
      String modelId = item.getModel();
      if (modelId == null && item.hasBlockType()) {
         BlockType blockType = BlockType.getAssetMap().getAsset(item.getId());
         if (blockType != null && blockType.getCustomModel() != null) {
            modelId = blockType.getCustomModel();
         }
      }

      return modelId;
   }

   @Nullable
   private Model getItemModel(@Nonnull Item item) {
      String modelId = this.getItemModelId(item);
      if (modelId == null) {
         return null;
      } else {
         ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset(modelId);
         return modelAsset != null ? Model.createStaticScaledModel(modelAsset, this.currentScale) : null;
      }
   }

   private void selectNPCRole(
      @Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull String npcRole, @Nonnull UICommandBuilder commandBuilder
   ) {
      if (this.selectedNpcRole != null && this.npcRoles.contains(this.selectedNpcRole)) {
         commandBuilder.set("#NPCList[" + this.npcRoles.indexOf(this.selectedNpcRole) + "] #Button.Style", BUTTON_LABEL_STYLE);
      }

      commandBuilder.set("#NPCList[" + this.npcRoles.indexOf(npcRole) + "] #Button.Style", BUTTON_LABEL_STYLE_SELECTED);
      commandBuilder.set("#SelectedName.Text", npcRole);
      this.selectedNpcRole = npcRole;
      this.createOrUpdatePreview(ref, store, commandBuilder, this.getNPCModel());
   }

   private void selectModel(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull String modelId, @Nonnull UICommandBuilder commandBuilder) {
      if (this.selectedModelId != null && this.modelIds.contains(this.selectedModelId)) {
         commandBuilder.set("#ModelList[" + this.modelIds.indexOf(this.selectedModelId) + "] #Button.Style", BUTTON_LABEL_STYLE);
      }

      commandBuilder.set("#ModelList[" + this.modelIds.indexOf(modelId) + "] #Button.Style", BUTTON_LABEL_STYLE_SELECTED);
      commandBuilder.set("#SelectedName.Text", modelId);
      this.selectedModelId = modelId;
      ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset(modelId);
      if (modelAsset != null) {
         this.createOrUpdatePreview(ref, store, commandBuilder, Model.createStaticScaledModel(modelAsset, this.currentScale));
      }
   }

   private void initPosition(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
      TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      HeadRotation headRotationComponent = store.getComponent(ref, HeadRotation.getComponentType());

      assert headRotationComponent != null;

      Vector3d playerPosition = transformComponent.getPosition();
      Vector3f headRotation = headRotationComponent.getRotation();
      Vector3d direction = Transform.getDirection(headRotation.getPitch(), headRotation.getYaw());
      Vector3d lookTarget = TargetUtil.getTargetLocation(ref, 4.0, store);
      Vector3d previewPosition;
      if (lookTarget != null) {
         previewPosition = lookTarget;
      } else {
         Vector3d aheadPosition = playerPosition.clone().add(direction.clone().scale(4.0));
         World world = store.getExternalData().getWorld();
         Vector3i groundTarget = TargetUtil.getTargetBlock(
            world, (blockId, fluidId) -> blockId != 0, aheadPosition.x, aheadPosition.y + 0.5, aheadPosition.z, 0.0, -1.0, 0.0, 3.0
         );
         if (groundTarget != null) {
            previewPosition = new Vector3d(groundTarget.x + 0.5, groundTarget.y + 1, groundTarget.z + 0.5);
         } else {
            previewPosition = aheadPosition;
         }
      }

      Vector3d relativePos = playerPosition.clone().subtract(previewPosition);
      relativePos.setY(0.0);
      Vector3f previewRotation = Vector3f.lookAt(relativePos);
      this.position = previewPosition;
      this.rotation = previewRotation;
      this.currentRotationOffset = 0.0F;
   }

   private void createOrUpdatePreview(
      @Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull UICommandBuilder commandBuilder, @Nullable Model model
   ) {
      if (model != null) {
         if (this.modelPreview != null && this.modelPreview.isValid()) {
            store.putComponent(this.modelPreview, ModelComponent.getComponentType(), new ModelComponent(model));
         } else {
            this.initPosition(ref, store);
            Holder<EntityStore> holder = store.getRegistry().newHolder();
            holder.addComponent(NetworkId.getComponentType(), new NetworkId(store.getExternalData().takeNextNetworkId()));
            holder.addComponent(EntityStore.REGISTRY.getNonSerializedComponentType(), NonSerialized.get());
            holder.addComponent(TransformComponent.getComponentType(), new TransformComponent(this.position, this.rotation));
            holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
            holder.addComponent(HeadRotation.getComponentType(), new HeadRotation(this.rotation));
            this.modelPreview = store.addEntity(holder, AddReason.SPAWN);
            this.lastPreviewScale = this.currentScale;
         }
      }
   }

   private void updatePreviewScale(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
      if (this.modelPreview != null && this.modelPreview.isValid()) {
         EntityScaleComponent existingScale = store.getComponent(this.modelPreview, EntityScaleComponent.getComponentType());
         if (existingScale != null) {
            boolean hasBlock = store.getComponent(this.modelPreview, BlockEntity.getComponentType()) != null;
            existingScale.setScale(hasBlock ? this.currentScale * 2.0F : this.currentScale);
         } else if ("Model".equals(this.activeTab) && this.selectedModelId != null) {
            ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset(this.selectedModelId);
            if (modelAsset != null) {
               store.putComponent(
                  this.modelPreview, ModelComponent.getComponentType(), new ModelComponent(Model.createStaticScaledModel(modelAsset, this.currentScale))
               );
            }
         }
      }
   }

   private void createOrUpdateBlockPreview(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull String blockTypeKey) {
      this.clearPreview(store);
      this.initPosition(ref, store);
      Holder<EntityStore> holder = store.getRegistry().newHolder();
      holder.addComponent(NetworkId.getComponentType(), new NetworkId(store.getExternalData().takeNextNetworkId()));
      holder.addComponent(EntityStore.REGISTRY.getNonSerializedComponentType(), NonSerialized.get());
      holder.addComponent(BlockEntity.getComponentType(), new BlockEntity(blockTypeKey));
      holder.addComponent(TransformComponent.getComponentType(), new TransformComponent(this.position, this.rotation));
      holder.addComponent(EntityScaleComponent.getComponentType(), new EntityScaleComponent(this.currentScale * 2.0F));
      holder.addComponent(HeadRotation.getComponentType(), new HeadRotation(this.rotation));
      this.modelPreview = store.addEntity(holder, AddReason.SPAWN);
      this.lastPreviewScale = this.currentScale;
   }

   private void createOrUpdateItemPreview(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull String itemId) {
      this.clearPreview(store);
      this.initPosition(ref, store);
      Holder<EntityStore> holder = store.getRegistry().newHolder();
      holder.addComponent(NetworkId.getComponentType(), new NetworkId(store.getExternalData().takeNextNetworkId()));
      holder.addComponent(EntityStore.REGISTRY.getNonSerializedComponentType(), NonSerialized.get());
      ItemStack itemStack = new ItemStack(itemId, 1);
      itemStack.setOverrideDroppedItemAnimation(true);
      holder.addComponent(ItemComponent.getComponentType(), new ItemComponent(itemStack));
      holder.addComponent(EntityScaleComponent.getComponentType(), new EntityScaleComponent(this.currentScale));
      holder.addComponent(PreventPickup.getComponentType(), PreventPickup.INSTANCE);
      holder.addComponent(TransformComponent.getComponentType(), new TransformComponent(this.position, this.rotation));
      holder.addComponent(HeadRotation.getComponentType(), new HeadRotation(this.rotation));
      this.modelPreview = store.addEntity(holder, AddReason.SPAWN);
      this.lastPreviewScale = this.currentScale;
   }

   @Nullable
   private Model getNPCModel() {
      NPCPlugin npcPlugin = NPCPlugin.get();
      int roleIndex = npcPlugin.getIndex(this.selectedNpcRole);
      npcPlugin.forceValidation(roleIndex);
      BuilderInfo roleBuilderInfo = npcPlugin.getRoleBuilderInfo(roleIndex);
      if (roleBuilderInfo == null) {
         throw new IllegalStateException("Can't find a matching role builder");
      } else if (!npcPlugin.testAndValidateRole(roleBuilderInfo)) {
         throw new GeneralCommandException(Message.translation("server.commands.npc.spawn.validation_failed"));
      } else {
         Builder<Role> roleBuilder = npcPlugin.tryGetCachedValidRole(roleIndex);
         if (roleBuilder == null) {
            throw new IllegalArgumentException("Can't find a matching role builder");
         } else if (roleBuilder instanceof ISpawnableWithModel spawnable) {
            if (!roleBuilder.isSpawnable()) {
               throw new IllegalArgumentException("Abstract role templates cannot be spawned directly - a variant needs to be created!");
            } else {
               SpawningContext spawningContext = new SpawningContext();
               if (!spawningContext.setSpawnable(spawnable)) {
                  throw new GeneralCommandException(Message.translation("server.commands.npc.spawn.cantSetRolebuilder"));
               } else {
                  return spawningContext.getModel();
               }
            }
         } else {
            throw new IllegalArgumentException("Role builder must support ISpawnableWithModel interface");
         }
      }
   }

   public static class EntitySpawnPageEventData {
      static final String KEY_NPC_ROLE = "NPCRole";
      static final String KEY_MODEL_ID = "ModelId";
      static final String KEY_ITEM_ID = "ItemId";
      static final String KEY_ITEM_STACK_ID = "ItemStackId";
      static final String KEY_TYPE = "Type";
      static final String KEY_TAB = "Tab";
      static final String KEY_SEARCH_QUERY = "@SearchQuery";
      static final String KEY_COUNT = "@Count";
      static final String KEY_ROTATION_OFFSET = "@RotationOffset";
      static final String KEY_SCALE = "@Scale";
      public static final BuilderCodec<EntitySpawnPage.EntitySpawnPageEventData> CODEC = BuilderCodec.builder(
            EntitySpawnPage.EntitySpawnPageEventData.class, EntitySpawnPage.EntitySpawnPageEventData::new
         )
         .append(new KeyedCodec<>("NPCRole", Codec.STRING), (entry, s) -> entry.npcRole = s, entry -> entry.npcRole)
         .add()
         .append(new KeyedCodec<>("ModelId", Codec.STRING), (entry, s) -> entry.modelId = s, entry -> entry.modelId)
         .add()
         .append(new KeyedCodec<>("ItemId", Codec.STRING), (entry, s) -> entry.itemId = s, entry -> entry.itemId)
         .add()
         .append(new KeyedCodec<>("ItemStackId", Codec.STRING), (entry, s) -> entry.itemStackId = s, entry -> entry.itemStackId)
         .add()
         .append(new KeyedCodec<>("Type", Codec.STRING), (entry, s) -> entry.type = s, entry -> entry.type)
         .add()
         .append(new KeyedCodec<>("Tab", Codec.STRING), (entry, s) -> entry.tab = s, entry -> entry.tab)
         .add()
         .append(new KeyedCodec<>("@SearchQuery", Codec.STRING), (entry, s) -> entry.searchQuery = s, entry -> entry.searchQuery)
         .add()
         .append(new KeyedCodec<>("@Count", Codec.INTEGER), (entry, s) -> entry.count = s, entry -> entry.count)
         .add()
         .append(new KeyedCodec<>("@RotationOffset", Codec.FLOAT), (entry, s) -> entry.rotationOffset = s, entry -> entry.rotationOffset)
         .add()
         .append(new KeyedCodec<>("@Scale", Codec.FLOAT), (entry, s) -> entry.scale = s, entry -> entry.scale)
         .add()
         .build();
      private String npcRole;
      private String modelId;
      private String itemId;
      private String itemStackId;
      private String type;
      private String tab;
      private String searchQuery;
      private int count;
      private float rotationOffset;
      private Float scale;
   }
}
