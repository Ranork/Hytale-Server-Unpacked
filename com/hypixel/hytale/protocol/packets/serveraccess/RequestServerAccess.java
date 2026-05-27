package com.hypixel.hytale.protocol.packets.serveraccess;

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

public class RequestServerAccess implements Packet, ToClientPacket {
   public static final int PACKET_ID = 250;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 3;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 3;
   public static final int MAX_SIZE = 3;
   @Nonnull
   public Access access = Access.Private;
   public short externalPort;

   @Override
   public int getId() {
      return 250;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public RequestServerAccess() {
   }

   public RequestServerAccess(@Nonnull Access access, short externalPort) {
      this.access = access;
      this.externalPort = externalPort;
   }

   public RequestServerAccess(@Nonnull RequestServerAccess other) {
      this.access = other.access;
      this.externalPort = other.externalPort;
   }

   @Nonnull
   public static RequestServerAccess deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 3) {
         throw ProtocolException.bufferTooSmall("RequestServerAccess", 3, buf.readableBytes() - offset);
      } else {
         RequestServerAccess obj = new RequestServerAccess();
         obj.access = Access.fromValue(buf.getByte(offset + 0));
         obj.externalPort = buf.getShortLE(offset + 1);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 3;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 3L;
   }

   public static Access getAccess(MemorySegment mem) {
      return getAccess(mem, 0);
   }

   public static Access getAccess(MemorySegment mem, int offset) {
      return Access.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0)));
   }

   public static short getExternalPort(MemorySegment mem) {
      return getExternalPort(mem, 0);
   }

   public static short getExternalPort(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_SHORT, (long)(offset + 1));
   }

   public static RequestServerAccess toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static RequestServerAccess toObject(MemorySegment mem, int offset) {
      if (offset + 3 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("RequestServerAccess", offset + 3, (int)mem.byteSize());
      } else {
         return new RequestServerAccess(Access.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0))), mem.get(PacketIO.PROTO_SHORT, (long)(offset + 1)));
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.access.getValue());
      buf.writeShortLE(this.externalPort);
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), (byte)this.access.getValue());
      mem.set(PacketIO.PROTO_SHORT, (long)(offset + 1), this.externalPort);
      return 3;
   }

   @Override
   public int computeSize() {
      return 3;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 3) {
         return ValidationResult.error("Buffer too small: expected at least 3 bytes");
      } else {
         int v = buffer.getByte(offset + 0) & 255;
         return v >= 4 ? ValidationResult.error("Invalid Access value for Access") : ValidationResult.OK;
      }
   }

   public RequestServerAccess clone() {
      RequestServerAccess copy = new RequestServerAccess();
      copy.access = this.access;
      copy.externalPort = this.externalPort;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof RequestServerAccess other) ? false : Objects.equals(this.access, other.access) && this.externalPort == other.externalPort;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.access, this.externalPort);
   }
}
