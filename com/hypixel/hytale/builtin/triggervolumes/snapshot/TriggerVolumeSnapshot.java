package com.hypixel.hytale.builtin.triggervolumes.snapshot;

import com.hypixel.hytale.builtin.buildertools.snapshot.SelectionSnapshot;
import com.hypixel.hytale.builtin.triggervolumes.EntityTargetType;
import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerCondition;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.builtin.triggervolumes.manager.ConditionTiming;
import com.hypixel.hytale.builtin.triggervolumes.manager.CooldownMode;
import com.hypixel.hytale.builtin.triggervolumes.manager.ProjectileSource;
import com.hypixel.hytale.builtin.triggervolumes.manager.RejectionDelayMode;
import com.hypixel.hytale.builtin.triggervolumes.manager.TriggerVolumeManager;
import com.hypixel.hytale.builtin.triggervolumes.manager.VolumeEntry;
import com.hypixel.hytale.builtin.triggervolumes.shape.TriggerVolumeShape;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3f;

public class TriggerVolumeSnapshot implements SelectionSnapshot<TriggerVolumeSnapshot> {
   private final TriggerVolumeSnapshot.SnapshotType type;
   private final String volumeId;
   private final String worldName;
   @Nullable
   private final Vector3d position;
   @Nullable
   private final TriggerVolumeShape shape;
   @Nullable
   private final List<TriggerCondition> conditions;
   @Nullable
   private final List<TriggerEffect> effects;
   @Nullable
   private final List<TriggerEffect> rejectionEffects;
   @Nullable
   private final ConditionTiming conditionTiming;
   @Nullable
   private final RejectionDelayMode rejectionDelayMode;
   @Nullable
   private final Set<EntityTargetType> targetTypes;
   @Nullable
   private final ProjectileSource projectileSource;
   private final boolean enabled;
   private final boolean keepLoaded;
   private final boolean fromWorldGen;
   private final boolean cancelDelayedEffectsOnExit;
   private final float activationDelay;
   private final float cooldown;
   @Nullable
   private final CooldownMode cooldownMode;
   @Nullable
   private final String effectAssetRef;
   @Nullable
   private final String groupId;
   @Nullable
   private final Vector3f color;
   @Nullable
   private final Map<String, String> rawTags;

   private TriggerVolumeSnapshot(
      @Nonnull TriggerVolumeSnapshot.SnapshotType type,
      @Nonnull String volumeId,
      @Nonnull String worldName,
      @Nullable Vector3d position,
      @Nullable TriggerVolumeShape shape,
      @Nullable List<TriggerCondition> conditions,
      @Nullable List<TriggerEffect> effects,
      @Nullable List<TriggerEffect> rejectionEffects,
      @Nullable ConditionTiming conditionTiming,
      @Nullable RejectionDelayMode rejectionDelayMode,
      @Nullable Set<EntityTargetType> targetTypes,
      @Nullable ProjectileSource projectileSource,
      boolean enabled,
      boolean keepLoaded,
      boolean fromWorldGen,
      boolean cancelDelayedEffectsOnExit,
      float activationDelay,
      float cooldown,
      @Nullable CooldownMode cooldownMode,
      @Nullable String effectAssetRef,
      @Nullable String groupId,
      @Nullable Vector3f color,
      @Nullable Map<String, String> rawTags
   ) {
      this.type = type;
      this.volumeId = volumeId;
      this.worldName = worldName;
      this.position = position;
      this.shape = shape;
      this.conditions = conditions;
      this.effects = effects;
      this.rejectionEffects = rejectionEffects;
      this.conditionTiming = conditionTiming;
      this.rejectionDelayMode = rejectionDelayMode;
      this.targetTypes = targetTypes;
      this.projectileSource = projectileSource;
      this.enabled = enabled;
      this.keepLoaded = keepLoaded;
      this.fromWorldGen = fromWorldGen;
      this.cancelDelayedEffectsOnExit = cancelDelayedEffectsOnExit;
      this.activationDelay = activationDelay;
      this.cooldown = cooldown;
      this.cooldownMode = cooldownMode;
      this.effectAssetRef = effectAssetRef;
      this.groupId = groupId;
      this.color = color;
      this.rawTags = rawTags;
   }

