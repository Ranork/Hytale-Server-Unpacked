package com.hypixel.hytale.codec.lookup;

import com.hypixel.hytale.codec.Codec;
import javax.annotation.Nonnull;

public class CodecMapCodec<T> extends StringCodecMapCodec<T, Codec<? extends T>> {
   public CodecMapCodec() {
   }

   public CodecMapCodec(String id) {
      super(id);
   }

   public CodecMapCodec(boolean allowDefault) {
      super(allowDefault);
   }

   public CodecMapCodec(String key, boolean allowDefault) {
      super(key, allowDefault);
   }

   public CodecMapCodec(String key, boolean allowDefault, boolean encodeDefaultKey) {
      super(key, allowDefault, encodeDefaultKey);
   }

   @Nonnull
   public CodecMapCodec<T> register(String id, Class<? extends T> aClass, Codec<? extends T> codec) {
      super.register(id, aClass, codec);
      return this;
   }

   @Nonnull
   public CodecMapCodec<T> register(@Nonnull Priority priority, @Nonnull String id, Class<? extends T> aClass, Codec<? extends T> codec) {
      super.register(priority, id, aClass, codec);
      return this;
   }
}
