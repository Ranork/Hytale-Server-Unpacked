package com.hypixel.hytale.builtin.triggervolumes.manager;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.builtin.triggervolumes.EntityTargetType;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerCondition;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerVolumeCodecs;
import com.hypixel.hytale.builtin.triggervolumes.shape.BoxShape;
import com.hypixel.hytale.builtin.triggervolumes.shape.TriggerVolumeShape;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.math.vector.Vector3fUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3f;

public class VolumeEntry {
   @Nonnull
   public static final BuilderCodec<VolumeEntry> CODEC = BuilderCodec.builder(VolumeEntry.class, VolumeEntry::new)
      .append(new KeyedCodec<>("Position", Vector3dUtil.CODEC), (volume, position) -> volume.position = position, volume -> volume.position)
      .add()
      .append(new KeyedCodec<>("Shape", TriggerVolumeShape.CODEC), (volume, shape) -> volume.shape = shape, volume -> volume.shape)
      .add()
      .append(
         new KeyedCodec<>("EffectAsset", Codec.STRING, false),
         (volume, effectAssetRef) -> volume.effectAssetRef = effectAssetRef,
         volume -> volume.effectAssetRef
      )
      .add()
      .append(new KeyedCodec<>("Conditions", TriggerVolumeCodecs.TOLERANT_CONDITIONS, false), (volume, conditions) -> {
         for (TriggerCondition condition : conditions) {
            if (condition != null) {
               volume.conditions.add(condition);
            }
         }
      }, volume -> volume.effectAssetRef == null && !volume.conditions.isEmpty() ? volume.conditions.toArray(TriggerCondition[]::new) : null)
      .add()
      .append(new KeyedCodec<>("Effects", TriggerVolumeCodecs.TOLERANT_EFFECTS, false), (volume, effects) -> {
         for (TriggerEffect effect : effects) {
            if (effect != null) {
               volume.effects.add(effect);
            }
         }
      }, volume -> volume.effectAssetRef == null && !volume.effects.isEmpty() ? volume.effects.toArray(TriggerEffect[]::new) : null)
      .add()
      .append(new KeyedCodec<>("RejectionEffects", TriggerVolumeCodecs.TOLERANT_EFFECTS, false), (volume, effects) -> {
         for (TriggerEffect effect : effects) {
            if (effect != null) {
               volume.rejectionEffects.add(effect);
            }
         }
      }, volume -> volume.effectAssetRef == null && !volume.rejectionEffects.isEmpty() ? volume.rejectionEffects.toArray(TriggerEffect[]::new) : null)
      .add()
      .append(
         new KeyedCodec<>("ConditionTiming", new EnumCodec<>(ConditionTiming.class), false),
         (volume, timing) -> volume.conditionTiming = timing,
         volume -> volume.conditionTiming != ConditionTiming.AFTER_VOLUME_DELAY ? volume.conditionTiming : null
      )
      .add()
      .append(
         new KeyedCodec<>("RejectionDelayMode", new EnumCodec<>(RejectionDelayMode.class, EnumCodec.EnumStyle.LEGACY), false),
         (volume, rejectionDelayMode) -> volume.rejectionDelayMode = rejectionDelayMode,
         volume -> volume.rejectionDelayMode != RejectionDelayMode.USE_VOLUME_DELAY ? volume.rejectionDelayMode : null
      )
      .add()
      .append(
         new KeyedCodec<>("TargetTypes", new ArrayCodec<>(new EnumCodec<>(EntityTargetType.class), EntityTargetType[]::new), false), (volume, targetTypes) -> {
            volume.targetTypes.clear();
            Collections.addAll(volume.targetTypes, targetTypes);
         }, volume -> volume.targetTypes.isEmpty() ? null : volume.targetTypes.toArray(EntityTargetType[]::new)
      )
      .add()
      .append(
         new KeyedCodec<>("ProjectileSource", new EnumCodec<>(ProjectileSource.class), false),
         (volume, projectileSource) -> volume.projectileSource = projectileSource != null ? projectileSource : ProjectileSource.SHOOTER,
         volume -> volume.projectileSource != ProjectileSource.SHOOTER ? volume.projectileSource : null
      )
      .add()
      .append(new KeyedCodec<>("Enabled", Codec.BOOLEAN, false), (volume, enabled) -> volume.enabled = enabled, volume -> volume.enabled)
      .add()
      .append(new KeyedCodec<>("GroupId", Codec.STRING, false), (volume, groupId) -> volume.groupId = groupId, volume -> volume.groupId)
      .add()
      .append(new KeyedCodec<>("Color", Vector3fUtil.CODEC, false), (volume, color) -> volume.color = color, volume -> volume.color)
      .add()
      .append(new KeyedCodec<>("KeepLoaded", Codec.BOOLEAN, false), (volume, keepLoaded) -> volume.keepLoaded = keepLoaded, volume -> volume.keepLoaded)
      .add()
      .append(
         new KeyedCodec<>("ActivationDelay", Codec.FLOAT, false),
         (volume, activationDelay) -> volume.activationDelay = activationDelay,
         volume -> volume.activationDelay > 0.0F ? volume.activationDelay : null
      )
      .add()
      .append(
         new KeyedCodec<>("CancelDelayedOnExit", Codec.BOOLEAN, false),
         (volume, cancelDelayedEffectsOnExit) -> volume.cancelDelayedEffectsOnExit = cancelDelayedEffectsOnExit,
         volume -> volume.cancelDelayedEffectsOnExit ? Boolean.TRUE : null
      )
      .add()
      .append(
         new KeyedCodec<>("Cooldown", Codec.FLOAT, false),
         (volume, cooldown) -> volume.cooldown = cooldown,
         volume -> volume.cooldown > 0.0F ? volume.cooldown : null
      )
      .add()
      .append(new KeyedCodec<>("CooldownMode", Codec.STRING, false), (volume, cooldownMode) -> {
         try {
            volume.cooldownMode = CooldownMode.valueOf(cooldownMode.toUpperCase());
         } catch (IllegalArgumentException var3) {
         }
      }, volume -> volume.cooldownMode != CooldownMode.PER_ENTITY ? volume.cooldownMode.name() : null)
      .add()
      .append(new KeyedCodec<>("Tags", TriggerVolumeCodecs.TAGS, false), VolumeEntry::setTags, volume -> volume.rawTags.isEmpty() ? null : volume.rawTags)
      .add()
      .append(
         new KeyedCodec<>("FromWorldGen", Codec.BOOLEAN, false),
         (volume, fromWorldGen) -> volume.fromWorldGen = fromWorldGen,
         volume -> volume.fromWorldGen ? Boolean.TRUE : null
      )
      .add()
      .build();
   @Nonnull
   private String volumeId = "";
   @Nonnull
   private String worldName = "";
   @Nonnull
   private Vector3d position = new Vector3d();
   @Nonnull
   private TriggerVolumeShape shape = new BoxShape();
   @Nonnull
   private final List<TriggerCondition> conditions;
   @Nonnull
   private final List<TriggerEffect> effects;
   @Nonnull
   private final List<TriggerEffect> rejectionEffects;
   @Nonnull
   private final Set<EntityTargetType> targetTypes;
   @Nonnull
   private ProjectileSource projectileSource = ProjectileSource.SHOOTER;
   @Nonnull
   private ConditionTiming conditionTiming = ConditionTiming.AFTER_VOLUME_DELAY;
   @Nonnull
   private RejectionDelayMode rejectionDelayMode = RejectionDelayMode.USE_VOLUME_DELAY;
   private boolean enabled = true;
   private boolean keepLoaded;
   private boolean fromWorldGen;
   private boolean cancelDelayedEffectsOnExit;
   private float activationDelay = 0.0F;
   private float cooldown = 0.0F;
   @Nonnull
   private CooldownMode cooldownMode = CooldownMode.PER_ENTITY;
   private transient boolean pendingDestroy;
   private transient long lastGlobalActivationNanos;
   private final transient Map<UUID, Long> lastEntityActivationNanos = new HashMap<>();
   @Nullable
   private String effectAssetRef;
   @Nullable
   private String groupId;
   @Nullable
   private Vector3f color;
   @Nonnull
   private Map<String, String> rawTags = Collections.emptyMap();
   @Nonnull
   private IntSet expandedTagIndexes = IntSets.EMPTY_SET;
   private final Map<UUID, Ref<EntityStore>> trackedEntities = new HashMap<>();
   private final Map<VolumeEntry.EffectEntityKey, Long> lastFireTimes = new HashMap<>();
   private final Set<UUID> activatedEntities = new HashSet<>();
   private final Set<UUID> volumeTickRejectionsFired = new HashSet<>();
   private final Set<UUID> pendingVolumeActivations = new HashSet<>();
   private final Set<VolumeEntry.GroupEntityKey> activatedGroups = new HashSet<>();
   private final Set<VolumeEntry.GroupEntityKey> groupTickRejectionsFired = new HashSet<>();
   private final Set<VolumeEntry.GroupEntityKey> pendingGroupActivations = new HashSet<>();
   private final Set<VolumeEntry.EffectEntityKey> pendingDelayedEffects = new HashSet<>();

