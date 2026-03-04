package com.hypixel.hytale.server.core.meta;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DynamicMetaStore<K> extends AbstractMetaStore<K> {
   @Nonnull
   private final Int2ObjectMap<Object> map = new Int2ObjectOpenHashMap();

   public DynamicMetaStore(K parent, IMetaRegistry<K> registry) {
      this(parent, registry, false);
   }

   public DynamicMetaStore(K parent, IMetaRegistry<K> registry, boolean bypassEncodedCache) {
      super(parent, registry, bypassEncodedCache);
   }

   @Override
   protected <T> T get0(@Nonnull MetaKey<T> key) {
      return (T)this.map.get(key.getId());
   }

   @Override
   public <T> T getMetaObject(@Nonnull MetaKey<T> key) {
      T o = this.get0(key);
      if (o == null) {
         this.map.put(key.getId(), o = this.decodeOrNewMetaObject(key));
      }

      return o;
   }

   @Override
   public <T> T getIfPresentMetaObject(@Nonnull MetaKey<T> key) {
      return this.get0(key);
   }

   @Override
   public <T> T putMetaObject(@Nonnull MetaKey<T> key, T obj) {
      this.markMetaStoreDirty();
      return (T)this.map.put(key.getId(), obj);
   }

   @Override
   public <T> T removeMetaObject(@Nonnull MetaKey<T> key) {
      this.markMetaStoreDirty();
      return (T)this.map.remove(key.getId());
   }

   @Nullable
   @Override
   public <T> T removeSerializedMetaObject(MetaKey<T> key) {
      this.markMetaStoreDirty();
      if (key instanceof PersistentMetaKey) {
         this.tryDecodeUnknownKey((PersistentMetaKey<T>)key);
      }

      return this.removeMetaObject(key);
   }

   @Override
   public boolean hasMetaObject(@Nonnull MetaKey<?> key) {
      return this.map.containsKey(key.getId());
   }

   @Override
   public void forEachMetaObject(@Nonnull IMetaStore.MetaEntryConsumer consumer) {
      ObjectIterator var2 = this.map.int2ObjectEntrySet().iterator();

      while (var2.hasNext()) {
         Entry entry = (Entry)var2.next();
         consumer.accept(entry.getIntKey(), entry.getValue());
      }
   }

   @Nonnull
   public DynamicMetaStore<K> clone(K parent) {
      DynamicMetaStore<K> clone = new DynamicMetaStore<>(parent, this.registry);
      clone.map.putAll(this.map);
      return clone;
   }

   public void copyFrom(@Nonnull DynamicMetaStore<K> other) {
      this.markMetaStoreDirty();
      if (this.registry != other.registry) {
         throw new IllegalArgumentException("Wrong registry used in `copyFrom`.");
      } else {
         this.map.putAll(other.map);
      }
   }
}
