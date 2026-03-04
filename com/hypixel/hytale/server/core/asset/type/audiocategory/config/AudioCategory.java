package com.hypixel.hytale.server.core.asset.type.audiocategory.config;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditor;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.common.util.AudioUtil;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import java.lang.ref.SoftReference;
import javax.annotation.Nonnull;

public class AudioCategory
   implements JsonAssetWithMap<String, IndexedLookupTableAssetMap<String, AudioCategory>>,
   NetworkSerializable<com.hypixel.hytale.protocol.AudioCategory> {
   public static final int EMPTY_ID = 0;
   public static final String EMPTY = "EMPTY";
   public static final AudioCategory EMPTY_AUDIO_CATEGORY = new AudioCategory("EMPTY");
   public static final AssetBuilderCodec<String, AudioCategory> CODEC = AssetBuilderCodec.builder(
         AudioCategory.class, AudioCategory::new, Codec.STRING, (t, k) -> t.id = k, t -> t.id, (asset, data) -> asset.data = data, asset -> asset.data
      )
      .documentation(
         "An asset used to define an audio category. Can be used to adjust the volume of all sound events that reference a given category. Note: When using an inheritance structure, these categories act a bit like an audio bus where the category's volume is combined with the volumes further up in the hierarchy. e.g. if the category's volume is 4dB and the parent is -2dB, the final volume will be 2dB."
      )
      .<Float>appendInherited(
         new KeyedCodec<>("Volume", Codec.FLOAT),
         (category, f) -> category.volume = AudioUtil.decibelsToLinearGain(f),
         category -> AudioUtil.linearGainToDecibels(category.volume),
         (category, parent) -> category.volume = parent.volume
      )
      .metadata(new UIEditor(new UIEditor.FormattedNumber(null, " dB", null)))
      .addValidator(Validators.range(-100.0F, 10.0F))
      .documentation("Volume adjustment for the audio category in decibels.")
      .add()
      .build();
   public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(AudioCategory::getAssetStore));
   private static AssetStore<String, AudioCategory, IndexedLookupTableAssetMap<String, AudioCategory>> ASSET_STORE;
   protected AssetExtraInfo.Data data;
   protected String id;
   protected float volume = AudioUtil.decibelsToLinearGain(0.0F);
   private SoftReference<com.hypixel.hytale.protocol.AudioCategory> cachedPacket;

   public static AssetStore<String, AudioCategory, IndexedLookupTableAssetMap<String, AudioCategory>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(AudioCategory.class);
      }

      return ASSET_STORE;
   }

   public static IndexedLookupTableAssetMap<String, AudioCategory> getAssetMap() {
      return (IndexedLookupTableAssetMap<String, AudioCategory>)getAssetStore().getAssetMap();
   }

   public AudioCategory(String id) {
      this.id = id;
   }

   protected AudioCategory() {
   }

   public String getId() {
      return this.id;
   }

   public float getVolume() {
      return this.volume;
   }

   @Nonnull
   @Override
   public String toString() {
      return "AudioCategory{id='" + this.id + "', volume=" + this.volume + "}";
   }

   @Nonnull
   public com.hypixel.hytale.protocol.AudioCategory toPacket() {
      com.hypixel.hytale.protocol.AudioCategory cached = this.cachedPacket == null ? null : this.cachedPacket.get();
      if (cached != null) {
         return cached;
      } else {
         com.hypixel.hytale.protocol.AudioCategory packet = new com.hypixel.hytale.protocol.AudioCategory();
         packet.id = this.id;
         packet.volume = this.volume;
         AssetExtraInfo.Data parentData = this.data;

         while (parentData != null) {
            String parentKey = ASSET_STORE.transformKey(parentData.getParentKey());
            if (parentKey == null) {
               break;
            }

            AudioCategory parent = (AudioCategory)((IndexedLookupTableAssetMap)ASSET_STORE.getAssetMap()).getAsset(parentKey);
            if (parent == null) {
               break;
            }

            packet.volume = packet.volume * parent.volume;
            parentData = parent.data;
         }

         this.cachedPacket = new SoftReference<>(packet);
         return packet;
      }
   }
}
