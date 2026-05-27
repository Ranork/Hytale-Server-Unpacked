package com.hypixel.hytale.builtin.triggervolumes.manager;

import com.hypixel.hytale.builtin.triggervolumes.EntityTargetType;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerCondition;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerVolumeCodecs;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import org.joml.Vector3d;

public class GroupEntry {
   @Nonnull
   public static final BuilderCodec<GroupEntry> CODEC = BuilderCodec.builder(GroupEntry.class, GroupEntry::new)
      .append(new KeyedCodec<>("Origin", Vector3dUtil.CODEC), (group, origin) -> group.origin = origin, group -> group.origin)
      .add()
      .append(new KeyedCodec<>("Conditions", TriggerVolumeCodecs.TOLERANT_CONDITIONS, false), (group, conditions) -> {
         for (TriggerCondition condition : conditions) {
            if (condition != null) {
               group.conditions.add(condition);
            }
         }
      }, group -> group.conditions.isEmpty() ? null : group.conditions.toArray(TriggerCondition[]::new))
      .add()
      .append(new KeyedCodec<>("Effects", TriggerVolumeCodecs.TOLERANT_EFFECTS, false), (group, effects) -> {
         for (TriggerEffect effect : effects) {
            if (effect != null) {
               group.effects.add(effect);
            }
         }
      }, group -> group.effects.isEmpty() ? null : group.effects.toArray(TriggerEffect[]::new))
      .add()
      .append(new KeyedCodec<>("RejectionEffects", TriggerVolumeCodecs.TOLERANT_EFFECTS, false), (group, effects) -> {
         for (TriggerEffect effect : effects) {
            if (effect != null) {
               group.rejectionEffects.add(effect);
            }
         }
      }, group -> group.rejectionEffects.isEmpty() ? null : group.rejectionEffects.toArray(TriggerEffect[]::new))
      .add()
      .append(
         new KeyedCodec<>("ConditionTiming", new EnumCodec<>(ConditionTiming.class, EnumCodec.EnumStyle.LEGACY), false),
         (group, timing) -> group.conditionTiming = timing,
         group -> group.conditionTiming != ConditionTiming.AFTER_VOLUME_DELAY ? group.conditionTiming : null
      )
      .add()
      .append(
         new KeyedCodec<>("RejectionDelayMode", new EnumCodec<>(RejectionDelayMode.class, EnumCodec.EnumStyle.LEGACY), false),
         (group, rejectionDelayMode) -> group.rejectionDelayMode = rejectionDelayMode,
         group -> group.rejectionDelayMode != RejectionDelayMode.USE_VOLUME_DELAY ? group.rejectionDelayMode : null
      )
      .add()
      .append(
         new KeyedCodec<>("TargetTypes", new ArrayCodec<>(new EnumCodec<>(EntityTargetType.class), EntityTargetType[]::new), false), (group, targetTypes) -> {
            group.targetTypes.clear();
            Collections.addAll(group.targetTypes, targetTypes);
         }, group -> group.targetTypes.isEmpty() ? null : group.targetTypes.toArray(EntityTargetType[]::new)
      )
      .add()
      .append(new KeyedCodec<>("Enabled", Codec.BOOLEAN, false), (group, enabled) -> group.enabled = enabled, group -> group.enabled)
      .add()
      .append(new KeyedCodec<>("Color", Codec.INTEGER, false), (group, color) -> group.color = color, group -> group.color)
      .add()
      .append(new KeyedCodec<>("Tags", TriggerVolumeCodecs.TAGS, false), GroupEntry::setTags, group -> group.rawTags.isEmpty() ? null : group.rawTags)
      .add()
      .build();
   @Nonnull
   private String groupId = "";
   @Nonnull
   private String worldName = "";
   @Nonnull
   private Vector3d origin = new Vector3d();
   @Nonnull
   private final List<TriggerCondition> conditions;
   @Nonnull
   private final List<TriggerEffect> effects;
   @Nonnull
   private final List<TriggerEffect> rejectionEffects;
   @Nonnull
   private final Set<EntityTargetType> targetTypes;
   @Nonnull
   private ConditionTiming conditionTiming = ConditionTiming.AFTER_VOLUME_DELAY;
   @Nonnull
   private RejectionDelayMode rejectionDelayMode = RejectionDelayMode.USE_VOLUME_DELAY;
   private boolean enabled = true;
   private int color;
   @Nonnull
   private Map<String, String> rawTags = Collections.emptyMap();
   @Nonnull
   private final Set<String> memberVolumeIds = new LinkedHashSet<>();

