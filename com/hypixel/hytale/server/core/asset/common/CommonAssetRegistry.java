package com.hypixel.hytale.server.core.asset.common;

import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.common.util.PatternUtil;
import it.unimi.dsi.fastutil.booleans.BooleanObjectPair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CommonAssetRegistry {
   private static final Map<String, List<CommonAssetRegistry.PackAsset>> assetByNameMap = new ConcurrentHashMap<>();
   private static final Map<String, List<CommonAssetRegistry.PackAsset>> assetByHashMap = new ConcurrentHashMap<>();
   private static final AtomicInteger duplicateAssetCount = new AtomicInteger();
   private static final Collection<List<CommonAssetRegistry.PackAsset>> unmodifiableAssetByNameMapValues = Collections.unmodifiableCollection(
      assetByNameMap.values()
   );

   public static int getDuplicateAssetCount() {
      return duplicateAssetCount.get();
   }

   @Nonnull
   public static Map<String, List<CommonAssetRegistry.PackAsset>> getDuplicatedAssets() {
      Map<String, List<CommonAssetRegistry.PackAsset>> duplicates = new Object2ObjectOpenHashMap();

      for (Entry<String, List<CommonAssetRegistry.PackAsset>> entry : assetByHashMap.entrySet()) {
         if (entry.getValue().size() > 1) {
            duplicates.put(entry.getKey(), new ObjectArrayList(entry.getValue()));
         }
      }

      return duplicates;
   }

   @Nonnull
   public static Collection<List<CommonAssetRegistry.PackAsset>> getAllAssets() {
      return unmodifiableAssetByNameMapValues;
   }

   public static void clearAllAssets() {
      assetByNameMap.clear();
      assetByHashMap.clear();
   }

   @Nonnull
   public static CommonAssetRegistry.AddCommonAssetResult addCommonAsset(String pack, @Nonnull CommonAsset asset) {
      CommonAssetRegistry.AddCommonAssetResult result = new CommonAssetRegistry.AddCommonAssetResult();
      result.newPackAsset = new CommonAssetRegistry.PackAsset(pack, asset);
      List<CommonAssetRegistry.PackAsset> list = assetByNameMap.computeIfAbsent(asset.getName(), v -> new CopyOnWriteArrayList<>());
      boolean added = false;
      boolean addHash = true;

      for (int i = 0; i < list.size(); i++) {
         CommonAssetRegistry.PackAsset e = list.get(i);
         if (e.pack().equals(pack)) {
            result.previousNameAsset = e;
            if (i == list.size() - 1) {
               assetByHashMap.get(e.asset.getHash()).remove(e);
               assetByHashMap.compute(e.asset.getHash(), (k, v) -> v != null && !v.isEmpty() ? v : null);
            } else {
               addHash = false;
            }

            list.set(i, result.newPackAsset);
            added = true;
            break;
         }
      }

      if (!added) {
         if (!list.isEmpty()) {
            CommonAssetRegistry.PackAsset e = list.getLast();
            assetByHashMap.get(e.asset.getHash()).remove(e);
            assetByHashMap.compute(e.asset.getHash(), (k, v) -> v != null && !v.isEmpty() ? v : null);
            result.previousNameAsset = e;
         }

         list.add(result.newPackAsset);
      }

      if (addHash) {
         List<CommonAssetRegistry.PackAsset> commonAssets = assetByHashMap.computeIfAbsent(asset.getHash(), k -> new CopyOnWriteArrayList<>());
         if (!commonAssets.isEmpty()) {
            result.previousHashAssets = commonAssets.toArray(CommonAssetRegistry.PackAsset[]::new);
         }

         commonAssets.add(result.newPackAsset);
      }

      if (result.previousHashAssets != null || result.previousNameAsset != null) {
         result.duplicateAssetId = duplicateAssetCount.getAndIncrement();
      }

      result.activeAsset = list.getLast();
      return result;
   }

   @Nullable
   public static BooleanObjectPair<CommonAssetRegistry.PackAsset> removeCommonAssetByName(String pack, String name) {
      name = PatternUtil.replaceBackslashWithForwardSlash(name);
      List<CommonAssetRegistry.PackAsset> oldAssets = assetByNameMap.get(name);
      if (oldAssets == null) {
         return null;
      } else {
         CommonAssetRegistry.PackAsset previousCurrent = oldAssets.getLast();
         oldAssets.removeIf(v -> v.pack().equals(pack));
         assetByNameMap.compute(name, (k, v) -> v != null && !v.isEmpty() ? v : null);
         if (oldAssets.isEmpty()) {
            removeCommonAssetByHash0(previousCurrent);
            return BooleanObjectPair.of(false, previousCurrent);
         } else {
            CommonAssetRegistry.PackAsset newCurrent = oldAssets.getLast();
            if (newCurrent.equals(previousCurrent)) {
               return null;
            } else {
               removeCommonAssetByHash0(previousCurrent);
               assetByHashMap.computeIfAbsent(newCurrent.asset.getHash(), v -> new CopyOnWriteArrayList<>()).add(newCurrent);
               return BooleanObjectPair.of(true, newCurrent);
            }
         }
      }
   }

   @Nonnull
   public static List<CommonAsset> getCommonAssetsStartingWith(String pack, String name) {
      List<CommonAsset> oldAssets = new ObjectArrayList();

      for (List<CommonAssetRegistry.PackAsset> assets : assetByNameMap.values()) {
         for (CommonAssetRegistry.PackAsset asset : assets) {
            if (asset.asset().getName().startsWith(name) && asset.pack().equals(pack)) {
               oldAssets.add(asset.asset());
            }
         }
      }

      return oldAssets;
   }

   public static boolean hasCommonAsset(String name) {
      return assetByNameMap.containsKey(name);
   }

   public static boolean hasCommonAsset(AssetPack pack, String name) {
      List<CommonAssetRegistry.PackAsset> packAssets = assetByNameMap.get(name);
      if (packAssets != null) {
         for (CommonAssetRegistry.PackAsset packAsset : packAssets) {
            if (packAsset.pack.equals(pack.getName())) {
               return true;
            }
         }
      }

      return false;
   }

   @Nullable
   public static CommonAsset getByName(String name) {
      name = PatternUtil.replaceBackslashWithForwardSlash(name);
      List<CommonAssetRegistry.PackAsset> asset = assetByNameMap.get(name);
      return asset == null ? null : asset.getLast().asset();
   }

   @Nullable
   public static CommonAsset getByHash(@Nonnull String hash) {
      List<CommonAssetRegistry.PackAsset> assets = assetByHashMap.get(hash.toLowerCase());
      return assets != null && !assets.isEmpty() ? assets.getFirst().asset() : null;
   }

   private static void removeCommonAssetByHash0(@Nonnull CommonAssetRegistry.PackAsset oldAsset) {
      List<CommonAssetRegistry.PackAsset> commonAssets = assetByHashMap.get(oldAsset.asset().getHash());
      if (commonAssets != null && commonAssets.remove(oldAsset) && commonAssets.isEmpty()) {
         assetByHashMap.compute(oldAsset.asset().getHash(), (key, assets) -> assets != null && !assets.isEmpty() ? assets : null);
      }
   }

   public static class AddCommonAssetResult {
      private CommonAssetRegistry.PackAsset newPackAsset;
      private CommonAssetRegistry.PackAsset previousNameAsset;
      private CommonAssetRegistry.PackAsset activeAsset;
      private CommonAssetRegistry.PackAsset[] previousHashAssets;
      private int duplicateAssetId;

      public CommonAssetRegistry.PackAsset getNewPackAsset() {
         return this.newPackAsset;
      }

      public CommonAssetRegistry.PackAsset getPreviousNameAsset() {
         return this.previousNameAsset;
      }

      public CommonAssetRegistry.PackAsset getActiveAsset() {
         return this.activeAsset;
      }

      public CommonAssetRegistry.PackAsset[] getPreviousHashAssets() {
         return this.previousHashAssets;
      }

      public int getDuplicateAssetId() {
         return this.duplicateAssetId;
      }

      @Nonnull
      @Override
      public String toString() {
         return "AddCommonAssetResult{previousNameAsset="
            + this.previousNameAsset
            + ", previousHashAssets="
            + Arrays.toString((Object[])this.previousHashAssets)
            + ", duplicateAssetId="
            + this.duplicateAssetId
            + "}";
      }
   }

   public record PackAsset(String pack, CommonAsset asset) {
      @Override
      public boolean equals(@Nullable Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            CommonAssetRegistry.PackAsset packAsset = (CommonAssetRegistry.PackAsset)o;
            return !this.pack.equals(packAsset.pack) ? false : this.asset.equals(packAsset.asset);
         } else {
            return false;
         }
      }

      @Nonnull
      @Override
      public String toString() {
         return "PackAsset{pack='" + this.pack + "', asset=" + this.asset + "}";
      }
   }
}
