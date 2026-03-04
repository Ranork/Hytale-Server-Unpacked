package com.hypixel.hytale.server.spawning.assets.spawnsuppression;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.IndexedAssetMap;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.tagset.config.NPCGroup;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.Validators;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class SpawnSuppression implements JsonAssetWithMap<String, IndexedAssetMap<String, SpawnSuppression>> {
   public static final AssetBuilderCodec<String, SpawnSuppression> CODEC = AssetBuilderCodec.builder(
         SpawnSuppression.class, SpawnSuppression::new, Codec.STRING, (t, k) -> t.id = k, t -> t.id, (asset, data) -> asset.data = data, asset -> asset.data
      )
      .documentation("A configuration designed to prevent certain NPCs spawns within a given region.")
      .<Double>appendInherited(
         new KeyedCodec<>("SuppressionRadius", Codec.DOUBLE),
         (suppressor, d) -> suppressor.radius = d,
         suppressor -> suppressor.radius,
         (suppressor, parent) -> suppressor.radius = parent.radius
      )
      .documentation(
         "The radius this spawn suppression should cover. Any chunk which falls even partially within this radius will be affected by the suppression on the x and z axes, but will use exact distance for the y axis. This allows NPCs to continue to spawn in caves below the position or in the skies above, but is slightly more efficient and provides no noticeable differences in world spawns."
      )
      .addValidator(Validators.greaterThan(0.0))
      .add()
      .<String[]>appendInherited(
         new KeyedCodec<>("SuppressedGroups", Codec.STRING_ARRAY),
         (suppressor, s) -> suppressor.suppressedGroups = s,
         suppressor -> suppressor.suppressedGroups,
         (suppressor, parent) -> suppressor.suppressedGroups = parent.suppressedGroups
      )
      .documentation("An array of NPCGroup ids that will be suppressed.")
      .addValidator(NPCGroup.VALIDATOR_CACHE.getArrayValidator())
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("SuppressSpawnMarkers", Codec.BOOLEAN),
         (suppressor, b) -> suppressor.suppressSpawnMarkers = b,
         suppressor -> suppressor.suppressSpawnMarkers,
         (suppressor, parent) -> suppressor.suppressSpawnMarkers = parent.suppressSpawnMarkers
      )
      .documentation(
         "Whether or not to suppress any spawn markers within the range of this suppression. If set to true, any spawn marker within this range will cease to function while the suppression exists"
      )
      .add()
      .afterDecode(suppressor -> {
         if (suppressor.suppressedGroups != null && suppressor.suppressedGroups.length > 0) {
            IndexedLookupTableAssetMap<String, NPCGroup> npcGroups = NPCGroup.getAssetMap();
            IntOpenHashSet set = new IntOpenHashSet();

            for (String group : suppressor.suppressedGroups) {
               set.add(npcGroups.getIndex(group));
            }

            suppressor.suppressedGroupIds = set.toIntArray();
         }
      })
      .build();
   public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(SpawnSuppression::getAssetStore));
   private static AssetStore<String, SpawnSuppression, IndexedAssetMap<String, SpawnSuppression>> ASSET_STORE;
   private AssetExtraInfo.Data data;
   protected String id;
   protected double radius = 10.0;
   protected String[] suppressedGroups;
   protected int[] suppressedGroupIds;
   protected boolean suppressSpawnMarkers;

   public static AssetStore<String, SpawnSuppression, IndexedAssetMap<String, SpawnSuppression>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(SpawnSuppression.class);
      }

      return ASSET_STORE;
   }

   public static IndexedAssetMap<String, SpawnSuppression> getAssetMap() {
      return (IndexedAssetMap<String, SpawnSuppression>)getAssetStore().getAssetMap();
   }

   public SpawnSuppression(String id) {
      this.id = id;
   }

   public SpawnSuppression(String id, double radius, String[] suppressedGroups, int[] suppressedGroupIds, boolean suppressSpawnMarkers) {
      this.id = id;
      this.radius = radius;
      this.suppressedGroups = suppressedGroups;
      this.suppressedGroupIds = suppressedGroupIds;
      this.suppressSpawnMarkers = suppressSpawnMarkers;
   }

   protected SpawnSuppression() {
   }

   public String getId() {
      return this.id;
   }

   public double getRadius() {
      return this.radius;
   }

   public int[] getSuppressedGroupIds() {
      return this.suppressedGroupIds;
   }

   public boolean isSuppressSpawnMarkers() {
      return this.suppressSpawnMarkers;
   }

   @Nonnull
   @Override
   public String toString() {
      return "SpawnSuppression{radius="
         + this.radius
         + ", suppressedGroups="
         + Arrays.toString((Object[])this.suppressedGroups)
         + ", suppressSpawnMarkers="
         + this.suppressSpawnMarkers
         + "}";
   }
}
