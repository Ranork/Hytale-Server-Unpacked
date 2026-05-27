package com.hypixel.hytale.protocol.packets.interface_;

import com.hypixel.hytale.protocol.FormattedMessage;
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

public class ShowEventTitle implements Packet, ToClientPacket {
   public static final int PACKET_ID = 214;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 14;
   public static final int VARIABLE_FIELD_COUNT = 3;
   public static final int VARIABLE_BLOCK_START = 26;
   public static final int MAX_SIZE = 1677721600;
   public float fadeInDuration;
   public float fadeOutDuration;
   public float duration;
   @Nullable
   public String icon;
   public boolean isMajor;
   @Nullable
   public FormattedMessage primaryTitle;
   @Nullable
   public FormattedMessage secondaryTitle;

   @Override
   public int getId() {
      return 214;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public ShowEventTitle() {
   }

   public ShowEventTitle(
      float fadeInDuration,
      float fadeOutDuration,
      float duration,
      @Nullable String icon,
      boolean isMajor,
      @Nullable FormattedMessage primaryTitle,
      @Nullable FormattedMessage secondaryTitle
   ) {
      this.fadeInDuration = fadeInDuration;
      this.fadeOutDuration = fadeOutDuration;
      this.duration = duration;
      this.icon = icon;
      this.isMajor = isMajor;
      this.primaryTitle = primaryTitle;
      this.secondaryTitle = secondaryTitle;
   }

   public ShowEventTitle(@Nonnull ShowEventTitle other) {
      this.fadeInDuration = other.fadeInDuration;
      this.fadeOutDuration = other.fadeOutDuration;
      this.duration = other.duration;
      this.icon = other.icon;
      this.isMajor = other.isMajor;
      this.primaryTitle = other.primaryTitle;
      this.secondaryTitle = other.secondaryTitle;
   }

   @Nonnull
   public static ShowEventTitle deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 26) {
         throw ProtocolException.bufferTooSmall("ShowEventTitle", 26, buf.readableBytes() - offset);
      } else {
         ShowEventTitle obj = new ShowEventTitle();
         byte nullBits = buf.getByte(offset);
         obj.fadeInDuration = buf.getFloatLE(offset + 1);
         obj.fadeOutDuration = buf.getFloatLE(offset + 5);
         obj.duration = buf.getFloatLE(offset + 9);
         obj.isMajor = buf.getByte(offset + 13) != 0;
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 14);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 26) {
               throw ProtocolException.invalidOffset("Icon", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 26 + varPosBase0;
            int iconLen = VarInt.peek(buf, varPos0);
            if (iconLen < 0) {
               throw ProtocolException.invalidVarInt("Icon");
            }

            int iconVarIntLen = VarInt.size(iconLen);
            if (iconLen > 4096000) {
               throw ProtocolException.stringTooLong("Icon", iconLen, 4096000);
            }

            if (varPos0 + iconVarIntLen + iconLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Icon", varPos0 + iconVarIntLen + iconLen, buf.readableBytes());
            }

            obj.icon = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 18);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 26) {
               throw ProtocolException.invalidOffset("PrimaryTitle", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 26 + varPosBase1;
            obj.primaryTitle = FormattedMessage.deserialize(buf, varPos1);
         }

         if ((nullBits & 4) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 22);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 26) {
               throw ProtocolException.invalidOffset("SecondaryTitle", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 26 + varPosBase2;
            obj.secondaryTitle = FormattedMessage.deserialize(buf, varPos2);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 26;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 14);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 26) {
            throw ProtocolException.invalidOffset("Icon", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 26 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 18);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 26) {
            throw ProtocolException.invalidOffset("PrimaryTitle", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 26 + fieldOffset1;
         pos1 += FormattedMessage.computeBytesConsumed(buf, pos1);
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 22);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 26) {
            throw ProtocolException.invalidOffset("SecondaryTitle", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 26 + fieldOffset2;
         pos2 += FormattedMessage.computeBytesConsumed(buf, pos2);
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 26L;
   }

   public static float getFadeInDuration(MemorySegment mem) {
      return getFadeInDuration(mem, 0);
   }

   public static float getFadeInDuration(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1));
   }

   public static float getFadeOutDuration(MemorySegment mem) {
      return getFadeOutDuration(mem, 0);
   }

   public static float getFadeOutDuration(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 5));
   }

   public static float getDuration(MemorySegment mem) {
      return getDuration(mem, 0);
   }

   public static float getDuration(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 9));
   }

   @Nullable
   public static String getIcon(MemorySegment mem) {
      return getIcon(mem, 0);
   }

   @Nullable
   public static String getIcon(MemorySegment mem, int offset) {
      return hasIcon(mem, offset)
         ? PacketIO.readVarString("Icon", mem, offset + getValidatedOffset(mem, offset, 14, 26, "Icon"), 4096000, PacketIO.UTF8)
         : null;
   }

   public static boolean getIsMajor(MemorySegment mem) {
      return getIsMajor(mem, 0);
   }

   public static boolean getIsMajor(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 13));
   }

   @Nullable
   public static FormattedMessage getPrimaryTitle(MemorySegment mem) {
      return getPrimaryTitle(mem, 0);
   }

   @Nullable
   public static FormattedMessage getPrimaryTitle(MemorySegment mem, int offset) {
      return hasPrimaryTitle(mem, offset) ? FormattedMessage.toObject(mem, offset + getValidatedOffset(mem, offset, 18, 26, "PrimaryTitle")) : null;
   }

   @Nullable
   public static FormattedMessage getSecondaryTitle(MemorySegment mem) {
      return getSecondaryTitle(mem, 0);
   }

   @Nullable
   public static FormattedMessage getSecondaryTitle(MemorySegment mem, int offset) {
      return hasSecondaryTitle(mem, offset) ? FormattedMessage.toObject(mem, offset + getValidatedOffset(mem, offset, 22, 26, "SecondaryTitle")) : null;
   }

   public static boolean hasIcon(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasPrimaryTitle(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasSecondaryTitle(MemorySegment mem, int offset) {
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

   public static ShowEventTitle toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ShowEventTitle toObject(MemorySegment mem, int offset) {
      if (offset + 26 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ShowEventTitle", offset + 26, (int)mem.byteSize());
      } else {
         return new ShowEventTitle(
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 5)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 9)),
            hasIcon(mem, offset) ? PacketIO.readVarString("Icon", mem, offset + getValidatedOffset(mem, offset, 14, 26, "Icon"), 4096000, PacketIO.UTF8) : null,
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 13)),
            hasPrimaryTitle(mem, offset) ? FormattedMessage.toObject(mem, offset + getValidatedOffset(mem, offset, 18, 26, "PrimaryTitle")) : null,
            hasSecondaryTitle(mem, offset) ? FormattedMessage.toObject(mem, offset + getValidatedOffset(mem, offset, 22, 26, "SecondaryTitle")) : null
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.icon != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.primaryTitle != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.secondaryTitle != null) {
         nullBits = (byte)(nullBits | 4);
      }

      buf.writeByte(nullBits);
      buf.writeFloatLE(this.fadeInDuration);
      buf.writeFloatLE(this.fadeOutDuration);
      buf.writeFloatLE(this.duration);
      buf.writeByte(this.isMajor ? 1 : 0);
      int iconOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int primaryTitleOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int secondaryTitleOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.icon != null) {
         buf.setIntLE(iconOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.icon, 4096000);
      } else {
         buf.setIntLE(iconOffsetSlot, -1);
      }

      if (this.primaryTitle != null) {
         buf.setIntLE(primaryTitleOffsetSlot, buf.writerIndex() - varBlockStart);
         this.primaryTitle.serialize(buf);
      } else {
         buf.setIntLE(primaryTitleOffsetSlot, -1);
      }

      if (this.secondaryTitle != null) {
         buf.setIntLE(secondaryTitleOffsetSlot, buf.writerIndex() - varBlockStart);
         this.secondaryTitle.serialize(buf);
      } else {
         buf.setIntLE(secondaryTitleOffsetSlot, -1);
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.icon != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.primaryTitle != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.secondaryTitle != null) {
         nullBits = (byte)(nullBits | 4);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 1), this.fadeInDuration);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 5), this.fadeOutDuration);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 9), this.duration);
      mem.set(PacketIO.PROTO_BOOL, offset + 13, this.isMajor);
      int varOffset = offset + 26;
      if (this.icon != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 14), varOffset - offset - 26);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.icon, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 14), -1);
      }

      if (this.primaryTitle != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 18), varOffset - offset - 26);
         varOffset += this.primaryTitle.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 18), -1);
      }

      if (this.secondaryTitle != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 22), varOffset - offset - 26);
         varOffset += this.secondaryTitle.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 22), -1);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 26;
      if (this.icon != null) {
         size += PacketIO.stringSize(this.icon);
      }

      if (this.primaryTitle != null) {
         size += this.primaryTitle.computeSize();
      }

      if (this.secondaryTitle != null) {
         size += this.secondaryTitle.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 26) {
         return ValidationResult.error("Buffer too small: expected at least 26 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int iconOffset = buffer.getIntLE(offset + 14);
            if (iconOffset < 0 || iconOffset > buffer.writerIndex() - offset - 26) {
               return ValidationResult.error("Invalid offset for Icon");
            }

            int pos = offset + 26 + iconOffset;
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

         if ((nullBits & 2) != 0) {
            int primaryTitleOffset = buffer.getIntLE(offset + 18);
            if (primaryTitleOffset < 0 || primaryTitleOffset > buffer.writerIndex() - offset - 26) {
               return ValidationResult.error("Invalid offset for PrimaryTitle");
            }

            int posx = offset + 26 + primaryTitleOffset;
            ValidationResult primaryTitleResult = FormattedMessage.validateStructure(buffer, posx);
            if (!primaryTitleResult.isValid()) {
               return ValidationResult.error("Invalid PrimaryTitle: " + primaryTitleResult.error());
            }

            posx += FormattedMessage.computeBytesConsumed(buffer, posx);
         }

         if ((nullBits & 4) != 0) {
            int secondaryTitleOffset = buffer.getIntLE(offset + 22);
            if (secondaryTitleOffset < 0 || secondaryTitleOffset > buffer.writerIndex() - offset - 26) {
               return ValidationResult.error("Invalid offset for SecondaryTitle");
            }

            int posx = offset + 26 + secondaryTitleOffset;
            ValidationResult secondaryTitleResult = FormattedMessage.validateStructure(buffer, posx);
            if (!secondaryTitleResult.isValid()) {
               return ValidationResult.error("Invalid SecondaryTitle: " + secondaryTitleResult.error());
            }

            posx += FormattedMessage.computeBytesConsumed(buffer, posx);
         }

         return ValidationResult.OK;
      }
   }

   public ShowEventTitle clone() {
      ShowEventTitle copy = new ShowEventTitle();
      copy.fadeInDuration = this.fadeInDuration;
      copy.fadeOutDuration = this.fadeOutDuration;
      copy.duration = this.duration;
      copy.icon = this.icon;
      copy.isMajor = this.isMajor;
      copy.primaryTitle = this.primaryTitle != null ? this.primaryTitle.clone() : null;
      copy.secondaryTitle = this.secondaryTitle != null ? this.secondaryTitle.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ShowEventTitle other)
            ? false
            : this.fadeInDuration == other.fadeInDuration
               && this.fadeOutDuration == other.fadeOutDuration
               && this.duration == other.duration
               && Objects.equals(this.icon, other.icon)
               && this.isMajor == other.isMajor
               && Objects.equals(this.primaryTitle, other.primaryTitle)
               && Objects.equals(this.secondaryTitle, other.secondaryTitle);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.fadeInDuration, this.fadeOutDuration, this.duration, this.icon, this.isMajor, this.primaryTitle, this.secondaryTitle);
   }
}
