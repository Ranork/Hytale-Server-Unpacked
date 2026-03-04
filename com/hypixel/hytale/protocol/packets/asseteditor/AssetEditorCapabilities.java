package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class AssetEditorCapabilities implements Packet, ToClientPacket {
   public static final int PACKET_ID = 304;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 5;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 5;
   public static final int MAX_SIZE = 5;
   public boolean canDiscardAssets;
   public boolean canEditAssets;
   public boolean canCreateAssetPacks;
   public boolean canEditAssetPacks;
   public boolean canDeleteAssetPacks;

   @Override
   public int getId() {
      return 304;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public AssetEditorCapabilities() {
   }

   public AssetEditorCapabilities(
      boolean canDiscardAssets, boolean canEditAssets, boolean canCreateAssetPacks, boolean canEditAssetPacks, boolean canDeleteAssetPacks
   ) {
      this.canDiscardAssets = canDiscardAssets;
      this.canEditAssets = canEditAssets;
      this.canCreateAssetPacks = canCreateAssetPacks;
      this.canEditAssetPacks = canEditAssetPacks;
      this.canDeleteAssetPacks = canDeleteAssetPacks;
   }

   public AssetEditorCapabilities(@Nonnull AssetEditorCapabilities other) {
      this.canDiscardAssets = other.canDiscardAssets;
      this.canEditAssets = other.canEditAssets;
      this.canCreateAssetPacks = other.canCreateAssetPacks;
      this.canEditAssetPacks = other.canEditAssetPacks;
      this.canDeleteAssetPacks = other.canDeleteAssetPacks;
   }

   @Nonnull
   public static AssetEditorCapabilities deserialize(@Nonnull ByteBuf buf, int offset) {
      AssetEditorCapabilities obj = new AssetEditorCapabilities();
      obj.canDiscardAssets = buf.getByte(offset + 0) != 0;
      obj.canEditAssets = buf.getByte(offset + 1) != 0;
      obj.canCreateAssetPacks = buf.getByte(offset + 2) != 0;
      obj.canEditAssetPacks = buf.getByte(offset + 3) != 0;
      obj.canDeleteAssetPacks = buf.getByte(offset + 4) != 0;
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 5;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.canDiscardAssets ? 1 : 0);
      buf.writeByte(this.canEditAssets ? 1 : 0);
      buf.writeByte(this.canCreateAssetPacks ? 1 : 0);
      buf.writeByte(this.canEditAssetPacks ? 1 : 0);
      buf.writeByte(this.canDeleteAssetPacks ? 1 : 0);
   }

   @Override
   public int computeSize() {
      return 5;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 5 ? ValidationResult.error("Buffer too small: expected at least 5 bytes") : ValidationResult.OK;
   }

   public AssetEditorCapabilities clone() {
      AssetEditorCapabilities copy = new AssetEditorCapabilities();
      copy.canDiscardAssets = this.canDiscardAssets;
      copy.canEditAssets = this.canEditAssets;
      copy.canCreateAssetPacks = this.canCreateAssetPacks;
      copy.canEditAssetPacks = this.canEditAssetPacks;
      copy.canDeleteAssetPacks = this.canDeleteAssetPacks;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AssetEditorCapabilities other)
            ? false
            : this.canDiscardAssets == other.canDiscardAssets
               && this.canEditAssets == other.canEditAssets
               && this.canCreateAssetPacks == other.canCreateAssetPacks
               && this.canEditAssetPacks == other.canEditAssetPacks
               && this.canDeleteAssetPacks == other.canDeleteAssetPacks;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.canDiscardAssets, this.canEditAssets, this.canCreateAssetPacks, this.canEditAssetPacks, this.canDeleteAssetPacks);
   }
}
