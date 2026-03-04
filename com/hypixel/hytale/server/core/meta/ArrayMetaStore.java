package com.hypixel.hytale.server.core.meta;

import com.hypixel.hytale.common.util.ArrayUtil;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ArrayMetaStore<K> extends AbstractMetaStore<K> {
   private static final Object NO_ENTRY = new Object();
   private Object[] array = ArrayUtil.emptyArray();

   public ArrayMetaStore(K parent, IMetaRegistry<K> registry) {
      this(parent, registry, false);
   }

   public ArrayMetaStore(K parent, IMetaRegistry<K> registry, boolean bypassEncodedCache) {
      super(parent, registry, bypassEncodedCache);
   }

   @Override
   protected <T> T get0(@Nonnull MetaKey<T> key) {
      return (T)this.array[key.getId()];
   }

   @Override
   public <T> T getMetaObject(@Nonnull MetaKey<T> key) {
      int id = key.getId();
      if (id >= this.array.length) {
         T obj = this.decodeOrNewMetaObject(key);
         this.resizeArray(obj, id);
         return obj;
      } else {
         T obj = this.get0(key);
         if (obj == NO_ENTRY) {
            this.array[id] = obj = this.decodeOrNewMetaObject(key);
         }

         return obj;
      }
   }

   @Nullable
   @Override
   public <T> T getIfPresentMetaObject(@Nonnull MetaKey<T> key) {
      if (key.getId() >= this.array.length) {
         return null;
      } else {
         T oldObj = this.get0(key);
         return oldObj != NO_ENTRY ? oldObj : null;
      }
   }

   @Nullable
   @Override
   public <T> T putMetaObject(@Nonnull MetaKey<T> key, T obj) {
      this.markMetaStoreDirty();
      int id = key.getId();
      if (id >= this.array.length) {
         this.resizeArray(obj, id);
         return null;
      } else {
         T oldObj = (T)this.array[id];
         this.array[id] = obj;
         return oldObj != NO_ENTRY ? oldObj : null;
      }
   }

   @Nullable
   @Override
   public <T> T removeMetaObject(@Nonnull MetaKey<T> key) {
      if (key.getId() >= this.array.length) {
         return null;
      } else {
         this.markMetaStoreDirty();
         T oldObj = (T)this.array[key.getId()];
         this.array[key.getId()] = NO_ENTRY;
         return oldObj != NO_ENTRY ? oldObj : null;
      }
   }

   @Nullable
   @Override
   public <T> T removeSerializedMetaObject(@Nonnull MetaKey<T> key) {
      if (key.getId() >= this.array.length && key instanceof PersistentMetaKey) {
         this.tryDecodeUnknownKey((PersistentMetaKey<T>)key);
      }

      return this.removeMetaObject(key);
   }

   @Override
   public boolean hasMetaObject(@Nonnull MetaKey<?> key) {
      int id = key.getId();
      return id >= this.array.length ? false : this.array[id] != NO_ENTRY;
   }

   @Override
   public void forEachMetaObject(@Nonnull IMetaStore.MetaEntryConsumer consumer) {
      for (int i = 0; i < this.array.length; i++) {
         Object o = this.array[i];
         if (o != NO_ENTRY) {
            consumer.accept(i, o);
         }
      }
   }

   private <T> void resizeArray(T obj, int id) {
      Object[] arr = new Object[id + 1];
      Arrays.fill(arr, this.array.length, arr.length, NO_ENTRY);
      System.arraycopy(this.array, 0, arr, 0, this.array.length);
      arr[id] = obj;
      this.array = arr;
   }
}
