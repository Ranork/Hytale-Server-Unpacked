package com.hypixel.hytale.server.core.asset.type.blocktype.config;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.StringIntegerCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;

public class BlockMigration implements JsonAssetWithMap<Integer, DefaultAssetMap<Integer, BlockMigration>> {
   public static final AssetBuilderCodec<Integer, BlockMigration> CODEC = AssetBuilderCodec.builder(
         BlockMigration.class,
         BlockMigration::new,
         new StringIntegerCodec(),
         (blockMigration, i) -> blockMigration.id = i,
         blockMigration -> blockMigration.id,
         (asset, data) -> asset.data = data,
         asset -> asset.data
      )
      .addField(
         new KeyedCodec<>("DirectMigrations", new MapCodec<>(Codec.STRING, HashMap::new)),
         (blockMigration, document) -> blockMigration.directMigrations = document,
         blockMigration -> blockMigration.directMigrations
      )
      .addField(
         new KeyedCodec<>("NameMigrations", new MapCodec<>(Codec.STRING, HashMap::new)),
         (blockMigration, document) -> blockMigration.nameMigrations = document,
         blockMigration -> blockMigration.nameMigrations
      )
      .build();
   private static DefaultAssetMap<Integer, BlockMigration> ASSET_MAP;
   protected AssetExtraInfo.Data data;
   protected int id;
   protected Map<String, String> directMigrations = Collections.emptyMap();
   protected Map<String, String> nameMigrations = Collections.emptyMap();

   public static DefaultAssetMap<Integer, BlockMigration> getAssetMap() {
      if (ASSET_MAP == null) {
         ASSET_MAP = (DefaultAssetMap<Integer, BlockMigration>)AssetRegistry.getAssetStore(BlockMigration.class).getAssetMap();
      }

      return ASSET_MAP;
   }

   public BlockMigration(int id, Map<String, String> directMigrations, Map<String, String> nameMigrations) {
      this.id = id;
      this.directMigrations = directMigrations;
      this.nameMigrations = nameMigrations;
   }

   protected BlockMigration() {
   }

   @Nonnull
   public Integer getId() {
      return this.id;
   }

   @Nonnull
   public String getMigration(@Nonnull String blockTypeKey) {
      String direct = this.directMigrations.get(blockTypeKey);
      if (direct != null) {
         return direct;
      } else {
         String name = this.nameMigrations.get(blockTypeKey);
         return name != null ? name : blockTypeKey;
      }
   }

   public String getDirectMigration(String blockTypeKey) {
      return this.directMigrations.getOrDefault(blockTypeKey, blockTypeKey);
   }

   public String getNameMigration(@Nonnull String blockTypeKey) {
      return this.nameMigrations.getOrDefault(blockTypeKey, blockTypeKey);
   }

   public Map<String, String> getDirectMigrations() {
      return this.directMigrations;
   }

   public Map<String, String> getNameMigrations() {
      return this.nameMigrations;
   }

   @Nonnull
   @Override
   public String toString() {
      return "BlockMigration{id='" + this.id + "', directMigrations=" + this.directMigrations + ", nameMigrations=" + this.nameMigrations + "}";
   }
}
