package com.hypixel.hytale.protocol.packets.buildertools;

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

public class BuilderToolRotateClipboard implements Packet, ToServerPacket {
   public static final int PACKET_ID = 406;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 5;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 5;
   public static final int MAX_SIZE = 5;
   public int angle;
   @Nonnull
   public Axis axis = Axis.X;

   @Override
   public int getId() {
      return 406;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public BuilderToolRotateClipboard() {
   }

   public BuilderToolRotateClipboard(int angle, @Nonnull Axis axis) {
      this.angle = angle;
      this.axis = axis;
   }

   public BuilderToolRotateClipboard(@Nonnull BuilderToolRotateClipboard other) {
      this.angle = other.angle;
      this.axis = other.axis;
   }

   @Nonnull
   public static BuilderToolRotateClipboard deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 5) {
         throw ProtocolException.bufferTooSmall("BuilderToolRotateClipboard", 5, buf.readableBytes() - offset);
      } else {
         BuilderToolRotateClipboard obj = new BuilderToolRotateClipboard();
         obj.angle = buf.getIntLE(offset + 0);
         obj.axis = Axis.fromValue(buf.getByte(offset + 4));
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 5;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 5L;
   }

   public static int getAngle(MemorySegment mem) {
      return getAngle(mem, 0);
   }

   public static int getAngle(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 0));
   }

   public static Axis getAxis(MemorySegment mem) {
      return getAxis(mem, 0);
   }

   public static Axis getAxis(MemorySegment mem, int offset) {
      return Axis.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 4)));
   }

   public static BuilderToolRotateClipboard toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static BuilderToolRotateClipboard toObject(MemorySegment mem, int offset) {
      if (offset + 5 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("BuilderToolRotateClipboard", offset + 5, (int)mem.byteSize());
      } else {
         return new BuilderToolRotateClipboard(
            mem.get(PacketIO.PROTO_INT, (long)(offset + 0)), Axis.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 4)))
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.angle);
      buf.writeByte(this.axis.getValue());
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_INT, (long)(offset + 0), this.angle);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 4), (byte)this.axis.getValue());
      return 5;
   }

   @Override
   public int computeSize() {
      return 5;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 5) {
         return ValidationResult.error("Buffer too small: expected at least 5 bytes");
      } else {
         int v = buffer.getByte(offset + 4) & 255;
         return v >= 3 ? ValidationResult.error("Invalid Axis value for Axis") : ValidationResult.OK;
      }
   }

   public BuilderToolRotateClipboard clone() {
      BuilderToolRotateClipboard copy = new BuilderToolRotateClipboard();
      copy.angle = this.angle;
      copy.axis = this.axis;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BuilderToolRotateClipboard other) ? false : this.angle == other.angle && Objects.equals(this.axis, other.axis);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.angle, this.axis);
   }
}
