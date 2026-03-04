package com.hypixel.hytale.codec;

import com.hypixel.hytale.codec.util.RawJsonReader;
import java.io.IOException;
import javax.annotation.Nullable;

public interface RawJsonInheritCodec<T> extends RawJsonCodec<T> {
   @Nullable
   T decodeAndInheritJson(RawJsonReader var1, T var2, ExtraInfo var3) throws IOException;

   void decodeAndInheritJson(RawJsonReader var1, T var2, T var3, ExtraInfo var4) throws IOException;
}
