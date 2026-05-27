package com.hypixel.hytale.builtin.triggervolumes.ui;

import com.hypixel.hytale.builtin.triggervolumes.EntityTargetType;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerCondition;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.builtin.triggervolumes.manager.ConditionTiming;
import com.hypixel.hytale.builtin.triggervolumes.manager.CooldownMode;
import com.hypixel.hytale.builtin.triggervolumes.manager.GroupEntry;
import com.hypixel.hytale.builtin.triggervolumes.manager.ProjectileSource;
import com.hypixel.hytale.builtin.triggervolumes.manager.RejectionDelayMode;
import com.hypixel.hytale.builtin.triggervolumes.manager.VolumeEntry;
import com.hypixel.hytale.builtin.triggervolumes.shape.BoxShape;
import com.hypixel.hytale.builtin.triggervolumes.shape.CylinderShape;
import com.hypixel.hytale.builtin.triggervolumes.shape.SphereShape;
import com.hypixel.hytale.builtin.triggervolumes.shape.TriggerVolumeShape;
import com.hypixel.hytale.protocol.packets.player.TriggerVolumeShapeType;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3f;

final class TriggerVolumeInspectorDrafts {
   private TriggerVolumeInspectorDrafts() {
   }

   @Nonnull
   static Map<String, String> copyTags(@Nonnull Map<String, String> tags) {
      LinkedHashMap<String, String> copy = new LinkedHashMap<>();

      for (Entry<String, String> entry : tags.entrySet()) {
         copy.put(entry.getKey(), entry.getValue());
      }

      return copy;
   }

   static void remapVolumeGroupId(
      @Nonnull Collection<TriggerVolumeInspectorDrafts.VolumeDraft> volumeDrafts, @Nonnull String oldGroupId, @Nonnull String newGroupId
   ) {
      for (TriggerVolumeInspectorDrafts.VolumeDraft volumeDraft : volumeDrafts) {
         if (oldGroupId.equals(volumeDraft.groupId)) {
            volumeDraft.groupId = newGroupId;
         }
      }
   }

   static final class GroupDraft {
      @Nonnull
      String id;
      @Nonnull
      String originalId;
      @Nonnull
      String worldName;
      @Nonnull
      Vector3d origin;
      int color;
      @Nonnull
      Set<EntityTargetType> targetTypes;
      boolean enabled;
      @Nonnull
      ConditionTiming conditionTiming;
      @Nonnull
      RejectionDelayMode rejectionDelayMode;
      @Nonnull
      List<TriggerCondition> conditions;
      @Nonnull
      List<TriggerEffect> effects;
      @Nonnull
      List<TriggerEffect> rejectionEffects;
      @Nonnull
      Map<String, String> tags;
      @Nonnull
      Set<String> memberVolumeIds;
      boolean dirty;

      @Nonnull
      static TriggerVolumeInspectorDrafts.GroupDraft from(@Nonnull GroupEntry entry) {
         TriggerVolumeInspectorDrafts.GroupDraft draft = new TriggerVolumeInspectorDrafts.GroupDraft();
         draft.id = entry.getId();
         draft.originalId = entry.getId();
         draft.worldName = entry.getWorldName();
         draft.origin = new Vector3d(entry.getOrigin());
         draft.color = entry.getColor();
         draft.targetTypes = entry.getTargetTypes().isEmpty() ? EnumSet.noneOf(EntityTargetType.class) : EnumSet.copyOf(entry.getTargetTypes());
         draft.enabled = entry.isEnabled();
         draft.conditionTiming = entry.getConditionTiming();
         draft.rejectionDelayMode = entry.getRejectionDelayMode();
         draft.conditions = TriggerCondition.deepCopyList(entry.getConditions());
         draft.effects = TriggerEffect.deepCopyList(entry.getEffects());
         draft.rejectionEffects = TriggerEffect.deepCopyList(entry.getRejectionEffects());
         draft.tags = TriggerVolumeInspectorDrafts.copyTags(entry.getRawTags());
         draft.memberVolumeIds = new LinkedHashSet<>(entry.getMemberVolumeIds());
         return draft;
      }

      void markDirty() {
         this.dirty = true;
      }