   GroupEntry() {
      this.conditions = new ArrayList<>();
      this.effects = new ArrayList<>();
      this.rejectionEffects = new ArrayList<>();
      this.targetTypes = EnumSet.of(EntityTargetType.PLAYER);
   }

   public GroupEntry(
      @Nonnull String groupId,
      @Nonnull String worldName,
      @Nonnull Vector3d origin,
      @Nonnull List<TriggerEffect> effects,
      @Nonnull Set<EntityTargetType> targetTypes,
      boolean enabled,
      int color
   ) {
      this.groupId = groupId;
      this.worldName = worldName;
      this.origin = origin;
      this.conditions = new ArrayList<>();
      this.effects = effects;
      this.rejectionEffects = new ArrayList<>();
      this.targetTypes = targetTypes;
      this.enabled = enabled;
      this.color = color;
   }

   public void setId(@Nonnull String groupId) {
      this.groupId = groupId;
   }

   public void setWorldName(@Nonnull String worldName) {
      this.worldName = worldName;
   }

   @Nonnull
   public String getId() {
      return this.groupId;
   }

   @Nonnull
   public String getWorldName() {
      return this.worldName;
   }

   @Nonnull
   public Vector3d getOrigin() {
      return this.origin;
   }

   public void setOrigin(@Nonnull Vector3d origin) {
      this.origin = origin;
   }

   @Nonnull
   public List<TriggerEffect> getEffects() {
      return this.effects;
   }

   @Nonnull
   public List<TriggerCondition> getConditions() {
      return this.conditions;
   }

   @Nonnull
   public List<TriggerEffect> getRejectionEffects() {
      return this.rejectionEffects;
   }

   @Nonnull
   public ConditionTiming getConditionTiming() {
      return this.conditionTiming;
   }

   public void setConditionTiming(@Nonnull ConditionTiming conditionTiming) {
      this.conditionTiming = conditionTiming;
   }

   @Nonnull
   public RejectionDelayMode getRejectionDelayMode() {
      return this.rejectionDelayMode;
   }

   public void setRejectionDelayMode(@Nonnull RejectionDelayMode rejectionDelayMode) {
      this.rejectionDelayMode = rejectionDelayMode;
   }

   @Nonnull
   public Set<EntityTargetType> getTargetTypes() {
      return this.targetTypes;
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   public int getColor() {
      return this.color;
   }

   public void setColor(int color) {
      this.color = color;
   }

   @Nonnull
   public Set<String> getMemberVolumeIds() {
      return this.memberVolumeIds;
   }

   public void addMember(@Nonnull String volumeId) {
      this.memberVolumeIds.add(volumeId);
   }

   public void removeMember(@Nonnull String volumeId) {
      this.memberVolumeIds.remove(volumeId);
   }

   @Nonnull
   public Map<String, String> getRawTags() {
      return this.rawTags;
   }

   public void setTags(@Nonnull Map<String, String> tags) {
      this.rawTags = normalizeTags(tags);
   }

   @Nonnull
   private static Map<String, String> normalizeTags(@Nonnull Map<String, String> tags) {
      if (tags.isEmpty()) {
         return Collections.emptyMap();
      } else {
         LinkedHashMap<String, String> normalizedTags = new LinkedHashMap<>();

         for (Entry<String, String> entry : tags.entrySet()) {
            String key = entry.getKey().trim();
            if (!key.isEmpty()) {
               normalizedTags.put(key, TriggerVolumeCodecs.TaggedValue.normalize(entry.getValue()));
            }
         }

         return (Map<String, String>)(normalizedTags.isEmpty() ? Collections.emptyMap() : normalizedTags);
      }
   }
}
