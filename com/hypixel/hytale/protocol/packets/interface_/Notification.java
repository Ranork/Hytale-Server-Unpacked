package com.hypixel.hytale.protocol.packets.interface_;

import com.hypixel.hytale.protocol.FormattedMessage;
import com.hypixel.hytale.protocol.ItemWithAllMetadata;
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

public class Notification implements Packet, ToClientPacket {
   public static final int PACKET_ID = 212;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 2;
   public static final int VARIABLE_FIELD_COUNT = 4;
   public static final int VARIABLE_BLOCK_START = 18;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public FormattedMessage message;
   @Nullable
   public FormattedMessage secondaryMessage;
   @Nullable
   public String icon;
   @Nullable
   public ItemWithAllMetadata item;
   @Nonnull
   public NotificationStyle style = NotificationStyle.Default;

   @Override
   public int getId() {
      return 212;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public Notification() {
   }

   public Notification(
      @Nullable FormattedMessage message,
      @Nullable FormattedMessage secondaryMessage,
      @Nullable String icon,
      @Nullable ItemWithAllMetadata item,
      @Nonnull NotificationStyle style
   ) {
      this.message = message;
      this.secondaryMessage = secondaryMessage;
      this.icon = icon;
      this.item = item;
      this.style = style;
   }

   public Notification(@Nonnull Notification other) {
      this.message = other.message;
      this.secondaryMessage = other.secondaryMessage;
      this.icon = other.icon;
      this.item = other.item;
      this.style = other.style;
   }

   @Nonnull
   public static Notification deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 18) {
         throw ProtocolException.bufferTooSmall("Notification", 18, buf.readableBytes() - offset);
      } else {
         Notification obj = new Notification();
         byte nullBits = buf.getByte(offset);
         obj.style = NotificationStyle.fromValue(buf.getByte(offset + 1));
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 2);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 18) {
               throw ProtocolException.invalidOffset("Message", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 18 + varPosBase0;
            obj.message = FormattedMessage.deserialize(buf, varPos0);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 6);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 18) {
               throw ProtocolException.invalidOffset("SecondaryMessage", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 18 + varPosBase1;
            obj.secondaryMessage = FormattedMessage.deserialize(buf, varPos1);
         }

         if ((nullBits & 4) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 10);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 18) {
               throw ProtocolException.invalidOffset("Icon", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 18 + varPosBase2;
            int iconLen = VarInt.peek(buf, varPos2);
            if (iconLen < 0) {
               throw ProtocolException.invalidVarInt("Icon");
            }

            int iconVarIntLen = VarInt.size(iconLen);
            if (iconLen > 4096000) {
               throw ProtocolException.stringTooLong("Icon", iconLen, 4096000);
            }

            if (varPos2 + iconVarIntLen + iconLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Icon", varPos2 + iconVarIntLen + iconLen, buf.readableBytes());
            }

            obj.icon = PacketIO.readVarString(buf, varPos2, PacketIO.UTF8);
         }

         if ((nullBits & 8) != 0) {
            int varPosBase3 = buf.getIntLE(offset + 14);
            if (varPosBase3 < 0 || varPosBase3 > buf.writerIndex() - offset - 18) {
               throw ProtocolException.invalidOffset("Item", varPosBase3, buf.readableBytes());
            }

            int varPos3 = offset + 18 + varPosBase3;
            obj.item = ItemWithAllMetadata.deserialize(buf, varPos3);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 18;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 2);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 18) {
            throw ProtocolException.invalidOffset("Message", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 18 + fieldOffset0;
         pos0 += FormattedMessage.computeBytesConsumed(buf, pos0);
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 6);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 18) {
            throw ProtocolException.invalidOffset("SecondaryMessage", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 18 + fieldOffset1;
         pos1 += FormattedMessage.computeBytesConsumed(buf, pos1);
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 10);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 18) {
            throw ProtocolException.invalidOffset("Icon", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 18 + fieldOffset2;
         int sl = VarInt.peek(buf, pos2);
         pos2 += VarInt.size(sl) + sl;
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 14);
         if (fieldOffset3 < 0 || fieldOffset3 > buf.writerIndex() - offset - 18) {
            throw ProtocolException.invalidOffset("Item", fieldOffset3, maxEnd);
         }

         int pos3 = offset + 18 + fieldOffset3;
         pos3 += ItemWithAllMetadata.computeBytesConsumed(buf, pos3);
         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 18L;
   }

   @Nullable
   public static FormattedMessage getMessage(MemorySegment mem) {
      return getMessage(mem, 0);
   }

   @Nullable
   public static FormattedMessage getMessage(MemorySegment mem, int offset) {
      return hasMessage(mem, offset) ? FormattedMessage.toObject(mem, offset + getValidatedOffset(mem, offset, 2, 18, "Message")) : null;
   }

   @Nullable
   public static FormattedMessage getSecondaryMessage(MemorySegment mem) {
      return getSecondaryMessage(mem, 0);
   }

   @Nullable
   public static FormattedMessage getSecondaryMessage(MemorySegment mem, int offset) {
      return hasSecondaryMessage(mem, offset) ? FormattedMessage.toObject(mem, offset + getValidatedOffset(mem, offset, 6, 18, "SecondaryMessage")) : null;
   }

   @Nullable
   public static String getIcon(MemorySegment mem) {
      return getIcon(mem, 0);
   }

   @Nullable
   public static String getIcon(MemorySegment mem, int offset) {
      return hasIcon(mem, offset)
         ? PacketIO.readVarString("Icon", mem, offset + getValidatedOffset(mem, offset, 10, 18, "Icon"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static ItemWithAllMetadata getItem(MemorySegment mem) {
      return getItem(mem, 0);
   }

   @Nullable
   public static ItemWithAllMetadata getItem(MemorySegment mem, int offset) {
      return hasItem(mem, offset) ? ItemWithAllMetadata.toObject(mem, offset + getValidatedOffset(mem, offset, 14, 18, "Item")) : null;
   }

   public static NotificationStyle getStyle(MemorySegment mem) {
      return getStyle(mem, 0);
   }

   public static NotificationStyle getStyle(MemorySegment mem, int offset) {
      return NotificationStyle.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1)));
   }

   public static boolean hasMessage(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasSecondaryMessage(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasIcon(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasItem(MemorySegment mem, int offset) {
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

   public static Notification toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static Notification toObject(MemorySegment mem, int offset) {
      if (offset + 18 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("Notification", offset + 18, (int)mem.byteSize());
      } else {
         return new Notification(
            hasMessage(mem, offset) ? FormattedMessage.toObject(mem, offset + getValidatedOffset(mem, offset, 2, 18, "Message")) : null,
            hasSecondaryMessage(mem, offset) ? FormattedMessage.toObject(mem, offset + getValidatedOffset(mem, offset, 6, 18, "SecondaryMessage")) : null,
            hasIcon(mem, offset) ? PacketIO.readVarString("Icon", mem, offset + getValidatedOffset(mem, offset, 10, 18, "Icon"), 4096000, PacketIO.UTF8) : null,
            hasItem(mem, offset) ? ItemWithAllMetadata.toObject(mem, offset + getValidatedOffset(mem, offset, 14, 18, "Item")) : null,
            NotificationStyle.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1)))
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.message != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.secondaryMessage != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.icon != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.item != null) {
         nullBits = (byte)(nullBits | 8);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.style.getValue());
      int messageOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int secondaryMessageOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int iconOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int itemOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.message != null) {
         buf.setIntLE(messageOffsetSlot, buf.writerIndex() - varBlockStart);
         this.message.serialize(buf);
      } else {
         buf.setIntLE(messageOffsetSlot, -1);
      }

      if (this.secondaryMessage != null) {
         buf.setIntLE(secondaryMessageOffsetSlot, buf.writerIndex() - varBlockStart);
         this.secondaryMessage.serialize(buf);
      } else {
         buf.setIntLE(secondaryMessageOffsetSlot, -1);
      }

      if (this.icon != null) {
         buf.setIntLE(iconOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.icon, 4096000);
      } else {
         buf.setIntLE(iconOffsetSlot, -1);
      }

      if (this.item != null) {
         buf.setIntLE(itemOffsetSlot, buf.writerIndex() - varBlockStart);
         this.item.serialize(buf);
      } else {
         buf.setIntLE(itemOffsetSlot, -1);
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.message != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.secondaryMessage != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.icon != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.item != null) {
         nullBits = (byte)(nullBits | 8);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.style.getValue());
      int varOffset = offset + 18;
      if (this.message != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 2), varOffset - offset - 18);
         varOffset += this.message.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 2), -1);
      }

      if (this.secondaryMessage != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 6), varOffset - offset - 18);
         varOffset += this.secondaryMessage.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 6), -1);
      }

      if (this.icon != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 10), varOffset - offset - 18);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.icon, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 10), -1);
      }

      if (this.item != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 14), varOffset - offset - 18);
         varOffset += this.item.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 14), -1);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 18;
      if (this.message != null) {
         size += this.message.computeSize();
      }

      if (this.secondaryMessage != null) {
         size += this.secondaryMessage.computeSize();
      }

      if (this.icon != null) {
         size += PacketIO.stringSize(this.icon);
      }

      if (this.item != null) {
         size += this.item.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 18) {
         return ValidationResult.error("Buffer too small: expected at least 18 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int v = buffer.getByte(offset + 1) & 255;
         if (v >= 4) {
            return ValidationResult.error("Invalid NotificationStyle value for Style");
         } else {
            if ((nullBits & 1) != 0) {
               v = buffer.getIntLE(offset + 2);
               if (v < 0 || v > buffer.writerIndex() - offset - 18) {
                  return ValidationResult.error("Invalid offset for Message");
               }

               int pos = offset + 18 + v;
               ValidationResult messageResult = FormattedMessage.validateStructure(buffer, pos);
               if (!messageResult.isValid()) {
                  return ValidationResult.error("Invalid Message: " + messageResult.error());
               }

               pos += FormattedMessage.computeBytesConsumed(buffer, pos);
            }

            if ((nullBits & 2) != 0) {
               v = buffer.getIntLE(offset + 6);
               if (v < 0 || v > buffer.writerIndex() - offset - 18) {
                  return ValidationResult.error("Invalid offset for SecondaryMessage");
               }

               int pos = offset + 18 + v;
               ValidationResult secondaryMessageResult = FormattedMessage.validateStructure(buffer, pos);
               if (!secondaryMessageResult.isValid()) {
                  return ValidationResult.error("Invalid SecondaryMessage: " + secondaryMessageResult.error());
               }

               pos += FormattedMessage.computeBytesConsumed(buffer, pos);
            }

            if ((nullBits & 4) != 0) {
               v = buffer.getIntLE(offset + 10);
               if (v < 0 || v > buffer.writerIndex() - offset - 18) {
                  return ValidationResult.error("Invalid offset for Icon");
               }

               int pos = offset + 18 + v;
               int iconLen = VarInt.peek(buffer, pos);
               if (iconLen < 0) {
                  return ValidationResult.error("Invalid string length for Icon");
               }

               if (iconLen > 4096000) {
                  return ValidationResult.error("Icon exceeds max length 4096000");
               }

               pos += VarInt.size(iconLen);
               pos += iconLen;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading Icon");
               }
            }

            if ((nullBits & 8) != 0) {
               v = buffer.getIntLE(offset + 14);
               if (v < 0 || v > buffer.writerIndex() - offset - 18) {
                  return ValidationResult.error("Invalid offset for Item");
               }

               int posx = offset + 18 + v;
               ValidationResult itemResult = ItemWithAllMetadata.validateStructure(buffer, posx);
               if (!itemResult.isValid()) {
                  return ValidationResult.error("Invalid Item: " + itemResult.error());
               }

               posx += ItemWithAllMetadata.computeBytesConsumed(buffer, posx);
            }

            return ValidationResult.OK;
         }
      }
   }

   public Notification clone() {
      Notification copy = new Notification();
      copy.message = this.message != null ? this.message.clone() : null;
      copy.secondaryMessage = this.secondaryMessage != null ? this.secondaryMessage.clone() : null;
      copy.icon = this.icon;
      copy.item = this.item != null ? this.item.clone() : null;
      copy.style = this.style;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof Notification other)
            ? false
            : Objects.equals(this.message, other.message)
               && Objects.equals(this.secondaryMessage, other.secondaryMessage)
               && Objects.equals(this.icon, other.icon)
               && Objects.equals(this.item, other.item)
               && Objects.equals(this.style, other.style);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.message, this.secondaryMessage, this.icon, this.item, this.style);
   }
}
