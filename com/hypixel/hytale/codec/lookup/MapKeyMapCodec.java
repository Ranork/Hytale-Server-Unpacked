package com.hypixel.hytale.codec.lookup;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.EmptyExtraInfo;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.logger.HytaleLogger;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonDocument;
import org.bson.BsonValue;

public class MapKeyMapCodec<V> extends AMapProvidedMapCodec<Class<? extends V>, V, Codec<V>, MapKeyMapCodec.TypeMap<V>> {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private static final Set<Reference<MapKeyMapCodec.TypeMap<?>>> ACTIVE_MAPS = ConcurrentHashMap.newKeySet();
   private static final ReferenceQueue<MapKeyMapCodec.TypeMap<?>> MAP_REFERENCE_QUEUE = new ReferenceQueue<>();
   private static final StampedLock DATA_LOCK = new StampedLock();
   protected final Map<String, Class<? extends V>> idToClass = new ConcurrentHashMap<>();
   protected final Map<Class<? extends V>, String> classToId = new ConcurrentHashMap<>();

   public MapKeyMapCodec() {
      this(true);
   }

   public MapKeyMapCodec(boolean unmodifiable) {
      super(new ConcurrentHashMap<>(), Function.identity(), unmodifiable);
   }

   public <T extends V> void register(@Nonnull Class<T> tClass, @Nonnull String id, @Nonnull Codec<T> codec) {
      long lock = DATA_LOCK.writeLock();

      try {
         if (this.codecProvider.put(tClass, codec) != null) {
            throw new IllegalArgumentException("Id already registered");
         }

         if (this.idToClass.put(id, tClass) != null) {
            throw new IllegalArgumentException("Id already registered");
         }

         if (this.classToId.put(tClass, id) != null) {
            throw new IllegalArgumentException("Class already registered");
         }

         for (Reference<MapKeyMapCodec.TypeMap<?>> mapRef : ACTIVE_MAPS) {
            MapKeyMapCodec.TypeMap<?> map = mapRef.get();
            if (map != null && map.codec == this) {
               map.tryUpgrade(tClass, id, codec);
            }
         }
      } finally {
         DATA_LOCK.unlockWrite(lock);
      }
   }

   public <T extends V> void unregister(@Nonnull Class<T> tClass) {
      long lock = DATA_LOCK.writeLock();

      try {
         Codec<V> codec = this.codecProvider.get(tClass);
         if (codec == null) {
            throw new IllegalStateException(tClass + " not registered");
         }

         String id = this.classToId.get(tClass);

         for (Reference<MapKeyMapCodec.TypeMap<?>> mapRef : ACTIVE_MAPS) {
            MapKeyMapCodec.TypeMap<?> map = mapRef.get();
            if (map != null && map.codec == this) {
               map.tryDowngrade(tClass, id, codec);
            }
         }

         this.codecProvider.remove(tClass);
         this.classToId.remove(tClass);
         this.idToClass.remove(id);
      } finally {
         DATA_LOCK.unlockWrite(lock);
      }
   }

   @Nullable
   @Deprecated(forRemoval = true)
   public V decodeById(@Nonnull String id, BsonValue value, ExtraInfo extraInfo) {
      Codec<V> codec = this.codecProvider.get(this.getKeyForId(id));
      return codec.decode(value, extraInfo);
   }

   protected String getIdForKey(Class<? extends V> key) {
      return this.classToId.get(key);
   }

   @Nonnull
   public MapKeyMapCodec.TypeMap<V> createMap() {
      return new MapKeyMapCodec.TypeMap<>(this);
   }

   public void handleUnknown(@Nonnull MapKeyMapCodec.TypeMap<V> map, @Nonnull String key, BsonValue value, @Nonnull ExtraInfo extraInfo) {
      extraInfo.addUnknownKey(key);
      map.unknownValues.put(key, value);
   }

   public void handleUnknown(@Nonnull MapKeyMapCodec.TypeMap<V> map, @Nonnull String key, @Nonnull RawJsonReader reader, @Nonnull ExtraInfo extraInfo) throws IOException {
      extraInfo.addUnknownKey(key);
      map.unknownValues.put(key, RawJsonReader.readBsonValue(reader));
   }

   protected void encodeExtra(@Nonnull BsonDocument document, @Nonnull MapKeyMapCodec.TypeMap<V> map, ExtraInfo extraInfo) {
      document.putAll(map.unknownValues);
   }

   public Class<? extends V> getKeyForId(String id) {
      return this.idToClass.get(id);
   }

   @Nonnull
   protected MapKeyMapCodec.TypeMap<V> emptyMap() {
      return MapKeyMapCodec.TypeMap.EMPTY;
   }

   @Nonnull
   protected MapKeyMapCodec.TypeMap<V> unmodifiableMap(@Nonnull MapKeyMapCodec.TypeMap<V> m) {
      return new MapKeyMapCodec.TypeMap<>(this, Collections.unmodifiableMap(m.map), m.map, m.unknownValues);
   }

   static {
      Thread thread = new Thread(() -> {
         while (!Thread.interrupted()) {
            try {
               ACTIVE_MAPS.remove(MAP_REFERENCE_QUEUE.remove());
            } catch (InterruptedException var1) {
               Thread.currentThread().interrupt();
               return;
            }
         }
      }, "MapKeyMapCodec");
      thread.setDaemon(true);
      thread.start();
   }

