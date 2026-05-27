package com.hypixel.hytale.protocol.packets.buildertools;

import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.protocol.Model;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
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
      if (buf.readableBytes() - offset < 53) {
         throw ProtocolException.bufferTooSmall("ClipboardEntityChange", 53, buf.readableBytes() - offset);
      } else {
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
            int varPosBase0 = buf.getIntLE(offset + 45);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 53) {
               throw ProtocolException.invalidOffset("Model", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 53 + varPosBase0;
            obj.model = Model.deserialize(buf, varPos0);
         }

         if ((nullBits & 8) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 49);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 53) {
               throw ProtocolException.invalidOffset("ItemId", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 53 + varPosBase1;
            int itemIdLen = VarInt.peek(buf, varPos1);
            if (itemIdLen < 0) {
               throw ProtocolException.invalidVarInt("ItemId");
            }

            int itemIdVarIntLen = VarInt.size(itemIdLen);
            if (itemIdLen > 4096000) {
               throw ProtocolException.stringTooLong("ItemId", itemIdLen, 4096000);
            }

            if (varPos1 + itemIdVarIntLen + itemIdLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("ItemId", varPos1 + itemIdVarIntLen + itemIdLen, buf.readableBytes());
            }

            obj.itemId = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 53;
      if ((nullBits & 4) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 45);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 53) {
            throw ProtocolException.invalidOffset("Model", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 53 + fieldOffset0;
         pos0 += Model.computeBytesConsumed(buf, pos0);
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 49);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 53) {
            throw ProtocolException.invalidOffset("ItemId", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 53 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(sl) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 53L;
   }

   public static float getX(MemorySegment mem) {
      return getX(mem, 0);
   }

   public static float getX(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1));
   }

   public static float getY(MemorySegment mem) {
      return getY(mem, 0);
   }

   public static float getY(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 5));
   }

   public static float getZ(MemorySegment mem) {
      return getZ(mem, 0);
   }

   public static float getZ(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 9));
   }

   public static int getBlockId(MemorySegment mem) {
      return getBlockId(mem, 0);
   }

   public static int getBlockId(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 13));
   }

   @Nullable
   public static Model getModel(MemorySegment mem) {
      return getModel(mem, 0);
   }

   @Nullable
   public static Model getModel(MemorySegment mem, int offset) {
      return hasModel(mem, offset) ? Model.toObject(mem, offset + getValidatedOffset(mem, offset, 45, 53, "Model")) : null;
   }

   @Nullable
   public static String getItemId(MemorySegment mem) {
      return getItemId(mem, 0);
   }

   @Nullable
   public static String getItemId(MemorySegment mem, int offset) {
      return hasItemId(mem, offset)
         ? PacketIO.readVarString("ItemId", mem, offset + getValidatedOffset(mem, offset, 49, 53, "ItemId"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static Direction getBodyOrientation(MemorySegment mem) {
      return getBodyOrientation(mem, 0);
   }

   @Nullable
   public static Direction getBodyOrientation(MemorySegment mem, int offset) {
      return hasBodyOrientation(mem, offset) ? Direction.toObject(mem, offset + 17) : null;
   }

   @Nullable
   public static Direction getLookOrientation(MemorySegment mem) {
      return getLookOrientation(mem, 0);
   }

   @Nullable
   public static Direction getLookOrientation(MemorySegment mem, int offset) {
      return hasLookOrientation(mem, offset) ? Direction.toObject(mem, offset + 29) : null;
   }

   public static float getScale(MemorySegment mem) {
      return getScale(mem, 0);
   }

   public static float getScale(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 41));
   }

   public static boolean hasBodyOrientation(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasLookOrientation(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasModel(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasItemId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 8) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static ClipboardEntityChange toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ClipboardEntityChange toObject(MemorySegment mem, int offset) {
      if (offset + 53 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ClipboardEntityChange", offset + 53, (int)mem.byteSize());
      } else {
         return new ClipboardEntityChange(
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 5)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 9)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 13)),
            hasModel(mem, offset) ? Model.toObject(mem, offset + getValidatedOffset(mem, offset, 45, 53, "Model")) : null,
            hasItemId(mem, offset)
               ? PacketIO.readVarString("ItemId", mem, offset + getValidatedOffset(mem, offset, 49, 53, "ItemId"), 4096000, PacketIO.UTF8)
               : null,
            hasBodyOrientation(mem, offset) ? Direction.toObject(mem, offset + 17) : null,
            hasLookOrientation(mem, offset) ? Direction.toObject(mem, offset + 29) : null,
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 41))
         );
      }
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

   public int serialize(@Nonnull MemorySegment mem, int offset) {
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

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 1), this.x);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 5), this.y);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 9), this.z);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 13), this.blockId);
      if (this.bodyOrientation != null) {
         this.bodyOrientation.serialize(mem, offset + 17);
      } else {
         mem.asSlice(offset + 17, 12L).fill((byte)0);
      }

      if (this.lookOrientation != null) {
         this.lookOrientation.serialize(mem, offset + 29);
      } else {
         mem.asSlice(offset + 29, 12L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 41), this.scale);
      int varOffset = offset + 53;
      if (this.model != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 45), varOffset - offset - 53);
         varOffset += this.model.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 45), -1);
      }

      if (this.itemId != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 49), varOffset - offset - 53);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.itemId, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 49), -1);
      }

      return varOffset - offset;
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
            if (modelOffset < 0 || modelOffset > buffer.writerIndex() - offset - 53) {
               return ValidationResult.error("Invalid offset for Model");
            }

            int pos = offset + 53 + modelOffset;
            ValidationResult modelResult = Model.validateStructure(buffer, pos);
            if (!modelResult.isValid()) {
               return ValidationResult.error("Invalid Model: " + modelResult.error());
            }

            pos += Model.computeBytesConsumed(buffer, pos);
         }

         if ((nullBits & 8) != 0) {
            int itemIdOffset = buffer.getIntLE(offset + 49);
            if (itemIdOffset < 0 || itemIdOffset > buffer.writerIndex() - offset - 53) {
               return ValidationResult.error("Invalid offset for ItemId");
            }

            int pos = offset + 53 + itemIdOffset;
            int itemIdLen = VarInt.peek(buffer, pos);
            if (itemIdLen < 0) {
               return ValidationResult.error("Invalid string length for ItemId");
            }

            if (itemIdLen > 4096000) {
               return ValidationResult.error("ItemId exceeds max length 4096000");
            }

            pos += VarInt.size(itemIdLen);
            pos += itemIdLen;
            if (pos > buffer.writerIndex()) {
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
