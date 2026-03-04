package com.hypixel.hytale.assetstore.map;

import com.hypixel.fastutil.ints.Int2ObjectConcurrentHashMap;
import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetMap;
import com.hypixel.hytale.assetstore.JsonAsset;
import com.hypixel.hytale.assetstore.codec.AssetCodec;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.StampedLock;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DefaultAssetMap<K, T extends JsonAsset<K>> extends AssetMap<K, T> {
   public static final DefaultAssetMap.AssetRef[] EMPTY_PAIR_ARRAY = new DefaultAssetMap.AssetRef[0];
   public static final String DEFAULT_PACK_KEY = "Hytale:Hytale";
   protected final StampedLock assetMapLock = new StampedLock();
   @Nonnull
   protected final Map<K, T> assetMap;
   protected final Map<K, DefaultAssetMap.AssetRef<T>[]> assetChainMap;
   protected final Map<String, ObjectSet<K>> packAssetKeys = new ConcurrentHashMap<>();
   protected final Map<Path, ObjectSet<K>> pathToKeyMap = new ConcurrentHashMap<>();
   protected final Map<K, ObjectSet<K>> assetChildren;
   protected final Int2ObjectConcurrentHashMap<Set<K>> tagStorage = new Int2ObjectConcurrentHashMap<>();
   protected final Int2ObjectConcurrentHashMap<Set<K>> unmodifiableTagStorage = new Int2ObjectConcurrentHashMap<>();
   protected final IntSet unmodifiableTagKeys = IntSets.unmodifiable(this.tagStorage.keySet());

   public DefaultAssetMap() {
      this.assetMap = new Object2ObjectOpenCustomHashMap(CaseInsensitiveHashStrategy.getInstance());
      this.assetChainMap = new Object2ObjectOpenCustomHashMap(CaseInsensitiveHashStrategy.getInstance());
      this.assetChildren = new Object2ObjectOpenCustomHashMap(CaseInsensitiveHashStrategy.getInstance());
   }

   public DefaultAssetMap(@Nonnull Map<K, T> assetMap) {
      this.assetMap = assetMap;
      this.assetChainMap = new Object2ObjectOpenCustomHashMap(CaseInsensitiveHashStrategy.getInstance());
      this.assetChildren = new Object2ObjectOpenCustomHashMap(CaseInsensitiveHashStrategy.getInstance());
   }

   @Nullable
   @Override
   public T getAsset(K key) {
      long stamp = this.assetMapLock.tryOptimisticRead();
      T value = this.assetMap.get(key);
      if (this.assetMapLock.validate(stamp)) {
         return value;
      } else {
         stamp = this.assetMapLock.readLock();

         JsonAsset var5;
         try {
            var5 = this.assetMap.get(key);
         } finally {
            this.assetMapLock.unlockRead(stamp);
         }

         return (T)var5;
      }
   }

   @Nullable
   @Override
   public T getAsset(@Nonnull String packKey, K key) {
      long stamp = this.assetMapLock.tryOptimisticRead();
      T result = this.getAssetForPack0(packKey, key);
      if (this.assetMapLock.validate(stamp)) {
         return result;
      } else {
         stamp = this.assetMapLock.readLock();

         JsonAsset var6;
         try {
            var6 = this.getAssetForPack0(packKey, key);
         } finally {
            this.assetMapLock.unlockRead(stamp);
         }

         return (T)var6;
      }
   }

   private T getAssetForPack0(@Nonnull String packKey, K key) {
      DefaultAssetMap.AssetRef<T>[] chain = this.assetChainMap.get(key);
      if (chain == null) {
         return null;
      } else {
         for (int i = 0; i < chain.length; i++) {
            DefaultAssetMap.AssetRef<T> pair = chain[i];
            if (Objects.equals(pair.pack, packKey)) {
               if (i == 0) {
                  return null;
               }

               return chain[i - 1].value;
            }
         }

         return this.assetMap.get(key);
      }
   }

   @Nullable
   @Override
   public Path getPath(K key) {
      long stamp = this.assetMapLock.tryOptimisticRead();
      Path result = this.getPath0(key);
      if (this.assetMapLock.validate(stamp)) {
         return result;
      } else {
         stamp = this.assetMapLock.readLock();

         Path var5;
         try {
            var5 = this.getPath0(key);
         } finally {
            this.assetMapLock.unlockRead(stamp);
         }

         return var5;
      }
   }

   @Nullable
   @Override
   public String getAssetPack(K key) {
      long stamp = this.assetMapLock.tryOptimisticRead();
      String result = this.getAssetPack0(key);
      if (this.assetMapLock.validate(stamp)) {
         return result;
      } else {
         stamp = this.assetMapLock.readLock();

         String var5;
         try {
            var5 = this.getAssetPack0(key);
         } finally {
            this.assetMapLock.unlockRead(stamp);
         }

         return var5;
      }
   }

   @Nullable
   private Path getPath0(K key) {
      DefaultAssetMap.AssetRef<T> result = this.getAssetRef(key);
      return result != null ? result.path : null;
   }

   @Nullable
   private String getAssetPack0(K key) {
      DefaultAssetMap.AssetRef<T> result = this.getAssetRef(key);
      return result != null ? result.pack : null;
   }

   @Nullable
   private DefaultAssetMap.AssetRef<T> getAssetRef(K key) {
      DefaultAssetMap.AssetRef<T>[] chain = this.assetChainMap.get(key);
      return chain == null ? null : chain[chain.length - 1];
   }

   @Override
   public Set<K> getKeys(@Nonnull Path path) {
      ObjectSet<K> set = this.pathToKeyMap.get(path);
      return set == null ? ObjectSets.emptySet() : ObjectSets.unmodifiable(set);
   }

   @Override
   public Set<K> getChildren(K key) {
      long stamp = this.assetMapLock.tryOptimisticRead();
      ObjectSet<K> children = this.assetChildren.get(key);
      Set<K> result = children == null ? ObjectSets.emptySet() : ObjectSets.unmodifiable(children);
      if (this.assetMapLock.validate(stamp)) {
         return result;
      } else {
         stamp = this.assetMapLock.readLock();

         ObjectSet var6;
         try {
            children = this.assetChildren.get(key);
            var6 = children == null ? ObjectSets.emptySet() : ObjectSets.unmodifiable(children);
         } finally {
            this.assetMapLock.unlockRead(stamp);
         }

         return var6;
      }
   }

   @Override
   public int getAssetCount() {
      return this.assetMap.size();
   }

   @Nonnull
   @Override
   public Map<K, T> getAssetMap() {
      return Collections.unmodifiableMap(this.assetMap);
   }

   @Nonnull
   @Override
   public Map<K, Path> getPathMap(@Nonnull String packKey) {
      long stamp = this.assetMapLock.readLock();

      Map var4;
      try {
         var4 = this.assetChainMap
            .entrySet()
            .stream()
            .map(e -> Map.entry(e.getKey(), Arrays.stream(e.getValue()).filter(v -> Objects.equals(v.pack, packKey)).findFirst()))
            .filter(e -> e.getValue().isPresent())
            .filter(e -> e.getValue().get().path != null)
            .collect(Collectors.toMap(Entry::getKey, e -> e.getValue().get().path));
      } finally {
         this.assetMapLock.unlockRead(stamp);
      }

      return var4;
   }

   @Override
   public Set<K> getKeysForTag(int tagIndex) {
      return this.unmodifiableTagStorage.getOrDefault(tagIndex, ObjectSets.emptySet());
   }

   @Nonnull
   @Override
   public IntSet getTagIndexes() {
      return this.unmodifiableTagKeys;
   }

   @Override
   public int getTagCount() {
      return this.tagStorage.size();
   }

   @Override
   protected void clear() {
      long stamp = this.assetMapLock.writeLock();

      try {
         this.assetChildren.clear();
         this.assetChainMap.clear();
         this.pathToKeyMap.clear();
         this.assetMap.clear();
         this.tagStorage.clear();
         this.unmodifiableTagStorage.clear();
      } finally {
         this.assetMapLock.unlockWrite(stamp);
      }
   }

   @Override
   protected void putAll(
      @Nonnull String packKey,
      @Nonnull AssetCodec<K, T> codec,
      @Nonnull Map<K, T> loadedAssets,
      @Nonnull Map<K, Path> loadedKeyToPathMap,
      @Nonnull Map<K, Set<K>> loadedAssetChildren
   ) {
      long stamp = this.assetMapLock.writeLock();

      try {
         for (Entry<K, Set<K>> entry : loadedAssetChildren.entrySet()) {
            this.assetChildren.computeIfAbsent(entry.getKey(), k -> new ObjectOpenHashSet(3)).addAll(entry.getValue());
         }

         for (Entry<K, Path> entry : loadedKeyToPathMap.entrySet()) {
            this.pathToKeyMap.computeIfAbsent(entry.getValue(), k -> new ObjectOpenHashSet(1)).add(entry.getKey());
         }

         for (Entry<K, T> e : loadedAssets.entrySet()) {
            K key = e.getKey();
            this.packAssetKeys.computeIfAbsent(packKey, v -> new ObjectOpenHashSet()).add(key);
            DefaultAssetMap.AssetRef<T>[] chain = this.assetChainMap.get(key);
            if (chain == null) {
               chain = EMPTY_PAIR_ARRAY;
            }

            boolean found = false;
            DefaultAssetMap.AssetRef[] finalVal = chain;
            int var14 = chain.length;
            int var15 = 0;

            while (true) {
               if (var15 < var14) {
                  DefaultAssetMap.AssetRef<T> pair = finalVal[var15];
                  if (!Objects.equals(pair.pack, packKey)) {
                     var15++;
                     continue;
                  }

                  pair.value = e.getValue();
                  found = true;
               }

               if (!found) {
                  chain = Arrays.copyOf(chain, chain.length + 1);
                  chain[chain.length - 1] = new DefaultAssetMap.AssetRef<>(packKey, loadedKeyToPathMap.get(e.getKey()), e.getValue());
                  this.assetChainMap.put(key, chain);
               }

               T finalValx = chain[chain.length - 1].value;
               this.assetMap.put(key, finalValx);
               break;
            }
         }
      } finally {
         this.assetMapLock.unlockWrite(stamp);
      }

      this.putAssetTags(codec, loadedAssets);
   }

   protected void putAssetTags(@Nonnull AssetCodec<K, T> codec, @Nonnull Map<K, T> loadedAssets) {
      for (Entry<K, T> entry : loadedAssets.entrySet()) {
         AssetExtraInfo.Data data = codec.getData(entry.getValue());
         if (data != null) {
            K key = entry.getKey();
            IntIterator iterator = data.getExpandedTagIndexes().iterator();

            while (iterator.hasNext()) {
               int tag = iterator.nextInt();
               this.putAssetTag(key, tag);
            }
         }
      }
   }

   protected void putAssetTag(K key, int tag) {
      this.tagStorage.computeIfAbsent(tag, k -> {
         ObjectOpenHashSet<K> set = new ObjectOpenHashSet(3);
         this.unmodifiableTagStorage.put(k, ObjectSets.unmodifiable(set));
         return set;
      }).add(key);
   }

   @Override
   public Set<K> getKeysForPack(@Nonnull String name) {
      return (Set<K>)this.packAssetKeys.get(name);
   }

   @Override
   protected Set<K> remove(@Nonnull Set<K> keys) {
      long stamp = this.assetMapLock.writeLock();

      Object var16;
      try {
         Set<K> children = new HashSet<>();

         for (K key : keys) {
            DefaultAssetMap.AssetRef<T>[] chain = this.assetChainMap.remove(key);
            if (chain != null) {
               DefaultAssetMap.AssetRef<T> info = chain[chain.length - 1];
               if (info.path != null) {
                  this.pathToKeyMap.computeIfPresent(info.path, (p, list) -> {
                     list.remove(key);
                     return list.isEmpty() ? null : list;
                  });
               }

               this.assetMap.remove(key);

               for (DefaultAssetMap.AssetRef<T> c : chain) {
                  this.packAssetKeys.get(Objects.requireNonNullElse(c.pack, "Hytale:Hytale")).remove(key);
               }

               for (ObjectSet<K> child : this.assetChildren.values()) {
                  child.remove(key);
               }

               ObjectSet<K> child = this.assetChildren.remove(key);
               if (child != null) {
                  children.addAll(child);
               }
            }
         }

         this.tagStorage.forEach((_k, value, removedKeys) -> value.removeAll(removedKeys), keys);
         children.removeAll(keys);
         var16 = children;
      } finally {
         this.assetMapLock.unlockWrite(stamp);
      }

      return (Set<K>)var16;
   }

   @Override
   protected Set<K> remove(@Nonnull String packKey, @Nonnull Set<K> keys, @Nonnull List<Entry<String, Object>> pathsToReload) {
      long stamp = this.assetMapLock.writeLock();

      Set iterator;
      try {
         Set<K> children = new HashSet<>();
         ObjectSet<K> packKeys = this.packAssetKeys.get(Objects.requireNonNullElse(packKey, "Hytale:Hytale"));
         if (packKeys != null) {
            Iterator<K> iteratorx = keys.iterator();

            while (iteratorx.hasNext()) {
               K key = iteratorx.next();
               packKeys.remove(key);
               DefaultAssetMap.AssetRef<T>[] chain = this.assetChainMap.remove(key);
               if (chain.length == 1) {
                  DefaultAssetMap.AssetRef<T> info = chain[0];
                  if (!Objects.equals(info.pack, packKey)) {
                     iteratorx.remove();
                     this.assetChainMap.put(key, chain);
                  } else {
                     if (info.path != null) {
                        this.pathToKeyMap.computeIfPresent(info.path, (p, list) -> {
                           list.remove(key);
                           return list.isEmpty() ? null : list;
                        });
                     }

                     this.assetMap.remove(key);

                     for (ObjectSet<K> child : this.assetChildren.values()) {
                        child.remove(key);
                     }

                     ObjectSet<K> child = this.assetChildren.remove(key);
                     if (child != null) {
                        children.addAll(child);
                     }
                  }
               } else {
                  iteratorx.remove();
                  DefaultAssetMap.AssetRef<T>[] newChain = new DefaultAssetMap.AssetRef[chain.length - 1];
                  int offset = 0;

                  for (int i = 0; i < chain.length; i++) {
                     DefaultAssetMap.AssetRef<T> pair = chain[i];
                     if (Objects.equals(pair.pack, packKey)) {
                        if (pair.path != null) {
                           this.pathToKeyMap.computeIfPresent(pair.path, (p, list) -> {
                              list.remove(key);
                              return list.isEmpty() ? null : list;
                           });
                        }
                     } else {
                        newChain[offset++] = pair;
                        if (pair.path != null) {
                           pathsToReload.add(Map.entry(pair.pack, pair.path));
                        } else {
                           pathsToReload.add(Map.entry(pair.pack, pair.value));
                        }
                     }
                  }

                  this.assetChainMap.put(key, newChain);
                  DefaultAssetMap.AssetRef<T> newAsset = newChain[newChain.length - 1];
                  this.assetMap.put(key, newAsset.value);
                  if (newAsset.path != null) {
                     this.pathToKeyMap.computeIfAbsent(newAsset.path, k -> new ObjectOpenHashSet(1)).add(key);
                  }
               }
            }

            this.tagStorage.forEach((_k, value, removedKeys) -> value.removeAll(removedKeys), keys);
            children.removeAll(keys);
            return children;
         }

         iterator = Collections.emptySet();
      } finally {
         this.assetMapLock.unlockWrite(stamp);
      }

      return iterator;
   }

   protected static class AssetRef<T> {
      protected final String pack;
      protected final Path path;
      protected T value;

      protected AssetRef(String pack, Path path, T value) {
         this.pack = pack;
         this.path = path;
         this.value = value;
      }
   }
}
