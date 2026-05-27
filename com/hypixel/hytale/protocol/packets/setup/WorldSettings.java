package com.hypixel.hytale.protocol.packets.setup;

import com.hypixel.hytale.protocol.Asset;
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

public class WorldSettings implements Packet, ToClientPacket {
   public static final int PACKET_ID = 20;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 5;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 5;
   public static final int MAX_SIZE = 1677721600;
   public int worldHeight;
   @Nullable
   public Asset[] requiredAssets;

   @Override
   public int getId() {
      return 20;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public WorldSettings() {
   }

   public WorldSettings(int worldHeight, @Nullable Asset[] requiredAssets) {
      this.worldHeight = worldHeight;
      this.requiredAssets = requiredAssets;
   }

   public WorldSettings(@Nonnull WorldSettings other) {
      this.worldHeight = other.worldHeight;
      this.requiredAssets = other.requiredAssets;
   }

   @Nonnull
   public static WorldSettings deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 5) {
         throw ProtocolException.bufferTooSmall("WorldSettings", 5, buf.readableBytes() - offset);
      } else {
         WorldSettings obj = new WorldSettings();
         byte nullBits = buf.getByte(offset);
         obj.worldHeight = buf.getIntLE(offset + 1);
         int pos = offset + 5;
         if ((nullBits & 1) != 0) {
            int requiredAssetsCount = VarInt.peek(buf, pos);
            if (requiredAssetsCount < 0) {
               throw ProtocolException.invalidVarInt("RequiredAssets");
            }

            int requiredAssetsVarLen = VarInt.size(requiredAssetsCount);
            if (requiredAssetsCount > 4096000) {
               throw ProtocolException.arrayTooLong("RequiredAssets", requiredAssetsCount, 4096000);
            }

            if (pos + requiredAssetsVarLen + requiredAssetsCount * 64L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("RequiredAssets", pos + requiredAssetsVarLen + requiredAssetsCount * 64, buf.readableBytes());
            }

            pos += requiredAssetsVarLen;
            obj.requiredAssets = new Asset[requiredAssetsCount];

            for (int i = 0; i < requiredAssetsCount; i++) {
               obj.requiredAssets[i] = Asset.deserialize(buf, pos);
               pos += Asset.computeBytesConsumed(buf, pos);
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 5;
      if ((nullBits & 1) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos += Asset.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 5L;
   }

   public static int getWorldHeight(MemorySegment mem) {
      return getWorldHeight(mem, 0);
   }

   public static int getWorldHeight(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   @Nullable
   public static Asset[] getRequiredAssets(MemorySegment mem) {
      return getRequiredAssets(mem, 0);
   }

   @Nullable
   public static Asset[] getRequiredAssets(MemorySegment mem, int offset) {
      if (!hasRequiredAssets(mem, offset)) {
         return null;
      } else {
         int off = offset + 5;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("RequiredAssets", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("RequiredAssets", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("RequiredAssets", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               Asset[] data = new Asset[len];

               for (int i = 0; i < len; i++) {
                  data[i] = Asset.toObject(mem, off);
                  off += data[i].computeSize();
               }

               return data;
            }
         }
      }
   }

   public static boolean hasRequiredAssets(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static WorldSettings toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static WorldSettings toObject(MemorySegment mem, int offset) {
      if (offset + 5 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("WorldSettings", offset + 5, (int)mem.byteSize());
      } else {
         Asset[] requiredAssets = null;
         if (hasRequiredAssets(mem, offset)) {
            int off = offset + 5;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("RequiredAssets", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("RequiredAssets", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("RequiredAssets", off + lenOffset + len, (int)mem.byteSize());
            }

            off += lenOffset;
            requiredAssets = new Asset[len];

            for (int i = 0; i < len; i++) {
               requiredAssets[i] = Asset.toObject(mem, off);
               off += requiredAssets[i].computeSize();
            }
         }

         return new WorldSettings(mem.get(PacketIO.PROTO_INT, (long)(offset + 1)), requiredAssets);
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.requiredAssets != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.worldHeight);
      if (this.requiredAssets != null) {
         if (this.requiredAssets.length > 4096000) {
            throw ProtocolException.arrayTooLong("RequiredAssets", this.requiredAssets.length, 4096000);
         }

         VarInt.write(buf, this.requiredAssets.length);

         for (Asset item : this.requiredAssets) {
            item.serialize(buf);
         }
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.requiredAssets != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.worldHeight);
      int varOffset = offset + 5;
      if (this.requiredAssets != null) {
         if (this.requiredAssets.length > 4096000) {
            throw ProtocolException.arrayTooLong("RequiredAssets", this.requiredAssets.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.requiredAssets.length);
         int requiredAssetsValueOffset = 0;

         for (int i = 0; i < this.requiredAssets.length; i++) {
            requiredAssetsValueOffset += this.requiredAssets[i].serialize(mem, varOffset + requiredAssetsValueOffset);
         }

         varOffset += requiredAssetsValueOffset;
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 5;
      if (this.requiredAssets != null) {
         int requiredAssetsSize = 0;

         for (Asset elem : this.requiredAssets) {
            requiredAssetsSize += elem.computeSize();
         }

         size += VarInt.size(this.requiredAssets.length) + requiredAssetsSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 5) {
         return ValidationResult.error("Buffer too small: expected at least 5 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 5;
         if ((nullBits & 1) != 0) {
            int requiredAssetsCount = VarInt.peek(buffer, pos);
            if (requiredAssetsCount < 0) {
               return ValidationResult.error("Invalid array count for RequiredAssets");
            }

            if (requiredAssetsCount > 4096000) {
               return ValidationResult.error("RequiredAssets exceeds max length 4096000");
            }

            pos += VarInt.size(requiredAssetsCount);

            for (int i = 0; i < requiredAssetsCount; i++) {
               ValidationResult structResult = Asset.validateStructure(buffer, pos);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid Asset in RequiredAssets[" + i + "]: " + structResult.error());
               }

               pos += Asset.computeBytesConsumed(buffer, pos);
            }
         }

         return ValidationResult.OK;
      }
   }

   public WorldSettings clone() {
      WorldSettings copy = new WorldSettings();
      copy.worldHeight = this.worldHeight;
      copy.requiredAssets = this.requiredAssets != null ? Arrays.stream(this.requiredAssets).map(e -> e.clone()).toArray(Asset[]::new) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof WorldSettings other)
            ? false
            : this.worldHeight == other.worldHeight && Arrays.equals((Object[])this.requiredAssets, (Object[])other.requiredAssets);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Integer.hashCode(this.worldHeight);
      return 31 * result + Arrays.hashCode((Object[])this.requiredAssets);
   }
}
