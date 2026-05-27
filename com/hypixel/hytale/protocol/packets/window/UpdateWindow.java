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
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UpdateWindow implements Packet, ToClientPacket {
   public static final int PACKET_ID = 201;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 5;
   public static final int VARIABLE_FIELD_COUNT = 3;
   public static final int VARIABLE_BLOCK_START = 17;
   public static final int MAX_SIZE = 1677721600;
   public int id;
   @Nullable
   public String windowData;
   @Nullable
   public InventorySection inventory;
   @Nullable
   public ExtraResources extraResources;

   @Override
   public int getId() {
      return 201;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateWindow() {
   }

   public UpdateWindow(int id, @Nullable String windowData, @Nullable InventorySection inventory, @Nullable ExtraResources extraResources) {
      this.id = id;
      this.windowData = windowData;
      this.inventory = inventory;
      this.extraResources = extraResources;
   }

   public UpdateWindow(@Nonnull UpdateWindow other) {
      this.id = other.id;
      this.windowData = other.windowData;
      this.inventory = other.inventory;
      this.extraResources = other.extraResources;
   }

   @Nonnull
   public static UpdateWindow deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 17) {
         throw ProtocolException.bufferTooSmall("UpdateWindow", 17, buf.readableBytes() - offset);
      } else {
         UpdateWindow obj = new UpdateWindow();
         byte nullBits = buf.getByte(offset);
         obj.id = buf.getIntLE(offset + 1);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 5);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 17) {
               throw ProtocolException.invalidOffset("WindowData", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 17 + varPosBase0;
            int windowDataLen = VarInt.peek(buf, varPos0);
            if (windowDataLen < 0) {
               throw ProtocolException.invalidVarInt("WindowData");
            }

            int windowDataVarIntLen = VarInt.size(windowDataLen);
            if (windowDataLen > 4096000) {
               throw ProtocolException.stringTooLong("WindowData", windowDataLen, 4096000);
            }

            if (varPos0 + windowDataVarIntLen + windowDataLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("WindowData", varPos0 + windowDataVarIntLen + windowDataLen, buf.readableBytes());
            }

            obj.windowData = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 9);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 17) {
               throw ProtocolException.invalidOffset("Inventory", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 17 + varPosBase1;
            obj.inventory = InventorySection.deserialize(buf, varPos1);
         }

         if ((nullBits & 4) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 13);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 17) {
               throw ProtocolException.invalidOffset("ExtraResources", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 17 + varPosBase2;
            obj.extraResources = ExtraResources.deserialize(buf, varPos2);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 17;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 5);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 17) {
            throw ProtocolException.invalidOffset("WindowData", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 17 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 9);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 17) {
            throw ProtocolException.invalidOffset("Inventory", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 17 + fieldOffset1;
         pos1 += InventorySection.computeBytesConsumed(buf, pos1);
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 13);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 17) {
            throw ProtocolException.invalidOffset("ExtraResources", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 17 + fieldOffset2;
         pos2 += ExtraResources.computeBytesConsumed(buf, pos2);
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 17L;
   }

   public static int getId(MemorySegment mem) {
      return getId(mem, 0);
   }

   public static int getId(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   @Nullable
   public static String getWindowData(MemorySegment mem) {
      return getWindowData(mem, 0);
   }

   @Nullable
   public static String getWindowData(MemorySegment mem, int offset) {
      return hasWindowData(mem, offset)
         ? PacketIO.readVarString("WindowData", mem, offset + getValidatedOffset(mem, offset, 5, 17, "WindowData"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static InventorySection getInventory(MemorySegment mem) {
      return getInventory(mem, 0);
   }

   @Nullable
   public static InventorySection getInventory(MemorySegment mem, int offset) {
      return hasInventory(mem, offset) ? InventorySection.toObject(mem, offset + getValidatedOffset(mem, offset, 9, 17, "Inventory")) : null;
   }

   @Nullable
   public static ExtraResources getExtraResources(MemorySegment mem) {
      return getExtraResources(mem, 0);
   }

   @Nullable
   public static ExtraResources getExtraResources(MemorySegment mem, int offset) {
      return hasExtraResources(mem, offset) ? ExtraResources.toObject(mem, offset + getValidatedOffset(mem, offset, 13, 17, "ExtraResources")) : null;
   }

   public static boolean hasWindowData(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasInventory(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasExtraResources(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static UpdateWindow toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UpdateWindow toObject(MemorySegment mem, int offset) {
      if (offset + 17 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UpdateWindow", offset + 17, (int)mem.byteSize());
      } else {
         return new UpdateWindow(
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1)),
            hasWindowData(mem, offset)
               ? PacketIO.readVarString("WindowData", mem, offset + getValidatedOffset(mem, offset, 5, 17, "WindowData"), 4096000, PacketIO.UTF8)
               : null,
            hasInventory(mem, offset) ? InventorySection.toObject(mem, offset + getValidatedOffset(mem, offset, 9, 17, "Inventory")) : null,
            hasExtraResources(mem, offset) ? ExtraResources.toObject(mem, offset + getValidatedOffset(mem, offset, 13, 17, "ExtraResources")) : null
         );
      }
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
   public int serialize(@Nonnull MemorySegment mem, int offset) {
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

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.id);
      int varOffset = offset + 17;
      if (this.windowData != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), varOffset - offset - 17);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.windowData, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), -1);
      }

      if (this.inventory != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), varOffset - offset - 17);
         varOffset += this.inventory.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), -1);
      }

      if (this.extraResources != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 13), varOffset - offset - 17);
         varOffset += this.extraResources.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 13), -1);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 17;
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
      if (buffer.readableBytes() - offset < 17) {
         return ValidationResult.error("Buffer too small: expected at least 17 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int windowDataOffset = buffer.getIntLE(offset + 5);
            if (windowDataOffset < 0 || windowDataOffset > buffer.writerIndex() - offset - 17) {
               return ValidationResult.error("Invalid offset for WindowData");
            }

            int pos = offset + 17 + windowDataOffset;
            int windowDataLen = VarInt.peek(buffer, pos);
            if (windowDataLen < 0) {
               return ValidationResult.error("Invalid string length for WindowData");
            }

            if (windowDataLen > 4096000) {
               return ValidationResult.error("WindowData exceeds max length 4096000");
            }

            pos += VarInt.size(windowDataLen);
            pos += windowDataLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading WindowData");
            }
         }

         if ((nullBits & 2) != 0) {
            int inventoryOffset = buffer.getIntLE(offset + 9);
            if (inventoryOffset < 0 || inventoryOffset > buffer.writerIndex() - offset - 17) {
               return ValidationResult.error("Invalid offset for Inventory");
            }

            int posx = offset + 17 + inventoryOffset;
            ValidationResult inventoryResult = InventorySection.validateStructure(buffer, posx);
            if (!inventoryResult.isValid()) {
               return ValidationResult.error("Invalid Inventory: " + inventoryResult.error());
            }

            posx += InventorySection.computeBytesConsumed(buffer, posx);
         }

         if ((nullBits & 4) != 0) {
            int extraResourcesOffset = buffer.getIntLE(offset + 13);
            if (extraResourcesOffset < 0 || extraResourcesOffset > buffer.writerIndex() - offset - 17) {
               return ValidationResult.error("Invalid offset for ExtraResources");
            }

            int posx = offset + 17 + extraResourcesOffset;
            ValidationResult extraResourcesResult = ExtraResources.validateStructure(buffer, posx);
            if (!extraResourcesResult.isValid()) {
               return ValidationResult.error("Invalid ExtraResources: " + extraResourcesResult.error());
            }

            posx += ExtraResources.computeBytesConsumed(buffer, posx);
         }

         return ValidationResult.OK;
      }
   }

   public UpdateWindow clone() {
      UpdateWindow copy = new UpdateWindow();
      copy.id = this.id;
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
         return !(obj instanceof UpdateWindow other)
            ? false
            : this.id == other.id
               && Objects.equals(this.windowData, other.windowData)
               && Objects.equals(this.inventory, other.inventory)
               && Objects.equals(this.extraResources, other.extraResources);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.id, this.windowData, this.inventory, this.extraResources);
   }
}
