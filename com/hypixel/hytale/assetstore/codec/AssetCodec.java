package com.hypixel.hytale.assetstore.codec;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.JsonAsset;
import com.hypixel.hytale.codec.InheritCodec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.codec.validation.ValidatableCodec;
import java.io.IOException;
import javax.annotation.Nullable;

public interface AssetCodec<K, T extends JsonAsset<K>> extends InheritCodec<T>, ValidatableCodec<T> {
   KeyedCodec<K> getKeyCodec();

   KeyedCodec<K> getParentCodec();

   @Nullable
   AssetExtraInfo.Data getData(T var1);

   T decodeJsonAsset(RawJsonReader var1, AssetExtraInfo<K> var2) throws IOException;

   T decodeAndInheritJsonAsset(RawJsonReader var1, T var2, AssetExtraInfo<K> var3) throws IOException;
}
