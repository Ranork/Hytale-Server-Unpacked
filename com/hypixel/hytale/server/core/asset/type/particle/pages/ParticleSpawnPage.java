package com.hypixel.hytale.server.core.asset.type.particle.pages;

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
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.Rangef;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.asset.type.particle.config.ParticleSpawnerGroup;
import com.hypixel.hytale.server.core.asset.type.particle.config.ParticleSystem;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ParticleSpawnPage extends InteractiveCustomUIPage<ParticleSpawnPage.ParticleSpawnPageEventData> {
   private static final String COMMON_TEXT_BUTTON_DOCUMENT = "Common/TextButton.ui";
   private static final Value<String> BUTTON_LABEL_STYLE = Value.ref("Common/TextButton.ui", "LabelStyle");
   private static final Value<String> BUTTON_LABEL_STYLE_SELECTED = Value.ref("Common/TextButton.ui", "SelectedLabelStyle");
   @Nonnull
   private String searchQuery = "";
   private List<String> particleSystemIds;
   @Nullable
   private String selectedParticleSystemId;
   @Nullable
   private Ref<EntityStore> particleSystemPreview;
   private Vector3d position;
   private Vector3f rotation;

   public ParticleSpawnPage(@Nonnull PlayerRef playerRef) {
      super(playerRef, CustomPageLifetime.CanDismiss, ParticleSpawnPage.ParticleSpawnPageEventData.CODEC);
   }

   @Override
   public void build(
      @Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store
   ) {
      commandBuilder.append("Pages/ParticleSpawnPage.ui");
      eventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#SearchInput", EventData.of("@SearchQuery", "#SearchInput.Value"), false);
      eventBuilder.addEventBinding(
         CustomUIEventBindingType.ValueChanged,
         "#RotationOffset",
         new EventData().append("Action", "UpdateRotationOffset").append("@RotationOffset", "#RotationOffset.Value"),
         false
      );
      eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#Spawn", new EventData().append("Action", "Spawn"), false);
      this.buildParticleList(ref, commandBuilder, eventBuilder, store);
   }

   public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull ParticleSpawnPage.ParticleSpawnPageEventData data) {
      if (data.searchQuery != null) {
         this.searchQuery = data.searchQuery.trim().toLowerCase();
         UICommandBuilder commandBuilder = new UICommandBuilder();
         UIEventBuilder eventBuilder = new UIEventBuilder();
         this.buildParticleList(ref, commandBuilder, eventBuilder, store);
         this.sendUpdate(commandBuilder, eventBuilder, false);
      } else {
         String var8 = data.action;
         switch (var8) {
            case "Select":
               if (data.particleSystemId != null) {
                  UICommandBuilder commandBuilder = new UICommandBuilder();
                  this.selectParticleSystem(ref, store, data.particleSystemId, commandBuilder);
                  this.sendUpdate(commandBuilder, null, false);
               }
               break;
            case "UpdateRotationOffset":
               if (this.particleSystemPreview.isValid()) {
                  TransformComponent transform = store.getComponent(this.particleSystemPreview, TransformComponent.getComponentType());
                  transform.getRotation().setYaw(this.rotation.getYaw() + (float)Math.toRadians(data.rotationOffset));
                  HeadRotation headRotation = store.getComponent(this.particleSystemPreview, HeadRotation.getComponentType());
                  if (headRotation != null) {
                     headRotation.getRotation().setYaw(this.rotation.getYaw() + (float)Math.toRadians(data.rotationOffset));
                  }
               }
               break;
            case "Spawn":
               if (this.selectedParticleSystemId != null) {
                  if (this.particleSystemPreview.isValid()) {
                     store.removeEntity(this.particleSystemPreview, RemoveReason.REMOVE);
                  }

                  SpatialResource<Ref<EntityStore>, EntityStore> playerSpatialResource = store.getResource(EntityModule.get().getPlayerSpatialResourceType());
                  ObjectList<Ref<EntityStore>> results = SpatialResource.getThreadLocalReferenceList();
                  playerSpatialResource.getSpatialStructure().collect(this.position, 75.0, results);
                  ParticleUtil.spawnParticleEffect(this.selectedParticleSystemId, this.position, this.rotation, results, store);
               }
         }
      }
   }

   @Override
   public void onDismiss(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
      if (this.particleSystemPreview.isValid()) {
         store.removeEntity(this.particleSystemPreview, RemoveReason.REMOVE);
      }
   }

   private void buildParticleList(
      @Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store
   ) {
      commandBuilder.clear("#ParticleSystemList");
      Set<String> roleTemplateNames = ParticleSystem.getAssetMap().getAssetMap().keySet();
      if (!this.searchQuery.isEmpty()) {
         Object2IntMap<String> map = new Object2IntOpenHashMap(roleTemplateNames.size());

         for (String value : roleTemplateNames) {
            int fuzzyDistance = StringCompareUtil.getFuzzyDistance(value, this.searchQuery, Locale.ENGLISH);
            if (fuzzyDistance > 0) {
               map.put(value, fuzzyDistance);
            }
         }

         this.particleSystemIds = map.keySet()
            .stream()
            .sorted()
            .sorted(Comparator.comparingInt(map::getInt).reversed())
            .limit(20L)
            .collect(Collectors.toList());
      } else {
         this.particleSystemIds = roleTemplateNames.stream().sorted(String::compareTo).collect(Collectors.toList());
      }

      int i = 0;

      for (int bound = this.particleSystemIds.size(); i < bound; i++) {
         String id = this.particleSystemIds.get(i);
         String selector = "#ParticleSystemList[" + i + "]";
         commandBuilder.append("#ParticleSystemList", "Common/TextButton.ui");
         commandBuilder.set(selector + " #Button.Text", id);
         eventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating, selector + " #Button", new EventData().append("Action", "Select").append("ParticleSystemId", id), false
         );
      }

      if (!this.particleSystemIds.isEmpty()) {
         if (!this.particleSystemIds.contains(this.selectedParticleSystemId)) {
            this.selectParticleSystem(ref, store, this.particleSystemIds.getFirst(), commandBuilder);
         } else if (this.selectedParticleSystemId != null) {
            this.selectParticleSystem(ref, store, this.selectedParticleSystemId, commandBuilder);
         }
      }
   }

   private void selectParticleSystem(
      @Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull String particleSystemId, @Nonnull UICommandBuilder commandBuilder
   ) {
      if (this.selectedParticleSystemId != null && this.particleSystemIds.contains(this.selectedParticleSystemId)) {
         commandBuilder.set("#ParticleSystemList[" + this.particleSystemIds.indexOf(this.selectedParticleSystemId) + "] #Button.Style", BUTTON_LABEL_STYLE);
      }

      commandBuilder.set("#ParticleSystemList[" + this.particleSystemIds.indexOf(particleSystemId) + "] #Button.Style", BUTTON_LABEL_STYLE_SELECTED);
      commandBuilder.set("#ParticleSystemName.Text", particleSystemId);
      ParticleSystem particleSystem = ParticleSystem.getAssetMap().getAsset(particleSystemId);
      float lifeSpan = particleSystem.getLifeSpan();
      commandBuilder.set("#SystemLifespan.Text", lifeSpan <= 0.0F ? "Infinite" : String.valueOf(lifeSpan));
      float lifeSpanMin = Float.MAX_VALUE;
      float lifeSpanMax = -1.0F;

      for (ParticleSpawnerGroup spawner : particleSystem.getSpawners()) {
         Rangef groupLifespawn = spawner.getLifeSpan();
         if (groupLifespawn != null) {
            lifeSpanMin = Math.min(lifeSpanMin, groupLifespawn.min);
            lifeSpanMax = Math.max(lifeSpanMax, groupLifespawn.max);
         } else {
            lifeSpanMin = -1.0F;
            lifeSpanMax = Float.MAX_VALUE;
         }
      }

      commandBuilder.set(
         "#ParticleGroupLifespan.Text",
         String.format(
            "%s - %s",
            lifeSpanMin < 0.0F ? "Unset" : String.valueOf(lifeSpanMin),
            lifeSpanMax >= Float.MAX_VALUE ? "Infinite" : (lifeSpanMax < 0.0F ? "Unset" : String.valueOf(lifeSpanMax))
         )
      );
      this.selectedParticleSystemId = particleSystemId;
      if (this.particleSystemPreview != null && this.particleSystemPreview.isValid()) {
         if (this.particleSystemPreview != null && this.particleSystemPreview.isValid()) {
         }
      } else {
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

         previewPosition.add(0.0, particleSystem.getBoundingRadius(), 0.0);
         Vector3d relativePos = playerPosition.clone().subtract(previewPosition);
         relativePos.setY(0.0);
         Vector3f previewRotation = Vector3f.lookAt(relativePos);
         this.position = previewPosition;
         this.rotation = previewRotation;
         Holder<EntityStore> holder = store.getRegistry().newHolder();
         holder.addComponent(EntityStore.REGISTRY.getNonSerializedComponentType(), NonSerialized.get());
         holder.addComponent(NetworkId.getComponentType(), new NetworkId(store.getExternalData().takeNextNetworkId()));
         holder.addComponent(TransformComponent.getComponentType(), new TransformComponent(previewPosition, previewRotation));
         this.particleSystemPreview = store.addEntity(holder, AddReason.SPAWN);
      }
   }

   public static class ParticleSpawnPageEventData {
      static final String KEY_ACTION = "Action";
      static final String KEY_PARTICLE_SYSTEM_ID = "ParticleSystemId";
      static final String KEY_SEARCH_QUERY = "@SearchQuery";
      static final String KEY_ROTATION_OFFSET = "@RotationOffset";
      public static final BuilderCodec<ParticleSpawnPage.ParticleSpawnPageEventData> CODEC = BuilderCodec.builder(
            ParticleSpawnPage.ParticleSpawnPageEventData.class, ParticleSpawnPage.ParticleSpawnPageEventData::new
         )
         .append(new KeyedCodec<>("Action", Codec.STRING), (entry, s) -> entry.action = s, entry -> entry.action)
         .add()
         .append(new KeyedCodec<>("ParticleSystemId", Codec.STRING), (entry, s) -> entry.particleSystemId = s, entry -> entry.particleSystemId)
         .add()
         .append(new KeyedCodec<>("@SearchQuery", Codec.STRING), (entry, s) -> entry.searchQuery = s, entry -> entry.searchQuery)
         .add()
         .append(new KeyedCodec<>("@RotationOffset", Codec.FLOAT), (entry, s) -> entry.rotationOffset = s, entry -> entry.rotationOffset)
         .add()
         .build();
      private String particleSystemId;
      private String action;
      private String searchQuery;
      private float rotationOffset;
   }
}
