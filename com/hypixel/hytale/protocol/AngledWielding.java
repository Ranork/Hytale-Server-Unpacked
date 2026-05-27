package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class AngledWielding {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 9;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 9;
   public float angleRad;
   public float angleDistanceRad;
   public boolean hasModifiers;

   public AngledWielding() {
   }

   public AngledWielding(float angleRad, float angleDistanceRad, boolean hasModifiers) {
      this.angleRad = angleRad;
      this.angleDistanceRad = angleDistanceRad;
      this.hasModifiers = hasModifiers;
   }

   public AngledWielding(@Nonnull AngledWielding other) {
      this.angleRad = other.angleRad;
      this.angleDistanceRad = other.angleDistanceRad;
      this.hasModifiers = other.hasModifiers;
   }

   @Nonnull
   public static AngledWielding deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 9) {
         throw ProtocolException.bufferTooSmall("AngledWielding", 9, buf.readableBytes() - offset);
      } else {
         AngledWielding obj = new AngledWielding();
         obj.angleRad = buf.getFloatLE(offset + 0);
         obj.angleDistanceRad = buf.getFloatLE(offset + 4);
         obj.hasModifiers = buf.getByte(offset + 8) != 0;
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 9;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 9L;
   }

   public static float getAngleRad(MemorySegment mem) {
      return getAngleRad(mem, 0);
   }

   public static float getAngleRad(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 0));
   }

   public static float getAngleDistanceRad(MemorySegment mem) {
      return getAngleDistanceRad(mem, 0);
   }

   public static float getAngleDistanceRad(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 4));
   }

   public static boolean getHasModifiers(MemorySegment mem) {
      return getHasModifiers(mem, 0);
   }

   public static boolean getHasModifiers(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 8));
   }

   public static AngledWielding toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static AngledWielding toObject(MemorySegment mem, int offset) {
      if (offset + 9 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("AngledWielding", offset + 9, (int)mem.byteSize());
      } else {
         return new AngledWielding(
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 0)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 4)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 8))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeFloatLE(this.angleRad);
      buf.writeFloatLE(this.angleDistanceRad);
      buf.writeByte(this.hasModifiers ? 1 : 0);
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 0), this.angleRad);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 4), this.angleDistanceRad);
      mem.set(PacketIO.PROTO_BOOL, offset + 8, this.hasModifiers);
      return 9;
   }

   public int computeSize() {
      return 9;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 9 ? ValidationResult.error("Buffer too small: expected at least 9 bytes") : ValidationResult.OK;
   }

   public AngledWielding clone() {
      AngledWielding copy = new AngledWielding();
      copy.angleRad = this.angleRad;
      copy.angleDistanceRad = this.angleDistanceRad;
      copy.hasModifiers = this.hasModifiers;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AngledWielding other)
            ? false
            : this.angleRad == other.angleRad && this.angleDistanceRad == other.angleDistanceRad && this.hasModifiers == other.hasModifiers;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.angleRad, this.angleDistanceRad, this.hasModifiers);
   }
}
