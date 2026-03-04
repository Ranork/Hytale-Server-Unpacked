package com.hypixel.hytale.server.core.meta;

import com.hypixel.hytale.codec.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MetaRegistry<K> implements IMetaRegistry<K> {
   private final Map<String, MetaRegistry.MetaRegistryEntry> parameterMapping = new Object2ObjectOpenHashMap();
   private final List<MetaRegistry.MetaRegistryEntry> suppliers = new ObjectArrayList();
   private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

   @Override
   public <T> MetaKey<T> registerMetaObject(Function<K, T> function, boolean persistent, String keyName, @Nonnull Codec<T> codec) {
      this.lock.writeLock().lock();

      MetaKey var8;
      try {
         if (persistent && codec == null) {
            throw new IllegalStateException("Codec cannot be null if persistence is enabled.");
         }

         int metaId = this.suppliers.size();
         MetaKey<T> key;
         if (persistent) {
            key = new PersistentMetaKey<>(metaId, keyName, codec);
         } else {
            key = new MetaKey<>(metaId);
         }

         MetaRegistry<K>.MetaRegistryEntry<T> metaEntry = new MetaRegistry.MetaRegistryEntry<>(function, key);
         this.suppliers.add(metaEntry);
         if (persistent) {
            if (this.parameterMapping.containsKey(keyName)) {
               throw new IllegalStateException("Codec key is already registered. Given: " + keyName);
            }

            this.parameterMapping.put(keyName, metaEntry);
         }

         var8 = metaEntry.getKey();
      } finally {
         this.lock.writeLock().unlock();
      }

      return var8;
   }

   @Override
   public <T> T newMetaObject(@Nonnull MetaKey<T> key, K parent) {
      this.lock.readLock().lock();

      Object var3;
      try {
         var3 = this.suppliers.get(key.getId()).getFunction().apply(parent);
      } finally {
         this.lock.readLock().unlock();
      }

      return (T)var3;
   }

   @Override
   public void forEachMetaEntry(@Nonnull IMetaStore<K> store, @Nonnull final IMetaRegistry.MetaEntryConsumer consumer) {
      store.forEachMetaObject(new IMetaStore.MetaEntryConsumer() {
         @Override
         public <T> void accept(int id, T value) {
            MetaRegistry<K>.MetaRegistryEntry<T> entry = MetaRegistry.this.suppliers.get(id);
            consumer.accept(entry.getKey(), value);
         }
      });
   }

   @Nullable
   @Override
   public PersistentMetaKey<?> getMetaKeyForCodecKey(String codecKey) {
      MetaRegistry.MetaRegistryEntry entry = this.parameterMapping.get(codecKey);
      return entry == null ? null : (PersistentMetaKey)entry.getKey();
   }

   private class MetaRegistryEntry<T> {
      private final Function<K, T> function;
      private final MetaKey<T> key;

      public MetaRegistryEntry(Function<K, T> function, MetaKey<T> key) {
         this.function = function;
         this.key = key;
      }

      public Function<K, T> getFunction() {
         return this.function;
      }

      public MetaKey<T> getKey() {
         return this.key;
      }
   }
}
