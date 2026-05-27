package com.hypixel.hytale.protocol.packets.stream;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
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
      if (buf.readableBytes() - offset < 3) {
         throw ProtocolException.bufferTooSmall("StreamOpenResponse", 3, buf.readableBytes() - offset);
      } else {
         StreamOpenResponse obj = new StreamOpenResponse();
         byte nullBits = buf.getByte(offset);
         obj.type = StreamType.fromValue(buf.getByte(offset + 1));
         obj.accepted = buf.getByte(offset + 2) != 0;
         int pos = offset + 3;
         if ((nullBits & 1) != 0) {
            int rejectionReasonLen = VarInt.peek(buf, pos);
            if (rejectionReasonLen < 0) {
               throw ProtocolException.invalidVarInt("RejectionReason");
            }

            int rejectionReasonVarLen = VarInt.size(rejectionReasonLen);
            if (rejectionReasonLen > 4096000) {
               throw ProtocolException.stringTooLong("RejectionReason", rejectionReasonLen, 4096000);
            }

            if (pos + rejectionReasonVarLen + rejectionReasonLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("RejectionReason", pos + rejectionReasonVarLen + rejectionReasonLen, buf.readableBytes());
            }

            obj.rejectionReason = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
            pos += rejectionReasonVarLen + rejectionReasonLen;
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 3;
      if ((nullBits & 1) != 0) {
         int sl = VarInt.peek(buf, pos);
         pos += VarInt.size(sl) + sl;
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 3L;
   }

   public static StreamType getType(MemorySegment mem) {
      return getType(mem, 0);
   }

   public static StreamType getType(MemorySegment mem, int offset) {
      return StreamType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1)));
   }

   public static boolean getAccepted(MemorySegment mem) {
      return getAccepted(mem, 0);
   }

   public static boolean getAccepted(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 2));
   }

   @Nullable
   public static String getRejectionReason(MemorySegment mem) {
      return getRejectionReason(mem, 0);
   }

   @Nullable
   public static String getRejectionReason(MemorySegment mem, int offset) {
      return hasRejectionReason(mem, offset) ? PacketIO.readVarString("RejectionReason", mem, offset + 3, 4096000, PacketIO.UTF8) : null;
   }

   public static boolean hasRejectionReason(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static StreamOpenResponse toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static StreamOpenResponse toObject(MemorySegment mem, int offset) {
      if (offset + 3 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("StreamOpenResponse", offset + 3, (int)mem.byteSize());
      } else {
         return new StreamOpenResponse(
            StreamType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 2)),
            hasRejectionReason(mem, offset) ? PacketIO.readVarString("RejectionReason", mem, offset + 3, 4096000, PacketIO.UTF8) : null
         );
      }
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
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.rejectionReason != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.type.getValue());
      mem.set(PacketIO.PROTO_BOOL, offset + 2, this.accepted);
      int varOffset = offset + 3;
      if (this.rejectionReason != null) {
         varOffset += PacketIO.writeVarString(mem, varOffset, this.rejectionReason, 4096000);
      }

      return varOffset - offset;
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
         int v = buffer.getByte(offset + 1) & 255;
         if (v >= 2) {
            return ValidationResult.error("Invalid StreamType value for Type");
         } else {
            v = offset + 3;
            if ((nullBits & 1) != 0) {
               int rejectionReasonLen = VarInt.peek(buffer, v);
               if (rejectionReasonLen < 0) {
                  return ValidationResult.error("Invalid string length for RejectionReason");
               }

               if (rejectionReasonLen > 4096000) {
                  return ValidationResult.error("RejectionReason exceeds max length 4096000");
               }

               v += VarInt.size(rejectionReasonLen);
               v += rejectionReasonLen;
               if (v > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading RejectionReason");
               }
            }

            return ValidationResult.OK;
         }
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
