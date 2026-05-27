package com.hypixel.hytale.protocol.packets.connection;

import com.hypixel.hytale.protocol.FormattedMessage;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ServerDisconnect implements Packet, ToClientPacket {
   public static final int PACKET_ID = 2;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 2;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 2;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public FormattedMessage reason;
   @Nonnull
   public DisconnectType type = DisconnectType.Disconnect;

   @Override
   public int getId() {
      return 2;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public ServerDisconnect() {
   }

   public ServerDisconnect(@Nullable FormattedMessage reason, @Nonnull DisconnectType type) {
      this.reason = reason;
      this.type = type;
   }

   public ServerDisconnect(@Nonnull ServerDisconnect other) {
      this.reason = other.reason;
      this.type = other.type;
   }

   @Nonnull
   public static ServerDisconnect deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 2) {
         throw ProtocolException.bufferTooSmall("ServerDisconnect", 2, buf.readableBytes() - offset);
      } else {
         ServerDisconnect obj = new ServerDisconnect();
         byte nullBits = buf.getByte(offset);
         obj.type = DisconnectType.fromValue(buf.getByte(offset + 1));
         int pos = offset + 2;
         if ((nullBits & 1) != 0) {
            obj.reason = FormattedMessage.deserialize(buf, pos);
            pos += FormattedMessage.computeBytesConsumed(buf, pos);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 2;
      if ((nullBits & 1) != 0) {
         pos += FormattedMessage.computeBytesConsumed(buf, pos);
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 2L;
   }

   @Nullable
   public static FormattedMessage getReason(MemorySegment mem) {
      return getReason(mem, 0);
   }

   @Nullable
   public static FormattedMessage getReason(MemorySegment mem, int offset) {
      return hasReason(mem, offset) ? FormattedMessage.toObject(mem, offset + 2) : null;
   }

   public static DisconnectType getType(MemorySegment mem) {
      return getType(mem, 0);
   }

   public static DisconnectType getType(MemorySegment mem, int offset) {
      return DisconnectType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1)));
   }

   public static boolean hasReason(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static ServerDisconnect toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ServerDisconnect toObject(MemorySegment mem, int offset) {
      if (offset + 2 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ServerDisconnect", offset + 2, (int)mem.byteSize());
      } else {
         return new ServerDisconnect(
            hasReason(mem, offset) ? FormattedMessage.toObject(mem, offset + 2) : null,
            DisconnectType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1)))
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.reason != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      if (this.reason != null) {
         this.reason.serialize(buf);
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.reason != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.type.getValue());
      int varOffset = offset + 2;
      if (this.reason != null) {
         varOffset += this.reason.serialize(mem, varOffset);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 2;
      if (this.reason != null) {
         size += this.reason.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 2) {
         return ValidationResult.error("Buffer too small: expected at least 2 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int v = buffer.getByte(offset + 1) & 255;
         if (v >= 2) {
            return ValidationResult.error("Invalid DisconnectType value for Type");
         } else {
            v = offset + 2;
            if ((nullBits & 1) != 0) {
               ValidationResult reasonResult = FormattedMessage.validateStructure(buffer, v);
               if (!reasonResult.isValid()) {
                  return ValidationResult.error("Invalid Reason: " + reasonResult.error());
               }

               v += FormattedMessage.computeBytesConsumed(buffer, v);
            }

            return ValidationResult.OK;
         }
      }
   }

   public ServerDisconnect clone() {
      ServerDisconnect copy = new ServerDisconnect();
      copy.reason = this.reason != null ? this.reason.clone() : null;
      copy.type = this.type;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ServerDisconnect other) ? false : Objects.equals(this.reason, other.reason) && Objects.equals(this.type, other.type);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.reason, this.type);
   }
}