   public static TriggerVolumeSnapshot ofCreate(@Nonnull VolumeEntry entry) {
      return new TriggerVolumeSnapshot(
         TriggerVolumeSnapshot.SnapshotType.CREATE,
         entry.getId(),
         entry.getWorldName(),
         null,
         null,
         null,
         null,
         null,
         null,
         null,
         null,
         ProjectileSource.SHOOTER,
         true,
         false,
         false,
         true,
         0.0F,
         0.0F,
         CooldownMode.PER_ENTITY,
         null,
         null,
         null,
         null
      );
   }

   public static TriggerVolumeSnapshot ofDelete(@Nonnull VolumeEntry entry) {
      return captureState(TriggerVolumeSnapshot.SnapshotType.DELETE, entry);
   }

   public static TriggerVolumeSnapshot ofMutate(@Nonnull VolumeEntry entry) {
      return captureState(TriggerVolumeSnapshot.SnapshotType.MUTATE, entry);
   }

   private static TriggerVolumeSnapshot captureState(@Nonnull TriggerVolumeSnapshot.SnapshotType type, @Nonnull VolumeEntry entry) {
      return new TriggerVolumeSnapshot(
         type,
         entry.getId(),
         entry.getWorldName(),
         new Vector3d(entry.getPosition()),
         entry.getShape().copy(),
         TriggerCondition.deepCopyList(entry.getConditions()),
         TriggerEffect.deepCopyList(entry.getEffects()),
         TriggerEffect.deepCopyList(entry.getRejectionEffects()),
         entry.getConditionTiming(),
         entry.getRejectionDelayMode(),
         EnumSet.copyOf(entry.getTargetTypes()),
         entry.getProjectileSource(),
         entry.isEnabled(),
         entry.isKeepLoaded(),
         entry.isFromWorldGen(),
         entry.isCancelDelayedEffectsOnExit(),
         entry.getActivationDelay(),
         entry.getCooldown(),
         entry.getCooldownMode(),
         entry.getEffectAssetRef(),
         entry.getGroupId(),
         entry.getColor() != null ? new Vector3f(entry.getColor()) : null,
         deepCopyTags(entry.getRawTags())
      );
   }

   @Nonnull
   private static Map<String, String> deepCopyTags(@Nonnull Map<String, String> tags) {
      if (tags.isEmpty()) {
         return Collections.emptyMap();
      } else {
         HashMap<String, String> copy = new HashMap<>(tags.size());

         for (Entry<String, String> entry : tags.entrySet()) {
            copy.put(entry.getKey(), entry.getValue());
         }

         return copy;
      }
   }

