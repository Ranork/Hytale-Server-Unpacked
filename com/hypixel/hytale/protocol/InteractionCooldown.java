package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InteractionCooldown {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 8;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 16;
   public static final int MAX_SIZE = 32768026;
   @Nullable
   public String cooldownId;
   public float cooldown;
   public boolean clickBypass;
   @Nullable
   public float[] chargeTimes;
   public boolean skipCooldownReset;
   public boolean interruptRecharge;

   public InteractionCooldown() {
   }

   public InteractionCooldown(
      @Nullable String cooldownId, float cooldown, boolean clickBypass, @Nullable float[] chargeTimes, boolean skipCooldownReset, boolean interruptRecharge
   ) {
      this.cooldownId = cooldownId;
      this.cooldown = cooldown;
      this.clickBypass = clickBypass;
      this.chargeTimes = chargeTimes;
      this.skipCooldownReset = skipCooldownReset;
      this.interruptRecharge = interruptRecharge;
   }

   public InteractionCooldown(@Nonnull InteractionCooldown other) {
      this.cooldownId = other.cooldownId;
      this.cooldown = other.cooldown;
      this.clickBypass = other.clickBypass;
      this.chargeTimes = other.chargeTimes;
      this.skipCooldownReset = other.skipCooldownReset;
      this.interruptRecharge = other.interruptRecharge;
   }

   @Nonnull
   public static InteractionCooldown deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 16) {
         throw ProtocolException.bufferTooSmall("InteractionCooldown", 16, buf.readableBytes() - offset);
      } else {
         InteractionCooldown obj = new InteractionCooldown();
         byte nullBits = buf.getByte(offset);
         obj.cooldown = buf.getFloatLE(offset + 1);
         obj.clickBypass = buf.getByte(offset + 5) != 0;
         obj.skipCooldownReset = buf.getByte(offset + 6) != 0;
         obj.interruptRecharge = buf.getByte(offset + 7) != 0;
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 8);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 16) {
               throw ProtocolException.invalidOffset("CooldownId", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 16 + varPosBase0;
            int cooldownIdLen = VarInt.peek(buf, varPos0);
            if (cooldownIdLen < 0) {
               throw ProtocolException.invalidVarInt("CooldownId");
            }

            int cooldownIdVarIntLen = VarInt.size(cooldownIdLen);
            if (cooldownIdLen > 4096000) {
               throw ProtocolException.stringTooLong("CooldownId", cooldownIdLen, 4096000);
            }

            if (varPos0 + cooldownIdVarIntLen + cooldownIdLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("CooldownId", varPos0 + cooldownIdVarIntLen + cooldownIdLen, buf.readableBytes());
            }

            obj.cooldownId = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 12);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 16) {
               throw ProtocolException.invalidOffset("ChargeTimes", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 16 + varPosBase1;
            int chargeTimesCount = VarInt.peek(buf, varPos1);
            if (chargeTimesCount < 0) {
               throw ProtocolException.invalidVarInt("ChargeTimes");
            }

            int varIntLen = VarInt.size(chargeTimesCount);
            if (chargeTimesCount > 4096000) {
               throw ProtocolException.arrayTooLong("ChargeTimes", chargeTimesCount, 4096000);
            }

            if (varPos1 + varIntLen + chargeTimesCount * 4L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("ChargeTimes", varPos1 + varIntLen + chargeTimesCount * 4, buf.readableBytes());
            }

            obj.chargeTimes = new float[chargeTimesCount];

            for (int i = 0; i < chargeTimesCount; i++) {
               obj.chargeTimes[i] = buf.getFloatLE(varPos1 + varIntLen + i * 4);
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 16;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 8);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 16) {
            throw ProtocolException.invalidOffset("CooldownId", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 16 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 12);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 16) {
            throw ProtocolException.invalidOffset("ChargeTimes", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 16 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(arrLen) + arrLen * 4;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 16L;
   }

   @Nullable
   public static String getCooldownId(MemorySegment mem) {
      return getCooldownId(mem, 0);
   }

   @Nullable
   public static String getCooldownId(MemorySegment mem, int offset) {
      return hasCooldownId(mem, offset)
         ? PacketIO.readVarString("CooldownId", mem, offset + getValidatedOffset(mem, offset, 8, 16, "CooldownId"), 4096000, PacketIO.UTF8)
         : null;
   }

   public static float getCooldown(MemorySegment mem) {
      return getCooldown(mem, 0);
   }

   public static float getCooldown(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1));
   }

   public static boolean getClickBypass(MemorySegment mem) {
      return getClickBypass(mem, 0);
   }

   public static boolean getClickBypass(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 5));
   }

   @Nullable
   public static float[] getChargeTimes(MemorySegment mem) {
      return getChargeTimes(mem, 0);
   }

   @Nullable
   public static float[] getChargeTimes(MemorySegment mem, int offset) {
      if (!hasChargeTimes(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 12, 16, "ChargeTimes");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("ChargeTimes", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("ChargeTimes", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 4L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("ChargeTimes", off + lenOffset + len * 4, (int)mem.byteSize());
            } else {
               off += lenOffset;
               float[] data = new float[len];
               MemorySegment.copy(mem, PacketIO.PROTO_FLOAT, off, data, 0, len);
               return data;
            }
         }
      }
   }

   public static boolean getSkipCooldownReset(MemorySegment mem) {
      return getSkipCooldownReset(mem, 0);
   }

   public static boolean getSkipCooldownReset(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 6));
   }

   public static boolean getInterruptRecharge(MemorySegment mem) {
      return getInterruptRecharge(mem, 0);
   }

   public static boolean getInterruptRecharge(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 7));
   }

   public static boolean hasCooldownId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasChargeTimes(MemorySegment mem, int offset) {
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

   public static InteractionCooldown toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static InteractionCooldown toObject(MemorySegment mem, int offset) {
      if (offset + 16 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("InteractionCooldown", offset + 16, (int)mem.byteSize());
      } else {
         float[] chargeTimes = null;
         if (hasChargeTimes(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 12, 16, "ChargeTimes");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("ChargeTimes", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("ChargeTimes", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 4L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("ChargeTimes", off + lenOffset + len * 4, (int)mem.byteSize());
            }

            off += lenOffset;
            chargeTimes = new float[len];
            MemorySegment.copy(mem, PacketIO.PROTO_FLOAT, off, chargeTimes, 0, len);
         }

         return new InteractionCooldown(
            hasCooldownId(mem, offset)
               ? PacketIO.readVarString("CooldownId", mem, offset + getValidatedOffset(mem, offset, 8, 16, "CooldownId"), 4096000, PacketIO.UTF8)
               : null,
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 5)),
            chargeTimes,
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 6)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 7))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.cooldownId != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.chargeTimes != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      buf.writeFloatLE(this.cooldown);
      buf.writeByte(this.clickBypass ? 1 : 0);
      buf.writeByte(this.skipCooldownReset ? 1 : 0);
      buf.writeByte(this.interruptRecharge ? 1 : 0);
      int cooldownIdOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int chargeTimesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.cooldownId != null) {
         buf.setIntLE(cooldownIdOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.cooldownId, 4096000);
      } else {
         buf.setIntLE(cooldownIdOffsetSlot, -1);
      }

      if (this.chargeTimes != null) {
         buf.setIntLE(chargeTimesOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.chargeTimes.length > 4096000) {
            throw ProtocolException.arrayTooLong("ChargeTimes", this.chargeTimes.length, 4096000);
         }

         VarInt.write(buf, this.chargeTimes.length);

         for (float item : this.chargeTimes) {
            buf.writeFloatLE(item);
         }
      } else {
         buf.setIntLE(chargeTimesOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.cooldownId != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.chargeTimes != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 1), this.cooldown);
      mem.set(PacketIO.PROTO_BOOL, offset + 5, this.clickBypass);
      mem.set(PacketIO.PROTO_BOOL, offset + 6, this.skipCooldownReset);
      mem.set(PacketIO.PROTO_BOOL, offset + 7, this.interruptRecharge);
      int varOffset = offset + 16;
      if (this.cooldownId != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 8), varOffset - offset - 16);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.cooldownId, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 8), -1);
      }

      if (this.chargeTimes != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 12), varOffset - offset - 16);
         if (this.chargeTimes.length > 4096000) {
            throw ProtocolException.arrayTooLong("ChargeTimes", this.chargeTimes.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.chargeTimes.length);
         MemorySegment.copy(this.chargeTimes, 0, mem, PacketIO.PROTO_FLOAT, varOffset, this.chargeTimes.length);
         varOffset += this.chargeTimes.length * 4;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 12), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 16;
      if (this.cooldownId != null) {
         size += PacketIO.stringSize(this.cooldownId);
      }

      if (this.chargeTimes != null) {
         size += VarInt.size(this.chargeTimes.length) + this.chargeTimes.length * 4;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 16) {
         return ValidationResult.error("Buffer too small: expected at least 16 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int cooldownIdOffset = buffer.getIntLE(offset + 8);
            if (cooldownIdOffset < 0 || cooldownIdOffset > buffer.writerIndex() - offset - 16) {
               return ValidationResult.error("Invalid offset for CooldownId");
            }

            int pos = offset + 16 + cooldownIdOffset;
            int cooldownIdLen = VarInt.peek(buffer, pos);
            if (cooldownIdLen < 0) {
               return ValidationResult.error("Invalid string length for CooldownId");
            }

            if (cooldownIdLen > 4096000) {
               return ValidationResult.error("CooldownId exceeds max length 4096000");
            }

            pos += VarInt.size(cooldownIdLen);
            pos += cooldownIdLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading CooldownId");
            }
         }

         if ((nullBits & 2) != 0) {
            int chargeTimesOffset = buffer.getIntLE(offset + 12);
            if (chargeTimesOffset < 0 || chargeTimesOffset > buffer.writerIndex() - offset - 16) {
               return ValidationResult.error("Invalid offset for ChargeTimes");
            }

            int posx = offset + 16 + chargeTimesOffset;
            int chargeTimesCount = VarInt.peek(buffer, posx);
            if (chargeTimesCount < 0) {
               return ValidationResult.error("Invalid array count for ChargeTimes");
            }

            if (chargeTimesCount > 4096000) {
               return ValidationResult.error("ChargeTimes exceeds max length 4096000");
            }

            posx += VarInt.size(chargeTimesCount);
            posx += chargeTimesCount * 4;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading ChargeTimes");
            }
         }

         return ValidationResult.OK;
      }
   }

   public InteractionCooldown clone() {
      InteractionCooldown copy = new InteractionCooldown();
      copy.cooldownId = this.cooldownId;
      copy.cooldown = this.cooldown;
      copy.clickBypass = this.clickBypass;
      copy.chargeTimes = this.chargeTimes != null ? Arrays.copyOf(this.chargeTimes, this.chargeTimes.length) : null;
      copy.skipCooldownReset = this.skipCooldownReset;
      copy.interruptRecharge = this.interruptRecharge;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof InteractionCooldown other)
            ? false
            : Objects.equals(this.cooldownId, other.cooldownId)
               && this.cooldown == other.cooldown
               && this.clickBypass == other.clickBypass
               && Arrays.equals(this.chargeTimes, other.chargeTimes)
               && this.skipCooldownReset == other.skipCooldownReset
               && this.interruptRecharge == other.interruptRecharge;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.cooldownId);
      result = 31 * result + Float.hashCode(this.cooldown);
      result = 31 * result + Boolean.hashCode(this.clickBypass);
      result = 31 * result + Arrays.hashCode(this.chargeTimes);
      result = 31 * result + Boolean.hashCode(this.skipCooldownReset);
      return 31 * result + Boolean.hashCode(this.interruptRecharge);
   }
}
