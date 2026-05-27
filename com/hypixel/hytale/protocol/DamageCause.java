package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DamageCause {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 32768019;
   @Nullable
   public String id;
   @Nullable
   public String damageTextColor;

   public DamageCause() {
   }

   public DamageCause(@Nullable String id, @Nullable String damageTextColor) {
      this.id = id;
      this.damageTextColor = damageTextColor;
   }

   public DamageCause(@Nonnull DamageCause other) {
      this.id = other.id;
      this.damageTextColor = other.damageTextColor;
   }

   @Nonnull
   public static DamageCause deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 9) {
         throw ProtocolException.bufferTooSmall("DamageCause", 9, buf.readableBytes() - offset);
      } else {
         DamageCause obj = new DamageCause();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 1);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 9) {
               throw ProtocolException.invalidOffset("Id", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 9 + varPosBase0;
            int idLen = VarInt.peek(buf, varPos0);
            if (idLen < 0) {
               throw ProtocolException.invalidVarInt("Id");
            }

            int idVarIntLen = VarInt.size(idLen);
            if (idLen > 4096000) {
               throw ProtocolException.stringTooLong("Id", idLen, 4096000);
            }

            if (varPos0 + idVarIntLen + idLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Id", varPos0 + idVarIntLen + idLen, buf.readableBytes());
            }

            obj.id = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 5);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 9) {
               throw ProtocolException.invalidOffset("DamageTextColor", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 9 + varPosBase1;
            int damageTextColorLen = VarInt.peek(buf, varPos1);
            if (damageTextColorLen < 0) {
               throw ProtocolException.invalidVarInt("DamageTextColor");
            }

            int damageTextColorVarIntLen = VarInt.size(damageTextColorLen);
            if (damageTextColorLen > 4096000) {
               throw ProtocolException.stringTooLong("DamageTextColor", damageTextColorLen, 4096000);
            }

            if (varPos1 + damageTextColorVarIntLen + damageTextColorLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("DamageTextColor", varPos1 + damageTextColorVarIntLen + damageTextColorLen, buf.readableBytes());
            }

            obj.damageTextColor = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 9;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 1);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 9) {
            throw ProtocolException.invalidOffset("Id", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 9 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 9) {
            throw ProtocolException.invalidOffset("DamageTextColor", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 9 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(sl) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 9L;
   }

   @Nullable
   public static String getId(MemorySegment mem) {
      return getId(mem, 0);
   }

   @Nullable
   public static String getId(MemorySegment mem, int offset) {
      return hasId(mem, offset) ? PacketIO.readVarString("Id", mem, offset + getValidatedOffset(mem, offset, 1, 9, "Id"), 4096000, PacketIO.UTF8) : null;
   }

   @Nullable
   public static String getDamageTextColor(MemorySegment mem) {
      return getDamageTextColor(mem, 0);
   }

   @Nullable
   public static String getDamageTextColor(MemorySegment mem, int offset) {
      return hasDamageTextColor(mem, offset)
         ? PacketIO.readVarString("DamageTextColor", mem, offset + getValidatedOffset(mem, offset, 5, 9, "DamageTextColor"), 4096000, PacketIO.UTF8)
         : null;
   }

   public static boolean hasId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasDamageTextColor(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static DamageCause toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static DamageCause toObject(MemorySegment mem, int offset) {
      if (offset + 9 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("DamageCause", offset + 9, (int)mem.byteSize());
      } else {
         return new DamageCause(
            hasId(mem, offset) ? PacketIO.readVarString("Id", mem, offset + getValidatedOffset(mem, offset, 1, 9, "Id"), 4096000, PacketIO.UTF8) : null,
            hasDamageTextColor(mem, offset)
               ? PacketIO.readVarString("DamageTextColor", mem, offset + getValidatedOffset(mem, offset, 5, 9, "DamageTextColor"), 4096000, PacketIO.UTF8)
               : null
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.id != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.damageTextColor != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      int idOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int damageTextColorOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.id != null) {
         buf.setIntLE(idOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.id, 4096000);
      } else {
         buf.setIntLE(idOffsetSlot, -1);
      }

      if (this.damageTextColor != null) {
         buf.setIntLE(damageTextColorOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.damageTextColor, 4096000);
      } else {
         buf.setIntLE(damageTextColorOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.id != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.damageTextColor != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 9;
      if (this.id != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), varOffset - offset - 9);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.id, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), -1);
      }

      if (this.damageTextColor != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), varOffset - offset - 9);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.damageTextColor, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 9;
      if (this.id != null) {
         size += PacketIO.stringSize(this.id);
      }

      if (this.damageTextColor != null) {
         size += PacketIO.stringSize(this.damageTextColor);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 9) {
         return ValidationResult.error("Buffer too small: expected at least 9 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int idOffset = buffer.getIntLE(offset + 1);
            if (idOffset < 0 || idOffset > buffer.writerIndex() - offset - 9) {
               return ValidationResult.error("Invalid offset for Id");
            }

            int pos = offset + 9 + idOffset;
            int idLen = VarInt.peek(buffer, pos);
            if (idLen < 0) {
               return ValidationResult.error("Invalid string length for Id");
            }

            if (idLen > 4096000) {
               return ValidationResult.error("Id exceeds max length 4096000");
            }

            pos += VarInt.size(idLen);
            pos += idLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Id");
            }
         }

         if ((nullBits & 2) != 0) {
            int damageTextColorOffset = buffer.getIntLE(offset + 5);
            if (damageTextColorOffset < 0 || damageTextColorOffset > buffer.writerIndex() - offset - 9) {
               return ValidationResult.error("Invalid offset for DamageTextColor");
            }

            int posx = offset + 9 + damageTextColorOffset;
            int damageTextColorLen = VarInt.peek(buffer, posx);
            if (damageTextColorLen < 0) {
               return ValidationResult.error("Invalid string length for DamageTextColor");
            }

            if (damageTextColorLen > 4096000) {
               return ValidationResult.error("DamageTextColor exceeds max length 4096000");
            }

            posx += VarInt.size(damageTextColorLen);
            posx += damageTextColorLen;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading DamageTextColor");
            }
         }

         return ValidationResult.OK;
      }
   }

   public DamageCause clone() {
      DamageCause copy = new DamageCause();
      copy.id = this.id;
      copy.damageTextColor = this.damageTextColor;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof DamageCause other) ? false : Objects.equals(this.id, other.id) && Objects.equals(this.damageTextColor, other.damageTextColor);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.id, this.damageTextColor);
   }
}
