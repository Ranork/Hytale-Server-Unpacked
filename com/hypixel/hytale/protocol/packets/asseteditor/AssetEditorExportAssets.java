package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AssetEditorExportAssets implements Packet, ToServerPacket {
   public static final int PACKET_ID = 342;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public AssetPath[] paths;

   @Override
   public int getId() {
      return 342;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public AssetEditorExportAssets() {
   }

   public AssetEditorExportAssets(@Nullable AssetPath[] paths) {
      this.paths = paths;
   }

   public AssetEditorExportAssets(@Nonnull AssetEditorExportAssets other) {
      this.paths = other.paths;
   }

   @Nonnull
   public static AssetEditorExportAssets deserialize(@Nonnull ByteBuf buf, int offset) {
      AssetEditorExportAssets obj = new AssetEditorExportAssets();
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int pathsCount = VarInt.peek(buf, pos);
         if (pathsCount < 0) {
            throw ProtocolException.negativeLength("Paths", pathsCount);
         }

         if (pathsCount > 4096000) {
            throw ProtocolException.arrayTooLong("Paths", pathsCount, 4096000);
         }

         int pathsVarLen = VarInt.size(pathsCount);
         if (pos + pathsVarLen + pathsCount * 1L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Paths", pos + pathsVarLen + pathsCount * 1, buf.readableBytes());
         }

         pos += pathsVarLen;
         obj.paths = new AssetPath[pathsCount];

         for (int i = 0; i < pathsCount; i++) {
            obj.paths[i] = AssetPath.deserialize(buf, pos);
            pos += AssetPath.computeBytesConsumed(buf, pos);
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos);

         for (int i = 0; i < arrLen; i++) {
            pos += AssetPath.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.paths != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.paths != null) {
         if (this.paths.length > 4096000) {
            throw ProtocolException.arrayTooLong("Paths", this.paths.length, 4096000);
         }

         VarInt.write(buf, this.paths.length);

         for (AssetPath item : this.paths) {
            item.serialize(buf);
         }
      }
   }

   @Override
   public int computeSize() {
      int size = 1;
      if (this.paths != null) {
         int pathsSize = 0;

         for (AssetPath elem : this.paths) {
            pathsSize += elem.computeSize();
         }

         size += VarInt.size(this.paths.length) + pathsSize;
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
            int pathsCount = VarInt.peek(buffer, pos);
            if (pathsCount < 0) {
               return ValidationResult.error("Invalid array count for Paths");
            }

            if (pathsCount > 4096000) {
               return ValidationResult.error("Paths exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);

            for (int i = 0; i < pathsCount; i++) {
               ValidationResult structResult = AssetPath.validateStructure(buffer, pos);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid AssetPath in Paths[" + i + "]: " + structResult.error());
               }

               pos += AssetPath.computeBytesConsumed(buffer, pos);
            }
         }

         return ValidationResult.OK;
      }
   }

   public AssetEditorExportAssets clone() {
      AssetEditorExportAssets copy = new AssetEditorExportAssets();
      copy.paths = this.paths != null ? Arrays.stream(this.paths).map(e -> e.clone()).toArray(AssetPath[]::new) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof AssetEditorExportAssets other ? Arrays.equals((Object[])this.paths, (Object[])other.paths) : false;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      return 31 * result + Arrays.hashCode((Object[])this.paths);
   }
}
