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
      if (buf.readableBytes() - offset < 18) {
         throw ProtocolException.bufferTooSmall("OpenWindow", 18, buf.readableBytes() - offset);
      } else {
         OpenWindow obj = new OpenWindow();
         byte nullBits = buf.getByte(offset);
         obj.id = buf.getIntLE(offset + 1);
         obj.windowType = WindowType.fromValue(buf.getByte(offset + 5));
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 6);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 18) {
               throw ProtocolException.invalidOffset("WindowData", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 18 + varPosBase0;
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
            int varPosBase1 = buf.getIntLE(offset + 10);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 18) {
               throw ProtocolException.invalidOffset("Inventory", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 18 + varPosBase1;
            obj.inventory = InventorySection.deserialize(buf, varPos1);
         }

         if ((nullBits & 4) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 14);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 18) {
               throw ProtocolException.invalidOffset("ExtraResources", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 18 + varPosBase2;
            obj.extraResources = ExtraResources.deserialize(buf, varPos2);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 18;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 6);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 18) {
            throw ProtocolException.invalidOffset("WindowData", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 18 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 10);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 18) {
            throw ProtocolException.invalidOffset("Inventory", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 18 + fieldOffset1;
         pos1 += InventorySection.computeBytesConsumed(buf, pos1);
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 14);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 18) {
            throw ProtocolException.invalidOffset("ExtraResources", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 18 + fieldOffset2;
         pos2 += ExtraResources.computeBytesConsumed(buf, pos2);
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 18L;
   }

   public static int getId(MemorySegment mem) {
      return getId(mem, 0);
   }

   public static int getId(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   public static WindowType getWindowType(MemorySegment mem) {
      return getWindowType(mem, 0);
   }

   public static WindowType getWindowType(MemorySegment mem, int offset) {
      return WindowType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 5)));
   }

   @Nullable
   public static String getWindowData(MemorySegment mem) {
      return getWindowData(mem, 0);
   }

   @Nullable
   public static String getWindowData(MemorySegment mem, int offset) {
      return hasWindowData(mem, offset)
         ? PacketIO.readVarString("WindowData", mem, offset + getValidatedOffset(mem, offset, 6, 18, "WindowData"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static InventorySection getInventory(MemorySegment mem) {
      return getInventory(mem, 0);
   }

   @Nullable
   public static InventorySection getInventory(MemorySegment mem, int offset) {
      return hasInventory(mem, offset) ? InventorySection.toObject(mem, offset + getValidatedOffset(mem, offset, 10, 18, "Inventory")) : null;
   }

   @Nullable
   public static ExtraResources getExtraResources(MemorySegment mem) {
      return getExtraResources(mem, 0);
   }

   @Nullable
   public static ExtraResources getExtraResources(MemorySegment mem, int offset) {
      return hasExtraResources(mem, offset) ? ExtraResources.toObject(mem, offset + getValidatedOffset(mem, offset, 14, 18, "ExtraResources")) : null;
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

   public static OpenWindow toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static OpenWindow toObject(MemorySegment mem, int offset) {
      if (offset + 18 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("OpenWindow", offset + 18, (int)mem.byteSize());
      } else {
         return new OpenWindow(
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1)),
            WindowType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 5))),
            hasWindowData(mem, offset)
               ? PacketIO.readVarString("WindowData", mem, offset + getValidatedOffset(mem, offset, 6, 18, "WindowData"), 4096000, PacketIO.UTF8)
               : null,
            hasInventory(mem, offset) ? InventorySection.toObject(mem, offset + getValidatedOffset(mem, offset, 10, 18, "Inventory")) : null,
            hasExtraResources(mem, offset) ? ExtraResources.toObject(mem, offset + getValidatedOffset(mem, offset, 14, 18, "ExtraResources")) : null
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
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 5), (byte)this.windowType.getValue());
      int varOffset = offset + 18;
      if (this.windowData != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 6), varOffset - offset - 18);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.windowData, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 6), -1);
      }

      if (this.inventory != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 10), varOffset - offset - 18);
         varOffset += this.inventory.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 10), -1);
      }

      if (this.extraResources != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 14), varOffset - offset - 18);
         varOffset += this.extraResources.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 14), -1);
      }

      return varOffset - offset;
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
         int v = buffer.getByte(offset + 5) & 255;
         if (v >= 7) {
            return ValidationResult.error("Invalid WindowType value for WindowType");
         } else {
            if ((nullBits & 1) != 0) {
               v = buffer.getIntLE(offset + 6);
               if (v < 0 || v > buffer.writerIndex() - offset - 18) {
                  return ValidationResult.error("Invalid offset for WindowData");
               }

               int pos = offset + 18 + v;
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
               v = buffer.getIntLE(offset + 10);
               if (v < 0 || v > buffer.writerIndex() - offset - 18) {
                  return ValidationResult.error("Invalid offset for Inventory");
               }

               int posx = offset + 18 + v;
               ValidationResult inventoryResult = InventorySection.validateStructure(buffer, posx);
               if (!inventoryResult.isValid()) {
                  return ValidationResult.error("Invalid Inventory: " + inventoryResult.error());
               }

               posx += InventorySection.computeBytesConsumed(buffer, posx);
            }

            if ((nullBits & 4) != 0) {
               v = buffer.getIntLE(offset + 14);
               if (v < 0 || v > buffer.writerIndex() - offset - 18) {
                  return ValidationResult.error("Invalid offset for ExtraResources");
               }

               int posx = offset + 18 + v;
               ValidationResult extraResourcesResult = ExtraResources.validateStructure(buffer, posx);
               if (!extraResourcesResult.isValid()) {
                  return ValidationResult.error("Invalid ExtraResources: " + extraResourcesResult.error());
               }

               posx += ExtraResources.computeBytesConsumed(buffer, posx);
            }

            return ValidationResult.OK;
         }
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
