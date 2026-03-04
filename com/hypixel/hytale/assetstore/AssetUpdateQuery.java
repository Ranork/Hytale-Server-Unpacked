package com.hypixel.hytale.assetstore;

import javax.annotation.Nonnull;

public class AssetUpdateQuery {
   public static final AssetUpdateQuery DEFAULT = new AssetUpdateQuery(AssetUpdateQuery.RebuildCache.DEFAULT);
   public static final AssetUpdateQuery DEFAULT_NO_REBUILD = new AssetUpdateQuery(AssetUpdateQuery.RebuildCache.NO_REBUILD);
   private final boolean disableAssetCompare;
   private final AssetUpdateQuery.RebuildCache rebuildCache;

   public AssetUpdateQuery(boolean disableAssetCompare, AssetUpdateQuery.RebuildCache rebuildCache) {
      this.disableAssetCompare = disableAssetCompare;
      this.rebuildCache = rebuildCache;
   }

   public AssetUpdateQuery(AssetUpdateQuery.RebuildCache rebuildCache) {
      this(AssetStore.DISABLE_ASSET_COMPARE, rebuildCache);
   }

   public boolean isDisableAssetCompare() {
      return this.disableAssetCompare;
   }

   @Nonnull
   public AssetUpdateQuery.RebuildCache getRebuildCache() {
      return this.rebuildCache;
   }

   @Nonnull
   @Override
   public String toString() {
      return "AssetUpdateQuery{rebuildCache=" + this.rebuildCache + "}";
   }

   public static class RebuildCache {
      public static final AssetUpdateQuery.RebuildCache DEFAULT = new AssetUpdateQuery.RebuildCache(true, true, true, true, true, true);
      public static final AssetUpdateQuery.RebuildCache NO_REBUILD = new AssetUpdateQuery.RebuildCache(false, false, false, false, false, false);
      private final boolean blockTextures;
      private final boolean models;
      private final boolean modelTextures;
      private final boolean mapGeometry;
      private final boolean itemIcons;
      private final boolean commonAssetsRebuild;

      public RebuildCache(boolean blockTextures, boolean models, boolean modelTextures, boolean mapGeometry, boolean itemIcons, boolean commonAssetsRebuild) {
         this.blockTextures = blockTextures;
         this.models = models;
         this.modelTextures = modelTextures;
         this.mapGeometry = mapGeometry;
         this.itemIcons = itemIcons;
         this.commonAssetsRebuild = commonAssetsRebuild;
      }

      public boolean isBlockTextures() {
         return this.blockTextures;
      }

      public boolean isModels() {
         return this.models;
      }

      public boolean isModelTextures() {
         return this.modelTextures;
      }

      public boolean isMapGeometry() {
         return this.mapGeometry;
      }

      public boolean isItemIcons() {
         return this.itemIcons;
      }

      public boolean isCommonAssetsRebuild() {
         return this.commonAssetsRebuild;
      }

      @Nonnull
      public AssetUpdateQuery.RebuildCacheBuilder toBuilder() {
         return new AssetUpdateQuery.RebuildCacheBuilder(
            this.blockTextures, this.models, this.modelTextures, this.mapGeometry, this.itemIcons, this.commonAssetsRebuild
         );
      }

      @Nonnull
      public static AssetUpdateQuery.RebuildCacheBuilder builder() {
         return new AssetUpdateQuery.RebuildCacheBuilder();
      }

      @Nonnull
      @Override
      public String toString() {
         return "RebuildCache{blockTextures="
            + this.blockTextures
            + ", models="
            + this.models
            + ", modelTextures="
            + this.modelTextures
            + ", mapGeometry="
            + this.mapGeometry
            + ", icons="
            + this.itemIcons
            + ", commonAssetsRebuild="
            + this.commonAssetsRebuild
            + "}";
      }
   }

   public static class RebuildCacheBuilder {
      private boolean blockTextures;
      private boolean models;
      private boolean modelTextures;
      private boolean mapGeometry;
      private boolean itemIcons;
      private boolean commonAssetsRebuild;

      RebuildCacheBuilder() {
      }

      RebuildCacheBuilder(boolean blockTextures, boolean models, boolean modelTextures, boolean mapGeometry, boolean itemIcons, boolean commonAssetsRebuild) {
         this.blockTextures = blockTextures;
         this.models = models;
         this.modelTextures = modelTextures;
         this.mapGeometry = mapGeometry;
         this.itemIcons = itemIcons;
         this.commonAssetsRebuild = commonAssetsRebuild;
      }

      public void setBlockTextures(boolean blockTextures) {
         this.blockTextures = blockTextures;
      }

      public void setModels(boolean models) {
         this.models = models;
      }

      public void setModelTextures(boolean modelTextures) {
         this.modelTextures = modelTextures;
      }

      public void setMapGeometry(boolean mapGeometry) {
         this.mapGeometry = mapGeometry;
      }

      public void setItemIcons(boolean itemIcons) {
         this.itemIcons = itemIcons;
      }

      public void setCommonAssetsRebuild(boolean commonAssetsRebuild) {
         this.commonAssetsRebuild = commonAssetsRebuild;
      }

      @Nonnull
      public AssetUpdateQuery.RebuildCache build() {
         return new AssetUpdateQuery.RebuildCache(
            this.blockTextures, this.models, this.modelTextures, this.mapGeometry, this.itemIcons, this.commonAssetsRebuild
         );
      }

      @Nonnull
      @Override
      public String toString() {
         return "RebuildCache{blockTextures="
            + this.blockTextures
            + ", models="
            + this.models
            + ", modelTextures="
            + this.modelTextures
            + ", mapGeometry="
            + this.mapGeometry
            + ", icons="
            + this.itemIcons
            + ", commonAssetsRebuild="
            + this.commonAssetsRebuild
            + "}";
      }
   }
}
