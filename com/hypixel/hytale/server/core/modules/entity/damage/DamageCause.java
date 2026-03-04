package com.hypixel.hytale.server.core.modules.entity.damage;

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
import com.hypixel.hytale.codec.validation.ValidatorCache;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DamageCause implements JsonAssetWithMap<String, IndexedLookupTableAssetMap<String, DamageCause>> {
   @Nonnull
   public static final AssetBuilderCodec<String, DamageCause> CODEC = AssetBuilderCodec.builder(
         DamageCause.class,
         DamageCause::new,
         Codec.STRING,
         (damageCause, s) -> damageCause.id = s,
         damageCause -> damageCause.id,
         (asset, data) -> asset.data = data,
         asset -> asset.data
      )
      .append(new KeyedCodec<>("Inherits", Codec.STRING), (builder, name) -> builder.inherits = name, builder -> builder.inherits)
      .add()
      .append(new KeyedCodec<>("DurabilityLoss", Codec.BOOLEAN), (builder, name) -> builder.durabilityLoss = name, builder -> builder.durabilityLoss)
      .add()
      .append(new KeyedCodec<>("StaminaLoss", Codec.BOOLEAN), (builder, name) -> builder.staminaLoss = name, builder -> builder.staminaLoss)
      .add()
      .append(new KeyedCodec<>("BypassResistances", Codec.BOOLEAN), (builder, name) -> builder.bypassResistances = name, builder -> builder.bypassResistances)
      .add()
      .append(new KeyedCodec<>("DamageTextColor", Codec.STRING), (builder, name) -> builder.damageTextColor = name, builder -> builder.damageTextColor)
      .add()
      .append(new KeyedCodec<>("AnimationId", Codec.STRING), (builder, name) -> builder.animationId = name, builder -> builder.animationId)
      .add()
      .append(new KeyedCodec<>("DeathAnimationId", Codec.STRING), (builder, name) -> builder.deathAnimationId = name, builder -> builder.deathAnimationId)
      .add()
      .build();
   private static AssetStore<String, DamageCause, IndexedLookupTableAssetMap<String, DamageCause>> ASSET_STORE;
   @Nonnull
   public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(DamageCause::getAssetStore));
   @Nonnull
   public static final Codec<String> CHILD_ASSET_CODEC = new ContainedAssetCodec<>(DamageCause.class, CODEC);
   @Nullable
   @Deprecated
   public static DamageCause PHYSICAL;
   @Nullable
   @Deprecated
   public static DamageCause PROJECTILE;
   @Nullable
   @Deprecated
   public static DamageCause COMMAND;
   @Nullable
   @Deprecated
   public static DamageCause DROWNING;
   @Nullable
   @Deprecated
   public static DamageCause ENVIRONMENT;
   @Nullable
   @Deprecated
   public static DamageCause FALL;
   @Nullable
   @Deprecated
   public static DamageCause OUT_OF_WORLD;
   @Nullable
   @Deprecated
   public static DamageCause SUFFOCATION;
   protected AssetExtraInfo.Data data;
   protected String id;
   protected String inherits;
   protected boolean durabilityLoss;
   protected boolean staminaLoss;
   protected boolean bypassResistances;
   protected String damageTextColor;
   protected String animationId = "Hurt";
   protected String deathAnimationId = "Death";

   @Nonnull
   public static AssetStore<String, DamageCause, IndexedLookupTableAssetMap<String, DamageCause>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(DamageCause.class);
      }

      return ASSET_STORE;
   }

   @Nonnull
   public static IndexedLookupTableAssetMap<String, DamageCause> getAssetMap() {
      return (IndexedLookupTableAssetMap<String, DamageCause>)getAssetStore().getAssetMap();
   }

   public DamageCause() {
   }

   public DamageCause(@Nonnull String id) {
      this.id = id;
   }

   public DamageCause(@Nonnull String id, @Nonnull String inherits, boolean durabilityLoss, boolean staminaLoss, boolean bypassResistances) {
      this.id = id;
      this.inherits = inherits;
      this.durabilityLoss = durabilityLoss;
      this.staminaLoss = staminaLoss;
      this.bypassResistances = bypassResistances;
   }

   public String getId() {
      return this.id;
   }

   public boolean isDurabilityLoss() {
      return this.durabilityLoss;
   }

   public boolean isStaminaLoss() {
      return this.staminaLoss;
   }

   public boolean doesBypassResistances() {
      return this.bypassResistances;
   }

   public String getInherits() {
      return this.inherits;
   }

   public String getAnimationId() {
      return this.animationId;
   }

   public String getDeathAnimationId() {
      return this.deathAnimationId;
   }

   @Nonnull
   public com.hypixel.hytale.protocol.DamageCause toPacket() {
      return new com.hypixel.hytale.protocol.DamageCause(this.id, this.damageTextColor);
   }
}
