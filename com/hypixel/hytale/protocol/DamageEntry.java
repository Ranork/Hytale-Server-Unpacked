package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DamageEntry {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 9;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 16384014;
   @Nullable
   public String labelKey;
   public float min;
   public float max;

   public DamageEntry() {
   }

   public DamageEntry(@Nullable String labelKey, float min, float max) {
      this.labelKey = labelKey;
      this.min = min;
      this.max = max;
   }

   public DamageEntry(@Nonnull DamageEntry other) {
      this.labelKey = other.labelKey;
      this.min = other.min;
      this.max = other.max;
   }

   @Nonnull
   public static DamageEntry deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 9) {
         throw ProtocolException.bufferTooSmall("DamageEntry", 9, buf.readableBytes() - offset);
      } else {
         DamageEntry obj = new DamageEntry();
         byte nullBits = buf.getByte(offset);
         obj.min = buf.getFloatLE(offset + 1);
         obj.max = buf.getFloatLE(offset + 5);
         int pos = offset + 9;
         if ((nullBits & 1) != 0) {
            int labelKeyLen = VarInt.peek(buf, pos);
            if (labelKeyLen < 0) {
               throw ProtocolException.invalidVarInt("LabelKey");
            }

            int labelKeyVarLen = VarInt.size(labelKeyLen);
            if (labelKeyLen > 4096000) {
               throw ProtocolException.stringTooLong("LabelKey", labelKeyLen, 4096000);
            }

            if (pos + labelKeyVarLen + labelKeyLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("LabelKey", pos + labelKeyVarLen + labelKeyLen, buf.readableBytes());
            }

            obj.labelKey = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
            pos += labelKeyVarLen + labelKeyLen;
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 9;
      if ((nullBits & 1) != 0) {
         int sl = VarInt.peek(buf, pos);
         pos += VarInt.size(sl) + sl;
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 9L;
   }

   @Nullable
   public static String getLabelKey(MemorySegment mem) {
      return getLabelKey(mem, 0);
   }

   @Nullable
   public static String getLabelKey(MemorySegment mem, int offset) {
      return hasLabelKey(mem, offset) ? PacketIO.readVarString("LabelKey", mem, offset + 9, 4096000, PacketIO.UTF8) : null;
   }

   public static float getMin(MemorySegment mem) {
      return getMin(mem, 0);
   }

   public static float getMin(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1));
   }

   public static float getMax(MemorySegment mem) {
      return getMax(mem, 0);
   }

   public static float getMax(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 5));
   }

   public static boolean hasLabelKey(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static DamageEntry toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static DamageEntry toObject(MemorySegment mem, int offset) {
      if (offset + 9 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("DamageEntry", offset + 9, (int)mem.byteSize());
      } else {
         return new DamageEntry(
            hasLabelKey(mem, offset) ? PacketIO.readVarString("LabelKey", mem, offset + 9, 4096000, PacketIO.UTF8) : null,
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 5))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.labelKey != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeFloatLE(this.min);
      buf.writeFloatLE(this.max);
      if (this.labelKey != null) {
         PacketIO.writeVarString(buf, this.labelKey, 4096000);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.labelKey != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 1), this.min);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 5), this.max);
      int varOffset = offset + 9;
      if (this.labelKey != null) {
         varOffset += PacketIO.writeVarString(mem, varOffset, this.labelKey, 4096000);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 9;
      if (this.labelKey != null) {
         size += PacketIO.stringSize(this.labelKey);
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
            int labelKeyLen = VarInt.peek(buffer, pos);
            if (labelKeyLen < 0) {
               return ValidationResult.error("Invalid string length for LabelKey");
            }

            if (labelKeyLen > 4096000) {
               return ValidationResult.error("LabelKey exceeds max length 4096000");
            }

            pos += VarInt.size(labelKeyLen);
            pos += labelKeyLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading LabelKey");
            }
         }

         return ValidationResult.OK;
      }
   }

   public DamageEntry clone() {
      DamageEntry copy = new DamageEntry();
      copy.labelKey = this.labelKey;
      copy.min = this.min;
      copy.max = this.max;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof DamageEntry other) ? false : Objects.equals(this.labelKey, other.labelKey) && this.min == other.min && this.max == other.max;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.labelKey, this.min, this.max);
   }
}
