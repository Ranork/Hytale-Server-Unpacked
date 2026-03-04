package com.hypixel.hytale.server.core.meta;

import javax.annotation.Nullable;

public interface IMetaStore<K> {
   IMetaStoreImpl<K> getMetaStore();

   default <T> T getMetaObject(MetaKey<T> key) {
      return this.getMetaStore().getMetaObject(key);
   }

   @Nullable
   default <T> T getIfPresentMetaObject(MetaKey<T> key) {
      return this.getMetaStore().getIfPresentMetaObject(key);
   }

   @Nullable
   default <T> T putMetaObject(MetaKey<T> key, T obj) {
      return this.getMetaStore().putMetaObject(key, obj);
   }

   @Nullable
   default <T> T removeMetaObject(MetaKey<T> key) {
      return this.getMetaStore().removeMetaObject(key);
   }

   @Nullable
   default <T> T removeSerializedMetaObject(MetaKey<T> key) {
      return this.getMetaStore().removeSerializedMetaObject(key);
   }

   default boolean hasMetaObject(MetaKey<?> key) {
      return this.getMetaStore().hasMetaObject(key);
   }

   default void forEachMetaObject(IMetaStore.MetaEntryConsumer consumer) {
      this.getMetaStore().forEachMetaObject(consumer);
   }

   default void markMetaStoreDirty() {
      this.getMetaStore().markMetaStoreDirty();
   }

   default boolean consumeMetaStoreDirty() {
      return this.getMetaStore().consumeMetaStoreDirty();
   }

   @FunctionalInterface
   public interface MetaEntryConsumer {
      <T> void accept(int var1, T var2);
   }
}
