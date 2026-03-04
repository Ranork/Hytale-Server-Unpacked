package com.hypixel.hytale.codec.lookup;

import com.hypixel.hytale.codec.Codec;
import javax.annotation.Nonnull;

public class ObjectCodecMapCodec<K, T> extends ACodecMapCodec<K, T, Codec<? extends T>> {
   public ObjectCodecMapCodec(Codec<K> keyCodec) {
      super(keyCodec);
   }

   public ObjectCodecMapCodec(Codec<K> keyCodec, boolean allowDefault) {
      super(keyCodec, allowDefault);
   }

   public ObjectCodecMapCodec(String id, Codec<K> keyCodec) {
      super(id, keyCodec);
   }

   public ObjectCodecMapCodec(String key, Codec<K> keyCodec, boolean allowDefault) {
      super(key, keyCodec, allowDefault);
   }

   public ObjectCodecMapCodec(String key, Codec<K> keyCodec, boolean allowDefault, boolean encodeDefaultKey) {
      super(key, keyCodec, allowDefault, encodeDefaultKey);
   }

   @Nonnull
   public ObjectCodecMapCodec<K, T> register(K id, Class<? extends T> aClass, Codec<? extends T> codec) {
      super.register(id, aClass, codec);
      return this;
   }

   @Nonnull
   public ObjectCodecMapCodec<K, T> register(@Nonnull Priority priority, K id, Class<? extends T> aClass, Codec<? extends T> codec) {
      super.register(priority, id, aClass, codec);
      return this;
   }
}
