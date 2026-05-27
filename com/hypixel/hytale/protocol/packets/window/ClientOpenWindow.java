package com.hypixel.hytale.protocol.packets.window;

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

public class ClientOpenWindow implements Packet, ToServerPacket {
   public static final int PACKET_ID = 204;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 1;
   @Nonnull
   public WindowType type = WindowType.Container;

   @Override
   public int getId() {
      return 204;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public ClientOpenWindow() {
   }

   public ClientOpenWindow(@Nonnull WindowType type) {
      this.type = type;
   }

   public ClientOpenWindow(@Nonnull ClientOpenWindow other) {
      this.type = other.type;
   }

   @Nonnull
   public static ClientOpenWindow deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 1) {
         throw ProtocolException.bufferTooSmall("ClientOpenWindow", 1, buf.readableBytes() - offset);
      } else {
         ClientOpenWindow obj = new ClientOpenWindow();
         obj.type = WindowType.fromValue(buf.getByte(offset + 0));
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 1;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 1L;
   }

   public static WindowType getType(MemorySegment mem) {
      return getType(mem, 0);
   }

   public static WindowType getType(MemorySegment mem, int offset) {
      return WindowType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0)));
   }

   public static ClientOpenWindow toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ClientOpenWindow toObject(MemorySegment mem, int offset) {
      if (offset + 1 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ClientOpenWindow", offset + 1, (int)mem.byteSize());
      } else {
         return new ClientOpenWindow(WindowType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0))));
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
         return v >= 7 ? ValidationResult.error("Invalid WindowType value for Type") : ValidationResult.OK;
      }
   }

   public ClientOpenWindow clone() {
      ClientOpenWindow copy = new ClientOpenWindow();
      copy.type = this.type;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof ClientOpenWindow other ? Objects.equals(this.type, other.type) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type);
   }
}
