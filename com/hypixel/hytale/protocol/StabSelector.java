package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class StabSelector extends Selector {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 37;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 37;
   public static final int MAX_SIZE = 37;
   public float extendTop;
   public float extendBottom;
   public float extendLeft;
   public float extendRight;
   public float yawOffset;
   public float pitchOffset;
   public float rollOffset;
   public float startDistance;
   public float endDistance;
   public boolean testLineOfSight;

   public StabSelector() {
   }

   public StabSelector(
      float extendTop,
      float extendBottom,
      float extendLeft,
      float extendRight,
      float yawOffset,
      float pitchOffset,
      float rollOffset,
      float startDistance,
      float endDistance,
      boolean testLineOfSight
   ) {
      this.extendTop = extendTop;
      this.extendBottom = extendBottom;
      this.extendLeft = extendLeft;
      this.extendRight = extendRight;
      this.yawOffset = yawOffset;
      this.pitchOffset = pitchOffset;
      this.rollOffset = rollOffset;
      this.startDistance = startDistance;
      this.endDistance = endDistance;
      this.testLineOfSight = testLineOfSight;
   }

   public StabSelector(@Nonnull StabSelector other) {
      this.extendTop = other.extendTop;
      this.extendBottom = other.extendBottom;
      this.extendLeft = other.extendLeft;
      this.extendRight = other.extendRight;
      this.yawOffset = other.yawOffset;
      this.pitchOffset = other.pitchOffset;
      this.rollOffset = other.rollOffset;
      this.startDistance = other.startDistance;
      this.endDistance = other.endDistance;
      this.testLineOfSight = other.testLineOfSight;
   }

   @Nonnull
   public static StabSelector deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 37) {
         throw ProtocolException.bufferTooSmall("StabSelector", 37, buf.readableBytes() - offset);
      } else {
         StabSelector obj = new StabSelector();
         obj.extendTop = buf.getFloatLE(offset + 0);
         obj.extendBottom = buf.getFloatLE(offset + 4);
         obj.extendLeft = buf.getFloatLE(offset + 8);
         obj.extendRight = buf.getFloatLE(offset + 12);
         obj.yawOffset = buf.getFloatLE(offset + 16);
         obj.pitchOffset = buf.getFloatLE(offset + 20);
         obj.rollOffset = buf.getFloatLE(offset + 24);
         obj.startDistance = buf.getFloatLE(offset + 28);
         obj.endDistance = buf.getFloatLE(offset + 32);
         obj.testLineOfSight = buf.getByte(offset + 36) != 0;
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 37;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 37L;
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

   public static float getExtendLeft(MemorySegment mem) {
      return getExtendLeft(mem, 0);
   }

   public static float getExtendLeft(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 8));
   }

   public static float getExtendRight(MemorySegment mem) {
      return getExtendRight(mem, 0);
   }

   public static float getExtendRight(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 12));
   }

   public static float getYawOffset(MemorySegment mem) {
      return getYawOffset(mem, 0);
   }

   public static float getYawOffset(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 16));
   }

   public static float getPitchOffset(MemorySegment mem) {
      return getPitchOffset(mem, 0);
   }

   public static float getPitchOffset(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 20));
   }

   public static float getRollOffset(MemorySegment mem) {
      return getRollOffset(mem, 0);
   }

   public static float getRollOffset(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 24));
   }

   public static float getStartDistance(MemorySegment mem) {
      return getStartDistance(mem, 0);
   }

   public static float getStartDistance(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 28));
   }

   public static float getEndDistance(MemorySegment mem) {
      return getEndDistance(mem, 0);
   }

   public static float getEndDistance(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 32));
   }

   public static boolean getTestLineOfSight(MemorySegment mem) {
      return getTestLineOfSight(mem, 0);
   }

   public static boolean getTestLineOfSight(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 36));
   }

   public static StabSelector toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static StabSelector toObject(MemorySegment mem, int offset) {
      if (offset + 37 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("StabSelector", offset + 37, (int)mem.byteSize());
      } else {
         return new StabSelector(
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 0)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 4)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 8)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 12)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 16)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 20)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 24)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 28)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 32)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 36))
         );
      }
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      buf.writeFloatLE(this.extendTop);
      buf.writeFloatLE(this.extendBottom);
      buf.writeFloatLE(this.extendLeft);
      buf.writeFloatLE(this.extendRight);
      buf.writeFloatLE(this.yawOffset);
      buf.writeFloatLE(this.pitchOffset);
      buf.writeFloatLE(this.rollOffset);
      buf.writeFloatLE(this.startDistance);
      buf.writeFloatLE(this.endDistance);
      buf.writeByte(this.testLineOfSight ? 1 : 0);
      return buf.writerIndex() - startPos;
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 0), this.extendTop);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 4), this.extendBottom);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 8), this.extendLeft);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 12), this.extendRight);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 16), this.yawOffset);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 20), this.pitchOffset);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 24), this.rollOffset);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 28), this.startDistance);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 32), this.endDistance);
      mem.set(PacketIO.PROTO_BOOL, offset + 36, this.testLineOfSight);
      return 37;
   }

   @Override
   public int computeSize() {
      return 37;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 37 ? ValidationResult.error("Buffer too small: expected at least 37 bytes") : ValidationResult.OK;
   }

   public StabSelector clone() {
      StabSelector copy = new StabSelector();
      copy.extendTop = this.extendTop;
      copy.extendBottom = this.extendBottom;
      copy.extendLeft = this.extendLeft;
      copy.extendRight = this.extendRight;
      copy.yawOffset = this.yawOffset;
      copy.pitchOffset = this.pitchOffset;
      copy.rollOffset = this.rollOffset;
      copy.startDistance = this.startDistance;
      copy.endDistance = this.endDistance;
      copy.testLineOfSight = this.testLineOfSight;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof StabSelector other)
            ? false
            : this.extendTop == other.extendTop
               && this.extendBottom == other.extendBottom
               && this.extendLeft == other.extendLeft
               && this.extendRight == other.extendRight
               && this.yawOffset == other.yawOffset
               && this.pitchOffset == other.pitchOffset
               && this.rollOffset == other.rollOffset
               && this.startDistance == other.startDistance
               && this.endDistance == other.endDistance
               && this.testLineOfSight == other.testLineOfSight;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(
         this.extendTop,
         this.extendBottom,
         this.extendLeft,
         this.extendRight,
         this.yawOffset,
         this.pitchOffset,
         this.rollOffset,
         this.startDistance,
         this.endDistance,
         this.testLineOfSight
      );
   }
}
