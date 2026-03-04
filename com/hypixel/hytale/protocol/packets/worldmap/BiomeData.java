package com.hypixel.hytale.protocol.packets.worldmap;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BiomeData {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 9;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 17;
   public static final int MAX_SIZE = 32768027;
   public int zoneId;
   @Nullable
   public String zoneName;
   @Nullable
   public String biomeName;
   public int biomeColor;

   public BiomeData() {
   }

   public BiomeData(int zoneId, @Nullable String zoneName, @Nullable String biomeName, int biomeColor) {
      this.zoneId = zoneId;
      this.zoneName = zoneName;
      this.biomeName = biomeName;
      this.biomeColor = biomeColor;
   }

   public BiomeData(@Nonnull BiomeData other) {
      this.zoneId = other.zoneId;
      this.zoneName = other.zoneName;
      this.biomeName = other.biomeName;
      this.biomeColor = other.biomeColor;
   }

   @Nonnull
   public static BiomeData deserialize(@Nonnull ByteBuf buf, int offset) {
      BiomeData obj = new BiomeData();
      byte nullBits = buf.getByte(offset);
      obj.zoneId = buf.getIntLE(offset + 1);
      obj.biomeColor = buf.getIntLE(offset + 5);
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 17 + buf.getIntLE(offset + 9);
         int zoneNameLen = VarInt.peek(buf, varPos0);
         if (zoneNameLen < 0) {
            throw ProtocolException.negativeLength("ZoneName", zoneNameLen);
         }

         if (zoneNameLen > 4096000) {
            throw ProtocolException.stringTooLong("ZoneName", zoneNameLen, 4096000);
         }

         obj.zoneName = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 17 + buf.getIntLE(offset + 13);
         int biomeNameLen = VarInt.peek(buf, varPos1);
         if (biomeNameLen < 0) {
            throw ProtocolException.negativeLength("BiomeName", biomeNameLen);
         }

         if (biomeNameLen > 4096000) {
            throw ProtocolException.stringTooLong("BiomeName", biomeNameLen, 4096000);
         }

         obj.biomeName = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 17;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 9);
         int pos0 = offset + 17 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 13);
         int pos1 = offset + 17 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.zoneName != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.biomeName != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.zoneId);
      buf.writeIntLE(this.biomeColor);
      int zoneNameOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int biomeNameOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.zoneName != null) {
         buf.setIntLE(zoneNameOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.zoneName, 4096000);
      } else {
         buf.setIntLE(zoneNameOffsetSlot, -1);
      }

      if (this.biomeName != null) {
         buf.setIntLE(biomeNameOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.biomeName, 4096000);
      } else {
         buf.setIntLE(biomeNameOffsetSlot, -1);
      }
   }

   public int computeSize() {
      int size = 17;
      if (this.zoneName != null) {
         size += PacketIO.stringSize(this.zoneName);
      }

      if (this.biomeName != null) {
         size += PacketIO.stringSize(this.biomeName);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 17) {
         return ValidationResult.error("Buffer too small: expected at least 17 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int zoneNameOffset = buffer.getIntLE(offset + 9);
            if (zoneNameOffset < 0) {
               return ValidationResult.error("Invalid offset for ZoneName");
            }

            int pos = offset + 17 + zoneNameOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for ZoneName");
            }

            int zoneNameLen = VarInt.peek(buffer, pos);
            if (zoneNameLen < 0) {
               return ValidationResult.error("Invalid string length for ZoneName");
            }

            if (zoneNameLen > 4096000) {
               return ValidationResult.error("ZoneName exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += zoneNameLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading ZoneName");
            }
         }

         if ((nullBits & 2) != 0) {
            int biomeNameOffset = buffer.getIntLE(offset + 13);
            if (biomeNameOffset < 0) {
               return ValidationResult.error("Invalid offset for BiomeName");
            }

            int posx = offset + 17 + biomeNameOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for BiomeName");
            }

            int biomeNameLen = VarInt.peek(buffer, posx);
            if (biomeNameLen < 0) {
               return ValidationResult.error("Invalid string length for BiomeName");
            }

            if (biomeNameLen > 4096000) {
               return ValidationResult.error("BiomeName exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);
            posx += biomeNameLen;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading BiomeName");
            }
         }

         return ValidationResult.OK;
      }
   }

   public BiomeData clone() {
      BiomeData copy = new BiomeData();
      copy.zoneId = this.zoneId;
      copy.zoneName = this.zoneName;
      copy.biomeName = this.biomeName;
      copy.biomeColor = this.biomeColor;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BiomeData other)
            ? false
            : this.zoneId == other.zoneId
               && Objects.equals(this.zoneName, other.zoneName)
               && Objects.equals(this.biomeName, other.biomeName)
               && this.biomeColor == other.biomeColor;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.zoneId, this.zoneName, this.biomeName, this.biomeColor);
   }
}
