package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AssetEditorSelectAsset implements Packet, ToServerPacket {
   public static final int PACKET_ID = 336;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 32768020;
   @Nullable
   public AssetPath path;

   @Override
   public int getId() {
      return 336;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public AssetEditorSelectAsset() {
   }

   public AssetEditorSelectAsset(@Nullable AssetPath path) {
      this.path = path;
   }

   public AssetEditorSelectAsset(@Nonnull AssetEditorSelectAsset other) {
      this.path = other.path;
   }

   @Nonnull
   public static AssetEditorSelectAsset deserialize(@Nonnull ByteBuf buf, int offset) {
      AssetEditorSelectAsset obj = new AssetEditorSelectAsset();
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         obj.path = AssetPath.deserialize(buf, pos);
         pos += AssetPath.computeBytesConsumed(buf, pos);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         pos += AssetPath.computeBytesConsumed(buf, pos);
      }

      return pos - offset;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.path != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.path != null) {
         this.path.serialize(buf);
      }
   }

   @Override
   public int computeSize() {
      int size = 1;
      if (this.path != null) {
         size += this.path.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 1) {
         return ValidationResult.error("Buffer too small: expected at least 1 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 1;
         if ((nullBits & 1) != 0) {
            ValidationResult pathResult = AssetPath.validateStructure(buffer, pos);
            if (!pathResult.isValid()) {
               return ValidationResult.error("Invalid Path: " + pathResult.error());
            }

            pos += AssetPath.computeBytesConsumed(buffer, pos);
         }

         return ValidationResult.OK;
      }
   }

   public AssetEditorSelectAsset clone() {
      AssetEditorSelectAsset copy = new AssetEditorSelectAsset();
      copy.path = this.path != null ? this.path.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof AssetEditorSelectAsset other ? Objects.equals(this.path, other.path) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.path);
   }
}
