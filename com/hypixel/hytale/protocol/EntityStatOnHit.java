package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityStatOnHit {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 13;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 13;
   public static final int MAX_SIZE = 16384018;
   public int entityStatIndex;
   public float amount;
   @Nullable
   public float[] multipliersPerEntitiesHit;
   public float multiplierPerExtraEntityHit;

   public EntityStatOnHit() {
   }

   public EntityStatOnHit(int entityStatIndex, float amount, @Nullable float[] multipliersPerEntitiesHit, float multiplierPerExtraEntityHit) {
      this.entityStatIndex = entityStatIndex;
      this.amount = amount;
      this.multipliersPerEntitiesHit = multipliersPerEntitiesHit;
      this.multiplierPerExtraEntityHit = multiplierPerExtraEntityHit;
   }

   public EntityStatOnHit(@Nonnull EntityStatOnHit other) {
      this.entityStatIndex = other.entityStatIndex;
      this.amount = other.amount;
      this.multipliersPerEntitiesHit = other.multipliersPerEntitiesHit;
      this.multiplierPerExtraEntityHit = other.multiplierPerExtraEntityHit;
   }

   @Nonnull
   public static EntityStatOnHit deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 13) {
         throw ProtocolException.bufferTooSmall("EntityStatOnHit", 13, buf.readableBytes() - offset);
      } else {
         EntityStatOnHit obj = new EntityStatOnHit();
         byte nullBits = buf.getByte(offset);
         obj.entityStatIndex = buf.getIntLE(offset + 1);
         obj.amount = buf.getFloatLE(offset + 5);
         obj.multiplierPerExtraEntityHit = buf.getFloatLE(offset + 9);
         int pos = offset + 13;
         if ((nullBits & 1) != 0) {
            int multipliersPerEntitiesHitCount = VarInt.peek(buf, pos);
            if (multipliersPerEntitiesHitCount < 0) {
               throw ProtocolException.invalidVarInt("MultipliersPerEntitiesHit");
            }

            int multipliersPerEntitiesHitVarLen = VarInt.size(multipliersPerEntitiesHitCount);
            if (multipliersPerEntitiesHitCount > 4096000) {
               throw ProtocolException.arrayTooLong("MultipliersPerEntitiesHit", multipliersPerEntitiesHitCount, 4096000);
            }

            if (pos + multipliersPerEntitiesHitVarLen + multipliersPerEntitiesHitCount * 4L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall(
                  "MultipliersPerEntitiesHit", pos + multipliersPerEntitiesHitVarLen + multipliersPerEntitiesHitCount * 4, buf.readableBytes()
               );
            }

            pos += multipliersPerEntitiesHitVarLen;
            obj.multipliersPerEntitiesHit = new float[multipliersPerEntitiesHitCount];

            for (int i = 0; i < multipliersPerEntitiesHitCount; i++) {
               obj.multipliersPerEntitiesHit[i] = buf.getFloatLE(pos + i * 4);
            }

            pos += multipliersPerEntitiesHitCount * 4;
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 13;
      if ((nullBits & 1) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.size(arrLen) + arrLen * 4;
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 13L;
   }

   public static int getEntityStatIndex(MemorySegment mem) {
      return getEntityStatIndex(mem, 0);
   }

   public static int getEntityStatIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   public static float getAmount(MemorySegment mem) {
      return getAmount(mem, 0);
   }

   public static float getAmount(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 5));
   }

   @Nullable
   public static float[] getMultipliersPerEntitiesHit(MemorySegment mem) {
      return getMultipliersPerEntitiesHit(mem, 0);
   }

   @Nullable
   public static float[] getMultipliersPerEntitiesHit(MemorySegment mem, int offset) {
      if (!hasMultipliersPerEntitiesHit(mem, offset)) {
         return null;
      } else {
         int off = offset + 13;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("MultipliersPerEntitiesHit", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("MultipliersPerEntitiesHit", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 4L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("MultipliersPerEntitiesHit", off + lenOffset + len * 4, (int)mem.byteSize());
            } else {
               off += lenOffset;
               float[] data = new float[len];
               MemorySegment.copy(mem, PacketIO.PROTO_FLOAT, off, data, 0, len);
               return data;
            }
         }
      }
   }

   public static float getMultiplierPerExtraEntityHit(MemorySegment mem) {
      return getMultiplierPerExtraEntityHit(mem, 0);
   }

   public static float getMultiplierPerExtraEntityHit(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 9));
   }

   public static boolean hasMultipliersPerEntitiesHit(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static EntityStatOnHit toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static EntityStatOnHit toObject(MemorySegment mem, int offset) {
      if (offset + 13 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("EntityStatOnHit", offset + 13, (int)mem.byteSize());
      } else {
         float[] multipliersPerEntitiesHit = null;
         if (hasMultipliersPerEntitiesHit(mem, offset)) {
            int off = offset + 13;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("MultipliersPerEntitiesHit", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("MultipliersPerEntitiesHit", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 4L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("MultipliersPerEntitiesHit", off + lenOffset + len * 4, (int)mem.byteSize());
            }

            off += lenOffset;
            multipliersPerEntitiesHit = new float[len];
            MemorySegment.copy(mem, PacketIO.PROTO_FLOAT, off, multipliersPerEntitiesHit, 0, len);
         }

         return new EntityStatOnHit(
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 5)),
            multipliersPerEntitiesHit,
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 9))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.multipliersPerEntitiesHit != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.entityStatIndex);
      buf.writeFloatLE(this.amount);
      buf.writeFloatLE(this.multiplierPerExtraEntityHit);
      if (this.multipliersPerEntitiesHit != null) {
         if (this.multipliersPerEntitiesHit.length > 4096000) {
            throw ProtocolException.arrayTooLong("MultipliersPerEntitiesHit", this.multipliersPerEntitiesHit.length, 4096000);
         }

         VarInt.write(buf, this.multipliersPerEntitiesHit.length);

         for (float item : this.multipliersPerEntitiesHit) {
            buf.writeFloatLE(item);
         }
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.multipliersPerEntitiesHit != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.entityStatIndex);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 5), this.amount);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 9), this.multiplierPerExtraEntityHit);
      int varOffset = offset + 13;
      if (this.multipliersPerEntitiesHit != null) {
         if (this.multipliersPerEntitiesHit.length > 4096000) {
            throw ProtocolException.arrayTooLong("MultipliersPerEntitiesHit", this.multipliersPerEntitiesHit.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.multipliersPerEntitiesHit.length);
         MemorySegment.copy(this.multipliersPerEntitiesHit, 0, mem, PacketIO.PROTO_FLOAT, varOffset, this.multipliersPerEntitiesHit.length);
         varOffset += this.multipliersPerEntitiesHit.length * 4;
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 13;
      if (this.multipliersPerEntitiesHit != null) {
         size += VarInt.size(this.multipliersPerEntitiesHit.length) + this.multipliersPerEntitiesHit.length * 4;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 13) {
         return ValidationResult.error("Buffer too small: expected at least 13 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 13;
         if ((nullBits & 1) != 0) {
            int multipliersPerEntitiesHitCount = VarInt.peek(buffer, pos);
            if (multipliersPerEntitiesHitCount < 0) {
               return ValidationResult.error("Invalid array count for MultipliersPerEntitiesHit");
            }

            if (multipliersPerEntitiesHitCount > 4096000) {
               return ValidationResult.error("MultipliersPerEntitiesHit exceeds max length 4096000");
            }

            pos += VarInt.size(multipliersPerEntitiesHitCount);
            pos += multipliersPerEntitiesHitCount * 4;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading MultipliersPerEntitiesHit");
            }
         }

         return ValidationResult.OK;
      }
   }

   public EntityStatOnHit clone() {
      EntityStatOnHit copy = new EntityStatOnHit();
      copy.entityStatIndex = this.entityStatIndex;
      copy.amount = this.amount;
      copy.multipliersPerEntitiesHit = this.multipliersPerEntitiesHit != null
         ? Arrays.copyOf(this.multipliersPerEntitiesHit, this.multipliersPerEntitiesHit.length)
         : null;
      copy.multiplierPerExtraEntityHit = this.multiplierPerExtraEntityHit;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof EntityStatOnHit other)
            ? false
            : this.entityStatIndex == other.entityStatIndex
               && this.amount == other.amount
               && Arrays.equals(this.multipliersPerEntitiesHit, other.multipliersPerEntitiesHit)
               && this.multiplierPerExtraEntityHit == other.multiplierPerExtraEntityHit;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Integer.hashCode(this.entityStatIndex);
      result = 31 * result + Float.hashCode(this.amount);
      result = 31 * result + Arrays.hashCode(this.multipliersPerEntitiesHit);
      return 31 * result + Float.hashCode(this.multiplierPerExtraEntityHit);
   }
}
