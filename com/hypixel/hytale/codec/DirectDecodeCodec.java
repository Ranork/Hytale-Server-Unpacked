package com.hypixel.hytale.codec;

import org.bson.BsonValue;

public interface DirectDecodeCodec<T> extends Codec<T> {
   void decode(BsonValue var1, T var2, ExtraInfo var3);
}
