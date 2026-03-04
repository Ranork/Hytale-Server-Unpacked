package com.hypixel.hytale.builtin.model.pages;

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
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.cosmetics.CosmeticsModule;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSkinComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ChangeModelPage extends InteractiveCustomUIPage<ChangeModelPage.PageEventData> {
   @Nonnull
   private static final String COMMON_TEXT_BUTTON_DOCUMENT = "Common/TextButton.ui";
   @Nonnull
   private static final Value<String> BUTTON_LABEL_STYLE = Value.ref("Common/TextButton.ui", "LabelStyle");
   @Nonnull
   private static final Value<String> BUTTON_LABEL_STYLE_SELECTED = Value.ref("Common/TextButton.ui", "SelectedLabelStyle");
   @Nonnull
   private String searchQuery = "";
   private List<String> models;
   @Nullable
   private String selectedModel;
   @Nullable
   private Ref<EntityStore> modelPreview;
   private Vector3d position;
   private Vector3f rotation;
   private float scale = 1.0F;

   public ChangeModelPage(@Nonnull PlayerRef playerRef) {
      super(playerRef, CustomPageLifetime.CanDismiss, ChangeModelPage.PageEventData.CODEC);
   }

   @Override
   public void build(
      @Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store
   ) {
      commandBuilder.append("Pages/ChangeModelPage.ui");
      eventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#SearchInput", EventData.of("@SearchQuery", "#SearchInput.Value"), false);
      eventBuilder.addEventBinding(
         CustomUIEventBindingType.ValueChanged, "#Scale", new EventData().append("Type", "UpdateScale").append("@Scale", "#Scale.Value"), false
      );
      eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#ChangeModel", new EventData().append("Type", "ChangeModel"), false);
      eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#ResetModel", new EventData().append("Type", "ResetModel"), false);
      this.buildModelList(ref, store, commandBuilder, eventBuilder);
   }

   public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull ChangeModelPage.PageEventData data) {
      if (data.searchQuery != null) {
         this.searchQuery = data.searchQuery.trim().toLowerCase();
         UICommandBuilder commandBuilder = new UICommandBuilder();
         UIEventBuilder eventBuilder = new UIEventBuilder();
         this.buildModelList(ref, store, commandBuilder, eventBuilder);
         this.sendUpdate(commandBuilder, eventBuilder, false);
      } else {
         String var9 = data.type;
         switch (var9) {
            case "Select":
               if (data.model != null) {
                  UICommandBuilder commandBuilder = new UICommandBuilder();
                  this.selectModel(ref, store, data.model, commandBuilder);
                  this.sendUpdate(commandBuilder, null, false);
               }
               break;
            case "UpdateScale":
               this.scale = data.scale;
               if (this.modelPreview.isValid()) {
                  store.putComponent(this.modelPreview, ModelComponent.getComponentType(), new ModelComponent(this.getModel(this.scale)));
               }
               break;
            case "ChangeModel":
               if (this.selectedModel != null) {
                  if (this.modelPreview.isValid()) {
                     store.removeEntity(this.modelPreview, RemoveReason.REMOVE);
                  }

                  Model model = this.getModel(this.scale);
                  store.putComponent(ref, ModelComponent.getComponentType(), new ModelComponent(model));
                  store.getComponent(ref, Player.getComponentType()).getPageManager().setPage(ref, store, Page.None);
               }
               break;
            case "ResetModel":
               PlayerSkinComponent skinComponent = store.getComponent(ref, PlayerSkinComponent.getComponentType());
               if (skinComponent == null) {
                  return;
               }

               PlayerSkinComponent playerSkinComponent = store.getComponent(ref, PlayerSkinComponent.getComponentType());
               Model newModel = CosmeticsModule.get().createModel(playerSkinComponent.getPlayerSkin());
               store.putComponent(ref, ModelComponent.getComponentType(), new ModelComponent(newModel));
               playerSkinComponent.setNetworkOutdated();
         }
      }
   }

   @Override
   public void onDismiss(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
      if (this.modelPreview != null && this.modelPreview.isValid()) {
         store.removeEntity(this.modelPreview, RemoveReason.REMOVE);
      }
   }

   private void buildModelList(
      @Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder
   ) {
      commandBuilder.clear("#ModelList");
      Set<String> roleTemplateNames = ModelAsset.getAssetMap().getAssetMap().keySet();
      if (!this.searchQuery.isEmpty()) {
         Object2IntMap<String> map = new Object2IntOpenHashMap(roleTemplateNames.size());

         for (String value : roleTemplateNames) {
            int fuzzyDistance = StringCompareUtil.getFuzzyDistance(value, this.searchQuery, Locale.ENGLISH);
            if (fuzzyDistance > 0) {
               map.put(value, fuzzyDistance);
            }
         }

         this.models = map.keySet().stream().sorted().sorted(Comparator.comparingInt(map::getInt).reversed()).limit(20L).collect(Collectors.toList());
      } else {
         this.models = roleTemplateNames.stream().sorted().sorted(String::compareTo).collect(Collectors.toList());
      }

      int i = 0;

      for (int bound = this.models.size(); i < bound; i++) {
         String id = this.models.get(i);
         String selector = "#ModelList[" + i + "]";
         commandBuilder.append("#ModelList", "Common/TextButton.ui");
         commandBuilder.set(selector + " #Button.Text", id);
         eventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating, selector + " #Button", new EventData().append("Type", "Select").append("Model", id), false
         );
      }

      if (!this.models.isEmpty()) {
         if (!this.models.contains(this.selectedModel)) {
            this.selectModel(ref, store, this.models.getFirst(), commandBuilder);
         } else if (this.selectedModel != null) {
            this.selectModel(ref, store, this.selectedModel, commandBuilder);
         }
      }
   }

   private void selectModel(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull String modelId, @Nonnull UICommandBuilder commandBuilder) {
      if (this.selectedModel != null && this.models.contains(this.selectedModel)) {
         commandBuilder.set("#ModelList[" + this.models.indexOf(this.selectedModel) + "] #Button.Style", BUTTON_LABEL_STYLE);
      }

      commandBuilder.set("#ModelList[" + this.models.indexOf(modelId) + "] #Button.Style", BUTTON_LABEL_STYLE_SELECTED);
      commandBuilder.set("#ModelName.Text", modelId);
      this.selectedModel = modelId;
      if (this.modelPreview == null || !this.modelPreview.isValid()) {
         TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

         assert transformComponent != null;

         HeadRotation headRotationComponent = store.getComponent(ref, HeadRotation.getComponentType());

         assert headRotationComponent != null;

         Vector3d playerPosition = transformComponent.getPosition();
         Vector3f headRotation = headRotationComponent.getRotation();
         Vector3d previewPosition = TargetUtil.getTargetLocation(ref, 8.0, store);
         if (previewPosition == null) {
            previewPosition = playerPosition.clone().add(Transform.getDirection(headRotation.getPitch(), headRotation.getYaw()).scale(4.0));
         }

         Vector3d targetGround = TargetUtil.getTargetLocation(
            store.getExternalData().getWorld(), blockId -> blockId != 0, previewPosition.x, previewPosition.y, previewPosition.z, 0.0, -1.0, 0.0, 8.0
         );
         if (targetGround != null) {
            previewPosition = targetGround;
         }

         Vector3d relativePos = playerPosition.clone().subtract(previewPosition);
         relativePos.setY(0.0);
         Vector3f previewRotation = Vector3f.lookAt(relativePos);
         this.position = previewPosition;
         this.rotation = previewRotation;
         Holder<EntityStore> holder = store.getRegistry().newHolder();
         holder.addComponent(NetworkId.getComponentType(), new NetworkId(store.getExternalData().takeNextNetworkId()));
         holder.addComponent(EntityStore.REGISTRY.getNonSerializedComponentType(), NonSerialized.get());
         holder.addComponent(TransformComponent.getComponentType(), new TransformComponent(previewPosition, previewRotation));
         holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(this.getModel(this.scale)));
         holder.addComponent(HeadRotation.getComponentType(), new HeadRotation(previewRotation));
         this.modelPreview = store.addEntity(holder, AddReason.SPAWN);
      } else if (this.modelPreview != null && this.modelPreview.isValid()) {
         store.putComponent(this.modelPreview, ModelComponent.getComponentType(), new ModelComponent(this.getModel(1.0F)));
      }
   }

   @Nullable
   private Model getModel(float scale) {
      ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset(this.selectedModel);
      return Model.createScaledModel(modelAsset, scale);
   }

   public static class PageEventData {
      @Nonnull
      static final String KEY_MODEL = "Model";
      @Nonnull
      static final String KEY_TYPE = "Type";
      @Nonnull
      static final String KEY_SEARCH_QUERY = "@SearchQuery";
      @Nonnull
      static final String KEY_SCALE = "@Scale";
      @Nonnull
      public static final BuilderCodec<ChangeModelPage.PageEventData> CODEC = BuilderCodec.builder(
            ChangeModelPage.PageEventData.class, ChangeModelPage.PageEventData::new
         )
         .append(new KeyedCodec<>("Model", Codec.STRING), (entry, s) -> entry.model = s, entry -> entry.model)
         .add()
         .append(new KeyedCodec<>("Type", Codec.STRING), (entry, s) -> entry.type = s, entry -> entry.type)
         .add()
         .append(new KeyedCodec<>("@SearchQuery", Codec.STRING), (entry, s) -> entry.searchQuery = s, entry -> entry.searchQuery)
         .add()
         .append(new KeyedCodec<>("@Scale", Codec.FLOAT), (entry, s) -> entry.scale = s, entry -> entry.scale)
         .add()
         .build();
      private String model;
      private String type;
      private String searchQuery;
      private float scale;
   }
}
