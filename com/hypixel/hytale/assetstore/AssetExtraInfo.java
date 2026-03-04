package com.hypixel.hytale.assetstore;

import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.common.util.ArrayUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AssetExtraInfo<K> extends ExtraInfo {
   @Nullable
   private final Path assetPath;
   private final AssetExtraInfo.Data data;

   public AssetExtraInfo(AssetExtraInfo.Data data) {
      super(Integer.MAX_VALUE, AssetValidationResults::new);
      this.assetPath = null;
      this.data = data;
   }

   public AssetExtraInfo(Path assetPath, AssetExtraInfo.Data data) {
      super(Integer.MAX_VALUE, AssetValidationResults::new);
      this.assetPath = assetPath;
      this.data = data;
   }

   @Nonnull
   public String generateKey() {
      return "*" + this.getKey() + "_" + this.peekKey('_');
   }

   public K getKey() {
      return (K)this.getData().getKey();
   }

   @Nullable
   public Path getAssetPath() {
      return this.assetPath;
   }

   public AssetExtraInfo.Data getData() {
      return this.data;
   }

   @Override
   public void appendDetailsTo(@Nonnull StringBuilder sb) {
      sb.append("Id: ").append(this.getKey()).append("\n");
      if (this.assetPath != null) {
         sb.append("Path: ").append(this.assetPath).append("\n");
      }
   }

   public AssetValidationResults getValidationResults() {
      return (AssetValidationResults)super.getValidationResults();
   }

   @Nonnull
   @Override
   public String toString() {
      return "AssetExtraInfo{assetPath=" + this.assetPath + ", data=" + this.data + "} " + super.toString();
   }

   public static class Data {
      public static final char TAG_VALUE_SEPARATOR = '=';
      private Map<Class<? extends JsonAssetWithMap>, List<Object>> containedAssets;
      private Map<Class<? extends JsonAssetWithMap>, List<RawAsset<Object>>> containedRawAssets;
      @Nullable
      private AssetExtraInfo.Data containerData;
      private Class<? extends JsonAsset<?>> assetClass;
      private Object key;
      private Object parentKey;
      private final Map<String, String[]> rawTags = new HashMap<>(0);
      private final Int2ObjectMap<IntSet> tagStorage = new Int2ObjectOpenHashMap(0);
      private final Int2ObjectMap<IntSet> unmodifiableTagStorage = new Int2ObjectOpenHashMap(0);
      private final IntSet expandedTagStorage = new IntOpenHashSet(0);
      private final IntSet unmodifiableExpandedTagStorage = IntSets.unmodifiable(this.expandedTagStorage);

      public <K> Data(Class<? extends JsonAsset<K>> assetClass, K key, K parentKey) {
         this.assetClass = assetClass;
         this.key = key;
         this.parentKey = parentKey;
      }

      public <K> Data(@Nullable AssetExtraInfo.Data containerData, Class<? extends JsonAsset<K>> aClass, K key, K parentKey, boolean inheritContainerTags) {
         this(aClass, key, parentKey);
         this.containerData = containerData;
         if (containerData != null && inheritContainerTags) {
            this.putTags(containerData.rawTags);
         }
      }

      public Class<? extends JsonAsset<?>> getAssetClass() {
         return this.assetClass;
      }

      public Object getKey() {
         return this.key;
      }

      public Object getParentKey() {
         return this.parentKey;
      }

      @Nonnull
      public AssetExtraInfo.Data getRootContainerData() {
         AssetExtraInfo.Data temp = this;

         while (temp.containerData != null) {
            temp = temp.containerData;
         }

         return temp;
      }

      @Nullable
      public AssetExtraInfo.Data getContainerData() {
         return this.containerData;
      }

      @Nullable
      public <K> K getContainerKey(Class<? extends JsonAsset<K>> aClass) {
         if (this.containerData == null) {
            return null;
         } else {
            return (K)(this.containerData.assetClass.equals(aClass) ? this.containerData.key : this.containerData.getContainerKey(aClass));
         }
      }

      public void putTags(@Nonnull Map<String, String[]> tags) {
         for (Entry<String, String[]> entry : tags.entrySet()) {
            String tag = entry.getKey().intern();
            this.rawTags.merge(tag, entry.getValue(), ArrayUtil::combine);
            IntSet tagIndexes = this.ensureTag(tag);

            for (String value : entry.getValue()) {
               tagIndexes.add(AssetRegistry.getOrCreateTagIndex(value));
               this.ensureTag(value);
               this.rawTags.putIfAbsent(value, ArrayUtil.EMPTY_STRING_ARRAY);
               String valueTag = (tag + "=" + value).intern();
               this.rawTags.putIfAbsent(valueTag, ArrayUtil.EMPTY_STRING_ARRAY);
               this.ensureTag(valueTag);
            }
         }
      }

      @Nonnull
      public Map<String, String[]> getRawTags() {
         return Collections.unmodifiableMap(this.rawTags);
      }

      @Nonnull
      public Int2ObjectMap<IntSet> getTags() {
         return Int2ObjectMaps.unmodifiable(this.unmodifiableTagStorage);
      }

      @Nonnull
      public IntSet getExpandedTagIndexes() {
         return this.unmodifiableExpandedTagStorage;
      }

      public IntSet getTag(int tagIndex) {
         return (IntSet)this.unmodifiableTagStorage.getOrDefault(tagIndex, IntSets.EMPTY_SET);
      }

      public <K, T extends JsonAssetWithMap<K, M>, M extends AssetMap<K, T>> void addContainedAsset(Class<T> assetClass, T asset) {
         if (this.containedAssets == null) {
            this.containedAssets = new HashMap<>();
         }

         this.containedAssets.computeIfAbsent(assetClass, k -> new ArrayList<>()).add(asset);
      }

      public <K, T extends JsonAssetWithMap<K, M>, M extends AssetMap<K, T>> void addContainedAsset(Class<T> assetClass, RawAsset<K> rawAsset) {
         if (this.containedRawAssets == null) {
            this.containedRawAssets = new HashMap<>();
         }

         this.containedRawAssets.computeIfAbsent(assetClass, k -> new ArrayList<>()).add(rawAsset);
      }

      public <K> void fetchContainedAssets(K key, @Nonnull Map<Class<? extends JsonAssetWithMap>, Map<K, List<Object>>> containedAssets) {
         if (this.containedAssets != null) {
            for (Entry<Class<? extends JsonAssetWithMap>, List<Object>> entry : this.containedAssets.entrySet()) {
               containedAssets.computeIfAbsent(entry.getKey(), k -> new HashMap<>()).computeIfAbsent(key, k -> new ArrayList<>(3)).addAll(entry.getValue());
            }
         }
      }

      public <K> void fetchContainedRawAssets(K key, @Nonnull Map<Class<? extends JsonAssetWithMap>, Map<K, List<RawAsset<Object>>>> containedAssets) {
         if (this.containedRawAssets != null) {
            for (Entry<Class<? extends JsonAssetWithMap>, List<RawAsset<Object>>> entry : this.containedRawAssets.entrySet()) {
               containedAssets.computeIfAbsent(entry.getKey(), k -> new HashMap<>()).computeIfAbsent(key, k -> new ArrayList<>(3)).addAll(entry.getValue());
            }
         }
      }

      public <K, T extends JsonAssetWithMap<K, M>, M extends AssetMap<K, T>> boolean containsAsset(Class<T> tClass, K key) {
         if (this.containedAssets != null) {
            List<T> assets = (List<T>)this.containedAssets.get(tClass);
            if (assets != null) {
               Function<T, K> keyFunction = AssetRegistry.<K, T, M>getAssetStore(tClass).getKeyFunction();

               for (T asset : assets) {
                  if (key.equals(keyFunction.apply(asset))) {
                     return true;
                  }
               }
            }
         }

         if (this.containedRawAssets != null) {
            List<RawAsset<T>> rawAssets = (List<RawAsset<T>>)this.containedRawAssets.get(tClass);
            if (rawAssets != null) {
               for (RawAsset<T> rawAsset : rawAssets) {
                  if (key.equals(rawAsset.getKey())) {
                     return true;
                  }
               }
            }
         }

         return false;
      }

      public void loadContainedAssets(boolean reloading) {
         if (this.containedAssets != null) {
            for (Entry<Class<? extends JsonAssetWithMap>, List<Object>> entry : this.containedAssets.entrySet()) {
               AssetRegistry.getAssetStore(entry.getKey()).loadAssets("Hytale:Hytale", entry.getValue(), AssetUpdateQuery.DEFAULT, reloading);
            }
         }

         if (this.containedRawAssets != null) {
            for (Entry<Class<? extends JsonAssetWithMap>, List<RawAsset<Object>>> entry : this.containedRawAssets.entrySet()) {
               AssetRegistry.getAssetStore(entry.getKey()).loadBuffersWithKeys("Hytale:Hytale", entry.getValue(), AssetUpdateQuery.DEFAULT, reloading);
            }
         }
      }

      @Nonnull
      private IntSet ensureTag(@Nonnull String tag) {
         int idx = AssetRegistry.getOrCreateTagIndex(tag);
         this.expandedTagStorage.add(idx);
         return (IntSet)this.tagStorage.computeIfAbsent(idx, k -> {
            IntSet set = new IntOpenHashSet(3);
            this.unmodifiableTagStorage.put(k, IntSets.unmodifiable(set));
            return set;
         });
      }

      @Nonnull
      @Override
      public String toString() {
         return "Data{containedAssets="
            + this.containedRawAssets
            + ", rawTags="
            + this.rawTags
            + ", parent="
            + this.containerData
            + ", assetClass="
            + this.assetClass
            + ", key="
            + this.key
            + "}";
      }
   }
}
