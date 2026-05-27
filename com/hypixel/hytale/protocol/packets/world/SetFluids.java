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

public class SetFluids implements Packet, ToClientPacket {
   public static final int PACKET_ID = 136;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 13;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 13;
   public static final int MAX_SIZE = 4096018;
   public int x;
   public int y;
   public int z;
   @Nullable
   public byte[] data;

   @Override
   public int getId() {
      return 136;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Chunks;
   }

   public SetFluids() {
   }

   public SetFluids(int x, int y, int z, @Nullable byte[] data) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.data = data;
   }

   public SetFluids(@Nonnull SetFluids other) {
      this.x = other.x;
      this.y = other.y;
      this.z = other.z;
      this.data = other.data;
   }

   @Nonnull
   public static SetFluids deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 13) {
         throw ProtocolException.bufferTooSmall("SetFluids", 13, buf.readableBytes() - offset);
      } else {
         SetFluids obj = new SetFluids();
         byte nullBits = buf.getByte(offset);
         obj.x = buf.getIntLE(offset + 1);
         obj.y = buf.getIntLE(offset + 5);
         obj.z = buf.getIntLE(offset + 9);
         int pos = offset + 13;
         if ((nullBits & 1) != 0) {
            int dataCount = VarInt.peek(buf, pos);
            if (dataCount < 0) {
               throw ProtocolException.invalidVarInt("Data");
            }

            int dataVarLen = VarInt.size(dataCount);
            if (dataCount > 4096000) {
               throw ProtocolException.arrayTooLong("Data", dataCount, 4096000);
            }

            if (pos + dataVarLen + dataCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Data", pos + dataVarLen + dataCount * 1, buf.readableBytes());
            }

            pos += dataVarLen;
            obj.data = new byte[dataCount];

            for (int i = 0; i < dataCount; i++) {
               obj.data[i] = buf.getByte(pos + i * 1);
            }

            pos += dataCount * 1;
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 13;
      if ((nullBits & 1) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.size(arrLen) + arrLen * 1;
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 13L;
   }

   public static int getX(MemorySegment mem) {
      return getX(mem, 0);
   }

   public static int getX(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   public static int getY(MemorySegment mem) {
      return getY(mem, 0);
   }

   public static int getY(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 5));
   }

   public static int getZ(MemorySegment mem) {
      return getZ(mem, 0);
   }

   public static int getZ(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 9));
   }

   @Nullable
   public static byte[] getData(MemorySegment mem) {
      return getData(mem, 0);
   }

   @Nullable
   public static byte[] getData(MemorySegment mem, int offset) {
      if (!hasData(mem, offset)) {
         return null;
      } else {
         int off = offset + 13;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Data", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Data", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Data", off + lenOffset + len * 1, (int)mem.byteSize());
            } else {
               off += lenOffset;
               byte[] data = new byte[len];
               MemorySegment.copy(mem, PacketIO.PROTO_BYTE, off, data, 0, len);
               return data;
            }
         }
      }
   }

   public static boolean hasData(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static SetFluids toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static SetFluids toObject(MemorySegment mem, int offset) {
      if (offset + 13 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("SetFluids", offset + 13, (int)mem.byteSize());
      } else {
         byte[] data = null;
         if (hasData(mem, offset)) {
            int off = offset + 13;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Data", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Data", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Data", off + lenOffset + len * 1, (int)mem.byteSize());
            }

            off += lenOffset;
            data = new byte[len];
            MemorySegment.copy(mem, PacketIO.PROTO_BYTE, off, data, 0, len);
         }

         return new SetFluids(
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 5)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 9)),
            data
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.data != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.x);
      buf.writeIntLE(this.y);
      buf.writeIntLE(this.z);
      if (this.data != null) {
         if (this.data.length > 4096000) {
            throw ProtocolException.arrayTooLong("Data", this.data.length, 4096000);
         }

         VarInt.write(buf, this.data.length);

         for (byte item : this.data) {
            buf.writeByte(item);
         }
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.data != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.x);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 5), this.y);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 9), this.z);
      int varOffset = offset + 13;
      if (this.data != null) {
         if (this.data.length > 4096000) {
            throw ProtocolException.arrayTooLong("Data", this.data.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.data.length);
         MemorySegment.copy(this.data, 0, mem, PacketIO.PROTO_BYTE, varOffset, this.data.length);
         varOffset += this.data.length * 1;
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 13;
      if (this.data != null) {
         size += VarInt.size(this.data.length) + this.data.length * 1;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 13) {
         return ValidationResult.error("Buffer too small: expected at least 13 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 13;
         if ((nullBits & 1) != 0) {
            int dataCount = VarInt.peek(buffer, pos);
            if (dataCount < 0) {
               return ValidationResult.error("Invalid array count for Data");
            }

            if (dataCount > 4096000) {
               return ValidationResult.error("Data exceeds max length 4096000");
            }

            pos += VarInt.size(dataCount);
            pos += dataCount * 1;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Data");
            }
         }

         return ValidationResult.OK;
      }
   }

   public SetFluids clone() {
      SetFluids copy = new SetFluids();
      copy.x = this.x;
      copy.y = this.y;
      copy.z = this.z;
      copy.data = this.data != null ? Arrays.copyOf(this.data, this.data.length) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof SetFluids other) ? false : this.x == other.x && this.y == other.y && this.z == other.z && Arrays.equals(this.data, other.data);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Integer.hashCode(this.x);
      result = 31 * result + Integer.hashCode(this.y);
      result = 31 * result + Integer.hashCode(this.z);
      return 31 * result + Arrays.hashCode(this.data);
   }
}