   VolumeEntry() {
      this.conditions = new ArrayList<>();
      this.effects = new ArrayList<>();
      this.rejectionEffects = new ArrayList<>();
      this.targetTypes = EnumSet.of(EntityTargetType.PLAYER);
   }

   public VolumeEntry(
      @Nonnull String volumeId,
      @Nonnull String worldName,
      @Nonnull Vector3d position,
      @Nonnull TriggerVolumeShape shape,
      @Nonnull List<TriggerEffect> effects,
      @Nonnull Set<EntityTargetType> targetTypes,
      boolean enabled
   ) {
      this.volumeId = volumeId;
      this.worldName = worldName;
      this.position = position;
      this.shape = shape;
      this.conditions = new ArrayList<>();
      this.effects = effects;
      this.rejectionEffects = new ArrayList<>();
      this.targetTypes = targetTypes;
      this.enabled = enabled;
   }

   public void setId(@Nonnull String volumeId) {
      this.volumeId = volumeId;
   }

   public void setWorldName(@Nonnull String worldName) {
      this.worldName = worldName;
   }

   @Nonnull
   public String getId() {
      return this.volumeId;
   }

   @Nonnull
   public String getWorldName() {
      return this.worldName;
   }

   @Nonnull
   public Vector3d getPosition() {
      return this.position;
   }

