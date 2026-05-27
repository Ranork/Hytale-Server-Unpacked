package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TargetedDamage {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 9;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 1677721600;
   public int index;
   @Nullable
   public DamageEffects damageEffects;
   public int next;

   public TargetedDamage() {
   }

   public TargetedDamage(int index, @Nullable DamageEffects damageEffects, int next) {
      this.index = index;
      this.damageEffects = damageEffects;
      this.next = next;
   }

   public TargetedDamage(@Nonnull TargetedDamage other) {
      this.index = other.index;
      this.damageEffects = other.damageEffects;
      this.next = other.next;
   }

   @Nonnull
   public static TargetedDamage deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 9) {
         throw ProtocolException.bufferTooSmall("TargetedDamage", 9, buf.readableBytes() - offset);
      } else {
         TargetedDamage obj = new TargetedDamage();
         byte nullBits = buf.getByte(offset);
         obj.index = buf.getIntLE(offset + 1);
         obj.next = buf.getIntLE(offset + 5);
         int pos = offset + 9;
         if ((nullBits & 1) != 0) {
            obj.damageEffects = DamageEffects.deserialize(buf, pos);
            pos += DamageEffects.computeBytesConsumed(buf, pos);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 9;
      if ((nullBits & 1) != 0) {
         pos += DamageEffects.computeBytesConsumed(buf, pos);
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 9L;
   }

   public static int getIndex(MemorySegment mem) {
      return getIndex(mem, 0);
   }

   public static int getIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   @Nullable
   public static DamageEffects getDamageEffects(MemorySegment mem) {
      return getDamageEffects(mem, 0);
   }

   @Nullable
   public static DamageEffects getDamageEffects(MemorySegment mem, int offset) {
      return hasDamageEffects(mem, offset) ? DamageEffects.toObject(mem, offset + 9) : null;
   }

   public static int getNext(MemorySegment mem) {
      return getNext(mem, 0);
   }

   public static int getNext(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 5));
   }

   public static boolean hasDamageEffects(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static TargetedDamage toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static TargetedDamage toObject(MemorySegment mem, int offset) {
      if (offset + 9 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("TargetedDamage", offset + 9, (int)mem.byteSize());
      } else {
         return new TargetedDamage(
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1)),
            hasDamageEffects(mem, offset) ? DamageEffects.toObject(mem, offset + 9) : null,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 5))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.damageEffects != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.index);
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
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.index);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 5), this.next);
      int varOffset = offset + 9;
      if (this.damageEffects != null) {
         varOffset += this.damageEffects.serialize(mem, varOffset);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 9;
      if (this.damageEffects != null) {
         size += this.damageEffects.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 9) {
         return ValidationResult.error("Buffer too small: expected at least 9 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 9;
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

   public TargetedDamage clone() {
      TargetedDamage copy = new TargetedDamage();
      copy.index = this.index;
      copy.damageEffects = this.damageEffects != null ? this.damageEffects.clone() : null;
      copy.next = this.next;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof TargetedDamage other)
            ? false
            : this.index == other.index && Objects.equals(this.damageEffects, other.damageEffects) && this.next == other.next;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.index, this.damageEffects, this.next);
   }
}
