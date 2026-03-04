package com.hypixel.hytale.builtin.adventure.reputation.assets;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import javax.annotation.Nonnull;

public class ReputationGroup implements JsonAssetWithMap<String, DefaultAssetMap<String, ReputationGroup>> {
   @Nonnull
   public static final AssetBuilderCodec<String, ReputationGroup> CODEC = AssetBuilderCodec.builder(
         ReputationGroup.class, ReputationGroup::new, Codec.STRING, (t, k) -> t.id = k, t -> t.id, (t, data) -> t.data = data, t -> t.data
      )
      .addField(
         new KeyedCodec<>("NPCGroups", Codec.STRING_ARRAY), (reputationRank, s) -> reputationRank.npcGroups = s, reputationRank -> reputationRank.npcGroups
      )
      .addField(
         new KeyedCodec<>("InitialReputationValue", Codec.INTEGER),
         (reputationRank, s) -> reputationRank.initialReputationValue = s,
         reputationRank -> reputationRank.initialReputationValue
      )
      .build();
   @Nonnull
   public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(ReputationGroup::getAssetStore));
   private static AssetStore<String, ReputationGroup, DefaultAssetMap<String, ReputationGroup>> ASSET_STORE;
   protected AssetExtraInfo.Data data;
   protected String id;
   protected String[] npcGroups;
   protected int initialReputationValue;

   @Nonnull
   public static AssetStore<String, ReputationGroup, DefaultAssetMap<String, ReputationGroup>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(ReputationGroup.class);
      }

      return ASSET_STORE;
   }

   public static DefaultAssetMap<String, ReputationGroup> getAssetMap() {
      return (DefaultAssetMap<String, ReputationGroup>)getAssetStore().getAssetMap();
   }

   public ReputationGroup(String id, String[] npcGroups, int initialReputationValue) {
      this.id = id;
      this.npcGroups = npcGroups;
      this.initialReputationValue = initialReputationValue;
   }

   protected ReputationGroup() {
   }

   public String getId() {
      return this.id;
   }

   public String[] getNpcGroups() {
      return this.npcGroups;
   }

   public int getInitialReputationValue() {
      return this.initialReputationValue;
   }
}
