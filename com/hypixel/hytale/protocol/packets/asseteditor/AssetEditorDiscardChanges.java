package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AssetEditorDiscardChanges implements Packet, ToServerPacket {
   public static final int PACKET_ID = 330;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public TimestampedAssetReference[] assets;

   @Override
   public int getId() {
      return 330;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public AssetEditorDiscardChanges() {
   }

   public AssetEditorDiscardChanges(@Nullable TimestampedAssetReference[] assets) {
      this.assets = assets;
   }

   public AssetEditorDiscardChanges(@Nonnull AssetEditorDiscardChanges other) {
      this.assets = other.assets;
   }

   @Nonnull
   public static AssetEditorDiscardChanges deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 1) {
         throw ProtocolException.bufferTooSmall("AssetEditorDiscardChanges", 1, buf.readableBytes() - offset);
      } else {
         AssetEditorDiscardChanges obj = new AssetEditorDiscardChanges();
         byte nullBits = buf.getByte(offset);
         int pos = offset + 1;
         if ((nullBits & 1) != 0) {
            int assetsCount = VarInt.peek(buf, pos);
            if (assetsCount < 0) {
               throw ProtocolException.invalidVarInt("Assets");
            }

            int assetsVarLen = VarInt.size(assetsCount);
            if (assetsCount > 4096000) {
               throw ProtocolException.arrayTooLong("Assets", assetsCount, 4096000);
            }

            if (pos + assetsVarLen + assetsCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Assets", pos + assetsVarLen + assetsCount * 1, buf.readableBytes());
            }

            pos += assetsVarLen;
            obj.assets = new TimestampedAssetReference[assetsCount];

            for (int i = 0; i < assetsCount; i++) {
               obj.assets[i] = TimestampedAssetReference.deserialize(buf, pos);
               pos += TimestampedAssetReference.computeBytesConsumed(buf, pos);
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
            pos += TimestampedAssetReference.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 1L;
   }

   @Nullable
   public static TimestampedAssetReference[] getAssets(MemorySegment mem) {
      return getAssets(mem, 0);
   }

   @Nullable
   public static TimestampedAssetReference[] getAssets(MemorySegment mem, int offset) {
      if (!hasAssets(mem, offset)) {
         return null;
      } else {
         int off = offset + 1;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Assets", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Assets", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Assets", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               TimestampedAssetReference[] data = new TimestampedAssetReference[len];

               for (int i = 0; i < len; i++) {
                  data[i] = TimestampedAssetReference.toObject(mem, off);
                  off += data[i].computeSize();
               }

               return data;
            }
         }
      }
   }

   public static boolean hasAssets(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static AssetEditorDiscardChanges toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static AssetEditorDiscardChanges toObject(MemorySegment mem, int offset) {
      if (offset + 1 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("AssetEditorDiscardChanges", offset + 1, (int)mem.byteSize());
      } else {
         TimestampedAssetReference[] assets = null;
         if (hasAssets(mem, offset)) {
            int off = offset + 1;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Assets", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Assets", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Assets", off + lenOffset + len, (int)mem.byteSize());
            }

            off += lenOffset;
            assets = new TimestampedAssetReference[len];

            for (int i = 0; i < len; i++) {
               assets[i] = TimestampedAssetReference.toObject(mem, off);
               off += assets[i].computeSize();
            }
         }

         return new AssetEditorDiscardChanges(assets);
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.assets != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.assets != null) {
         if (this.assets.length > 4096000) {
            throw ProtocolException.arrayTooLong("Assets", this.assets.length, 4096000);
         }

         VarInt.write(buf, this.assets.length);

         for (TimestampedAssetReference item : this.assets) {
            item.serialize(buf);
         }
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.assets != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 1;
      if (this.assets != null) {
         if (this.assets.length > 4096000) {
            throw ProtocolException.arrayTooLong("Assets", this.assets.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.assets.length);
         int assetsValueOffset = 0;

         for (int i = 0; i < this.assets.length; i++) {
            assetsValueOffset += this.assets[i].serialize(mem, varOffset + assetsValueOffset);
         }

         varOffset += assetsValueOffset;
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 1;
      if (this.assets != null) {
         int assetsSize = 0;

         for (TimestampedAssetReference elem : this.assets) {
            assetsSize += elem.computeSize();
         }

         size += VarInt.size(this.assets.length) + assetsSize;
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
            int assetsCount = VarInt.peek(buffer, pos);
            if (assetsCount < 0) {
               return ValidationResult.error("Invalid array count for Assets");
            }

            if (assetsCount > 4096000) {
               return ValidationResult.error("Assets exceeds max length 4096000");
            }

            pos += VarInt.size(assetsCount);

            for (int i = 0; i < assetsCount; i++) {
               ValidationResult structResult = TimestampedAssetReference.validateStructure(buffer, pos);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid TimestampedAssetReference in Assets[" + i + "]: " + structResult.error());
               }

               pos += TimestampedAssetReference.computeBytesConsumed(buffer, pos);
            }
         }

         return ValidationResult.OK;
      }
   }

   public AssetEditorDiscardChanges clone() {
      AssetEditorDiscardChanges copy = new AssetEditorDiscardChanges();
      copy.assets = this.assets != null ? Arrays.stream(this.assets).map(e -> e.clone()).toArray(TimestampedAssetReference[]::new) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof AssetEditorDiscardChanges other ? Arrays.equals((Object[])this.assets, (Object[])other.assets) : false;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      return 31 * result + Arrays.hashCode((Object[])this.assets);
   }
}
