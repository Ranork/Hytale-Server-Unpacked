package com.hypixel.hytale.server.core.plugin.registry;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.lookup.MapKeyMapCodec;
import com.hypixel.hytale.function.consumer.BooleanConsumer;
import java.util.List;
import javax.annotation.Nonnull;

public class MapKeyMapRegistry<V> implements IRegistry {
   protected final MapKeyMapCodec<V> mapCodec;
   protected final List<BooleanConsumer> unregister;

   public MapKeyMapRegistry(List<BooleanConsumer> unregister, MapKeyMapCodec<V> mapCodec) {
      this.unregister = unregister;
      this.mapCodec = mapCodec;
   }

   @Nonnull
   public <T extends V> MapKeyMapRegistry<V> register(@Nonnull Class<T> tClass, @Nonnull String id, @Nonnull Codec<T> codec) {
      this.mapCodec.register(tClass, id, codec);
      this.unregister.add(shutdown -> {
         if (!shutdown) {
            com.hypixel.hytale.assetstore.AssetRegistry.ASSET_LOCK.writeLock().lock();

            try {
               this.mapCodec.unregister(tClass);
            } finally {
               com.hypixel.hytale.assetstore.AssetRegistry.ASSET_LOCK.writeLock().unlock();
            }
         }
      });
      return this;
   }

   @Override
   public void shutdown() {
   }
}
