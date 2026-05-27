package com.hypixel.hytale.protocol.packets.connection;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class ClientDisconnect implements Packet, ToServerPacket {
   public static final int PACKET_ID = 1;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 2;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 2;
   public static final int MAX_SIZE = 2;
   @Nonnull
   public ClientDisconnectReason reason = ClientDisconnectReason.PlayerLeave;
   @Nonnull
   public DisconnectType type = DisconnectType.Disconnect;

   @Override
   public int getId() {
      return 1;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public ClientDisconnect() {
   }

   public ClientDisconnect(@Nonnull ClientDisconnectReason reason, @Nonnull DisconnectType type) {
      this.reason = reason;
      this.type = type;
   }

   public ClientDisconnect(@Nonnull ClientDisconnect other) {
      this.reason = other.reason;
      this.type = other.type;
   }

   @Nonnull
   public static ClientDisconnect deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 2) {
         throw ProtocolException.bufferTooSmall("ClientDisconnect", 2, buf.readableBytes() - offset);
      } else {
         ClientDisconnect obj = new ClientDisconnect();
         obj.reason = ClientDisconnectReason.fromValue(buf.getByte(offset + 0));
         obj.type = DisconnectType.fromValue(buf.getByte(offset + 1));
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 2;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 2L;
   }

   public static ClientDisconnectReason getReason(MemorySegment mem) {
      return getReason(mem, 0);
   }

   public static ClientDisconnectReason getReason(MemorySegment mem, int offset) {
      return ClientDisconnectReason.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0)));
   }

   public static DisconnectType getType(MemorySegment mem) {
      return getType(mem, 0);
   }

   public static DisconnectType getType(MemorySegment mem, int offset) {
      return DisconnectType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1)));
   }

   public static ClientDisconnect toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ClientDisconnect toObject(MemorySegment mem, int offset) {
      if (offset + 2 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ClientDisconnect", offset + 2, (int)mem.byteSize());
      } else {
         return new ClientDisconnect(
            ClientDisconnectReason.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0))),
            DisconnectType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1)))
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.reason.getValue());
      buf.writeByte(this.type.getValue());
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), (byte)this.reason.getValue());
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.type.getValue());
      return 2;
   }

   @Override
   public int computeSize() {
      return 2;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 2) {
         return ValidationResult.error("Buffer too small: expected at least 2 bytes");
      } else {
         int v = buffer.getByte(offset + 0) & 255;
         if (v >= 4) {
            return ValidationResult.error("Invalid ClientDisconnectReason value for Reason");
         } else {
            v = buffer.getByte(offset + 1) & 255;
            return v >= 2 ? ValidationResult.error("Invalid DisconnectType value for Type") : ValidationResult.OK;
         }
      }
   }

   public ClientDisconnect clone() {
      ClientDisconnect copy = new ClientDisconnect();
      copy.reason = this.reason;
      copy.type = this.type;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ClientDisconnect other) ? false : Objects.equals(this.reason, other.reason) && Objects.equals(this.type, other.type);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.reason, this.type);
   }
}
