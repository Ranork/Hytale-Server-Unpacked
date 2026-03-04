package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ResourceType {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 32768019;
   @Nullable
   public String id;
   @Nullable
   public String icon;

   public ResourceType() {
   }

   public ResourceType(@Nullable String id, @Nullable String icon) {
      this.id = id;
      this.icon = icon;
   }

   public ResourceType(@Nonnull ResourceType other) {
      this.id = other.id;
      this.icon = other.icon;
   }

   @Nonnull
   public static ResourceType deserialize(@Nonnull ByteBuf buf, int offset) {
      ResourceType obj = new ResourceType();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 9 + buf.getIntLE(offset + 1);
         int idLen = VarInt.peek(buf, varPos0);
         if (idLen < 0) {
            throw ProtocolException.negativeLength("Id", idLen);
         }

         if (idLen > 4096000) {
            throw ProtocolException.stringTooLong("Id", idLen, 4096000);
         }

         obj.id = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 9 + buf.getIntLE(offset + 5);
         int iconLen = VarInt.peek(buf, varPos1);
         if (iconLen < 0) {
            throw ProtocolException.negativeLength("Icon", iconLen);
         }

         if (iconLen > 4096000) {
            throw ProtocolException.stringTooLong("Icon", iconLen, 4096000);
         }

         obj.icon = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 9;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 1);
         int pos0 = offset + 9 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         int pos1 = offset + 9 + fieldOffset1;
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
      if (this.id != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.icon != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      int idOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int iconOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.id != null) {
         buf.setIntLE(idOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.id, 4096000);
      } else {
         buf.setIntLE(idOffsetSlot, -1);
      }

      if (this.icon != null) {
         buf.setIntLE(iconOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.icon, 4096000);
      } else {
         buf.setIntLE(iconOffsetSlot, -1);
      }
   }

   public int computeSize() {
      int size = 9;
      if (this.id != null) {
         size += PacketIO.stringSize(this.id);
      }

      if (this.icon != null) {
         size += PacketIO.stringSize(this.icon);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 9) {
         return ValidationResult.error("Buffer too small: expected at least 9 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int idOffset = buffer.getIntLE(offset + 1);
            if (idOffset < 0) {
               return ValidationResult.error("Invalid offset for Id");
            }

            int pos = offset + 9 + idOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Id");
            }

            int idLen = VarInt.peek(buffer, pos);
            if (idLen < 0) {
               return ValidationResult.error("Invalid string length for Id");
            }

            if (idLen > 4096000) {
               return ValidationResult.error("Id exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += idLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Id");
            }
         }

         if ((nullBits & 2) != 0) {
            int iconOffset = buffer.getIntLE(offset + 5);
            if (iconOffset < 0) {
               return ValidationResult.error("Invalid offset for Icon");
            }

            int posx = offset + 9 + iconOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Icon");
            }

            int iconLen = VarInt.peek(buffer, posx);
            if (iconLen < 0) {
               return ValidationResult.error("Invalid string length for Icon");
            }

            if (iconLen > 4096000) {
               return ValidationResult.error("Icon exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);
            posx += iconLen;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Icon");
            }
         }

         return ValidationResult.OK;
      }
   }

   public ResourceType clone() {
      ResourceType copy = new ResourceType();
      copy.id = this.id;
      copy.icon = this.icon;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ResourceType other) ? false : Objects.equals(this.id, other.id) && Objects.equals(this.icon, other.icon);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.id, this.icon);
   }
}
