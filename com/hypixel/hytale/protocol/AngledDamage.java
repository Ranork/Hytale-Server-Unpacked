package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AngledDamage {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 21;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 21;
   public static final int MAX_SIZE = 1677721600;
   public double angle;
   public double angleDistance;
   @Nullable
   public DamageEffects damageEffects;
   public int next;

   public AngledDamage() {
   }

   public AngledDamage(double angle, double angleDistance, @Nullable DamageEffects damageEffects, int next) {
      this.angle = angle;
      this.angleDistance = angleDistance;
      this.damageEffects = damageEffects;
      this.next = next;
   }

   public AngledDamage(@Nonnull AngledDamage other) {
      this.angle = other.angle;
      this.angleDistance = other.angleDistance;
      this.damageEffects = other.damageEffects;
      this.next = other.next;
   }

   @Nonnull
   public static AngledDamage deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 21) {
         throw ProtocolException.bufferTooSmall("AngledDamage", 21, buf.readableBytes() - offset);
      } else {
         AngledDamage obj = new AngledDamage();
         byte nullBits = buf.getByte(offset);
         obj.angle = buf.getDoubleLE(offset + 1);
         obj.angleDistance = buf.getDoubleLE(offset + 9);
         obj.next = buf.getIntLE(offset + 17);
         int pos = offset + 21;
         if ((nullBits & 1) != 0) {
            obj.damageEffects = DamageEffects.deserialize(buf, pos);
            pos += DamageEffects.computeBytesConsumed(buf, pos);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 21;
      if ((nullBits & 1) != 0) {
         pos += DamageEffects.computeBytesConsumed(buf, pos);
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 21L;
   }

   public static double getAngle(MemorySegment mem) {
      return getAngle(mem, 0);
   }

   public static double getAngle(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_DOUBLE, (long)(offset + 1));
   }

   public static double getAngleDistance(MemorySegment mem) {
      return getAngleDistance(mem, 0);
   }

   public static double getAngleDistance(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_DOUBLE, (long)(offset + 9));
   }

   @Nullable
   public static DamageEffects getDamageEffects(MemorySegment mem) {
      return getDamageEffects(mem, 0);
   }

   @Nullable
   public static DamageEffects getDamageEffects(MemorySegment mem, int offset) {
      return hasDamageEffects(mem, offset) ? DamageEffects.toObject(mem, offset + 21) : null;
   }

   public static int getNext(MemorySegment mem) {
      return getNext(mem, 0);
   }

   public static int getNext(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 17));
   }

   public static boolean hasDamageEffects(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static AngledDamage toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static AngledDamage toObject(MemorySegment mem, int offset) {
      if (offset + 21 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("AngledDamage", offset + 21, (int)mem.byteSize());
      } else {
         return new AngledDamage(
            mem.get(PacketIO.PROTO_DOUBLE, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_DOUBLE, (long)(offset + 9)),
            hasDamageEffects(mem, offset) ? DamageEffects.toObject(mem, offset + 21) : null,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 17))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.damageEffects != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeDoubleLE(this.angle);
      buf.writeDoubleLE(this.angleDistance);
      buf.writeIntLE(this.next);
      if (this.damageEffects != null) {
         this.damageEffects.serialize(buf);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.damageEffects != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_DOUBLE, (long)(offset + 1), this.angle);
      mem.set(PacketIO.PROTO_DOUBLE, (long)(offset + 9), this.angleDistance);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 17), this.next);
      int varOffset = offset + 21;
      if (this.damageEffects != null) {
         varOffset += this.damageEffects.serialize(mem, varOffset);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 21;
      if (this.damageEffects != null) {
         size += this.damageEffects.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 21) {
         return ValidationResult.error("Buffer too small: expected at least 21 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 21;
         if ((nullBits & 1) != 0) {
            ValidationResult damageEffectsResult = DamageEffects.validateStructure(buffer, pos);
            if (!damageEffectsResult.isValid()) {
               return ValidationResult.error("Invalid DamageEffects: " + damageEffectsResult.error());
            }

            pos += DamageEffects.computeBytesConsumed(buffer, pos);
         }

         return ValidationResult.OK;
      }
   }

   public AngledDamage clone() {
      AngledDamage copy = new AngledDamage();
      copy.angle = this.angle;
      copy.angleDistance = this.angleDistance;
      copy.damageEffects = this.damageEffects != null ? this.damageEffects.clone() : null;
      copy.next = this.next;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AngledDamage other)
            ? false
            : this.angle == other.angle
               && this.angleDistance == other.angleDistance
               && Objects.equals(this.damageEffects, other.damageEffects)
               && this.next == other.next;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.angle, this.angleDistance, this.damageEffects, this.next);
   }
}
