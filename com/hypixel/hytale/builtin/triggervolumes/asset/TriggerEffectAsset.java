package com.hypixel.hytale.builtin.triggervolumes.asset;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.triggervolumes.EntityTargetType;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerCondition;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerVolumeCodecs;
import com.hypixel.hytale.builtin.triggervolumes.manager.ConditionTiming;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TriggerEffectAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, TriggerEffectAsset>> {
   @Nonnull
   public static final AssetBuilderCodec<String, TriggerEffectAsset> CODEC = AssetBuilderCodec.builder(
         TriggerEffectAsset.class, TriggerEffectAsset::new, Codec.STRING, (a, id) -> a.id = id, a -> a.id, (a, data) -> a.data = data, a -> a.data
      )
      .append(
         new KeyedCodec<>("Conditions", TriggerVolumeCodecs.TOLERANT_CONDITIONS, false),
         (a, v) -> a.conditions = filterConditions(v),
         a -> a.conditions.length == 0 ? null : a.conditions
      )
      .add()
      .append(new KeyedCodec<>("Effects", TriggerVolumeCodecs.TOLERANT_EFFECTS), (a, v) -> a.effects = filterEffects(v), a -> a.effects)
      .add()
      .append(
         new KeyedCodec<>("RejectionEffects", TriggerVolumeCodecs.TOLERANT_EFFECTS, false),
         (a, v) -> a.rejectionEffects = filterEffects(v),
         a -> a.rejectionEffects.length == 0 ? null : a.rejectionEffects
      )
      .add()
      .append(
         new KeyedCodec<>("ConditionTiming", new EnumCodec<>(ConditionTiming.class, EnumCodec.EnumStyle.LEGACY), false),
         (a, v) -> a.conditionTiming = v,
         a -> a.conditionTiming != ConditionTiming.AFTER_VOLUME_DELAY ? a.conditionTiming : null
      )
      .add()
      .append(
         new KeyedCodec<>("TargetTypes", new ArrayCodec<>(new EnumCodec<>(EntityTargetType.class), EntityTargetType[]::new), false),
         (a, v) -> a.targetTypeArray = v,
         a -> a.targetTypeArray
      )
      .add()
      .afterDecode(TriggerEffectAsset::resolveTargetTypes)
      .build();
   private String id;
   private AssetExtraInfo.Data data;
   private TriggerCondition[] conditions = new TriggerCondition[0];
   private TriggerEffect[] effects = new TriggerEffect[0];
   private TriggerEffect[] rejectionEffects = new TriggerEffect[0];
   @Nonnull
   private ConditionTiming conditionTiming = ConditionTiming.AFTER_VOLUME_DELAY;
   @Nullable
   private EntityTargetType[] targetTypeArray;
   private transient Set<EntityTargetType> targetTypes;

   private void resolveTargetTypes() {
      this.targetTypes = EnumSet.noneOf(EntityTargetType.class);
      if (this.targetTypeArray != null) {
         this.targetTypes.addAll(Arrays.asList(this.targetTypeArray));
      }

      if (this.targetTypes.isEmpty()) {
         this.targetTypes.add(EntityTargetType.PLAYER);
      }
   }

   @Nonnull
   public static TriggerEffectAsset create(@Nonnull String id, @Nonnull TriggerEffect[] effects) {
      return create(id, new TriggerCondition[0], effects, new TriggerEffect[0], ConditionTiming.AFTER_VOLUME_DELAY);
   }

   @Nonnull
   public static TriggerEffectAsset create(
      @Nonnull String id,
      @Nonnull TriggerCondition[] conditions,
      @Nonnull TriggerEffect[] effects,
      @Nonnull TriggerEffect[] rejectionEffects,
      @Nonnull ConditionTiming conditionTiming
   ) {
      TriggerEffectAsset asset = new TriggerEffectAsset();
      asset.id = id;
      asset.conditions = conditions;
      asset.effects = effects;
      asset.rejectionEffects = rejectionEffects;
      asset.conditionTiming = conditionTiming;
      asset.resolveTargetTypes();
      return asset;
   }

   @Nonnull
   public String getId() {
      return this.id;
   }

   public void setId(@Nonnull String id) {
      this.id = id;
   }

   @Nonnull
   public TriggerEffect[] getEffects() {
      return this.effects;
   }

   @Nonnull
   public TriggerCondition[] getConditions() {
      return this.conditions;
   }

   @Nonnull
   public TriggerEffect[] getRejectionEffects() {
      return this.rejectionEffects;
   }

   @Nonnull
   public ConditionTiming getConditionTiming() {
      return this.conditionTiming;
   }

   @Nonnull
   public Set<EntityTargetType> getTargetTypes() {
      return this.targetTypes != null ? this.targetTypes : Set.of(EntityTargetType.PLAYER);
   }

   @Nonnull
   private static TriggerCondition[] filterConditions(@Nonnull TriggerCondition[] conditions) {
      return Arrays.stream(conditions).filter(Objects::nonNull).toArray(TriggerCondition[]::new);
   }

   @Nonnull
   private static TriggerEffect[] filterEffects(@Nonnull TriggerEffect[] effects) {
      return Arrays.stream(effects).filter(Objects::nonNull).toArray(TriggerEffect[]::new);
   }
}
