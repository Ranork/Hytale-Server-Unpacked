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

public class AssetEditorLastModifiedAssets implements Packet, ToClientPacket {
   public static final int PACKET_ID = 339;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public AssetInfo[] assets;

   @Override
   public int getId() {
      return 339;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public AssetEditorLastModifiedAssets() {
   }

   public AssetEditorLastModifiedAssets(@Nullable AssetInfo[] assets) {
      this.assets = assets;
   }

   public AssetEditorLastModifiedAssets(@Nonnull AssetEditorLastModifiedAssets other) {
      this.assets = other.assets;
   }

   @Nonnull
   public static AssetEditorLastModifiedAssets deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 1) {
         throw ProtocolException.bufferTooSmall("AssetEditorLastModifiedAssets", 1, buf.readableBytes() - offset);
      } else {
         AssetEditorLastModifiedAssets obj = new AssetEditorLastModifiedAssets();
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

            if (pos + assetsVarLen + assetsCount * 11L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Assets", pos + assetsVarLen + assetsCount * 11, buf.readableBytes());
            }

            pos += assetsVarLen;
            obj.assets = new AssetInfo[assetsCount];

            for (int i = 0; i < assetsCount; i++) {
               obj.assets[i] = AssetInfo.deserialize(buf, pos);
               pos += AssetInfo.computeBytesConsumed(buf, pos);
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
            pos += AssetInfo.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 1L;
   }

   @Nullable
   public static AssetInfo[] getAssets(MemorySegment mem) {
      return getAssets(mem, 0);
   }

   @Nullable
   public static AssetInfo[] getAssets(MemorySegment mem, int offset) {
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
               AssetInfo[] data = new AssetInfo[len];

               for (int i = 0; i < len; i++) {
                  data[i] = AssetInfo.toObject(mem, off);
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

   public static AssetEditorLastModifiedAssets toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static AssetEditorLastModifiedAssets toObject(MemorySegment mem, int offset) {
      if (offset + 1 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("AssetEditorLastModifiedAssets", offset + 1, (int)mem.byteSize());
      } else {
         AssetInfo[] assets = null;
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
            assets = new AssetInfo[len];

            for (int i = 0; i < len; i++) {
               assets[i] = AssetInfo.toObject(mem, off);
               off += assets[i].computeSize();
            }
         }

         return new AssetEditorLastModifiedAssets(assets);
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

         for (AssetInfo item : this.assets) {
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

         for (AssetInfo elem : this.assets) {
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
               ValidationResult structResult = AssetInfo.validateStructure(buffer, pos);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid AssetInfo in Assets[" + i + "]: " + structResult.error());
               }

               pos += AssetInfo.computeBytesConsumed(buffer, pos);
            }
         }

         return ValidationResult.OK;
      }
   }

   public AssetEditorLastModifiedAssets clone() {
      AssetEditorLastModifiedAssets copy = new AssetEditorLastModifiedAssets();
      copy.assets = this.assets != null ? Arrays.stream(this.assets).map(e -> e.clone()).toArray(AssetInfo[]::new) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof AssetEditorLastModifiedAssets other ? Arrays.equals((Object[])this.assets, (Object[])other.assets) : false;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      return 31 * result + Arrays.hashCode((Object[])this.assets);
   }
}
