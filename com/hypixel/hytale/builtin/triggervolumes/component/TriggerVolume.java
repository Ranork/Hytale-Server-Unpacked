package com.hypixel.hytale.builtin.triggervolumes.component;

import com.hypixel.hytale.builtin.triggervolumes.EntityTargetType;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerCondition;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerVolumeCodecs;
import com.hypixel.hytale.builtin.triggervolumes.manager.ConditionTiming;
import com.hypixel.hytale.builtin.triggervolumes.manager.CooldownMode;
import com.hypixel.hytale.builtin.triggervolumes.manager.ProjectileSource;
import com.hypixel.hytale.builtin.triggervolumes.manager.RejectionDelayMode;
import com.hypixel.hytale.builtin.triggervolumes.manager.VolumeEntry;
import com.hypixel.hytale.builtin.triggervolumes.shape.BoxShape;
import com.hypixel.hytale.builtin.triggervolumes.shape.TriggerVolumeShape;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.math.vector.Vector3fUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.ArrayList;
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

public class TriggerVolume implements Component<EntityStore> {
   @Nonnull
   public static final BuilderCodec<TriggerVolume> CODEC = BuilderCodec.builder(TriggerVolume.class, TriggerVolume::new)
      .append(new KeyedCodec<>("Shape", TriggerVolumeShape.CODEC), (c, v) -> c.shape = v, c -> c.shape)
      .add()
      .append(
         new KeyedCodec<>("Effects", TriggerVolumeCodecs.TOLERANT_EFFECTS, false),
         (c, v) -> c.effects = v != null ? List.of(v) : null,
         c -> c.effects != null ? c.effects.toArray(TriggerEffect[]::new) : null
      )
      .add()
      .append(
         new KeyedCodec<>("Conditions", TriggerVolumeCodecs.TOLERANT_CONDITIONS, false),
         (component, conditions) -> component.conditions = conditions != null ? List.of(conditions) : null,
         component -> component.conditions != null ? component.conditions.toArray(TriggerCondition[]::new) : null
      )
      .add()
      .append(
         new KeyedCodec<>("RejectionEffects", TriggerVolumeCodecs.TOLERANT_EFFECTS, false),
         (component, rejectionEffects) -> component.rejectionEffects = rejectionEffects != null ? List.of(rejectionEffects) : null,
         component -> component.rejectionEffects != null ? component.rejectionEffects.toArray(TriggerEffect[]::new) : null
      )
      .add()
      .append(
         new KeyedCodec<>("ConditionTiming", new EnumCodec<>(ConditionTiming.class), false),
         (component, conditionTiming) -> component.conditionTiming = conditionTiming,
         component -> component.conditionTiming != ConditionTiming.AFTER_VOLUME_DELAY ? component.conditionTiming : null
      )
      .add()
      .append(
         new KeyedCodec<>("RejectionDelayMode", new EnumCodec<>(RejectionDelayMode.class, EnumCodec.EnumStyle.LEGACY), false),
         (component, rejectionDelayMode) -> component.rejectionDelayMode = rejectionDelayMode,
         component -> component.rejectionDelayMode != RejectionDelayMode.USE_VOLUME_DELAY ? component.rejectionDelayMode : null
      )
      .add()
      .append(new KeyedCodec<>("Enabled", Codec.BOOLEAN, false), (c, v) -> c.enabled = v, c -> c.enabled)
      .add()
      .append(new KeyedCodec<>("TargetTypes", new ArrayCodec<>(new EnumCodec<>(EntityTargetType.class), EntityTargetType[]::new), false), (c, arr) -> {
         c.targetTypes.clear();
         Collections.addAll(c.targetTypes, arr);
      }, c -> c.targetTypes.isEmpty() ? null : c.targetTypes.toArray(EntityTargetType[]::new))
      .add()
      .append(new KeyedCodec<>("EffectAsset", Codec.STRING, false), (c, v) -> c.effectAssetRef = v, c -> c.effectAssetRef)
      .add()
      .append(
         new KeyedCodec<>("ProjectileSource", new EnumCodec<>(ProjectileSource.class), false),
         (component, projectileSource) -> component.projectileSource = projectileSource != null ? projectileSource : ProjectileSource.SHOOTER,
         component -> component.projectileSource != ProjectileSource.SHOOTER ? component.projectileSource : null
      )
      .add()
      .append(new KeyedCodec<>("Tags", TriggerVolumeCodecs.TAGS, false), (c, v) -> c.rawTags = v, c -> c.rawTags.isEmpty() ? null : c.rawTags)
      .add()
      .append(new KeyedCodec<>("KeepLoaded", Codec.BOOLEAN, false), (c, v) -> c.keepLoaded = v, c -> c.keepLoaded)
      .add()
      .append(new KeyedCodec<>("Color", Vector3fUtil.CODEC, false), (c, v) -> c.color = v, c -> c.color)
      .add()
      .append(
         new KeyedCodec<>("ActivationDelay", Codec.FLOAT, false), (c, v) -> c.activationDelay = v, c -> c.activationDelay > 0.0F ? c.activationDelay : null
      )
      .add()
      .append(
         new KeyedCodec<>("CancelDelayedOnExit", Codec.BOOLEAN, false),
         (c, v) -> c.cancelDelayedEffectsOnExit = v,
         c -> c.cancelDelayedEffectsOnExit ? Boolean.TRUE : null
      )
      .add()
      .append(new KeyedCodec<>("Cooldown", Codec.FLOAT, false), (c, v) -> c.cooldown = v, c -> c.cooldown > 0.0F ? c.cooldown : null)
      .add()
      .append(new KeyedCodec<>("CooldownMode", Codec.STRING, false), (c, v) -> {
         try {
            c.cooldownMode = CooldownMode.valueOf(v.toUpperCase());
         } catch (IllegalArgumentException var3) {
         }
      }, c -> c.cooldownMode != CooldownMode.PER_ENTITY ? c.cooldownMode.name() : null)
      .add()
      .append(new KeyedCodec<>("GroupLinkId", Codec.STRING, false), (c, v) -> c.groupLinkId = v, c -> c.groupLinkId)
      .add()
      .build();
   @Nullable
   private TriggerVolumeShape shape;
   @Nullable
   private List<TriggerEffect> effects;
   @Nullable
   private List<TriggerCondition> conditions;
   @Nullable
   private List<TriggerEffect> rejectionEffects;
   @Nonnull
   private ConditionTiming conditionTiming = ConditionTiming.AFTER_VOLUME_DELAY;
   @Nonnull
   private RejectionDelayMode rejectionDelayMode = RejectionDelayMode.USE_VOLUME_DELAY;
   private boolean enabled = true;
   @Nonnull
   private Set<EntityTargetType> targetTypes = EnumSet.of(EntityTargetType.PLAYER);
   @Nonnull
   private ProjectileSource projectileSource = ProjectileSource.SHOOTER;
   @Nullable
   private String effectAssetRef;
   @Nonnull
   private Map<String, String> rawTags = Collections.emptyMap();
   private boolean keepLoaded;
   @Nullable
   private Vector3f color;
   private boolean cancelDelayedEffectsOnExit;
   private float activationDelay;
   private float cooldown;
   @Nonnull
   private CooldownMode cooldownMode = CooldownMode.PER_ENTITY;
   @Nullable
   private String groupLinkId;

