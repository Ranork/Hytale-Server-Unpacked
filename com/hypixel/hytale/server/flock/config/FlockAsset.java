package com.hypixel.hytale.server.flock.config;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetCodecMapCodec;
import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.Priority;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.common.util.ArrayUtil;
import java.util.Arrays;
import javax.annotation.Nonnull;

public abstract class FlockAsset implements JsonAssetWithMap<String, IndexedLookupTableAssetMap<String, FlockAsset>> {
   public static final BuilderCodec<FlockAsset> ABSTRACT_CODEC = BuilderCodec.abstractBuilder(FlockAsset.class)
      .documentation("A flock definition.")
      .<Integer>appendInherited(
         new KeyedCodec<>("MaxGrowSize", Codec.INTEGER),
         (flock, i) -> flock.maxGrowSize = i,
         flock -> flock.maxGrowSize,
         (flock, parent) -> flock.maxGrowSize = parent.maxGrowSize
      )
      .documentation(
         "The maximum size a flock can possibly grow to after spawning. It is technically possible to spawn a flock without specifying a definition (e.g. via a command), in which case the maximum grow size is irrelevant."
      )
      .addValidator(Validators.greaterThanOrEqual(0))
      .add()
      .<String[]>appendInherited(
         new KeyedCodec<>("BlockedRoles", Codec.STRING_ARRAY),
         (flock, o) -> flock.blockedRoles = o,
         flock -> flock.blockedRoles,
         (flock, parent) -> flock.blockedRoles = parent.blockedRoles
      )
      .documentation(
         "An array of roles that will not be allowed to join this flock once it has been spawned. This is used to exclude roles from the list of allowed roles in the NPC configuration of the initial leader."
      )
      .addValidator(Validators.uniqueInArray())
      .add()
      .build();
   public static final AssetCodecMapCodec<String, FlockAsset> CODEC = new AssetCodecMapCodec<String, RangeSizeFlockAsset>(
         Codec.STRING, (t, k) -> t.id = k, t -> t.id, (t, data) -> t.data = data, t -> t.data, true
      )
      .register(Priority.DEFAULT, "Default", RangeSizeFlockAsset.class, RangeSizeFlockAsset.CODEC);
   public static final Codec<String> CHILD_ASSET_CODEC = new ContainedAssetCodec<>(FlockAsset.class, CODEC);
   public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(FlockAsset::getAssetStore));
   private static AssetStore<String, FlockAsset, IndexedLookupTableAssetMap<String, FlockAsset>> ASSET_STORE;
   private AssetExtraInfo.Data data;
   protected String id;
   protected int maxGrowSize = 8;
   protected String[] blockedRoles = ArrayUtil.EMPTY_STRING_ARRAY;

   public static AssetStore<String, FlockAsset, IndexedLookupTableAssetMap<String, FlockAsset>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(FlockAsset.class);
      }

      return ASSET_STORE;
   }

   public static IndexedLookupTableAssetMap<String, FlockAsset> getAssetMap() {
      return (IndexedLookupTableAssetMap<String, FlockAsset>)getAssetStore().getAssetMap();
   }

   protected FlockAsset() {
   }

   protected FlockAsset(String id) {
      this.id = id;
   }

   public String getId() {
      return this.id;
   }

   public abstract int getMinFlockSize();

   public abstract int pickFlockSize();

   public int getMaxGrowSize() {
      return this.maxGrowSize;
   }

   public String[] getBlockedRoles() {
      return this.blockedRoles;
   }

   @Nonnull
   @Override
   public String toString() {
      return "FlockAsset{id='" + this.id + "', maxGrowSize=" + this.maxGrowSize + ", blockedRoles=" + Arrays.toString((Object[])this.blockedRoles) + "}";
   }

   static {
      CODEC.register("Weighted", WeightedSizeFlockAsset.class, WeightedSizeFlockAsset.CODEC);
   }
}
