package com.hypixel.hytale.protocol.packets.buildertools;

import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.protocol.Model;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ClipboardEntityChange {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 45;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 53;
   public static final int MAX_SIZE = 1677721600;
   public float x;
   public float y;
   public float z;
   public int blockId;
   @Nullable
   public Model model;
   @Nullable
   public String itemId;
   @Nullable
   public Direction bodyOrientation;
   @Nullable
   public Direction lookOrientation;
   public float scale;

   public ClipboardEntityChange() {
   }

   public ClipboardEntityChange(
      float x,
      float y,
      float z,
      int blockId,
      @Nullable Model model,
      @Nullable String itemId,
      @Nullable Direction bodyOrientation,
      @Nullable Direction lookOrientation,
      float scale
   ) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.blockId = blockId;
      this.model = model;
      this.itemId = itemId;
      this.bodyOrientation = bodyOrientation;
      this.lookOrientation = lookOrientation;
      this.scale = scale;
   }

   public ClipboardEntityChange(@Nonnull ClipboardEntityChange other) {
      this.x = other.x;
      this.y = other.y;
      this.z = other.z;
      this.blockId = other.blockId;
      this.model = other.model;
      this.itemId = other.itemId;
      this.bodyOrientation = other.bodyOrientation;
      this.lookOrientation = other.lookOrientation;
      this.scale = other.scale;
   }

   @Nonnull
   public static ClipboardEntityChange deserialize(@Nonnull ByteBuf buf, int offset) {
      ClipboardEntityChange obj = new ClipboardEntityChange();
      byte nullBits = buf.getByte(offset);
      obj.x = buf.getFloatLE(offset + 1);
      obj.y = buf.getFloatLE(offset + 5);
      obj.z = buf.getFloatLE(offset + 9);
      obj.blockId = buf.getIntLE(offset + 13);
      if ((nullBits & 1) != 0) {
         obj.bodyOrientation = Direction.deserialize(buf, offset + 17);
      }

      if ((nullBits & 2) != 0) {
         obj.lookOrientation = Direction.deserialize(buf, offset + 29);
      }

      obj.scale = buf.getFloatLE(offset + 41);
      if ((nullBits & 4) != 0) {
         int varPos0 = offset + 53 + buf.getIntLE(offset + 45);
         obj.model = Model.deserialize(buf, varPos0);
      }

      if ((nullBits & 8) != 0) {
         int varPos1 = offset + 53 + buf.getIntLE(offset + 49);
         int itemIdLen = VarInt.peek(buf, varPos1);
         if (itemIdLen < 0) {
            throw ProtocolException.negativeLength("ItemId", itemIdLen);
         }

         if (itemIdLen > 4096000) {
            throw ProtocolException.stringTooLong("ItemId", itemIdLen, 4096000);
         }

         obj.itemId = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 53;
      if ((nullBits & 4) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 45);
         int pos0 = offset + 53 + fieldOffset0;
         pos0 += Model.computeBytesConsumed(buf, pos0);
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 49);
         int pos1 = offset + 53 + fieldOffset1;
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
      if (this.bodyOrientation != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.lookOrientation != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.model != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.itemId != null) {
         nullBits = (byte)(nullBits | 8);
      }

      buf.writeByte(nullBits);
      buf.writeFloatLE(this.x);
      buf.writeFloatLE(this.y);
      buf.writeFloatLE(this.z);
      buf.writeIntLE(this.blockId);
      if (this.bodyOrientation != null) {
         this.bodyOrientation.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      if (this.lookOrientation != null) {
         this.lookOrientation.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      buf.writeFloatLE(this.scale);
      int modelOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int itemIdOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.model != null) {
         buf.setIntLE(modelOffsetSlot, buf.writerIndex() - varBlockStart);
         this.model.serialize(buf);
      } else {
         buf.setIntLE(modelOffsetSlot, -1);
      }

      if (this.itemId != null) {
         buf.setIntLE(itemIdOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.itemId, 4096000);
      } else {
         buf.setIntLE(itemIdOffsetSlot, -1);
      }
   }

   public int computeSize() {
      int size = 53;
      if (this.model != null) {
         size += this.model.computeSize();
      }

      if (this.itemId != null) {
         size += PacketIO.stringSize(this.itemId);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 53) {
         return ValidationResult.error("Buffer too small: expected at least 53 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 4) != 0) {
            int modelOffset = buffer.getIntLE(offset + 45);
            if (modelOffset < 0) {
               return ValidationResult.error("Invalid offset for Model");
            }

            int pos = offset + 53 + modelOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Model");
            }

            ValidationResult modelResult = Model.validateStructure(buffer, pos);
            if (!modelResult.isValid()) {
               return ValidationResult.error("Invalid Model: " + modelResult.error());
            }

            pos += Model.computeBytesConsumed(buffer, pos);
         }

         if ((nullBits & 8) != 0) {
            int itemIdOffset = buffer.getIntLE(offset + 49);
            if (itemIdOffset < 0) {
               return ValidationResult.error("Invalid offset for ItemId");
            }

            int posx = offset + 53 + itemIdOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for ItemId");
            }

            int itemIdLen = VarInt.peek(buffer, posx);
            if (itemIdLen < 0) {
               return ValidationResult.error("Invalid string length for ItemId");
            }

            if (itemIdLen > 4096000) {
               return ValidationResult.error("ItemId exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);
            posx += itemIdLen;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading ItemId");
            }
         }

         return ValidationResult.OK;
      }
   }

   public ClipboardEntityChange clone() {
      ClipboardEntityChange copy = new ClipboardEntityChange();
      copy.x = this.x;
      copy.y = this.y;
      copy.z = this.z;
      copy.blockId = this.blockId;
      copy.model = this.model != null ? this.model.clone() : null;
      copy.itemId = this.itemId;
      copy.bodyOrientation = this.bodyOrientation != null ? this.bodyOrientation.clone() : null;
      copy.lookOrientation = this.lookOrientation != null ? this.lookOrientation.clone() : null;
      copy.scale = this.scale;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ClipboardEntityChange other)
            ? false
            : this.x == other.x
               && this.y == other.y
               && this.z == other.z
               && this.blockId == other.blockId
               && Objects.equals(this.model, other.model)
               && Objects.equals(this.itemId, other.itemId)
               && Objects.equals(this.bodyOrientation, other.bodyOrientation)
               && Objects.equals(this.lookOrientation, other.lookOrientation)
               && this.scale == other.scale;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.x, this.y, this.z, this.blockId, this.model, this.itemId, this.bodyOrientation, this.lookOrientation, this.scale);
   }
}
