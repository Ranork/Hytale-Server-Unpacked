package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
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
            throw ProtocolException.negativeLength("StatusEffectIcon", statusEffectIconLen);
         }

         if (statusEffectIconLen > 4096000) {
            throw ProtocolException.stringTooLong("StatusEffectIcon", statusEffectIconLen, 4096000);
         }

         int statusEffectIconVarLen = VarInt.length(buf, pos);
         obj.statusEffectIcon = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
         pos += statusEffectIconVarLen + statusEffectIconLen;
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 12;
      if ((nullBits & 1) != 0) {
         int sl = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos) + sl;
      }

      return pos - offset;
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
         int pos = offset + 12;
         if ((nullBits & 1) != 0) {
            int statusEffectIconLen = VarInt.peek(buffer, pos);
            if (statusEffectIconLen < 0) {
               return ValidationResult.error("Invalid string length for StatusEffectIcon");
            }

            if (statusEffectIconLen > 4096000) {
               return ValidationResult.error("StatusEffectIcon exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += statusEffectIconLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading StatusEffectIcon");
            }
         }

         return ValidationResult.OK;
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
