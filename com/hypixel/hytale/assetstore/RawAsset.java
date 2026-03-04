package com.hypixel.hytale.assetstore;

import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.util.RawJsonReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNullableByDefault;

@ParametersAreNullableByDefault
public class RawAsset<K> implements AssetHolder<K> {
   private final Path parentPath;
   @Nullable
   private final K key;
   private final int lineOffset;
   private final boolean parentKeyResolved;
   @Nullable
   private final K parentKey;
   @Nullable
   private final Path path;
   @Nullable
   private final char[] buffer;
   @Nullable
   private final AssetExtraInfo.Data containerData;
   @Nonnull
   private final ContainedAssetCodec.Mode containedAssetMode;

   public RawAsset(K key, Path path) {
      this.key = key;
      this.lineOffset = 0;
      this.parentKeyResolved = false;
      this.parentKey = null;
      this.path = path;
      this.parentPath = null;
      this.buffer = null;
      this.containerData = null;
      this.containedAssetMode = ContainedAssetCodec.Mode.NONE;
   }

   public RawAsset(
      Path parentPath,
      K key,
      K parentKey,
      int lineOffset,
      char[] buffer,
      AssetExtraInfo.Data containerData,
      @Nonnull ContainedAssetCodec.Mode containedAssetMode
   ) {
      this.key = key;
      this.lineOffset = lineOffset;
      this.parentKeyResolved = true;
      this.parentKey = parentKey;
      this.path = null;
      this.parentPath = parentPath;
      this.buffer = buffer;
      this.containerData = containerData;
      this.containedAssetMode = containedAssetMode;
   }

   private RawAsset(
      K key,
      boolean parentKeyResolved,
      K parentKey,
      Path path,
      char[] buffer,
      AssetExtraInfo.Data containerData,
      @Nonnull ContainedAssetCodec.Mode containedAssetMode
   ) {
      this.key = key;
      this.lineOffset = 0;
      this.parentKeyResolved = parentKeyResolved;
      this.parentKey = parentKey;
      this.path = path;
      this.parentPath = null;
      this.buffer = buffer;
      this.containerData = containerData;
      this.containedAssetMode = containedAssetMode;
   }

   @Nullable
   public K getKey() {
      return this.key;
   }

   public boolean isParentKeyResolved() {
      return this.parentKeyResolved;
   }

   @Nullable
   public K getParentKey() {
      return this.parentKey;
   }

   @Nullable
   public Path getPath() {
      return this.path;
   }

   public Path getParentPath() {
      return this.parentPath;
   }

   public int getLineOffset() {
      return this.lineOffset;
   }

   public char[] getBuffer() {
      return this.buffer;
   }

   @Nonnull
   public ContainedAssetCodec.Mode getContainedAssetMode() {
      return this.containedAssetMode;
   }

   @Nonnull
   public RawJsonReader toRawJsonReader(@Nonnull Supplier<char[]> bufferSupplier) throws IOException {
      return this.path != null ? RawJsonReader.fromPath(this.path, bufferSupplier.get()) : RawJsonReader.fromBuffer(this.buffer);
   }

   @Nonnull
   public AssetExtraInfo.Data makeData(Class<? extends JsonAssetWithMap<K, ?>> aClass, K key, K parentKey) {
      boolean inheritTags = switch (this.containedAssetMode) {
         case INHERIT_ID, INHERIT_ID_AND_PARENT, INJECT_PARENT -> true;
         case NONE, GENERATE_ID -> false;
      };
      return new AssetExtraInfo.Data(this.containerData, aClass, key, parentKey, inheritTags);
   }

   @Nonnull
   public RawAsset<K> withResolveKeys(K key, K parentKey) {
      return new RawAsset<>(key, true, parentKey, this.path, this.buffer, this.containerData, this.containedAssetMode);
   }

   @Nonnull
   @Override
   public String toString() {
      return "RawAsset{key="
         + this.key
         + ", parentKeyResolved="
         + this.parentKeyResolved
         + ", parentKey="
         + this.parentKey
         + ", path="
         + this.path
         + ", buffer.length="
         + (this.buffer != null ? this.buffer.length : -1)
         + "}";
   }
}
