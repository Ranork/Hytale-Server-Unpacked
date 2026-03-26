package com.hypixel.hytale.protocol.packets.connection;

import com.hypixel.hytale.protocol.FormattedMessage;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
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

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 2;
      if ((nullBits & 1) != 0) {
         pos += FormattedMessage.computeBytesConsumed(buf, pos);
      }

      return pos - offset;
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
         int pos = offset + 2;
         if ((nullBits & 1) != 0) {
            ValidationResult reasonResult = FormattedMessage.validateStructure(buffer, pos);
            if (!reasonResult.isValid()) {
               return ValidationResult.error("Invalid Reason: " + reasonResult.error());
            }

            pos += FormattedMessage.computeBytesConsumed(buffer, pos);
         }

         return ValidationResult.OK;
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