   public void setPosition(@Nonnull Vector3d position) {
      this.position = position;
   }

   @Nonnull
   public TriggerVolumeShape getShape() {
      return this.shape;
   }

   public void setShape(@Nonnull TriggerVolumeShape shape) {
      this.shape = shape;
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

   @Nonnull
   public ProjectileSource getProjectileSource() {
      return this.projectileSource;
   }

   public void setProjectileSource(@Nonnull ProjectileSource projectileSource) {
      this.projectileSource = projectileSource;
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   public boolean isKeepLoaded() {
      return this.keepLoaded;
   }

   public void setKeepLoaded(boolean keepLoaded) {
      this.keepLoaded = keepLoaded;
   }

   public boolean isFromWorldGen() {
      return this.fromWorldGen;
   }

   public void setFromWorldGen(boolean fromWorldGen) {
      this.fromWorldGen = fromWorldGen;
   }

   public boolean isCancelDelayedEffectsOnExit() {
      return this.cancelDelayedEffectsOnExit;
   }

   public void setCancelDelayedEffectsOnExit(boolean cancelDelayedEffectsOnExit) {
      this.cancelDelayedEffectsOnExit = cancelDelayedEffectsOnExit;
   }

   public float getActivationDelay() {
      return this.activationDelay;
   }

   public void setActivationDelay(float activationDelay) {
      this.activationDelay = activationDelay;
   }

   public float getCooldown() {
      return this.cooldown;
   }

   public void setCooldown(float cooldown) {
      this.cooldown = cooldown;
   }

   @Nonnull
   public CooldownMode getCooldownMode() {
      return this.cooldownMode;
   }

   public void setCooldownMode(@Nonnull CooldownMode cooldownMode) {
      this.cooldownMode = cooldownMode;
   }

   public boolean isOnCooldown(@Nonnull UUID entityUuid, long nowNanos) {
      if (this.cooldown <= 0.0F) {
         return false;
      } else {
         long cooldownNanos = (long)(this.cooldown * 1.0E9F);
         if (this.cooldownMode != CooldownMode.TOTAL) {
            Long last = this.lastEntityActivationNanos.get(entityUuid);
            if (last == null) {
               return false;
            } else if (nowNanos - last >= cooldownNanos) {
               this.lastEntityActivationNanos.remove(entityUuid);
               return false;
            } else {
               return true;
            }
         } else {
            return this.lastGlobalActivationNanos != 0L && nowNanos - this.lastGlobalActivationNanos < cooldownNanos;
         }
      }
   }

   public void recordActivation(@Nonnull UUID entityUuid, long nowNanos) {
      if (!(this.cooldown <= 0.0F)) {
         if (this.cooldownMode == CooldownMode.TOTAL) {
            this.lastGlobalActivationNanos = nowNanos;
         } else {
            this.lastEntityActivationNanos.put(entityUuid, nowNanos);
         }
      }
   }

   public boolean isPendingDestroy() {
      return this.pendingDestroy;
   }

   public void markPendingDestroy() {
      this.pendingDestroy = true;
   }

   @Nullable
   public String getEffectAssetRef() {
      return this.effectAssetRef;
   }

   public void setEffectAssetRef(@Nullable String effectAssetRef) {
      this.effectAssetRef = effectAssetRef;
   }

   @Nullable
   public String getGroupId() {
      return this.groupId;
   }

   public void setGroupId(@Nullable String groupId) {
      this.groupId = groupId;
   }

   @Nullable
   public Vector3f getColor() {
      return this.color;
   }

   public void setColor(@Nullable Vector3f color) {
      this.color = color;
   }

   @Nonnull
   public Map<String, String> getRawTags() {
      return this.rawTags;
   }

   public void setTags(@Nonnull Map<String, String> tags) {
      this.rawTags = normalizeTags(tags);
      this.expandTags();
   }

   @Nonnull
   public IntSet getExpandedTagIndexes() {
      return this.expandedTagIndexes;
   }

   public void setExpandedTagIndexes(@Nonnull IntSet expandedTagIndexes) {
      this.expandedTagIndexes = expandedTagIndexes;
   }

   public boolean hasTag(int tagIndex) {
      return this.expandedTagIndexes.contains(tagIndex);
   }

   void expandTags() {
      if (this.rawTags.isEmpty()) {
         this.expandedTagIndexes = IntSets.EMPTY_SET;
      } else {
         IntOpenHashSet indexes = new IntOpenHashSet();

         for (Entry<String, String> entry : this.rawTags.entrySet()) {
            String tag = entry.getKey();
            indexes.add(AssetRegistry.getOrCreateTagIndex(tag));
            String value = entry.getValue();
            indexes.add(AssetRegistry.getOrCreateTagIndex(value));
            indexes.add(AssetRegistry.getOrCreateTagIndex(tag + "=" + value));
         }

         this.expandedTagIndexes = IntSets.unmodifiable(indexes);
      }
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

   @Nonnull
   public Map<UUID, Ref<EntityStore>> getTrackedEntities() {
      return this.trackedEntities;
   }

   @Nonnull
   public Map<VolumeEntry.EffectEntityKey, Long> getLastFireTimes() {
      return this.lastFireTimes;
   }

   public boolean isVolumeActivated(@Nonnull UUID entityUuid) {
      return this.activatedEntities.contains(entityUuid);
   }

   public void markVolumeActivated(@Nonnull UUID entityUuid) {
      this.activatedEntities.add(entityUuid);
   }

   public boolean markVolumeActivationPending(@Nonnull UUID entityUuid) {
      return this.pendingVolumeActivations.add(entityUuid);
   }

   public void clearVolumeActivationPending(@Nonnull UUID entityUuid) {
      this.pendingVolumeActivations.remove(entityUuid);
   }

   public boolean markVolumeTickRejectionFired(@Nonnull UUID entityUuid) {
      return this.volumeTickRejectionsFired.add(entityUuid);
   }

   public boolean isGroupActivated(@Nonnull String groupId, @Nonnull UUID entityUuid) {
      return this.activatedGroups.contains(new VolumeEntry.GroupEntityKey(groupId, entityUuid));
   }

   public void markGroupActivated(@Nonnull String groupId, @Nonnull UUID entityUuid) {
      this.activatedGroups.add(new VolumeEntry.GroupEntityKey(groupId, entityUuid));
   }

   public boolean markGroupActivationPending(@Nonnull String groupId, @Nonnull UUID entityUuid) {
      return this.pendingGroupActivations.add(new VolumeEntry.GroupEntityKey(groupId, entityUuid));
   }

   public void clearGroupActivationPending(@Nonnull String groupId, @Nonnull UUID entityUuid) {
      this.pendingGroupActivations.remove(new VolumeEntry.GroupEntityKey(groupId, entityUuid));
   }

   public void clearGroupActivationState(@Nonnull String groupId) {
      this.activatedGroups.removeIf(key -> key.groupId().equals(groupId));
      this.groupTickRejectionsFired.removeIf(key -> key.groupId().equals(groupId));
      this.pendingGroupActivations.removeIf(key -> key.groupId().equals(groupId));
   }

   public boolean markGroupTickRejectionFired(@Nonnull String groupId, @Nonnull UUID entityUuid) {
      return this.groupTickRejectionsFired.add(new VolumeEntry.GroupEntityKey(groupId, entityUuid));
   }

   public boolean markDelayedEffectPending(@Nonnull VolumeEntry.EffectEntityKey key) {
      return this.pendingDelayedEffects.add(key);
   }

   public void clearDelayedEffectPending(@Nonnull VolumeEntry.EffectEntityKey key) {
      this.pendingDelayedEffects.remove(key);
   }

   public void clearEntityRuntimeState(@Nonnull UUID entityUuid) {
      this.activatedEntities.remove(entityUuid);
      this.volumeTickRejectionsFired.remove(entityUuid);
      this.pendingVolumeActivations.remove(entityUuid);
      this.activatedGroups.removeIf(key -> key.entityId().equals(entityUuid));
      this.groupTickRejectionsFired.removeIf(key -> key.entityId().equals(entityUuid));
      this.pendingGroupActivations.removeIf(key -> key.entityId().equals(entityUuid));
      this.pendingDelayedEffects.removeIf(key -> key.entityId().equals(entityUuid));
      this.lastFireTimes.entrySet().removeIf(entry -> entry.getKey().entityId().equals(entityUuid));
   }

   public static enum EffectBucket {
      VOLUME,
      VOLUME_REJECTION,
      GROUP,
      GROUP_REJECTION;
   }

   public record EffectEntityKey(@Nonnull VolumeEntry.EffectBucket bucket, int effectIndex, @Nonnull UUID entityId) {
   }

   public record GroupEntityKey(@Nonnull String groupId, @Nonnull UUID entityId) {
   }
}
