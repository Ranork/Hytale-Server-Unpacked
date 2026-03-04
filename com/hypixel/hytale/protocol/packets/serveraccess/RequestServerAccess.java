package com.hypixel.hytale.protocol.packets.serveraccess;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
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
      RequestServerAccess obj = new RequestServerAccess();
      obj.access = Access.fromValue(buf.getByte(offset + 0));
      obj.externalPort = buf.getShortLE(offset + 1);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 3;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.access.getValue());
      buf.writeShortLE(this.externalPort);
   }

   @Override
   public int computeSize() {
      return 3;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 3 ? ValidationResult.error("Buffer too small: expected at least 3 bytes") : ValidationResult.OK;
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
