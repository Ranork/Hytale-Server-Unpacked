package com.hypixel.hytale.protocol.packets.setup;

import com.hypixel.hytale.protocol.Asset;
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

public class RequestAssets implements Packet, ToServerPacket {
   public static final int PACKET_ID = 23;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public Asset[] assets;

   @Override
   public int getId() {
      return 23;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public RequestAssets() {
   }

   public RequestAssets(@Nullable Asset[] assets) {
      this.assets = assets;
   }

   public RequestAssets(@Nonnull RequestAssets other) {
      this.assets = other.assets;
   }

   @Nonnull
   public static RequestAssets deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 1) {
         throw ProtocolException.bufferTooSmall("RequestAssets", 1, buf.readableBytes() - offset);
      } else {
         RequestAssets obj = new RequestAssets();
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

            if (pos + assetsVarLen + assetsCount * 64L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Assets", pos + assetsVarLen + assetsCount * 64, buf.readableBytes());
            }

            pos += assetsVarLen;
            obj.assets = new Asset[assetsCount];

            for (int i = 0; i < assetsCount; i++) {
               obj.assets[i] = Asset.deserialize(buf, pos);
               pos += Asset.computeBytesConsumed(buf, pos);
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
            pos += Asset.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 1L;
   }

   @Nullable
   public static Asset[] getAssets(MemorySegment mem) {
      return getAssets(mem, 0);
   }

   @Nullable
   public static Asset[] getAssets(MemorySegment mem, int offset) {
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

   public static boolean hasAssets(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static RequestAssets toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static RequestAssets toObject(MemorySegment mem, int offset) {
      if (offset + 1 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("RequestAssets", offset + 1, (int)mem.byteSize());
      } else {
         Asset[] assets = null;
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
            assets = new Asset[len];

            for (int i = 0; i < len; i++) {
               assets[i] = Asset.toObject(mem, off);
               off += assets[i].computeSize();
            }
         }

         return new RequestAssets(assets);
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

         for (Asset item : this.assets) {
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

         for (Asset elem : this.assets) {
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
               ValidationResult structResult = Asset.validateStructure(buffer, pos);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid Asset in Assets[" + i + "]: " + structResult.error());
               }

               pos += Asset.computeBytesConsumed(buffer, pos);
            }
         }

         return ValidationResult.OK;
      }
   }

   public RequestAssets clone() {
      RequestAssets copy = new RequestAssets();
      copy.assets = this.assets != null ? Arrays.stream(this.assets).map(e -> e.clone()).toArray(Asset[]::new) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof RequestAssets other ? Arrays.equals((Object[])this.assets, (Object[])other.assets) : false;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      return 31 * result + Arrays.hashCode((Object[])this.assets);
   }
}