   public static class TypeMap<V> implements Map<Class<? extends V>, V> {
      private static final MapKeyMapCodec.TypeMap EMPTY = new MapKeyMapCodec.TypeMap(null, Collections.emptyMap(), Collections.emptyMap());
      private final MapKeyMapCodec<V> codec;
      @Nonnull
      private final Map<Class<? extends V>, V> map;
      @Nonnull
      private final Map<Class<? extends V>, V> internalMap;
      @Nonnull
      private final Map<String, BsonValue> unknownValues;

      public TypeMap(MapKeyMapCodec<V> codec) {
         this(codec, new Object2ObjectOpenHashMap(), new Object2ObjectOpenHashMap());
      }

      public TypeMap(MapKeyMapCodec<V> codec, @Nonnull Map<Class<? extends V>, V> map, @Nonnull Map<String, BsonValue> unknownValues) {
         this(codec, map, map, unknownValues);
      }

      public TypeMap(
         MapKeyMapCodec<V> codec,
         @Nonnull Map<Class<? extends V>, V> map,
         @Nonnull Map<Class<? extends V>, V> internalMap,
         @Nonnull Map<String, BsonValue> unknownValues
      ) {
         this.codec = codec;
         this.map = map;
         this.internalMap = internalMap;
         this.unknownValues = unknownValues;
         MapKeyMapCodec.ACTIVE_MAPS.add(new WeakReference<>(this, MapKeyMapCodec.MAP_REFERENCE_QUEUE));
      }

      public <T extends V> void tryUpgrade(@Nonnull Class<T> tClass, @Nonnull String id, @Nonnull Codec<T> codec) {
         BsonValue unknownValue = this.unknownValues.remove(id);
         if (unknownValue != null) {
            T value = codec.decode(unknownValue, EmptyExtraInfo.EMPTY);
            this.internalMap.put(tClass, (V)value);
            ((HytaleLogger.Api)MapKeyMapCodec.LOGGER.atInfo()).log("Upgrade " + id + " from unknown value");
         }
      }

      public <T extends V> void tryDowngrade(@Nonnull Class<T> tClass, @Nonnull String id, @Nonnull Codec<T> codec) {
         V value = this.internalMap.remove(tClass);
         if (value != null) {
            BsonValue encoded = codec.encode((T)value, EmptyExtraInfo.EMPTY);
            this.unknownValues.put(id, encoded);
            ((HytaleLogger.Api)MapKeyMapCodec.LOGGER.atInfo()).log("Downgraded " + id + " to unknown value");
         }
      }

      @Override
      public int size() {
         return this.map.size();
      }

      @Override
      public boolean isEmpty() {
         return this.map.isEmpty();
      }

      @Override
      public boolean containsKey(Object key) {
         return this.map.containsKey(key);
      }

      @Override
      public boolean containsValue(Object value) {
         return this.map.containsValue(value);
      }

      @Override
      public V get(Object key) {
         return this.map.get(key);
      }

      @Nullable
      public <T extends V> T get(Class<? extends T> key) {
         long lock = MapKeyMapCodec.DATA_LOCK.readLock();

         Object var4;
         try {
            var4 = this.map.get(key);
         } finally {
            MapKeyMapCodec.DATA_LOCK.unlockRead(lock);
         }

         return (T)var4;
      }

      public V put(@Nonnull Class<? extends V> key, V value) {
         long lock = MapKeyMapCodec.DATA_LOCK.readLock();

         Object var5;
         try {
            if (!key.isInstance(value)) {
               throw new IllegalArgumentException("Passed value '" + value + "' isn't of type: " + key);
            }

            var5 = this.map.put(key, value);
         } finally {
            MapKeyMapCodec.DATA_LOCK.unlockRead(lock);
         }

         return (V)var5;
      }

      @Override
      public V remove(Object key) {
         return this.map.remove(key);
      }

      @Override
      public void putAll(@Nonnull Map<? extends Class<? extends V>, ? extends V> m) {
         for (Entry<? extends Class<? extends V>, ? extends V> e : m.entrySet()) {
            this.put((Class<? extends V>)e.getKey(), (V)e.getValue());
         }
      }

      @Override
      public void clear() {
         this.map.clear();
      }

      @Nonnull
      @Override
      public Set<Class<? extends V>> keySet() {
         return this.map.keySet();
      }

      @Nonnull
      @Override
      public Collection<V> values() {
         return this.map.values();
      }

      @Nonnull
      @Override
      public Set<Entry<Class<? extends V>, V>> entrySet() {
         return this.map.entrySet();
      }

      public <T extends V> T computeIfAbsent(Class<? extends T> key, @Nonnull Function<? super Class<? extends V>, T> mappingFunction) {
         long lock = MapKeyMapCodec.DATA_LOCK.readLock();

         Object var5;
         try {
            var5 = this.map.computeIfAbsent(key, mappingFunction);
         } finally {
            MapKeyMapCodec.DATA_LOCK.unlockRead(lock);
         }

         return (T)var5;
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else {
            return !(o instanceof Map) ? false : this.entrySet().equals(((Map)o).entrySet());
         }
      }

      @Override
      public int hashCode() {
         return this.map.hashCode();
      }

      @Nonnull
      @Override
      public String toString() {
         return "TypeMap{map=" + this.map + "}";
      }

      public static <V> MapKeyMapCodec.TypeMap<V> empty() {
         return EMPTY;
      }
   }
}
