package com.hypixel.hytale.protocol.packets.window;

import com.hypixel.hytale.protocol.ExtraResources;
import com.hypixel.hytale.protocol.InventorySection;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class OpenWindow implements Packet, ToClientPacket {
   public static final int PACKET_ID = 200;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 6;
   public static final int VARIABLE_FIELD_COUNT = 3;
   public static final int VARIABLE_BLOCK_START = 18;
   public static final int MAX_SIZE = 1677721600;
   public int id;
   @Nonnull
   public WindowType windowType = WindowType.Container;
   @Nullable
   public String windowData;
   @Nullable
   public InventorySection inventory;
   @Nullable
   public ExtraResources extraResources;

   @Override
   public int getId() {
      return 200;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public OpenWindow() {
   }

   public OpenWindow(
      int id, @Nonnull WindowType windowType, @Nullable String windowData, @Nullable InventorySection inventory, @Nullable ExtraResources extraResources
   ) {
      this.id = id;
      this.windowType = windowType;
      this.windowData = windowData;
      this.inventory = inventory;
      this.extraResources = extraResources;
   }

   public OpenWindow(@Nonnull OpenWindow other) {
      this.id = other.id;
      this.windowType = other.windowType;
      this.windowData = other.windowData;
      this.inventory = other.inventory;
      this.extraResources = other.extraResources;
   }

   @Nonnull
   public static OpenWindow deserialize(@Nonnull ByteBuf buf, int offset) {
      OpenWindow obj = new OpenWindow();
      byte nullBits = buf.getByte(offset);
      obj.id = buf.getIntLE(offset + 1);
      obj.windowType = WindowType.fromValue(buf.getByte(offset + 5));
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 18 + buf.getIntLE(offset + 6);
         int windowDataLen = VarInt.peek(buf, varPos0);
         if (windowDataLen < 0) {
            throw ProtocolException.negativeLength("WindowData", windowDataLen);
         }

         if (windowDataLen > 4096000) {
            throw ProtocolException.stringTooLong("WindowData", windowDataLen, 4096000);
         }

         obj.windowData = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 18 + buf.getIntLE(offset + 10);
         obj.inventory = InventorySection.deserialize(buf, varPos1);
      }

      if ((nullBits & 4) != 0) {
         int varPos2 = offset + 18 + buf.getIntLE(offset + 14);
         obj.extraResources = ExtraResources.deserialize(buf, varPos2);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 18;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 6);
         int pos0 = offset + 18 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 10);
         int pos1 = offset + 18 + fieldOffset1;
         pos1 += InventorySection.computeBytesConsumed(buf, pos1);
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 14);
         int pos2 = offset + 18 + fieldOffset2;
         pos2 += ExtraResources.computeBytesConsumed(buf, pos2);
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      return maxEnd;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.windowData != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.inventory != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.extraResources != null) {
         nullBits = (byte)(nullBits | 4);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.id);
      buf.writeByte(this.windowType.getValue());
      int windowDataOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int inventoryOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int extraResourcesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.windowData != null) {
         buf.setIntLE(windowDataOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.windowData, 4096000);
      } else {
         buf.setIntLE(windowDataOffsetSlot, -1);
      }

      if (this.inventory != null) {
         buf.setIntLE(inventoryOffsetSlot, buf.writerIndex() - varBlockStart);
         this.inventory.serialize(buf);
      } else {
         buf.setIntLE(inventoryOffsetSlot, -1);
      }

      if (this.extraResources != null) {
         buf.setIntLE(extraResourcesOffsetSlot, buf.writerIndex() - varBlockStart);
         this.extraResources.serialize(buf);
      } else {
         buf.setIntLE(extraResourcesOffsetSlot, -1);
      }
   }

   @Override
   public int computeSize() {
      int size = 18;
      if (this.windowData != null) {
         size += PacketIO.stringSize(this.windowData);
      }

      if (this.inventory != null) {
         size += this.inventory.computeSize();
      }

      if (this.extraResources != null) {
         size += this.extraResources.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 18) {
         return ValidationResult.error("Buffer too small: expected at least 18 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int windowDataOffset = buffer.getIntLE(offset + 6);
            if (windowDataOffset < 0) {
               return ValidationResult.error("Invalid offset for WindowData");
            }

            int pos = offset + 18 + windowDataOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for WindowData");
            }

            int windowDataLen = VarInt.peek(buffer, pos);
            if (windowDataLen < 0) {
               return ValidationResult.error("Invalid string length for WindowData");
            }

            if (windowDataLen > 4096000) {
               return ValidationResult.error("WindowData exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += windowDataLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading WindowData");
            }
         }

         if ((nullBits & 2) != 0) {
            int inventoryOffset = buffer.getIntLE(offset + 10);
            if (inventoryOffset < 0) {
               return ValidationResult.error("Invalid offset for Inventory");
            }

            int posx = offset + 18 + inventoryOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Inventory");
            }

            ValidationResult inventoryResult = InventorySection.validateStructure(buffer, posx);
            if (!inventoryResult.isValid()) {
               return ValidationResult.error("Invalid Inventory: " + inventoryResult.error());
            }

            posx += InventorySection.computeBytesConsumed(buffer, posx);
         }

         if ((nullBits & 4) != 0) {
            int extraResourcesOffset = buffer.getIntLE(offset + 14);
            if (extraResourcesOffset < 0) {
               return ValidationResult.error("Invalid offset for ExtraResources");
            }

            int posxx = offset + 18 + extraResourcesOffset;
            if (posxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for ExtraResources");
            }

            ValidationResult extraResourcesResult = ExtraResources.validateStructure(buffer, posxx);
            if (!extraResourcesResult.isValid()) {
               return ValidationResult.error("Invalid ExtraResources: " + extraResourcesResult.error());
            }

            posxx += ExtraResources.computeBytesConsumed(buffer, posxx);
         }

         return ValidationResult.OK;
      }
   }

   public OpenWindow clone() {
      OpenWindow copy = new OpenWindow();
      copy.id = this.id;
      copy.windowType = this.windowType;
      copy.windowData = this.windowData;
      copy.inventory = this.inventory != null ? this.inventory.clone() : null;
      copy.extraResources = this.extraResources != null ? this.extraResources.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof OpenWindow other)
            ? false
            : this.id == other.id
               && Objects.equals(this.windowType, other.windowType)
               && Objects.equals(this.windowData, other.windowData)
               && Objects.equals(this.inventory, other.inventory)
               && Objects.equals(this.extraResources, other.extraResources);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.id, this.windowType, this.windowData, this.inventory, this.extraResources);
   }
}
