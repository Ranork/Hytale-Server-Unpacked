package com.hypixel.hytale.protocol.packets.stream;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StreamOpenResponse implements Packet, ToClientPacket {
   public static final int PACKET_ID = 461;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 3;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 3;
   public static final int MAX_SIZE = 16384008;
   @Nonnull
   public StreamType type = StreamType.Game;
   public boolean accepted;
   @Nullable
   public String rejectionReason;

   @Override
   public int getId() {
      return 461;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public StreamOpenResponse() {
   }

   public StreamOpenResponse(@Nonnull StreamType type, boolean accepted, @Nullable String rejectionReason) {
      this.type = type;
      this.accepted = accepted;
      this.rejectionReason = rejectionReason;
   }

   public StreamOpenResponse(@Nonnull StreamOpenResponse other) {
      this.type = other.type;
      this.accepted = other.accepted;
      this.rejectionReason = other.rejectionReason;
   }

   @Nonnull
   public static StreamOpenResponse deserialize(@Nonnull ByteBuf buf, int offset) {
      StreamOpenResponse obj = new StreamOpenResponse();
      byte nullBits = buf.getByte(offset);
      obj.type = StreamType.fromValue(buf.getByte(offset + 1));
      obj.accepted = buf.getByte(offset + 2) != 0;
      int pos = offset + 3;
      if ((nullBits & 1) != 0) {
         int rejectionReasonLen = VarInt.peek(buf, pos);
         if (rejectionReasonLen < 0) {
            throw ProtocolException.negativeLength("RejectionReason", rejectionReasonLen);
         }

         if (rejectionReasonLen > 4096000) {
            throw ProtocolException.stringTooLong("RejectionReason", rejectionReasonLen, 4096000);
         }

         int rejectionReasonVarLen = VarInt.length(buf, pos);
         obj.rejectionReason = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
         pos += rejectionReasonVarLen + rejectionReasonLen;
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 3;
      if ((nullBits & 1) != 0) {
         int sl = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos) + sl;
      }

      return pos - offset;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.rejectionReason != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      buf.writeByte(this.accepted ? 1 : 0);
      if (this.rejectionReason != null) {
         PacketIO.writeVarString(buf, this.rejectionReason, 4096000);
      }
   }

   @Override
   public int computeSize() {
      int size = 3;
      if (this.rejectionReason != null) {
         size += PacketIO.stringSize(this.rejectionReason);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 3) {
         return ValidationResult.error("Buffer too small: expected at least 3 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 3;
         if ((nullBits & 1) != 0) {
            int rejectionReasonLen = VarInt.peek(buffer, pos);
            if (rejectionReasonLen < 0) {
               return ValidationResult.error("Invalid string length for RejectionReason");
            }

            if (rejectionReasonLen > 4096000) {
               return ValidationResult.error("RejectionReason exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += rejectionReasonLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading RejectionReason");
            }
         }

         return ValidationResult.OK;
      }
   }

   public StreamOpenResponse clone() {
      StreamOpenResponse copy = new StreamOpenResponse();
      copy.type = this.type;
      copy.accepted = this.accepted;
      copy.rejectionReason = this.rejectionReason;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof StreamOpenResponse other)
            ? false
            : Objects.equals(this.type, other.type) && this.accepted == other.accepted && Objects.equals(this.rejectionReason, other.rejectionReason);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.accepted, this.rejectionReason);
   }
}