   @Nonnull
   public static TriggerVolume fromVolumeEntry(@Nonnull VolumeEntry entry) {
      TriggerVolume tv = new TriggerVolume();
      tv.shape = entry.getShape().copy();
      tv.effects = entry.getEffects().isEmpty() ? null : TriggerEffect.deepCopyList(entry.getEffects());
      tv.conditions = entry.getConditions().isEmpty() ? null : TriggerCondition.deepCopyList(entry.getConditions());
      tv.rejectionEffects = entry.getRejectionEffects().isEmpty() ? null : TriggerEffect.deepCopyList(entry.getRejectionEffects());
      tv.conditionTiming = entry.getConditionTiming();
      tv.rejectionDelayMode = entry.getRejectionDelayMode();
      tv.enabled = entry.isEnabled();
      tv.targetTypes = EnumSet.copyOf(entry.getTargetTypes());
      tv.projectileSource = entry.getProjectileSource();
      tv.effectAssetRef = entry.getEffectAssetRef();
      tv.rawTags = copyTags(entry.getRawTags());
      tv.keepLoaded = entry.isKeepLoaded();
      tv.color = entry.getColor() != null ? new Vector3f(entry.getColor()) : null;
      tv.cancelDelayedEffectsOnExit = entry.isCancelDelayedEffectsOnExit();
      tv.activationDelay = entry.getActivationDelay();
      tv.cooldown = entry.getCooldown();
      tv.cooldownMode = entry.getCooldownMode();
      tv.groupLinkId = null;
      return tv;
   }

