package com.hypixel.hytale.server.core.meta;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.DirectDecodeCodec;
import com.hypixel.hytale.codec.ExtraInfo;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonDocument;
import org.bson.BsonValue;

public abstract class AbstractMetaStore<K> implements IMetaStoreImpl<K> {
   protected final K parent;
   protected final IMetaRegistry<K> registry;
   @Nonnull
   private final BsonDocument unknownValues;
   @Nonnull
   private final IntSet notUnknownKeys;
   @Nullable
   private BsonDocument cachedEncoded;
   private boolean dirty;
   private boolean bypassEncodedCache;

   public AbstractMetaStore(K parent, IMetaRegistry<K> registry, boolean bypassEncodedCache) {
      this.parent = parent;
      this.registry = registry;
      this.unknownValues = new BsonDocument();
      this.notUnknownKeys = new IntOpenHashSet();
      this.dirty = false;
      this.bypassEncodedCache = bypassEncodedCache;
   }

   protected abstract <T> T get0(MetaKey<T> var1);

   @Nonnull
   @Override
   public IMetaStoreImpl<K> getMetaStore() {
      return this;
   }

   @Override
   public IMetaRegistry<K> getRegistry() {
      return this.registry;
   }

   @Override
   public void forEachUnknownEntry(BiConsumer<String, BsonValue> consumer) {
      this.unknownValues.forEach(consumer);
   }

   @Override
   public final void markMetaStoreDirty() {
      this.dirty = true;
      this.cachedEncoded = null;
   }

   @Override
   public final boolean consumeMetaStoreDirty() {
      boolean previous = this.dirty;
      this.dirty = false;
      return previous;
   }

   protected <T> T decodeOrNewMetaObject(MetaKey<T> key) {
      return key instanceof PersistentMetaKey && this.tryDecodeUnknownKey((PersistentMetaKey<T>)key)
         ? this.get0(key)
         : this.registry.newMetaObject(key, this.parent);
   }

   protected <T> boolean tryDecodeUnknownKey(@Nonnull PersistentMetaKey<T> key) {
      if (!this.notUnknownKeys.add(key.getId())) {
         return false;
      } else {
         BsonValue value = this.unknownValues.get(key.getKey());
         if (value != null) {
            Codec<T> codec = key.getCodec();
            T obj;
            if (codec instanceof DirectDecodeCodec) {
               obj = this.registry.newMetaObject(key, this.parent);
               ((DirectDecodeCodec)codec).decode(value, obj, null);
            } else {
               obj = codec.decode(value, null);
            }

            this.unknownValues.remove(key.getKey());
            this.putMetaObject(key, obj);
            return true;
         } else {
            return false;
         }
      }
   }

   @Nonnull
   @Override
   public BsonDocument encode(final ExtraInfo extraInfo) {
      if (!this.bypassEncodedCache && this.cachedEncoded != null) {
         return this.cachedEncoded;
      } else {
         final BsonDocument document = new BsonDocument();
         document.putAll(this.unknownValues);
         this.getRegistry().forEachMetaEntry(this, new IMetaRegistry.MetaEntryConsumer() {
            @Override
            public <T> void accept(MetaKey<T> key, T value) {
               if (key instanceof PersistentMetaKey<T> persistentKey) {
                  document.put(persistentKey.getKey(), persistentKey.getCodec().encode(value, extraInfo));
               }
            }
         });
         if (!this.bypassEncodedCache) {
            this.cachedEncoded = document;
         }

         return document;
      }
   }

   @Override
   public void decode(@Nonnull BsonDocument document, ExtraInfo extraInfo) {
      if (!Codec.isNullBsonValue(document)) {
         for (Entry<String, BsonValue> entry : document.entrySet()) {
            String key = entry.getKey();
            BsonValue value = entry.getValue();
            PersistentMetaKey metaKey = this.getRegistry().getMetaKeyForCodecKey(key);
            if (metaKey == null) {
               this.unknownValues.put(key, value);
            } else if (metaKey.getCodec() instanceof DirectDecodeCodec) {
               Object obj = this.registry.newMetaObject(metaKey, this.parent);
               ((DirectDecodeCodec)metaKey.getCodec()).decode(value, obj, extraInfo);
               this.putMetaObject(metaKey, obj);
            } else {
               this.putMetaObject(metaKey, metaKey.getCodec().decode(value, extraInfo));
            }
         }
      }
   }
}
