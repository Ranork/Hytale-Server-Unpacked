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

public class ItemTool {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 5;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 5;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public ItemToolSpec[] specs;
   public float speed;

   public ItemTool() {
   }

   public ItemTool(@Nullable ItemToolSpec[] specs, float speed) {
      this.specs = specs;
      this.speed = speed;
   }

   public ItemTool(@Nonnull ItemTool other) {
      this.specs = other.specs;
      this.speed = other.speed;
   }

   @Nonnull
   public static ItemTool deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 5) {
         throw ProtocolException.bufferTooSmall("ItemTool", 5, buf.readableBytes() - offset);
      } else {
         ItemTool obj = new ItemTool();
         byte nullBits = buf.getByte(offset);
         obj.speed = buf.getFloatLE(offset + 1);
         int pos = offset + 5;
         if ((nullBits & 1) != 0) {
            int specsCount = VarInt.peek(buf, pos);
            if (specsCount < 0) {
               throw ProtocolException.invalidVarInt("Specs");
            }

            int specsVarLen = VarInt.size(specsCount);
            if (specsCount > 4096000) {
               throw ProtocolException.arrayTooLong("Specs", specsCount, 4096000);
            }

            if (pos + specsVarLen + specsCount * 9L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Specs", pos + specsVarLen + specsCount * 9, buf.readableBytes());
            }

            pos += specsVarLen;
            obj.specs = new ItemToolSpec[specsCount];

            for (int i = 0; i < specsCount; i++) {
               obj.specs[i] = ItemToolSpec.deserialize(buf, pos);
               pos += ItemToolSpec.computeBytesConsumed(buf, pos);
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 5;
      if ((nullBits & 1) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos += ItemToolSpec.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 5L;
   }

   @Nullable
   public static ItemToolSpec[] getSpecs(MemorySegment mem) {
      return getSpecs(mem, 0);
   }

   @Nullable
   public static ItemToolSpec[] getSpecs(MemorySegment mem, int offset) {
      if (!hasSpecs(mem, offset)) {
         return null;
      } else {
         int off = offset + 5;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Specs", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Specs", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Specs", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               ItemToolSpec[] data = new ItemToolSpec[len];

               for (int i = 0; i < len; i++) {
                  data[i] = ItemToolSpec.toObject(mem, off);
                  off += data[i].computeSize();
               }

               return data;
            }
         }
      }
   }

   public static float getSpeed(MemorySegment mem) {
      return getSpeed(mem, 0);
   }

   public static float getSpeed(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1));
   }

   public static boolean hasSpecs(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static ItemTool toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ItemTool toObject(MemorySegment mem, int offset) {
      if (offset + 5 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ItemTool", offset + 5, (int)mem.byteSize());
      } else {
         ItemToolSpec[] specs = null;
         if (hasSpecs(mem, offset)) {
            int off = offset + 5;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Specs", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Specs", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Specs", off + lenOffset + len, (int)mem.byteSize());
            }

            off += lenOffset;
            specs = new ItemToolSpec[len];

            for (int i = 0; i < len; i++) {
               specs[i] = ItemToolSpec.toObject(mem, off);
               off += specs[i].computeSize();
            }
         }

         return new ItemTool(specs, mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1)));
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.specs != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeFloatLE(this.speed);
      if (this.specs != null) {
         if (this.specs.length > 4096000) {
            throw ProtocolException.arrayTooLong("Specs", this.specs.length, 4096000);
         }

         VarInt.write(buf, this.specs.length);

         for (ItemToolSpec item : this.specs) {
            item.serialize(buf);
         }
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.specs != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 1), this.speed);
      int varOffset = offset + 5;
      if (this.specs != null) {
         if (this.specs.length > 4096000) {
            throw ProtocolException.arrayTooLong("Specs", this.specs.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.specs.length);
         int specsValueOffset = 0;

         for (int i = 0; i < this.specs.length; i++) {
            specsValueOffset += this.specs[i].serialize(mem, varOffset + specsValueOffset);
         }

         varOffset += specsValueOffset;
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 5;
      if (this.specs != null) {
         int specsSize = 0;

         for (ItemToolSpec elem : this.specs) {
            specsSize += elem.computeSize();
         }

         size += VarInt.size(this.specs.length) + specsSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 5) {
         return ValidationResult.error("Buffer too small: expected at least 5 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 5;
         if ((nullBits & 1) != 0) {
            int specsCount = VarInt.peek(buffer, pos);
            if (specsCount < 0) {
               return ValidationResult.error("Invalid array count for Specs");
            }

            if (specsCount > 4096000) {
               return ValidationResult.error("Specs exceeds max length 4096000");
            }

            pos += VarInt.size(specsCount);

            for (int i = 0; i < specsCount; i++) {
               ValidationResult structResult = ItemToolSpec.validateStructure(buffer, pos);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid ItemToolSpec in Specs[" + i + "]: " + structResult.error());
               }

               pos += ItemToolSpec.computeBytesConsumed(buffer, pos);
            }
         }

         return ValidationResult.OK;
      }
   }

   public ItemTool clone() {
      ItemTool copy = new ItemTool();
      copy.specs = this.specs != null ? Arrays.stream(this.specs).map(e -> e.clone()).toArray(ItemToolSpec[]::new) : null;
      copy.speed = this.speed;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ItemTool other) ? false : Arrays.equals((Object[])this.specs, (Object[])other.specs) && this.speed == other.speed;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Arrays.hashCode((Object[])this.specs);
      return 31 * result + Float.hashCode(this.speed);
   }
}
