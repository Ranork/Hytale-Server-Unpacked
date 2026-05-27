package com.hypixel.hytale.protocol.packets.serveraccess;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SetServerAccess implements Packet, ToServerPacket {
   public static final int PACKET_ID = 252;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 2;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 2;
   public static final int MAX_SIZE = 16384007;
   @Nonnull
   public Access access = Access.Private;
   @Nullable
   public String password;

   @Override
   public int getId() {
      return 252;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public SetServerAccess() {
   }

   public SetServerAccess(@Nonnull Access access, @Nullable String password) {
      this.access = access;
      this.password = password;
   }

   public SetServerAccess(@Nonnull SetServerAccess other) {
      this.access = other.access;
      this.password = other.password;
   }

   @Nonnull
   public static SetServerAccess deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 2) {
         throw ProtocolException.bufferTooSmall("SetServerAccess", 2, buf.readableBytes() - offset);
      } else {
         SetServerAccess obj = new SetServerAccess();
         byte nullBits = buf.getByte(offset);
         obj.access = Access.fromValue(buf.getByte(offset + 1));
         int pos = offset + 2;
         if ((nullBits & 1) != 0) {
            int passwordLen = VarInt.peek(buf, pos);
            if (passwordLen < 0) {
               throw ProtocolException.invalidVarInt("Password");
            }

            int passwordVarLen = VarInt.size(passwordLen);
            if (passwordLen > 4096000) {
               throw ProtocolException.stringTooLong("Password", passwordLen, 4096000);
            }

            if (pos + passwordVarLen + passwordLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Password", pos + passwordVarLen + passwordLen, buf.readableBytes());
            }

            obj.password = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
            pos += passwordVarLen + passwordLen;
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 2;
      if ((nullBits & 1) != 0) {
         int sl = VarInt.peek(buf, pos);
         pos += VarInt.size(sl) + sl;
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 2L;
   }

   public static Access getAccess(MemorySegment mem) {
      return getAccess(mem, 0);
   }

   public static Access getAccess(MemorySegment mem, int offset) {
      return Access.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1)));
   }

   @Nullable
   public static String getPassword(MemorySegment mem) {
      return getPassword(mem, 0);
   }

   @Nullable
   public static String getPassword(MemorySegment mem, int offset) {
      return hasPassword(mem, offset) ? PacketIO.readVarString("Password", mem, offset + 2, 4096000, PacketIO.UTF8) : null;
   }

   public static boolean hasPassword(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static SetServerAccess toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static SetServerAccess toObject(MemorySegment mem, int offset) {
      if (offset + 2 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("SetServerAccess", offset + 2, (int)mem.byteSize());
      } else {
         return new SetServerAccess(
            Access.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))),
            hasPassword(mem, offset) ? PacketIO.readVarString("Password", mem, offset + 2, 4096000, PacketIO.UTF8) : null
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.password != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.access.getValue());
      if (this.password != null) {
         PacketIO.writeVarString(buf, this.password, 4096000);
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.password != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.access.getValue());
      int varOffset = offset + 2;
      if (this.password != null) {
         varOffset += PacketIO.writeVarString(mem, varOffset, this.password, 4096000);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 2;
      if (this.password != null) {
         size += PacketIO.stringSize(this.password);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 2) {
         return ValidationResult.error("Buffer too small: expected at least 2 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int v = buffer.getByte(offset + 1) & 255;
         if (v >= 4) {
            return ValidationResult.error("Invalid Access value for Access");
         } else {
            v = offset + 2;
            if ((nullBits & 1) != 0) {
               int passwordLen = VarInt.peek(buffer, v);
               if (passwordLen < 0) {
                  return ValidationResult.error("Invalid string length for Password");
               }

               if (passwordLen > 4096000) {
                  return ValidationResult.error("Password exceeds max length 4096000");
               }

               v += VarInt.size(passwordLen);
               v += passwordLen;
               if (v > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading Password");
               }
            }

            return ValidationResult.OK;
         }
      }
   }

   public SetServerAccess clone() {
      SetServerAccess copy = new SetServerAccess();
      copy.access = this.access;
      copy.password = this.password;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof SetServerAccess other) ? false : Objects.equals(this.access, other.access) && Objects.equals(this.password, other.password);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.access, this.password);
   }
}