   @Nonnull
   public VolumeEntry toVolumeEntry(@Nonnull String id, @Nonnull String worldName, @Nonnull Vector3d position) {
      return this.toVolumeEntry(id, worldName, position, 0.0F);
   }

   @Nonnull
   public VolumeEntry toVolumeEntry(@Nonnull String id, @Nonnull String worldName, @Nonnull Vector3d position, float yawRadians) {
      TriggerVolumeShape entryShape = (TriggerVolumeShape)(this.shape != null ? this.shape.copy() : new BoxShape());
      entryShape.rotateInPlace(yawRadians);
      VolumeEntry entry = new VolumeEntry(
         id,
         worldName,
         position,
         entryShape,
         (List<TriggerEffect>)(this.effects != null ? TriggerEffect.deepCopyList(this.effects) : new ArrayList<>()),
         EnumSet.copyOf(this.targetTypes),
         this.enabled
      );
      if (this.conditions != null) {
         entry.getConditions().addAll(TriggerCondition.deepCopyList(this.conditions));
      }

      if (this.rejectionEffects != null) {
         entry.getRejectionEffects().addAll(TriggerEffect.deepCopyList(this.rejectionEffects));
      }

      entry.setConditionTiming(this.conditionTiming);
      entry.setRejectionDelayMode(this.rejectionDelayMode);
      entry.setEffectAssetRef(this.effectAssetRef);
      entry.setKeepLoaded(this.keepLoaded);
      if (this.color != null) {
         entry.setColor(new Vector3f(this.color));
      }

      entry.setProjectileSource(this.projectileSource);
      entry.setActivationDelay(this.activationDelay);
      entry.setCancelDelayedEffectsOnExit(this.cancelDelayedEffectsOnExit);
      entry.setCooldown(this.cooldown);
      entry.setCooldownMode(this.cooldownMode);
      if (!this.rawTags.isEmpty()) {
         entry.setTags(copyTags(this.rawTags));
      }

      return entry;
   }

   @Nonnull
   private static Map<String, String> copyTags(@Nonnull Map<String, String> tags) {
      if (tags.isEmpty()) {
         return Collections.emptyMap();
      } else {
         HashMap<String, String> copy = new HashMap<>();

         for (Entry<String, String> entry : tags.entrySet()) {
            copy.put(entry.getKey(), entry.getValue());
         }

         return copy;
      }
   }

   @Nullable
   public String getGroupLinkId() {
      return this.groupLinkId;
   }

   public void setGroupLinkId(@Nullable String groupLinkId) {
      this.groupLinkId = groupLinkId;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      TriggerVolume clone = new TriggerVolume();
      clone.shape = this.shape != null ? this.shape.copy() : null;
      clone.effects = this.effects != null ? TriggerEffect.deepCopyList(this.effects) : null;
      clone.conditions = this.conditions != null ? TriggerCondition.deepCopyList(this.conditions) : null;
      clone.rejectionEffects = this.rejectionEffects != null ? TriggerEffect.deepCopyList(this.rejectionEffects) : null;
      clone.conditionTiming = this.conditionTiming;
      clone.rejectionDelayMode = this.rejectionDelayMode;
      clone.enabled = this.enabled;
      clone.targetTypes = EnumSet.copyOf(this.targetTypes);
      clone.projectileSource = this.projectileSource;
      clone.effectAssetRef = this.effectAssetRef;
      clone.rawTags = copyTags(this.rawTags);
      clone.keepLoaded = this.keepLoaded;
      clone.color = this.color != null ? new Vector3f(this.color) : null;
      clone.cancelDelayedEffectsOnExit = this.cancelDelayedEffectsOnExit;
      clone.activationDelay = this.activationDelay;
      clone.cooldown = this.cooldown;
      clone.cooldownMode = this.cooldownMode;
      clone.groupLinkId = this.groupLinkId;
      return clone;
   }

   @Nullable
   public TriggerVolumeShape getShape() {
      return this.shape;
   }

   @Nullable
   public List<TriggerEffect> getEffects() {
      return this.effects;
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }
}
