package com.hypixel.hytale.codec;

import javax.annotation.Nullable;
import org.bson.BsonDocument;

public interface InheritCodec<T> extends Codec<T>, RawJsonInheritCodec<T> {
   @Nullable
   T decodeAndInherit(BsonDocument var1, T var2, ExtraInfo var3);

   void decodeAndInherit(BsonDocument var1, T var2, T var3, ExtraInfo var4);
}
