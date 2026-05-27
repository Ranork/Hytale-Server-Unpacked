package com.hypixel.hytale.protocol.packets.stream;

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

public class StreamOpen implements Packet, ToServerPacket {
   public static final int PACKET_ID = 460;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 1;
   @Nonnull
   public StreamType type = StreamType.Game;

   @Override
   public int getId() {
      return 460;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public StreamOpen() {
   }

   public StreamOpen(@Nonnull StreamType type) {
      this.type = type;
   }

   public StreamOpen(@Nonnull StreamOpen other) {
      this.type = other.type;
   }

   @Nonnull
   public static StreamOpen deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 1) {
         throw ProtocolException.bufferTooSmall("StreamOpen", 1, buf.readableBytes() - offset);
      } else {
         StreamOpen obj = new StreamOpen();
         obj.type = StreamType.fromValue(buf.getByte(offset + 0));
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 1;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 1L;
   }

   public static StreamType getType(MemorySegment mem) {
      return getType(mem, 0);
   }

   public static StreamType getType(MemorySegment mem, int offset) {
      return StreamType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0)));
   }

   public static StreamOpen toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static StreamOpen toObject(MemorySegment mem, int offset) {
      if (offset + 1 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("StreamOpen", offset + 1, (int)mem.byteSize());
      } else {
         return new StreamOpen(StreamType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0))));
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.type.getValue());
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), (byte)this.type.getValue());
      return 1;
   }

   @Override
   public int computeSize() {
      return 1;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 1) {
         return ValidationResult.error("Buffer too small: expected at least 1 bytes");
      } else {
         int v = buffer.getByte(offset + 0) & 255;
         return v >= 2 ? ValidationResult.error("Invalid StreamType value for Type") : ValidationResult.OK;
      }
   }

   public StreamOpen clone() {
      StreamOpen copy = new StreamOpen();
      copy.type = this.type;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof StreamOpen other ? Objects.equals(this.type, other.type) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type);
   }
}
