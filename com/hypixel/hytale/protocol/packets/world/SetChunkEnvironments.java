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

public class SetChunkEnvironments implements Packet, ToClientPacket {
   public static final int PACKET_ID = 134;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 9;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 4096014;
   public int x;
   public int z;
   @Nullable
   public byte[] environments;

   @Override
   public int getId() {
      return 134;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Chunks;
   }

   public SetChunkEnvironments() {
   }

   public SetChunkEnvironments(int x, int z, @Nullable byte[] environments) {
      this.x = x;
      this.z = z;
      this.environments = environments;
   }

   public SetChunkEnvironments(@Nonnull SetChunkEnvironments other) {
      this.x = other.x;
      this.z = other.z;
      this.environments = other.environments;
   }

   @Nonnull
   public static SetChunkEnvironments deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 9) {
         throw ProtocolException.bufferTooSmall("SetChunkEnvironments", 9, buf.readableBytes() - offset);
      } else {
         SetChunkEnvironments obj = new SetChunkEnvironments();
         byte nullBits = buf.getByte(offset);
         obj.x = buf.getIntLE(offset + 1);
         obj.z = buf.getIntLE(offset + 5);
         int pos = offset + 9;
         if ((nullBits & 1) != 0) {
            int environmentsCount = VarInt.peek(buf, pos);
            if (environmentsCount < 0) {
               throw ProtocolException.invalidVarInt("Environments");
            }

            int environmentsVarLen = VarInt.size(environmentsCount);
            if (environmentsCount > 4096000) {
               throw ProtocolException.arrayTooLong("Environments", environmentsCount, 4096000);
            }

            if (pos + environmentsVarLen + environmentsCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Environments", pos + environmentsVarLen + environmentsCount * 1, buf.readableBytes());
            }

            pos += environmentsVarLen;
            obj.environments = new byte[environmentsCount];

            for (int i = 0; i < environmentsCount; i++) {
               obj.environments[i] = buf.getByte(pos + i * 1);
            }

            pos += environmentsCount * 1;
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
   public static byte[] getEnvironments(MemorySegment mem) {
      return getEnvironments(mem, 0);
   }

   @Nullable
   public static byte[] getEnvironments(MemorySegment mem, int offset) {
      if (!hasEnvironments(mem, offset)) {
         return null;
      } else {
         int off = offset + 9;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Environments", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Environments", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Environments", off + lenOffset + len * 1, (int)mem.byteSize());
            } else {
               off += lenOffset;
               byte[] data = new byte[len];
               MemorySegment.copy(mem, PacketIO.PROTO_BYTE, off, data, 0, len);
               return data;
            }
         }
      }
   }

   public static boolean hasEnvironments(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static SetChunkEnvironments toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static SetChunkEnvironments toObject(MemorySegment mem, int offset) {
      if (offset + 9 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("SetChunkEnvironments", offset + 9, (int)mem.byteSize());
      } else {
         byte[] environments = null;
         if (hasEnvironments(mem, offset)) {
            int off = offset + 9;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Environments", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Environments", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Environments", off + lenOffset + len * 1, (int)mem.byteSize());
            }

            off += lenOffset;
            environments = new byte[len];
            MemorySegment.copy(mem, PacketIO.PROTO_BYTE, off, environments, 0, len);
         }

         return new SetChunkEnvironments(mem.get(PacketIO.PROTO_INT, (long)(offset + 1)), mem.get(PacketIO.PROTO_INT, (long)(offset + 5)), environments);
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.environments != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.x);
      buf.writeIntLE(this.z);
      if (this.environments != null) {
         if (this.environments.length > 4096000) {
            throw ProtocolException.arrayTooLong("Environments", this.environments.length, 4096000);
         }

         VarInt.write(buf, this.environments.length);

         for (byte item : this.environments) {
            buf.writeByte(item);
         }
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.environments != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.x);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 5), this.z);
      int varOffset = offset + 9;
      if (this.environments != null) {
         if (this.environments.length > 4096000) {
            throw ProtocolException.arrayTooLong("Environments", this.environments.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.environments.length);
         MemorySegment.copy(this.environments, 0, mem, PacketIO.PROTO_BYTE, varOffset, this.environments.length);
         varOffset += this.environments.length * 1;
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 9;
      if (this.environments != null) {
         size += VarInt.size(this.environments.length) + this.environments.length * 1;
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
            int environmentsCount = VarInt.peek(buffer, pos);
            if (environmentsCount < 0) {
               return ValidationResult.error("Invalid array count for Environments");
            }

            if (environmentsCount > 4096000) {
               return ValidationResult.error("Environments exceeds max length 4096000");
            }

            pos += VarInt.size(environmentsCount);
            pos += environmentsCount * 1;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Environments");
            }
         }

         return ValidationResult.OK;
      }
   }

   public SetChunkEnvironments clone() {
      SetChunkEnvironments copy = new SetChunkEnvironments();
      copy.x = this.x;
      copy.z = this.z;
      copy.environments = this.environments != null ? Arrays.copyOf(this.environments, this.environments.length) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof SetChunkEnvironments other)
            ? false
            : this.x == other.x && this.z == other.z && Arrays.equals(this.environments, other.environments);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Integer.hashCode(this.x);
      result = 31 * result + Integer.hashCode(this.z);
      return 31 * result + Arrays.hashCode(this.environments);
   }
}
