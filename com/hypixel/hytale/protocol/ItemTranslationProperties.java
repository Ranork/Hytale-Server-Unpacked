package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemTranslationProperties {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 32768019;
   @Nullable
   public String name;
   @Nullable
   public String description;

   public ItemTranslationProperties() {
   }

   public ItemTranslationProperties(@Nullable String name, @Nullable String description) {
      this.name = name;
      this.description = description;
   }

   public ItemTranslationProperties(@Nonnull ItemTranslationProperties other) {
      this.name = other.name;
      this.description = other.description;
   }

   @Nonnull
   public static ItemTranslationProperties deserialize(@Nonnull ByteBuf buf, int offset) {
      ItemTranslationProperties obj = new ItemTranslationProperties();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 9 + buf.getIntLE(offset + 1);
         int nameLen = VarInt.peek(buf, varPos0);
         if (nameLen < 0) {
            throw ProtocolException.negativeLength("Name", nameLen);
         }

         if (nameLen > 4096000) {
            throw ProtocolException.stringTooLong("Name", nameLen, 4096000);
         }

         obj.name = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 9 + buf.getIntLE(offset + 5);
         int descriptionLen = VarInt.peek(buf, varPos1);
         if (descriptionLen < 0) {
            throw ProtocolException.negativeLength("Description", descriptionLen);
         }

         if (descriptionLen > 4096000) {
            throw ProtocolException.stringTooLong("Description", descriptionLen, 4096000);
         }

         obj.description = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
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
      if (this.name != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.description != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      int nameOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int descriptionOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.name != null) {
         buf.setIntLE(nameOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.name, 4096000);
      } else {
         buf.setIntLE(nameOffsetSlot, -1);
      }

      if (this.description != null) {
         buf.setIntLE(descriptionOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.description, 4096000);
      } else {
         buf.setIntLE(descriptionOffsetSlot, -1);
      }
   }

   public int computeSize() {
      int size = 9;
      if (this.name != null) {
         size += PacketIO.stringSize(this.name);
      }

      if (this.description != null) {
         size += PacketIO.stringSize(this.description);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 9) {
         return ValidationResult.error("Buffer too small: expected at least 9 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int nameOffset = buffer.getIntLE(offset + 1);
            if (nameOffset < 0) {
               return ValidationResult.error("Invalid offset for Name");
            }

            int pos = offset + 9 + nameOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Name");
            }

            int nameLen = VarInt.peek(buffer, pos);
            if (nameLen < 0) {
               return ValidationResult.error("Invalid string length for Name");
            }

            if (nameLen > 4096000) {
               return ValidationResult.error("Name exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += nameLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Name");
            }
         }

         if ((nullBits & 2) != 0) {
            int descriptionOffset = buffer.getIntLE(offset + 5);
            if (descriptionOffset < 0) {
               return ValidationResult.error("Invalid offset for Description");
            }

            int posx = offset + 9 + descriptionOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Description");
            }

            int descriptionLen = VarInt.peek(buffer, posx);
            if (descriptionLen < 0) {
               return ValidationResult.error("Invalid string length for Description");
            }

            if (descriptionLen > 4096000) {
               return ValidationResult.error("Description exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);
            posx += descriptionLen;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Description");
            }
         }

         return ValidationResult.OK;
      }
   }

   public ItemTranslationProperties clone() {
      ItemTranslationProperties copy = new ItemTranslationProperties();
      copy.name = this.name;
      copy.description = this.description;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ItemTranslationProperties other)
            ? false
            : Objects.equals(this.name, other.name) && Objects.equals(this.description, other.description);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.name, this.description);
   }
}
