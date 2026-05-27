package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AbilityEffects {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 4096006;
   @Nullable
   public InteractionType[] disabled;

   public AbilityEffects() {
   }

   public AbilityEffects(@Nullable InteractionType[] disabled) {
      this.disabled = disabled;
   }

   public AbilityEffects(@Nonnull AbilityEffects other) {
      this.disabled = other.disabled;
   }

   @Nonnull
   public static AbilityEffects deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 1) {
         throw ProtocolException.bufferTooSmall("AbilityEffects", 1, buf.readableBytes() - offset);
      } else {
         AbilityEffects obj = new AbilityEffects();
         byte nullBits = buf.getByte(offset);
         int pos = offset + 1;
         if ((nullBits & 1) != 0) {
            int disabledCount = VarInt.peek(buf, pos);
            if (disabledCount < 0) {
               throw ProtocolException.invalidVarInt("Disabled");
            }

            int disabledVarLen = VarInt.size(disabledCount);
            if (disabledCount > 4096000) {
               throw ProtocolException.arrayTooLong("Disabled", disabledCount, 4096000);
            }

            if (pos + disabledVarLen + disabledCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Disabled", pos + disabledVarLen + disabledCount * 1, buf.readableBytes());
            }

            pos += disabledVarLen;
            obj.disabled = new InteractionType[disabledCount];

            for (int i = 0; i < disabledCount; i++) {
               obj.disabled[i] = InteractionType.fromValue(buf.getByte(pos));
               pos++;
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.size(arrLen) + arrLen * 1;
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 1L;
   }

   @Nullable
   public static InteractionType[] getDisabled(MemorySegment mem) {
      return getDisabled(mem, 0);
   }

   @Nullable
   public static InteractionType[] getDisabled(MemorySegment mem, int offset) {
      if (!hasDisabled(mem, offset)) {
         return null;
      } else {
         int off = offset + 1;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Disabled", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Disabled", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Disabled", off + lenOffset + len * 1, (int)mem.byteSize());
            } else {
               off += lenOffset;
               InteractionType[] data = new InteractionType[len];

               for (int i = 0; i < len; i++) {
                  data[i] = InteractionType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(off + i * 1)));
               }

               return data;
            }
         }
      }
   }

   public static boolean hasDisabled(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static AbilityEffects toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static AbilityEffects toObject(MemorySegment mem, int offset) {
      if (offset + 1 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("AbilityEffects", offset + 1, (int)mem.byteSize());
      } else {
         InteractionType[] disabled = null;
         if (hasDisabled(mem, offset)) {
            int off = offset + 1;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Disabled", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Disabled", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Disabled", off + lenOffset + len * 1, (int)mem.byteSize());
            }

            off += lenOffset;
            disabled = new InteractionType[len];

            for (int i = 0; i < len; i++) {
               disabled[i] = InteractionType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(off + i * 1)));
            }
         }

         return new AbilityEffects(disabled);
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.disabled != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.disabled != null) {
         if (this.disabled.length > 4096000) {
            throw ProtocolException.arrayTooLong("Disabled", this.disabled.length, 4096000);
         }

         VarInt.write(buf, this.disabled.length);

         for (InteractionType item : this.disabled) {
            buf.writeByte(item.getValue());
         }
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.disabled != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 1;
      if (this.disabled != null) {
         if (this.disabled.length > 4096000) {
            throw ProtocolException.arrayTooLong("Disabled", this.disabled.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.disabled.length);

         for (int i = 0; i < this.disabled.length; i++) {
            mem.set(PacketIO.PROTO_BYTE, (long)(varOffset + i * 1), (byte)this.disabled[i].getValue());
         }

         varOffset += this.disabled.length * 1;
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 1;
      if (this.disabled != null) {
         size += VarInt.size(this.disabled.length) + this.disabled.length * 1;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 1) {
         return ValidationResult.error("Buffer too small: expected at least 1 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 1;
         if ((nullBits & 1) != 0) {
            int disabledCount = VarInt.peek(buffer, pos);
            if (disabledCount < 0) {
               return ValidationResult.error("Invalid array count for Disabled");
            }

            if (disabledCount > 4096000) {
               return ValidationResult.error("Disabled exceeds max length 4096000");
            }

            pos += VarInt.size(disabledCount);
            if (pos + disabledCount * 1L > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Disabled");
            }

            for (int i = 0; i < disabledCount; i++) {
               int v = buffer.getByte(pos) & 255;
               if (v >= 25) {
                  return ValidationResult.error("Invalid InteractionType value for Disabled[i]");
               }

               pos++;
            }
         }

         return ValidationResult.OK;
      }
   }

   public AbilityEffects clone() {
      AbilityEffects copy = new AbilityEffects();
      copy.disabled = this.disabled != null ? Arrays.copyOf(this.disabled, this.disabled.length) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof AbilityEffects other ? Arrays.equals((Object[])this.disabled, (Object[])other.disabled) : false;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      return 31 * result + Arrays.hashCode((Object[])this.disabled);
   }
}
