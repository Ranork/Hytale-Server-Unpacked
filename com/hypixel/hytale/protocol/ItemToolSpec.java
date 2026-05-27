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

public class ItemToolSpec {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 9;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 16384014;
   @Nullable
   public String gatherType;
   public float power;
   public int quality;

   public ItemToolSpec() {
   }

   public ItemToolSpec(@Nullable String gatherType, float power, int quality) {
      this.gatherType = gatherType;
      this.power = power;
      this.quality = quality;
   }

   public ItemToolSpec(@Nonnull ItemToolSpec other) {
      this.gatherType = other.gatherType;
      this.power = other.power;
      this.quality = other.quality;
   }

   @Nonnull
   public static ItemToolSpec deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 9) {
         throw ProtocolException.bufferTooSmall("ItemToolSpec", 9, buf.readableBytes() - offset);
      } else {
         ItemToolSpec obj = new ItemToolSpec();
         byte nullBits = buf.getByte(offset);
         obj.power = buf.getFloatLE(offset + 1);
         obj.quality = buf.getIntLE(offset + 5);
         int pos = offset + 9;
         if ((nullBits & 1) != 0) {
            int gatherTypeLen = VarInt.peek(buf, pos);
            if (gatherTypeLen < 0) {
               throw ProtocolException.invalidVarInt("GatherType");
            }

            int gatherTypeVarLen = VarInt.size(gatherTypeLen);
            if (gatherTypeLen > 4096000) {
               throw ProtocolException.stringTooLong("GatherType", gatherTypeLen, 4096000);
            }

            if (pos + gatherTypeVarLen + gatherTypeLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("GatherType", pos + gatherTypeVarLen + gatherTypeLen, buf.readableBytes());
            }

            obj.gatherType = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
            pos += gatherTypeVarLen + gatherTypeLen;
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
   public static String getGatherType(MemorySegment mem) {
      return getGatherType(mem, 0);
   }

   @Nullable
   public static String getGatherType(MemorySegment mem, int offset) {
      return hasGatherType(mem, offset) ? PacketIO.readVarString("GatherType", mem, offset + 9, 4096000, PacketIO.UTF8) : null;
   }

   public static float getPower(MemorySegment mem) {
      return getPower(mem, 0);
   }

   public static float getPower(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1));
   }

   public static int getQuality(MemorySegment mem) {
      return getQuality(mem, 0);
   }

   public static int getQuality(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 5));
   }

   public static boolean hasGatherType(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static ItemToolSpec toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ItemToolSpec toObject(MemorySegment mem, int offset) {
      if (offset + 9 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ItemToolSpec", offset + 9, (int)mem.byteSize());
      } else {
         return new ItemToolSpec(
            hasGatherType(mem, offset) ? PacketIO.readVarString("GatherType", mem, offset + 9, 4096000, PacketIO.UTF8) : null,
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 5))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.gatherType != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeFloatLE(this.power);
      buf.writeIntLE(this.quality);
      if (this.gatherType != null) {
         PacketIO.writeVarString(buf, this.gatherType, 4096000);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.gatherType != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 1), this.power);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 5), this.quality);
      int varOffset = offset + 9;
      if (this.gatherType != null) {
         varOffset += PacketIO.writeVarString(mem, varOffset, this.gatherType, 4096000);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 9;
      if (this.gatherType != null) {
         size += PacketIO.stringSize(this.gatherType);
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
            int gatherTypeLen = VarInt.peek(buffer, pos);
            if (gatherTypeLen < 0) {
               return ValidationResult.error("Invalid string length for GatherType");
            }

            if (gatherTypeLen > 4096000) {
               return ValidationResult.error("GatherType exceeds max length 4096000");
            }

            pos += VarInt.size(gatherTypeLen);
            pos += gatherTypeLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading GatherType");
            }
         }

         return ValidationResult.OK;
      }
   }

   public ItemToolSpec clone() {
      ItemToolSpec copy = new ItemToolSpec();
      copy.gatherType = this.gatherType;
      copy.power = this.power;
      copy.quality = this.quality;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ItemToolSpec other)
            ? false
            : Objects.equals(this.gatherType, other.gatherType) && this.power == other.power && this.quality == other.quality;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.gatherType, this.power, this.quality);
   }
}
