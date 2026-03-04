package com.hypixel.hytale.codec;

import com.hypixel.hytale.codec.util.RawJsonReader;
import java.io.IOException;
import javax.annotation.Nullable;

public interface RawJsonCodec<T> {
   @Nullable
   @Deprecated
   default T decodeJson(RawJsonReader reader) throws IOException {
      return this.decodeJson(reader, EmptyExtraInfo.EMPTY);
   }

   @Nullable
   T decodeJson(RawJsonReader var1, ExtraInfo var2) throws IOException;
}
