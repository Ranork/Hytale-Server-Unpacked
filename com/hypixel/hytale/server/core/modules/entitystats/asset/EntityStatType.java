package com.hypixel.hytale.server.core.modules.entitystats.asset;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.protocol.EntityStatResetBehavior;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelParticle;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.condition.Condition;
import com.hypixel.hytale.server.core.modules.entitystats.asset.modifier.RegeneratingModifier;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import java.lang.ref.SoftReference;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityStatType
   implements JsonAssetWithMap<String, IndexedLookupTableAssetMap<String, EntityStatType>>,
   NetworkSerializable<com.hypixel.hytale.protocol.EntityStatType> {
   public static final AssetBuilderCodec<String, EntityStatType> CODEC = AssetBuilderCodec.builder(
         EntityStatType.class,
         EntityStatType::new,
         Codec.STRING,
         (statType, s) -> statType.id = s,
         statType -> statType.id,
         (asset, data) -> asset.data = data,
         asset -> asset.data
      )
      .appendInherited(
         new KeyedCodec<>("InitialValue", Codec.FLOAT),
         (asset, o) -> asset.initialValue = o,
         asset -> asset.initialValue,
         (asset, parent) -> asset.initialValue = parent.initialValue
      )
      .addValidator(Validators.nonNull())
      .add()
      .<Float>appendInherited(new KeyedCodec<>("Min", Codec.FLOAT), (asset, o) -> asset.min = o, asset -> asset.min, (asset, parent) -> asset.min = parent.min)
      .addValidator(Validators.nonNull())
      .add()
      .<Float>appendInherited(new KeyedCodec<>("Max", Codec.FLOAT), (asset, o) -> asset.max = o, asset -> asset.max, (asset, parent) -> asset.max = parent.max)
      .addValidator(Validators.nonNull())
      .add()
      .appendInherited(
         new KeyedCodec<>("Shared", Codec.BOOLEAN), (asset, o) -> asset.shared = o, asset -> asset.shared, (asset, parent) -> asset.shared = parent.shared
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("Regenerating", new ArrayCodec<>(EntityStatType.Regenerating.CODEC, EntityStatType.Regenerating[]::new)),
         (asset, o) -> asset.regenerating = o,
         asset -> asset.regenerating,
         (asset, parent) -> asset.regenerating = parent.regenerating
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("MinValueEffects", EntityStatType.EntityStatEffects.CODEC),
         (entityStatType, entityStatEffects) -> entityStatType.minValueEffects = entityStatEffects,
         entityStatType -> entityStatType.minValueEffects,
         (entityStatType, parent) -> entityStatType.minValueEffects = parent.minValueEffects
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("MaxValueEffects", EntityStatType.EntityStatEffects.CODEC),
         (entityStatType, entityStatEffects) -> entityStatType.maxValueEffects = entityStatEffects,
         entityStatType -> entityStatType.maxValueEffects,
         (entityStatType, parent) -> entityStatType.maxValueEffects = parent.maxValueEffects
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("ResetType", new EnumCodec<>(EntityStatResetBehavior.class)),
         (entityStatType, value) -> entityStatType.resetBehavior = value,
         entityStatType -> entityStatType.resetBehavior,
         (entityStatType, parent) -> entityStatType.resetBehavior = parent.resetBehavior
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("IgnoreInvulnerability", Codec.BOOLEAN),
         (entityStatType, value) -> entityStatType.ignoreInvulnerability = value,
         entityStatType -> entityStatType.ignoreInvulnerability,
         (entityStatType, parent) -> entityStatType.ignoreInvulnerability = parent.ignoreInvulnerability
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("HideFromTooltip", Codec.BOOLEAN),
         (entityStatType, aBoolean) -> entityStatType.hideFromTooltip = aBoolean,
         entityStatType -> entityStatType.hideFromTooltip,
         (entityStatType, parent) -> entityStatType.hideFromTooltip = parent.hideFromTooltip
      )
      .add()
      .build();
   public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(EntityStatType::getAssetStore));
   private static AssetStore<String, EntityStatType, IndexedLookupTableAssetMap<String, EntityStatType>> ASSET_STORE;
   public static final EntityStatType UNKNOWN = getUnknownFor("Unknown");
   public static final int UNKNOWN_ID = 0;
   protected String id;
   protected AssetExtraInfo.Data data;
   protected boolean unknown;
   protected float initialValue;
   protected float min;
   protected float max;
   protected boolean shared = false;
   @Nullable
   protected EntityStatType.Regenerating[] regenerating;
   protected boolean ignoreInvulnerability;
   protected boolean hideFromTooltip;
   protected EntityStatType.EntityStatEffects minValueEffects;
   protected EntityStatType.EntityStatEffects maxValueEffects;
   protected EntityStatResetBehavior resetBehavior = EntityStatResetBehavior.InitialValue;
   private transient SoftReference<com.hypixel.hytale.protocol.EntityStatType> cachedPacket;

   public static AssetStore<String, EntityStatType, IndexedLookupTableAssetMap<String, EntityStatType>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(EntityStatType.class);
      }

      return ASSET_STORE;
   }

   public static IndexedLookupTableAssetMap<String, EntityStatType> getAssetMap() {
      return (IndexedLookupTableAssetMap<String, EntityStatType>)getAssetStore().getAssetMap();
   }

   protected EntityStatType() {
   }

   public EntityStatType(
      String id,
      int initialValue,
      int min,
      int max,
      boolean shared,
      @Nullable EntityStatType.Regenerating[] regenerating,
      EntityStatType.EntityStatEffects minValueEffects,
      EntityStatType.EntityStatEffects maxValueEffects,
      EntityStatResetBehavior entityStatResetBehavior
   ) {
      this.id = id;
      this.initialValue = initialValue;
      this.min = min;
      this.max = max;
      this.shared = shared;
      this.regenerating = regenerating;
      this.minValueEffects = minValueEffects;
      this.maxValueEffects = maxValueEffects;
      this.resetBehavior = entityStatResetBehavior;
   }

   public String getId() {
      return this.id;
   }

   public boolean isUnknown() {
      return this.unknown;
   }

   public float getInitialValue() {
      return this.initialValue;
   }

   public float getMin() {
      return this.min;
   }

   public float getMax() {
      return this.max;
   }

   public boolean getIgnoreInvulnerability() {
      return this.ignoreInvulnerability;
   }

   public boolean isShared() {
      return this.shared;
   }

   public EntityStatType.EntityStatEffects getMinValueEffects() {
      return this.minValueEffects;
   }

   public EntityStatType.EntityStatEffects getMaxValueEffects() {
      return this.maxValueEffects;
   }

   @Nullable
   public EntityStatType.Regenerating[] getRegenerating() {
      return this.regenerating;
   }

   public EntityStatResetBehavior getResetBehavior() {
      return this.resetBehavior;
   }

   @Nonnull
   public com.hypixel.hytale.protocol.EntityStatType toPacket() {
      com.hypixel.hytale.protocol.EntityStatType cached = this.cachedPacket == null ? null : this.cachedPacket.get();
      if (cached != null) {
         return cached;
      } else {
         com.hypixel.hytale.protocol.EntityStatType packet = new com.hypixel.hytale.protocol.EntityStatType();
         packet.id = this.id;
         packet.value = this.initialValue;
         packet.min = this.min;
         packet.max = this.max;
         if (this.minValueEffects != null) {
            packet.minValueEffects = this.minValueEffects.toPacket();
         }

         if (this.maxValueEffects != null) {
            packet.maxValueEffects = this.maxValueEffects.toPacket();
         }

         packet.resetBehavior = this.resetBehavior;
         packet.hideFromTooltip = this.hideFromTooltip;
         this.cachedPacket = new SoftReference<>(packet);
         return packet;
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "EntityStatType{id='"
         + this.id
         + "', data="
         + this.data
         + ", unknown="
         + this.unknown
         + ", initialValue="
         + this.initialValue
         + ", min="
         + this.min
         + ", max="
         + this.max
         + ", shared="
         + this.shared
         + ", regenerating="
         + Arrays.toString((Object[])this.regenerating)
         + ", minValueEffects="
         + this.minValueEffects
         + ", maxValueEffects="
         + this.maxValueEffects
         + ", resetBehavior="
         + this.resetBehavior
         + ", ignoreInvulnerability="
         + this.ignoreInvulnerability
         + ", hideFromTooltip="
         + this.hideFromTooltip
         + "}";
   }

   @Nonnull
   public static EntityStatType getUnknownFor(final String unknownId) {
      return new EntityStatType() {
         {
            this.id = unknownId;
            this.unknown = true;
         }
      };
   }

   public static class EntityStatEffects implements NetworkSerializable<com.hypixel.hytale.protocol.EntityStatEffects> {
      public static final BuilderCodec<EntityStatType.EntityStatEffects> CODEC = BuilderCodec.builder(
            EntityStatType.EntityStatEffects.class, EntityStatType.EntityStatEffects::new
         )
         .append(
            new KeyedCodec<>("TriggerAtZero", Codec.BOOLEAN),
            (entityStatEffects, f) -> entityStatEffects.triggerAtZero = f,
            entityStatEffects -> entityStatEffects.triggerAtZero
         )
         .documentation("Indicates that the effects should trigger when the stat reaches zero")
         .add()
         .<String>append(
            new KeyedCodec<>("SoundEventId", Codec.STRING),
            (entityStatEffects, s) -> entityStatEffects.soundEventId = s,
            entityStatEffects -> entityStatEffects.soundEventId
         )
         .addValidator(SoundEvent.VALIDATOR_CACHE.getValidator())
         .add()
         .append(
            new KeyedCodec<>("Particles", ModelParticle.ARRAY_CODEC),
            (entityStatEffects, modelParticles) -> entityStatEffects.particles = modelParticles,
            entityStatEffects -> entityStatEffects.particles
         )
         .add()
         .<String>append(
            new KeyedCodec<>("Interactions", RootInteraction.CHILD_ASSET_CODEC),
            (entityStatEffects, interactions) -> entityStatEffects.interactions = interactions,
            entityStatEffects -> entityStatEffects.interactions
         )
         .addValidatorLate(() -> RootInteraction.VALIDATOR_CACHE.getValidator().late())
         .add()
         .afterDecode(entityStatEffects -> {
            String soundEventId = entityStatEffects.soundEventId;
            if (soundEventId != null) {
               entityStatEffects.soundEventIndex = SoundEvent.getAssetMap().getIndex(soundEventId);
            }
         })
         .build();
      private boolean triggerAtZero;
      @Nullable
      private String soundEventId;
      private int soundEventIndex;
      private ModelParticle[] particles;
      private String interactions;

      public EntityStatEffects(@Nullable String soundEventId, ModelParticle[] particles, String interactions) {
         this.soundEventId = soundEventId;
         if (soundEventId != null) {
            this.soundEventIndex = SoundEvent.getAssetMap().getIndex(soundEventId);
         }

         this.particles = particles;
         this.interactions = interactions;
      }

      protected EntityStatEffects() {
      }

      @Nullable
      public String getSoundEventId() {
         return this.soundEventId;
      }

      public int getSoundEventIndex() {
         return this.soundEventIndex;
      }

      public ModelParticle[] getParticles() {
         return this.particles;
      }

      public String getInteractions() {
         return this.interactions;
      }

      public boolean triggerAtZero() {
         return this.triggerAtZero;
      }

      @Nonnull
      public com.hypixel.hytale.protocol.EntityStatEffects toPacket() {
         com.hypixel.hytale.protocol.EntityStatEffects packet = new com.hypixel.hytale.protocol.EntityStatEffects();
         packet.soundEventIndex = this.soundEventIndex;
         packet.triggerAtZero = this.triggerAtZero;
         if (this.particles != null && this.particles.length > 0) {
            packet.particles = new com.hypixel.hytale.protocol.ModelParticle[this.particles.length];

            for (int i = 0; i < this.particles.length; i++) {
               packet.particles[i] = this.particles[i].toPacket();
            }
         }

         return packet;
      }

      @Nonnull
      @Override
      public String toString() {
         return "EntityStatEffects{soundEventId='"
            + this.soundEventId
            + "', particles="
            + Arrays.toString((Object[])this.particles)
            + ", interactions="
            + this.interactions
            + "}";
      }
   }

   public static class Regenerating {
      public static final BuilderCodec<EntityStatType.Regenerating> CODEC = BuilderCodec.builder(
            EntityStatType.Regenerating.class, EntityStatType.Regenerating::new
         )
         .addField(new KeyedCodec<>("Interval", Codec.FLOAT), (regenerating, value) -> regenerating.interval = value, regenerating -> regenerating.interval)
         .addField(new KeyedCodec<>("Amount", Codec.FLOAT), (regenerating, value) -> regenerating.amount = value, regenerating -> regenerating.amount)
         .<Boolean>append(
            new KeyedCodec<>("ClampAtZero", Codec.BOOLEAN), (regenerating, value) -> regenerating.clampAtZero = value, regenerating -> regenerating.clampAtZero
         )
         .documentation("Prevents this regenerating value from taking the stat value below zero.")
         .add()
         .addField(
            new KeyedCodec<>("RegenType", new EnumCodec<>(EntityStatType.Regenerating.RegenType.class)),
            (regenerating, value) -> regenerating.regenType = value,
            regenerating -> regenerating.regenType
         )
         .addField(
            new KeyedCodec<>("Conditions", new ArrayCodec<>(Condition.CODEC, Condition[]::new)),
            (regenerating, value) -> regenerating.conditions = value,
            regenerating -> regenerating.conditions
         )
         .addField(
            new KeyedCodec<>("Modifiers", new ArrayCodec<>(RegeneratingModifier.CODEC, RegeneratingModifier[]::new)),
            (regenerating, value) -> regenerating.modifiers = value,
            regenerating -> regenerating.modifiers
         )
         .build();
      private float interval;
      private float amount;
      private boolean clampAtZero;
      private EntityStatType.Regenerating.RegenType regenType;
      @Nullable
      private Condition[] conditions;
      private RegeneratingModifier[] modifiers;

      public Regenerating() {
      }

      public Regenerating(
         long interval, float amount, EntityStatType.Regenerating.RegenType regenType, @Nullable Condition[] conditions, RegeneratingModifier[] modifiers
      ) {
         this.interval = (float)interval;
         this.amount = amount;
         this.regenType = regenType;
         this.conditions = conditions;
         this.modifiers = modifiers;
      }

      public float getInterval() {
         return this.interval;
      }

      public float getAmount() {
         return this.amount;
      }

      public float clampAmount(float toAdd, float currentAmount, @Nonnull EntityStatValue statValue) {
         if (this.clampAtZero && !(toAdd > 0.0F)) {
            if (statValue.getMin() >= 0.0F) {
               return toAdd;
            } else {
               float resultValue = statValue.get() + currentAmount + toAdd;
               return resultValue >= 0.0F ? toAdd : Math.min(0.0F, toAdd - resultValue);
            }
         } else {
            return toAdd;
         }
      }

      public EntityStatType.Regenerating.RegenType getRegenType() {
         return this.regenType;
      }

      @Nullable
      public Condition[] getConditions() {
         return this.conditions;
      }

      public RegeneratingModifier[] getModifiers() {
         return this.modifiers;
      }

      @Nonnull
      @Override
      public String toString() {
         return "Regenerating{interval="
            + this.interval
            + ", amount="
            + this.amount
            + ", regenType="
            + this.regenType
            + ", conditions="
            + Arrays.toString((Object[])this.conditions)
            + ", modifiers="
            + Arrays.toString((Object[])this.modifiers)
            + "}";
      }

      public static enum RegenType {
         ADDITIVE,
         PERCENTAGE;
      }
   }
}
