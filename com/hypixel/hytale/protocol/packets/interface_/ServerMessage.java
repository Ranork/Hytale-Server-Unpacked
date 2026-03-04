package com.hypixel.hytale.protocol.packets.interface_;

import com.hypixel.hytale.protocol.FormattedMessage;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ServerMessage implements Packet, ToClientPacket {
   public static final int PACKET_ID = 210;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 2;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 2;
   public static final int MAX_SIZE = 1677721600;
   @Nonnull
   public ChatType type = ChatType.Chat;
   @Nullable
   public FormattedMessage message;

   @Override
   public int getId() {
      return 210;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public ServerMessage() {
   }

   public ServerMessage(@Nonnull ChatType type, @Nullable FormattedMessage message) {
      this.type = type;
      this.message = message;
   }

   public ServerMessage(@Nonnull ServerMessage other) {
      this.type = other.type;
      this.message = other.message;
   }

   @Nonnull
   public static ServerMessage deserialize(@Nonnull ByteBuf buf, int offset) {
      ServerMessage obj = new ServerMessage();
      byte nullBits = buf.getByte(offset);
      obj.type = ChatType.fromValue(buf.getByte(offset + 1));
      int pos = offset + 2;
      if ((nullBits & 1) != 0) {
         obj.message = FormattedMessage.deserialize(buf, pos);
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
      if (this.message != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      if (this.message != null) {
         this.message.serialize(buf);
      }
   }

   @Override
   public int computeSize() {
      int size = 2;
      if (this.message != null) {
         size += this.message.computeSize();
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
            ValidationResult messageResult = FormattedMessage.validateStructure(buffer, pos);
            if (!messageResult.isValid()) {
               return ValidationResult.error("Invalid Message: " + messageResult.error());
            }

            pos += FormattedMessage.computeBytesConsumed(buffer, pos);
         }

         return ValidationResult.OK;
      }
   }

   public ServerMessage clone() {
      ServerMessage copy = new ServerMessage();
      copy.type = this.type;
      copy.message = this.message != null ? this.message.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ServerMessage other) ? false : Objects.equals(this.type, other.type) && Objects.equals(this.message, other.message);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.message);
   }
}
