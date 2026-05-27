package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StateTransition {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 15;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 15;
   public static final int MAX_SIZE = 15;
   public int fromValueIndex;
   public int toValueIndex;
   public float durationMs;
   @Nonnull
   public FadeCurve curve = FadeCurve.Linear;
   @Nullable
   public SyncPoint syncTo;

   public StateTransition() {
   }

   public StateTransition(int fromValueIndex, int toValueIndex, float durationMs, @Nonnull FadeCurve curve, @Nullable SyncPoint syncTo) {
      this.fromValueIndex = fromValueIndex;
      this.toValueIndex = toValueIndex;
      this.durationMs = durationMs;
      this.curve = curve;
      this.syncTo = syncTo;
   }

   public StateTransition(@Nonnull StateTransition other) {
      this.fromValueIndex = other.fromValueIndex;
      this.toValueIndex = other.toValueIndex;
      this.durationMs = other.durationMs;
      this.curve = other.curve;
      this.syncTo = other.syncTo;
   }

   @Nonnull
   public static StateTransition deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 15) {
         throw ProtocolException.bufferTooSmall("StateTransition", 15, buf.readableBytes() - offset);
      } else {
         StateTransition obj = new StateTransition();
         byte nullBits = buf.getByte(offset);
         obj.fromValueIndex = buf.getIntLE(offset + 1);
         obj.toValueIndex = buf.getIntLE(offset + 5);
         obj.durationMs = buf.getFloatLE(offset + 9);
         obj.curve = FadeCurve.fromValue(buf.getByte(offset + 13));
         if ((nullBits & 1) != 0) {
            obj.syncTo = SyncPoint.fromValue(buf.getByte(offset + 14));
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 15;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 15L;
   }

   public static int getFromValueIndex(MemorySegment mem) {
      return getFromValueIndex(mem, 0);
   }

   public static int getFromValueIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   public static int getToValueIndex(MemorySegment mem) {
      return getToValueIndex(mem, 0);
   }

   public static int getToValueIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 5));
   }

   public static float getDurationMs(MemorySegment mem) {
      return getDurationMs(mem, 0);
   }

   public static float getDurationMs(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 9));
   }

   public static FadeCurve getCurve(MemorySegment mem) {
      return getCurve(mem, 0);
   }

   public static FadeCurve getCurve(MemorySegment mem, int offset) {
      return FadeCurve.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 13)));
   }

   @Nullable
   public static SyncPoint getSyncTo(MemorySegment mem) {
      return getSyncTo(mem, 0);
   }

   @Nullable
   public static SyncPoint getSyncTo(MemorySegment mem, int offset) {
      return hasSyncTo(mem, offset) ? SyncPoint.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 14))) : null;
   }

   public static boolean hasSyncTo(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static StateTransition toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static StateTransition toObject(MemorySegment mem, int offset) {
      if (offset + 15 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("StateTransition", offset + 15, (int)mem.byteSize());
      } else {
         return new StateTransition(
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 5)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 9)),
            FadeCurve.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 13))),
            hasSyncTo(mem, offset) ? SyncPoint.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 14))) : null
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.syncTo != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.fromValueIndex);
      buf.writeIntLE(this.toValueIndex);
      buf.writeFloatLE(this.durationMs);
      buf.writeByte(this.curve.getValue());
      if (this.syncTo != null) {
         buf.writeByte(this.syncTo.getValue());
      } else {
         buf.writeZero(1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.syncTo != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.fromValueIndex);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 5), this.toValueIndex);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 9), this.durationMs);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 13), (byte)this.curve.getValue());
      if (this.syncTo != null) {
         mem.set(PacketIO.PROTO_BYTE, (long)(offset + 14), (byte)this.syncTo.getValue());
      } else {
         mem.asSlice(offset + 14, 1L).fill((byte)0);
      }

      return 15;
   }

   public int computeSize() {
      return 15;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 15) {
         return ValidationResult.error("Buffer too small: expected at least 15 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int v = buffer.getByte(offset + 13) & 255;
         if (v >= 5) {
            return ValidationResult.error("Invalid FadeCurve value for Curve");
         } else {
            if ((nullBits & 1) != 0) {
               v = buffer.getByte(offset + 14) & 255;
               if (v >= 4) {
                  return ValidationResult.error("Invalid SyncPoint value for SyncTo");
               }
            }

            return ValidationResult.OK;
         }
      }
   }

   public StateTransition clone() {
      StateTransition copy = new StateTransition();
      copy.fromValueIndex = this.fromValueIndex;
      copy.toValueIndex = this.toValueIndex;
      copy.durationMs = this.durationMs;
      copy.curve = this.curve;
      copy.syncTo = this.syncTo;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof StateTransition other)
            ? false
            : this.fromValueIndex == other.fromValueIndex
               && this.toValueIndex == other.toValueIndex
               && this.durationMs == other.durationMs
               && Objects.equals(this.curve, other.curve)
               && Objects.equals(this.syncTo, other.syncTo);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.fromValueIndex, this.toValueIndex, this.durationMs, this.curve, this.syncTo);
   }
}
