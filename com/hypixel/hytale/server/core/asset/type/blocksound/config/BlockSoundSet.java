package com.hypixel.hytale.server.core.asset.type.blocksound.config;

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
import com.hypixel.hytale.math.range.FloatRange;
import com.hypixel.hytale.protocol.BlockSoundEvent;
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

public class BlockSoundSet
   implements JsonAssetWithMap<String, IndexedLookupTableAssetMap<String, BlockSoundSet>>,
   NetworkSerializable<com.hypixel.hytale.protocol.BlockSoundSet> {
   public static final int EMPTY_ID = 0;
   public static final String EMPTY = "EMPTY";
   private static final FloatRange DEFAULT_MOVE_IN_REPEAT_RANGE = new FloatRange(0.5F, 1.5F);
   public static final BlockSoundSet EMPTY_BLOCK_SOUND_SET = new BlockSoundSet("EMPTY");
   public static final AssetBuilderCodec<String, BlockSoundSet> CODEC = AssetBuilderCodec.builder(
         BlockSoundSet.class,
         BlockSoundSet::new,
         Codec.STRING,
         (blockSounds, s) -> blockSounds.id = s,
         blockSounds -> blockSounds.id,
         (asset, data) -> asset.data = data,
         asset -> asset.data
      )
      .appendInherited(
         new KeyedCodec<>("SoundEvents", new EnumMapCodec<>(BlockSoundEvent.class, Codec.STRING)),
         (blockParticleSet, s) -> blockParticleSet.soundEventIds = s,
         blockParticleSet -> blockParticleSet.soundEventIds,
         (blockParticleSet, parent) -> blockParticleSet.soundEventIds = parent.soundEventIds
      )
      .addValidator(Validators.nonNull())
      .addValidator(SoundEvent.VALIDATOR_CACHE.getMapValueValidator())
      .addValidator(SoundEventValidators.MONO_VALIDATOR_CACHE.getMapValueValidator())
      .addValidator(SoundEventValidators.ONESHOT_VALIDATOR_CACHE.getMapValueValidator())
      .metadata(UIDefaultCollapsedState.UNCOLLAPSED)
      .add()
      .appendInherited(
         new KeyedCodec<>("MoveInRepeatRange", FloatRange.CODEC),
         (blockSounds, f) -> blockSounds.moveInRepeatRange = f,
         blockSounds -> blockSounds.moveInRepeatRange,
         (blockSounds, parent) -> blockSounds.moveInRepeatRange = parent.moveInRepeatRange
      )
      .add()
      .afterDecode(BlockSoundSet::processConfig)
      .build();
   public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(BlockSoundSet::getAssetStore));
   private static AssetStore<String, BlockSoundSet, IndexedLookupTableAssetMap<String, BlockSoundSet>> ASSET_STORE;
   protected AssetExtraInfo.Data data;
   protected String id;
   protected Map<BlockSoundEvent, String> soundEventIds = Collections.emptyMap();
   protected transient Object2IntMap<BlockSoundEvent> soundEventIndices = Object2IntMaps.emptyMap();
   protected FloatRange moveInRepeatRange = DEFAULT_MOVE_IN_REPEAT_RANGE;
   private SoftReference<com.hypixel.hytale.protocol.BlockSoundSet> cachedPacket;

   public static AssetStore<String, BlockSoundSet, IndexedLookupTableAssetMap<String, BlockSoundSet>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(BlockSoundSet.class);
      }

      return ASSET_STORE;
   }

   public static IndexedLookupTableAssetMap<String, BlockSoundSet> getAssetMap() {
      return (IndexedLookupTableAssetMap<String, BlockSoundSet>)getAssetStore().getAssetMap();
   }

   public BlockSoundSet(String id, Map<BlockSoundEvent, String> soundEventIds) {
      this.id = id;
      this.soundEventIds = soundEventIds;
   }

   public BlockSoundSet(String id) {
      this.id = id;
   }

   protected BlockSoundSet() {
   }

   @Nonnull
   public com.hypixel.hytale.protocol.BlockSoundSet toPacket() {
      com.hypixel.hytale.protocol.BlockSoundSet cached = this.cachedPacket == null ? null : this.cachedPacket.get();
      if (cached != null) {
         return cached;
      } else {
         com.hypixel.hytale.protocol.BlockSoundSet packet = new com.hypixel.hytale.protocol.BlockSoundSet();
         packet.id = this.id;
         packet.soundEventIndices = this.soundEventIndices;
         packet.moveInRepeatRange = new com.hypixel.hytale.protocol.FloatRange(
            this.moveInRepeatRange.getInclusiveMin(), this.moveInRepeatRange.getInclusiveMax()
         );
         this.cachedPacket = new SoftReference<>(packet);
         return packet;
      }
   }

   public String getId() {
      return this.id;
   }

   public Map<BlockSoundEvent, String> getSoundEventIds() {
      return this.soundEventIds;
   }

   public Object2IntMap<BlockSoundEvent> getSoundEventIndices() {
      return this.soundEventIndices;
   }

   public FloatRange getMoveInRepeatRange() {
      return this.moveInRepeatRange;
   }

   protected void processConfig() {
      if (!this.soundEventIds.isEmpty()) {
         this.soundEventIndices = new Object2IntOpenHashMap();

         for (Entry<BlockSoundEvent, String> entry : this.soundEventIds.entrySet()) {
            this.soundEventIndices.put(entry.getKey(), SoundEvent.getAssetMap().getIndex(entry.getValue()));
         }
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "BlockSoundSet{id='"
         + this.id
         + "', soundEventIds="
         + this.soundEventIds
         + ", soundEventIndices="
         + this.soundEventIndices
         + ", moveInRepeatRange="
         + this.moveInRepeatRange
         + "}";
   }
}
