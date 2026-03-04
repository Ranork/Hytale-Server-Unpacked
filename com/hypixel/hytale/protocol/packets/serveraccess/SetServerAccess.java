package com.hypixel.hytale.protocol.packets.serveraccess;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
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
      SetServerAccess obj = new SetServerAccess();
      byte nullBits = buf.getByte(offset);
      obj.access = Access.fromValue(buf.getByte(offset + 1));
      int pos = offset + 2;
      if ((nullBits & 1) != 0) {
         int passwordLen = VarInt.peek(buf, pos);
         if (passwordLen < 0) {
            throw ProtocolException.negativeLength("Password", passwordLen);
         }

         if (passwordLen > 4096000) {
            throw ProtocolException.stringTooLong("Password", passwordLen, 4096000);
         }

         int passwordVarLen = VarInt.length(buf, pos);
         obj.password = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
         pos += passwordVarLen + passwordLen;
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 2;
      if ((nullBits & 1) != 0) {
         int sl = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos) + sl;
      }

      return pos - offset;
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
         int pos = offset + 2;
         if ((nullBits & 1) != 0) {
            int passwordLen = VarInt.peek(buffer, pos);
            if (passwordLen < 0) {
               return ValidationResult.error("Invalid string length for Password");
            }

            if (passwordLen > 4096000) {
               return ValidationResult.error("Password exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += passwordLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Password");
            }
         }

         return ValidationResult.OK;
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
