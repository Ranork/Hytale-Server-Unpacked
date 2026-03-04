package com.hypixel.hytale.codec;

public interface WrappedCodec<T> {
   Codec<T> getChildCodec();
}
