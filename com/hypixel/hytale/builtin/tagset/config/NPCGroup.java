package com.hypixel.hytale.builtin.tagset.config;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.tagset.TagSet;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import javax.annotation.Nonnull;

public class NPCGroup implements JsonAssetWithMap<String, IndexedLookupTableAssetMap<String, NPCGroup>>, TagSet {
   public static final AssetBuilderCodec<String, NPCGroup> CODEC = AssetBuilderCodec.builder(
         NPCGroup.class, NPCGroup::new, Codec.STRING, (t, k) -> t.id = k, t -> t.id, (asset, data) -> asset.data = data, asset -> asset.data
      )
      .documentation("Defines a group or collection of NPC types.")
      .<String[]>append(
         new KeyedCodec<>("IncludeRoles", Codec.STRING_ARRAY), (npcGroup, strings) -> npcGroup.includedRoles = strings, npcGroup -> npcGroup.includedRoles
      )
      .documentation("A list of individual types to include.")
      .add()
      .<String[]>append(
         new KeyedCodec<>("ExcludeRoles", Codec.STRING_ARRAY), (npcGroup, strings) -> npcGroup.excludedRoles = strings, npcGroup -> npcGroup.excludedRoles
      )
      .documentation("A list of individual types to exclude.")
      .add()
      .<String[]>append(
         new KeyedCodec<>("IncludeGroups", Codec.STRING_ARRAY),
         (npcGroup, strings) -> npcGroup.includedGroupTags = strings,
         npcGroup -> npcGroup.includedGroupTags
      )
      .documentation("A list of other groups to include.")
      .add()
      .<String[]>append(
         new KeyedCodec<>("ExcludeGroups", Codec.STRING_ARRAY),
         (npcGroup, strings) -> npcGroup.excludedGroupTags = strings,
         npcGroup -> npcGroup.excludedGroupTags
      )
      .documentation("A list of other groups to exclude.")
      .add()
      .build();
   @Nonnull
   public static final Codec<String> CHILD_ASSET_CODEC = new ContainedAssetCodec<>(NPCGroup.class, CODEC);
   public static final Codec<String[]> CHILD_ASSET_CODEC_ARRAY = new ArrayCodec<>(CHILD_ASSET_CODEC, String[]::new);
   public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(NPCGroup::getAssetStore));
   private static AssetStore<String, NPCGroup, IndexedLookupTableAssetMap<String, NPCGroup>> ASSET_STORE;
   protected AssetExtraInfo.Data data;
   protected String id;
   protected String[] includedGroupTags;
   protected String[] excludedGroupTags;
   protected String[] includedRoles;
   protected String[] excludedRoles;

   public static AssetStore<String, NPCGroup, IndexedLookupTableAssetMap<String, NPCGroup>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(NPCGroup.class);
      }

      return ASSET_STORE;
   }

   public static IndexedLookupTableAssetMap<String, NPCGroup> getAssetMap() {
      return (IndexedLookupTableAssetMap<String, NPCGroup>)getAssetStore().getAssetMap();
   }

   public NPCGroup(String id) {
      this.id = id;
   }

   protected NPCGroup() {
   }

   public String getId() {
      return this.id;
   }

   @Override
   public String[] getIncludedTagSets() {
      return this.includedGroupTags;
   }

   @Override
   public String[] getExcludedTagSets() {
      return this.excludedGroupTags;
   }

   @Override
   public String[] getIncludedTags() {
      return this.includedRoles;
   }

   @Override
   public String[] getExcludedTags() {
      return this.excludedRoles;
   }
}
