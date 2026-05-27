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
      if (buf.readableBytes() - offset < 13) {
         throw ProtocolException.bufferTooSmall("KillFeedMessage", 13, buf.readableBytes() - offset);
      } else {
         KillFeedMessage obj = new KillFeedMessage();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 1);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 13) {
               throw ProtocolException.invalidOffset("Killer", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 13 + varPosBase0;
            obj.killer = FormattedMessage.deserialize(buf, varPos0);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 5);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 13) {
               throw ProtocolException.invalidOffset("Decedent", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 13 + varPosBase1;
            obj.decedent = FormattedMessage.deserialize(buf, varPos1);
         }

         if ((nullBits & 4) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 9);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 13) {
               throw ProtocolException.invalidOffset("Icon", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 13 + varPosBase2;
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

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 13;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 1);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 13) {
            throw ProtocolException.invalidOffset("Killer", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 13 + fieldOffset0;
         pos0 += FormattedMessage.computeBytesConsumed(buf, pos0);
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 13) {
            throw ProtocolException.invalidOffset("Decedent", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 13 + fieldOffset1;
         pos1 += FormattedMessage.computeBytesConsumed(buf, pos1);
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 9);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 13) {
            throw ProtocolException.invalidOffset("Icon", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 13 + fieldOffset2;
         int sl = VarInt.peek(buf, pos2);
         pos2 += VarInt.size(sl) + sl;
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 13L;
   }

   @Nullable
   public static FormattedMessage getKiller(MemorySegment mem) {
      return getKiller(mem, 0);
   }

   @Nullable
   public static FormattedMessage getKiller(MemorySegment mem, int offset) {
      return hasKiller(mem, offset) ? FormattedMessage.toObject(mem, offset + getValidatedOffset(mem, offset, 1, 13, "Killer")) : null;
   }

   @Nullable
   public static FormattedMessage getDecedent(MemorySegment mem) {
      return getDecedent(mem, 0);
   }

   @Nullable
   public static FormattedMessage getDecedent(MemorySegment mem, int offset) {
      return hasDecedent(mem, offset) ? FormattedMessage.toObject(mem, offset + getValidatedOffset(mem, offset, 5, 13, "Decedent")) : null;
   }

   @Nullable
   public static String getIcon(MemorySegment mem) {
      return getIcon(mem, 0);
   }

   @Nullable
   public static String getIcon(MemorySegment mem, int offset) {
      return hasIcon(mem, offset) ? PacketIO.readVarString("Icon", mem, offset + getValidatedOffset(mem, offset, 9, 13, "Icon"), 4096000, PacketIO.UTF8) : null;
   }

   public static boolean hasKiller(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasDecedent(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasIcon(MemorySegment mem, int offset) {
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

   public static KillFeedMessage toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static KillFeedMessage toObject(MemorySegment mem, int offset) {
      if (offset + 13 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("KillFeedMessage", offset + 13, (int)mem.byteSize());
      } else {
         return new KillFeedMessage(
            hasKiller(mem, offset) ? FormattedMessage.toObject(mem, offset + getValidatedOffset(mem, offset, 1, 13, "Killer")) : null,
            hasDecedent(mem, offset) ? FormattedMessage.toObject(mem, offset + getValidatedOffset(mem, offset, 5, 13, "Decedent")) : null,
            hasIcon(mem, offset) ? PacketIO.readVarString("Icon", mem, offset + getValidatedOffset(mem, offset, 9, 13, "Icon"), 4096000, PacketIO.UTF8) : null
         );
      }
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
   public int serialize(@Nonnull MemorySegment mem, int offset) {
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

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 13;
      if (this.killer != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), varOffset - offset - 13);
         varOffset += this.killer.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), -1);
      }

      if (this.decedent != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), varOffset - offset - 13);
         varOffset += this.decedent.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), -1);
      }

      if (this.icon != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), varOffset - offset - 13);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.icon, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), -1);
      }

      return varOffset - offset;
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
            if (killerOffset < 0 || killerOffset > buffer.writerIndex() - offset - 13) {
               return ValidationResult.error("Invalid offset for Killer");
            }

            int pos = offset + 13 + killerOffset;
            ValidationResult killerResult = FormattedMessage.validateStructure(buffer, pos);
            if (!killerResult.isValid()) {
               return ValidationResult.error("Invalid Killer: " + killerResult.error());
            }

            pos += FormattedMessage.computeBytesConsumed(buffer, pos);
         }

         if ((nullBits & 2) != 0) {
            int decedentOffset = buffer.getIntLE(offset + 5);
            if (decedentOffset < 0 || decedentOffset > buffer.writerIndex() - offset - 13) {
               return ValidationResult.error("Invalid offset for Decedent");
            }

            int pos = offset + 13 + decedentOffset;
            ValidationResult decedentResult = FormattedMessage.validateStructure(buffer, pos);
            if (!decedentResult.isValid()) {
               return ValidationResult.error("Invalid Decedent: " + decedentResult.error());
            }

            pos += FormattedMessage.computeBytesConsumed(buffer, pos);
         }

         if ((nullBits & 4) != 0) {
            int iconOffset = buffer.getIntLE(offset + 9);
            if (iconOffset < 0 || iconOffset > buffer.writerIndex() - offset - 13) {
               return ValidationResult.error("Invalid offset for Icon");
            }

            int pos = offset + 13 + iconOffset;
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
