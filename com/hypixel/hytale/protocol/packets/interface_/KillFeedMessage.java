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

public class KillFeedMessage implements Packet, ToClientPacket {
   public static final int PACKET_ID = 213;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 3;
   public static final int VARIABLE_BLOCK_START = 13;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public FormattedMessage killer;
   @Nullable
   public FormattedMessage decedent;
   @Nullable
   public String icon;

   @Override
   public int getId() {
      return 213;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public KillFeedMessage() {
   }

   public KillFeedMessage(@Nullable FormattedMessage killer, @Nullable FormattedMessage decedent, @Nullable String icon) {
      this.killer = killer;
      this.decedent = decedent;
      this.icon = icon;
   }

   public KillFeedMessage(@Nonnull KillFeedMessage other) {
      this.killer = other.killer;
      this.decedent = other.decedent;
      this.icon = other.icon;
   }

   @Nonnull
   public static KillFeedMessage deserialize(@Nonnull ByteBuf buf, int offset) {
      KillFeedMessage obj = new KillFeedMessage();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 13 + buf.getIntLE(offset + 1);
         obj.killer = FormattedMessage.deserialize(buf, varPos0);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 13 + buf.getIntLE(offset + 5);
         obj.decedent = FormattedMessage.deserialize(buf, varPos1);
      }

      if ((nullBits & 4) != 0) {
         int varPos2 = offset + 13 + buf.getIntLE(offset + 9);
         int iconLen = VarInt.peek(buf, varPos2);
         if (iconLen < 0) {
            throw ProtocolException.negativeLength("Icon", iconLen);
         }

         if (iconLen > 4096000) {
            throw ProtocolException.stringTooLong("Icon", iconLen, 4096000);
         }

         obj.icon = PacketIO.readVarString(buf, varPos2, PacketIO.UTF8);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 13;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 1);
         int pos0 = offset + 13 + fieldOffset0;
         pos0 += FormattedMessage.computeBytesConsumed(buf, pos0);
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         int pos1 = offset + 13 + fieldOffset1;
         pos1 += FormattedMessage.computeBytesConsumed(buf, pos1);
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 9);
         int pos2 = offset + 13 + fieldOffset2;
         int sl = VarInt.peek(buf, pos2);
         pos2 += VarInt.length(buf, pos2) + sl;
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
      if (this.killer != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.decedent != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.icon != null) {
         nullBits = (byte)(nullBits | 4);
      }

      buf.writeByte(nullBits);
      int killerOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int decedentOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int iconOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.killer != null) {
         buf.setIntLE(killerOffsetSlot, buf.writerIndex() - varBlockStart);
         this.killer.serialize(buf);
      } else {
         buf.setIntLE(killerOffsetSlot, -1);
      }

      if (this.decedent != null) {
         buf.setIntLE(decedentOffsetSlot, buf.writerIndex() - varBlockStart);
         this.decedent.serialize(buf);
      } else {
         buf.setIntLE(decedentOffsetSlot, -1);
      }

      if (this.icon != null) {
         buf.setIntLE(iconOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.icon, 4096000);
      } else {
         buf.setIntLE(iconOffsetSlot, -1);
      }
   }

   @Override
   public int computeSize() {
      int size = 13;
      if (this.killer != null) {
         size += this.killer.computeSize();
      }

      if (this.decedent != null) {
         size += this.decedent.computeSize();
      }

      if (this.icon != null) {
         size += PacketIO.stringSize(this.icon);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 13) {
         return ValidationResult.error("Buffer too small: expected at least 13 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int killerOffset = buffer.getIntLE(offset + 1);
            if (killerOffset < 0) {
               return ValidationResult.error("Invalid offset for Killer");
            }

            int pos = offset + 13 + killerOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Killer");
            }

            ValidationResult killerResult = FormattedMessage.validateStructure(buffer, pos);
            if (!killerResult.isValid()) {
               return ValidationResult.error("Invalid Killer: " + killerResult.error());
            }

            pos += FormattedMessage.computeBytesConsumed(buffer, pos);
         }

         if ((nullBits & 2) != 0) {
            int decedentOffset = buffer.getIntLE(offset + 5);
            if (decedentOffset < 0) {
               return ValidationResult.error("Invalid offset for Decedent");
            }

            int posx = offset + 13 + decedentOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Decedent");
            }

            ValidationResult decedentResult = FormattedMessage.validateStructure(buffer, posx);
            if (!decedentResult.isValid()) {
               return ValidationResult.error("Invalid Decedent: " + decedentResult.error());
            }

            posx += FormattedMessage.computeBytesConsumed(buffer, posx);
         }

         if ((nullBits & 4) != 0) {
            int iconOffset = buffer.getIntLE(offset + 9);
            if (iconOffset < 0) {
               return ValidationResult.error("Invalid offset for Icon");
            }

            int posxx = offset + 13 + iconOffset;
            if (posxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Icon");
            }

            int iconLen = VarInt.peek(buffer, posxx);
            if (iconLen < 0) {
               return ValidationResult.error("Invalid string length for Icon");
            }

            if (iconLen > 4096000) {
               return ValidationResult.error("Icon exceeds max length 4096000");
            }

            posxx += VarInt.length(buffer, posxx);
            posxx += iconLen;
            if (posxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Icon");
            }
         }

         return ValidationResult.OK;
      }
   }

   public KillFeedMessage clone() {
      KillFeedMessage copy = new KillFeedMessage();
      copy.killer = this.killer != null ? this.killer.clone() : null;
      copy.decedent = this.decedent != null ? this.decedent.clone() : null;
      copy.icon = this.icon;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof KillFeedMessage other)
            ? false
            : Objects.equals(this.killer, other.killer) && Objects.equals(this.decedent, other.decedent) && Objects.equals(this.icon, other.icon);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.killer, this.decedent, this.icon);
   }
}
