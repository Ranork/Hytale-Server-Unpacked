package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.FormattedMessage;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FailureReply implements Packet, ToServerPacket, ToClientPacket {
   public static final int PACKET_ID = 300;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 5;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 5;
   public static final int MAX_SIZE = 1677721600;
   public int token;
   @Nullable
   public FormattedMessage message;

   @Override
   public int getId() {
      return 300;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public FailureReply() {
   }

   public FailureReply(int token, @Nullable FormattedMessage message) {
      this.token = token;
      this.message = message;
   }

   public FailureReply(@Nonnull FailureReply other) {
      this.token = other.token;
      this.message = other.message;
   }

   @Nonnull
   public static FailureReply deserialize(@Nonnull ByteBuf buf, int offset) {
      FailureReply obj = new FailureReply();
      byte nullBits = buf.getByte(offset);
      obj.token = buf.getIntLE(offset + 1);
      int pos = offset + 5;
      if ((nullBits & 1) != 0) {
         obj.message = FormattedMessage.deserialize(buf, pos);
         pos += FormattedMessage.computeBytesConsumed(buf, pos);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 5;
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
      buf.writeIntLE(this.token);
      if (this.message != null) {
         this.message.serialize(buf);
      }
   }

   @Override
   public int computeSize() {
      int size = 5;
      if (this.message != null) {
         size += this.message.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 5) {
         return ValidationResult.error("Buffer too small: expected at least 5 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 5;
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

   public FailureReply clone() {
      FailureReply copy = new FailureReply();
      copy.token = this.token;
      copy.message = this.message != null ? this.message.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof FailureReply other) ? false : this.token == other.token && Objects.equals(this.message, other.message);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.token, this.message);
   }
}
