package com.hypixel.hytale.builtin.triggervolumes.effect.builtin.conditions;

import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerCondition;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEventType;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.TaggedVolumeEffectUtil;
import com.hypixel.hytale.builtin.triggervolumes.manager.TriggerVolumeManager;
import com.hypixel.hytale.builtin.triggervolumes.manager.VolumeEntry;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;

public class TagCondition extends TriggerCondition {
   @Nonnull
   public static final BuilderCodec<TagCondition> CODEC = BuilderCodec.builder(TagCondition.class, TagCondition::new, BASE_CODEC)
      .append(
         new KeyedCodec<>("Source", new EnumCodec<>(TagCondition.Source.class), false),
         (condition, source) -> condition.source = source,
         condition -> condition.source
      )
      .add()
      .append(new KeyedCodec<>("TagKey", Codec.STRING), (condition, tagKey) -> condition.tagKey = tagKey, condition -> condition.tagKey)
      .add()
      .append(new KeyedCodec<>("TagValue", Codec.STRING, false), (condition, tagValue) -> condition.tagValue = tagValue, condition -> condition.tagValue)
      .add()
      .append(new KeyedCodec<>("MatchKey", Codec.STRING, false), (condition, matchKey) -> condition.matchKey = matchKey, condition -> condition.matchKey)
      .add()
      .append(
         new KeyedCodec<>("MatchValue", Codec.STRING, false), (condition, matchValue) -> condition.matchValue = matchValue, condition -> condition.matchValue
      )
      .add()
      .append(new KeyedCodec<>("Radius", Codec.DOUBLE, false), (condition, radius) -> condition.radius = radius, condition -> condition.radius)
      .add()
      .append(
         new KeyedCodec<>("Center", new EnumCodec<>(TaggedVolumeEffectUtil.Center.class), false),
         (condition, center) -> condition.center = center,
         condition -> condition.center
      )
      .add()
      .append(
         new KeyedCodec<>("MinimumCount", Codec.INTEGER, false),
         (condition, minimumCount) -> condition.minimumCount = minimumCount,
         condition -> condition.minimumCount > 0 ? condition.minimumCount : null
      )
      .add()
      .build();
   @Nonnull
   private TagCondition.Source source = TagCondition.Source.SELF;
   @Nonnull
   private String tagKey = "";
   @Nullable
   private String tagValue;
   @Nullable
   private String matchKey;
   @Nullable
   private String matchValue;
   private double radius = 50.0;
   @Nonnull
   private TaggedVolumeEffectUtil.Center center = TaggedVolumeEffectUtil.Center.VOLUME;
   private int minimumCount;

   @Nonnull
   public static TagCondition forEvent(@Nonnull TriggerEventType eventType, @Nonnull String tagKey, @Nullable String tagValue) {
      TagCondition condition = new TagCondition();
      condition.setEventType(eventType);
      condition.source = TagCondition.Source.EVENT;
      condition.tagKey = tagKey;
      condition.tagValue = tagValue;
      return condition;
   }

   @Nonnull
   public static TagCondition forSelf(@Nonnull TriggerEventType eventType, @Nonnull String tagKey, @Nullable String tagValue) {
      TagCondition condition = new TagCondition();
      condition.setEventType(eventType);
      condition.source = TagCondition.Source.SELF;
      condition.tagKey = tagKey;
      condition.tagValue = tagValue;
      return condition;
   }

   @Nonnull
   public static TagCondition forGroup(@Nonnull TriggerEventType eventType, @Nonnull String tagKey, @Nullable String tagValue, int minimumCount) {
      TagCondition condition = new TagCondition();
      condition.setEventType(eventType);
      condition.source = TagCondition.Source.GROUP;
      condition.tagKey = tagKey;
      condition.tagValue = tagValue;
      condition.minimumCount = minimumCount;
      return condition;
   }

   @Override
   public boolean test(@Nonnull TriggerContext context) {
      if (this.tagKey.isBlank()) {
         return false;
      } else {
         return switch (this.source) {
            case EVENT -> this.testEvent(context);
            case SELF -> VolumeTagMatcher.hasTag(context.getVolume(), this.tagKey, this.tagValue);
            case GROUP -> this.testVolumes(context.getSpatialVolumes());
            case RADIUS -> this.testVolumes(this.collectRadiusTargets(context));
         };
      }
   }

   private boolean testEvent(@Nonnull TriggerContext context) {
      if (!this.tagKey.equals(context.getTagKey())) {
         return false;
      } else {
         return this.tagValue != null && !this.tagValue.isBlank() ? this.tagValue.equals(context.getTagValue()) : true;
      }
   }

   private boolean testVolumes(@Nonnull Collection<VolumeEntry> volumes) {
      if (volumes.isEmpty()) {
         return false;
      } else {
         int matchingCount = 0;

         for (VolumeEntry volume : volumes) {
            if (VolumeTagMatcher.hasTag(volume, this.tagKey, this.tagValue)) {
               matchingCount++;
            } else if (this.minimumCount <= 0) {
               return false;
            }
         }

         return this.minimumCount > 0 ? matchingCount >= this.minimumCount : matchingCount == volumes.size();
      }
   }

   @Nonnull
   private List<VolumeEntry> collectRadiusTargets(@Nonnull TriggerContext context) {
      Store<EntityStore> store = context.getStore();
      if (store == null) {
         return List.of();
      } else {
         String tagFilter = TaggedVolumeEffectUtil.composeTagFilter(this.matchKey, this.matchValue);
         if (tagFilter != null) {
            return TaggedVolumeEffectUtil.collectTargets(context, tagFilter, this.radius, this.center);
         } else {
            TriggerVolumeManager manager = store.getResource(TriggerVolumesPlugin.get().getManagerResourceType());
            if (manager == null) {
               return List.of();
            } else if (this.radius <= 0.0) {
               return List.copyOf(manager.getVolumes());
            } else {
               Vector3d origin = this.resolveCenter(context, store);
               double radiusSquared = this.radius * this.radius;
               ArrayList<VolumeEntry> targets = new ArrayList<>();

               for (VolumeEntry volume : manager.getVolumes()) {
                  if (volume.getPosition().distanceSquared(origin) <= radiusSquared) {
                     targets.add(volume);
                  }
               }

               return targets;
            }
         }
      }
   }

   @Nonnull
   private Vector3d resolveCenter(@Nonnull TriggerContext context, @Nonnull Store<EntityStore> store) {
      if (this.center == TaggedVolumeEffectUtil.Center.ENTITY && context.getEntityRef() != null) {
         TransformComponent transform = store.getComponent(context.getEntityRef(), TransformComponent.getComponentType());
         if (transform != null) {
            return new Vector3d(transform.getPosition());
         }
      }

      return new Vector3d(context.getVolume().getPosition());
   }

   public static enum Source {
      EVENT,
      SELF,
      GROUP,
      RADIUS;
   }
}
