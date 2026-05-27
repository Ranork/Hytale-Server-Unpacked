package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AssetEditorExportDeleteAssets implements Packet, ToClientPacket {
   public static final int PACKET_ID = 346;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public AssetEditorAsset[] asset;

   @Override
   public int getId() {
      return 346;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public AssetEditorExportDeleteAssets() {
   }

   public AssetEditorExportDeleteAssets(@Nullable AssetEditorAsset[] asset) {
      this.asset = asset;
   }

   public AssetEditorExportDeleteAssets(@Nonnull AssetEditorExportDeleteAssets other) {
      this.asset = other.asset;
   }

   @Nonnull
   public static AssetEditorExportDeleteAssets deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 1) {
         throw ProtocolException.bufferTooSmall("AssetEditorExportDeleteAssets", 1, buf.readableBytes() - offset);
      } else {
         AssetEditorExportDeleteAssets obj = new AssetEditorExportDeleteAssets();
         byte nullBits = buf.getByte(offset);
         int pos = offset + 1;
         if ((nullBits & 1) != 0) {
            int assetCount = VarInt.peek(buf, pos);
            if (assetCount < 0) {
               throw ProtocolException.invalidVarInt("Asset");
            }

            int assetVarLen = VarInt.size(assetCount);
            if (assetCount > 4096000) {
               throw ProtocolException.arrayTooLong("Asset", assetCount, 4096000);
            }

            if (pos + assetVarLen + assetCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Asset", pos + assetVarLen + assetCount * 1, buf.readableBytes());
            }

            pos += assetVarLen;
            obj.asset = new AssetEditorAsset[assetCount];

            for (int i = 0; i < assetCount; i++) {
               obj.asset[i] = AssetEditorAsset.deserialize(buf, pos);
               pos += AssetEditorAsset.computeBytesConsumed(buf, pos);
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos += AssetEditorAsset.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 1L;
   }

   @Nullable
   public static AssetEditorAsset[] getAsset(MemorySegment mem) {
      return getAsset(mem, 0);
   }

   @Nullable
   public static AssetEditorAsset[] getAsset(MemorySegment mem, int offset) {
      if (!hasAsset(mem, offset)) {
         return null;
      } else {
         int off = offset + 1;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Asset", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Asset", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Asset", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               AssetEditorAsset[] data = new AssetEditorAsset[len];

               for (int i = 0; i < len; i++) {
                  data[i] = AssetEditorAsset.toObject(mem, off);
                  off += data[i].computeSize();
               }

               return data;
            }
         }
      }
   }

   public static boolean hasAsset(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static AssetEditorExportDeleteAssets toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static AssetEditorExportDeleteAssets toObject(MemorySegment mem, int offset) {
      if (offset + 1 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("AssetEditorExportDeleteAssets", offset + 1, (int)mem.byteSize());
      } else {
         AssetEditorAsset[] asset = null;
         if (hasAsset(mem, offset)) {
            int off = offset + 1;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Asset", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Asset", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Asset", off + lenOffset + len, (int)mem.byteSize());
            }

            off += lenOffset;
            asset = new AssetEditorAsset[len];

            for (int i = 0; i < len; i++) {
               asset[i] = AssetEditorAsset.toObject(mem, off);
               off += asset[i].computeSize();
            }
         }

         return new AssetEditorExportDeleteAssets(asset);
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.asset != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.asset != null) {
         if (this.asset.length > 4096000) {
            throw ProtocolException.arrayTooLong("Asset", this.asset.length, 4096000);
         }

         VarInt.write(buf, this.asset.length);

         for (AssetEditorAsset item : this.asset) {
            item.serialize(buf);
         }
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.asset != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 1;
      if (this.asset != null) {
         if (this.asset.length > 4096000) {
            throw ProtocolException.arrayTooLong("Asset", this.asset.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.asset.length);
         int assetValueOffset = 0;

         for (int i = 0; i < this.asset.length; i++) {
            assetValueOffset += this.asset[i].serialize(mem, varOffset + assetValueOffset);
         }

         varOffset += assetValueOffset;
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 1;
      if (this.asset != null) {
         int assetSize = 0;

         for (AssetEditorAsset elem : this.asset) {
            assetSize += elem.computeSize();
         }

         size += VarInt.size(this.asset.length) + assetSize;
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
            int assetCount = VarInt.peek(buffer, pos);
            if (assetCount < 0) {
               return ValidationResult.error("Invalid array count for Asset");
            }

            if (assetCount > 4096000) {
               return ValidationResult.error("Asset exceeds max length 4096000");
            }

            pos += VarInt.size(assetCount);

            for (int i = 0; i < assetCount; i++) {
               ValidationResult structResult = AssetEditorAsset.validateStructure(buffer, pos);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid AssetEditorAsset in Asset[" + i + "]: " + structResult.error());
               }

               pos += AssetEditorAsset.computeBytesConsumed(buffer, pos);
            }
         }

         return ValidationResult.OK;
      }
   }

   public AssetEditorExportDeleteAssets clone() {
      AssetEditorExportDeleteAssets copy = new AssetEditorExportDeleteAssets();
      copy.asset = this.asset != null ? Arrays.stream(this.asset).map(e -> e.clone()).toArray(AssetEditorAsset[]::new) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof AssetEditorExportDeleteAssets other ? Arrays.equals((Object[])this.asset, (Object[])other.asset) : false;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      return 31 * result + Arrays.hashCode((Object[])this.asset);
   }
}