   @Nullable
   public TriggerVolumeSnapshot restore(@Nonnull Ref<EntityStore> ref, PlayerRef playerRef, World world, ComponentAccessor<EntityStore> componentAccessor) {
      TriggerVolumesPlugin plugin = TriggerVolumesPlugin.get();
      TriggerVolumeManager manager = world.getEntityStore().getStore().getResource(plugin.getManagerResourceType());
      if (manager == null) {
         return null;
      } else {
         return switch (this.type) {
            case CREATE -> {
               VolumeEntry entry = manager.getVolume(this.volumeId);
               if (entry == null) {
                  yield null;
               } else {
                  TriggerVolumeSnapshot inverse = ofDelete(entry);
                  manager.unregister(this.volumeId);
                  manager.notifyViewersRemove(this.volumeId);
                  yield inverse;
               }
            }
            case DELETE -> {
               if (this.position != null
                  && this.shape != null
                  && this.conditions != null
                  && this.effects != null
                  && this.rejectionEffects != null
                  && this.conditionTiming != null
                  && this.rejectionDelayMode != null
                  && this.targetTypes != null
                  && this.projectileSource != null) {
                  VolumeEntry entry = new VolumeEntry(
                     this.volumeId,
                     this.worldName,
                     new Vector3d(this.position),
                     this.shape.copy(),
                     TriggerEffect.deepCopyList(this.effects),
                     EnumSet.copyOf(this.targetTypes),
                     this.enabled
                  );
                  entry.getConditions().addAll(TriggerCondition.deepCopyList(this.conditions));
                  entry.getRejectionEffects().addAll(TriggerEffect.deepCopyList(this.rejectionEffects));
                  entry.setConditionTiming(this.conditionTiming);
                  entry.setRejectionDelayMode(this.rejectionDelayMode);
                  entry.setProjectileSource(this.projectileSource);
                  entry.setKeepLoaded(this.keepLoaded);
                  entry.setFromWorldGen(this.fromWorldGen);
                  entry.setCancelDelayedEffectsOnExit(this.cancelDelayedEffectsOnExit);
                  entry.setActivationDelay(this.activationDelay);
                  entry.setCooldown(this.cooldown);
                  if (this.cooldownMode != null) {
                     entry.setCooldownMode(this.cooldownMode);
                  }

                  entry.setEffectAssetRef(this.effectAssetRef);
                  entry.setGroupId(this.groupId);
                  entry.setColor(this.color != null ? new Vector3f(this.color) : null);
                  if (this.rawTags != null) {
                     entry.setTags(deepCopyTags(this.rawTags));
                  }

                  manager.register(this.volumeId, entry);
                  manager.notifyViewersAdd(entry);
                  yield ofCreate(entry);
               } else {
                  yield null;
               }
            }
            case MUTATE -> {
               VolumeEntry entry = manager.getVolume(this.volumeId);
               if (entry == null) {
                  yield null;
               } else {
                  TriggerVolumeSnapshot inverse = ofMutate(entry);
                  if (this.position != null) {
                     entry.setPosition(new Vector3d(this.position));
                  }

                  if (this.shape != null) {
                     entry.setShape(this.shape.copy());
                  }

                  if (this.effects != null) {
                     entry.getEffects().clear();
                     entry.getEffects().addAll(TriggerEffect.deepCopyList(this.effects));
                  }

                  if (this.conditions != null) {
                     entry.getConditions().clear();
                     entry.getConditions().addAll(TriggerCondition.deepCopyList(this.conditions));
                  }

                  if (this.rejectionEffects != null) {
                     entry.getRejectionEffects().clear();
                     entry.getRejectionEffects().addAll(TriggerEffect.deepCopyList(this.rejectionEffects));
                  }

                  if (this.conditionTiming != null) {
                     entry.setConditionTiming(this.conditionTiming);
                  }

                  if (this.rejectionDelayMode != null) {
                     entry.setRejectionDelayMode(this.rejectionDelayMode);
                  }

                  if (this.targetTypes != null) {
                     entry.getTargetTypes().clear();
                     entry.getTargetTypes().addAll(this.targetTypes);
                  }

                  if (this.projectileSource != null) {
                     entry.setProjectileSource(this.projectileSource);
                  }

                  entry.setEnabled(this.enabled);
                  entry.setKeepLoaded(this.keepLoaded);
                  entry.setFromWorldGen(this.fromWorldGen);
                  entry.setCancelDelayedEffectsOnExit(this.cancelDelayedEffectsOnExit);
                  entry.setActivationDelay(this.activationDelay);
                  entry.setCooldown(this.cooldown);
                  if (this.cooldownMode != null) {
                     entry.setCooldownMode(this.cooldownMode);
                  }

                  entry.setEffectAssetRef(this.effectAssetRef);
                  entry.setGroupId(this.groupId);
                  entry.setColor(this.color != null ? new Vector3f(this.color) : null);
                  if (this.rawTags != null) {
                     entry.setTags(deepCopyTags(this.rawTags));
                  }

                  manager.markSpatialDirty();
                  manager.notifyViewersAdd(entry);
                  yield inverse;
               }
            }
         };
      }
   }

   public static enum SnapshotType {
      CREATE,
      DELETE,
      MUTATE;
   }
}
