package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class MouseButtonEvent {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 3;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 3;
   public static final int MAX_SIZE = 3;
   @Nonnull
   public MouseButtonType mouseButtonType = MouseButtonType.Left;
   @Nonnull
   public MouseButtonState state = MouseButtonState.Pressed;
   public byte clicks;

   public MouseButtonEvent() {
   }

   public MouseButtonEvent(@Nonnull MouseButtonType mouseButtonType, @Nonnull MouseButtonState state, byte clicks) {
      this.mouseButtonType = mouseButtonType;
      this.state = state;
      this.clicks = clicks;
   }

   public MouseButtonEvent(@Nonnull MouseButtonEvent other) {
      this.mouseButtonType = other.mouseButtonType;
      this.state = other.state;
      this.clicks = other.clicks;
   }

   @Nonnull
   public static MouseButtonEvent deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 3) {
         throw ProtocolException.bufferTooSmall("MouseButtonEvent", 3, buf.readableBytes() - offset);
      } else {
         MouseButtonEvent obj = new MouseButtonEvent();
         obj.mouseButtonType = MouseButtonType.fromValue(buf.getByte(offset + 0));
         obj.state = MouseButtonState.fromValue(buf.getByte(offset + 1));
         obj.clicks = buf.getByte(offset + 2);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 3;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 3L;
   }

   public static MouseButtonType getMouseButtonType(MemorySegment mem) {
      return getMouseButtonType(mem, 0);
   }

   public static MouseButtonType getMouseButtonType(MemorySegment mem, int offset) {
      return MouseButtonType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0)));
   }

   public static MouseButtonState getState(MemorySegment mem) {
      return getState(mem, 0);
   }

   public static MouseButtonState getState(MemorySegment mem, int offset) {
      return MouseButtonState.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1)));
   }

   public static byte getClicks(MemorySegment mem) {
      return getClicks(mem, 0);
   }

   public static byte getClicks(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BYTE, (long)(offset + 2));
   }

   public static MouseButtonEvent toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static MouseButtonEvent toObject(MemorySegment mem, int offset) {
      if (offset + 3 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("MouseButtonEvent", offset + 3, (int)mem.byteSize());
      } else {
         return new MouseButtonEvent(
            MouseButtonType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0))),
            MouseButtonState.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))),
            mem.get(PacketIO.PROTO_BYTE, (long)(offset + 2))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.mouseButtonType.getValue());
      buf.writeByte(this.state.getValue());
      buf.writeByte(this.clicks);
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), (byte)this.mouseButtonType.getValue());
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.state.getValue());
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 2), this.clicks);
      return 3;
   }

   public int computeSize() {
      return 3;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 3) {
         return ValidationResult.error("Buffer too small: expected at least 3 bytes");
      } else {
         int v = buffer.getByte(offset + 0) & 255;
         if (v >= 5) {
            return ValidationResult.error("Invalid MouseButtonType value for MouseButtonType");
         } else {
            v = buffer.getByte(offset + 1) & 255;
            return v >= 2 ? ValidationResult.error("Invalid MouseButtonState value for State") : ValidationResult.OK;
         }
      }
   }

   public MouseButtonEvent clone() {
      MouseButtonEvent copy = new MouseButtonEvent();
      copy.mouseButtonType = this.mouseButtonType;
      copy.state = this.state;
      copy.clicks = this.clicks;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof MouseButtonEvent other)
            ? false
            : Objects.equals(this.mouseButtonType, other.mouseButtonType) && Objects.equals(this.state, other.state) && this.clicks == other.clicks;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.mouseButtonType, this.state, this.clicks);
   }
}
