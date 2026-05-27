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

public class EntityEffectUpdate {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 12;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 12;
   public static final int MAX_SIZE = 16384017;
   @Nonnull
   public EffectOp type = EffectOp.Add;
   public int id;
   public float remainingTime;
   public boolean infinite;
   public boolean debuff;
   @Nullable
   public String statusEffectIcon;

   public EntityEffectUpdate() {
   }

   public EntityEffectUpdate(@Nonnull EffectOp type, int id, float remainingTime, boolean infinite, boolean debuff, @Nullable String statusEffectIcon) {
      this.type = type;
      this.id = id;
      this.remainingTime = remainingTime;
      this.infinite = infinite;
      this.debuff = debuff;
      this.statusEffectIcon = statusEffectIcon;
   }

   public EntityEffectUpdate(@Nonnull EntityEffectUpdate other) {
      this.type = other.type;
      this.id = other.id;
      this.remainingTime = other.remainingTime;
      this.infinite = other.infinite;
      this.debuff = other.debuff;
      this.statusEffectIcon = other.statusEffectIcon;
   }

   @Nonnull
   public static EntityEffectUpdate deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 12) {
         throw ProtocolException.bufferTooSmall("EntityEffectUpdate", 12, buf.readableBytes() - offset);
      } else {
         EntityEffectUpdate obj = new EntityEffectUpdate();
         byte nullBits = buf.getByte(offset);
         obj.type = EffectOp.fromValue(buf.getByte(offset + 1));
         obj.id = buf.getIntLE(offset + 2);
         obj.remainingTime = buf.getFloatLE(offset + 6);
         obj.infinite = buf.getByte(offset + 10) != 0;
         obj.debuff = buf.getByte(offset + 11) != 0;
         int pos = offset + 12;
         if ((nullBits & 1) != 0) {
            int statusEffectIconLen = VarInt.peek(buf, pos);
            if (statusEffectIconLen < 0) {
               throw ProtocolException.invalidVarInt("StatusEffectIcon");
            }

            int statusEffectIconVarLen = VarInt.size(statusEffectIconLen);
            if (statusEffectIconLen > 4096000) {
               throw ProtocolException.stringTooLong("StatusEffectIcon", statusEffectIconLen, 4096000);
            }

            if (pos + statusEffectIconVarLen + statusEffectIconLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("StatusEffectIcon", pos + statusEffectIconVarLen + statusEffectIconLen, buf.readableBytes());
            }

            obj.statusEffectIcon = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
            pos += statusEffectIconVarLen + statusEffectIconLen;
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 12;
      if ((nullBits & 1) != 0) {
         int sl = VarInt.peek(buf, pos);
         pos += VarInt.size(sl) + sl;
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 12L;
   }

   public static EffectOp getType(MemorySegment mem) {
      return getType(mem, 0);
   }

   public static EffectOp getType(MemorySegment mem, int offset) {
      return EffectOp.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1)));
   }

   public static int getId(MemorySegment mem) {
      return getId(mem, 0);
   }

   public static int getId(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 2));
   }

   public static float getRemainingTime(MemorySegment mem) {
      return getRemainingTime(mem, 0);
   }

   public static float getRemainingTime(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 6));
   }

   public static boolean getInfinite(MemorySegment mem) {
      return getInfinite(mem, 0);
   }

   public static boolean getInfinite(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 10));
   }

   public static boolean getDebuff(MemorySegment mem) {
      return getDebuff(mem, 0);
   }

   public static boolean getDebuff(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 11));
   }

   @Nullable
   public static String getStatusEffectIcon(MemorySegment mem) {
      return getStatusEffectIcon(mem, 0);
   }

   @Nullable
   public static String getStatusEffectIcon(MemorySegment mem, int offset) {
      return hasStatusEffectIcon(mem, offset) ? PacketIO.readVarString("StatusEffectIcon", mem, offset + 12, 4096000, PacketIO.UTF8) : null;
   }

   public static boolean hasStatusEffectIcon(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static EntityEffectUpdate toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static EntityEffectUpdate toObject(MemorySegment mem, int offset) {
      if (offset + 12 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("EntityEffectUpdate", offset + 12, (int)mem.byteSize());
      } else {
         return new EntityEffectUpdate(
            EffectOp.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 2)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 6)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 10)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 11)),
            hasStatusEffectIcon(mem, offset) ? PacketIO.readVarString("StatusEffectIcon", mem, offset + 12, 4096000, PacketIO.UTF8) : null
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.statusEffectIcon != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      buf.writeIntLE(this.id);
      buf.writeFloatLE(this.remainingTime);
      buf.writeByte(this.infinite ? 1 : 0);
      buf.writeByte(this.debuff ? 1 : 0);
      if (this.statusEffectIcon != null) {
         PacketIO.writeVarString(buf, this.statusEffectIcon, 4096000);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.statusEffectIcon != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.type.getValue());
      mem.set(PacketIO.PROTO_INT, (long)(offset + 2), this.id);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 6), this.remainingTime);
      mem.set(PacketIO.PROTO_BOOL, offset + 10, this.infinite);
      mem.set(PacketIO.PROTO_BOOL, offset + 11, this.debuff);
      int varOffset = offset + 12;
      if (this.statusEffectIcon != null) {
         varOffset += PacketIO.writeVarString(mem, varOffset, this.statusEffectIcon, 4096000);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 12;
      if (this.statusEffectIcon != null) {
         size += PacketIO.stringSize(this.statusEffectIcon);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 12) {
         return ValidationResult.error("Buffer too small: expected at least 12 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int v = buffer.getByte(offset + 1) & 255;
         if (v >= 2) {
            return ValidationResult.error("Invalid EffectOp value for Type");
         } else {
            v = offset + 12;
            if ((nullBits & 1) != 0) {
               int statusEffectIconLen = VarInt.peek(buffer, v);
               if (statusEffectIconLen < 0) {
                  return ValidationResult.error("Invalid string length for StatusEffectIcon");
               }

               if (statusEffectIconLen > 4096000) {
                  return ValidationResult.error("StatusEffectIcon exceeds max length 4096000");
               }

               v += VarInt.size(statusEffectIconLen);
               v += statusEffectIconLen;
               if (v > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading StatusEffectIcon");
               }
            }

            return ValidationResult.OK;
         }
      }
   }

   public EntityEffectUpdate clone() {
      EntityEffectUpdate copy = new EntityEffectUpdate();
      copy.type = this.type;
      copy.id = this.id;
      copy.remainingTime = this.remainingTime;
      copy.infinite = this.infinite;
      copy.debuff = this.debuff;
      copy.statusEffectIcon = this.statusEffectIcon;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof EntityEffectUpdate other)
            ? false
            : Objects.equals(this.type, other.type)
               && this.id == other.id
               && this.remainingTime == other.remainingTime
               && this.infinite == other.infinite
               && this.debuff == other.debuff
               && Objects.equals(this.statusEffectIcon, other.statusEffectIcon);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.id, this.remainingTime, this.infinite, this.debuff, this.statusEffectIcon);
   }
}
