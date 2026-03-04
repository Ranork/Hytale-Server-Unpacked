package com.hypixel.hytale.assetstore;

import com.hypixel.hytale.assetstore.event.RegisterAssetStoreEvent;
import com.hypixel.hytale.assetstore.event.RemoveAssetStoreEvent;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.event.IEventDispatcher;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;
import javax.annotation.Nonnull;

public class AssetRegistry {
   public static final ReadWriteLock ASSET_LOCK = new ReentrantReadWriteLock();
   public static boolean HAS_INIT = false;
   public static final int TAG_NOT_FOUND = Integer.MIN_VALUE;
   private static final Map<Class<? extends JsonAssetWithMap>, AssetStore<?, ?, ?>> storeMap = new HashMap<>();
   private static final Map<Class<? extends JsonAssetWithMap>, AssetStore<?, ?, ?>> storeMapUnmodifiable = Collections.unmodifiableMap(storeMap);
   private static final AtomicInteger NEXT_TAG_INDEX = new AtomicInteger();
   private static final StampedLock TAG_LOCK = new StampedLock();
   private static final Object2IntMap<String> TAG_MAP = new Object2IntOpenHashMap();
   private static final Object2IntMap<String> CLIENT_TAG_MAP = new Object2IntOpenHashMap();

   @Nonnull
   public static Map<Class<? extends JsonAssetWithMap>, AssetStore<?, ?, ?>> getStoreMap() {
      return storeMapUnmodifiable;
   }

   public static <K, T extends JsonAssetWithMap<K, M>, M extends AssetMap<K, T>> AssetStore<K, T, M> getAssetStore(Class<T> tClass) {
      return (AssetStore<K, T, M>)storeMap.get(tClass);
   }

   @Nonnull
   public static <K, T extends JsonAssetWithMap<K, M>, M extends AssetMap<K, T>, S extends AssetStore<K, T, M>> S register(@Nonnull S assetStore) {
      ASSET_LOCK.writeLock().lock();

      try {
         if (storeMap.putIfAbsent(assetStore.getAssetClass(), assetStore) != null) {
            throw new IllegalArgumentException("Asset Store already exists for " + assetStore.getAssetClass());
         }
      } finally {
         ASSET_LOCK.writeLock().unlock();
      }

      IEventDispatcher<RegisterAssetStoreEvent, RegisterAssetStoreEvent> dispatch = assetStore.getEventBus().dispatchFor(RegisterAssetStoreEvent.class);
      if (dispatch.hasListener()) {
         dispatch.dispatch(new RegisterAssetStoreEvent(assetStore));
      }

      return assetStore;
   }

   public static <K, T extends JsonAssetWithMap<K, M>, M extends AssetMap<K, T>, S extends AssetStore<K, T, M>> void unregister(@Nonnull S assetStore) {
      ASSET_LOCK.writeLock().lock();

      try {
         storeMap.remove(assetStore.getAssetClass());
      } finally {
         ASSET_LOCK.writeLock().unlock();
      }

      IEventDispatcher<RemoveAssetStoreEvent, RemoveAssetStoreEvent> dispatch = assetStore.getEventBus().dispatchFor(RemoveAssetStoreEvent.class);
      if (dispatch.hasListener()) {
         dispatch.dispatch(new RemoveAssetStoreEvent(assetStore));
      }
   }

   public static int getTagIndex(@Nonnull String tag) {
      if (tag == null) {
         throw new IllegalArgumentException("tag can't be null!");
      } else {
         long stamp = TAG_LOCK.readLock();

         int var3;
         try {
            var3 = TAG_MAP.getInt(tag);
         } finally {
            TAG_LOCK.unlockRead(stamp);
         }

         return var3;
      }
   }

   public static int getOrCreateTagIndex(@Nonnull String tag) {
      if (tag == null) {
         throw new IllegalArgumentException("tag can't be null!");
      } else {
         long stamp = TAG_LOCK.writeLock();

         int var3;
         try {
            var3 = TAG_MAP.computeIfAbsent(tag.intern(), k -> NEXT_TAG_INDEX.getAndIncrement());
         } finally {
            TAG_LOCK.unlockWrite(stamp);
         }

         return var3;
      }
   }

   public static boolean registerClientTag(@Nonnull String tag) {
      if (tag == null) {
         throw new IllegalArgumentException("tag can't be null!");
      } else {
         long stamp = TAG_LOCK.writeLock();

         boolean var3;
         try {
            var3 = CLIENT_TAG_MAP.put(tag, TAG_MAP.computeIfAbsent(tag, k -> NEXT_TAG_INDEX.getAndIncrement())) == Integer.MIN_VALUE;
         } finally {
            TAG_LOCK.unlockWrite(stamp);
         }

         return var3;
      }
   }

   @Nonnull
   public static Object2IntMap<String> getClientTags() {
      long stamp = TAG_LOCK.readLock();

      Object2IntOpenHashMap var2;
      try {
         var2 = new Object2IntOpenHashMap(CLIENT_TAG_MAP);
      } finally {
         TAG_LOCK.unlockRead(stamp);
      }

      return var2;
   }

   static {
      TAG_MAP.defaultReturnValue(Integer.MIN_VALUE);
      CLIENT_TAG_MAP.defaultReturnValue(Integer.MIN_VALUE);
   }
}
