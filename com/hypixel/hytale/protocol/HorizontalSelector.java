package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class HorizontalSelector extends Selector {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 34;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 34;
   public static final int MAX_SIZE = 34;
   public float extendTop;
   public float extendBottom;
   public float yawLength;
   public float yawStartOffset;
   public float pitchOffset;
   public float rollOffset;
   public float startDistance;
   public float endDistance;
   @Nonnull
   public HorizontalSelectorDirection direction = HorizontalSelectorDirection.ToLeft;
   public boolean testLineOfSight;

   public HorizontalSelector() {
   }

   public HorizontalSelector(
      float extendTop,
      float extendBottom,
      float yawLength,
      float yawStartOffset,
      float pitchOffset,
      float rollOffset,
      float startDistance,
      float endDistance,
      @Nonnull HorizontalSelectorDirection direction,
      boolean testLineOfSight
   ) {
      this.extendTop = extendTop;
      this.extendBottom = extendBottom;
      this.yawLength = yawLength;
      this.yawStartOffset = yawStartOffset;
      this.pitchOffset = pitchOffset;
      this.rollOffset = rollOffset;
      this.startDistance = startDistance;
      this.endDistance = endDistance;
      this.direction = direction;
      this.testLineOfSight = testLineOfSight;
   }

   public HorizontalSelector(@Nonnull HorizontalSelector other) {
      this.extendTop = other.extendTop;
      this.extendBottom = other.extendBottom;
      this.yawLength = other.yawLength;
      this.yawStartOffset = other.yawStartOffset;
      this.pitchOffset = other.pitchOffset;
      this.rollOffset = other.rollOffset;
      this.startDistance = other.startDistance;
      this.endDistance = other.endDistance;
      this.direction = other.direction;
      this.testLineOfSight = other.testLineOfSight;
   }

   @Nonnull
   public static HorizontalSelector deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 34) {
         throw ProtocolException.bufferTooSmall("HorizontalSelector", 34, buf.readableBytes() - offset);
      } else {
         HorizontalSelector obj = new HorizontalSelector();
         obj.extendTop = buf.getFloatLE(offset + 0);
         obj.extendBottom = buf.getFloatLE(offset + 4);
         obj.yawLength = buf.getFloatLE(offset + 8);
         obj.yawStartOffset = buf.getFloatLE(offset + 12);
         obj.pitchOffset = buf.getFloatLE(offset + 16);
         obj.rollOffset = buf.getFloatLE(offset + 20);
         obj.startDistance = buf.getFloatLE(offset + 24);
         obj.endDistance = buf.getFloatLE(offset + 28);
         obj.direction = HorizontalSelectorDirection.fromValue(buf.getByte(offset + 32));
         obj.testLineOfSight = buf.getByte(offset + 33) != 0;
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 34;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 34L;
   }

   public static float getExtendTop(MemorySegment mem) {
      return getExtendTop(mem, 0);
   }

   public static float getExtendTop(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 0));
   }

   public static float getExtendBottom(MemorySegment mem) {
      return getExtendBottom(mem, 0);
   }

   public static float getExtendBottom(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 4));
   }

   public static float getYawLength(MemorySegment mem) {
      return getYawLength(mem, 0);
   }

   public static float getYawLength(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 8));
   }

   public static float getYawStartOffset(MemorySegment mem) {
      return getYawStartOffset(mem, 0);
   }

   public static float getYawStartOffset(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 12));
   }

   public static float getPitchOffset(MemorySegment mem) {
      return getPitchOffset(mem, 0);
   }

   public static float getPitchOffset(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 16));
   }

   public static float getRollOffset(MemorySegment mem) {
      return getRollOffset(mem, 0);
   }

   public static float getRollOffset(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 20));
   }

   public static float getStartDistance(MemorySegment mem) {
      return getStartDistance(mem, 0);
   }

   public static float getStartDistance(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 24));
   }

   public static float getEndDistance(MemorySegment mem) {
      return getEndDistance(mem, 0);
   }

   public static float getEndDistance(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 28));
   }

   public static HorizontalSelectorDirection getDirection(MemorySegment mem) {
      return getDirection(mem, 0);
   }

   public static HorizontalSelectorDirection getDirection(MemorySegment mem, int offset) {
      return HorizontalSelectorDirection.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 32)));
   }

   public static boolean getTestLineOfSight(MemorySegment mem) {
      return getTestLineOfSight(mem, 0);
   }

   public static boolean getTestLineOfSight(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 33));
   }

   public static HorizontalSelector toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static HorizontalSelector toObject(MemorySegment mem, int offset) {
      if (offset + 34 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("HorizontalSelector", offset + 34, (int)mem.byteSize());
      } else {
         return new HorizontalSelector(
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 0)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 4)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 8)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 12)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 16)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 20)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 24)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 28)),
            HorizontalSelectorDirection.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 32))),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 33))
         );
      }
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      buf.writeFloatLE(this.extendTop);
      buf.writeFloatLE(this.extendBottom);
      buf.writeFloatLE(this.yawLength);
      buf.writeFloatLE(this.yawStartOffset);
      buf.writeFloatLE(this.pitchOffset);
      buf.writeFloatLE(this.rollOffset);
      buf.writeFloatLE(this.startDistance);
      buf.writeFloatLE(this.endDistance);
      buf.writeByte(this.direction.getValue());
      buf.writeByte(this.testLineOfSight ? 1 : 0);
      return buf.writerIndex() - startPos;
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 0), this.extendTop);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 4), this.extendBottom);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 8), this.yawLength);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 12), this.yawStartOffset);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 16), this.pitchOffset);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 20), this.rollOffset);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 24), this.startDistance);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 28), this.endDistance);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 32), (byte)this.direction.getValue());
      mem.set(PacketIO.PROTO_BOOL, offset + 33, this.testLineOfSight);
      return 34;
   }

   @Override
   public int computeSize() {
      return 34;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 34) {
         return ValidationResult.error("Buffer too small: expected at least 34 bytes");
      } else {
         int v = buffer.getByte(offset + 32) & 255;
         return v >= 2 ? ValidationResult.error("Invalid HorizontalSelectorDirection value for Direction") : ValidationResult.OK;
      }
   }

   public HorizontalSelector clone() {
      HorizontalSelector copy = new HorizontalSelector();
      copy.extendTop = this.extendTop;
      copy.extendBottom = this.extendBottom;
      copy.yawLength = this.yawLength;
      copy.yawStartOffset = this.yawStartOffset;
      copy.pitchOffset = this.pitchOffset;
      copy.rollOffset = this.rollOffset;
      copy.startDistance = this.startDistance;
      copy.endDistance = this.endDistance;
      copy.direction = this.direction;
      copy.testLineOfSight = this.testLineOfSight;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof HorizontalSelector other)
            ? false
            : this.extendTop == other.extendTop
               && this.extendBottom == other.extendBottom
               && this.yawLength == other.yawLength
               && this.yawStartOffset == other.yawStartOffset
               && this.pitchOffset == other.pitchOffset
               && this.rollOffset == other.rollOffset
               && this.startDistance == other.startDistance
               && this.endDistance == other.endDistance
               && Objects.equals(this.direction, other.direction)
               && this.testLineOfSight == other.testLineOfSight;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(
         this.extendTop,
         this.extendBottom,
         this.yawLength,
         this.yawStartOffset,
         this.pitchOffset,
         this.rollOffset,
         this.startDistance,
         this.endDistance,
         this.direction,
         this.testLineOfSight
      );
   }
}
