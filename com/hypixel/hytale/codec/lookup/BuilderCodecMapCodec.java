package com.hypixel.hytale.codec.lookup;

import com.hypixel.hytale.codec.builder.BuilderCodec;

public class BuilderCodecMapCodec<T> extends StringCodecMapCodec<T, BuilderCodec<? extends T>> {
   public BuilderCodecMapCodec() {
   }

   public BuilderCodecMapCodec(boolean allowDefault) {
      super(allowDefault);
   }

   public BuilderCodecMapCodec(String id) {
      super(id);
   }

   public BuilderCodecMapCodec(String key, boolean allowDefault) {
      super(key, allowDefault);
   }

   public T getDefault() {
      return (T)this.getDefaultCodec().getDefaultValue();
   }
}
