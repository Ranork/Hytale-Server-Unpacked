package com.hypixel.hytale.builtin.asseteditor.data;

import com.hypixel.hytale.builtin.asseteditor.EditorClient;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.common.util.PathUtil;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetInfo;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetPath;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ModifiedAsset {
   public static final BuilderCodec<ModifiedAsset> CODEC = BuilderCodec.builder(ModifiedAsset.class, ModifiedAsset::new)
      .append(new KeyedCodec<>("File", Codec.PATH), (asset, s) -> asset.dataFile = s, asset -> asset.dataFile)
      .add()
      .append(new KeyedCodec<>("Path", Codec.PATH), (asset, s) -> asset.path = s, asset -> asset.path)
      .add()
      .append(new KeyedCodec<>("OldPath", Codec.PATH), (asset, s) -> asset.oldPath = s, asset -> asset.oldPath)
      .add()
      .append(new KeyedCodec<>("IsNew", Codec.BOOLEAN), (asset, s) -> asset.state = s ? AssetState.NEW : asset.state, asset -> null)
      .add()
      .append(new KeyedCodec<>("IsDeleted", Codec.BOOLEAN), (asset, s) -> asset.state = s ? AssetState.DELETED : asset.state, asset -> null)
      .add()
      .append(new KeyedCodec<>("State", new EnumCodec<>(AssetState.class)), (asset, s) -> asset.state = s, asset -> asset.state)
      .add()
      .append(
         new KeyedCodec<>("LastModificationTimestamp", Codec.INSTANT, true),
         (asset, s) -> asset.lastModificationTimestamp = s,
         asset -> asset.lastModificationTimestamp
      )
      .add()
      .append(
         new KeyedCodec<>("LastModificationPlayerUuid", Codec.UUID_STRING, true),
         (asset, s) -> asset.lastModificationPlayerUuid = s,
         asset -> asset.lastModificationPlayerUuid
      )
      .add()
      .append(
         new KeyedCodec<>("LastModificationUsername", Codec.STRING, true),
         (asset, s) -> asset.lastModificationUsername = s,
         asset -> asset.lastModificationUsername
      )
      .add()
      .build();
   @Nullable
   public Path dataFile;
   public Path path;
   @Nullable
   public Path oldPath;
   public AssetState state = AssetState.CHANGED;
   public Instant lastModificationTimestamp;
   public UUID lastModificationPlayerUuid;
   public String lastModificationUsername;

   public void markEditedBy(@Nonnull EditorClient editorClient) {
      this.lastModificationTimestamp = Instant.now();
      this.lastModificationUsername = editorClient.getUsername();
      this.lastModificationPlayerUuid = editorClient.getUuid();
   }

   @Nonnull
   public AssetInfo toAssetInfoPacket(String assetPack) {
      return new AssetInfo(
         new AssetPath(assetPack, PathUtil.toUnixPathString(this.path)),
         this.oldPath != null ? new AssetPath(assetPack, PathUtil.toUnixPathString(this.oldPath)) : null,
         this.state == AssetState.DELETED,
         this.state == AssetState.NEW,
         this.lastModificationTimestamp.toEpochMilli(),
         this.lastModificationUsername
      );
   }
}
