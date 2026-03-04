package com.hypixel.hytale.server.npc.config;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.map.EnumMapCodec;
import com.hypixel.hytale.server.core.asset.type.attitude.Attitude;
import java.util.Collections;
import java.util.Map;

public class AttitudeGroup implements JsonAssetWithMap<String, IndexedLookupTableAssetMap<String, AttitudeGroup>> {
   public static final AssetBuilderCodec<String, AttitudeGroup> CODEC = AssetBuilderCodec.builder(
         AttitudeGroup.class, AttitudeGroup::new, Codec.STRING, (t, k) -> t.id = k, t -> t.id, (asset, data) -> asset.data = data, asset -> asset.data
      )
      .documentation("Defines attitudes towards specific groups of NPCs.")
      .<Map<Attitude, String[]>>append(
         new KeyedCodec<>("Groups", new EnumMapCodec<>(Attitude.class, Codec.STRING_ARRAY)),
         (group, map) -> group.attitudeGroups = map,
         group -> group.attitudeGroups
      )
      .documentation("A map of attitudes to NPC groups.")
      .add()
      .build();
   private static IndexedLookupTableAssetMap<String, AttitudeGroup> ASSET_MAP;
   protected AssetExtraInfo.Data data;
   protected String id;
   protected Map<Attitude, String[]> attitudeGroups = Collections.emptyMap();

   public static IndexedLookupTableAssetMap<String, AttitudeGroup> getAssetMap() {
      if (ASSET_MAP == null) {
         ASSET_MAP = (IndexedLookupTableAssetMap<String, AttitudeGroup>)AssetRegistry.getAssetStore(AttitudeGroup.class).getAssetMap();
      }

      return ASSET_MAP;
   }

   public AttitudeGroup(String id) {
      this.id = id;
   }

   protected AttitudeGroup() {
   }

   public String getId() {
      return this.id;
   }

   public Map<Attitude, String[]> getAttitudeGroups() {
      return this.attitudeGroups;
   }
}
