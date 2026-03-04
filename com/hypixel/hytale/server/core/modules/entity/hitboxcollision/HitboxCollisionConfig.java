package com.hypixel.hytale.server.core.modules.entity.hitboxcollision;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.protocol.CollisionType;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import javax.annotation.Nonnull;

public class HitboxCollisionConfig
   implements JsonAssetWithMap<String, IndexedLookupTableAssetMap<String, HitboxCollisionConfig>>,
   NetworkSerializable<com.hypixel.hytale.protocol.HitboxCollisionConfig> {
   public static final AssetBuilderCodec<String, HitboxCollisionConfig> CODEC = AssetBuilderCodec.builder(
         HitboxCollisionConfig.class,
         HitboxCollisionConfig::new,
         Codec.STRING,
         (hitboxCollisionConfig, s) -> hitboxCollisionConfig.id = s,
         hitboxCollisionConfig -> hitboxCollisionConfig.id,
         (hitboxCollisionConfig, data) -> hitboxCollisionConfig.data = data,
         hitboxCollisionConfig -> hitboxCollisionConfig.data
      )
      .appendInherited(
         new KeyedCodec<>("CollisionType", new EnumCodec<>(CollisionType.class)),
         (hitboxCollisionConfig, collisionType) -> hitboxCollisionConfig.collisionType = collisionType,
         hitboxCollisionConfig -> hitboxCollisionConfig.collisionType,
         (hitboxCollisionConfig, parent) -> hitboxCollisionConfig.collisionType = parent.collisionType
      )
      .addValidator(Validators.nonNull())
      .documentation("The type of collision, possible values are: Hard, Soft")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("SoftCollisionOffsetRatio", Codec.FLOAT),
         (hitboxCollisionConfig, aFloat) -> hitboxCollisionConfig.softOffsetRatio = aFloat,
         hitboxCollisionConfig -> hitboxCollisionConfig.softOffsetRatio,
         (hitboxCollisionConfig, parent) -> hitboxCollisionConfig.softOffsetRatio = parent.softOffsetRatio
      )
      .documentation("The ratio for how much of the client move offset should be applied when going through a Soft HitboxCollision")
      .add()
      .build();
   public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(HitboxCollisionConfig::getAssetStore));
   private static AssetStore<String, HitboxCollisionConfig, IndexedLookupTableAssetMap<String, HitboxCollisionConfig>> ASSET_STORE;
   public static final int NO_HITBOX = -1;
   protected AssetExtraInfo.Data data;
   protected String id;
   protected CollisionType collisionType;
   protected float softOffsetRatio = 1.0F;

   public static AssetStore<String, HitboxCollisionConfig, IndexedLookupTableAssetMap<String, HitboxCollisionConfig>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(HitboxCollisionConfig.class);
      }

      return ASSET_STORE;
   }

   public static IndexedLookupTableAssetMap<String, HitboxCollisionConfig> getAssetMap() {
      return (IndexedLookupTableAssetMap<String, HitboxCollisionConfig>)getAssetStore().getAssetMap();
   }

   public HitboxCollisionConfig(String id) {
      this.id = id;
   }

   public HitboxCollisionConfig() {
   }

   public String getId() {
      return this.id;
   }

   public CollisionType getCollisionType() {
      return this.collisionType;
   }

   public float getSoftOffsetRatio() {
      return this.softOffsetRatio;
   }

   @Nonnull
   public com.hypixel.hytale.protocol.HitboxCollisionConfig toPacket() {
      com.hypixel.hytale.protocol.HitboxCollisionConfig packet = new com.hypixel.hytale.protocol.HitboxCollisionConfig();
      packet.collisionType = this.collisionType;
      packet.softCollisionOffsetRatio = this.softOffsetRatio;
      return packet;
   }

   @Nonnull
   @Override
   public String toString() {
      return "HitboxCollisionConfig{data="
         + this.data
         + ", id='"
         + this.id
         + "', collisionType="
         + this.collisionType
         + ", softOffsetRatio="
         + this.softOffsetRatio
         + "}";
   }
}
