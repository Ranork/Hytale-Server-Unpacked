package com.hypixel.hytale.server.core.asset.type.responsecurve.config;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetCodecMapCodec;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import java.lang.ref.WeakReference;
import javax.annotation.Nonnull;

public abstract class ResponseCurve implements JsonAssetWithMap<String, IndexedLookupTableAssetMap<String, ResponseCurve>> {
   public static final AssetCodecMapCodec<String, ResponseCurve> CODEC = new AssetCodecMapCodec<>(
      Codec.STRING, (t, k) -> t.id = k, t -> t.id, (t, data) -> t.extraData = data, t -> t.extraData
   );
   public static final BuilderCodec<ResponseCurve> BASE_CODEC = BuilderCodec.abstractBuilder(ResponseCurve.class)
      .afterDecode(responseCurve -> responseCurve.reference = new WeakReference<>(responseCurve))
      .build();
   public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(ResponseCurve::getAssetStore));
   private static AssetStore<String, ResponseCurve, IndexedLookupTableAssetMap<String, ResponseCurve>> ASSET_STORE;
   protected AssetExtraInfo.Data extraData;
   protected String id;
   protected WeakReference<ResponseCurve> reference;

   public static AssetStore<String, ResponseCurve, IndexedLookupTableAssetMap<String, ResponseCurve>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(ResponseCurve.class);
      }

      return ASSET_STORE;
   }

   public static IndexedLookupTableAssetMap<String, ResponseCurve> getAssetMap() {
      return (IndexedLookupTableAssetMap<String, ResponseCurve>)getAssetStore().getAssetMap();
   }

   public ResponseCurve(String id) {
      this.id = id;
   }

   protected ResponseCurve() {
   }

   public String getId() {
      return this.id;
   }

   public WeakReference<ResponseCurve> getReference() {
      return this.reference;
   }

   public abstract double computeY(double var1);

   @Nonnull
   @Override
   public String toString() {
      return "ResponseCurve{id='" + this.id + "'}";
   }

   static {
      CODEC.register("Exponential", ExponentialResponseCurve.class, ExponentialResponseCurve.CODEC);
      CODEC.register("Logistic", LogisticResponseCurve.class, LogisticResponseCurve.CODEC);
      CODEC.register("SineWave", SineWaveResponseCurve.class, SineWaveResponseCurve.CODEC);
   }

   public static class Reference {
      private int index;
      private WeakReference<ResponseCurve> reference;

      public Reference(int index, @Nonnull ResponseCurve responseCurve) {
         this.index = index;
         this.reference = responseCurve.getReference();
      }

      @Nonnull
      public ResponseCurve get() {
         ResponseCurve responseCurve = this.reference.get();
         if (responseCurve == null) {
            responseCurve = ResponseCurve.getAssetMap().getAsset(this.index);
            this.reference = responseCurve.getReference();
         }

         return responseCurve;
      }
   }
}
