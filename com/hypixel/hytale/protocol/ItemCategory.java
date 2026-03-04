package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemCategory {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 6;
   public static final int VARIABLE_FIELD_COUNT = 4;
   public static final int VARIABLE_BLOCK_START = 22;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public String id;
   @Nullable
   public String name;
   @Nullable
   public String icon;
   public int order;
   @Nonnull
   public ItemGridInfoDisplayMode infoDisplayMode = ItemGridInfoDisplayMode.Tooltip;
   @Nullable
   public ItemCategory[] children;

   public ItemCategory() {
   }

   public ItemCategory(
      @Nullable String id,
      @Nullable String name,
      @Nullable String icon,
      int order,
      @Nonnull ItemGridInfoDisplayMode infoDisplayMode,
      @Nullable ItemCategory[] children
   ) {
      this.id = id;
      this.name = name;
      this.icon = icon;
      this.order = order;
      this.infoDisplayMode = infoDisplayMode;
      this.children = children;
   }

   public ItemCategory(@Nonnull ItemCategory other) {
      this.id = other.id;
      this.name = other.name;
      this.icon = other.icon;
      this.order = other.order;
      this.infoDisplayMode = other.infoDisplayMode;
      this.children = other.children;
   }

   @Nonnull
   public static ItemCategory deserialize(@Nonnull ByteBuf buf, int offset) {
      ItemCategory obj = new ItemCategory();
      byte nullBits = buf.getByte(offset);
      obj.order = buf.getIntLE(offset + 1);
      obj.infoDisplayMode = ItemGridInfoDisplayMode.fromValue(buf.getByte(offset + 5));
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 22 + buf.getIntLE(offset + 6);
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
         int varPos1 = offset + 22 + buf.getIntLE(offset + 10);
         int nameLen = VarInt.peek(buf, varPos1);
         if (nameLen < 0) {
            throw ProtocolException.negativeLength("Name", nameLen);
         }

         if (nameLen > 4096000) {
            throw ProtocolException.stringTooLong("Name", nameLen, 4096000);
         }

         obj.name = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
      }

      if ((nullBits & 4) != 0) {
         int varPos2 = offset + 22 + buf.getIntLE(offset + 14);
         int iconLen = VarInt.peek(buf, varPos2);
         if (iconLen < 0) {
            throw ProtocolException.negativeLength("Icon", iconLen);
         }

         if (iconLen > 4096000) {
            throw ProtocolException.stringTooLong("Icon", iconLen, 4096000);
         }

         obj.icon = PacketIO.readVarString(buf, varPos2, PacketIO.UTF8);
      }

      if ((nullBits & 8) != 0) {
         int varPos3 = offset + 22 + buf.getIntLE(offset + 18);
         int childrenCount = VarInt.peek(buf, varPos3);
         if (childrenCount < 0) {
            throw ProtocolException.negativeLength("Children", childrenCount);
         }

         if (childrenCount > 4096000) {
            throw ProtocolException.arrayTooLong("Children", childrenCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos3);
         if (varPos3 + varIntLen + childrenCount * 6L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Children", varPos3 + varIntLen + childrenCount * 6, buf.readableBytes());
         }

         obj.children = new ItemCategory[childrenCount];
         int elemPos = varPos3 + varIntLen;

         for (int i = 0; i < childrenCount; i++) {
            obj.children[i] = deserialize(buf, elemPos);
            elemPos += computeBytesConsumed(buf, elemPos);
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 22;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 6);
         int pos0 = offset + 22 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 10);
         int pos1 = offset + 22 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 14);
         int pos2 = offset + 22 + fieldOffset2;
         int sl = VarInt.peek(buf, pos2);
         pos2 += VarInt.length(buf, pos2) + sl;
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 18);
         int pos3 = offset + 22 + fieldOffset3;
         int arrLen = VarInt.peek(buf, pos3);
         pos3 += VarInt.length(buf, pos3);

         for (int i = 0; i < arrLen; i++) {
            pos3 += computeBytesConsumed(buf, pos3);
         }

         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
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

      if (this.name != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.icon != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.children != null) {
         nullBits = (byte)(nullBits | 8);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.order);
      buf.writeByte(this.infoDisplayMode.getValue());
      int idOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int nameOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int iconOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int childrenOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.id != null) {
         buf.setIntLE(idOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.id, 4096000);
      } else {
         buf.setIntLE(idOffsetSlot, -1);
      }

      if (this.name != null) {
         buf.setIntLE(nameOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.name, 4096000);
      } else {
         buf.setIntLE(nameOffsetSlot, -1);
      }

      if (this.icon != null) {
         buf.setIntLE(iconOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.icon, 4096000);
      } else {
         buf.setIntLE(iconOffsetSlot, -1);
      }

      if (this.children != null) {
         buf.setIntLE(childrenOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.children.length > 4096000) {
            throw ProtocolException.arrayTooLong("Children", this.children.length, 4096000);
         }

         VarInt.write(buf, this.children.length);

         for (ItemCategory item : this.children) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(childrenOffsetSlot, -1);
      }
   }

   public int computeSize() {
      int size = 22;
      if (this.id != null) {
         size += PacketIO.stringSize(this.id);
      }

      if (this.name != null) {
         size += PacketIO.stringSize(this.name);
      }

      if (this.icon != null) {
         size += PacketIO.stringSize(this.icon);
      }

      if (this.children != null) {
         int childrenSize = 0;

         for (ItemCategory elem : this.children) {
            childrenSize += elem.computeSize();
         }

         size += VarInt.size(this.children.length) + childrenSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 22) {
         return ValidationResult.error("Buffer too small: expected at least 22 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int idOffset = buffer.getIntLE(offset + 6);
            if (idOffset < 0) {
               return ValidationResult.error("Invalid offset for Id");
            }

            int pos = offset + 22 + idOffset;
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
            int nameOffset = buffer.getIntLE(offset + 10);
            if (nameOffset < 0) {
               return ValidationResult.error("Invalid offset for Name");
            }

            int posx = offset + 22 + nameOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Name");
            }

            int nameLen = VarInt.peek(buffer, posx);
            if (nameLen < 0) {
               return ValidationResult.error("Invalid string length for Name");
            }

            if (nameLen > 4096000) {
               return ValidationResult.error("Name exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);
            posx += nameLen;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Name");
            }
         }

         if ((nullBits & 4) != 0) {
            int iconOffset = buffer.getIntLE(offset + 14);
            if (iconOffset < 0) {
               return ValidationResult.error("Invalid offset for Icon");
            }

            int posxx = offset + 22 + iconOffset;
            if (posxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Icon");
            }

            int iconLen = VarInt.peek(buffer, posxx);
            if (iconLen < 0) {
               return ValidationResult.error("Invalid string length for Icon");
            }

            if (iconLen > 4096000) {
               return ValidationResult.error("Icon exceeds max length 4096000");
            }

            posxx += VarInt.length(buffer, posxx);
            posxx += iconLen;
            if (posxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Icon");
            }
         }

         if ((nullBits & 8) != 0) {
            int childrenOffset = buffer.getIntLE(offset + 18);
            if (childrenOffset < 0) {
               return ValidationResult.error("Invalid offset for Children");
            }

            int posxxx = offset + 22 + childrenOffset;
            if (posxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Children");
            }

            int childrenCount = VarInt.peek(buffer, posxxx);
            if (childrenCount < 0) {
               return ValidationResult.error("Invalid array count for Children");
            }

            if (childrenCount > 4096000) {
               return ValidationResult.error("Children exceeds max length 4096000");
            }

            posxxx += VarInt.length(buffer, posxxx);

            for (int i = 0; i < childrenCount; i++) {
               ValidationResult structResult = validateStructure(buffer, posxxx);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid ItemCategory in Children[" + i + "]: " + structResult.error());
               }

               posxxx += computeBytesConsumed(buffer, posxxx);
            }
         }

         return ValidationResult.OK;
      }
   }

   public ItemCategory clone() {
      ItemCategory copy = new ItemCategory();
      copy.id = this.id;
      copy.name = this.name;
      copy.icon = this.icon;
      copy.order = this.order;
      copy.infoDisplayMode = this.infoDisplayMode;
      copy.children = this.children != null ? Arrays.stream(this.children).map(e -> e.clone()).toArray(ItemCategory[]::new) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ItemCategory other)
            ? false
            : Objects.equals(this.id, other.id)
               && Objects.equals(this.name, other.name)
               && Objects.equals(this.icon, other.icon)
               && this.order == other.order
               && Objects.equals(this.infoDisplayMode, other.infoDisplayMode)
               && Arrays.equals((Object[])this.children, (Object[])other.children);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.id);
      result = 31 * result + Objects.hashCode(this.name);
      result = 31 * result + Objects.hashCode(this.icon);
      result = 31 * result + Integer.hashCode(this.order);
      result = 31 * result + Objects.hashCode(this.infoDisplayMode);
      return 31 * result + Arrays.hashCode((Object[])this.children);
   }
}