      void applyTo(@Nonnull GroupEntry entry) {
         entry.setId(this.id);
         entry.setWorldName(this.worldName);
         entry.setOrigin(new Vector3d(this.origin));
         entry.setColor(this.color);
         entry.getTargetTypes().clear();
         entry.getTargetTypes().addAll(this.targetTypes);
         entry.setEnabled(this.enabled);
         entry.setConditionTiming(this.conditionTiming);
         entry.setRejectionDelayMode(this.rejectionDelayMode);
         entry.getConditions().clear();
         entry.getConditions().addAll(TriggerCondition.deepCopyList(this.conditions));
         entry.getEffects().clear();
         entry.getEffects().addAll(TriggerEffect.deepCopyList(this.effects));
         entry.getRejectionEffects().clear();
         entry.getRejectionEffects().addAll(TriggerEffect.deepCopyList(this.rejectionEffects));
         entry.setTags(TriggerVolumeInspectorDrafts.copyTags(this.tags));
         entry.getMemberVolumeIds().clear();
         entry.getMemberVolumeIds().addAll(this.memberVolumeIds);
      }
   }

   static final class VolumeDraft {
      @Nonnull
      String id;
      @Nonnull
      String originalId;
      @Nonnull
      String worldName;
      @Nonnull
      Vector3d position;
      @Nonnull
      TriggerVolumeShapeType shapeType;
      @Nonnull
      Vector3d dimensions;
      @Nonnull
      Vector3d anchorOffset;
      @Nullable
      Vector3f color;
      @Nonnull
      Set<EntityTargetType> targetTypes;
      @Nonnull
      ProjectileSource projectileSource;
      boolean dirty;
      boolean enabled;
      boolean keepLoaded;
      boolean cancelDelayedOnExit;
      float activationDelay;
      float cooldown;
      @Nonnull
      CooldownMode cooldownMode;
      @Nonnull
      ConditionTiming conditionTiming;
      @Nonnull
      RejectionDelayMode rejectionDelayMode;
      @Nullable
      String groupId;
      @Nullable
      String effectAssetRef;
      @Nonnull
      List<TriggerCondition> conditions;
      @Nonnull
      List<TriggerEffect> effects;
      @Nonnull
      List<TriggerEffect> rejectionEffects;
      @Nonnull
      Map<String, String> tags;

      @Nonnull
      static TriggerVolumeInspectorDrafts.VolumeDraft from(@Nonnull VolumeEntry entry) {
         TriggerVolumeInspectorDrafts.VolumeDraft draft = new TriggerVolumeInspectorDrafts.VolumeDraft();
         draft.id = entry.getId();
         draft.originalId = entry.getId();
         draft.worldName = entry.getWorldName();
         draft.position = new Vector3d(entry.getPosition());
         draft.shapeType = shapeTypeOf(entry);
         draft.dimensions = dimensionsOf(entry);
         draft.anchorOffset = anchorOffsetOf(entry);
         draft.color = entry.getColor() != null ? new Vector3f(entry.getColor()) : null;
         draft.targetTypes = entry.getTargetTypes().isEmpty() ? EnumSet.noneOf(EntityTargetType.class) : EnumSet.copyOf(entry.getTargetTypes());
         draft.projectileSource = entry.getProjectileSource();
         draft.enabled = entry.isEnabled();
         draft.keepLoaded = entry.isKeepLoaded();
         draft.cancelDelayedOnExit = entry.isCancelDelayedEffectsOnExit();
         draft.activationDelay = entry.getActivationDelay();
         draft.cooldown = entry.getCooldown();
         draft.cooldownMode = entry.getCooldownMode();
         draft.conditionTiming = entry.getConditionTiming();
         draft.rejectionDelayMode = entry.getRejectionDelayMode();
         draft.groupId = entry.getGroupId();
         draft.effectAssetRef = entry.getEffectAssetRef();
         draft.conditions = TriggerCondition.deepCopyList(entry.getConditions());
         draft.effects = TriggerEffect.deepCopyList(entry.getEffects());
         draft.rejectionEffects = TriggerEffect.deepCopyList(entry.getRejectionEffects());
         draft.tags = TriggerVolumeInspectorDrafts.copyTags(entry.getRawTags());
         return draft;
      }

      void markDirty() {
         this.dirty = true;
      }

      void rescaleAnchorOffset(@Nonnull Vector3d oldDimensions) {
         this.anchorOffset
            .set(
               rescaleAnchorComponent(this.anchorOffset.x(), oldDimensions.x(), this.dimensions.x()),
               rescaleAnchorComponent(this.anchorOffset.y(), oldDimensions.y(), this.dimensions.y()),
               rescaleAnchorComponent(this.anchorOffset.z(), oldDimensions.z(), this.dimensions.z())
            );
      }

