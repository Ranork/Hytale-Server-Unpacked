package com.hypixel.hytale.protocol.packets.setup;

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

public class ViewRadius implements Packet, ToServerPacket, ToClientPacket {
   public static final int PACKET_ID = 32;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 4;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 4;
   public static final int MAX_SIZE = 4;
   public int value;

   @Override
   public int getId() {
      return 32;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public ViewRadius() {
   }

   public ViewRadius(int value) {
      this.value = value;
   }

   public ViewRadius(@Nonnull ViewRadius other) {
      this.value = other.value;
   }

   @Nonnull
   public static ViewRadius deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 4) {
         throw ProtocolException.bufferTooSmall("ViewRadius", 4, buf.readableBytes() - offset);
      } else {
         ViewRadius obj = new ViewRadius();
         obj.value = buf.getIntLE(offset + 0);
         if (obj.value < 0) {
            throw ProtocolException.valueBelowMinimum("Value", obj.value, 0.0);
         } else {
            return obj;
         }
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 4;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 4L;
   }

   public static int getValue(MemorySegment mem) {
      return getValue(mem, 0);
   }

   public static int getValue(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 0));
   }

   public static ViewRadius toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ViewRadius toObject(MemorySegment mem, int offset) {
      if (offset + 4 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ViewRadius", offset + 4, (int)mem.byteSize());
      } else {
         return new ViewRadius(mem.get(PacketIO.PROTO_INT, (long)(offset + 0)));
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      if (this.value < 0) {
         throw ProtocolException.valueBelowMinimum("Value", this.value, 0.0);
      } else {
         buf.writeIntLE(this.value);
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_INT, (long)(offset + 0), this.value);
      return 4;
   }

   @Override
   public int computeSize() {
      return 4;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 4) {
         return ValidationResult.error("Buffer too small: expected at least 4 bytes");
      } else {
         int valueVal = buffer.getIntLE(offset + 0);
         return valueVal < 0 ? ValidationResult.error("Value value out of range") : ValidationResult.OK;
      }
   }

   public ViewRadius clone() {
      ViewRadius copy = new ViewRadius();
      copy.value = this.value;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof ViewRadius other ? this.value == other.value : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.value);
   }
}
