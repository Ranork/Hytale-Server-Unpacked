package com.hypixel.hytale.server.core.modules.entity.repulsion;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import javax.annotation.Nonnull;

public class RepulsionConfig
   implements JsonAssetWithMap<String, IndexedLookupTableAssetMap<String, RepulsionConfig>>,
   NetworkSerializable<com.hypixel.hytale.protocol.RepulsionConfig> {
   public static final AssetBuilderCodec<String, RepulsionConfig> CODEC = AssetBuilderCodec.builder(
         RepulsionConfig.class,
         RepulsionConfig::new,
         Codec.STRING,
         (repulsion, s) -> repulsion.id = s,
         repulsion -> repulsion.id,
         (repulsion, data) -> repulsion.data = data,
         repulsion -> repulsion.data
      )
      .appendInherited(
         new KeyedCodec<>("Radius", Codec.FLOAT),
         (repulsion, radius) -> repulsion.radius = radius,
         repulsion -> repulsion.radius,
         (repulsion, parent) -> repulsion.radius = parent.radius
      )
      .documentation("The radius around the entity")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("MinForce", Codec.FLOAT),
         (repulsion, minForce) -> repulsion.minForce = minForce,
         repulsion -> repulsion.minForce,
         (repulsion, parent) -> repulsion.minForce = parent.minForce
      )
      .documentation("The floor of the applied force while within effective radius")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("MaxForce", Codec.FLOAT),
         (repulsion, maxForce) -> repulsion.maxForce = maxForce,
         repulsion -> repulsion.maxForce,
         (repulsion, parent) -> repulsion.maxForce = parent.maxForce
      )
      .documentation("The max force to be applied at 100% intersection")
      .add()
      .build();
   public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(RepulsionConfig::getAssetStore));
   private static AssetStore<String, RepulsionConfig, IndexedLookupTableAssetMap<String, RepulsionConfig>> ASSET_STORE;
   public static final int NO_REPULSION = -1;
   protected AssetExtraInfo.Data data;
   protected String id;
   protected float radius;
   protected float minForce;
   protected float maxForce;

   public static AssetStore<String, RepulsionConfig, IndexedLookupTableAssetMap<String, RepulsionConfig>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(RepulsionConfig.class);
      }

      return ASSET_STORE;
   }

   public static IndexedLookupTableAssetMap<String, RepulsionConfig> getAssetMap() {
      return (IndexedLookupTableAssetMap<String, RepulsionConfig>)getAssetStore().getAssetMap();
   }

   public RepulsionConfig() {
   }

   public RepulsionConfig(String id) {
      this.id = id;
   }

   public RepulsionConfig(@Nonnull RepulsionConfig repulsion) {
      this(repulsion.radius, repulsion.minForce, repulsion.maxForce);
   }

   public RepulsionConfig(float radius, float maxForce) {
      this(radius, 0.0F, maxForce);
   }

   public RepulsionConfig(float radius, float minForce, float maxForce) {
      this.radius = radius;
      this.minForce = minForce;
      this.maxForce = maxForce;
   }

   public String getId() {
      return this.id;
   }

   @Nonnull
   public com.hypixel.hytale.protocol.RepulsionConfig toPacket() {
      com.hypixel.hytale.protocol.RepulsionConfig packet = new com.hypixel.hytale.protocol.RepulsionConfig();
      packet.radius = this.radius;
      packet.minForce = this.minForce;
      packet.maxForce = this.maxForce;
      return packet;
   }

   @Nonnull
   @Override
   public String toString() {
      return "repulsionConfig{data="
         + this.data
         + ", id='"
         + this.id
         + "', radius="
         + this.radius
         + ", minForce="
         + this.minForce
         + ", maxForce="
         + this.maxForce
         + "}";
   }
}
