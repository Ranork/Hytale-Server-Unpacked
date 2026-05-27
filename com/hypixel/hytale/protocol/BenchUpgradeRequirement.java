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

public class BenchUpgradeRequirement {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 9;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public MaterialQuantity[] material;
   public double timeSeconds;

   public BenchUpgradeRequirement() {
   }

   public BenchUpgradeRequirement(@Nullable MaterialQuantity[] material, double timeSeconds) {
      this.material = material;
      this.timeSeconds = timeSeconds;
   }

   public BenchUpgradeRequirement(@Nonnull BenchUpgradeRequirement other) {
      this.material = other.material;
      this.timeSeconds = other.timeSeconds;
   }

   @Nonnull
   public static BenchUpgradeRequirement deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 9) {
         throw ProtocolException.bufferTooSmall("BenchUpgradeRequirement", 9, buf.readableBytes() - offset);
      } else {
         BenchUpgradeRequirement obj = new BenchUpgradeRequirement();
         byte nullBits = buf.getByte(offset);
         obj.timeSeconds = buf.getDoubleLE(offset + 1);
         int pos = offset + 9;
         if ((nullBits & 1) != 0) {
            int materialCount = VarInt.peek(buf, pos);
            if (materialCount < 0) {
               throw ProtocolException.invalidVarInt("Material");
            }

            int materialVarLen = VarInt.size(materialCount);
            if (materialCount > 4096000) {
               throw ProtocolException.arrayTooLong("Material", materialCount, 4096000);
            }

            if (pos + materialVarLen + materialCount * 9L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Material", pos + materialVarLen + materialCount * 9, buf.readableBytes());
            }

            pos += materialVarLen;
            obj.material = new MaterialQuantity[materialCount];

            for (int i = 0; i < materialCount; i++) {
               obj.material[i] = MaterialQuantity.deserialize(buf, pos);
               pos += MaterialQuantity.computeBytesConsumed(buf, pos);
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 9;
      if ((nullBits & 1) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos += MaterialQuantity.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 9L;
   }

   @Nullable
   public static MaterialQuantity[] getMaterial(MemorySegment mem) {
      return getMaterial(mem, 0);
   }

   @Nullable
   public static MaterialQuantity[] getMaterial(MemorySegment mem, int offset) {
      if (!hasMaterial(mem, offset)) {
         return null;
      } else {
         int off = offset + 9;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Material", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Material", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Material", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               MaterialQuantity[] data = new MaterialQuantity[len];

               for (int i = 0; i < len; i++) {
                  data[i] = MaterialQuantity.toObject(mem, off);
                  off += data[i].computeSize();
               }

               return data;
            }
         }
      }
   }

   public static double getTimeSeconds(MemorySegment mem) {
      return getTimeSeconds(mem, 0);
   }

   public static double getTimeSeconds(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_DOUBLE, (long)(offset + 1));
   }

   public static boolean hasMaterial(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static BenchUpgradeRequirement toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static BenchUpgradeRequirement toObject(MemorySegment mem, int offset) {
      if (offset + 9 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("BenchUpgradeRequirement", offset + 9, (int)mem.byteSize());
      } else {
         MaterialQuantity[] material = null;
         if (hasMaterial(mem, offset)) {
            int off = offset + 9;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Material", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Material", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Material", off + lenOffset + len, (int)mem.byteSize());
            }

            off += lenOffset;
            material = new MaterialQuantity[len];

            for (int i = 0; i < len; i++) {
               material[i] = MaterialQuantity.toObject(mem, off);
               off += material[i].computeSize();
            }
         }

         return new BenchUpgradeRequirement(material, mem.get(PacketIO.PROTO_DOUBLE, (long)(offset + 1)));
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.material != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeDoubleLE(this.timeSeconds);
      if (this.material != null) {
         if (this.material.length > 4096000) {
            throw ProtocolException.arrayTooLong("Material", this.material.length, 4096000);
         }

         VarInt.write(buf, this.material.length);

         for (MaterialQuantity item : this.material) {
            item.serialize(buf);
         }
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.material != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_DOUBLE, (long)(offset + 1), this.timeSeconds);
      int varOffset = offset + 9;
      if (this.material != null) {
         if (this.material.length > 4096000) {
            throw ProtocolException.arrayTooLong("Material", this.material.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.material.length);
         int materialValueOffset = 0;

         for (int i = 0; i < this.material.length; i++) {
            materialValueOffset += this.material[i].serialize(mem, varOffset + materialValueOffset);
         }

         varOffset += materialValueOffset;
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 9;
      if (this.material != null) {
         int materialSize = 0;

         for (MaterialQuantity elem : this.material) {
            materialSize += elem.computeSize();
         }

         size += VarInt.size(this.material.length) + materialSize;
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
            int materialCount = VarInt.peek(buffer, pos);
            if (materialCount < 0) {
               return ValidationResult.error("Invalid array count for Material");
            }

            if (materialCount > 4096000) {
               return ValidationResult.error("Material exceeds max length 4096000");
            }

            pos += VarInt.size(materialCount);

            for (int i = 0; i < materialCount; i++) {
               ValidationResult structResult = MaterialQuantity.validateStructure(buffer, pos);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid MaterialQuantity in Material[" + i + "]: " + structResult.error());
               }

               pos += MaterialQuantity.computeBytesConsumed(buffer, pos);
            }
         }

         return ValidationResult.OK;
      }
   }

   public BenchUpgradeRequirement clone() {
      BenchUpgradeRequirement copy = new BenchUpgradeRequirement();
      copy.material = this.material != null ? Arrays.stream(this.material).map(e -> e.clone()).toArray(MaterialQuantity[]::new) : null;
      copy.timeSeconds = this.timeSeconds;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BenchUpgradeRequirement other)
            ? false
            : Arrays.equals((Object[])this.material, (Object[])other.material) && this.timeSeconds == other.timeSeconds;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Arrays.hashCode((Object[])this.material);
      return 31 * result + Double.hashCode(this.timeSeconds);
   }
}
