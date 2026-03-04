package com.hypixel.hytale.server.core.plugin.registry;

import com.hypixel.hytale.assetstore.JsonAsset;
import com.hypixel.hytale.assetstore.codec.AssetCodecMapCodec;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.Priority;
import com.hypixel.hytale.codec.lookup.StringCodecMapCodec;
import com.hypixel.hytale.function.consumer.BooleanConsumer;
import java.util.List;
import javax.annotation.Nonnull;

public class CodecMapRegistry<T, C extends Codec<? extends T>> implements IRegistry {
   protected final StringCodecMapCodec<T, C> mapCodec;
   protected final List<BooleanConsumer> unregister;

   public CodecMapRegistry(List<BooleanConsumer> unregister, StringCodecMapCodec<T, C> mapCodec) {
      this.unregister = unregister;
      this.mapCodec = mapCodec;
   }

   @Nonnull
   public CodecMapRegistry<T, C> register(String id, Class<? extends T> aClass, C codec) {
      this.mapCodec.register(id, aClass, codec);
      this.unregister.add(shutdown -> {
         com.hypixel.hytale.assetstore.AssetRegistry.ASSET_LOCK.writeLock().lock();

         try {
            this.mapCodec.remove(aClass);
         } finally {
            com.hypixel.hytale.assetstore.AssetRegistry.ASSET_LOCK.writeLock().unlock();
         }
      });
      return this;
   }

   @Nonnull
   public CodecMapRegistry<T, C> register(@Nonnull Priority priority, @Nonnull String id, Class<? extends T> aClass, C codec) {
      this.mapCodec.register(priority, id, aClass, codec);
      this.unregister.add(shutdown -> {
         com.hypixel.hytale.assetstore.AssetRegistry.ASSET_LOCK.writeLock().lock();

         try {
            this.mapCodec.remove(aClass);
         } finally {
            com.hypixel.hytale.assetstore.AssetRegistry.ASSET_LOCK.writeLock().unlock();
         }
      });
      return this;
   }

   @Override
   public void shutdown() {
   }

   public static class Assets<T extends JsonAsset<?>, C extends Codec<? extends T>> extends CodecMapRegistry<T, C> {
      public Assets(List<BooleanConsumer> unregister, StringCodecMapCodec<T, C> mapCodec) {
         super(unregister, mapCodec);
      }

      @Nonnull
      public CodecMapRegistry.Assets<T, C> register(@Nonnull String id, Class<? extends T> aClass, BuilderCodec<? extends T> codec) {
         ((AssetCodecMapCodec)this.mapCodec).register(id, aClass, codec);
         this.unregister.add(shutdown -> {
            com.hypixel.hytale.assetstore.AssetRegistry.ASSET_LOCK.writeLock().lock();

            try {
               this.mapCodec.remove(aClass);
            } finally {
               com.hypixel.hytale.assetstore.AssetRegistry.ASSET_LOCK.writeLock().unlock();
            }
         });
         return this;
      }

      @Nonnull
      public CodecMapRegistry.Assets<T, C> register(@Nonnull Priority priority, @Nonnull String id, Class<? extends T> aClass, BuilderCodec<? extends T> codec) {
         ((AssetCodecMapCodec)this.mapCodec).register(priority, id, aClass, codec);
         this.unregister.add(shutdown -> {
            com.hypixel.hytale.assetstore.AssetRegistry.ASSET_LOCK.writeLock().lock();

            try {
               this.mapCodec.remove(aClass);
            } finally {
               com.hypixel.hytale.assetstore.AssetRegistry.ASSET_LOCK.writeLock().unlock();
            }
         });
         return this;
      }
   }
}
