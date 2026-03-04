package com.hypixel.hytale.server.core.asset.type.entityeffect.config;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.codec.codecs.map.Object2FloatMapCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.protocol.ValueType;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemArmor;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.combat.DamageCalculator;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.combat.DamageEffects;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityEffect
   implements JsonAssetWithMap<String, IndexedLookupTableAssetMap<String, EntityEffect>>,
   NetworkSerializable<com.hypixel.hytale.protocol.EntityEffect> {
   @Nonnull
   public static final AssetBuilderCodec<String, EntityEffect> CODEC = AssetBuilderCodec.builder(
         EntityEffect.class,
         EntityEffect::new,
         Codec.STRING,
         (entityEffect, s) -> entityEffect.id = s,
         entityEffect -> entityEffect.id,
         (asset, data) -> asset.data = data,
         asset -> asset.data
      )
      .appendInherited(
         new KeyedCodec<>("Name", Codec.STRING),
         (entityEffect, s) -> entityEffect.name = s,
         entityEffect -> entityEffect.name,
         (entityEffect, parent) -> entityEffect.name = parent.name
      )
      .documentation("The name of this entity effect that will be displayed in the UI. This must be a localization key.")
      .add()
      .appendInherited(
         new KeyedCodec<>("ApplicationEffects", ApplicationEffects.CODEC),
         (entityEffect, s) -> entityEffect.applicationEffects = s,
         entityEffect -> entityEffect.applicationEffects,
         (entityEffect, parent) -> entityEffect.applicationEffects = parent.applicationEffects
      )
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("WorldRemovalSoundEventId", Codec.STRING),
         (entityEffect, s) -> entityEffect.worldRemovalSoundEventId = s,
         entityEffect -> entityEffect.worldRemovalSoundEventId,
         (entityEffect, parent) -> entityEffect.worldRemovalSoundEventId = parent.worldRemovalSoundEventId
      )
      .addValidator(SoundEvent.VALIDATOR_CACHE.getValidator())
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("LocalRemovalSoundEventId", Codec.STRING),
         (entityEffect, s) -> entityEffect.localRemovalSoundEventId = s,
         entityEffect -> entityEffect.localRemovalSoundEventId,
         (entityEffect, parent) -> entityEffect.localRemovalSoundEventId = parent.localRemovalSoundEventId
      )
      .addValidator(SoundEvent.VALIDATOR_CACHE.getValidator())
      .add()
      .appendInherited(
         new KeyedCodec<>("DamageCalculator", DamageCalculator.CODEC),
         (entityEffect, s) -> entityEffect.damageCalculator = s,
         entityEffect -> entityEffect.damageCalculator,
         (entityEffect, parent) -> entityEffect.damageCalculator = parent.damageCalculator
      )
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("DamageCalculatorCooldown", Codec.FLOAT),
         (entityEffect, s) -> entityEffect.damageCalculatorCooldown = s,
         entityEffect -> entityEffect.damageCalculatorCooldown,
         (entityEffect, parent) -> entityEffect.damageCalculatorCooldown = parent.damageCalculatorCooldown
      )
      .addValidator(Validators.greaterThanOrEqual(0.0F))
      .add()
      .appendInherited(
         new KeyedCodec<>("DamageEffects", DamageEffects.CODEC),
         (entityEffect, s) -> entityEffect.damageEffects = s,
         entityEffect -> entityEffect.damageEffects,
         (entityEffect, parent) -> entityEffect.damageEffects = parent.damageEffects
      )
      .add()
      .<DamageEffects>appendInherited(
         new KeyedCodec<>("StatModifierEffects", DamageEffects.CODEC),
         (entityEffect, s) -> entityEffect.statModifierEffects = s,
         entityEffect -> entityEffect.statModifierEffects,
         (entityEffect, parent) -> entityEffect.statModifierEffects = parent.statModifierEffects
      )
      .documentation("Effects to play when stat modifiers are applied and updated.")
      .add()
      .appendInherited(
         new KeyedCodec<>("ModelOverride", ModelOverride.CODEC),
         (entityEffect, o) -> entityEffect.modelOverride = o,
         entityEffect -> entityEffect.modelOverride,
         (entityEffect, parent) -> entityEffect.modelOverride = parent.modelOverride
      )
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("ModelChange", Codec.STRING),
         (entityEffect, s) -> entityEffect.modelChange = s,
         entityEffect -> entityEffect.modelChange,
         (entityEffect, parent) -> entityEffect.modelChange = parent.modelChange
      )
      .addValidator(ModelAsset.VALIDATOR_CACHE.getValidator())
      .documentation("A model to change the affected entity's appearance to.")
      .add()
      .<Map>append(
         new KeyedCodec<>("RawStatModifiers", new MapCodec<>(new ArrayCodec<>(StaticModifier.CODEC, StaticModifier[]::new), HashMap::new)),
         (entityEffect, map) -> entityEffect.rawStatModifiers = map,
         entityEffect -> entityEffect.rawStatModifiers
      )
      .addValidator(EntityStatType.VALIDATOR_CACHE.getMapKeyValidator().late())
      .add()
      .<Object2FloatMap<String>>appendInherited(
         new KeyedCodec<>("StatModifiers", new Object2FloatMapCodec<>(Codec.STRING, Object2FloatOpenHashMap::new)),
         (entityEffect, o) -> entityEffect.unknownEntityStats = o,
         entityEffect -> entityEffect.unknownEntityStats,
         (entityEffect, parent) -> entityEffect.entityStats = parent.entityStats
      )
      .addValidator(EntityStatType.VALIDATOR_CACHE.getMapKeyValidator())
      .documentation("Modifiers to apply to EntityStats.")
      .add()
      .<ValueType>appendInherited(
         new KeyedCodec<>("ValueType", new EnumCodec<>(ValueType.class)),
         (entityEffect, valueType) -> entityEffect.valueType = valueType,
         entityEffect -> entityEffect.valueType,
         (entityEffect, parent) -> entityEffect.valueType = parent.valueType
      )
      .documentation(
         "Enum to specify if the StatModifiers must be considered as absolute values or percent. Default value is Absolute. When using ValueType.Absolute, '100' matches the max value."
      )
      .addValidator(Validators.nonNull())
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("Duration", Codec.FLOAT),
         (entityEffect, o) -> entityEffect.duration = o,
         entityEffect -> entityEffect.duration,
         (entityEffect, parent) -> entityEffect.duration = parent.duration
      )
      .documentation("Value used by default unless specified otherwise in the method's call.")
      .add()
      .<OverlapBehavior>appendInherited(
         new KeyedCodec<>("OverlapBehavior", OverlapBehavior.CODEC),
         (entityEffect, s) -> entityEffect.overlapBehavior = s,
         entityEffect -> entityEffect.overlapBehavior,
         (entityEffect, parent) -> entityEffect.overlapBehavior = parent.overlapBehavior
      )
      .documentation("Value used by default unless specified otherwise in the method's call.")
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("Infinite", Codec.BOOLEAN),
         (entityEffect, aBoolean) -> entityEffect.infinite = aBoolean,
         entityEffect -> entityEffect.infinite,
         (entityEffect, parent) -> entityEffect.infinite = parent.infinite
      )
      .documentation("Value used by default unless specified otherwise in the method's call.")
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("Debuff", Codec.BOOLEAN),
         (entityEffect, aBoolean) -> entityEffect.debuff = aBoolean,
         entityEffect -> entityEffect.debuff,
         (entityEffect, parent) -> entityEffect.debuff = parent.debuff
      )
      .documentation("Value used by default unless specified otherwise in the method's call.")
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("Locale", Codec.STRING),
         (entityEffect, aString) -> entityEffect.locale = aString,
         entityEffect -> entityEffect.locale,
         (entityEffect, parent) -> entityEffect.locale = parent.locale
      )
      .documentation("[Deprecated - DeathMessageKey instead] Use An optional translation key, used to display the damage cause upon death.")
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("StatusEffectIcon", Codec.STRING),
         (entityEffect, aString) -> entityEffect.statusEffectIcon = aString,
         entityEffect -> entityEffect.statusEffectIcon,
         (entityEffect, parent) -> entityEffect.statusEffectIcon = parent.statusEffectIcon
      )
      .documentation("Value used by default unless specified otherwise in the method's call.")
      .add()
      .<RemovalBehavior>appendInherited(
         new KeyedCodec<>("RemovalBehavior", RemovalBehavior.CODEC),
         (entityEffect, removalBehavior) -> entityEffect.removalBehavior = removalBehavior,
         entityEffect -> entityEffect.removalBehavior,
         (entityEffect, parent) -> entityEffect.removalBehavior = parent.removalBehavior
      )
      .documentation("Value used by default unless specified otherwise in the method's call.")
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("Invulnerable", Codec.BOOLEAN),
         (entityEffect, aBoolean) -> entityEffect.invulnerable = aBoolean,
         entityEffect -> entityEffect.invulnerable,
         (entityEffect, parent) -> entityEffect.invulnerable = parent.invulnerable
      )
      .documentation("Determines whether this effect applies the invulnerable component to the entity whilst active.")
      .add()
      .<Map>appendInherited(
         new KeyedCodec<>("DamageResistance", new MapCodec<>(new ArrayCodec<>(StaticModifier.CODEC, StaticModifier[]::new), HashMap::new)),
         (entityEffect, map) -> entityEffect.damageResistanceValuesRaw = map,
         entityEffect -> entityEffect.damageResistanceValuesRaw,
         (entityEffect, parent) -> entityEffect.damageResistanceValuesRaw = parent.damageResistanceValuesRaw
      )
      .addValidator(DamageCause.VALIDATOR_CACHE.getMapKeyValidator())
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("DeathMessageKey", Codec.STRING),
         (entityEffect, s) -> entityEffect.deathMessageKey = s,
         entityEffect -> entityEffect.deathMessageKey,
         (entityEffect, parent) -> entityEffect.deathMessageKey = parent.deathMessageKey
      )
      .documentation("Localization key used on the death screen when this EntityEffect kills a player.")
      .add()
      .afterDecode(entityEffect -> {
         entityEffect.entityStats = EntityStatsModule.resolveEntityStats(entityEffect.unknownEntityStats);
         entityEffect.statModifiers = EntityStatsModule.resolveEntityStats(entityEffect.rawStatModifiers);
         if (entityEffect.damageResistanceValuesRaw != null && !entityEffect.damageResistanceValuesRaw.isEmpty()) {
            entityEffect.damageResistanceValues = ItemArmor.convertStringKeyToDamageCause(entityEffect.damageResistanceValuesRaw);
         }

         if (entityEffect.worldRemovalSoundEventId != null) {
            entityEffect.worldRemovalSoundEventIndex = SoundEvent.getAssetMap().getIndex(entityEffect.worldRemovalSoundEventId);
         }

         if (entityEffect.localRemovalSoundEventId != null) {
            entityEffect.localRemovalSoundEventIndex = SoundEvent.getAssetMap().getIndex(entityEffect.localRemovalSoundEventId);
         }
      })
      .build();
   public static final Codec<String> CHILD_ASSET_CODEC = new ContainedAssetCodec<>(EntityEffect.class, CODEC);
   @Nullable
   private static AssetStore<String, EntityEffect, IndexedLookupTableAssetMap<String, EntityEffect>> STORE;
   public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(EntityEffect::getAssetStore));
   protected AssetExtraInfo.Data data;
   protected String id;
   @Nullable
   protected String name;
   @Nullable
   protected ApplicationEffects applicationEffects;
   @Nullable
   protected String worldRemovalSoundEventId;
   protected transient int worldRemovalSoundEventIndex = 0;
   @Nullable
   protected String localRemovalSoundEventId;
   protected transient int localRemovalSoundEventIndex = 0;
   @Nullable
   protected DamageCalculator damageCalculator;
   protected float damageCalculatorCooldown;
   @Nullable
   protected DamageEffects damageEffects;
   @Nullable
   protected DamageEffects statModifierEffects;
   @Nullable
   protected ModelOverride modelOverride;
   @Nullable
   protected String modelChange;
   @Nullable
   protected Object2FloatMap<String> unknownEntityStats;
   @Nullable
   protected Int2FloatMap entityStats;
   @Nonnull
   protected ValueType valueType = ValueType.Absolute;
   protected float duration = 0.0F;
   @Nonnull
   protected OverlapBehavior overlapBehavior = OverlapBehavior.IGNORE;
   @Nonnull
   protected RemovalBehavior removalBehavior = RemovalBehavior.COMPLETE;
   protected boolean infinite;
   protected boolean debuff;
   @Nullable
   protected String statusEffectIcon;
   @Nullable
   protected String locale;
   protected boolean invulnerable = false;
   protected String deathMessageKey;
   @Nullable
   protected Map<String, StaticModifier[]> rawStatModifiers;
   @Nullable
   protected Int2ObjectMap<StaticModifier[]> statModifiers;
   @Nullable
   protected Map<String, StaticModifier[]> damageResistanceValuesRaw;
   @Nullable
   protected Map<DamageCause, StaticModifier[]> damageResistanceValues;
   @Nullable
   private SoftReference<com.hypixel.hytale.protocol.EntityEffect> cachedPacket;

   @Nonnull
   public static AssetStore<String, EntityEffect, IndexedLookupTableAssetMap<String, EntityEffect>> getAssetStore() {
      if (STORE == null) {
         STORE = AssetRegistry.getAssetStore(EntityEffect.class);
      }

      return STORE;
   }

   @Nonnull
   public static IndexedLookupTableAssetMap<String, EntityEffect> getAssetMap() {
      return (IndexedLookupTableAssetMap<String, EntityEffect>)getAssetStore().getAssetMap();
   }

   public EntityEffect(@Nonnull String id) {
      this.id = id;
   }

   protected EntityEffect() {
   }

   @Nullable
   public String getName() {
      return this.name;
   }

   @Nullable
   public Int2ObjectMap<StaticModifier[]> getStatModifiers() {
      return this.statModifiers;
   }

   public String getId() {
      return this.id;
   }

   @Nullable
   public ApplicationEffects getApplicationEffects() {
      return this.applicationEffects;
   }

   @Nullable
   public DamageCalculator getDamageCalculator() {
      return this.damageCalculator;
   }

   public float getDamageCalculatorCooldown() {
      return this.damageCalculatorCooldown;
   }

   @Nullable
   public DamageEffects getDamageEffects() {
      return this.damageEffects;
   }

   @Nullable
   public DamageEffects getStatModifierEffects() {
      return this.statModifierEffects;
   }

   @Nullable
   public ModelOverride getModelOverride() {
      return this.modelOverride;
   }

   @Nullable
   public String getModelChange() {
      return this.modelChange;
   }

   @Nullable
   public Int2FloatMap getEntityStats() {
      return this.entityStats;
   }

   public float getDuration() {
      return this.duration;
   }

   @Nonnull
   public OverlapBehavior getOverlapBehavior() {
      return this.overlapBehavior;
   }

   public boolean isInfinite() {
      return this.infinite;
   }

   public boolean isDebuff() {
      return this.debuff;
   }

   @Nullable
   public String getStatusEffectIcon() {
      return this.statusEffectIcon;
   }

   @Nullable
   public String getLocale() {
      return this.locale;
   }

   @Nonnull
   public RemovalBehavior getRemovalBehavior() {
      return this.removalBehavior;
   }

   @Nonnull
   public ValueType getValueType() {
      return this.valueType;
   }

   public boolean isInvulnerable() {
      return this.invulnerable;
   }

   @Nullable
   public Map<DamageCause, StaticModifier[]> getDamageResistanceValues() {
      return this.damageResistanceValues;
   }

   public String getDeathMessageKey() {
      return this.deathMessageKey;
   }

   @Nonnull
   public com.hypixel.hytale.protocol.EntityEffect toPacket() {
      com.hypixel.hytale.protocol.EntityEffect cached = this.cachedPacket == null ? null : this.cachedPacket.get();
      if (cached != null) {
         return cached;
      } else {
         com.hypixel.hytale.protocol.EntityEffect packet = new com.hypixel.hytale.protocol.EntityEffect();
         packet.id = this.id;
         packet.name = this.name;
         if (this.applicationEffects != null) {
            packet.applicationEffects = this.applicationEffects.toPacket();
         }

         packet.worldRemovalSoundEventIndex = this.worldRemovalSoundEventIndex;
         packet.localRemovalSoundEventIndex = this.localRemovalSoundEventIndex != 0 ? this.localRemovalSoundEventIndex : this.worldRemovalSoundEventIndex;
         if (this.modelOverride != null) {
            packet.modelOverride = this.modelOverride.toPacket();
         }

         packet.duration = this.duration;
         packet.infinite = this.infinite;
         packet.debuff = this.debuff;
         packet.statusEffectIcon = this.statusEffectIcon;

         packet.overlapBehavior = switch (this.overlapBehavior) {
            case EXTEND -> com.hypixel.hytale.protocol.OverlapBehavior.Extend;
            case OVERWRITE -> com.hypixel.hytale.protocol.OverlapBehavior.Overwrite;
            case IGNORE -> com.hypixel.hytale.protocol.OverlapBehavior.Ignore;
         };
         packet.damageCalculatorCooldown = this.damageCalculatorCooldown;
         packet.statModifiers = this.entityStats;
         packet.valueType = this.valueType;
         this.cachedPacket = new SoftReference<>(packet);
         return packet;
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "EntityEffect{id='"
         + this.id
         + "', name="
         + this.name
         + ", applicationEffects="
         + this.applicationEffects
         + ", damageCalculator="
         + this.damageCalculator
         + ", damageCalculatorCooldown="
         + this.damageCalculatorCooldown
         + ", damageEffects="
         + this.damageEffects
         + ", modelOverride="
         + this.modelOverride
         + ", modelChange='"
         + this.modelChange
         + "', unknownEntityStats="
         + this.unknownEntityStats
         + ", entityStats="
         + this.entityStats
         + ", valueType="
         + this.valueType
         + ", duration="
         + this.duration
         + ", overlapBehavior="
         + this.overlapBehavior
         + ", infinite="
         + this.infinite
         + ", debuff="
         + this.debuff
         + ", statusEffectIcon="
         + this.statusEffectIcon
         + ", locale="
         + this.locale
         + ", removalBehavior="
         + this.removalBehavior
         + ", invulnerable="
         + this.invulnerable
         + ", damageResistanceValues="
         + this.damageResistanceValues
         + "}";
   }
}
