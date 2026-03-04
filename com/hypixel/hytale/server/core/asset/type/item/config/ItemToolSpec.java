package com.hypixel.hytale.server.core.asset.type.item.config;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.codec.AssetCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.asset.type.soundevent.validator.SoundEventValidators;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import java.lang.ref.SoftReference;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemToolSpec
   implements JsonAssetWithMap<String, DefaultAssetMap<String, ItemToolSpec>>,
   NetworkSerializable<com.hypixel.hytale.protocol.ItemToolSpec> {
   public static final AssetCodec<String, ItemToolSpec> CODEC = AssetBuilderCodec.builder(
         ItemToolSpec.class,
         ItemToolSpec::new,
         Codec.STRING,
         (itemToolSpec, s) -> itemToolSpec.gatherType = s,
         itemToolSpec -> itemToolSpec.gatherType,
         (asset, data) -> asset.data = data,
         asset -> asset.data
      )
      .addField(new KeyedCodec<>("GatherType", Codec.STRING), (itemToolSpec, s) -> itemToolSpec.gatherType = s, itemToolSpec -> itemToolSpec.gatherType)
      .addField(new KeyedCodec<>("Power", Codec.DOUBLE), (itemToolSpec, d) -> itemToolSpec.power = d.floatValue(), itemToolSpec -> (double)itemToolSpec.power)
      .addField(new KeyedCodec<>("Quality", Codec.INTEGER), (itemToolSpec, i) -> itemToolSpec.quality = i, itemToolSpec -> itemToolSpec.quality)
      .addField(new KeyedCodec<>("IsIncorrect", Codec.BOOLEAN), (itemToolSpec, i) -> itemToolSpec.incorrect = i, itemToolSpec -> itemToolSpec.incorrect)
      .<String>appendInherited(
         new KeyedCodec<>("HitSoundLayer", Codec.STRING),
         (spec, s) -> spec.hitSoundLayerId = s,
         spec -> spec.hitSoundLayerId,
         (spec, parent) -> spec.hitSoundLayerId = parent.hitSoundLayerId
      )
      .addValidator(SoundEvent.VALIDATOR_CACHE.getValidator())
      .addValidator(SoundEventValidators.MONO)
      .documentation("Sound to play in addition to the block breaking sound when hitting this block type.")
      .add()
      .afterDecode(ItemToolSpec::processConfig)
      .build();
   public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(ItemToolSpec::getAssetStore));
   private static AssetStore<String, ItemToolSpec, DefaultAssetMap<String, ItemToolSpec>> ASSET_STORE;
   protected AssetExtraInfo.Data data;
   protected String gatherType;
   protected float power;
   protected int quality;
   protected boolean incorrect;
   @Nullable
   protected String hitSoundLayerId = null;
   protected transient int hitSoundLayerIndex = 0;
   private SoftReference<com.hypixel.hytale.protocol.ItemToolSpec> cachedPacket;

   public static AssetStore<String, ItemToolSpec, DefaultAssetMap<String, ItemToolSpec>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(ItemToolSpec.class);
      }

      return ASSET_STORE;
   }

   public static DefaultAssetMap<String, ItemToolSpec> getAssetMap() {
      return (DefaultAssetMap<String, ItemToolSpec>)getAssetStore().getAssetMap();
   }

   public ItemToolSpec(String gatherType, float power, int quality) {
      this.gatherType = gatherType;
      this.power = power;
      this.quality = quality;
   }

   protected ItemToolSpec() {
   }

   protected void processConfig() {
      if (this.hitSoundLayerId != null) {
         this.hitSoundLayerIndex = SoundEvent.getAssetMap().getIndex(this.hitSoundLayerId);
      }
   }

   @Nonnull
   public com.hypixel.hytale.protocol.ItemToolSpec toPacket() {
      com.hypixel.hytale.protocol.ItemToolSpec cached = this.cachedPacket == null ? null : this.cachedPacket.get();
      if (cached != null) {
         return cached;
      } else {
         com.hypixel.hytale.protocol.ItemToolSpec packet = new com.hypixel.hytale.protocol.ItemToolSpec();
         packet.gatherType = this.gatherType;
         packet.power = this.power;
         packet.quality = this.quality;
         this.cachedPacket = new SoftReference<>(packet);
         return packet;
      }
   }

   public String getId() {
      return this.gatherType;
   }

   public String getGatherType() {
      return this.gatherType;
   }

   public float getPower() {
      return this.power;
   }

   public int getQuality() {
      return this.quality;
   }

   public boolean isIncorrect() {
      return this.incorrect;
   }

   public int getHitSoundLayerIndex() {
      return this.hitSoundLayerIndex;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ItemToolSpec{gatherType='"
         + this.gatherType
         + "', power="
         + this.power
         + ", quality="
         + this.quality
         + ", incorrect="
         + this.incorrect
         + ", hitSoundLayerId='"
         + this.hitSoundLayerId
         + "', hitSoundLayerIndex="
         + this.hitSoundLayerIndex
         + "}";
   }
}
