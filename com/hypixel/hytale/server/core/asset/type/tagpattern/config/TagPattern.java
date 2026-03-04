package com.hypixel.hytale.server.core.asset.type.tagpattern.config;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetCodecMapCodec;
import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.lang.ref.SoftReference;
import javax.annotation.Nonnull;

public abstract class TagPattern
   implements JsonAssetWithMap<String, IndexedLookupTableAssetMap<String, TagPattern>>,
   NetworkSerializable<com.hypixel.hytale.protocol.TagPattern> {
   public static final AssetCodecMapCodec<String, TagPattern> CODEC = new AssetCodecMapCodec<>(
      "Op", Codec.STRING, (t, k) -> t.id = k, t -> t.id, (t, data) -> t.data = data, t -> t.data
   );
   public static final BuilderCodec<TagPattern> BASE_CODEC = BuilderCodec.abstractBuilder(TagPattern.class).build();
   public static final Codec<String> CHILD_ASSET_CODEC = new ContainedAssetCodec<>(TagPattern.class, CODEC);
   public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(TagPattern::getAssetStore));
   private static AssetStore<String, TagPattern, IndexedLookupTableAssetMap<String, TagPattern>> ASSET_STORE;
   private AssetExtraInfo.Data data;
   protected String id;
   protected SoftReference<com.hypixel.hytale.protocol.TagPattern> cachedPacket;

   public static AssetStore<String, TagPattern, IndexedLookupTableAssetMap<String, TagPattern>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(TagPattern.class);
      }

      return ASSET_STORE;
   }

   public static IndexedLookupTableAssetMap<String, TagPattern> getAssetMap() {
      return (IndexedLookupTableAssetMap<String, TagPattern>)getAssetStore().getAssetMap();
   }

   public String getId() {
      return this.id;
   }

   public abstract boolean test(Int2ObjectMap<IntSet> var1);

   @Nonnull
   @Override
   public String toString() {
      return "TagPattern{id='" + this.id + "'}";
   }
}
