package com.hypixel.hytale.protocol.packets.interface_;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CustomPageEvent implements Packet, ToServerPacket {
   public static final int PACKET_ID = 219;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 2;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 2;
   public static final int MAX_SIZE = 16384007;
   @Nonnull
   public CustomPageEventType type = CustomPageEventType.Acknowledge;
   @Nullable
   public String data;

   @Override
   public int getId() {
      return 219;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public CustomPageEvent() {
   }

   public CustomPageEvent(@Nonnull CustomPageEventType type, @Nullable String data) {
      this.type = type;
      this.data = data;
   }

   public CustomPageEvent(@Nonnull CustomPageEvent other) {
      this.type = other.type;
      this.data = other.data;
   }

   @Nonnull
   public static CustomPageEvent deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 2) {
         throw ProtocolException.bufferTooSmall("CustomPageEvent", 2, buf.readableBytes() - offset);
      } else {
         CustomPageEvent obj = new CustomPageEvent();
         byte nullBits = buf.getByte(offset);
         obj.type = CustomPageEventType.fromValue(buf.getByte(offset + 1));
         int pos = offset + 2;
         if ((nullBits & 1) != 0) {
            int dataLen = VarInt.peek(buf, pos);
            if (dataLen < 0) {
               throw ProtocolException.invalidVarInt("Data");
            }

            int dataVarLen = VarInt.size(dataLen);
            if (dataLen > 4096000) {
               throw ProtocolException.stringTooLong("Data", dataLen, 4096000);
            }

            if (pos + dataVarLen + dataLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Data", pos + dataVarLen + dataLen, buf.readableBytes());
            }

            obj.data = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
            pos += dataVarLen + dataLen;
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 2;
      if ((nullBits & 1) != 0) {
         int sl = VarInt.peek(buf, pos);
         pos += VarInt.size(sl) + sl;
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 2L;
   }

   public static CustomPageEventType getType(MemorySegment mem) {
      return getType(mem, 0);
   }

   public static CustomPageEventType getType(MemorySegment mem, int offset) {
      return CustomPageEventType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1)));
   }

   @Nullable
   public static String getData(MemorySegment mem) {
      return getData(mem, 0);
   }

   @Nullable
   public static String getData(MemorySegment mem, int offset) {
      return hasData(mem, offset) ? PacketIO.readVarString("Data", mem, offset + 2, 4096000, PacketIO.UTF8) : null;
   }

   public static boolean hasData(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static CustomPageEvent toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static CustomPageEvent toObject(MemorySegment mem, int offset) {
      if (offset + 2 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("CustomPageEvent", offset + 2, (int)mem.byteSize());
      } else {
         return new CustomPageEvent(
            CustomPageEventType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))),
            hasData(mem, offset) ? PacketIO.readVarString("Data", mem, offset + 2, 4096000, PacketIO.UTF8) : null
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.data != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      if (this.data != null) {
         PacketIO.writeVarString(buf, this.data, 4096000);
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.data != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.type.getValue());
      int varOffset = offset + 2;
      if (this.data != null) {
         varOffset += PacketIO.writeVarString(mem, varOffset, this.data, 4096000);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 2;
      if (this.data != null) {
         size += PacketIO.stringSize(this.data);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 2) {
         return ValidationResult.error("Buffer too small: expected at least 2 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int v = buffer.getByte(offset + 1) & 255;
         if (v >= 3) {
            return ValidationResult.error("Invalid CustomPageEventType value for Type");
         } else {
            v = offset + 2;
            if ((nullBits & 1) != 0) {
               int dataLen = VarInt.peek(buffer, v);
               if (dataLen < 0) {
                  return ValidationResult.error("Invalid string length for Data");
               }

               if (dataLen > 4096000) {
                  return ValidationResult.error("Data exceeds max length 4096000");
               }

               v += VarInt.size(dataLen);
               v += dataLen;
               if (v > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading Data");
               }
            }

            return ValidationResult.OK;
         }
      }
   }

   public CustomPageEvent clone() {
      CustomPageEvent copy = new CustomPageEvent();
      copy.type = this.type;
      copy.data = this.data;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof CustomPageEvent other) ? false : Objects.equals(this.type, other.type) && Objects.equals(this.data, other.data);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.data);
   }
}
