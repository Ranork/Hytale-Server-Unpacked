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

public class SetChunkTintmap implements Packet, ToClientPacket {
   public static final int PACKET_ID = 133;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 9;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 4096014;
   public int x;
   public int z;
   @Nullable
   public byte[] tintmap;

   @Override
   public int getId() {
      return 133;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Chunks;
   }

   public SetChunkTintmap() {
   }

   public SetChunkTintmap(int x, int z, @Nullable byte[] tintmap) {
      this.x = x;
      this.z = z;
      this.tintmap = tintmap;
   }

   public SetChunkTintmap(@Nonnull SetChunkTintmap other) {
      this.x = other.x;
      this.z = other.z;
      this.tintmap = other.tintmap;
   }

   @Nonnull
   public static SetChunkTintmap deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 9) {
         throw ProtocolException.bufferTooSmall("SetChunkTintmap", 9, buf.readableBytes() - offset);
      } else {
         SetChunkTintmap obj = new SetChunkTintmap();
         byte nullBits = buf.getByte(offset);
         obj.x = buf.getIntLE(offset + 1);
         obj.z = buf.getIntLE(offset + 5);
         int pos = offset + 9;
         if ((nullBits & 1) != 0) {
            int tintmapCount = VarInt.peek(buf, pos);
            if (tintmapCount < 0) {
               throw ProtocolException.invalidVarInt("Tintmap");
            }

            int tintmapVarLen = VarInt.size(tintmapCount);
            if (tintmapCount > 4096000) {
               throw ProtocolException.arrayTooLong("Tintmap", tintmapCount, 4096000);
            }

            if (pos + tintmapVarLen + tintmapCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Tintmap", pos + tintmapVarLen + tintmapCount * 1, buf.readableBytes());
            }

            pos += tintmapVarLen;
            obj.tintmap = new byte[tintmapCount];

            for (int i = 0; i < tintmapCount; i++) {
               obj.tintmap[i] = buf.getByte(pos + i * 1);
            }

            pos += tintmapCount * 1;
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
   public static byte[] getTintmap(MemorySegment mem) {
      return getTintmap(mem, 0);
   }

   @Nullable
   public static byte[] getTintmap(MemorySegment mem, int offset) {
      if (!hasTintmap(mem, offset)) {
         return null;
      } else {
         int off = offset + 9;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Tintmap", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Tintmap", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Tintmap", off + lenOffset + len * 1, (int)mem.byteSize());
            } else {
               off += lenOffset;
               byte[] data = new byte[len];
               MemorySegment.copy(mem, PacketIO.PROTO_BYTE, off, data, 0, len);
               return data;
            }
         }
      }
   }

   public static boolean hasTintmap(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static SetChunkTintmap toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static SetChunkTintmap toObject(MemorySegment mem, int offset) {
      if (offset + 9 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("SetChunkTintmap", offset + 9, (int)mem.byteSize());
      } else {
         byte[] tintmap = null;
         if (hasTintmap(mem, offset)) {
            int off = offset + 9;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Tintmap", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Tintmap", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Tintmap", off + lenOffset + len * 1, (int)mem.byteSize());
            }

            off += lenOffset;
            tintmap = new byte[len];
            MemorySegment.copy(mem, PacketIO.PROTO_BYTE, off, tintmap, 0, len);
         }

         return new SetChunkTintmap(mem.get(PacketIO.PROTO_INT, (long)(offset + 1)), mem.get(PacketIO.PROTO_INT, (long)(offset + 5)), tintmap);
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.tintmap != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.x);
      buf.writeIntLE(this.z);
      if (this.tintmap != null) {
         if (this.tintmap.length > 4096000) {
            throw ProtocolException.arrayTooLong("Tintmap", this.tintmap.length, 4096000);
         }

         VarInt.write(buf, this.tintmap.length);

         for (byte item : this.tintmap) {
            buf.writeByte(item);
         }
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.tintmap != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.x);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 5), this.z);
      int varOffset = offset + 9;
      if (this.tintmap != null) {
         if (this.tintmap.length > 4096000) {
            throw ProtocolException.arrayTooLong("Tintmap", this.tintmap.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.tintmap.length);
         MemorySegment.copy(this.tintmap, 0, mem, PacketIO.PROTO_BYTE, varOffset, this.tintmap.length);
         varOffset += this.tintmap.length * 1;
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 9;
      if (this.tintmap != null) {
         size += VarInt.size(this.tintmap.length) + this.tintmap.length * 1;
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
            int tintmapCount = VarInt.peek(buffer, pos);
            if (tintmapCount < 0) {
               return ValidationResult.error("Invalid array count for Tintmap");
            }

            if (tintmapCount > 4096000) {
               return ValidationResult.error("Tintmap exceeds max length 4096000");
            }

            pos += VarInt.size(tintmapCount);
            pos += tintmapCount * 1;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Tintmap");
            }
         }

         return ValidationResult.OK;
      }
   }

   public SetChunkTintmap clone() {
      SetChunkTintmap copy = new SetChunkTintmap();
      copy.x = this.x;
      copy.z = this.z;
      copy.tintmap = this.tintmap != null ? Arrays.copyOf(this.tintmap, this.tintmap.length) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof SetChunkTintmap other) ? false : this.x == other.x && this.z == other.z && Arrays.equals(this.tintmap, other.tintmap);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Integer.hashCode(this.x);
      result = 31 * result + Integer.hashCode(this.z);
      return 31 * result + Arrays.hashCode(this.tintmap);
   }
}
