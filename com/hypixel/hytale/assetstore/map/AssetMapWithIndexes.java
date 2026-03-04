package com.hypixel.hytale.assetstore.map;

import com.hypixel.fastutil.ints.Int2ObjectConcurrentHashMap;
import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.JsonAsset;
import com.hypixel.hytale.assetstore.codec.AssetCodec;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import java.util.Map;
import javax.annotation.Nonnull;

public abstract class AssetMapWithIndexes<K, T extends JsonAsset<K>> extends DefaultAssetMap<K, T> {
   public static final int NOT_FOUND = Integer.MIN_VALUE;
   protected final Int2ObjectConcurrentHashMap<IntSet> indexedTagStorage = new Int2ObjectConcurrentHashMap<>();
   protected final Int2ObjectConcurrentHashMap<IntSet> unmodifiableIndexedTagStorage = new Int2ObjectConcurrentHashMap<>();

   @Override
   protected void clear() {
      super.clear();
      this.indexedTagStorage.clear();
      this.unmodifiableIndexedTagStorage.clear();
   }

   public IntSet getIndexesForTag(int index) {
      return this.unmodifiableIndexedTagStorage.getOrDefault(index, IntSets.EMPTY_SET);
   }

   @Override
   protected void putAssetTags(AssetCodec<K, T> codec, Map<K, T> loadedAssets) {
   }

   protected void putAssetTag(@Nonnull AssetCodec<K, T> codec, K key, int index, T value) {
      AssetExtraInfo.Data data = codec.getData(value);
      if (data != null) {
         IntIterator iterator = data.getExpandedTagIndexes().iterator();

         while (iterator.hasNext()) {
            int tag = iterator.nextInt();
            this.putAssetTag(key, index, tag);
         }
      }
   }

   protected void putAssetTag(K key, int index, int tag) {
      this.putAssetTag(key, tag);
      this.indexedTagStorage.computeIfAbsent(tag, k -> {
         IntSet set = Int2ObjectConcurrentHashMap.newKeySet(3);
         this.unmodifiableIndexedTagStorage.put(k, IntSets.unmodifiable(set));
         return set;
      }).add(index);
   }

   @Override
   public boolean requireReplaceOnRemove() {
      return true;
   }
}
