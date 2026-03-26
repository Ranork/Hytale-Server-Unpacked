package com.hypixel.hytale.protocol.packets.inventory;

import com.hypixel.hytale.protocol.InventorySection;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
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
      UpdatePlayerInventory obj = new UpdatePlayerInventory();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 25 + buf.getIntLE(offset + 1);
         obj.storage = InventorySection.deserialize(buf, varPos0);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 25 + buf.getIntLE(offset + 5);
         obj.armor = InventorySection.deserialize(buf, varPos1);
      }

      if ((nullBits & 4) != 0) {
         int varPos2 = offset + 25 + buf.getIntLE(offset + 9);
         obj.hotbar = InventorySection.deserialize(buf, varPos2);
      }

      if ((nullBits & 8) != 0) {
         int varPos3 = offset + 25 + buf.getIntLE(offset + 13);
         obj.utility = InventorySection.deserialize(buf, varPos3);
      }

      if ((nullBits & 16) != 0) {
         int varPos4 = offset + 25 + buf.getIntLE(offset + 17);
         obj.tools = InventorySection.deserialize(buf, varPos4);
      }

      if ((nullBits & 32) != 0) {
         int varPos5 = offset + 25 + buf.getIntLE(offset + 21);
         obj.backpack = InventorySection.deserialize(buf, varPos5);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 25;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 1);
         int pos0 = offset + 25 + fieldOffset0;
         pos0 += InventorySection.computeBytesConsumed(buf, pos0);
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         int pos1 = offset + 25 + fieldOffset1;
         pos1 += InventorySection.computeBytesConsumed(buf, pos1);
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 9);
         int pos2 = offset + 25 + fieldOffset2;
         pos2 += InventorySection.computeBytesConsumed(buf, pos2);
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 13);
         int pos3 = offset + 25 + fieldOffset3;
         pos3 += InventorySection.computeBytesConsumed(buf, pos3);
         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      if ((nullBits & 16) != 0) {
         int fieldOffset4 = buf.getIntLE(offset + 17);
         int pos4 = offset + 25 + fieldOffset4;
         pos4 += InventorySection.computeBytesConsumed(buf, pos4);
         if (pos4 - offset > maxEnd) {
            maxEnd = pos4 - offset;
         }
      }

      if ((nullBits & 32) != 0) {
         int fieldOffset5 = buf.getIntLE(offset + 21);
         int pos5 = offset + 25 + fieldOffset5;
         pos5 += InventorySection.computeBytesConsumed(buf, pos5);
         if (pos5 - offset > maxEnd) {
            maxEnd = pos5 - offset;
         }
      }

      return maxEnd;
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
            if (storageOffset < 0) {
               return ValidationResult.error("Invalid offset for Storage");
            }

            int pos = offset + 25 + storageOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Storage");
            }

            ValidationResult storageResult = InventorySection.validateStructure(buffer, pos);
            if (!storageResult.isValid()) {
               return ValidationResult.error("Invalid Storage: " + storageResult.error());
            }

            pos += InventorySection.computeBytesConsumed(buffer, pos);
         }

         if ((nullBits & 2) != 0) {
            int armorOffset = buffer.getIntLE(offset + 5);
            if (armorOffset < 0) {
               return ValidationResult.error("Invalid offset for Armor");
            }

            int posx = offset + 25 + armorOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Armor");
            }

            ValidationResult armorResult = InventorySection.validateStructure(buffer, posx);
            if (!armorResult.isValid()) {
               return ValidationResult.error("Invalid Armor: " + armorResult.error());
            }

            posx += InventorySection.computeBytesConsumed(buffer, posx);
         }

         if ((nullBits & 4) != 0) {
            int hotbarOffset = buffer.getIntLE(offset + 9);
            if (hotbarOffset < 0) {
               return ValidationResult.error("Invalid offset for Hotbar");
            }

            int posxx = offset + 25 + hotbarOffset;
            if (posxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Hotbar");
            }

            ValidationResult hotbarResult = InventorySection.validateStructure(buffer, posxx);
            if (!hotbarResult.isValid()) {
               return ValidationResult.error("Invalid Hotbar: " + hotbarResult.error());
            }

            posxx += InventorySection.computeBytesConsumed(buffer, posxx);
         }

         if ((nullBits & 8) != 0) {
            int utilityOffset = buffer.getIntLE(offset + 13);
            if (utilityOffset < 0) {
               return ValidationResult.error("Invalid offset for Utility");
            }

            int posxxx = offset + 25 + utilityOffset;
            if (posxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Utility");
            }

            ValidationResult utilityResult = InventorySection.validateStructure(buffer, posxxx);
            if (!utilityResult.isValid()) {
               return ValidationResult.error("Invalid Utility: " + utilityResult.error());
            }

            posxxx += InventorySection.computeBytesConsumed(buffer, posxxx);
         }

         if ((nullBits & 16) != 0) {
            int toolsOffset = buffer.getIntLE(offset + 17);
            if (toolsOffset < 0) {
               return ValidationResult.error("Invalid offset for Tools");
            }

            int posxxxx = offset + 25 + toolsOffset;
            if (posxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Tools");
            }

            ValidationResult toolsResult = InventorySection.validateStructure(buffer, posxxxx);
            if (!toolsResult.isValid()) {
               return ValidationResult.error("Invalid Tools: " + toolsResult.error());
            }

            posxxxx += InventorySection.computeBytesConsumed(buffer, posxxxx);
         }

         if ((nullBits & 32) != 0) {
            int backpackOffset = buffer.getIntLE(offset + 21);
            if (backpackOffset < 0) {
               return ValidationResult.error("Invalid offset for Backpack");
            }

            int posxxxxx = offset + 25 + backpackOffset;
            if (posxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Backpack");
            }

            ValidationResult backpackResult = InventorySection.validateStructure(buffer, posxxxxx);
            if (!backpackResult.isValid()) {
               return ValidationResult.error("Invalid Backpack: " + backpackResult.error());
            }

            posxxxxx += InventorySection.computeBytesConsumed(buffer, posxxxxx);
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