      void applyTo(@Nonnull VolumeEntry entry) {
         entry.setId(this.id);
         entry.setWorldName(this.worldName);
         entry.setPosition(new Vector3d(this.position));
         entry.setShape(this.buildShape());
         entry.setColor(this.color != null ? new Vector3f(this.color) : null);
         entry.getTargetTypes().clear();
         entry.getTargetTypes().addAll(this.targetTypes);
         entry.setProjectileSource(this.projectileSource);
         entry.setEnabled(this.enabled);
         entry.setKeepLoaded(this.keepLoaded);
         entry.setCancelDelayedEffectsOnExit(this.cancelDelayedOnExit);
         entry.setActivationDelay(this.activationDelay);
         entry.setCooldown(this.cooldown);
         entry.setCooldownMode(this.cooldownMode);
         entry.setConditionTiming(this.conditionTiming);
         entry.setRejectionDelayMode(this.rejectionDelayMode);
         entry.setGroupId(this.groupId);
         entry.setEffectAssetRef(this.effectAssetRef);
         entry.getConditions().clear();
         entry.getConditions().addAll(TriggerCondition.deepCopyList(this.conditions));
         entry.getEffects().clear();
         entry.getEffects().addAll(TriggerEffect.deepCopyList(this.effects));
         entry.getRejectionEffects().clear();
         entry.getRejectionEffects().addAll(TriggerEffect.deepCopyList(this.rejectionEffects));
         entry.setTags(TriggerVolumeInspectorDrafts.copyTags(this.tags));
      }

      @Nonnull
      private TriggerVolumeShape buildShape() {
         Vector3d extents = new Vector3d(Math.max(0.0, this.dimensions.x()), Math.max(0.0, this.dimensions.y()), Math.max(0.0, this.dimensions.z()));

         return (TriggerVolumeShape)(switch (this.shapeType) {
            case Sphere -> new SphereShape(new Vector3d(this.anchorOffset), extents.x());
            case Cylinder -> new CylinderShape(new Vector3d(this.anchorOffset).sub(0.0, extents.y() * 0.5, 0.0), extents.x(), extents.y());
            case Box -> new BoxShape(new Vector3d(this.anchorOffset).sub(extents), new Vector3d(this.anchorOffset).add(extents));
         });
      }

      @Nonnull
      private static TriggerVolumeShapeType shapeTypeOf(@Nonnull VolumeEntry entry) {
         if (entry.getShape() instanceof SphereShape) {
            return TriggerVolumeShapeType.Sphere;
         } else {
            return entry.getShape() instanceof CylinderShape ? TriggerVolumeShapeType.Cylinder : TriggerVolumeShapeType.Box;
         }
      }

      @Nonnull
      private static Vector3d dimensionsOf(@Nonnull VolumeEntry entry) {
         if (entry.getShape() instanceof BoxShape box) {
            return new Vector3d(box.getMax()).sub(box.getMin()).mul(0.5).absolute();
         } else if (entry.getShape() instanceof SphereShape sphere) {
            return new Vector3d(sphere.getRadius(), 0.0, 0.0);
         } else {
            return entry.getShape() instanceof CylinderShape cylinder
               ? new Vector3d(cylinder.getRadius(), cylinder.getHeight(), 0.0)
               : new Vector3d(1.0, 1.0, 1.0);
         }
      }

      @Nonnull
      private static Vector3d anchorOffsetOf(@Nonnull VolumeEntry entry) {
         if (entry.getShape() instanceof BoxShape box) {
            return new Vector3d(box.getMin()).add(box.getMax()).mul(0.5);
         } else if (entry.getShape() instanceof SphereShape sphere) {
            return new Vector3d(sphere.getCenter());
         } else {
            return entry.getShape() instanceof CylinderShape cylinder
               ? new Vector3d(cylinder.getCenter()).add(0.0, cylinder.getHeight() * 0.5, 0.0)
               : new Vector3d();
         }
      }

      private static double rescaleAnchorComponent(double offset, double oldDimension, double newDimension) {
         return Math.abs(oldDimension) > 1.0E-9 ? offset * newDimension / oldDimension : offset;
      }
   }
}
