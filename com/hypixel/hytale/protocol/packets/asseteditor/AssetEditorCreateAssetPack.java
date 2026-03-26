package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AssetEditorCreateAssetPack implements Packet, ToServerPacket {
   public static final int PACKET_ID = 316;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 9;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 1677721600;
   public int token;
   @Nullable
   public AssetPackManifest manifest;
   public int targetDirectoryIndex;

   @Override
   public int getId() {
      return 316;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public AssetEditorCreateAssetPack() {
   }

   public AssetEditorCreateAssetPack(int token, @Nullable AssetPackManifest manifest, int targetDirectoryIndex) {
      this.token = token;
      this.manifest = manifest;
      this.targetDirectoryIndex = targetDirectoryIndex;
   }

   public AssetEditorCreateAssetPack(@Nonnull AssetEditorCreateAssetPack other) {
      this.token = other.token;
      this.manifest = other.manifest;
      this.targetDirectoryIndex = other.targetDirectoryIndex;
   }

   @Nonnull
   public static AssetEditorCreateAssetPack deserialize(@Nonnull ByteBuf buf, int offset) {
      AssetEditorCreateAssetPack obj = new AssetEditorCreateAssetPack();
      byte nullBits = buf.getByte(offset);
      obj.token = buf.getIntLE(offset + 1);
      obj.targetDirectoryIndex = buf.getIntLE(offset + 5);
      int pos = offset + 9;
      if ((nullBits & 1) != 0) {
         obj.manifest = AssetPackManifest.deserialize(buf, pos);
         pos += AssetPackManifest.computeBytesConsumed(buf, pos);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 9;
      if ((nullBits & 1) != 0) {
         pos += AssetPackManifest.computeBytesConsumed(buf, pos);
      }

      return pos - offset;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.manifest != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.token);
      buf.writeIntLE(this.targetDirectoryIndex);
      if (this.manifest != null) {
         this.manifest.serialize(buf);
      }
   }

   @Override
   public int computeSize() {
      int size = 9;
      if (this.manifest != null) {
         size += this.manifest.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 9) {
         return ValidationResult.error("Buffer too small: expected at least 9 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 9;
         if ((nullBits & 1) != 0) {
            ValidationResult manifestResult = AssetPackManifest.validateStructure(buffer, pos);
            if (!manifestResult.isValid()) {
               return ValidationResult.error("Invalid Manifest: " + manifestResult.error());
            }

            pos += AssetPackManifest.computeBytesConsumed(buffer, pos);
         }

         return ValidationResult.OK;
      }
   }

   public AssetEditorCreateAssetPack clone() {
      AssetEditorCreateAssetPack copy = new AssetEditorCreateAssetPack();
      copy.token = this.token;
      copy.manifest = this.manifest != null ? this.manifest.clone() : null;
      copy.targetDirectoryIndex = this.targetDirectoryIndex;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AssetEditorCreateAssetPack other)
            ? false
            : this.token == other.token && Objects.equals(this.manifest, other.manifest) && this.targetDirectoryIndex == other.targetDirectoryIndex;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.token, this.manifest, this.targetDirectoryIndex);
   }
}
