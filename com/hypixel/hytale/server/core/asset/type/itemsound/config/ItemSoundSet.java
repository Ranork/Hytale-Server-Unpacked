package com.hypixel.hytale.server.core.asset.type.itemsound.config;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.map.EnumMapCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIDefaultCollapsedState;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.protocol.ItemSoundEvent;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.asset.type.soundevent.validator.SoundEventValidators;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;

public class ItemSoundSet
   implements JsonAssetWithMap<String, IndexedLookupTableAssetMap<String, ItemSoundSet>>,
   NetworkSerializable<com.hypixel.hytale.protocol.ItemSoundSet> {
   public static final AssetBuilderCodec<String, ItemSoundSet> CODEC = AssetBuilderCodec.builder(
         ItemSoundSet.class,
         ItemSoundSet::new,
         Codec.STRING,
         (itemSounds, s) -> itemSounds.id = s,
         itemSounds -> itemSounds.id,
         (asset, data) -> asset.data = data,
         asset -> asset.data
      )
      .appendInherited(
         new KeyedCodec<>("SoundEvents", new EnumMapCodec<>(ItemSoundEvent.class, Codec.STRING)),
         (itemParticleSet, s) -> itemParticleSet.soundEventIds = s,
         itemParticleSet -> itemParticleSet.soundEventIds,
         (itemParticleSet, parent) -> itemParticleSet.soundEventIds = parent.soundEventIds
      )
      .addValidator(Validators.nonNull())
      .addValidator(SoundEvent.VALIDATOR_CACHE.getMapValueValidator())
      .addValidator(SoundEventValidators.STEREO_VALIDATOR_CACHE.getMapValueValidator())
      .addValidator(SoundEventValidators.ONESHOT_VALIDATOR_CACHE.getMapValueValidator())
      .metadata(UIDefaultCollapsedState.UNCOLLAPSED)
      .add()
      .afterDecode(ItemSoundSet::processConfig)
      .build();
   public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(ItemSoundSet::getAssetStore));
   private static AssetStore<String, ItemSoundSet, IndexedLookupTableAssetMap<String, ItemSoundSet>> ASSET_STORE;
   protected AssetExtraInfo.Data data;
   protected String id;
   protected Map<ItemSoundEvent, String> soundEventIds = Collections.emptyMap();
   protected transient Object2IntMap<ItemSoundEvent> soundEventIndices = Object2IntMaps.emptyMap();
   private SoftReference<com.hypixel.hytale.protocol.ItemSoundSet> cachedPacket;

   public static AssetStore<String, ItemSoundSet, IndexedLookupTableAssetMap<String, ItemSoundSet>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(ItemSoundSet.class);
      }

      return ASSET_STORE;
   }

   public static IndexedLookupTableAssetMap<String, ItemSoundSet> getAssetMap() {
      return (IndexedLookupTableAssetMap<String, ItemSoundSet>)getAssetStore().getAssetMap();
   }

   public ItemSoundSet(String id, Map<ItemSoundEvent, String> soundEventIds) {
      this.id = id;
      this.soundEventIds = soundEventIds;
   }

   public ItemSoundSet(String id) {
      this.id = id;
   }

   protected ItemSoundSet() {
   }

   @Nonnull
   public com.hypixel.hytale.protocol.ItemSoundSet toPacket() {
      com.hypixel.hytale.protocol.ItemSoundSet cached = this.cachedPacket == null ? null : this.cachedPacket.get();
      if (cached != null) {
         return cached;
      } else {
         com.hypixel.hytale.protocol.ItemSoundSet packet = new com.hypixel.hytale.protocol.ItemSoundSet();
         packet.id = this.id;
         packet.soundEventIndices = this.soundEventIndices;
         this.cachedPacket = new SoftReference<>(packet);
         return packet;
      }
   }

   public String getId() {
      return this.id;
   }

   public Map<ItemSoundEvent, String> getSoundEventIds() {
      return this.soundEventIds;
   }

   public Object2IntMap<ItemSoundEvent> getSoundEventIndices() {
      return this.soundEventIndices;
   }

   protected void processConfig() {
      if (!this.soundEventIds.isEmpty()) {
         this.soundEventIndices = new Object2IntOpenHashMap();

         for (Entry<ItemSoundEvent, String> entry : this.soundEventIds.entrySet()) {
            this.soundEventIndices.put(entry.getKey(), SoundEvent.getAssetMap().getIndex(entry.getValue()));
         }
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "ItemSoundSet{id='" + this.id + "', soundEventIds=" + this.soundEventIds + ", soundEventIndices=" + this.soundEventIndices + "}";
   }
}
