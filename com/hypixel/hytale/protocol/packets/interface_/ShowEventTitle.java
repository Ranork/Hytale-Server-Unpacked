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
      ShowEventTitle obj = new ShowEventTitle();
      byte nullBits = buf.getByte(offset);
      obj.fadeInDuration = buf.getFloatLE(offset + 1);
      obj.fadeOutDuration = buf.getFloatLE(offset + 5);
      obj.duration = buf.getFloatLE(offset + 9);
      obj.isMajor = buf.getByte(offset + 13) != 0;
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 26 + buf.getIntLE(offset + 14);
         int iconLen = VarInt.peek(buf, varPos0);
         if (iconLen < 0) {
            throw ProtocolException.negativeLength("Icon", iconLen);
         }

         if (iconLen > 4096000) {
            throw ProtocolException.stringTooLong("Icon", iconLen, 4096000);
         }

         obj.icon = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 26 + buf.getIntLE(offset + 18);
         obj.primaryTitle = FormattedMessage.deserialize(buf, varPos1);
      }

      if ((nullBits & 4) != 0) {
         int varPos2 = offset + 26 + buf.getIntLE(offset + 22);
         obj.secondaryTitle = FormattedMessage.deserialize(buf, varPos2);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 26;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 14);
         int pos0 = offset + 26 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 18);
         int pos1 = offset + 26 + fieldOffset1;
         pos1 += FormattedMessage.computeBytesConsumed(buf, pos1);
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 22);
         int pos2 = offset + 26 + fieldOffset2;
         pos2 += FormattedMessage.computeBytesConsumed(buf, pos2);
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
            if (iconOffset < 0) {
               return ValidationResult.error("Invalid offset for Icon");
            }

            int pos = offset + 26 + iconOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Icon");
            }

            int iconLen = VarInt.peek(buffer, pos);
            if (iconLen < 0) {
               return ValidationResult.error("Invalid string length for Icon");
            }

            if (iconLen > 4096000) {
               return ValidationResult.error("Icon exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += iconLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Icon");
            }
         }

         if ((nullBits & 2) != 0) {
            int primaryTitleOffset = buffer.getIntLE(offset + 18);
            if (primaryTitleOffset < 0) {
               return ValidationResult.error("Invalid offset for PrimaryTitle");
            }

            int posx = offset + 26 + primaryTitleOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for PrimaryTitle");
            }

            ValidationResult primaryTitleResult = FormattedMessage.validateStructure(buffer, posx);
            if (!primaryTitleResult.isValid()) {
               return ValidationResult.error("Invalid PrimaryTitle: " + primaryTitleResult.error());
            }

            posx += FormattedMessage.computeBytesConsumed(buffer, posx);
         }

         if ((nullBits & 4) != 0) {
            int secondaryTitleOffset = buffer.getIntLE(offset + 22);
            if (secondaryTitleOffset < 0) {
               return ValidationResult.error("Invalid offset for SecondaryTitle");
            }

            int posxx = offset + 26 + secondaryTitleOffset;
            if (posxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for SecondaryTitle");
            }

            ValidationResult secondaryTitleResult = FormattedMessage.validateStructure(buffer, posxx);
            if (!secondaryTitleResult.isValid()) {
               return ValidationResult.error("Invalid SecondaryTitle: " + secondaryTitleResult.error());
            }

            posxx += FormattedMessage.computeBytesConsumed(buffer, posxx);
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
