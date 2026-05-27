package com.hypixel.hytale.protocol.packets.world;

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

public class SetChunkHeightmap implements Packet, ToClientPacket {
   public static final int PACKET_ID = 132;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 9;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 4096014;
   public int x;
   public int z;
   @Nullable
   public byte[] heightmap;

   @Override
   public int getId() {
      return 132;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Chunks;
   }

   public SetChunkHeightmap() {
   }

   public SetChunkHeightmap(int x, int z, @Nullable byte[] heightmap) {
      this.x = x;
      this.z = z;
      this.heightmap = heightmap;
   }

   public SetChunkHeightmap(@Nonnull SetChunkHeightmap other) {
      this.x = other.x;
      this.z = other.z;
      this.heightmap = other.heightmap;
   }

   @Nonnull
   public static SetChunkHeightmap deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 9) {
         throw ProtocolException.bufferTooSmall("SetChunkHeightmap", 9, buf.readableBytes() - offset);
      } else {
         SetChunkHeightmap obj = new SetChunkHeightmap();
         byte nullBits = buf.getByte(offset);
         obj.x = buf.getIntLE(offset + 1);
         obj.z = buf.getIntLE(offset + 5);
         int pos = offset + 9;
         if ((nullBits & 1) != 0) {
            int heightmapCount = VarInt.peek(buf, pos);
            if (heightmapCount < 0) {
               throw ProtocolException.invalidVarInt("Heightmap");
            }

            int heightmapVarLen = VarInt.size(heightmapCount);
            if (heightmapCount > 4096000) {
               throw ProtocolException.arrayTooLong("Heightmap", heightmapCount, 4096000);
            }

            if (pos + heightmapVarLen + heightmapCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Heightmap", pos + heightmapVarLen + heightmapCount * 1, buf.readableBytes());
            }

            pos += heightmapVarLen;
            obj.heightmap = new byte[heightmapCount];

            for (int i = 0; i < heightmapCount; i++) {
               obj.heightmap[i] = buf.getByte(pos + i * 1);
            }

            pos += heightmapCount * 1;
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 9;
      if ((nullBits & 1) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.size(arrLen) + arrLen * 1;
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 9L;
   }

   public static int getX(MemorySegment mem) {
      return getX(mem, 0);
   }

   public static int getX(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   public static int getZ(MemorySegment mem) {
      return getZ(mem, 0);
   }

   public static int getZ(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 5));
   }

   @Nullable
   public static byte[] getHeightmap(MemorySegment mem) {
      return getHeightmap(mem, 0);
   }

   @Nullable
   public static byte[] getHeightmap(MemorySegment mem, int offset) {
      if (!hasHeightmap(mem, offset)) {
         return null;
      } else {
         int off = offset + 9;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Heightmap", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Heightmap", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Heightmap", off + lenOffset + len * 1, (int)mem.byteSize());
            } else {
               off += lenOffset;
               byte[] data = new byte[len];
               MemorySegment.copy(mem, PacketIO.PROTO_BYTE, off, data, 0, len);
               return data;
            }
         }
      }
   }

   public static boolean hasHeightmap(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static SetChunkHeightmap toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static SetChunkHeightmap toObject(MemorySegment mem, int offset) {
      if (offset + 9 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("SetChunkHeightmap", offset + 9, (int)mem.byteSize());
      } else {
         byte[] heightmap = null;
         if (hasHeightmap(mem, offset)) {
            int off = offset + 9;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Heightmap", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Heightmap", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Heightmap", off + lenOffset + len * 1, (int)mem.byteSize());
            }

            off += lenOffset;
            heightmap = new byte[len];
            MemorySegment.copy(mem, PacketIO.PROTO_BYTE, off, heightmap, 0, len);
         }

         return new SetChunkHeightmap(mem.get(PacketIO.PROTO_INT, (long)(offset + 1)), mem.get(PacketIO.PROTO_INT, (long)(offset + 5)), heightmap);
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.heightmap != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.x);
      buf.writeIntLE(this.z);
      if (this.heightmap != null) {
         if (this.heightmap.length > 4096000) {
            throw ProtocolException.arrayTooLong("Heightmap", this.heightmap.length, 4096000);
         }

         VarInt.write(buf, this.heightmap.length);

         for (byte item : this.heightmap) {
            buf.writeByte(item);
         }
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.heightmap != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.x);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 5), this.z);
      int varOffset = offset + 9;
      if (this.heightmap != null) {
         if (this.heightmap.length > 4096000) {
            throw ProtocolException.arrayTooLong("Heightmap", this.heightmap.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.heightmap.length);
         MemorySegment.copy(this.heightmap, 0, mem, PacketIO.PROTO_BYTE, varOffset, this.heightmap.length);
         varOffset += this.heightmap.length * 1;
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 9;
      if (this.heightmap != null) {
         size += VarInt.size(this.heightmap.length) + this.heightmap.length * 1;
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
            int heightmapCount = VarInt.peek(buffer, pos);
            if (heightmapCount < 0) {
               return ValidationResult.error("Invalid array count for Heightmap");
            }

            if (heightmapCount > 4096000) {
               return ValidationResult.error("Heightmap exceeds max length 4096000");
            }

            pos += VarInt.size(heightmapCount);
            pos += heightmapCount * 1;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Heightmap");
            }
         }

         return ValidationResult.OK;
      }
   }

   public SetChunkHeightmap clone() {
      SetChunkHeightmap copy = new SetChunkHeightmap();
      copy.x = this.x;
      copy.z = this.z;
      copy.heightmap = this.heightmap != null ? Arrays.copyOf(this.heightmap, this.heightmap.length) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof SetChunkHeightmap other) ? false : this.x == other.x && this.z == other.z && Arrays.equals(this.heightmap, other.heightmap);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Integer.hashCode(this.x);
      result = 31 * result + Integer.hashCode(this.z);
      return 31 * result + Arrays.hashCode(this.heightmap);
   }
}
