package com.hypixel.hytale.protocol.packets.inventory;

import com.hypixel.hytale.protocol.InventorySection;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UpdatePlayerInventory implements Packet, ToClientPacket {
   public static final int PACKET_ID = 170;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 6;
   public static final int VARIABLE_BLOCK_START = 25;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public InventorySection storage;
   @Nullable
   public InventorySection armor;
   @Nullable
   public InventorySection hotbar;
   @Nullable
   public InventorySection utility;
   @Nullable
   public InventorySection tools;
   @Nullable
   public InventorySection backpack;

   @Override
   public int getId() {
      return 170;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdatePlayerInventory() {
   }

   public UpdatePlayerInventory(
      @Nullable InventorySection storage,
      @Nullable InventorySection armor,
      @Nullable InventorySection hotbar,
      @Nullable InventorySection utility,
      @Nullable InventorySection tools,
      @Nullable InventorySection backpack
   ) {
      this.storage = storage;
      this.armor = armor;
      this.hotbar = hotbar;
      this.utility = utility;
      this.tools = tools;
      this.backpack = backpack;
   }

   public UpdatePlayerInventory(@Nonnull UpdatePlayerInventory other) {
      this.storage = other.storage;
      this.armor = other.armor;
      this.hotbar = other.hotbar;
      this.utility = other.utility;
      this.tools = other.tools;
      this.backpack = other.backpack;
   }

   @Nonnull
   public static UpdatePlayerInventory deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 25) {
         throw ProtocolException.bufferTooSmall("UpdatePlayerInventory", 25, buf.readableBytes() - offset);
      } else {
         UpdatePlayerInventory obj = new UpdatePlayerInventory();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 1);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 25) {
               throw ProtocolException.invalidOffset("Storage", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 25 + varPosBase0;
            obj.storage = InventorySection.deserialize(buf, varPos0);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 5);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 25) {
               throw ProtocolException.invalidOffset("Armor", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 25 + varPosBase1;
            obj.armor = InventorySection.deserialize(buf, varPos1);
         }

         if ((nullBits & 4) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 9);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 25) {
               throw ProtocolException.invalidOffset("Hotbar", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 25 + varPosBase2;
            obj.hotbar = InventorySection.deserialize(buf, varPos2);
         }

         if ((nullBits & 8) != 0) {
            int varPosBase3 = buf.getIntLE(offset + 13);
            if (varPosBase3 < 0 || varPosBase3 > buf.writerIndex() - offset - 25) {
               throw ProtocolException.invalidOffset("Utility", varPosBase3, buf.readableBytes());
            }

            int varPos3 = offset + 25 + varPosBase3;
            obj.utility = InventorySection.deserialize(buf, varPos3);
         }

         if ((nullBits & 16) != 0) {
            int varPosBase4 = buf.getIntLE(offset + 17);
            if (varPosBase4 < 0 || varPosBase4 > buf.writerIndex() - offset - 25) {
               throw ProtocolException.invalidOffset("Tools", varPosBase4, buf.readableBytes());
            }

            int varPos4 = offset + 25 + varPosBase4;
            obj.tools = InventorySection.deserialize(buf, varPos4);
         }

         if ((nullBits & 32) != 0) {
            int varPosBase5 = buf.getIntLE(offset + 21);
            if (varPosBase5 < 0 || varPosBase5 > buf.writerIndex() - offset - 25) {
               throw ProtocolException.invalidOffset("Backpack", varPosBase5, buf.readableBytes());
            }

            int varPos5 = offset + 25 + varPosBase5;
            obj.backpack = InventorySection.deserialize(buf, varPos5);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 25;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 1);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 25) {
            throw ProtocolException.invalidOffset("Storage", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 25 + fieldOffset0;
         pos0 += InventorySection.computeBytesConsumed(buf, pos0);
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 25) {
            throw ProtocolException.invalidOffset("Armor", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 25 + fieldOffset1;
         pos1 += InventorySection.computeBytesConsumed(buf, pos1);
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 9);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 25) {
            throw ProtocolException.invalidOffset("Hotbar", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 25 + fieldOffset2;
         pos2 += InventorySection.computeBytesConsumed(buf, pos2);
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 13);
         if (fieldOffset3 < 0 || fieldOffset3 > buf.writerIndex() - offset - 25) {
            throw ProtocolException.invalidOffset("Utility", fieldOffset3, maxEnd);
         }

         int pos3 = offset + 25 + fieldOffset3;
         pos3 += InventorySection.computeBytesConsumed(buf, pos3);
         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      if ((nullBits & 16) != 0) {
         int fieldOffset4 = buf.getIntLE(offset + 17);
         if (fieldOffset4 < 0 || fieldOffset4 > buf.writerIndex() - offset - 25) {
            throw ProtocolException.invalidOffset("Tools", fieldOffset4, maxEnd);
         }

         int pos4 = offset + 25 + fieldOffset4;
         pos4 += InventorySection.computeBytesConsumed(buf, pos4);
         if (pos4 - offset > maxEnd) {
            maxEnd = pos4 - offset;
         }
      }

      if ((nullBits & 32) != 0) {
         int fieldOffset5 = buf.getIntLE(offset + 21);
         if (fieldOffset5 < 0 || fieldOffset5 > buf.writerIndex() - offset - 25) {
            throw ProtocolException.invalidOffset("Backpack", fieldOffset5, maxEnd);
         }

         int pos5 = offset + 25 + fieldOffset5;
         pos5 += InventorySection.computeBytesConsumed(buf, pos5);
         if (pos5 - offset > maxEnd) {
            maxEnd = pos5 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 25L;
   }

   @Nullable
   public static InventorySection getStorage(MemorySegment mem) {
      return getStorage(mem, 0);
   }

   @Nullable
   public static InventorySection getStorage(MemorySegment mem, int offset) {
      return hasStorage(mem, offset) ? InventorySection.toObject(mem, offset + getValidatedOffset(mem, offset, 1, 25, "Storage")) : null;
   }

   @Nullable
   public static InventorySection getArmor(MemorySegment mem) {
      return getArmor(mem, 0);
   }

   @Nullable
   public static InventorySection getArmor(MemorySegment mem, int offset) {
      return hasArmor(mem, offset) ? InventorySection.toObject(mem, offset + getValidatedOffset(mem, offset, 5, 25, "Armor")) : null;
   }

   @Nullable
   public static InventorySection getHotbar(MemorySegment mem) {
      return getHotbar(mem, 0);
   }

   @Nullable
   public static InventorySection getHotbar(MemorySegment mem, int offset) {
      return hasHotbar(mem, offset) ? InventorySection.toObject(mem, offset + getValidatedOffset(mem, offset, 9, 25, "Hotbar")) : null;
   }

   @Nullable
   public static InventorySection getUtility(MemorySegment mem) {
      return getUtility(mem, 0);
   }

   @Nullable
   public static InventorySection getUtility(MemorySegment mem, int offset) {
      return hasUtility(mem, offset) ? InventorySection.toObject(mem, offset + getValidatedOffset(mem, offset, 13, 25, "Utility")) : null;
   }

   @Nullable
   public static InventorySection getTools(MemorySegment mem) {
      return getTools(mem, 0);
   }

   @Nullable
   public static InventorySection getTools(MemorySegment mem, int offset) {
      return hasTools(mem, offset) ? InventorySection.toObject(mem, offset + getValidatedOffset(mem, offset, 17, 25, "Tools")) : null;
   }

   @Nullable
   public static InventorySection getBackpack(MemorySegment mem) {
      return getBackpack(mem, 0);
   }

   @Nullable
   public static InventorySection getBackpack(MemorySegment mem, int offset) {
      return hasBackpack(mem, offset) ? InventorySection.toObject(mem, offset + getValidatedOffset(mem, offset, 21, 25, "Backpack")) : null;
   }

   public static boolean hasStorage(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasArmor(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasHotbar(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasUtility(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 8) != 0;
   }

   public static boolean hasTools(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 16) != 0;
   }

   public static boolean hasBackpack(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 32) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static UpdatePlayerInventory toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UpdatePlayerInventory toObject(MemorySegment mem, int offset) {
      if (offset + 25 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UpdatePlayerInventory", offset + 25, (int)mem.byteSize());
      } else {
         return new UpdatePlayerInventory(
            hasStorage(mem, offset) ? InventorySection.toObject(mem, offset + getValidatedOffset(mem, offset, 1, 25, "Storage")) : null,
            hasArmor(mem, offset) ? InventorySection.toObject(mem, offset + getValidatedOffset(mem, offset, 5, 25, "Armor")) : null,
            hasHotbar(mem, offset) ? InventorySection.toObject(mem, offset + getValidatedOffset(mem, offset, 9, 25, "Hotbar")) : null,
            hasUtility(mem, offset) ? InventorySection.toObject(mem, offset + getValidatedOffset(mem, offset, 13, 25, "Utility")) : null,
            hasTools(mem, offset) ? InventorySection.toObject(mem, offset + getValidatedOffset(mem, offset, 17, 25, "Tools")) : null,
            hasBackpack(mem, offset) ? InventorySection.toObject(mem, offset + getValidatedOffset(mem, offset, 21, 25, "Backpack")) : null
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.storage != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.armor != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.hotbar != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.utility != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.tools != null) {
         nullBits = (byte)(nullBits | 16);
      }

      if (this.backpack != null) {
         nullBits = (byte)(nullBits | 32);
      }

      buf.writeByte(nullBits);
      int storageOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int armorOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int hotbarOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int utilityOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int toolsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int backpackOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.storage != null) {
         buf.setIntLE(storageOffsetSlot, buf.writerIndex() - varBlockStart);
         this.storage.serialize(buf);
      } else {
         buf.setIntLE(storageOffsetSlot, -1);
      }

      if (this.armor != null) {
         buf.setIntLE(armorOffsetSlot, buf.writerIndex() - varBlockStart);
         this.armor.serialize(buf);
      } else {
         buf.setIntLE(armorOffsetSlot, -1);
      }

      if (this.hotbar != null) {
         buf.setIntLE(hotbarOffsetSlot, buf.writerIndex() - varBlockStart);
         this.hotbar.serialize(buf);
      } else {
         buf.setIntLE(hotbarOffsetSlot, -1);
      }

      if (this.utility != null) {
         buf.setIntLE(utilityOffsetSlot, buf.writerIndex() - varBlockStart);
         this.utility.serialize(buf);
      } else {
         buf.setIntLE(utilityOffsetSlot, -1);
      }

      if (this.tools != null) {
         buf.setIntLE(toolsOffsetSlot, buf.writerIndex() - varBlockStart);
         this.tools.serialize(buf);
      } else {
         buf.setIntLE(toolsOffsetSlot, -1);
      }

      if (this.backpack != null) {
         buf.setIntLE(backpackOffsetSlot, buf.writerIndex() - varBlockStart);
         this.backpack.serialize(buf);
      } else {
         buf.setIntLE(backpackOffsetSlot, -1);
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.storage != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.armor != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.hotbar != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.utility != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.tools != null) {
         nullBits = (byte)(nullBits | 16);
      }

      if (this.backpack != null) {
         nullBits = (byte)(nullBits | 32);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 25;
      if (this.storage != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), varOffset - offset - 25);
         varOffset += this.storage.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), -1);
      }

      if (this.armor != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), varOffset - offset - 25);
         varOffset += this.armor.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), -1);
      }

      if (this.hotbar != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), varOffset - offset - 25);
         varOffset += this.hotbar.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), -1);
      }

      if (this.utility != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 13), varOffset - offset - 25);
         varOffset += this.utility.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 13), -1);
      }

      if (this.tools != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 17), varOffset - offset - 25);
         varOffset += this.tools.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 17), -1);
      }

      if (this.backpack != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 21), varOffset - offset - 25);
         varOffset += this.backpack.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 21), -1);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 25;
      if (this.storage != null) {
         size += this.storage.computeSize();
      }

      if (this.armor != null) {
         size += this.armor.computeSize();
      }

      if (this.hotbar != null) {
         size += this.hotbar.computeSize();
      }

      if (this.utility != null) {
         size += this.utility.computeSize();
      }

      if (this.tools != null) {
         size += this.tools.computeSize();
      }

      if (this.backpack != null) {
         size += this.backpack.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 25) {
         return ValidationResult.error("Buffer too small: expected at least 25 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int storageOffset = buffer.getIntLE(offset + 1);
            if (storageOffset < 0 || storageOffset > buffer.writerIndex() - offset - 25) {
               return ValidationResult.error("Invalid offset for Storage");
            }

            int pos = offset + 25 + storageOffset;
            ValidationResult storageResult = InventorySection.validateStructure(buffer, pos);
            if (!storageResult.isValid()) {
               return ValidationResult.error("Invalid Storage: " + storageResult.error());
            }

            pos += InventorySection.computeBytesConsumed(buffer, pos);
         }

         if ((nullBits & 2) != 0) {
            int armorOffset = buffer.getIntLE(offset + 5);
            if (armorOffset < 0 || armorOffset > buffer.writerIndex() - offset - 25) {
               return ValidationResult.error("Invalid offset for Armor");
            }

            int pos = offset + 25 + armorOffset;
            ValidationResult armorResult = InventorySection.validateStructure(buffer, pos);
            if (!armorResult.isValid()) {
               return ValidationResult.error("Invalid Armor: " + armorResult.error());
            }

            pos += InventorySection.computeBytesConsumed(buffer, pos);
         }

         if ((nullBits & 4) != 0) {
            int hotbarOffset = buffer.getIntLE(offset + 9);
            if (hotbarOffset < 0 || hotbarOffset > buffer.writerIndex() - offset - 25) {
               return ValidationResult.error("Invalid offset for Hotbar");
            }

            int pos = offset + 25 + hotbarOffset;
            ValidationResult hotbarResult = InventorySection.validateStructure(buffer, pos);
            if (!hotbarResult.isValid()) {
               return ValidationResult.error("Invalid Hotbar: " + hotbarResult.error());
            }

            pos += InventorySection.computeBytesConsumed(buffer, pos);
         }

         if ((nullBits & 8) != 0) {
            int utilityOffset = buffer.getIntLE(offset + 13);
            if (utilityOffset < 0 || utilityOffset > buffer.writerIndex() - offset - 25) {
               return ValidationResult.error("Invalid offset for Utility");
            }

            int pos = offset + 25 + utilityOffset;
            ValidationResult utilityResult = InventorySection.validateStructure(buffer, pos);
            if (!utilityResult.isValid()) {
               return ValidationResult.error("Invalid Utility: " + utilityResult.error());
            }

            pos += InventorySection.computeBytesConsumed(buffer, pos);
         }

         if ((nullBits & 16) != 0) {
            int toolsOffset = buffer.getIntLE(offset + 17);
            if (toolsOffset < 0 || toolsOffset > buffer.writerIndex() - offset - 25) {
               return ValidationResult.error("Invalid offset for Tools");
            }

            int pos = offset + 25 + toolsOffset;
            ValidationResult toolsResult = InventorySection.validateStructure(buffer, pos);
            if (!toolsResult.isValid()) {
               return ValidationResult.error("Invalid Tools: " + toolsResult.error());
            }

            pos += InventorySection.computeBytesConsumed(buffer, pos);
         }

         if ((nullBits & 32) != 0) {
            int backpackOffset = buffer.getIntLE(offset + 21);
            if (backpackOffset < 0 || backpackOffset > buffer.writerIndex() - offset - 25) {
               return ValidationResult.error("Invalid offset for Backpack");
            }

            int pos = offset + 25 + backpackOffset;
            ValidationResult backpackResult = InventorySection.validateStructure(buffer, pos);
            if (!backpackResult.isValid()) {
               return ValidationResult.error("Invalid Backpack: " + backpackResult.error());
            }

            pos += InventorySection.computeBytesConsumed(buffer, pos);
         }

         return ValidationResult.OK;
      }
   }

   public UpdatePlayerInventory clone() {
      UpdatePlayerInventory copy = new UpdatePlayerInventory();
      copy.storage = this.storage != null ? this.storage.clone() : null;
      copy.armor = this.armor != null ? this.armor.clone() : null;
      copy.hotbar = this.hotbar != null ? this.hotbar.clone() : null;
      copy.utility = this.utility != null ? this.utility.clone() : null;
      copy.tools = this.tools != null ? this.tools.clone() : null;
      copy.backpack = this.backpack != null ? this.backpack.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdatePlayerInventory other)
            ? false
            : Objects.equals(this.storage, other.storage)
               && Objects.equals(this.armor, other.armor)
               && Objects.equals(this.hotbar, other.hotbar)
               && Objects.equals(this.utility, other.utility)
               && Objects.equals(this.tools, other.tools)
               && Objects.equals(this.backpack, other.backpack);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.storage, this.armor, this.hotbar, this.utility, this.tools, this.backpack);
   }
}
