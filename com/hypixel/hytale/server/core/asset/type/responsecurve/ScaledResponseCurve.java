package com.hypixel.hytale.server.core.asset.type.responsecurve;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetCodecMapCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.lookup.Priority;
import javax.annotation.Nonnull;

public abstract class ScaledResponseCurve implements JsonAssetWithMap<String, DefaultAssetMap<String, ScaledResponseCurve>> {
   public static final AssetCodecMapCodec<String, ScaledResponseCurve> CODEC = new AssetCodecMapCodec<String, ScaledXResponseCurve>(
         Codec.STRING, (t, k) -> t.id = k, t -> t.id, (t, data) -> t.data = data, t -> t.data, true
      )
      .register(Priority.DEFAULT, "Default", ScaledXResponseCurve.class, ScaledXResponseCurve.CODEC);
   protected AssetExtraInfo.Data data;
   protected String id;

   public ScaledResponseCurve(String id) {
      this.id = id;
   }

   protected ScaledResponseCurve() {
   }

   public abstract double computeY(double var1);

   public String getId() {
      return this.id;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ScaledResponseCurve{data=" + this.data + ", id='" + this.id + "'}";
   }

   static {
      CODEC.register("Switch", ScaledSwitchResponseCurve.class, ScaledSwitchResponseCurve.CODEC);
   }
}
