package com.hypixel.hytale.server.core.asset.type.physicalmaterial.config;

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
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import java.lang.ref.SoftReference;
import javax.annotation.Nonnull;

public class PhysicalMaterial
   implements JsonAssetWithMap<String, IndexedLookupTableAssetMap<String, PhysicalMaterial>>,
   NetworkSerializable<com.hypixel.hytale.protocol.PhysicalMaterial> {
   public static final int EMPTY_ID = 0;
   public static final String EMPTY = "EMPTY";
   public static final PhysicalMaterial EMPTY_PHYSICAL_MATERIAL = new PhysicalMaterial("EMPTY");
   public static final AssetBuilderCodec<String, PhysicalMaterial> CODEC = AssetBuilderCodec.builder(
         PhysicalMaterial.class,
         PhysicalMaterial::new,
         Codec.STRING,
         (mat, s) -> mat.id = s,
         mat -> mat.id,
         (asset, data) -> asset.data = data,
         asset -> asset.data
      )
      .appendInherited(
         new KeyedCodec<>("ReflectionCoeff", Codec.FLOAT),
         (mat, v) -> mat.reflectionCoeff = v,
         mat -> mat.reflectionCoeff,
         (mat, parent) -> mat.reflectionCoeff = parent.reflectionCoeff
      )
      .addValidator(Validators.range(0.0F, 1.0F))
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("AttenuationPerBlock", Codec.FLOAT),
         (mat, v) -> mat.attenuationPerBlock = v,
         mat -> mat.attenuationPerBlock,
         (mat, parent) -> mat.attenuationPerBlock = parent.attenuationPerBlock
      )
      .addValidator(Validators.min(0.0F))
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("HFAttenuationPerBlock", Codec.FLOAT),
         (mat, v) -> mat.hfAttenuationPerBlock = v,
         mat -> mat.hfAttenuationPerBlock,
         (mat, parent) -> mat.hfAttenuationPerBlock = parent.hfAttenuationPerBlock
      )
      .addValidator(Validators.min(0.0F))
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("ShelterOpacity", Codec.FLOAT),
         (mat, v) -> mat.shelterOpacity = v,
         mat -> mat.shelterOpacity,
         (mat, parent) -> mat.shelterOpacity = parent.shelterOpacity
      )
      .addValidator(Validators.range(0.0F, 1.0F))
      .add()
      .afterDecode(PhysicalMaterial::processConfig)
      .build();
   public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(PhysicalMaterial::getAssetStore));
   private static AssetStore<String, PhysicalMaterial, IndexedLookupTableAssetMap<String, PhysicalMaterial>> ASSET_STORE;
   protected AssetExtraInfo.Data data;
   protected String id;
   protected float reflectionCoeff;
   protected float attenuationPerBlock;
   protected float hfAttenuationPerBlock;
   protected float shelterOpacity;
   private SoftReference<com.hypixel.hytale.protocol.PhysicalMaterial> cachedPacket;

   public static AssetStore<String, PhysicalMaterial, IndexedLookupTableAssetMap<String, PhysicalMaterial>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(PhysicalMaterial.class);
      }

      return ASSET_STORE;
   }

   public static IndexedLookupTableAssetMap<String, PhysicalMaterial> getAssetMap() {
      return (IndexedLookupTableAssetMap<String, PhysicalMaterial>)getAssetStore().getAssetMap();
   }

   public PhysicalMaterial(String id) {
      this.id = id;
   }

   protected PhysicalMaterial() {
   }

   @Nonnull
   public com.hypixel.hytale.protocol.PhysicalMaterial toPacket() {
      com.hypixel.hytale.protocol.PhysicalMaterial cached = this.cachedPacket == null ? null : this.cachedPacket.get();
      if (cached != null) {
         return cached;
      } else {
         com.hypixel.hytale.protocol.PhysicalMaterial packet = new com.hypixel.hytale.protocol.PhysicalMaterial();
         packet.id = this.id;
         packet.reflectionCoeff = this.reflectionCoeff;
         packet.attenuationPerBlock = this.attenuationPerBlock;
         packet.hFAttenuationPerBlock = this.hfAttenuationPerBlock;
         packet.shelterOpacity = this.shelterOpacity;
         this.cachedPacket = new SoftReference<>(packet);
         return packet;
      }
   }

   public String getId() {
      return this.id;
   }

   public float getReflectionCoeff() {
      return this.reflectionCoeff;
   }

   public float getAttenuationPerBlock() {
      return this.attenuationPerBlock;
   }

   public float getHfAttenuationPerBlock() {
      return this.hfAttenuationPerBlock;
   }

   public float getShelterOpacity() {
      return this.shelterOpacity;
   }

   protected void processConfig() {
   }

   @Nonnull
   @Override
   public String toString() {
      return "PhysicalMaterial{id='"
         + this.id
         + "', reflectionCoeff="
         + this.reflectionCoeff
         + ", attenuationPerBlock="
         + this.attenuationPerBlock
         + ", hfAttenuationPerBlock="
         + this.hfAttenuationPerBlock
         + ", shelterOpacity="
         + this.shelterOpacity
         + "}";
   }
}
