package com.hypixel.hytale.assetstore;

import com.hypixel.hytale.assetstore.codec.AssetCodec;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class AssetMap<K, T extends JsonAsset<K>> {
   @Nullable
   public abstract T getAsset(K var1);

   @Nullable
   public abstract T getAsset(@Nonnull String var1, K var2);

   @Nullable
   public abstract Path getPath(K var1);

   @Nullable
   public abstract String getAssetPack(K var1);

   public abstract Set<K> getKeys(Path var1);

   public abstract Set<K> getChildren(K var1);

   public abstract int getAssetCount();

   public abstract Map<K, T> getAssetMap();

   public abstract Map<K, Path> getPathMap(@Nonnull String var1);

   public abstract Set<K> getKeysForTag(int var1);

   public abstract IntSet getTagIndexes();

   public abstract int getTagCount();

   protected abstract void clear();

   protected abstract void putAll(@Nonnull String var1, AssetCodec<K, T> var2, Map<K, T> var3, Map<K, Path> var4, Map<K, Set<K>> var5);

   protected abstract Set<K> remove(Set<K> var1);

   protected abstract Set<K> remove(@Nonnull String var1, Set<K> var2, List<Entry<String, Object>> var3);

   public boolean requireReplaceOnRemove() {
      return false;
   }

   public abstract Set<K> getKeysForPack(@Nonnull String var1);
}
