package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.FormattedMessage;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SuccessReply implements Packet, ToServerPacket, ToClientPacket {
   public static final int PACKET_ID = 301;
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
      return 301;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public SuccessReply() {
   }

   public SuccessReply(int token, @Nullable FormattedMessage message) {
      this.token = token;
      this.message = message;
   }

   public SuccessReply(@Nonnull SuccessReply other) {
      this.token = other.token;
      this.message = other.message;
   }

   @Nonnull
   public static SuccessReply deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 5) {
         throw ProtocolException.bufferTooSmall("SuccessReply", 5, buf.readableBytes() - offset);
      } else {
         SuccessReply obj = new SuccessReply();
         byte nullBits = buf.getByte(offset);
         obj.token = buf.getIntLE(offset + 1);
         int pos = offset + 5;
         if ((nullBits & 1) != 0) {
            obj.message = FormattedMessage.deserialize(buf, pos);
            pos += FormattedMessage.computeBytesConsumed(buf, pos);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 5;
      if ((nullBits & 1) != 0) {
         pos += FormattedMessage.computeBytesConsumed(buf, pos);
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 5L;
   }

   public static int getToken(MemorySegment mem) {
      return getToken(mem, 0);
   }

   public static int getToken(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   @Nullable
   public static FormattedMessage getMessage(MemorySegment mem) {
      return getMessage(mem, 0);
   }

   @Nullable
   public static FormattedMessage getMessage(MemorySegment mem, int offset) {
      return hasMessage(mem, offset) ? FormattedMessage.toObject(mem, offset + 5) : null;
   }

   public static boolean hasMessage(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static SuccessReply toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static SuccessReply toObject(MemorySegment mem, int offset) {
      if (offset + 5 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("SuccessReply", offset + 5, (int)mem.byteSize());
      } else {
         return new SuccessReply(mem.get(PacketIO.PROTO_INT, (long)(offset + 1)), hasMessage(mem, offset) ? FormattedMessage.toObject(mem, offset + 5) : null);
      }
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
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.message != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.token);
      int varOffset = offset + 5;
      if (this.message != null) {
         varOffset += this.message.serialize(mem, varOffset);
      }

      return varOffset - offset;
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

   public SuccessReply clone() {
      SuccessReply copy = new SuccessReply();
      copy.token = this.token;
      copy.message = this.message != null ? this.message.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof SuccessReply other) ? false : this.token == other.token && Objects.equals(this.message, other.message);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.token, this.message);
   }
}
